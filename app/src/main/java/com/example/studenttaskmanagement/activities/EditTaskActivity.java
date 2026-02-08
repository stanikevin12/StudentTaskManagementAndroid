package com.example.studenttaskmanagement.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.database.dao.TaskDao;
import com.example.studenttaskmanagement.model.Task;
import com.example.studenttaskmanagement.model.TaskStatus;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Activity responsible for editing an existing task.
 */
public class EditTaskActivity extends AppCompatActivity {

    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextDeadline;
    private Spinner spinnerStatus;
    private MaterialButton buttonUpdateTask;

    private TaskDao taskDao;
    private long taskId = -1L;
    private Task currentTask;

    // Deadline picker state
    private final Calendar deadlineCal = Calendar.getInstance();
    private boolean hasDeadline = false;
    private final SimpleDateFormat deadlineFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbarEditTask);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Task");
        }

        taskDao = new TaskDao(this);

        bindViews();
        setupStatusSpinner();
        setupDeadlinePicker();
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

    /**
     * Deadline picker-only field (tap to pick, long-press to clear).
     */
    private void setupDeadlinePicker() {
        editTextDeadline.setKeyListener(null);
        editTextDeadline.setFocusable(false);
        editTextDeadline.setClickable(true);

        editTextDeadline.setOnClickListener(v -> openDatePicker());

        editTextDeadline.setOnLongClickListener(v -> {
            hasDeadline = false;
            editTextDeadline.setText("");
            Toast.makeText(this, "Deadline cleared", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void openDatePicker() {
        Calendar now = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    deadlineCal.set(Calendar.YEAR, year);
                    deadlineCal.set(Calendar.MONTH, month);
                    deadlineCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    openTimePicker();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void openTimePicker() {
        Calendar now = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    deadlineCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    deadlineCal.set(Calendar.MINUTE, minute);
                    deadlineCal.set(Calendar.SECOND, 0);
                    deadlineCal.set(Calendar.MILLISECOND, 0);

                    hasDeadline = true;
                    editTextDeadline.setText(deadlineFormat.format(deadlineCal.getTime()));
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );

        dialog.show();
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

    private void loadTask() {
        if (taskId <= 0L) return;

        currentTask = taskDao.getTaskById(taskId);
        if (currentTask == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTextTitle.setText(nonNullText(currentTask.getTitle()));
        editTextDescription.setText(nonNullText(currentTask.getDescription()));

        // Load deadline into picker state (if exists)
        String deadline = currentTask.getDeadline();
        if (!TextUtils.isEmpty(deadline)) {
            hasDeadline = true;
            editTextDeadline.setText(deadline);

            // If it matches our format, also set calendar so next pick starts from previous value
            try {
                deadlineCal.setTime(deadlineFormat.parse(deadline));
            } catch (ParseException ignored) {
                // If older value is different format, keep text as-is but picker will start from "now"
            }
        } else {
            hasDeadline = false;
            editTextDeadline.setText("");
        }

        spinnerStatus.setSelection(currentTask.getStatus() == TaskStatus.COMPLETED ? 1 : 0);
    }

    private void updateTask() {
        if (currentTask == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = getTrimmedText(editTextTitle);
        String description = getOptionalText(editTextDescription);

        // If user never picked (or cleared), store null
        String deadline = hasDeadline ? getTrimmedText(editTextDeadline) : null;

        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("Title is required");
            editTextTitle.requestFocus();
            return;
        }

        hideKeyboard();

        currentTask.setTitle(title);
        currentTask.setDescription(description);
        currentTask.setDeadline(deadline);
        currentTask.setStatus(spinnerStatus.getSelectedItemPosition() == 1
                ? TaskStatus.COMPLETED : TaskStatus.PENDING);

        int updatedRows = taskDao.updateTask(currentTask);
        if (updatedRows > 0) {
            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(0, R.anim.fade_out);
        } else {
            Toast.makeText(this, "Unable to update task", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(0, R.anim.fade_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getTrimmedText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    @Nullable
    private String getOptionalText(TextInputEditText editText) {
        String value = getTrimmedText(editText);
        return TextUtils.isEmpty(value) ? null : value;
    }

    private String nonNullText(@Nullable String value) {
        return value == null ? "" : value;
    }
}