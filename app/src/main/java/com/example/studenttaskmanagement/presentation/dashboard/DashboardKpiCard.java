package com.example.studenttaskmanagement.presentation.dashboard;

public class DashboardKpiCard {

    public enum Trend {
        UP,
        DOWN,
        NEUTRAL
    }

    private final String label;
    private final String value;
    private final String deltaText;
    private final Trend trend;

    public DashboardKpiCard(String label, String value, String deltaText, Trend trend) {
        this.label = label;
        this.value = value;
        this.deltaText = deltaText;
        this.trend = trend;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public String getDeltaText() {
        return deltaText;
    }

    public Trend getTrend() {
        return trend;
    }
}
