package com.geocento.webapps.eobroker.customer.client.views;

import com.geocento.webapps.eobroker.common.client.utils.DateUtils;
import com.geocento.webapps.eobroker.common.client.utils.Utils;
import com.geocento.webapps.eobroker.common.client.widgets.ProgressButton;
import com.geocento.webapps.eobroker.common.client.widgets.UserWidget;
import com.geocento.webapps.eobroker.customer.client.ClientFactoryImpl;
import com.geocento.webapps.eobroker.customer.client.Customer;
import com.geocento.webapps.eobroker.customer.client.places.FeedbackPlace;
import com.geocento.webapps.eobroker.customer.shared.FeedbackDTO;
import com.geocento.webapps.eobroker.customer.shared.requests.MessageDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import gwt.material.design.addins.client.bubble.MaterialBubble;
import gwt.material.design.client.constants.ButtonType;
import gwt.material.design.client.constants.Color;
import gwt.material.design.client.constants.IconType;
import gwt.material.design.client.constants.Position;
import gwt.material.design.client.ui.*;

import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 09/05/2016.
 */
public class FeedbackViewImpl extends Composite implements FeedbackView {

    interface FeedbackViewUiBinder extends UiBinder<Widget, FeedbackViewImpl> {
    }

    private static FeedbackViewUiBinder ourUiBinder = GWT.create(FeedbackViewUiBinder.class);

    @UiField
    MaterialTitle title;
    @UiField
    MaterialRow messages;
    @UiField
    ProgressButton submitMessage;
    @UiField
    MaterialTextArea message;
    @UiField
    UserWidget userImage;
    @UiField
    MaterialLabel comment;
    @UiField
    MaterialRow previousFeedback;

    private ClientFactoryImpl clientFactory;

    public FeedbackViewImpl(ClientFactoryImpl clientFactory) {

        this.clientFactory = clientFactory;

        initWidget(ourUiBinder.createAndBindUi(this));

        userImage.setUser(Customer.getLoginInfo().getUserName());
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setPresenter(Presenter presenter) {

    }

    @Override
    public void displayFeedbackError(String message) {
        comment.setVisible(true);
        comment.setText(message);
    }

    @Override
    public void displayFeedbacksError(String message) {
        previousFeedback.clear();
        previousFeedback.add(new MaterialLabel(message));
    }

    @Override
    public void displayFeedbacks(List<FeedbackDTO> feedbackDTOs) {
        previousFeedback.clear();
        if(feedbackDTOs == null || feedbackDTOs.size() == 0) {
            previousFeedback.add(new MaterialLabel("No previous feedback provided..."));
        } else {
            for (final FeedbackDTO feedbackDTO : feedbackDTOs) {
                MaterialBubble materialBubble = new MaterialBubble();
                materialBubble.setBackgroundColor(Color.WHITE);
                materialBubble.setFloat(Style.Float.LEFT);
                materialBubble.setPosition(Position.LEFT);
                materialBubble.setMarginLeft(12);
                materialBubble.setWidth("50%");
                // add content
                MaterialButton materialButton = new MaterialButton(ButtonType.FLOATING);
                materialButton.setIconType(IconType.OPEN_IN_BROWSER);
                materialButton.setFloat(Style.Float.RIGHT);
                materialButton.addClickHandler(event -> clientFactory.getPlaceController().goTo(new FeedbackPlace(Utils.generateTokens(FeedbackPlace.TOKENS.feedbackid.toString(), feedbackDTO.getId()))));
                materialBubble.add(materialButton);
                MaterialLabel materialLabel = new MaterialLabel("Feedback '" + feedbackDTO.getTopic() + "'");
                materialLabel.setFontSize(1.2, Style.Unit.EM);
                materialBubble.add(materialLabel);
                materialLabel = new MaterialLabel("Created on " + DateUtils.formatDateOnly(feedbackDTO.getCreationDate()));
                materialLabel.setFontSize(0.8, Style.Unit.EM);
                materialBubble.add(materialLabel);
                previousFeedback.add(materialBubble);
            }
        }
    }

    @Override
    public void displayFeedbackMessages(List<MessageDTO> messages) {
        displayMessages(messages);
    }

    @Override
    public void setFeedbackTitle(String title) {
        this.title.setTitle(title);
    }

    @Override
    public void setFeedbackDescription(String description) {
        this.title.setDescription(description);
    }

    private void displayMessages(List<MessageDTO> messages) {
        this.messages.clear();
        this.message.setText("");
        String userName = Customer.getLoginInfo().getUserName();
        if(messages.size() == 0) {
            comment.setVisible(true);
            comment.setText("No messages yet...");
            message.setPlaceholder("Start the feedback...");
        } else {
            comment.setVisible(false);
            for (MessageDTO messageDTO : messages) {
                boolean isCustomer = !userName.contentEquals(messageDTO.getFrom());
                addMessage(messageDTO.getFrom(),
                        isCustomer, messageDTO.getMessage(), messageDTO.getCreationDate());
            }
            message.setPlaceholder("Reply...");
        }
    }

    @Override
    public void addMessage(String userName, boolean isCustomer, String message, Date date) {
        comment.setVisible(false);
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
/*
        userWidget.setName(userName);
        userWidget.initialize();
*/
    }

    @Override
    public HasClickHandlers getSubmitMessage() {
        return submitMessage;
    }

    @Override
    public HasText getMessageText() {
        return message;
    }

}