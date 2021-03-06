package com.geocento.webapps.eobroker.customer.client.activities;

import com.geocento.webapps.eobroker.common.client.widgets.maps.AoIUtil;
import com.geocento.webapps.eobroker.common.shared.entities.ImageService;
import com.geocento.webapps.eobroker.common.shared.entities.dtos.AoIDTO;
import com.geocento.webapps.eobroker.common.shared.entities.requests.RequestDTO;
import com.geocento.webapps.eobroker.common.shared.utils.ListUtil;
import com.geocento.webapps.eobroker.customer.client.ClientFactory;
import com.geocento.webapps.eobroker.customer.client.events.RequestCreated;
import com.geocento.webapps.eobroker.customer.client.places.RequestImageryPlace;
import com.geocento.webapps.eobroker.customer.client.services.ServicesUtil;
import com.geocento.webapps.eobroker.customer.client.views.RequestImageryView;
import com.geocento.webapps.eobroker.customer.shared.ImageRequestDTO;
import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 09/05/2016.
 */
public class RequestImageryActivity extends TemplateActivity implements RequestImageryView.Presenter {

    private RequestImageryView requestImageryView;

    public RequestImageryActivity(RequestImageryPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        requestImageryView = clientFactory.getRequestImageryView();
        requestImageryView.setPresenter(this);
        setTemplateView(requestImageryView.asWidget());
        selectMenu("requests");
        Window.setTitle("Earth Observation Broker");
        bind();
        displayFullLoading("Loading map...");
        requestImageryView.setMapLoadedHandler(new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {
                Window.alert("Failed to load map");
            }

            @Override
            public void onSuccess(Void result) {
                hideFullLoading();
                displayFullLoading("Loading image services...");
                try {
                    REST.withCallback(new MethodCallback<List<ImageService>>() {
                        @Override
                        public void onFailure(Method method, Throwable exception) {
                            Window.alert("Failed to load image services...");
                        }

                        @Override
                        public void onSuccess(Method method, List<ImageService> imageServices) {
                            hideFullLoading();
                            requestImageryView.setSuppliers(imageServices);
                            handleHistory();
                        }
                    }).call(ServicesUtil.assetsService).getImageServices();
                } catch (RequestException e) {
                }
            }
        });
    }

    private void handleHistory() {
        setAoI(currentAoI);
        clearRequest();
    }

    public void setAoI(AoIDTO aoi) {
        super.setAoI(aoi);
        requestImageryView.displayAoI(aoi);
    }

    @Override
    protected void bind() {
        super.bind();

        handlers.add(requestImageryView.getSubmitButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                submitRequest();
            }
        }));
   }

    private void submitRequest() {
        String imageType = requestImageryView.getImageType();
        if(imageType.length() == 0) {
            requestImageryView.displayFormError("Please provide a description of the imagery type");
            return;
        }
        Date start = requestImageryView.getStartDate();
        Date stop = requestImageryView.getStopDate();
        if(start == null || stop == null) {
            requestImageryView.displayFormError("Please provide a start and stop time");
            return;
        }
        String application = requestImageryView.getApplication();
        String additionalInformation = requestImageryView.getAdditionalInformation();
        List<ImageService> imageServices = requestImageryView.getSelectedServices();
        if(imageServices.size() == 0) {
            requestImageryView.displayFormError("Please select at least one supplier service");
            return;
        }
        ImageRequestDTO imageRequestDTO = new ImageRequestDTO();
        if(currentAoI == null) {
            requestImageryView.displayFormError("Please define an AoI first");
            return;
        }
        imageRequestDTO.setAoiWKT(AoIUtil.toWKT(currentAoI));
        imageRequestDTO.setImageType(imageType);
        imageRequestDTO.setStart(start);
        imageRequestDTO.setStop(stop);
        imageRequestDTO.setApplication(application);
        imageRequestDTO.setAdditionalInformation(additionalInformation);
        imageRequestDTO.setImageServiceIds(ListUtil.mutate(imageServices, new ListUtil.Mutate<ImageService, Long>() {
            @Override
            public Long mutate(ImageService imageService) {
                return imageService.getId();
            }
        }));
        try {
            requestImageryView.displaySubmitLoading(true);
            REST.withCallback(new MethodCallback<RequestDTO>() {
                @Override
                public void onFailure(Method method, Throwable exception) {
                    requestImageryView.displaySubmitLoading(false);
                    Window.alert("Could not submit image request");
                }

                @Override
                public void onSuccess(Method method, RequestDTO requestDTO) {
                    requestImageryView.displaySubmitLoading(false);
                    displaySuccess("Request submitted");
                    clientFactory.getEventBus().fireEvent(new RequestCreated(requestDTO));
                    clearRequest();
                }
            }).call(ServicesUtil.requestsService).submitImageRequest(imageRequestDTO);
        } catch (RequestException e) {
        }
    }

    private void clearRequest() {
        requestImageryView.clearRequest();
    }

    @Override
    public void aoiChanged(AoIDTO aoi) {
        setAoI(aoi);
    }
}
