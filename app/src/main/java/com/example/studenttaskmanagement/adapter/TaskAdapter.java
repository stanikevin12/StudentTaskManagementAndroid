package com.example.studenttaskmanagement.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.activities.TaskDetailActivity;
import com.example.studenttaskmanagement.model.Task;
import com.example.studenttaskmanagement.model.TaskStatus;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for rendering task items.
 * Contains only UI binding and click navigation logic.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final Context context;
    private final List<Task> taskList;

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList != null ? taskList : new ArrayList<>();
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
        holder.textStatus.setText(task.getStatus() == TaskStatus.COMPLETED
                ? TaskStatus.LABEL_COMPLETED : TaskStatus.LABEL_PENDING);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TaskDetailActivity.class);
            intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.getId()); // ✅ FIX
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

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        final TextView textTitle;
        final TextView textDeadline;
        final Chip textStatus;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTaskTitle);
            textDeadline = itemView.findViewById(R.id.textTaskDeadline);
            textStatus = itemView.findViewById(R.id.textTaskStatus);
        }
    }
}