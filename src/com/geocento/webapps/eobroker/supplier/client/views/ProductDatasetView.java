package com.geocento.webapps.eobroker.supplier.client.views;

import com.geocento.webapps.eobroker.common.shared.datasets.DatasetStandard;
import com.geocento.webapps.eobroker.common.shared.entities.*;
import com.geocento.webapps.eobroker.common.shared.entities.dtos.AoIDTO;
import com.geocento.webapps.eobroker.supplier.shared.dtos.ProductDTO;
import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.List;

/**
 * Created by thomas on 09/05/2016.
 */
public interface ProductDatasetView extends IsWidget {

    void setPresenter(Presenter presenter);

    void setTitleLine(String title);

    HasText getName();

    void setServiceType(ServiceType serviceType);

    ServiceType getServiceType();

    HasClickHandlers getSubmit();

    String getImageUrl();

    void setIconUrl(String iconURL);

    HasText getDescription();

    String getFullDescription();

    void setFullDescription(String fullDescription);

    ProductDTO getSelectedProduct();

    void setSelectedProduct(ProductDTO productDTO);

    TemplateView getTemplateView();

    void setExtent(AoIDTO aoi);

    void setCoverageLayers(List<DatasetAccessOGC> coverageLayers);

    List<DatasetAccessOGC> getCoverageLayers();

    void setMapLoadedHandler(Callback<Void, Exception> mapLoadedHandler);

    AoIDTO getExtent();

    void setDataAccess(List<DatasetAccess> datasetAccesses);

    List<DatasetAccess> getDataAccesses();

    void setSampleDataAccess(List<DatasetAccess> samples);

    List<FeatureDescription> getSelectedGeoinformation();

    void setProductGeoinformation(List<FeatureDescription> featureDescriptions);

    void setSelectedGeoinformation(List<FeatureDescription> featureDescriptions);

    List<DatasetAccess> getSamples();

    HasText getGeoinformationComment();

    List<PerformanceValue> getSelectedPerformances();

    void setProductPerformances(List<PerformanceDescription> performanceDescriptions);

    void setProvidedPerformances(List<PerformanceValue> performanceValues);

    HasText getPerformancesComment();

    void setSampleProductDatasetId(Long datasetId);

    void setTemporalCoverage(TemporalCoverage temporalCoverage);

    TemporalCoverage getTemporalCoverage();

    HasText getTemporalCoverageComment();

    HasClickHandlers getViewClient();

    HasText getDatasetURL();

    HasValue<DatasetStandard> getDatasetStandard();

    void setTermsAndConditions(String termsAndConditions);

    String getTermsAndConditions();

    public interface Presenter {

        void productChanged();

        void viewDataAccess(DatasetAccess datasetAccess);
    }

}
