package com.example.studenttaskmanagement.presentation.dashboard;

public class ProjectCompletionForecast {

    private final String completionPercentText;
    private final String estimatedCompletionDateText;
    private final boolean atRisk;

    public ProjectCompletionForecast(String completionPercentText, String estimatedCompletionDateText, boolean atRisk) {
        this.completionPercentText = completionPercentText;
        this.estimatedCompletionDateText = estimatedCompletionDateText;
        this.atRisk = atRisk;
    }

    public String getCompletionPercentText() {
        return completionPercentText;
    }

    public String getEstimatedCompletionDateText() {
        return estimatedCompletionDateText;
    }

    public boolean isAtRisk() {
        return atRisk;
    }
}
