package com.geocento.webapps.eobroker.customer.client.activities;

import com.geocento.webapps.eobroker.common.client.utils.Utils;
import com.geocento.webapps.eobroker.common.client.widgets.maps.AoIUtil;
import com.geocento.webapps.eobroker.common.shared.Suggestion;
import com.geocento.webapps.eobroker.common.shared.entities.Category;
import com.geocento.webapps.eobroker.common.shared.entities.ImageService;
import com.geocento.webapps.eobroker.common.shared.entities.SearchQuery;
import com.geocento.webapps.eobroker.common.shared.entities.dtos.AoIDTO;
import com.geocento.webapps.eobroker.common.shared.entities.requests.RequestDTO;
import com.geocento.webapps.eobroker.common.shared.imageapi.Product;
import com.geocento.webapps.eobroker.customer.client.ClientFactory;
import com.geocento.webapps.eobroker.customer.client.events.*;
import com.geocento.webapps.eobroker.customer.client.places.ImageSearchPlace;
import com.geocento.webapps.eobroker.customer.client.services.ServicesUtil;
import com.geocento.webapps.eobroker.customer.client.views.ImageSearchView;
import com.geocento.webapps.eobroker.customer.shared.ImagesRequestDTO;
import com.geocento.webapps.eobroker.customer.shared.ProductDTO;
import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import gwt.material.design.client.ui.MaterialToast;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomas on 09/05/2016.
 */
public class ImageSearchActivity extends TemplateActivity implements ImageSearchView.Presenter {

    private ImageSearchView imageSearchView;
    private String sensors;
    private Date startDate;
    private Date stopDate;
    private String currency;
    private long lastCall = 0;

    public Suggestion selectedSuggestion;

    private List<ImageService> imageServices;

    private ImageService selectedImageService;

    public ImageSearchActivity(ImageSearchPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        imageSearchView = clientFactory.getImageSearchView();
        imageSearchView.setPresenter(this);
        setTemplateView(imageSearchView.asWidget());
        Window.setTitle("Earth Observation Broker");
        bind();
        imageSearchView.showQuery();
        imageSearchView.setMapLoadedHandler(new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {
                Window.alert("Error " + reason.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                try {
                    REST.withCallback(new MethodCallback<List<ImageService>>() {
                        @Override
                        public void onFailure(Method method, Throwable exception) {

                        }

                        @Override
                        public void onSuccess(Method method, List<ImageService> imageServices) {
                            ImageSearchActivity.this.imageServices = imageServices;
                            imageSearchView.setSuppliers(imageServices);
                            selectService(imageServices.get(0));
                            handleHistory();
                        }
                    }).call(ServicesUtil.assetsService).getImageServices();
                } catch (RequestException e) {
                }
            }
        });
    }

    private void handleHistory() {
        HashMap<String, String> tokens = Utils.extractTokens(place.getToken());
        final String text = tokens.get(ImageSearchPlace.TOKENS.text.toString());
        Long productId = null;
        if (tokens.containsKey(ImageSearchPlace.TOKENS.product.toString())) {
            try {
                productId = Long.parseLong(tokens.get(ImageSearchPlace.TOKENS.product.toString()));
            } catch (Exception e) {

            }
        }
        imageSearchView.clearMap();
        setAoi(currentAoI);
        imageSearchView.centerOnAoI();
        Date now = new Date();
        setStartDate(new Date(now.getTime() - 10 * 24 * 3600 * 1000));
        setStopDate(now);
        setCurrency("EUR");
        if(productId != null) {
            try {
                REST.withCallback(new MethodCallback<ProductDTO>() {
                    @Override
                    public void onFailure(Method method, Throwable exception) {

                    }

                    @Override
                    public void onSuccess(Method method, ProductDTO productDTO) {
                        setSensors("Suitable for '" + productDTO.getName() + "'");
                        if(currentAoI != null && text != null && text.length() > 0) {
                            updateSearch();
                        }
                    }
                }).call(ServicesUtil.assetsService).getProduct(productId);
            } catch (RequestException e) {
                e.printStackTrace();
            }
        } else {
            setSensors(text);
            if(currentAoI != null && text != null && text.length() > 0) {
                updateSearch();
            }
        }
    }

    private void selectService(ImageService imageService) {
        this.selectedImageService = imageService;
        imageSearchView.displayService(imageService);
    }

    private void setSensors(String sensors) {
        this.sensors = sensors;
        imageSearchView.displaySensors(sensors);
    }

    private void setStartDate(Date startDate) {
        this.startDate = startDate;
        imageSearchView.displayStartDate(startDate);
    }

    private void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
        imageSearchView.displayStopDate(stopDate);
    }

    @Override
    protected void bind() {
        super.bind();

        handlers.add(imageSearchView.getUpdateButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateSearch();
            }
        }));

        handlers.add(imageSearchView.getQuoteButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                List<Product> products = imageSearchView.getSelectedProducts();
                ImagesRequestDTO imagesRequestDTO = new ImagesRequestDTO();
                imagesRequestDTO.setImageServiceId(selectedImageService.getId());
                imagesRequestDTO.setAoiWKT(AoIUtil.toWKT(currentAoI));
                imagesRequestDTO.setProducts(products);
                try {
                    REST.withCallback(new MethodCallback<RequestDTO>() {

                        @Override
                        public void onFailure(Method method, Throwable exception) {
                            Window.alert("Could not save image request");
                        }

                        @Override
                        public void onSuccess(Method method, RequestDTO imageOrder) {
                            activityEventBus.fireEvent(new RequestCreated(imageOrder));
                            // clean the selections
                            imageSearchView.clearProductsSelection();
                            displaySuccess("Quotation request submitted");
                        }
                    }).call(ServicesUtil.requestsService).submitImagesRequest(imagesRequestDTO);
                } catch (RequestException e) {
                }
            }
        }));

        activityEventBus.addHandler(TextSelected.TYPE, new TextSelectedHandler() {
            @Override
            public void onTextSelected(TextSelected event) {
                // TODO - correct the input if needed
                imageSearchView.setText(event.getText());
            }
        });
        
        activityEventBus.addHandler(SuggestionSelected.TYPE, new SuggestionSelectedHandler() {

            @Override
            public void onSuggestionSelected(SuggestionSelected event) {
                imageSearchView.setText(event.getSuggestion().getName());
                selectedSuggestion = event.getSuggestion();
                imageSearchView.setSearchTextValid(true);
            }
        });
    }

    private void updateSuggestions() {
        this.lastCall++;
        final long lastCall = this.lastCall;

        if(sensors != null && sensors.length() > 0) {
            REST.withCallback(new MethodCallback<List<Suggestion>>() {

                @Override
                public void onFailure(Method method, Throwable exception) {

                }

                @Override
                public void onSuccess(Method method, List<Suggestion> response) {
                    // show only if last one to be called
                    if (lastCall == ImageSearchActivity.this.lastCall) {
                        imageSearchView.displaySensorSuggestions(response);
                    }
                }
            }).call(ServicesUtil.searchService).completeSensors(sensors);
        }
    }

    private void updateSearch() {
        if(currentAoI == null) {
            MaterialToast.fireToast("Please select an area of interest");
            return;
        }
        if(sensors == null || sensors.length() == 0) {
            MaterialToast.fireToast("Please select sensors");
            return;
        }
        // check current suggestion
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setAoiWKT(currentAoI.getWktGeometry());
        if(selectedSuggestion != null) {
            if(selectedSuggestion.getCategory() == Category.imagery) {
                searchQuery.setSensors(selectedSuggestion.getName());
            } else {
                searchQuery.setProduct(Long.parseLong(selectedSuggestion.getUri().replace("access::", "")));
            }
        } else {
            searchQuery.setSensors(sensors);
        }
        searchQuery.setStart((long) (startDate.getTime() / 1000.0));
        searchQuery.setStop((long) (stopDate.getTime() / 1000.0));
        imageSearchView.clearMap();
        imageSearchView.displayAoI(currentAoI);
        imageSearchView.displayLoadingResults("Searching products...");
        enableUpdate(false);
        try {
            REST.withCallback(new MethodCallback<List<Product>>() {
                @Override
                public void onFailure(Method method, Throwable exception) {
                    Window.alert("Error");
                }

                @Override
                public void onSuccess(Method method, List<Product> imageProductDTOs) {
                    imageSearchView.hideLoadingResults();
                    // add all results to the interface
                    imageSearchView.displayImageProducts(imageProductDTOs);
                }
            }).call(ServicesUtil.searchService).queryImages(searchQuery);
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void aoiChanged(AoIDTO aoi) {
        setAoi(aoi);
    }

    @Override
    public void onProviderChanged(ImageService imageService) {
        selectService(imageService);
    }

    @Override
    public void onStartDateChanged(Date value) {
        startDate = value;
    }

    private void enableUpdate(boolean enable) {
        // always enabled
        imageSearchView.enableUpdate(true);
    }

    @Override
    public void onStopDateChanged(Date value) {
        stopDate = value;
    }

    @Override
    public void onSensorsChanged(String value) {
        sensors = value;
        selectedSuggestion = null;
        imageSearchView.setSearchTextValid(false);
        updateSuggestions();
    }

    public void setAoi(AoIDTO aoi) {
        super.setAoI(aoi);
        imageSearchView.displayAoI(aoi);
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }
}
