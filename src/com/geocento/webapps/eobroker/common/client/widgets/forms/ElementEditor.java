package com.geocento.webapps.eobroker.common.client.widgets.forms;

import com.geocento.webapps.eobroker.common.shared.entities.formelements.FormElement;
import com.geocento.webapps.eobroker.common.shared.entities.formelements.FormElementValue;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import gwt.material.design.client.constants.IconType;
import gwt.material.design.client.ui.MaterialIcon;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialTooltip;

import java.util.Iterator;

/**
 * Created by thomas on 23/06/2016.
 */
public abstract class ElementEditor<T extends FormElement> extends Composite implements HasWidgets {

    public static interface ChangeListener {
        void hasChanged();
    }

    interface ElementContainerUiBinder extends UiBinder<HTMLPanel, ElementEditor> {
    }

    private static ElementContainerUiBinder ourUiBinder = GWT.create(ElementContainerUiBinder.class);
    @UiField
    HTMLPanel buttons;
    @UiField
    HTMLPanel container;
    @UiField
    MaterialTooltip information;
    @UiField
    MaterialLabel label;
    @UiField
    MaterialIcon informationButton;

    protected T formElement;

    public ElementEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void setFormElement(T element) {
        this.formElement = element;
        setPlaceHolder(element.getName());
        setInformation(element.getDescription());
    }

    protected abstract void setPlaceHolder(String placeHolder);

    public void setLabel(String label) {
        this.label.setText(label);
    }

    public void setInformation(String information) {
        if(information == null) {
            informationButton.setVisible(false);
            return;
        }
        informationButton.setVisible(true);
        this.information.setText(information);
    }

    public void addAction(IconType edit, ClickHandler clickHandler) {
        MaterialIcon materialIcon = new MaterialIcon(edit);
        materialIcon.addClickHandler(clickHandler);
        buttons.add(materialIcon);
    }

    public abstract FormElementValue getFormElementValue() throws Exception;

    public abstract void setFormElementValue(String value);

    // return the form value in a readable format
    public FormElementValue getReadableFormElementValue() throws Exception {
        // default to the standard form element value
        return getFormElementValue();
    }

    public T getFormElement() {
        return formElement;
    }

    public abstract void resetValue();

    public abstract void setChangeListener(ChangeListener changeListener);

    @Override
    public void add(Widget w) {
        container.add(w);
    }

    @Override
    public void clear() {
        container.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return container.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return container.remove(w);
    }

}