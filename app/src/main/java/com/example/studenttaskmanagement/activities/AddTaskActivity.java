package com.example.studenttaskmanagement.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.database.dao.TaskDao;
import com.example.studenttaskmanagement.model.Task;
import com.example.studenttaskmanagement.model.TaskStatus;

/**
 * Activity responsible for collecting task details from the user
 * and saving a new task using {@link TaskDao}.
 */
public class AddTaskActivity extends AppCompatActivity {

    private static final int DEFAULT_STATUS = TaskStatus.PENDING;
    private static final long DEFAULT_CATEGORY_ID = 1L;
    private static final long DEFAULT_PRIORITY_ID = 1L;
    private static final long DEFAULT_USER_ID = 1L;

    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextDeadline;
    private Button buttonSaveTask;

    private TaskDao taskDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        taskDao = new TaskDao(this);

        bindViews();
        setupActions();
    }

    private void bindViews() {
        editTextTitle = findViewById(R.id.editTextTaskTitle);
        editTextDescription = findViewById(R.id.editTextTaskDescription);
        editTextDeadline = findViewById(R.id.editTextTaskDeadline);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);
    }

    private void setupActions() {
        buttonSaveTask.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String title = getTrimmedText(editTextTitle);
        String description = getOptionalText(editTextDescription);
        String deadline = getOptionalText(editTextDeadline);

        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("Title is required");
            editTextTitle.requestFocus();
            return;
        }

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setDeadline(deadline);
        task.setStatus(DEFAULT_STATUS);
        task.setCategoryId(DEFAULT_CATEGORY_ID);
        task.setPriorityId(DEFAULT_PRIORITY_ID);
        task.setUserId(DEFAULT_USER_ID);

        long insertedId = taskDao.insertTask(task);
        if (insertedId != -1) {
            Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Unable to save task", Toast.LENGTH_SHORT).show();
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
}
