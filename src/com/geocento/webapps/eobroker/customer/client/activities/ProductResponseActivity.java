package com.geocento.webapps.eobroker.customer.client.activities;

import com.geocento.webapps.eobroker.common.client.utils.Utils;
import com.geocento.webapps.eobroker.common.shared.utils.ListUtil;
import com.geocento.webapps.eobroker.customer.client.ClientFactory;
import com.geocento.webapps.eobroker.customer.client.Customer;
import com.geocento.webapps.eobroker.customer.client.places.ProductResponsePlace;
import com.geocento.webapps.eobroker.customer.client.services.ServicesUtil;
import com.geocento.webapps.eobroker.customer.client.views.ProductResponseView;
import com.geocento.webapps.eobroker.customer.shared.requests.MessageDTO;
import com.geocento.webapps.eobroker.customer.shared.requests.ProductServiceResponseDTO;
import com.geocento.webapps.eobroker.customer.shared.requests.ProductServiceSupplierResponseDTO;
import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import java.util.HashMap;

/**
 * Created by thomas on 09/05/2016.
 */
public class ProductResponseActivity extends TemplateActivity implements ProductResponseView.Presenter {

    private ProductResponseView productResponseView;

    private ProductServiceResponseDTO request;
    private ProductServiceSupplierResponseDTO selectedResponse;

    public ProductResponseActivity(ProductResponsePlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        productResponseView = clientFactory.getProductResponseView();
        productResponseView.setPresenter(this);
        setTemplateView(productResponseView.asWidget());
        Window.setTitle("Earth Observation Broker");
        bind();
        displayFullLoading("Loading map...");
        productResponseView.setMapLoadedHandler(new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {
                Window.alert("Failed to load map");
                hideFullLoading();
                handleHistory();
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

        String id = tokens.get(ProductResponsePlace.TOKENS.id.toString());
        if(id == null) {
            Window.alert("Request id cannot be null");
            History.back();
        }
        Long responseId = null;
        if(tokens.containsKey(ProductResponsePlace.TOKENS.responseid.toString())) {
            try {
                responseId = Long.valueOf(tokens.get(ProductResponsePlace.TOKENS.responseid.toString()));
            } catch (Exception e) {
            }
        }

        loadResponse(id, responseId);
    }

    private void loadResponse(String id, final Long responseId) {
        try {
            displayFullLoading("Loading response...");
            REST.withCallback(new MethodCallback<ProductServiceResponseDTO>() {
                @Override
                public void onFailure(Method method, Throwable exception) {
                    hideFullLoading();
                    Window.alert("Failed to load response");
                }

                @Override
                public void onSuccess(Method method, ProductServiceResponseDTO productServiceResponseDTO) {
                    hideFullLoading();
                    request = productServiceResponseDTO;
                    productResponseView.displayTitle("Viewing your product request '" + productServiceResponseDTO.getId() + "'");
                    productResponseView.displayComment("See below your request and the suppliers' responses");
                    productResponseView.displayProductRequest(productServiceResponseDTO);
                    if(responseId == null) {
                        selectedResponse = null; //request.getSupplierResponses().get(0);
                    } else {
                        selectedResponse = ListUtil.findValue(productServiceResponseDTO.getSupplierResponses(), new ListUtil.CheckValue<ProductServiceSupplierResponseDTO>() {
                            @Override
                            public boolean isValue(ProductServiceSupplierResponseDTO value) {
                                return value.getId().longValue() == responseId;
                            }
                        });
                    }
                    selectResponse(selectedResponse);
                }
            }).call(ServicesUtil.requestsService).getProductResponse(id);
        } catch (Exception e) {

        }
    }

    private void selectResponse(ProductServiceSupplierResponseDTO selectedResponse) {
        this.selectedResponse = selectedResponse;
        productResponseView.displayProductResponse(selectedResponse);
    }

    @Override
    protected void bind() {
        super.bind();

        handlers.add(productResponseView.getSubmitMessage().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                displayLoading();
                try {
                    REST.withCallback(new MethodCallback<MessageDTO>() {
                        @Override
                        public void onFailure(Method method, Throwable exception) {
                            hideLoading();
                            displayError(exception.getMessage());
                        }

                        @Override
                        public void onSuccess(Method method, MessageDTO response) {
                            hideLoading();
                            addMessage(response);
                            productResponseView.getMessageText().setText("");
                        }
                    }).call(ServicesUtil.requestsService).addProductResponseMessage(selectedResponse.getId(), productResponseView.getMessageText().getText());
                } catch (RequestException e) {
                    e.printStackTrace();
                }
            }
        }));

    }

    private void addMessage(MessageDTO messageDTO) {
        String userName = Customer.getLoginInfo().getUserName();
        boolean isCustomer = !userName.contentEquals(messageDTO.getFrom());
        productResponseView.addMessage(messageDTO.getFrom(),
                isCustomer, messageDTO.getMessage(), messageDTO.getCreationDate());
    }

    @Override
    public void responseSelected(ProductServiceSupplierResponseDTO productServiceSupplierResponseDTO) {
        selectResponse(productServiceSupplierResponseDTO);
    }
}
