package com.example.studenttaskmanagement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.database.dao.TaskDao;
import com.example.studenttaskmanagement.model.Task;
import com.example.studenttaskmanagement.model.TaskStatus;

/**
 * Shows details for a single task and provides simple actions
 * to edit or delete that task.
 */
public class TaskDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private TextView textViewTitle;
    private TextView textViewDescription;
    private TextView textViewDeadline;
    private TextView textViewStatus;
    private Button buttonEditTask;
    private Button buttonDeleteTask;

    private TaskDao taskDao;
    private long taskId = -1L;
    private Task currentTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        taskDao = new TaskDao(this);

        bindViews();
        readTaskId();
        setupActions();
        loadTask();
    }

    private void bindViews() {
        textViewTitle = findViewById(R.id.textViewTaskTitle);
        textViewDescription = findViewById(R.id.textViewTaskDescription);
        textViewDeadline = findViewById(R.id.textViewTaskDeadline);
        textViewStatus = findViewById(R.id.textViewTaskStatus);
        buttonEditTask = findViewById(R.id.buttonEditTask);
        buttonDeleteTask = findViewById(R.id.buttonDeleteTask);
    }

    private void readTaskId() {
        taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1L);
        if (taskId <= 0L) {
            Toast.makeText(this, "Invalid task", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupActions() {
        buttonEditTask.setOnClickListener(v -> openEditTask());
        buttonDeleteTask.setOnClickListener(v -> showDeleteConfirmation());
    }

    /**
     * Loads task details via DAO and renders them in the UI.
     */
    private void loadTask() {
        if (taskId <= 0L) {
            return;
        }

        currentTask = taskDao.getTaskById(taskId);
        if (currentTask == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textViewTitle.setText(nonNullText(currentTask.getTitle()));
        textViewDescription.setText(nonNullText(currentTask.getDescription()));
        textViewDeadline.setText(nonNullText(currentTask.getDeadline()));
        textViewStatus.setText(currentTask.getStatus() == TaskStatus.COMPLETED
                ? TaskStatus.LABEL_COMPLETED : TaskStatus.LABEL_PENDING);
    }

    private void openEditTask() {
        // Keep navigation simple: hand off task ID to EditTaskActivity.
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        startActivity(intent);
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTask())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes the current task and closes this screen to return to the list.
     */
    private void deleteTask() {
        int deletedRows = taskDao.deleteTask(taskId);
        if (deletedRows > 0) {
            Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Unable to delete task", Toast.LENGTH_SHORT).show();
        }
    }

    private String nonNullText(@Nullable String value) {
        return value == null ? "" : value;
    }
}
