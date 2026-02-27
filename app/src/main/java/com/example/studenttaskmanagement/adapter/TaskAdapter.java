package com.example.studenttaskmanagement.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.activities.TaskDetailActivity;
import com.example.studenttaskmanagement.database.dao.PriorityDao;
import com.example.studenttaskmanagement.model.Priority;
import com.example.studenttaskmanagement.model.Task;
import com.example.studenttaskmanagement.model.TaskStatus;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * RecyclerView adapter for rendering task items.
 * Contains only UI binding and click navigation logic.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final Context context;
    private final List<Task> taskList;
    private final Map<Long, Priority> priorityMap = new HashMap<>();

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList != null ? taskList : new ArrayList<>();
        preloadPriorities(context);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.textTitle.setText(task.getTitle() != null ? task.getTitle() : "Untitled Task");
        holder.textDeadline.setText(task.getDeadline() != null ? task.getDeadline() : "No deadline");
        holder.textStatus.setText(TaskStatus.getLabel(task.getStatus()));

        String priorityLabel = getPriorityLabel(task.getPriorityId());
        holder.textPriority.setText("Priority: " + priorityLabel);
        holder.viewPriorityIndicator.setBackgroundColor(resolvePriorityColor(priorityLabel));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TaskDetailActivity.class);
            intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    /**
     * Updates adapter data and refreshes the list.
     */
    public void setTasks(List<Task> tasks) {
        taskList.clear();
        if (tasks != null) {
            taskList.addAll(tasks);
        }
        notifyDataSetChanged();
    }

    private void preloadPriorities(Context context) {
        PriorityDao priorityDao = new PriorityDao(context);
        List<Priority> priorities = priorityDao.getAllPriorities();
        if (priorities == null) return;

        for (Priority priority : priorities) {
            priorityMap.put(priority.getId(), priority);
        }
    }

    private String getPriorityLabel(long priorityId) {
        Priority priority = priorityMap.get(priorityId);
        if (priority == null || priority.getLabel() == null || priority.getLabel().trim().isEmpty()) {
            return "Low";
        }
        return priority.getLabel().trim();
    }

    @ColorInt
    private int resolvePriorityColor(String priorityLabel) {
        String normalized = priorityLabel == null ? "" : priorityLabel.toLowerCase(Locale.ROOT);

        if (normalized.contains("high")) {
            return ContextCompat.getColor(context, android.R.color.holo_red_dark);
        }
        if (normalized.contains("medium")) {
            return ContextCompat.getColor(context, android.R.color.holo_orange_dark);
        }
        return ContextCompat.getColor(context, android.R.color.holo_green_dark);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        final TextView textTitle;
        final TextView textDeadline;
        final Chip textStatus;
        final TextView textPriority;
        final View viewPriorityIndicator;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTaskTitle);
            textDeadline = itemView.findViewById(R.id.textTaskDeadline);
            textStatus = itemView.findViewById(R.id.textTaskStatus);
            textPriority = itemView.findViewById(R.id.textTaskPriority);
            viewPriorityIndicator = itemView.findViewById(R.id.viewTaskPriorityIndicator);
        }
    }
}
