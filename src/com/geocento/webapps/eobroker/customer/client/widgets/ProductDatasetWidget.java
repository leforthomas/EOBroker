package com.geocento.webapps.eobroker.customer.client.widgets;

import com.geocento.webapps.eobroker.customer.client.Customer;
import com.geocento.webapps.eobroker.customer.client.places.FullViewPlace;
import com.geocento.webapps.eobroker.customer.shared.ProductDatasetDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import gwt.material.design.client.ui.MaterialCardAction;
import gwt.material.design.client.ui.MaterialImage;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLink;

/**
 * Created by thomas on 09/06/2016.
 */
public class ProductDatasetWidget extends Composite {

    interface DatasetProviderUiBinder extends UiBinder<Widget, ProductDatasetWidget> {
    }

    private static DatasetProviderUiBinder ourUiBinder = GWT.create(DatasetProviderUiBinder.class);

    @UiField
    MaterialImage image;
    @UiField
    MaterialCardAction action;
    @UiField
    MaterialLabel title;
    @UiField
    MaterialLink info;

    public ProductDatasetWidget(final ProductDatasetDTO productDatasetDTO) {
        initWidget(ourUiBinder.createAndBindUi(this));
        image.setUrl(productDatasetDTO.getImageUrl());
        title.setText(productDatasetDTO.getName());
        info.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Customer.clientFactory.getPlaceController().goTo(new FullViewPlace(FullViewPlace.TOKENS.productdatasetid.toString() + "=" + productDatasetDTO.getId()));
            }
        });
    }

}