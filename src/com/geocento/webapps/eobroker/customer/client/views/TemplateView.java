package com.geocento.webapps.eobroker.customer.client.views;

import com.geocento.webapps.eobroker.common.client.utils.CategoryUtils;
import com.geocento.webapps.eobroker.common.client.utils.DateUtils;
import com.geocento.webapps.eobroker.common.client.utils.StringUtils;
import com.geocento.webapps.eobroker.common.client.widgets.LoadingWidget;
import com.geocento.webapps.eobroker.common.client.widgets.MaterialMessage;
import com.geocento.webapps.eobroker.common.client.widgets.UserWidget;
import com.geocento.webapps.eobroker.common.shared.Suggestion;
import com.geocento.webapps.eobroker.common.shared.entities.Category;
import com.geocento.webapps.eobroker.common.shared.entities.dtos.LoginInfo;
import com.geocento.webapps.eobroker.common.shared.entities.requests.RequestDTO;
import com.geocento.webapps.eobroker.common.shared.utils.ListUtil;
import com.geocento.webapps.eobroker.customer.client.ClientFactoryImpl;
import com.geocento.webapps.eobroker.customer.client.events.LogOut;
import com.geocento.webapps.eobroker.customer.client.places.*;
import com.geocento.webapps.eobroker.customer.client.utils.NotificationHelper;
import com.geocento.webapps.eobroker.customer.shared.NotificationDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import gwt.material.design.client.base.HasHref;
import gwt.material.design.client.base.SearchObject;
import gwt.material.design.client.constants.ProgressType;
import gwt.material.design.client.ui.*;
import gwt.material.design.jquery.client.api.JQuery;
import gwt.material.design.jquery.client.api.JQueryElement;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by thomas on 09/05/2016.
 */
public class TemplateView extends Composite implements HasWidgets, ResizeHandler {

    interface TemplateViewUiBinder extends UiBinder<Widget, TemplateView> {
    }

    private static TemplateViewUiBinder ourUiBinder = GWT.create(TemplateViewUiBinder.class);

    public static interface Style extends CssResource {

        String navOpened();

        String selected();

        String category();
    }

    public static interface Presenter {

        void textChanged(String text);

        void suggestionSelected(Suggestion suggestion);

        void textSelected(String text);

        void onFocus();
    }

    @UiField
    Style style;

    @UiField
    MaterialContainer mainPanel;
    @UiField
    MaterialLabel title;
    @UiField
    MaterialNavBar navBar;
    @UiField
    MaterialPanel header;
    @UiField
    UserWidget userWidget;
    @UiField
    MaterialLink signIn;
    @UiField
    MaterialImage logo;
    @UiField
    MaterialLink orders;
    @UiField
    MaterialBadge ordersBadge;
    @UiField
    MaterialLink notifications;
    @UiField
    HTMLPanel panel;
    @UiField
    HTMLPanel navbarElements;
    @UiField
    com.geocento.webapps.eobroker.common.client.widgets.material.MaterialSearch textSearch;
    @UiField
    MaterialBadge notificationsBadge;
    @UiField
    MaterialDropDown notificationsPanel;
    @UiField
    MaterialLink signOut;
    @UiField
    MaterialPanel content;
    @UiField
    LoadingWidget loading;
    @UiField
    MaterialSideNav navigationPanel;
    @UiField
    MaterialLink productsCategory;
    @UiField
    MaterialLink productServicesCategory;
    @UiField
    MaterialLink productDatasetsCategory;
    @UiField
    MaterialLink softwareCategory;
    @UiField
    MaterialLink projectsCategory;
    @UiField
    MaterialLink companiesCategory;
    @UiField
    MaterialLink homeCategory;
    @UiField
    HTMLPanel menus;
    @UiField
    MaterialLink conversationsCategory;
    @UiField
    MaterialLink requestsCategory;
    @UiField
    MaterialLink notificationsCategory;
    @UiField
    MaterialLink testimoniesCategory;
    @UiField
    MaterialLink settingsCategory;
    @UiField
    MaterialLink feedbackCategory;
    @UiField
    MaterialLink helpCategory;
    @UiField
    MaterialPanel messagePanel;
    @UiField
    MaterialMessage message;

    private int maxNotifications = 5;

    private final ClientFactoryImpl clientFactory;

    private Presenter presenter;

    public TemplateView(final ClientFactoryImpl clientFactory) {
        this.clientFactory = clientFactory;

        initWidget(ourUiBinder.createAndBindUi(this));

        logo.addClickHandler(event -> clientFactory.getPlaceController().goTo(clientFactory.getDefaultPlace()));

        orders.setHref("#" + PlaceHistoryHelper.convertPlace(new RequestsPlace()));

        Image ogeoLogo = new Image("./images/OGEO_logo single.png");
        ogeoLogo.getElement().getStyle().setPosition(com.google.gwt.dom.client.Style.Position.ABSOLUTE);
        ogeoLogo.getElement().getStyle().setTop(10, com.google.gwt.dom.client.Style.Unit.PX);
        ogeoLogo.getElement().getStyle().setRight(60, com.google.gwt.dom.client.Style.Unit.PX);
        textSearch.add(ogeoLogo);

        // update icons
        productsCategory.setIconType(CategoryUtils.getIconType(Category.products));
        addCategoryTooltip(productsCategory, "See all relevant products");
        productServicesCategory.setIconType(CategoryUtils.getIconType(Category.productservices));
        addCategoryTooltip(productServicesCategory, "See all on demand services able to supply your request");
        productDatasetsCategory.setIconType(CategoryUtils.getIconType(Category.productdatasets));
        addCategoryTooltip(productDatasetsCategory, "See all off the shelf datasets matching your search");
        softwareCategory.setIconType(CategoryUtils.getIconType(Category.software));
        addCategoryTooltip(softwareCategory, "See all software solutions matching your search");
        projectsCategory.setIconType(CategoryUtils.getIconType(Category.project));
        addCategoryTooltip(projectsCategory, "See all relevant projects for your search");
        companiesCategory.setIconType(CategoryUtils.getIconType(Category.companies));
        addCategoryTooltip(companiesCategory, "See all relevant companies for your search");

        // set basic links
        homeCategory.setHref("#" + PlaceHistoryHelper.convertPlace(new LandingPagePlace()));
        conversationsCategory.setHref("#" + PlaceHistoryHelper.convertPlace(new ConversationPlace()));
        requestsCategory.setHref("#" + PlaceHistoryHelper.convertPlace(new RequestsPlace()));
        notificationsCategory.setHref("#" + PlaceHistoryHelper.convertPlace(new NotificationsPlace()));
        testimoniesCategory.setHref("#" + PlaceHistoryHelper.convertPlace(new TestimonialsPlace()));
        settingsCategory.setHref("#" + PlaceHistoryHelper.convertPlace(new SettingsPlace()));
        feedbackCategory.setHref("#" + PlaceHistoryHelper.convertPlace(new FeedbackPlace()));
        //helpCategory.setHref(Customer.getApplicationSettings().getHelpUrl());

        textSearch.setPresenter(new com.geocento.webapps.eobroker.common.client.widgets.material.MaterialSearch.Presenter() {
            @Override
            public void textChanged(String text) {
                presenter.textChanged(text);
            }

            @Override
            public void suggestionSelected(SearchObject searchObject) {
                presenter.suggestionSelected(searchObject == null ? null : (Suggestion) searchObject.getO());
            }

            @Override
            public void textSelected(String text) {
                presenter.textSelected(text);
            }
        });
        textSearch.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                presenter.onFocus();
            }
        });

        navigationPanel.addClosedHandler(event -> Scheduler.get().scheduleDeferred(() -> onResize(null)));

        navigationPanel.addOpenedHandler(event -> Scheduler.get().scheduleDeferred(() -> onResize(null)));

        onResize(null);
    }

    public HasHref getProductsCategory() {
        return productsCategory;
    }

    public HasHref getProductServicesCategory() {
        return productServicesCategory;
    }

    public HasHref getProductDatasetsCategory() {
        return productDatasetsCategory;
    }

    public HasHref getSoftwareCategory() {
        return softwareCategory;
    }

    public HasHref getProjectsCategory() {
        return projectsCategory;
    }

    public HasHref getCompaniesCategory() {
        return companiesCategory;
    }

    public void setMenu(String menuName) {
        JQueryElement menuItems = JQuery.$("." + style.category(), menus);
        menuItems.removeClass(style.selected());
        if(menuName == null) {
            return;
        }
        Widget widget = null;
        switch (menuName) {
            case "home":
                widget = homeCategory;
                break;
            case "products":
                widget = productsCategory;
                break;
            case "productservices":
                widget = productServicesCategory;
                break;
            case "productdatasets":
                widget = productDatasetsCategory;
                break;
            case "software":
                widget = softwareCategory;
                break;
            case "project":
                widget = projectsCategory;
                break;
            case "companies":
                widget = companiesCategory;
                break;
            case "notifications":
                widget = notificationsCategory;
                break;
            case "conversations":
                widget = conversationsCategory;
                break;
            case "requests":
                widget = requestsCategory;
                break;
            case "testimonials":
                widget = testimoniesCategory;
                break;
            case "settings":
                widget = settingsCategory;
                break;
            case "feedback":
                widget = feedbackCategory;
                break;
            case "help":
                widget = helpCategory;
                break;
        }
        if(widget != null) {
            widget.addStyleName(style.selected());
        }
    }

    private void addCategoryTooltip(MaterialLink materialLink, String message) {
/*
        MaterialTooltip materialTooltip = new MaterialTooltip();
        materialTooltip.setWidget(materialLink);
        materialTooltip.setPosition(Position.RIGHT);
        materialTooltip.setText(message);
        categories.add(materialTooltip);
*/
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void setTitleText(String title) {
        this.title.setText(title);
    }

    public ClientFactoryImpl getClientFactory() {
        return clientFactory;
    }

    @UiHandler("signIn")
    void signIn(ClickEvent clickEvent) {
        clientFactory.getEventBus().fireEvent(new PlaceChangeEvent(new LoginPagePlace(clientFactory.getPlaceController().getWhere())));
    }

    @UiHandler("signOut")
    void signOut(ClickEvent clickEvent) {
        clientFactory.getEventBus().fireEvent(new LogOut());
    }

    public void setLoading(String message) {
        displayLoading();
        MaterialToast.fireToast(message);
    }

    @UiChild(tagname = "header")
    public void addHeader(Widget widget) {
        header.add(widget);
    }

    @UiChild(tagname = "navBar")
    public void addNavBarElement(Widget widget) {
        navbarElements.add(widget);
    }

    public void displayLoading() {
        navBar.showProgress(ProgressType.INDETERMINATE);
    }

    public void hideLoading() {
        navBar.hideProgress();
    }

    public void displayError(String message) {
        MaterialToast.fireToast(message, "red darken-1");
    }

    public void displaySuccess(String message) {
        MaterialToast.fireToast(message, "green darken-1");
    }

    public void setRequests(List<RequestDTO> orders) {
        int count = orders == null ? 0 : orders.size();
        ordersBadge.setText(count + "");
    }

    public void setNotifications(List<NotificationDTO> notifications) {
        notificationsBadge.setText(notifications.size() + "");
        notificationsPanel.clear();
        // make sure notifications are ordered by creation date descending
        Collections.sort(notifications, new Comparator<NotificationDTO>() {
            @Override
            public int compare(NotificationDTO o1, NotificationDTO o2) {
                return o2.getCreationDate().compareTo(o1.getCreationDate());
            }
        });
        boolean hasNotifications = notifications != null && notifications.size() > 0;
        notificationsBadge.setVisible(hasNotifications);
        boolean hasMore = notifications.size() > maxNotifications;
        if(hasMore) {
            notifications = notifications.subList(0, maxNotifications);
        }
        if(hasNotifications) {
            for(NotificationDTO notificationDTO : notifications) {
                addNotification(notificationDTO);
            }
        } else {
            notificationsPanel.add(new MaterialLabel("No new notification"));
        }
        // add a link to more notifications
        if(hasMore) {
            MaterialLink materialLink = new MaterialLink("See all notifications");
            materialLink.setTruncate(true);
            materialLink.setFontSize("0.8em");
            materialLink.setPadding(10);
            materialLink.getElement().getStyle().setTextAlign(com.google.gwt.dom.client.Style.TextAlign.CENTER);
            materialLink.getElement().getStyle().setTextDecoration(com.google.gwt.dom.client.Style.TextDecoration.UNDERLINE);
            materialLink.setHref("#" + PlaceHistoryHelper.convertPlace(new NotificationsPlace()));
            notificationsPanel.add(materialLink);
        }
    }

    private void addNotification(NotificationDTO notificationDTO) {
        MaterialLink message = new MaterialLink(notificationDTO.getMessage());
        message.setTruncate(true);
        message.getElement().getStyle().setFontSize(0.8, com.google.gwt.dom.client.Style.Unit.EM);
        HTML timePanel = new HTML("<span style='text-align: right; font-size: 0.8em; color: black;'>" + DateUtils.getDuration(notificationDTO.getCreationDate()) + "</span>");
        timePanel.getElement().getStyle().setTextAlign(com.google.gwt.dom.client.Style.TextAlign.RIGHT);
        message.add(timePanel);
        EOBrokerPlace place = NotificationHelper.createNotificationPlace(notificationDTO);
        message.setHref("#" + PlaceHistoryHelper.convertPlace(place));
        notificationsPanel.add(message);
    }

    public void displaySignedIn(boolean signedIn) {
        //orders.setVisible(signedIn);
        notifications.setVisible(signedIn);
        signIn.setVisible(!signedIn);
        userWidget.setVisible(signedIn);
    }

    public void setUser(LoginInfo loginInfo) {
        userWidget.setUser(loginInfo.getUserName());
    }

    public void displayListSuggestionsLoading(String message) {
        textSearch.displaySearchLoading(message);
    }

    public void hideListSuggestionsLoading() {
        textSearch.hideListSuggestionsLoading();
    }

    public void displayListSuggestionsError(String message) {
        textSearch.displaySearchError(message);
    }

    public void displayListSuggestions(List<Suggestion> suggestions) {
        textSearch.setFocus(true);
        textSearch.displayListSearches(ListUtil.mutate(suggestions, new ListUtil.Mutate<Suggestion, SearchObject>() {
            @Override
            public SearchObject mutate(Suggestion suggestion) {
                SearchObject searchObject = new SearchObject();
                searchObject.setIcon(CategoryUtils.getIconType(suggestion.getCategory()));
                searchObject.setKeyword(suggestion.getName() + (StringUtils.isEmpty(suggestion.getAdditional()) ? "" : (" (" + suggestion.getAdditional() + ")")));
                searchObject.setO(suggestion);
                return searchObject;
            }
        }));
    }

    public void setSearchText(String text) {
        textSearch.setText(text);
    }

    public void setSearchTextFocus(boolean focus) {
        textSearch.setFocus(focus);
    }

    public void displaySearchError(String message) {
        MaterialToast.fireToast(message);
    }

    public void setPanelStyleName(String styleName, boolean added) {
        panel.setStyleName(styleName, added);
    }

    public void scrollToTop() {
        Window.scrollTo(0, 0);
    }

    public void displayContent(boolean display) {
        content.setVisible(display);
        messagePanel.setVisible(!display);
    }

    public void displayFullLoading(String message) {
        displayContent(false);
        loading.setVisible(true);
        this.message.setVisible(false);
        loading.setText(message);
    }

    public void hideFullLoading() {
        displayContent(true);
    }

    public void displayMenu(boolean display) {
        if(display) {
            navigationPanel.show();
        } else {
            navigationPanel.hide();
        }
    }

    public void displayFullError(String message) {
        displayContent(false);
        loading.setVisible(false);
        this.message.setVisible(true);
        this.message.displayErrorMessage(message);
    }

    public void displayWebsocketError(String message) {
        // TODO - change for something more specific
        displayError(message);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void add(Widget w) {
        content.add(w);
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return content.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return content.remove(w);
    }

    @Override
    public void onResize(ResizeEvent event) {
        panel.setStyleName(style.navOpened(), navigationPanel.isOpen());
        // force the hide or show, seems to be a bug
        if(!(navigationPanel.isVisible() && navigationPanel.isOpen())) {
            navigationPanel.setVisible(navigationPanel.isOpen());
        }
    }

}