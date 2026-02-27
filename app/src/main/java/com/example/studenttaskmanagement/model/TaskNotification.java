package com.example.studenttaskmanagement.model;

public class TaskNotification {
    private long id;
    private long taskId;
    private long notifyTimeMillis;
    private int isSent;

    public TaskNotification() {
    }

    public TaskNotification(long id, long taskId, long notifyTimeMillis, int isSent) {
        this.id = id;
        this.taskId = taskId;
        this.notifyTimeMillis = notifyTimeMillis;
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

    public long getNotifyTimeMillis() {
        return notifyTimeMillis;
    }

    public void setNotifyTimeMillis(long notifyTimeMillis) {
        this.notifyTimeMillis = notifyTimeMillis;
    }

    public int getIsSent() {
        return isSent;
    }

    public void setIsSent(int sent) {
        isSent = sent;
    }
}
