package com.example.studenttaskmanagement.model;

public class Task {
    private long id;
    private String title;
    private String description;
    private String deadline;
    private int status;
    private long categoryId;
    private long priorityId;
    private long userId;

    public Task() {
    }

    public Task(long id, String title, String description, String deadline, int status,
                long categoryId, long priorityId, long userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.status = status;
        this.categoryId = categoryId;
        this.priorityId = priorityId;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public long getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(long priorityId) {
        this.priorityId = priorityId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
