package com.geocento.webapps.eobroker.customer.client.activities;

import com.geocento.webapps.eobroker.common.client.utils.Utils;
import com.geocento.webapps.eobroker.common.client.widgets.maps.AoIUtil;
import com.geocento.webapps.eobroker.common.shared.entities.dtos.AoIDTO;
import com.geocento.webapps.eobroker.common.shared.entities.formelements.FormElement;
import com.geocento.webapps.eobroker.common.shared.entities.formelements.FormElementValue;
import com.geocento.webapps.eobroker.common.shared.feasibility.FeasibilityResponse;
import com.geocento.webapps.eobroker.common.shared.utils.ListUtil;
import com.geocento.webapps.eobroker.customer.client.ClientFactory;
import com.geocento.webapps.eobroker.customer.client.places.ConversationPlace;
import com.geocento.webapps.eobroker.customer.client.places.PlaceHistoryHelper;
import com.geocento.webapps.eobroker.customer.client.places.ProductFeasibilityPlace;
import com.geocento.webapps.eobroker.customer.client.places.ProductFormPlace;
import com.geocento.webapps.eobroker.customer.client.services.ServicesUtil;
import com.geocento.webapps.eobroker.customer.client.views.ProductFeasibilityView;
import com.geocento.webapps.eobroker.customer.shared.FeasibilityRequestDTO;
import com.geocento.webapps.eobroker.customer.shared.FeasibilityResponseDTO;
import com.geocento.webapps.eobroker.customer.shared.ProductFeasibilityDTO;
import com.geocento.webapps.eobroker.customer.shared.ProductServiceFeasibilityDTO;
import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import gwt.material.design.client.ui.MaterialToast;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomas on 09/05/2016.
 */
public class ProductFeasibilityActivity extends TemplateActivity implements ProductFeasibilityView.Presenter {

    private ProductFeasibilityView productFeasibilityView;

    private ProductFeasibilityDTO productFeasibilityDTO;
    private ProductServiceFeasibilityDTO productFeasibilityService;
    private Date start;
    private Date stop;
    private List<FormElementValue> formElementValues;

    // current response
    private FeasibilityResponseDTO feasibilityResponseDTO;

    public ProductFeasibilityActivity(ProductFeasibilityPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        productFeasibilityView = clientFactory.getProductFeasibilityView();
        productFeasibilityView.setPresenter(this);
        setTemplateView(productFeasibilityView.asWidget());
        displayMenu(false);
        Window.setTitle("Earth Observation Broker");
        setTitleText("Feasibility study");
        bind();
        productFeasibilityView.showQuery();
        displayFullLoading("Loading map library...");
        productFeasibilityView.setMapLoadedHandler(new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {
                hideFullLoading();
                Window.alert("Error " + reason.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                hideFullLoading();
                handleHistory();
            }
        });
    }

    private void handleHistory() {
        HashMap<String, String> tokens = Utils.extractTokens(place.getToken());
        Long productServiceId = null;
        if (tokens.containsKey(ProductFeasibilityPlace.TOKENS.productservice.toString())) {
            try {
                productServiceId = Long.parseLong(tokens.get(ProductFeasibilityPlace.TOKENS.productservice.toString()));
            } catch (Exception e) {

            }
        }
        if(productServiceId == null) {
            Window.alert("Missing bespoke service");
            History.back();
        }
        Long startDate = null;
        if (tokens.containsKey(ProductFeasibilityPlace.TOKENS.startDate.toString())) {
            try {
                startDate = Long.parseLong(tokens.get(ProductFeasibilityPlace.TOKENS.productservice.toString()));
                setStart(new Date(startDate));
            } catch (Exception e) {

            }
        }
        if(startDate != null) {
            setStart(new Date(startDate));
        } else {
            setStart(new Date(new Date().getTime() + 3 * 24 * 3600 * 1000));
        }
        Long stopDate = null;
        if (tokens.containsKey(ProductFeasibilityPlace.TOKENS.stopDate.toString())) {
            try {
                stopDate = Long.parseLong(tokens.get(ProductFeasibilityPlace.TOKENS.stopDate.toString()));
            } catch (Exception e) {

            }
        }
        if(stopDate != null) {
            setStop(new Date(stopDate));
        } else {
            setStop(new Date(start.getTime() + 10 * 24 * 3600 * 1000L));
        }
        loadProduct(productServiceId);
        setAoI(currentAoI);
        productFeasibilityView.centerOnAoI();
        enableUpdateMaybe();
    }

    public void setAoI(AoIDTO aoi) {
        super.setAoI(aoi);
        productFeasibilityView.displayAoI(aoi);
    }

    private void loadProduct(final Long productServiceId) {
        displayFullLoading("Loading bespoke service information...");
        try {
            REST.withCallback(new MethodCallback<ProductFeasibilityDTO>() {
                @Override
                public void onFailure(Method method, Throwable exception) {
                    hideFullLoading();
                    displayError(method.getResponse().getText());
                }

                @Override
                public void onSuccess(Method method, ProductFeasibilityDTO response) {
                    ProductFeasibilityActivity.this.productFeasibilityDTO = response;
                    hideFullLoading();
                    List<FormElement> formElements = new ArrayList<FormElement>();
                    formElements.addAll(response.getApiFormElements());
                    productFeasibilityView.setFormElements(formElements);
                    selectService(ListUtil.findValue(response.getProductServices(), new ListUtil.CheckValue<ProductServiceFeasibilityDTO>() {
                        @Override
                        public boolean isValue(ProductServiceFeasibilityDTO value) {
                            return value.getId().equals(productServiceId);
                        }
                    }));
                }
            }).call(ServicesUtil.assetsService).getProductFeasibility(productServiceId);
        } catch (RequestException e) {
        }
    }

    private void selectService(final ProductServiceFeasibilityDTO productServiceFeasibilityDTO) {
        this.productFeasibilityService = productServiceFeasibilityDTO;
        productFeasibilityView.selectService(productServiceFeasibilityDTO);
        productFeasibilityView.setOtherServices(ListUtil.filterValues(productFeasibilityDTO.getProductServices(), new ListUtil.CheckValue<ProductServiceFeasibilityDTO>() {
            @Override
            public boolean isValue(ProductServiceFeasibilityDTO value) {
                return value != productFeasibilityService;
            }
        }));
    }

    public void setStart(Date start) {
        this.start = start;
        productFeasibilityView.setStart(start);
    }

    public void setStop(Date stop) {
        this.stop = stop;
        productFeasibilityView.setStop(stop);
    }

    @Override
    protected void bind() {
        super.bind();
        handlers.add(productFeasibilityView.getUpdateButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                productFeasibilityView.clearResults();
                // create request
                FeasibilityRequestDTO feasibilityRequestDTO = new FeasibilityRequestDTO();
                feasibilityRequestDTO.setProductServiceId(productFeasibilityService.getId());
                // make sure AoI is not being edited first
                productFeasibilityView.validateAoI();
                feasibilityRequestDTO.setAoiWKT(AoIUtil.toWKT(currentAoI));
                feasibilityRequestDTO.setStart(start);
                feasibilityRequestDTO.setStop(stop);
                try {
                    feasibilityRequestDTO.setFormElementValues(productFeasibilityView.getFormElementValues());
                } catch (Exception e) {
                    displayError("Issue with parameters, reason is " + e.getMessage());
                    return;
                }
                try {
                    productFeasibilityView.displayLoadingResults("Checking feasibility...");
                    REST.withCallback(new MethodCallback<FeasibilityResponseDTO>() {
                        @Override
                        public void onFailure(Method method, Throwable exception) {
                            productFeasibilityView.hideLoadingResults();
                            productFeasibilityView.displayResultsError(method.getResponse().getText());
                        }

                        @Override
                        public void onSuccess(Method method, FeasibilityResponseDTO feasibilityResponseDTO) {
                            ProductFeasibilityActivity.this.feasibilityResponseDTO = feasibilityResponseDTO;
                            FeasibilityResponse feasibilityResponse = feasibilityResponseDTO.getFeasibilityResponse();
                            History.newItem(PlaceHistoryHelper.convertPlace(new ProductFeasibilityPlace(
                                    Utils.generateTokens(
                                            ProductFeasibilityPlace.TOKENS.productservice.toString(), productFeasibilityService.getId() + "",
                                            ProductFeasibilityPlace.TOKENS.feasibilityId.toString(), feasibilityResponseDTO.getSearchId()
                                    )
                            )), false);
                            productFeasibilityView.hideLoadingResults();
                            if(feasibilityResponse.getStatusCode() > 299) {
                                productFeasibilityView.displayResultsError(feasibilityResponse.getStatusMessage());
                                return;
                            }
                            productFeasibilityView.displayResponse(feasibilityResponse);
                        }
                    }).call(ServicesUtil.searchService).callSupplierAPI(feasibilityRequestDTO);
                } catch (Exception e) {

                }
            }
        }));

        handlers.add(productFeasibilityView.getRequestButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clientFactory.getPlaceController().goTo(new ProductFormPlace(
                        ProductFormPlace.TOKENS.searchId.toString() + "=" + feasibilityResponseDTO.getSearchId())
                );
            }
        }));
        productFeasibilityView.getContactButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clientFactory.getPlaceController().goTo(new ConversationPlace(
                        Utils.generateTokens(
                                ConversationPlace.TOKENS.companyid.toString(), productFeasibilityService.getCompany().getId() + "",
                                ConversationPlace.TOKENS.topic.toString(), "Information on service '" + productFeasibilityService.getName() + "'")));
            }
        });
    }

    @Override
    public void aoiChanged(AoIDTO aoi) {
        setAoI(aoi);
        enableUpdateMaybe();
    }

    private void enableUpdateMaybe() {
        if(currentAoI == null) {
            MaterialToast.fireToast("Please select AoIDTO");
            enableUpdate(false);
            return;
        }
        if(start == null || stop == null) {
            MaterialToast.fireToast("Please select start and stop dates");
            enableUpdate(false);
            return;
        }
        enableUpdate(true);
    }

    private void enableUpdate(boolean enable) {
        productFeasibilityView.enableUpdate(enable);
    }

    @Override
    public void onServiceChanged(ProductServiceFeasibilityDTO productServiceFeasibilityDTO) {
        clientFactory.getPlaceController().goTo(new ProductFeasibilityPlace(Utils.generateTokens(ProductFeasibilityPlace.TOKENS.productservice.toString(), productServiceFeasibilityDTO.getId() + "")));
    }

    @Override
    public void onStartDateChanged(Date start) {
        this.start = start;
        enableUpdateMaybe();
    }

    @Override
    public void onStopDateChanged(Date stop) {
        this.stop = stop;
        enableUpdateMaybe();
    }

    @Override
    public void onFormElementChanged(final FormElement formElement) {
        // needs updating?
/*
        ListUtil.findValue(formElementValues, new ListUtil.CheckValue<FormElementValue>() {
            @Override
            public boolean isValue(FormElementValue value) {
                return formElement.getFormid().contentEquals(value.getFormid());
            }
        });
*/
        enableUpdateMaybe();
    }
}
