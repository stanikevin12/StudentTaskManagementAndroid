package com.example.studenttaskmanagement.model;

public class Attachment {
    private long id;
    private long taskId;
    private String filePath;
    private String type;

    public Attachment() {
    }

    public Attachment(long id, long taskId, String filePath, String type) {
        this.id = id;
        this.taskId = taskId;
        this.filePath = filePath;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
