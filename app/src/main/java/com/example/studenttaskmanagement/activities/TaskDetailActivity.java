package com.example.studenttaskmanagement.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
 * to edit, delete, or view study sessions for that task.
 */
public class TaskDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private static final String PREFS_NAME = "student_task_prefs";
    private static final String KEY_LAST_OPENED_TASK_ID = "last_opened_task_id";

    private TextView textViewTitle;
    private TextView textViewDescription;
    private TextView textViewDeadline;
    private TextView textViewStatus;
    private Button buttonEditTask;
    private Button buttonDeleteTask;
    private Button buttonStudySessions;

    private TaskDao taskDao;
    private long taskId = -1L;
    private Task currentTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        taskDao = new TaskDao(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bindViews();
        readTaskId();
        readAndPersistLastOpenedTask();
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
        buttonStudySessions = findViewById(R.id.buttonStudySessions);
    }

    private void readTaskId() {
        taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1L);
        if (taskId <= 0L) {
            Toast.makeText(this, "Invalid task", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void readAndPersistLastOpenedTask() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.getLong(KEY_LAST_OPENED_TASK_ID, -1L);
        if (taskId > 0L) {
            preferences.edit().putLong(KEY_LAST_OPENED_TASK_ID, taskId).apply();
        }
    }

    private void setupActions() {
        buttonEditTask.setOnClickListener(v -> openEditTask());
        buttonDeleteTask.setOnClickListener(v -> showDeleteConfirmation());
        buttonStudySessions.setOnClickListener(v -> openStudySessions());
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
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
    }

    private void openStudySessions() {
        Intent intent = new Intent(this, StudySessionActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
    }

    private void shareTask() {
        if (currentTask == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String shareText = "Task: " + nonNullText(currentTask.getTitle())
                + "\nDescription: " + nonNullText(currentTask.getDescription());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Task"));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.menuEditTask) {
            openEditTask();
            return true;
        } else if (itemId == R.id.menuDeleteTask) {
            showDeleteConfirmation();
            return true;
        } else if (itemId == R.id.menuShareTask) {
            shareTask();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String nonNullText(@Nullable String value) {
        return value == null ? "" : value;
    }
}
