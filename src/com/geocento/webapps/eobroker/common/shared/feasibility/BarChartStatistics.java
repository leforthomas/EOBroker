package com.geocento.webapps.eobroker.common.shared.feasibility;

/**
 * Created by thomas on 15/06/2017.
 */
public class BarChartStatistics extends ValueStatistics {

    String xLabel;
    String yLabel;
    private boolean vertical;

    public BarChartStatistics() {
    }

    public String getxLabel() {
        return xLabel;
    }

    public void setxLabel(String xLabel) {
        this.xLabel = xLabel;
    }

    public String getyLabel() {
        return yLabel;
    }

    public void setyLabel(String yLabel) {
        this.yLabel = yLabel;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public boolean isVertical() {
        return vertical;
    }
}
