package com.example.studenttaskmanagement.model;

/**
 * Shared task status constants used across screens.
 */
public final class TaskStatus {

    public static final int PENDING = 0;
    public static final int COMPLETED = 1;
    public static final int NOT_DONE = 2;

    public static final String LABEL_PENDING = "Pending";
    public static final String LABEL_COMPLETED = "Completed";
    public static final String LABEL_NOT_DONE = "Not Done";

    public static final String[] LABELS = new String[]{
            LABEL_PENDING,
            LABEL_COMPLETED,
            LABEL_NOT_DONE
    };

    private TaskStatus() {
        // No instances.
    }

    public static String getLabel(int status) {
        if (status == COMPLETED) return LABEL_COMPLETED;
        if (status == NOT_DONE) return LABEL_NOT_DONE;
        return LABEL_PENDING;
    }
}
