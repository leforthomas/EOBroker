package com.geocento.webapps.eobroker.customer.client.views;

import com.geocento.webapps.eobroker.customer.client.ClientFactoryImpl;
import com.geocento.webapps.eobroker.customer.client.widgets.RequestWidget;
import com.geocento.webapps.eobroker.common.shared.entities.requests.RequestDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import gwt.material.design.client.ui.MaterialColumn;
import gwt.material.design.client.ui.MaterialRow;

import java.util.List;

/**
 * Created by thomas on 09/05/2016.
 */
public class RequestsViewImpl extends Composite implements RequestsView {

    private Presenter presenter;

    interface RequestsViewUiBinder extends UiBinder<Widget, RequestsViewImpl> {
    }

    private static RequestsViewUiBinder ourUiBinder = GWT.create(RequestsViewUiBinder.class);

    @UiField
    MaterialRow requestsList;

    public RequestsViewImpl(ClientFactoryImpl clientFactory) {

        initWidget(ourUiBinder.createAndBindUi(this));

    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setRequests(List<RequestDTO> requestDTOs) {
        requestsList.clear();
        for(RequestDTO requestDTO : requestDTOs) {
            MaterialColumn materialColumn = new MaterialColumn(12, 6, 4);
            materialColumn.add(new RequestWidget(requestDTO));
            requestsList.add(materialColumn);
        }
    }

    @Override
    public Widget asWidget() {
        return this;
    }

}