package com.geocento.webapps.eobroker.common.client.widgets.forms;

import com.geocento.webapps.eobroker.common.shared.entities.formelements.FormElementValue;
import com.geocento.webapps.eobroker.common.shared.entities.formelements.IntegerFormElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import gwt.material.design.client.ui.MaterialIntegerBox;

/**
 * Created by thomas on 23/06/2016.
 */
public class IntegerEditor extends ElementEditor<IntegerFormElement> {

    private final MaterialIntegerBox integerBox;

    public IntegerEditor() {
        integerBox = new MaterialIntegerBox();
        add(integerBox);
    }

    @Override
    public void setFormElement(IntegerFormElement integerFormElement) {
        super.setFormElement(integerFormElement);
        // TODO - add extra stuff here
        integerBox.setMin(integerFormElement.getMin() + "");
        integerBox.setMax(integerFormElement.getMax() + "");
    }

    @Override
    public FormElementValue getFormElementValue() throws Exception {
        Integer value = integerBox.getValue();
        if(value < formElement.getMin()) {
            throw new Exception("Minimum value is " + formElement.getMin());
        }
        if(value > formElement.getMax()) {
            throw new Exception("Maximum value is " + formElement.getMax());
        }
        FormElementValue formElementValue = new FormElementValue();
        formElementValue.setValue(value + "");
        formElementValue.setName(formElement.getName());
        formElementValue.setFormid(formElement.getFormid());
        return formElementValue;
    }

    @Override
    public void setFormElementValue(String value) {
        setValue(Integer.valueOf(value));
    }

    public void setValue(Integer value) {
        integerBox.setValue(value);
    }

    @Override
    public void setChangeListener(final ChangeListener changeListener) {
        integerBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                changeListener.hasChanged();
            }
        });
    }

}