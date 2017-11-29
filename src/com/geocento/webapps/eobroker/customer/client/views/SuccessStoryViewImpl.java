package com.geocento.webapps.eobroker.customer.client.views;

import com.geocento.webapps.eobroker.common.client.utils.CategoryUtils;
import com.geocento.webapps.eobroker.common.client.widgets.MaterialImageLoading;
import com.geocento.webapps.eobroker.common.client.widgets.MaterialLabelIcon;
import com.geocento.webapps.eobroker.common.client.widgets.MaterialMessage;
import com.geocento.webapps.eobroker.common.shared.entities.Category;
import com.geocento.webapps.eobroker.customer.client.ClientFactoryImpl;
import com.geocento.webapps.eobroker.customer.client.widgets.EndorsementWidget;
import com.geocento.webapps.eobroker.customer.client.widgets.ProductDatasetWidget;
import com.geocento.webapps.eobroker.customer.client.widgets.ProductServiceWidget;
import com.geocento.webapps.eobroker.customer.client.widgets.SoftwareWidget;
import com.geocento.webapps.eobroker.customer.shared.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import gwt.material.design.client.ui.*;

import java.util.List;

/**
 * Created by thomas on 09/05/2016.
 */
public class SuccessStoryViewImpl extends Composite implements SuccessStoryView {

    interface SuccessStoryViewUiBinder extends UiBinder<Widget, SuccessStoryViewImpl> {
    }

    private static SuccessStoryViewUiBinder ourUiBinder = GWT.create(SuccessStoryViewUiBinder.class);

    static DateTimeFormat fmt = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.YEAR_MONTH);

    @UiField
    HTMLPanel fullDescription;
    @UiField
    MaterialLabelIcon productCategory;
    @UiField
    MaterialLabelIcon company;
    @UiField
    MaterialRow endorsements;
    @UiField
    MaterialLabelIcon clientCompany;
    @UiField
    MaterialLabel title;
    @UiField
    MaterialImageLoading iconUrl;
    @UiField
    MaterialLink date;
    @UiField
    MaterialRow offeringsUsed;
    @UiField
    MaterialPanel colorPanel;

    private Presenter presenter;

    public SuccessStoryViewImpl(ClientFactoryImpl clientFactory) {

        initWidget(ourUiBinder.createAndBindUi(this));

        colorPanel.setBackgroundColor(CategoryUtils.getColor(Category.successStories));
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void displaySuccessStory(SuccessStoryDTO successStoryDTO) {
        iconUrl.setImageUrl(successStoryDTO.getImageUrl());
        title.setText(successStoryDTO.getName());
        company.setImageUrl(successStoryDTO.getCompany().getIconURL());
        company.setText(successStoryDTO.getCompany().getName());
        clientCompany.setImageUrl(successStoryDTO.getCustomer().getIconURL());
        clientCompany.setText(successStoryDTO.getCustomer().getName());
        productCategory.setImageUrl(successStoryDTO.getProductDTO().getImageUrl());
        productCategory.setText(successStoryDTO.getProductDTO().getName());
        if(successStoryDTO.getPeriod() != null) {
            date.setVisible(true);
            date.setText(successStoryDTO.getPeriod());
        } else {
            date.setVisible(false);
        }
        fullDescription.add(new HTML(successStoryDTO.getDescription()));
        List<EndorsementDTO> endorsementDTOs = successStoryDTO.getEndorsements();
        endorsements.clear();
        if(endorsementDTOs == null || endorsementDTOs.size() == 0) {
            MaterialMessage materialMessage = new MaterialMessage();
            materialMessage.displayWarningMessage("This success story hasn't been endorsed by the client company yet");
            endorsements.add(materialMessage);
        } else {
            for(EndorsementDTO endorsementDTO : endorsementDTOs) {
                endorsements.add(new EndorsementWidget(endorsementDTO));
            }
        }
        offeringsUsed.clear();
        for(ProductDatasetDTO productDatasetDTO : successStoryDTO.getDatasets()) {
            addDatasetOffering(productDatasetDTO);
        }
        for(ProductServiceDTO productServiceDTO : successStoryDTO.getServices()) {
            addServiceOffering(productServiceDTO);
        }
        for(SoftwareDTO softwareDTO : successStoryDTO.getSoftware()) {
            addSoftwareOffering(softwareDTO);
        }
    }

    private void addOffering(Widget widget) {
        MaterialColumn materialColumn = new MaterialColumn(12, 4, 3);
        materialColumn.add(widget);
        offeringsUsed.add(materialColumn);
    }

    private void addServiceOffering(ProductServiceDTO serviceDTO) {
        addOffering(new ProductServiceWidget(serviceDTO));
    }

    private void addDatasetOffering(ProductDatasetDTO productDatasetDTO) {
        addOffering(new ProductDatasetWidget(productDatasetDTO));
    }

    private void addSoftwareOffering(SoftwareDTO softwareDTO) {
        addOffering(new SoftwareWidget(softwareDTO));
    }

    @Override
    public Widget asWidget() {
        return this;
    }

}