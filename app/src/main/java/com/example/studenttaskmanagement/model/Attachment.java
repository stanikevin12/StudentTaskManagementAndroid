package com.example.studenttaskmanagement.model;

public class Attachment {
    private int id;
    private int taskId;
    private String filePath;
    private String type;

    public Attachment() {
    }

    public Attachment(int id, int taskId, String filePath, String type) {
        this.id = id;
        this.taskId = taskId;
        this.filePath = filePath;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
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
