package com.example.studenttaskmanagement.model;

public class TaskNotification {
    private long id;
    private long taskId;
    private String notifyTime;
    private int isSent;

    public TaskNotification() {
    }

    public TaskNotification(long id, long taskId, String notifyTime, int isSent) {
        this.id = id;
        this.taskId = taskId;
        this.notifyTime = notifyTime;
        this.isSent = isSent;
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
