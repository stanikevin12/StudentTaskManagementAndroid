package com.example.studenttaskmanagement.presentation.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardUiState {

    public enum Status {
        LOADING,
        EMPTY,
        CONTENT,
        ERROR
    }

    private final Status status;
    private final String message;
    private final List<DashboardKpiCard> cards;

    public DashboardUiState(Status status, String message, List<DashboardKpiCard> cards) {
        this.status = status;
        this.message = message;
        this.cards = cards == null ? new ArrayList<>() : new ArrayList<>(cards);
    }

    public static DashboardUiState loading(String message) {
        return new DashboardUiState(Status.LOADING, message, Collections.emptyList());
    }

    public static DashboardUiState empty(String message) {
        return new DashboardUiState(Status.EMPTY, message, Collections.emptyList());
    }

    public static DashboardUiState error(String message) {
        return new DashboardUiState(Status.ERROR, message, Collections.emptyList());
    }

    public static DashboardUiState content(List<DashboardKpiCard> cards) {
        return new DashboardUiState(Status.CONTENT, null, cards);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<DashboardKpiCard> getCards() {
        return new ArrayList<>(cards);
    }
}
