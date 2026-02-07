package com.example.studenttaskmanagement.model;

public class StudySession {
    private long id;
    private long taskId;
    private long startTime;
    private long endTime;
    private long duration;

    public StudySession() {
    }

    public StudySession(long id, long taskId, long startTime, long endTime, long duration) {
        this.id = id;
        this.taskId = taskId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
