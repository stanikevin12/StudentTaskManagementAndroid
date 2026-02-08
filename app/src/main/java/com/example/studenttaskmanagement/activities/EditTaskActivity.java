package com.example.studenttaskmanagement.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.database.dao.TaskDao;
import com.example.studenttaskmanagement.model.Task;
import com.example.studenttaskmanagement.model.TaskStatus;

/**
 * Activity responsible for editing an existing task.
 * It loads task data from DAO, allows updates, and saves changes through DAO.
 */
public class EditTaskActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextDeadline;
    private Spinner spinnerStatus;
    private Button buttonUpdateTask;

    private TaskDao taskDao;
    private long taskId = -1L;
    private Task currentTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        taskDao = new TaskDao(this);

        bindViews();
        setupStatusSpinner();
        readTaskId();
        setupActions();
        loadTask();
    }

    private void bindViews() {
        editTextTitle = findViewById(R.id.editTextTaskTitle);
        editTextDescription = findViewById(R.id.editTextTaskDescription);
        editTextDeadline = findViewById(R.id.editTextTaskDeadline);
        spinnerStatus = findViewById(R.id.spinnerTaskStatus);
        buttonUpdateTask = findViewById(R.id.buttonUpdateTask);
    }

    private void setupStatusSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{TaskStatus.LABEL_PENDING, TaskStatus.LABEL_COMPLETED}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
    }

    private void readTaskId() {
        taskId = getIntent().getLongExtra(TaskDetailActivity.EXTRA_TASK_ID, -1L);
        if (taskId <= 0L) {
            Toast.makeText(this, "Invalid task", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupActions() {
        buttonUpdateTask.setOnClickListener(v -> updateTask());
    }

    /**
     * Loads existing task and pre-fills editable fields.
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

        editTextTitle.setText(nonNullText(currentTask.getTitle()));
        editTextDescription.setText(nonNullText(currentTask.getDescription()));
        editTextDeadline.setText(nonNullText(currentTask.getDeadline()));
        spinnerStatus.setSelection(currentTask.getStatus() == TaskStatus.COMPLETED ? 1 : 0);
    }

    /**
     * Validates inputs and updates the task using TaskDao.
     */
    private void updateTask() {
        if (currentTask == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = getTrimmedText(editTextTitle);
        String description = getOptionalText(editTextDescription);
        String deadline = getOptionalText(editTextDeadline);

        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("Title is required");
            editTextTitle.requestFocus();
            return;
        }

        currentTask.setTitle(title);
        currentTask.setDescription(description);
        currentTask.setDeadline(deadline);
        currentTask.setStatus(spinnerStatus.getSelectedItemPosition() == 1
                ? TaskStatus.COMPLETED : TaskStatus.PENDING);

        int updatedRows = taskDao.updateTask(currentTask);
        if (updatedRows > 0) {
            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Unable to update task", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTrimmedText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    @Nullable
    private String getOptionalText(EditText editText) {
        String value = getTrimmedText(editText);
        return TextUtils.isEmpty(value) ? null : value;
    }

    private String nonNullText(@Nullable String value) {
        return value == null ? "" : value;
    }
}
