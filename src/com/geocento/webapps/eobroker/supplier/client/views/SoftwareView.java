package com.geocento.webapps.eobroker.supplier.client.views;

import com.geocento.webapps.eobroker.common.shared.entities.SoftwareType;
import com.geocento.webapps.eobroker.supplier.shared.dtos.ProductSoftwareDTO;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.List;

/**
 * Created by thomas on 09/05/2016.
 */
public interface SoftwareView extends IsWidget {

    void setPresenter(Presenter presenter);

    void setTitleLine(String title);

    HasText getName();

    HasClickHandlers getSubmit();

    String getImageUrl();

    void setIconUrl(String iconURL);

    HasText getDescription();

    void setSoftwareType(SoftwareType softwareType);

    SoftwareType getSoftwareType();

    String getFullDescription();

    void setFullDescription(String fullDescription);

    java.util.List<ProductSoftwareDTO> getSelectedProducts();

    void setSelectedProducts(List<ProductSoftwareDTO> selectedProducts);

    void setTermsAndConditions(String termsAndConditions);

    String getTermsAndConditions();

    HasClickHandlers getViewClient();

    TemplateView getTemplateView();

    public interface Presenter {
    }

}
