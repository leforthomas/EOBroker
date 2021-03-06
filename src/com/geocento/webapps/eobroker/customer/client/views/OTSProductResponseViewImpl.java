package com.geocento.webapps.eobroker.customer.client.views;

import com.geocento.webapps.eobroker.common.client.utils.CategoryUtils;
import com.geocento.webapps.eobroker.common.client.utils.DateUtils;
import com.geocento.webapps.eobroker.common.client.utils.StringUtils;
import com.geocento.webapps.eobroker.common.client.widgets.*;
import com.geocento.webapps.eobroker.common.client.widgets.maps.AoIUtil;
import com.geocento.webapps.eobroker.common.client.widgets.maps.MapContainer;
import com.geocento.webapps.eobroker.common.shared.entities.Category;
import com.geocento.webapps.eobroker.common.shared.entities.dtos.AoIDTO;
import com.geocento.webapps.eobroker.common.shared.entities.dtos.CompanyDTO;
import com.geocento.webapps.eobroker.common.shared.entities.formelements.FormElementValue;
import com.geocento.webapps.eobroker.common.shared.entities.requests.OTSProductRequest;
import com.geocento.webapps.eobroker.common.shared.entities.requests.Request;
import com.geocento.webapps.eobroker.customer.client.ClientFactoryImpl;
import com.geocento.webapps.eobroker.customer.client.Customer;
import com.geocento.webapps.eobroker.customer.client.events.ChangeStatus;
import com.geocento.webapps.eobroker.customer.client.places.FullViewPlace;
import com.geocento.webapps.eobroker.customer.client.places.PlaceHistoryHelper;
import com.geocento.webapps.eobroker.customer.shared.ProductDTO;
import com.geocento.webapps.eobroker.customer.shared.ProductDatasetDTO;
import com.geocento.webapps.eobroker.customer.shared.requests.*;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import gwt.material.design.addins.client.bubble.MaterialBubble;
import gwt.material.design.addins.client.rating.MaterialRating;
import gwt.material.design.client.constants.Color;
import gwt.material.design.client.constants.IconType;
import gwt.material.design.client.constants.Position;
import gwt.material.design.client.ui.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 09/05/2016.
 */
public class OTSProductResponseViewImpl extends Composite implements OTSProductResponseView {

    interface ProductResponseViewUiBinder extends UiBinder<Widget, OTSProductResponseViewImpl> {
    }

    private static ProductResponseViewUiBinder ourUiBinder = GWT.create(ProductResponseViewUiBinder.class);

    static public interface StyleFile extends CssResource {
        String sectionLabel();
    }

    @UiField
    StyleFile style;

    @UiField
    MaterialLabel title;
    @UiField
    protected
    MaterialColumn requestDescription;
    @UiField
    HTMLPanel requestResponse;
    @UiField
    MaterialRow messages;
    @UiField
    protected
    MapContainer mapContainer;
    @UiField
    ProgressButton submitMessage;
    @UiField
    MaterialTextArea message;
    @UiField
    UserWidget userImage;
    @UiField
    MaterialButton status;
    @UiField
    MaterialDropDown statuses;
    @UiField
    MaterialLabel description;
    @UiField
    MaterialRow requestTab;
    @UiField
    MaterialPanel colorPanel;
    @UiField
    MaterialImageLoading image;
    @UiField
    MaterialLabel messagesComment;
    @UiField
    MaterialLabelIcon company;
    @UiField
    MaterialNavBar navigation;

    private ClientFactoryImpl clientFactory;

    private Presenter presenter;

    public OTSProductResponseViewImpl(ClientFactoryImpl clientFactory) {

        this.clientFactory = clientFactory;

        initWidget(ourUiBinder.createAndBindUi(this));

        colorPanel.setBackgroundColor(CategoryUtils.getColor(Category.productdatasets));

        userImage.setUser(Customer.getLoginInfo().getUserName());

    }

    @Override
    public void setMapLoadedHandler(Callback<Void, Exception> mapLoadedHandler) {
        mapContainer.setMapLoadedHandler(mapLoadedHandler);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void addMessage(String userName, boolean isCustomer, String message, Date date) {
        MaterialRow materialRow = new MaterialRow();
        materialRow.setMarginBottom(0);
        messages.add(materialRow);
        Color colour = Color.WHITE;
        UserWidget userWidget = new UserWidget(userName);
        userWidget.setMarginTop(8);
        userWidget.setFloat(isCustomer ? Style.Float.LEFT : Style.Float.RIGHT);
        userWidget.setSize(40);
        materialRow.add(userWidget);
        MaterialBubble materialBubble = new MaterialBubble();
        materialBubble.setBackgroundColor(colour);
        materialBubble.setFloat(isCustomer ? Style.Float.LEFT : Style.Float.RIGHT);
        materialBubble.setPosition(isCustomer ? Position.LEFT : Position.RIGHT);
        if(isCustomer) {
            materialBubble.setMarginLeft(12);
        } else {
            materialBubble.setMarginRight(12);
        }
        materialBubble.setWidth("50%");
        materialRow.add(materialBubble);
        materialBubble.add(new MaterialLabel(message));
        MaterialLabel materialLabel = new MaterialLabel();
        materialLabel.setText(DateUtils.dateFormat.format(date));
        materialLabel.setFloat(Style.Float.RIGHT);
        materialLabel.setFontSize(0.6, Style.Unit.EM);
        materialBubble.add(materialLabel);
        updateMessagesComment();
    }

    protected void displayAoI(AoIDTO aoi) {
        mapContainer.displayAoI(aoi);
        mapContainer.centerOnAoI();
    }

    protected void addRequestValue(String name, String value) {
        this.requestDescription.add(new HTMLPanel("<p style='padding: 0.5rem;'><b>" + name + ":</b> " +
                (value == null ? "not provided" : value) + "</p>"));
    }

    protected void displayResponse(String response) {
        requestResponse.clear();
        if(StringUtils.isEmpty(response)) {
            MaterialLabel materialLabel = new MaterialLabel("This supplier hasn't provided an offer yet...");
            materialLabel.setMargin(20);
            materialLabel.setTextColor(Color.GREY);
            requestResponse.add(materialLabel);
        } else {
            requestResponse.add(new HTML(response));
        }
    }

    protected void displayMessages(List<MessageDTO> messages) {
        this.messages.clear();
        this.message.setText("");
        String userName = Customer.getLoginInfo().getUserName();
        if(messages != null && messages.size() > 0) {
            for (MessageDTO messageDTO : messages) {
                boolean isCustomer = !userName.contentEquals(messageDTO.getFrom());
                addMessage(messageDTO.getFrom(),
                        isCustomer, messageDTO.getMessage(), messageDTO.getCreationDate());
            }
        }
        updateMessagesComment();
    }

    private void updateMessagesComment() {
        boolean hasMessages = messages.getWidgetCount() > 0;
        messagesComment.setVisible(!hasMessages);
        if(!hasMessages) {
            messagesComment.setText("No messages yet...");
            message.setPlaceholder("Start a conversation...");
        } else {
            message.setPlaceholder("Reply...");
        }
    }

    protected void setStatus(Request.STATUS status) {
        statuses.clear();
        if(status == null) {
            this.status.setText("Submitted");
            this.status.setEnabled(false);
        } else {
            this.status.setText(status.toString());
            this.status.setEnabled(false);
            switch (status) {
                case submitted:
                    this.status.setEnabled(true);
                {
                    MaterialLink materialLink = new MaterialLink("Cancel");
                    materialLink.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            clientFactory.getEventBus().fireEvent(new ChangeStatus(Request.STATUS.cancelled));
                        }
                    });
                    statuses.add(materialLink);
                }
                {
                    MaterialLink materialLink = new MaterialLink("Complete");
                    materialLink.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            clientFactory.getEventBus().fireEvent(new ChangeStatus(Request.STATUS.completed));
                        }
                    });
                    statuses.add(materialLink);
                }
                break;
            }
        }
    }

    @Override
    public HasClickHandlers getSubmitMessage() {
        return submitMessage;
    }

    @Override
    public HasText getMessageText() {
        return message;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void displayProductResponse(OTSProductResponseDTO otsProductResponseDTO) {
        this.requestDescription.clear();
        ProductDatasetDTO productDataset = otsProductResponseDTO.getProductDataset();
        CompanyDTO companyDTO = productDataset.getCompany();
        navigation.clear();
        navigation.setBackgroundColor(CategoryUtils.getColor(Category.productdatasets));
        addBreadcrumb(companyDTO);
        addBreadcrumb(otsProductResponseDTO.getProduct());
        title.setText("Off the shelf product request '" + productDataset.getName() + "'");
        image.setImageUrl(productDataset.getImageUrl());
        description.setText("Off the shelf product request #" + otsProductResponseDTO.getId() + " " +
                "requested on " + otsProductResponseDTO.getCreationTime().toString() +
                " for " + otsProductResponseDTO.getSelection().split(OTSProductRequest.selectionSeparator).length + " products");
        company.setImageUrl(companyDTO.getIconURL());
        company.setText("From company " + companyDTO.getName());
        setStatus(otsProductResponseDTO.getStatus());
        addRequestValue("Your comments", otsProductResponseDTO.getComments());
        String productSelection = otsProductResponseDTO.getSelection();
        int index = 1;
        List<String> htmlValues = new ArrayList<String>();
        for(String productId : productSelection.split(OTSProductRequest.selectionSeparator)) {
            htmlValues.add(productId.startsWith("http") ?
                    "<a href='" + productId + "' target='_blank;'>product #" + index++ + "</a>" :
                    productId);
        }
        addRequestValue("Products ID selection", com.geocento.webapps.eobroker.common.shared.utils.StringUtils.join(htmlValues, ", "));
        if(otsProductResponseDTO.getFormValues().size() == 0) {
            //this.requestDescription.add(new HTMLPanel("<p>No search parameters provided</p>"));
        } else {
            this.requestDescription.add(new HTMLPanel("<p>Initial search parameters</p>"));
            for (FormElementValue formElementValue : otsProductResponseDTO.getFormValues()) {
                addRequestValue(formElementValue.getName(), formElementValue.getValue());
            }
        }
        displayAoI(AoIUtil.fromWKT(otsProductResponseDTO.getAoIWKT()));
        if(otsProductResponseDTO.getFormValues().size() == 0) {
            //this.requestDescription.add(new HTMLPanel("<p>No data provided</p>"));
        } else {
            for (FormElementValue formElementValue : otsProductResponseDTO.getFormValues()) {
                addRequestValue(formElementValue.getName(), formElementValue.getValue());
            }
        }
        // now add the responses
        displayResponse(otsProductResponseDTO.getResponse());
        displayMessages(otsProductResponseDTO.getMessages());
    }

    private void addBreadcrumb(Object dto) {
        navigation.setVisible(true);
        MaterialBreadcrumb materialBreadcrumb = new MaterialBreadcrumb();
        Color color = Color.WHITE; //CategoryUtils.getColor(category);
        materialBreadcrumb.setTextColor(color);
        materialBreadcrumb.setIconColor(color);
        String token = "";
        IconType iconType = IconType.ERROR;
        String text = "Unknown";
        String id = null;
        if(dto instanceof CompanyDTO) {
            token = FullViewPlace.TOKENS.companyid.toString();
            iconType = CategoryUtils.getIconType(Category.companies);
            text = ((CompanyDTO) dto).getName();
            id = ((CompanyDTO) dto).getId() + "";
        } else if(dto instanceof ProductDTO) {
            token = FullViewPlace.TOKENS.productid.toString();
            iconType = CategoryUtils.getIconType(Category.products);
            text = ((ProductDTO) dto).getName();
            id = ((ProductDTO) dto).getId() + "";
        }
        materialBreadcrumb.setIconType(iconType);
        materialBreadcrumb.setText(text);
        materialBreadcrumb.setHref("#" + PlaceHistoryHelper.convertPlace(new FullViewPlace(token + "=" + id)));
        navigation.add(materialBreadcrumb);
    }

}