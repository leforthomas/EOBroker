package com.geocento.webapps.eobroker.customer.client.widgets;

/*
 * #%L
 * GwtMaterial
 * %%
 * Copyright (C) 2015 GwtMaterialDesign
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.geocento.webapps.eobroker.common.client.utils.CategoryUtils;
import com.geocento.webapps.eobroker.common.shared.Suggestion;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.TextBox;
import gwt.material.design.client.base.HasActive;
import gwt.material.design.client.constants.Color;
import gwt.material.design.client.constants.IconType;
import gwt.material.design.client.constants.InputType;
import gwt.material.design.client.ui.MaterialIcon;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.MaterialSearchResult;
import gwt.material.design.client.ui.MaterialValueBox;
import gwt.material.design.client.ui.html.Label;

import java.util.List;

//@formatter:off

/**
 * Material Search is a value box component that returs a result based on your search
 *
 * <p>
 * <h3>UiBinder Usage:</h3>
 * <pre>
 * {@code
 * <m:MaterialSearch placeholder="Sample"/>
 * }
 * </pre>
 *
 * <h3>Populating the search result objects</h3>
 * {@code
 *
 * List<SearchObject> objects = new ArrayList<>();
 *
 * private void onInitSearch(){
 *   objects.add(new SearchObject(IconType.POLYMER, "Pushpin", "#!pushpin"));
 *   objects.add(new SearchObject(IconType.POLYMER, "SideNavs", "#!sidenavs"));
 *   objects.add(new SearchObject(IconType.POLYMER, "Scrollspy", "#!scrollspy"));
 *   objects.add(new SearchObject(IconType.POLYMER, "Tabs", "#!tabs"));
 *   txtSearch.setListSearches(objects);
 * }
 *
 * }
 * </p>
 *
 * @author kevzlou7979
 * @author Ben Dol
 * @see <a href="http://gwt-material-demo.herokuapp.com/#navigations">Material Search</a>
 */
//@formatter:on
public class MaterialSearch extends MaterialValueBox<String> implements HasCloseHandlers<String>, HasActive {

    public interface Presenter {
        void textChanged(String text);
        void suggestionSelected(Suggestion suggestion);
        void textSelected(String text);
    }

    private Label label = new Label();
    private MaterialIcon iconSearch = new MaterialIcon(IconType.SEARCH);
    private MaterialIcon iconClose = new MaterialIcon(IconType.CLOSE);

    /**
     * Panel to display the result items
     */
    private MaterialSearchResult searchResult;
    /**
     * Link selected to determine easily during the selection event (up / down key events)
     */
    private MaterialLink selectedLink;
    /**
     * Gets the selected object after Search Finish event
     */
    private Suggestion selectedObject;
    /**
     * -1 means that the selected index is not yet selected.
     * It will increment or decrement once triggere by key up / down events
     */
    private int curSel = -1;

    private Presenter presenter;

    public MaterialSearch() {
        super(new TextBox());
        setType(InputType.SEARCH);
        iconSearch.setLayoutPosition(Style.Position.ABSOLUTE);
        iconSearch.setLeft(10);
        insert(iconSearch, 0);
        //label.add(iconSearch);
        label.getElement().setAttribute("for", "search");
        add(label);
        add(iconClose);
        iconClose.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                CloseEvent.fire(MaterialSearch.this, getText());
            }
        });
        // populate the lists of search result on search panel
        searchResult = new MaterialSearchResult();
        add(searchResult);
        addKeyUpHandler(new KeyUpHandler() {

            private Timer fetchTimer;
            private String currentText = "";

            @Override
            public void onKeyUp(KeyUpEvent event) {
                // Apply selected search
                switch (event.getNativeEvent().getKeyCode()) {
                    case KeyCodes.KEY_ENTER: {
                        if (getCurSel() == -1) {
                            hideListSearches();
                            presenter.textSelected(getText());
/*
                            Customer.clientFactory.getEventBus().fireEvent(new TextSelected(getText()));
*/
                        }
                        MaterialLink selLink = getSelectedLink();
                        reset(selLink.getText());
                        selLink.fireEvent(new GwtEvent<ClickHandler>() {
                            @Override
                            public com.google.gwt.event.shared.GwtEvent.Type<ClickHandler> getAssociatedType() {
                                return ClickEvent.getType();
                            }

                            @Override
                            protected void dispatch(ClickHandler handler) {
                                handler.onClick(null);
                            }
                        });
                        event.stopPropagation();
                    }
                    break;
                    case KeyCodes.KEY_DOWN: {
                        int totalItems = searchResult.getWidgetCount();
                        if (curSel >= totalItems) {
                            setCurSel(getCurSel());
                            applyHighlightedItem((MaterialLink) searchResult.getWidget(curSel - 1));
                        } else {
                            setCurSel(getCurSel() + 1);
                            applyHighlightedItem((MaterialLink) searchResult.getWidget(curSel));
                        }
                        event.stopPropagation();
                    }
                    break;
                    case KeyCodes.KEY_UP: {
                        if (curSel <= -1) {
                            setCurSel(-1);
                            applyHighlightedItem((MaterialLink) searchResult.getWidget(curSel));
                        } else {
                            setCurSel(getCurSel() - 1);
                            applyHighlightedItem((MaterialLink) searchResult.getWidget(curSel));
                        }
                        event.stopPropagation();
                    }
                    break;
                    default:
                        if (fetchTimer != null) {
                            fetchTimer.cancel();
                            fetchTimer = null;
                        }
                        // create a timer to make sure we don't query too soon
                        fetchTimer = new Timer() {

                            @Override
                            public void run() {
                                // make sure we don't refresh options if the text hasn't changed
                                String text = getText();
                                if (text.contentEquals(currentText)) {
                                    return;
                                }
                                currentText = text;
                                presenter.textChanged(currentText);
                                fetchTimer = null;
                            }
                        };
                        // start the timer to make sure we waited long enough
                        fetchTimer.schedule(300);
                        break;
                }
            }
        });
        addCloseHandler(new CloseHandler<String>() {
            @Override
            public void onClose(CloseEvent<String> event) {
                setText("");
                presenter.textChanged("");
                setFocus(true);
            }
        });
        addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                new Timer() {

                    @Override
                    public void run() {
                        hideListSearches();
                    }
                }.schedule(300);
            }
        });
        addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                new Timer() {

                    @Override
                    public void run() {
                        presenter.textChanged("");
                    }
                }.schedule(300);
            }
        });
        hideListSearches();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    private void applyHighlightedItem(MaterialLink link){
        for(int index = 0; index < searchResult.getWidgetCount(); index++) {
            searchResult.getWidget(index).setStyleName("higlighted", false);
        }
        link.addStyleName("higlighted");
        setSelectedLink(link);
    }

    @Override
    public HandlerRegistration addCloseHandler(CloseHandler<String> handler) {
        return addHandler(handler, CloseEvent.getType());
    }

    @Override
    public void setActive(boolean active) {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    public MaterialLink getSelectedLink() {
        return selectedLink;
    }

    public void setSelectedLink(MaterialLink selectedLink) {
        this.selectedLink = selectedLink;
    }

    public void displayListSearches(List<Suggestion> listSearches) {
        // Clear the panel and temp objects
        searchResult.clear();
        if(listSearches == null) {
            return;
        }
        // Populate the search result items
        for(final Suggestion suggestion : listSearches) {
            MaterialLink link = new MaterialLink();
            link.setIconColor(Color.GREY);
            link.setTextColor(Color.BLACK);
            // Generate an icon
            link.setIconType(CategoryUtils.getIconType(suggestion.getCategory()));

            // Generate an image
            link.setText(suggestion.getName());
            link.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    //reset(suggestion.getName());
/*
                    Customer.clientFactory.getEventBus().fireEvent(new SuggestionSelected(suggestion));
*/
                    presenter.suggestionSelected(suggestion);
                }
            });
            searchResult.add(link);
        }
        searchResult.setVisible(true);
    }

    // Resets the search result panel
    private void reset(String keyword){
        curSel = -1;
        setText(keyword);
        searchResult.clear();
    }

    public int getCurSel() {
        return curSel;
    }

    public void setCurSel(int curSel) {
        this.curSel = curSel;
    }

    public void hideListSearches() {
        searchResult.setVisible(false);
    }

/*
    @Override
    public HandlerRegistration addBlurHandler(final BlurHandler handler) {
        return super.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                EventTarget nextFocus = event.getNativeEvent().getRelatedEventTarget();
                if(!Element.is(nextFocus) || Element.as(nextFocus) != searchResult.getElement()) {
                    handler.onBlur(event);
                }
            }
        });
    }
*/
}

