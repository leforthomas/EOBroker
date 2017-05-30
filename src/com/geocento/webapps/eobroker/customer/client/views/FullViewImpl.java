package com.geocento.webapps.eobroker.customer.client.views;

import com.geocento.webapps.eobroker.common.client.utils.CategoryUtils;
import com.geocento.webapps.eobroker.common.client.utils.Utils;
import com.geocento.webapps.eobroker.common.client.widgets.CountryEditor;
import com.geocento.webapps.eobroker.common.client.widgets.maps.AoIUtil;
import com.geocento.webapps.eobroker.common.client.widgets.maps.MapContainer;
import com.geocento.webapps.eobroker.common.client.widgets.maps.resources.ExtentJSNI;
import com.geocento.webapps.eobroker.common.shared.entities.*;
import com.geocento.webapps.eobroker.common.shared.entities.dtos.CompanyDTO;
import com.geocento.webapps.eobroker.customer.client.ClientFactoryImpl;
import com.geocento.webapps.eobroker.customer.client.places.*;
import com.geocento.webapps.eobroker.customer.client.widgets.*;
import com.geocento.webapps.eobroker.customer.shared.*;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import gwt.material.design.client.base.MaterialWidget;
import gwt.material.design.client.constants.*;
import gwt.material.design.client.ui.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas on 09/05/2016.
 */
public class FullViewImpl extends Composite implements FullView {

    interface FullViewImplUiBinder extends UiBinder<Widget, FullViewImpl> {
    }

    private static FullViewImplUiBinder ourUiBinder = GWT.create(FullViewImplUiBinder.class);

    static interface Style extends CssResource {

        String section();

        String subsection();

        String tabPanel();

        String vertical();

        String offeringSection();

        String offeringSubSection();

        String comment();
    }

    static DateTimeFormat fmt = DateTimeFormat.getFormat("MMM-yyyy");

    @UiField Style style;

    @UiField
    MaterialRow tabsContent;
    @UiField
    MaterialLabel title;
    @UiField
    MaterialImage image;
    @UiField
    HTMLPanel tags;
    @UiField
    MaterialLabel description;
    @UiField
    MaterialPanel tabsPanel;
    @UiField
    MaterialPanel colorPanel;
    @UiField
    MaterialPanel recommendationsPanel;
    @UiField
    MaterialNavBar navigation;
    @UiField
    MaterialPanel actions;
    @UiField
    MaterialBreadcrumb recommendationsLabel;

    private Presenter presenter;

    private MaterialTab materialTab;

    private ClientFactoryImpl clientFactory;

    public FullViewImpl(ClientFactoryImpl clientFactory) {

        this.clientFactory = clientFactory;

        initWidget(ourUiBinder.createAndBindUi(this));

    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void clearDetails() {
        navigation.clear();
        navigation.setVisible(false);
        actions.clear();
        tags.clear();
        tabsContent.clear();
    }

    @Override
    public void displayProduct(ProductDescriptionDTO productDescriptionDTO) {
        clearDetails();
        image.setUrl(Utils.getImageMaybe(productDescriptionDTO.getImageUrl()));
        title.setText(productDescriptionDTO.getName());
        description.setText(productDescriptionDTO.getShortDescription());
        setTabPanelColor(CategoryUtils.getColor(Category.products));
        // add tags
        {
            addTag(productDescriptionDTO.getThematic().getName(), Color.BLUE, Color.WHITE);
            addTag(productDescriptionDTO.getSector().getName(), Color.GREY, Color.WHITE);
            {
                ProductDTO productDTO = new ProductDTO();
                productDTO.setId(productDescriptionDTO.getId());
                productDTO.setFollowers(productDescriptionDTO.getFollowers());
                productDTO.setFollowing(productDescriptionDTO.isFollowing());
                ProductFollowWidget followWidget = new ProductFollowWidget(productDTO);
                followWidget.setName("product");
                followWidget.getElement().getStyle().setMarginLeft(10, com.google.gwt.dom.client.Style.Unit.PX);
                tags.add(followWidget);
            }
        }
        // add the tabs now
        MaterialPanel tabsPanel = createTabsPanel();
        materialTab = createTabs(tabsPanel);
        int numTabs = 2;
        int size = (int) Math.floor(12 / numTabs);
        // add description
        MaterialPanel fullDescriptionPanel = new MaterialPanel();
        HTML fullDescription = new HTML(productDescriptionDTO.getDescription());
        fullDescription.getElement().getStyle().setProperty("minHeight", "6em");
        fullDescriptionPanel.add(fullDescription);
        fullDescriptionPanel.setPadding(10);
        addTab(materialTab, tabsPanel, "Description", fullDescriptionPanel, size, "description");
        // create tab panel for offering
        MaterialPanel offeringPanel = new MaterialPanel();
        setMatchingServices(offeringPanel, productDescriptionDTO.getProductServices(), "#" + PlaceHistoryHelper.convertPlace(new SearchPagePlace(Utils.generateTokens(
                SearchPagePlace.TOKENS.category.toString(), Category.productservices.toString()))));
        setMatchingDatasets(offeringPanel, productDescriptionDTO.getProductDatasets(), "#" + PlaceHistoryHelper.convertPlace(new SearchPagePlace(Utils.generateTokens(
                SearchPagePlace.TOKENS.category.toString(), Category.productdatasets.toString()))));
        setMatchingSoftwares(offeringPanel, productDescriptionDTO.getSoftwares(), "#" + PlaceHistoryHelper.convertPlace(new SearchPagePlace(Utils.generateTokens(
                SearchPagePlace.TOKENS.category.toString(), Category.software.toString()))));
        setMatchingProjects(offeringPanel, productDescriptionDTO.getProjects(), "#" + PlaceHistoryHelper.convertPlace(new SearchPagePlace(Utils.generateTokens(
                SearchPagePlace.TOKENS.category.toString(), Category.project.toString()))));
        addTab(materialTab, tabsPanel, "Offering", offeringPanel, size, "offering");
        tabsContent.add(tabsPanel);

        // TODO - change?
        this.tabsPanel.clear();
        this.tabsPanel.add(materialTab);

        materialTab.selectTab("description");
        // add suggestions
        recommendationsPanel.clear();
        recommendationsLabel.setText("Similar products...");
        List<ProductDTO> suggestedProducts = productDescriptionDTO.getSuggestedProducts();
        if(suggestedProducts == null || suggestedProducts.size() == 0) {
            MaterialLabel materialLabel = new MaterialLabel("No suggestions...");
            materialLabel.setPadding(20);
            recommendationsPanel.add(materialLabel);
        } else {
            MaterialRow materialRow = new MaterialRow();
            recommendationsPanel.add(materialRow);
            for(ProductDTO productDTO : suggestedProducts) {
                MaterialColumn materialColumn = new MaterialColumn(12, 6, 3);
                ProductWidget productWidget = new ProductWidget(productDTO);
                materialColumn.add(productWidget);
                materialRow.add(materialColumn);
            }
        }
    }

    private void setTabPanelColor(Color color) {
        tabsPanel.setBackgroundColor(color);
        colorPanel.setBackgroundColor(color);
    }

    @Override
    public void displayProductService(final ProductServiceDescriptionDTO productServiceDescriptionDTO) {
        clearDetails();
        // insert header with information on company and product category
        {
            addBreadcrumb(productServiceDescriptionDTO.getCompany(), Category.productservices);
            addBreadcrumb(productServiceDescriptionDTO.getProduct(), Category.productservices);
        }
        image.setUrl(Utils.getImageMaybe(productServiceDescriptionDTO.getServiceImage()));
        title.setText(productServiceDescriptionDTO.getName());
        description.setText(productServiceDescriptionDTO.getDescription());
        setTabPanelColor(CategoryUtils.getColor(Category.productservices));
        // add tags
        {
/*
            MaterialChip product = new MaterialChip();
            tags.add(product);
*/
        }
        MaterialPanel tabsPanel = createTabsPanel();
        materialTab = createTabs(tabsPanel);
        int numTabs = 4;
        int size = (int) Math.floor(12 / numTabs);

        // add actions
        {
            if (productServiceDescriptionDTO.isHasFeasibility()) {
                addAction("CHECK FEASIBILITY", "#" + PlaceHistoryHelper.convertPlace(
                        new ProductFeasibilityPlace(
                                ProductFeasibilityPlace.TOKENS.productservice.toString() + "=" + productServiceDescriptionDTO.getId())));
            }
            {
                addAction("REQUEST QUOTE", "#" + PlaceHistoryHelper.convertPlace(
                        new ProductFormPlace(
                                ProductFormPlace.TOKENS.serviceid.toString() + "=" + productServiceDescriptionDTO.getId())));
            }
            {
                addAction("CONTACT", "#" + PlaceHistoryHelper.convertPlace(
                        new ConversationPlace(
                                Utils.generateTokens(
                                        ConversationPlace.TOKENS.companyid.toString(), productServiceDescriptionDTO.getCompany().getId() + "",
                                        ConversationPlace.TOKENS.topic.toString(), "Information on service '" + productServiceDescriptionDTO.getName() + "'"))));
            }
        }

        // create full description panel
        MaterialPanel fullDescriptionPanel = createFullDescriptionPanel(productServiceDescriptionDTO.getFullDescription());
        addTab(materialTab, tabsPanel, "Description", fullDescriptionPanel, size, "description");

        // create tab panel for services
        MaterialRow featuresPanel = new MaterialRow();
        MaterialColumn materialColumn = new MaterialColumn(12, 6, 6);
        featuresPanel.add(materialColumn);
        // add geoinformation provided
        materialColumn.add(createGeoinformationPanel(productServiceDescriptionDTO.getGeoinformation(), productServiceDescriptionDTO.getGeoinformationComment()));
        // add perfomances
        materialColumn.add(createPerformancesPanel(productServiceDescriptionDTO.getPerformances(), productServiceDescriptionDTO.getPerformancesComments()));
        // add time delivery information
        if(productServiceDescriptionDTO.getTimeToDelivery() != null) {
            MaterialPanel performancesPanel = new MaterialPanel();
            performancesPanel.add(createSubsection("Time to delivery"));
            MaterialLabel timeDeliveryLabel = new MaterialLabel();
            timeDeliveryLabel.setPadding(20);
            timeDeliveryLabel.setText(productServiceDescriptionDTO.getTimeToDelivery());
            performancesPanel.add(timeDeliveryLabel);
            materialColumn.add(performancesPanel);
        }
        // add map with extent of data
        materialColumn = new MaterialColumn(12, 6, 6);
        featuresPanel.add(materialColumn);
        boolean worldWide = productServiceDescriptionDTO.getExtent() == null;
        materialColumn.add(createSubsection("Extent of service"));
        final MapContainer mapContainer = new MapContainer();
        mapContainer.setHeight("300px");
        mapContainer.getElement().getStyle().setMarginTop(20, com.google.gwt.dom.client.Style.Unit.PX);
        mapContainer.setEditable(false);
        mapContainer.setBasemapVisible(false);
        mapContainer.setLayer(false);
        mapContainer.setMapLoadedHandler(new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {

            }

            @Override
            public void onSuccess(Void result) {
                if(worldWide) {
                    mapContainer.map.setZoom(1);
                } else {
                    mapContainer.displayAoI(AoIUtil.fromWKT(productServiceDescriptionDTO.getExtent()));
                    mapContainer.centerOnAoI();
                }
            }
        });
        materialColumn.add(mapContainer);
        if(worldWide) {
            MaterialLabel comment = new MaterialLabel("Bespoke service is available worldwide");
            comment.addStyleName(style.comment());
            materialColumn.add(comment);
        }
        // add a compare button
        {
            MaterialButton compareButton = new MaterialButton("Compare");
            compareButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Window.alert("TODO - show other offering to compare to");
                }
            });
            //featuresPanel.add(compareButton);
        }
        addTab(materialTab, tabsPanel, "Performances", featuresPanel, size, "performances");
        // create tab for data access information
        // add access panel
        // add access panel
        MaterialPanel accessPanel = new MaterialPanel();
        List<DatasetAccess> availableMapData = new ArrayList<DatasetAccess>();
        {
            accessPanel.add(createSubsection("Dissemination methods supported"));
            MaterialPanel materialPanel = new MaterialPanel();
            materialPanel.setMargin(10);
            accessPanel.add(materialPanel);
            List<AccessType> accessTypes = productServiceDescriptionDTO.getSelectedAccessTypes();
            for (AccessType accessType : AccessType.values()) {
                MaterialLink materialLink = new MaterialLink(accessType.getName());
                materialLink.setDisplay(Display.BLOCK);
                materialLink.setMargin(10);
                materialPanel.setMarginBottom(30);
                materialLink.setTextColor(Color.BLACK);
                if(accessTypes != null && accessTypes.contains(accessType)) {
                    materialLink.setIconType(IconType.CHECK);
                    materialLink.setIconColor(Color.AMBER);
                    materialPanel.add(materialLink);
                } else {
                    materialLink.setIconType(IconType.CHECK_BOX_OUTLINE_BLANK);
                    materialLink.setIconColor(Color.BLACK);
                }
            }
        }
        List<DatasetAccess> samples = productServiceDescriptionDTO.getSamples();
        if(samples != null && samples.size() > 0) {
            accessPanel.add(createSubsection("Sample data access"));
            MaterialRow materialRow = new MaterialRow();
            materialRow.setMargin(10);
            materialRow.setMarginBottom(30);
            accessPanel.add(materialRow);
            for(DatasetAccess datasetAccess : samples) {
                MaterialColumn materialColumnSample = new MaterialColumn(12, 12, 6);
                materialColumnSample.add(createDataAccessWidgetProductService(productServiceDescriptionDTO, datasetAccess, true));
                materialRow.add(materialColumnSample);
                if(datasetAccess instanceof DatasetAccessOGC) {
                    availableMapData.add(datasetAccess);
                }
            }
        }
        addTab(materialTab, tabsPanel, "Data Access", accessPanel, size, "dataaccess");
        // create tab panel for services
        HTMLPanel termsAndConditionsPanel = new HTMLPanel("<p class='" + style.subsection() + "'>No terms and conditions specified</p>");
        addTab(materialTab, tabsPanel, "Terms and Conditions", termsAndConditionsPanel, size, "termsandconditions");
        materialTab.selectTab("description");
        tabsContent.add(tabsPanel);

        // TODO - change?
        this.tabsPanel.clear();
        this.tabsPanel.add(materialTab);

        recommendationsPanel.clear();
        recommendationsLabel.setText("You might also be interested in...");
        List<ProductServiceDTO> suggestedServices = productServiceDescriptionDTO.getSuggestedServices();
        if(suggestedServices == null || suggestedServices.size() == 0) {
            MaterialLabel materialLabel = new MaterialLabel("No suggestions...");
            materialLabel.setPadding(20);
            recommendationsPanel.add(materialLabel);
        } else {
            MaterialRow materialRow = new MaterialRow();
            recommendationsPanel.add(materialRow);
            for(ProductServiceDTO productServiceDTO : productServiceDescriptionDTO.getSuggestedServices()) {
                materialColumn = new MaterialColumn(12, 6, 3);
                ProductServiceWidget productServiceWidget = new ProductServiceWidget(productServiceDTO);
                materialColumn.add(productServiceWidget);
                materialRow.add(materialColumn);
            }
        }
    }

    private Widget createPerformancesPanel(List<PerformanceValue> performances, String comment) {
        MaterialPanel performancesPanel = new MaterialPanel();
        performancesPanel.add(createSubsection("Precision and accuracy"));
        MaterialRow materialRow = new MaterialRow();
        materialRow.setMarginTop(20);
        performancesPanel.add(materialRow);
        if(performances.size() == 0) {
            MaterialColumn materialColumn = new MaterialColumn();
            materialRow.add(materialColumn);
            materialColumn.add(new MaterialLabel("No information provided..."));
        } else {
            for (PerformanceValue performance : performances) {
                MaterialColumn materialColumn = new MaterialColumn(12, 12, 12);
                materialRow.add(materialColumn);
                MaterialLink materialLink = new MaterialLink(performance.getPerformanceDescription().getName() +
                        " " + performance.getMinValue() +
                        (performance.getMaxValue() != null ? " - " + performance.getMaxValue() : "") +
                        " " + performance.getPerformanceDescription().getUnit());
                materialLink.setIconType(IconType.CHECK);
                materialLink.setIconColor(Color.AMBER);
                MaterialTooltip materialTooltip = new MaterialTooltip(materialLink, performance.getPerformanceDescription().getDescription());
                materialColumn.add(materialTooltip);
            }
        }
        if(comment != null && comment.length() > 0) {
            MaterialColumn materialColumn = new MaterialColumn(12, 12, 12);
            materialRow.add(materialColumn);
            MaterialLabel commentLabel = new MaterialLabel("Comment: " + comment);
            commentLabel.addStyleName(style.comment());
            //commentLabel.getElement().getStyle().setMarginLeft(10, com.google.gwt.dom.client.Style.Unit.PX);
            materialColumn.add(commentLabel);
        }
        return performancesPanel;
    }

    private MaterialPanel createGeoinformationPanel(List<FeatureDescription> geoinformation, String comment) {
        MaterialPanel geoinformationPanel = new MaterialPanel();
        geoinformationPanel.add(createSubsection("Geoinformation provided"));
        MaterialRow geoinformationRow = new MaterialRow();
        geoinformationRow.setMarginTop(20);
        geoinformationPanel.add(geoinformationRow);
        if(geoinformation.size() == 0) {
            MaterialColumn materialColumn = new MaterialColumn();
            geoinformationRow.add(materialColumn);
            materialColumn.add(new MaterialLabel("No geoinformation provided..."));
        } else {
            for (FeatureDescription featureDescription : geoinformation) {
                MaterialColumn materialColumn = new MaterialColumn(12, 12, 12);
                geoinformationRow.add(materialColumn);
                MaterialLink materialLink = new MaterialLink(featureDescription.getName());
                materialLink.setIconType(IconType.CHECK);
                materialLink.setIconColor(Color.AMBER);
                MaterialTooltip materialTooltip = new MaterialTooltip(materialLink, featureDescription.getDescription());
                materialColumn.add(materialTooltip);
            }
        }
        if(comment != null && comment.length() > 0) {
            MaterialColumn materialColumn = new MaterialColumn(12, 12, 12);
            geoinformationRow.add(materialColumn);
            MaterialLabel commentLabel = new MaterialLabel(comment);
            commentLabel.getElement().setInnerText("Comment: " + comment);
            commentLabel.addStyleName(style.comment());
            //commentLabel.getElement().getStyle().setMarginLeft(10, com.google.gwt.dom.client.Style.Unit.PX);
            materialColumn.add(commentLabel);
        }
        return geoinformationPanel;
    }

    @Override
    public void displayProductDataset(final ProductDatasetDescriptionDTO productDatasetDescriptionDTO) {
        clearDetails();
        image.setUrl(Utils.getImageMaybe(productDatasetDescriptionDTO.getImageUrl()));
        title.setText(productDatasetDescriptionDTO.getName());
        description.setText(productDatasetDescriptionDTO.getDescription());
        setTabPanelColor(CategoryUtils.getColor(Category.productdatasets));
        // add breadcrumbs
        {
            addBreadcrumb(productDatasetDescriptionDTO.getCompany(), Category.productservices);
            addBreadcrumb(productDatasetDescriptionDTO.getProduct(), Category.productservices);
        }
        // add tags
        {
            addTag(IconType.MONETIZATION_ON, productDatasetDescriptionDTO.isCommercial() ? "Commercial" : "Free", Color.AMBER, Color.WHITE);
        }

        MaterialPanel tabsPanel = createTabsPanel();
        materialTab = createTabs(tabsPanel);
        int numTabs = 4;
        int size = (int) Math.floor(12 / numTabs);

        // add actions
        addAction("CONTACT", "#" + PlaceHistoryHelper.convertPlace(
                new ConversationPlace(
                        Utils.generateTokens(
                                ConversationPlace.TOKENS.companyid.toString(), productDatasetDescriptionDTO.getCompany().getId() + "",
                                ConversationPlace.TOKENS.topic.toString(), "Information on off the shelf product '" + productDatasetDescriptionDTO.getName() + "'"))));
        // add description
        MaterialPanel fullDescriptionPanel = createFullDescriptionPanel(productDatasetDescriptionDTO.getFullDescription());
        addTab(materialTab, tabsPanel, "Description", fullDescriptionPanel, size, "description");

        // create tab panel for services
        MaterialRow featuresPanel = new MaterialRow();
        // add geoinformation provided
        MaterialColumn materialColumn = new MaterialColumn(12, 6, 6);
        featuresPanel.add(materialColumn);
        // add geoinformation provided
        materialColumn.add(createGeoinformationPanel(productDatasetDescriptionDTO.getGeoinformation(), productDatasetDescriptionDTO.getGeoinformationComment()));
        // add perfomances
        materialColumn.add(createPerformancesPanel(productDatasetDescriptionDTO.getPerformances(), productDatasetDescriptionDTO.getPerformancesComments()));
        // add map with extent of data
        materialColumn = new MaterialColumn(12, 6, 6);
        featuresPanel.add(materialColumn);
        materialColumn.add(createSubsection("Extent of data"));
        boolean worldWide = productDatasetDescriptionDTO.getExtent() == null;
        final MapContainer mapContainer = new MapContainer();
        mapContainer.setHeight("300px");
        mapContainer.getElement().getStyle().setMarginTop(20, com.google.gwt.dom.client.Style.Unit.PX);
        mapContainer.setEditable(false);
        mapContainer.setBasemapVisible(false);
        mapContainer.setLayer(false);
        mapContainer.setMapLoadedHandler(new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {

            }

            @Override
            public void onSuccess(Void result) {
                if(worldWide) {
                    mapContainer.map.setZoom(1);
                } else {
                    mapContainer.displayAoI(AoIUtil.fromWKT(productDatasetDescriptionDTO.getExtent()));
                    mapContainer.centerOnAoI();
                }
            }
        });
        materialColumn.add(mapContainer);
        if(worldWide) {
            MaterialLabel comment = new MaterialLabel("The data is available worldwide");
            comment.addStyleName(style.comment());
            materialColumn.add(comment);
        }
        // add a compare button
        {
            MaterialButton compareButton = new MaterialButton("Compare");
            compareButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Window.alert("TODO - show other offering to compare to");
                }
            });
            //featuresPanel.add(compareButton);
        }
        addTab(materialTab, tabsPanel, "Performances", featuresPanel, size, "performances");
        // add access panel
        MaterialPanel accessPanel = new MaterialPanel();
        List<DatasetAccess> availableMapData = new ArrayList<DatasetAccess>();
        List<DatasetAccess> dataAccesses = productDatasetDescriptionDTO.getDatasetAccesses();
        if(dataAccesses != null && dataAccesses.size() > 0) {
            accessPanel.add(createSubsection("The data for this off the shelf product can be accessed using the following methods:"));
            MaterialRow materialRow = new MaterialRow();
            materialRow.setMargin(10);
            materialRow.setMarginBottom(30);
            accessPanel.add(materialRow);
            for (DatasetAccess datasetAccess : dataAccesses) {
                MaterialColumn materialColumnAccess = new MaterialColumn(12, 12, 6);
                materialRow.add(materialColumnAccess);
                boolean freeAvailable = !productDatasetDescriptionDTO.isCommercial();
                DataAccessWidget dataAccessWidget = createDataAccessWidgetProductDataset(productDatasetDescriptionDTO, datasetAccess, freeAvailable);
                materialColumnAccess.add(dataAccessWidget);
                if (datasetAccess instanceof DatasetAccessOGC && freeAvailable) {
                    availableMapData.add(datasetAccess);
                }
            }
        }
        // check for catalogue browsing
        if(productDatasetDescriptionDTO.getCatalogueStandard() != null) {
            MaterialRow materialRow = new MaterialRow();
            materialRow.setMargin(10);
            materialRow.setMarginBottom(30);
            accessPanel.add(materialRow);
            MaterialLabel label = new MaterialLabel("This off the shelf data contains sub products which can be browsed ");
            MaterialLink materialLink = new MaterialLink();
/*
            materialLink.setIconPosition(IconPosition.RIGHT);
*/
            materialLink.setText("here");
            materialLink.setMarginLeft(0);
            materialLink.setHref("#" + PlaceHistoryHelper.convertPlace(new CatalogueSearchPlace(
                    Utils.generateTokens(CatalogueSearchPlace.TOKENS.productId.toString(), productDatasetDescriptionDTO.getId() + ""))));
            label.add(materialLink);
            label.setFontSize(1.2, com.google.gwt.dom.client.Style.Unit.EM);
            MaterialColumn materialColumnAccess = new MaterialColumn(12, 12, 12);
            materialRow.add(materialColumnAccess);
            materialColumnAccess.add(label);
        }
        List<DatasetAccess> samples = productDatasetDescriptionDTO.getSamples();
        if(samples != null && samples.size() > 0) {
            accessPanel.add(createSubsection("Some sample data is available for this off the shelf product"));
            MaterialRow materialRow = new MaterialRow();
            materialRow.setMargin(10);
            materialRow.setMarginBottom(30);
            accessPanel.add(materialRow);
            for(DatasetAccess datasetAccess : samples) {
                MaterialColumn materialColumnSample = new MaterialColumn(12, 12, 6);
                materialRow.add(materialColumnSample);
                materialColumnSample.add(createDataAccessWidgetProductDataset(productDatasetDescriptionDTO, datasetAccess, true));
                if(datasetAccess instanceof DatasetAccessOGC) {
                    availableMapData.add(datasetAccess);
                }
            }
        }
        addTab(materialTab, tabsPanel, "Data Access", accessPanel, size, "dataaccess");
        // add terms and conditions tab panel
        HTMLPanel termsAndConditionsPanel = new HTMLPanel("<p class='" + style.subsection() + "'>No terms and conditions specified</p>");
        addTab(materialTab, tabsPanel, "Terms and Conditions", termsAndConditionsPanel, size, "termsandconditions");
        materialTab.selectTab("description");
        // now add the tabs panel
        tabsContent.add(tabsPanel);

        // TODO - change?
        this.tabsPanel.clear();
        this.tabsPanel.add(materialTab);

        // add recommendations
        recommendationsPanel.clear();
        recommendationsLabel.setText("You might also be interested in...");
        List<ProductDatasetDTO> suggestedDatasets = productDatasetDescriptionDTO.getSuggestedDatasets();
        if(suggestedDatasets == null || suggestedDatasets.size() == 0) {
            MaterialLabel materialLabel = new MaterialLabel("No suggestions...");
            materialLabel.setPadding(20);
            recommendationsPanel.add(materialLabel);
        } else {
            MaterialRow materialRow = new MaterialRow();
            recommendationsPanel.add(materialRow);
            for (ProductDatasetDTO productDatasetDTO : suggestedDatasets) {
                materialColumn = new MaterialColumn(12, 6, 3);
                ProductDatasetWidget productDatasetWidget = new ProductDatasetWidget(productDatasetDTO);
                materialColumn.add(productDatasetWidget);
                materialRow.add(materialColumn);
            }
        }
    }

    @Override
    public void displaySoftware(SoftwareDescriptionDTO softwareDescriptionDTO) {
        clearDetails();
        image.setUrl(Utils.getImageMaybe(softwareDescriptionDTO.getImageUrl()));
        title.setText(softwareDescriptionDTO.getName());
        description.setText(softwareDescriptionDTO.getDescription());
        addBreadcrumb(softwareDescriptionDTO.getCompanyDTO(), Category.software);
        setTabPanelColor(CategoryUtils.getColor(Category.software));
        // add tags
        {
            addTag(IconType.MONETIZATION_ON, softwareDescriptionDTO.getSoftwareType().getName(), Color.AMBER, Color.WHITE);
        }
        MaterialPanel tabsPanel = createTabsPanel();
        materialTab = createTabs(tabsPanel);
        int numTabs = 4;
        int size = (int) Math.floor(12 / numTabs);

        // add actions
        addAction("CONTACT", "#" + PlaceHistoryHelper.convertPlace(
                    new ConversationPlace(
                            Utils.generateTokens(
                                    ConversationPlace.TOKENS.companyid.toString(), softwareDescriptionDTO.getCompanyDTO().getId() + "",
                                    ConversationPlace.TOKENS.topic.toString(), "Information on software solution '" + softwareDescriptionDTO.getName() + "'"))));
        // create full description panel
        MaterialPanel fullDescriptionPanel = createFullDescriptionPanel(softwareDescriptionDTO.getFullDescription());
        addTab(materialTab, tabsPanel, "Description", fullDescriptionPanel, size, "description");

        // add products tab
        {
            MaterialPanel productsPanel = new MaterialPanel();
            MaterialLabel materialLabel = createSubsection("Products covered by this software solution:");
            materialLabel.setMarginBottom(20);
            productsPanel.add(materialLabel);
            List<ProductSoftwareDTO> productsCovered = softwareDescriptionDTO.getProducts();
            if (productsCovered == null || productsCovered.size() == 0) {
                addColumnLine(new MaterialLabel("No products..."));
            } else {
                for(ProductSoftwareDTO productSoftwareDTO : productsCovered) {
                    MaterialRow materialRow = new MaterialRow();
                    productsPanel.add(materialRow);
                    MaterialColumn materialColumn = new MaterialColumn(6, 4, 3);
                    ProductWidget productWidget = new ProductWidget(productSoftwareDTO.getProduct());
                    materialColumn.add(productWidget);
                    materialRow.add(materialColumn);
                    materialColumn = new MaterialColumn(6, 8, 9);
                    materialColumn.add(new HTML("<h4>Pitch</h4><p>" + productSoftwareDTO.getPitch() + "</p>"));
                    materialRow.add(materialColumn);
                }
            }
            addTab(materialTab, tabsPanel, "Products", productsPanel, size, "products");
        }

        // add terms and conditions tab panel
        HTMLPanel termsAndConditionsPanel = new HTMLPanel("<p class='" + style.subsection() + "'>No terms and conditions specified</p>");
        addTab(materialTab, tabsPanel, "Terms and Conditions", termsAndConditionsPanel, size, "termsandconditions");

        // now add the tabs panel
        materialTab.selectTab("description");
        tabsContent.add(tabsPanel);

        // TODO - change?
        this.tabsPanel.clear();
        this.tabsPanel.add(materialTab);

        // add recommendations
        recommendationsPanel.clear();
        recommendationsLabel.setText("You might also be interested in...");
        List<SoftwareDTO> suggestedSoftware = softwareDescriptionDTO.getSuggestedSoftware();
        if(suggestedSoftware == null || suggestedSoftware.size() == 0) {
            MaterialLabel materialLabel = new MaterialLabel("No suggestions...");
            materialLabel.setPadding(20);
            recommendationsPanel.add(materialLabel);
        } else {
            MaterialRow materialRow = new MaterialRow();
            recommendationsPanel.add(materialRow);
            for (SoftwareDTO softwareDTO : suggestedSoftware) {
                MaterialColumn materialColumn = new MaterialColumn(12, 6, 3);
                SoftwareWidget softwareWidget = new SoftwareWidget(softwareDTO);
                materialColumn.add(softwareWidget);
                materialRow.add(materialColumn);
            }
        }
    }

    @Override
    public void displayProject(ProjectDescriptionDTO projectDescriptionDTO) {
        clearDetails();
        image.setUrl(Utils.getImageMaybe(projectDescriptionDTO.getImageUrl()));
        title.setText(projectDescriptionDTO.getName());
        description.setText(projectDescriptionDTO.getDescription());
        setTabPanelColor(CategoryUtils.getColor(Category.project));

        // add company
        addBreadcrumb(projectDescriptionDTO.getCompanyDTO(), Category.project);

        // add tags
        {
            if(projectDescriptionDTO.getStartDate() != null) {
                String timeFrame = fmt.format(projectDescriptionDTO.getStartDate()) + " - " +
                        projectDescriptionDTO.getStopDate() == null ? " on-going" : fmt.format(projectDescriptionDTO.getStopDate());
                addTag(IconType.TIMELINE, timeFrame, Color.AMBER, Color.WHITE);
            }
        }

        MaterialPanel tabsPanel = createTabsPanel();
        materialTab = createTabs(tabsPanel);
        int numTabs = 4;
        int size = (int) Math.floor(12 / numTabs);

        // add actions
        {
            addAction("CONTACT", "#" + PlaceHistoryHelper.convertPlace(
                    new ConversationPlace(
                            Utils.generateTokens(
                                    ConversationPlace.TOKENS.companyid.toString(), projectDescriptionDTO.getCompanyDTO().getId() + "",
                                    ConversationPlace.TOKENS.topic.toString(), "Information on project '" + projectDescriptionDTO.getName() + "'"))));
        }
        // create full description panel
        MaterialPanel fullDescriptionPanel = createFullDescriptionPanel(projectDescriptionDTO.getFullDescription());
        addTab(materialTab, tabsPanel, "Description", fullDescriptionPanel, size, "description");

        // add products tab
        {
            MaterialPanel productsPanel = new MaterialPanel();
            MaterialLabel materialLabel = createSubsection("Products covered within this project:");
            materialLabel.setMarginBottom(20);
            productsPanel.add(materialLabel);
            List<ProductProjectDTO> productsCovered = projectDescriptionDTO.getProducts();
            if (productsCovered == null || productsCovered.size() == 0) {
                addColumnLine(new MaterialLabel("No products..."));
            } else {
                for (ProductProjectDTO productProjectDTO : productsCovered) {
                    MaterialRow materialRow = new MaterialRow();
                    materialRow.setWidth("100%");
                    productsPanel.add(materialRow);
                    MaterialColumn materialColumn = new MaterialColumn(6, 4, 3);
                    ProductWidget productWidget = new ProductWidget(productProjectDTO.getProduct());
                    materialColumn.add(productWidget);
                    materialRow.add(materialColumn);
                    materialColumn = new MaterialColumn(6, 8, 9);
                    materialColumn.add(new HTML("<h5>Pitch</h5>" + productProjectDTO.getPitch()));
                    materialRow.add(materialColumn);
                }
            }
            addTab(materialTab, tabsPanel, "Products", productsPanel, size, "products");
        }

        // add consortium information
        {
            MaterialPanel consortiumPanel = new MaterialPanel();
            MaterialLabel materialLabel = createSubsection("Companies involved in this project:");
            materialLabel.setMarginBottom(20);
            consortiumPanel.add(materialLabel);
            List<CompanyRoleDTO> consortium = projectDescriptionDTO.getConsortium();
            for(CompanyRoleDTO companyRoleDTO : consortium) {
                MaterialRow materialRow = new MaterialRow();
                materialRow.setWidth("100%");
                consortiumPanel.add(materialRow);
                MaterialColumn materialColumn = new MaterialColumn(6, 4, 3);
                CompanyWidget companyWidget = new CompanyWidget(companyRoleDTO.getCompanyDTO());
                materialColumn.add(companyWidget);
                materialRow.add(materialColumn);
                materialColumn = new MaterialColumn(6, 8, 9);
                materialColumn.add(new HTML("<h5>Role in project</h5>" + companyRoleDTO.getRole()));
                materialRow.add(materialColumn);
            }
            addTab(materialTab, tabsPanel, "Consortium", consortiumPanel, size, "consortium");
        }
        materialTab.selectTab("description");
        // now add the tabs panel
        tabsContent.add(tabsPanel);

        // TODO - change?
        this.tabsPanel.clear();
        this.tabsPanel.add(materialTab);

        // add recommendations
        recommendationsPanel.clear();
        recommendationsLabel.setText("You might also be interested in...");
        List<ProjectDTO> suggestedProjects = projectDescriptionDTO.getSuggestedProjects();
        if(suggestedProjects == null || suggestedProjects.size() == 0) {
            MaterialLabel materialLabel = new MaterialLabel("No suggestions...");
            materialLabel.setPadding(20);
            recommendationsPanel.add(materialLabel);
        } else {
            MaterialRow materialRow = new MaterialRow();
            recommendationsPanel.add(materialRow);
            for (ProjectDTO projectDTO : suggestedProjects) {
                MaterialColumn materialColumn = new MaterialColumn(12, 6, 3);
                ProjectWidget projectWidget = new ProjectWidget(projectDTO);
                materialColumn.add(projectWidget);
                materialRow.add(materialColumn);
            }
        }
    }

    @Override
    public void selectTab(String tabName) {
        // TODO - change to something "cleaner"
        if(materialTab != null) {
            materialTab.selectTab(tabName);
        }
/*
        ((MaterialTab) tabsPanel.getChildren().get(0)).selectTab(tabName);
*/
    }

    private MaterialChip addTag(String text, Color backgroundColor) {
        return addTag(null, text, backgroundColor, Color.WHITE);
    }

    private MaterialChip addTag(String text, Color backgroundColor, Color textColor) {
        return addTag(null, text, backgroundColor, textColor);
    }

    private MaterialChip addTag(IconType iconType, String text, Color backgroundColor, Color textColor) {
        return addTag(iconType, text, backgroundColor, textColor, null, null);
    }

    private MaterialChip addTag(IconType iconType, String text, Color backgroundColor, Color textColor, String tooltip) {
        return addTag(iconType, text, backgroundColor, textColor, tooltip, null);
    }

    private MaterialChip addTag(IconType iconType, String text, Color backgroundColor, Color textColor, String tooltip, ClickHandler clickHandler) {
        MaterialChip materialChip = new MaterialChip();
        materialChip.setText(text);
        materialChip.setBackgroundColor(backgroundColor);
        materialChip.setTextColor(textColor);
        materialChip.setMarginLeft(20);
        if(iconType != null) {
            materialChip.setIconPosition(IconPosition.LEFT);
            materialChip.setIconType(iconType);
        }
        if(clickHandler != null) {
            materialChip.addClickHandler(clickHandler);
            materialChip.getElement().getStyle().setCursor(com.google.gwt.dom.client.Style.Cursor.POINTER);
        }
        if(tooltip != null) {
            materialChip.setTooltip(tooltip);
        }
        tags.add(materialChip);
        return materialChip;
    }

    @Override
    public void displayCompany(final CompanyDescriptionDTO companyDescriptionDTO) {
        clearDetails();
        image.setUrl(Utils.getImageMaybe(companyDescriptionDTO.getIconURL()));
        title.setText(companyDescriptionDTO.getName());
        description.setText(companyDescriptionDTO.getDescription());
        setTabPanelColor(CategoryUtils.getColor(Category.companies));
        // add tags
        if(companyDescriptionDTO.getCompanySize() != null) {
            addTag(IconType.FORMAT_SIZE, companyDescriptionDTO.getCompanySize().toString(), Color.BLUE, Color.WHITE, "The company size using the European Commission definition");
        }
        if(companyDescriptionDTO.getCountryCode() != null) {
            addTag(IconType.MY_LOCATION, CountryEditor.getDisplayName(companyDescriptionDTO.getCountryCode()), Color.GREY, Color.WHITE);
        }
        {
            CompanyDTO companyDTO = new CompanyDTO();
            companyDTO.setId(companyDescriptionDTO.getId());
            companyDTO.setFollowers(companyDescriptionDTO.getFollowers());
            companyDTO.setFollowing(companyDescriptionDTO.isFollowing());
            CompanyFollowWidget followWidget = new CompanyFollowWidget(companyDTO);
            followWidget.setName("company");
            followWidget.getElement().getStyle().setMarginLeft(10, com.google.gwt.dom.client.Style.Unit.PX);
            tags.add(followWidget);
        }

        MaterialPanel tabsPanel = createTabsPanel();
        materialTab = createTabs(tabsPanel);
        int numTabs = 4;
        int size = (int) Math.floor(12 / numTabs);

        // add actions
        {
            addAction("WEBSITE", new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Window.open(companyDescriptionDTO.getWebsite(), "_blank;", null);
                }
            });
            addAction("CONTACT", "#" + PlaceHistoryHelper.convertPlace(
                    new ConversationPlace(
                            ConversationPlace.TOKENS.companyid.toString() + "=" + companyDescriptionDTO.getId() +
                                    "&" + ConversationPlace.TOKENS.topic.toString() + "=Request for information"
                    )));
        }
        // create full description panel
        MaterialPanel fullDescriptionPanel = createFullDescriptionPanel(companyDescriptionDTO.getFullDescription());
        addTab(materialTab, tabsPanel, "Description", fullDescriptionPanel, size, "description");

        // create tab panel for offers
        MaterialPanel offeringPanel = new MaterialPanel();
        setMatchingServices(offeringPanel, companyDescriptionDTO.getProductServices(), "#" + PlaceHistoryHelper.convertPlace(new SearchPagePlace(Utils.generateTokens(
                SearchPagePlace.TOKENS.category.toString(), Category.productservices.toString()))));
        setMatchingDatasets(offeringPanel, companyDescriptionDTO.getProductDatasets(), "#" + PlaceHistoryHelper.convertPlace(new SearchPagePlace(Utils.generateTokens(
                SearchPagePlace.TOKENS.category.toString(), Category.productdatasets.toString()))));
        setMatchingSoftwares(offeringPanel, companyDescriptionDTO.getSoftware(), "#" + PlaceHistoryHelper.convertPlace(new SearchPagePlace(Utils.generateTokens(
                SearchPagePlace.TOKENS.category.toString(), Category.software.toString()))));
        setMatchingProjects(offeringPanel, companyDescriptionDTO.getProject(), "#" + PlaceHistoryHelper.convertPlace(new SearchPagePlace(Utils.generateTokens(
                SearchPagePlace.TOKENS.category.toString(), Category.project.toString()))));
        addTab(materialTab, tabsPanel, "Offering", offeringPanel, size, "offering");
        MaterialRow credentialsPanel = new MaterialRow();
        {
            MaterialColumn materialColumn = new MaterialColumn(12, 6, 6);
            materialColumn.add(createColumnSection("Testimonials received"));
            List<TestimonialDTO> testimonials = companyDescriptionDTO.getTestimonials();
            boolean hasTestimonials = testimonials != null && testimonials.size() > 0;
            if (hasTestimonials) {
                materialColumn.add(new MaterialLabel("This company has received " + testimonials.size() + " testimonials"));
                MaterialRow materialRow = new MaterialRow();
                materialColumn.add(materialRow);
                for (TestimonialDTO testimonialDTO : testimonials) {
                    MaterialColumn testimonialColumn = new MaterialColumn(12, 12, 12);
                    TestimonialWidget testimonialWidget = new TestimonialWidget(testimonialDTO);
                    testimonialWidget.displayTopic(false);
                    testimonialColumn.add(testimonialWidget);
                    materialRow.add(testimonialColumn);
                }
            } else {
                materialColumn.add(new MaterialLabel("This company has not received any testimonials yet"));
            }
            MaterialPanel materialPanel = new MaterialPanel();
            MaterialLabel label = new MaterialLabel("Have you worked with " + companyDescriptionDTO.getName() + "? ");
            label.setDisplay(Display.INLINE_BLOCK);
            materialPanel.add(label);
            MaterialLink addTestimonial = new MaterialLink("Add your own testimonial");
            addTestimonial.setPaddingLeft(10);
            addTestimonial.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    clientFactory.getPlaceController().goTo(new TestimonialPlace(Utils.generateTokens(
                            TestimonialPlace.TOKENS.category.toString(), Category.companies.toString(),
                            TestimonialPlace.TOKENS.id.toString(), companyDescriptionDTO.getId() + "")));
                }
            });
            materialPanel.add(addTestimonial);
            materialPanel.setMarginTop(20);
            materialColumn.add(materialPanel);
            credentialsPanel.add(materialColumn);
        }
        {
            MaterialColumn materialColumn = new MaterialColumn(12, 6, 6);
            materialColumn.add(createColumnSection("Awards received"));
            List<String> awards = companyDescriptionDTO.getAwards();
            boolean hasAwards = awards != null && awards.size() > 0;
            if(hasAwards) {
                materialColumn.add(new MaterialLabel("This company has received " + awards.size() + " awards"));
                MaterialPanel materialPanel = new MaterialPanel();
                materialPanel.setPaddingLeft(20);
                for (String award : awards) {
                    MaterialLink awardLabel = new MaterialLink(award);
                    awardLabel.setIconType(IconType.STAR);
                    awardLabel.setDisplay(Display.BLOCK);
                    awardLabel.setMarginTop(20);
                    materialPanel.add(awardLabel);
                }
                materialColumn.add(materialPanel);
            } else {
                materialColumn.add(new MaterialLabel("This company has not received any awards yet"));
            }
            credentialsPanel.add(materialColumn);
        }
        addTab(materialTab, tabsPanel, "Credentials", credentialsPanel, size, "credentials");
        materialTab.selectTab("description");
        tabsContent.add(tabsPanel);

        // TODO - change?
        this.tabsPanel.clear();
        this.tabsPanel.add(materialTab);

        // add recommendations
        recommendationsPanel.clear();
        recommendationsLabel.setText("Other similar companies...");
        List<CompanyDTO> suggestedCompanies = companyDescriptionDTO.getSuggestedCompanies();
        if(suggestedCompanies == null || suggestedCompanies.size() == 0) {
            MaterialLabel materialLabel = new MaterialLabel("No suggestions...");
            materialLabel.setPadding(20);
            recommendationsPanel.add(materialLabel);
        } else {
            MaterialRow materialRow = new MaterialRow();
            recommendationsPanel.add(materialRow);
            for (CompanyDTO companyDTO : suggestedCompanies) {
                MaterialColumn materialColumn = new MaterialColumn(12, 6, 3);
                CompanyWidget companyWidget = new CompanyWidget(companyDTO);
                materialColumn.add(companyWidget);
                materialRow.add(materialColumn);
            }
        }
    }

    private MaterialPanel createFullDescriptionPanel(String fullDescription) {
        MaterialPanel fullDescriptionPanel = new MaterialPanel();
        HTML fullDescriptionHTML = new HTML(fullDescription);
        fullDescriptionHTML.getElement().getStyle().setProperty("minHeight", "6em");
        fullDescriptionPanel.add(fullDescriptionHTML);
        fullDescriptionPanel.setPadding(10);
        return fullDescriptionPanel;
    }

    private void addBreadcrumb(Object dto, Category category) {
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

    private void addAction(String label, String url) {
        MaterialAnchorButton materialAnchorButton = addAction(label);
        materialAnchorButton.setHref(url);
    }

    private void addAction(String label, ClickHandler clickHandler) {
        MaterialAnchorButton materialAnchorButton = addAction(label);
        materialAnchorButton.addClickHandler(clickHandler);
    }

    private MaterialAnchorButton addAction(String label) {
        MaterialAnchorButton materialAnchorButton = new MaterialAnchorButton(label);
        materialAnchorButton.setMarginLeft(20);
        actions.add(materialAnchorButton);
        return materialAnchorButton;
    }

    private DataAccessWidget createDataAccessWidgetProductDataset(final ProductDatasetDescriptionDTO productDatasetDescriptionDTO, final DatasetAccess datasetAccess, final boolean freeAvailable) {
        DataAccessWidget dataAccessWidget = createDataAccessWidget(datasetAccess, freeAvailable);
        dataAccessWidget.getAction().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(datasetAccess instanceof DatasetAccessFile) {
                    String fileUri = datasetAccess.getUri();
                    if(fileUri.startsWith("./")) {
                        fileUri = GWT.getHostPageBaseURL() + "uploaded/" + fileUri;
                    }
                    Window.open(fileUri, "_blank", null);
                } else if(datasetAccess instanceof DatasetAccessAPP) {
                    Window.open(datasetAccess.getUri(), "_blank", null);
                } else if(datasetAccess instanceof DatasetAccessOGC) {
                    if(freeAvailable) {
                        clientFactory.getPlaceController().goTo(new VisualisationPlace(
                                Utils.generateTokens(
                                        VisualisationPlace.TOKENS.productDatasetId.toString(), productDatasetDescriptionDTO.getId() + "",
                                        VisualisationPlace.TOKENS.dataAccessId.toString(), datasetAccess.getId() + ""
                                )));
                    } else {
                        // just open the service web page
                        Window.open(datasetAccess.getUri(), "_blank", null);
                    }
                } else if(datasetAccess instanceof DatasetAccessAPI) {
                    Window.alert("TODO - show API end point and redirect to API help page? eg " + datasetAccess.getUri());
                }
            }
        });
        return dataAccessWidget;
    }

    private DataAccessWidget createDataAccessWidgetProductService(final ProductServiceDescriptionDTO productServiceDescriptionDTO, final DatasetAccess datasetAccess, final boolean freeAvailable) {
        DataAccessWidget dataAccessWidget = createDataAccessWidget(datasetAccess, freeAvailable);
        dataAccessWidget.getAction().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (datasetAccess instanceof DatasetAccessFile) {
                    String fileUri = datasetAccess.getUri();
                    if (fileUri.startsWith("./")) {
                        fileUri = GWT.getHostPageBaseURL() + "uploaded/" + fileUri;
                    }
                    Window.open(fileUri, "_blank", null);
                } else if (datasetAccess instanceof DatasetAccessAPP) {
                    Window.open(datasetAccess.getUri(), "_blank", null);
                } else if (datasetAccess instanceof DatasetAccessOGC) {
                    if (freeAvailable) {
                        clientFactory.getPlaceController().goTo(new VisualisationPlace(
                                Utils.generateTokens(
                                        VisualisationPlace.TOKENS.productServiceId.toString(), productServiceDescriptionDTO.getId() + "",
                                        VisualisationPlace.TOKENS.dataAccessId.toString(), datasetAccess.getId() + ""
                                )));
                    }
                } else if (datasetAccess instanceof DatasetAccessAPI) {
                    Window.alert("TODO - show API end point and redirect to API help page? eg " + datasetAccess.getUri());
                }
            }
        });
        return dataAccessWidget;
    }

    private DataAccessWidget createDataAccessWidget(final DatasetAccess datasetAccess, final boolean freeAvailable) {
        DataAccessWidget dataAccessWidget = new DataAccessWidget(datasetAccess, freeAvailable);
        dataAccessWidget.getElement().getStyle().setMarginTop(20, com.google.gwt.dom.client.Style.Unit.PX);
        dataAccessWidget.getElement().getStyle().setMarginBottom(20, com.google.gwt.dom.client.Style.Unit.PX);
        return dataAccessWidget;
    }

    private void addColumnLine(MaterialWidget materialWidget) {
        MaterialColumn materialColumn = new MaterialColumn(12, 12, 12);
        materialColumn.add(materialWidget);
        tabsContent.add(materialColumn);
    }

    private void addColumnSection(String message) {
        addColumnLine(createColumnSection(message));
    }

    private MaterialLabel createColumnSection(String message) {
        MaterialLabel label = new MaterialLabel(message);
        label.addStyleName(style.section());
        return label;
    }

    private void addTab(MaterialTab materialTab, MaterialPanel tabPanel, String name, Panel panel, int size, String tabId) {
        MaterialTabItem materialTabItem = new MaterialTabItem();
        materialTabItem.setWaves(WavesType.GREEN);
        materialTabItem.setGrid("s" + size + " m" + size + " l" + size);
        MaterialLink materialLink = new MaterialLink(name);
        materialLink.setHref("#" + tabId);
        materialLink.setTextColor(Color.WHITE);
        materialTabItem.add(materialLink);
        materialTab.add(materialTabItem);
        MaterialColumn materialColumn = new MaterialColumn(12, 12, 12);
        tabPanel.add(materialColumn);
        materialColumn.add(panel);
        materialColumn.setId(tabId);
        panel.addStyleName(style.tabPanel());
    }

    private MaterialPanel createTabsPanel() {
        MaterialPanel materialPanel = new MaterialPanel();
        return materialPanel;
    }

    private MaterialTab createTabs(MaterialPanel materialPanel) {
        MaterialTab materialTab = new MaterialTab();
        materialTab.setBackgroundColor(Color.TRANSPARENT);
        materialTab.setIndicatorColor(Color.WHITE);
        MaterialColumn materialColumn = new MaterialColumn(12, 12, 12);
        materialPanel.add(materialColumn);
        materialColumn.add(materialTab);
        return materialTab;
    }

    private MaterialLabel createSubsection(String message) {
        MaterialLabel label = new MaterialLabel(message);
        label.addStyleName(style.subsection());
        return label;
    }

    private void addTitle(MaterialRow productRow, String message, String style, String moreUrl) {
        MaterialPanel materialPanel = new MaterialPanel();
        if(moreUrl != null) {
            MaterialLink moreLink = new MaterialLink("More");
            moreLink.setTextColor(Color.BLUE);
            moreLink.setFloat(com.google.gwt.dom.client.Style.Float.RIGHT);
            materialPanel.add(moreLink);
            moreLink.setHref(moreUrl);
        }
        MaterialLabel title = new MaterialLabel(message);
        title.setTextColor(Color.BLACK);
        materialPanel.add(title);
        materialPanel.addStyleName(style);
        productRow.add(materialPanel);
    }

    public void setMatchingServices(MaterialPanel container, List<ProductServiceDTO> productServices, String moreUrl) {
        MaterialRow productRow = new MaterialRow();
        container.add(productRow);
        boolean hasMore = productServices != null && productServices.size() > 4;
        addTitle(productRow, "Bespoke services", style.offeringSection(), hasMore ? moreUrl : null);
        if(productServices != null && productServices.size() > 0) {
            if(hasMore) {
                productServices = productServices.subList(0, 4);
            }
            for (ProductServiceDTO productServiceDTO : productServices) {
                MaterialColumn serviceColumn = new MaterialColumn(12, 6, 3);
                productRow.add(serviceColumn);
                serviceColumn.add(new ProductServiceWidget(productServiceDTO));
            }
        } else {
            MaterialLabel label = new MaterialLabel("No services found...");
            label.addStyleName(style.offeringSubSection());
            productRow.add(label);
        }
    }

    public void setMatchingDatasets(MaterialPanel container, List<ProductDatasetDTO> productDatasets, String moreUrl) {
        MaterialRow productRow = new MaterialRow();
        container.add(productRow);
        boolean hasMore = productDatasets != null && productDatasets.size() > 4;
        addTitle(productRow, "Off-the-shelf data", style.offeringSection(), hasMore ? moreUrl : null);
        if(productDatasets != null && productDatasets.size() > 0) {
            if(hasMore) {
                productDatasets = productDatasets.subList(0, 4);
            }
            for (ProductDatasetDTO productDatasetDTO : productDatasets) {
                MaterialColumn serviceColumn = new MaterialColumn(12, 6, 3);
                productRow.add(serviceColumn);
                serviceColumn.add(new ProductDatasetWidget(productDatasetDTO));
            }
        } else {
            MaterialLabel label = new MaterialLabel("No off-the-shelf data found...");
            label.addStyleName(style.offeringSubSection());
            productRow.add(label);
        }
    }

    public void setMatchingSoftwares(MaterialPanel container, List<SoftwareDTO> softwares, String moreUrl) {
        MaterialRow productRow = new MaterialRow();
        container.add(productRow);
        boolean hasMore = softwares != null && softwares.size() > 4;
        addTitle(productRow, "Software solutions", style.offeringSection(), hasMore ? moreUrl : null);
        if(softwares != null && softwares.size() > 0) {
            if(hasMore) {
                softwares = softwares.subList(0, 4);
            }
            for (SoftwareDTO softwareDTO : softwares) {
                MaterialColumn serviceColumn = new MaterialColumn(12, 6, 3);
                productRow.add(serviceColumn);
                serviceColumn.add(new SoftwareWidget(softwareDTO));
            }
        } else {
            MaterialLabel label = new MaterialLabel("No software solutions found...");
            label.addStyleName(style.offeringSubSection());
            productRow.add(label);
        }
    }

    public void setMatchingProjects(MaterialPanel container, List<ProjectDTO> projects, String moreUrl) {
        MaterialRow productRow = new MaterialRow();
        container.add(productRow);
        boolean hasMore = projects != null && projects.size() > 4;
        addTitle(productRow, "R&D Projects", style.offeringSection(), hasMore ? moreUrl : null);
        if(projects != null && projects.size() > 0) {
            if(hasMore) {
                projects = projects.subList(0, 4);
            }
            for (ProjectDTO projectDTO : projects) {
                MaterialColumn serviceColumn = new MaterialColumn(12, 6, 3);
                productRow.add(serviceColumn);
                serviceColumn.add(new ProjectWidget(projectDTO));
            }
        } else {
            MaterialLabel label = new MaterialLabel("No projects found...");
            label.addStyleName(style.offeringSubSection());
            productRow.add(label);
        }
    }

    public void setMatchingImagery(MaterialPanel container, String text) {
        MaterialRow productRow = new MaterialRow();
        container.add(productRow);
        addTitle(productRow, "Search or request imagery for '" + text + "'", style.section(), null);
        MaterialColumn serviceColumn = new MaterialColumn(12, 6, 4);
        productRow.add(serviceColumn);
        serviceColumn.add(new ImageSearchWidget(text));
        serviceColumn = new MaterialColumn(12, 6, 4);
        productRow.add(serviceColumn);
        serviceColumn.add(new ImageRequestWidget(text));
    }

    @Override
    public Widget asWidget() {
        return this;
    }

}