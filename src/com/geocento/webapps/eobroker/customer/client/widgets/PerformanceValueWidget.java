package com.geocento.webapps.eobroker.customer.client.widgets;

import com.geocento.webapps.eobroker.common.shared.entities.PerformanceDescription;
import com.geocento.webapps.eobroker.common.shared.entities.PerformanceValue;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import gwt.material.design.client.ui.MaterialCheckBox;
import gwt.material.design.client.ui.MaterialTooltip;

/**
 * Created by thomas on 15/03/2017.
 */
public class PerformanceValueWidget extends Composite {

    interface PerformanceValueWidgetUiBinder extends UiBinder<Widget, PerformanceValueWidget> {
    }

    private static PerformanceValueWidgetUiBinder ourUiBinder = GWT.create(PerformanceValueWidgetUiBinder.class);

    @UiField
    DoubleBox minValue;
    @UiField
    DoubleBox maxValue;
    @UiField
    SpanElement unit;
    @UiField
    MaterialCheckBox performance;
    @UiField
    MaterialTooltip tooltip;
    @UiField
    HTMLPanel details;

    private PerformanceDescription performanceDescription;

    private PerformanceValue performanceValue;

    public PerformanceValueWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));

        performance.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                updateDisplay();
            }
        });

        updateDisplay();
    }

    public void setPerformanceDescription(PerformanceDescription performanceDescription) {
        this.performanceDescription = performanceDescription;
        performance.setText(performanceDescription.getName());
        tooltip.setText(performanceDescription.getDescription());
        unit.setInnerText(performanceDescription.getUnit());
    }

    public PerformanceDescription getPerformanceDescription() {
        return performanceDescription;
    }

    private void updateDisplay() {
        details.setVisible(performance.getValue());
    }

    public PerformanceValue getPerformanceValue() {
        if(!performance.getValue()) {
            return null;
        }
        if(performanceValue == null) {
            performanceValue = new PerformanceValue();
            performanceValue.setPerformanceDescription(performanceDescription);
        }
        performanceValue.setMinValue(minValue.getValue());
        performanceValue.setMaxValue(maxValue.getValue());
        return performanceValue;
    }
}