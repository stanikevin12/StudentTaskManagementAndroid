package com.example.studenttaskmanagement.model;

public class Priority {
    private long id;
    private String label;

    public Priority() {
    }

    public Priority(long id, String label) {
        this.id = id;
        this.label = label;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
