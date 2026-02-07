package com.example.studenttaskmanagement.model;

public class TaskNotification {
    private int id;
    private int taskId;
    private String notifyTime;
    private int isSent;

    public TaskNotification() {
    }

    public TaskNotification(int id, int taskId, String notifyTime, int isSent) {
        this.id = id;
        this.taskId = taskId;
        this.notifyTime = notifyTime;
        this.isSent = isSent;
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

    public String getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(String notifyTime) {
        this.notifyTime = notifyTime;
    }

    public int getIsSent() {
        return isSent;
    }

    public void setIsSent(int sent) {
        isSent = sent;
    }
}
