package com.example.studenttaskmanagement.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.auth.SessionManager;
import com.example.studenttaskmanagement.database.dao.PriorityDao;
import com.example.studenttaskmanagement.database.dao.TaskDao;
import com.example.studenttaskmanagement.database.dao.TaskNotificationDao;
import com.example.studenttaskmanagement.model.Priority;
import com.example.studenttaskmanagement.model.Task;
import com.example.studenttaskmanagement.notifications.NotificationPreferences;
import com.example.studenttaskmanagement.model.TaskStatus;
import com.example.studenttaskmanagement.notifications.NotificationStartup;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.List;

/**
 * Activity responsible for collecting task details from the user
 * and saving a new task using {@link TaskDao}.
 */
public class AddTaskActivity extends AppCompatActivity {

    private static final int DEFAULT_STATUS = TaskStatus.PENDING;
    private static final long DEFAULT_CATEGORY_ID = 1L;

    private static final int REMINDER_NONE = 0;
    private static final int REMINDER_AT_DEADLINE = 1;
    private static final int REMINDER_30_MIN_BEFORE = 2;
    private static final int REMINDER_60_MIN_BEFORE = 3;

    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextDeadline;
    private Spinner spinnerPriority;
    private Spinner spinnerReminder;
    private Button buttonSaveTask;

    private TaskDao taskDao;
    private PriorityDao priorityDao;
    private TaskNotificationDao taskNotificationDao;
    private SessionManager sessionManager;
    private List<Priority> priorities;

    // Deadline picker state
    private final Calendar deadlineCal = Calendar.getInstance();
    private boolean hasDeadline = false;
    private final SimpleDateFormat deadlineFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddTask);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Task");
        }

        taskDao = new TaskDao(this);
        priorityDao = new PriorityDao(this);
        taskNotificationDao = new TaskNotificationDao(this);
        sessionManager = new SessionManager(this);

        bindViews();
        setupPrioritySpinner();
        setupReminderSpinner();
        setupDeadlinePicker();
        setupActions();
    }

    private void bindViews() {
        editTextTitle = findViewById(R.id.editTextTaskTitle);
        editTextDescription = findViewById(R.id.editTextTaskDescription);
        editTextDeadline = findViewById(R.id.editTextTaskDeadline);
        spinnerPriority = findViewById(R.id.spinnerTaskPriority);
        spinnerReminder = findViewById(R.id.spinnerTaskReminder);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);
    }


    private void setupPrioritySpinner() {
        priorities = priorityDao.getAllPriorities();

        String[] labels;
        if (priorities == null || priorities.isEmpty()) {
            labels = new String[]{"Low"};
        } else {
            labels = new String[priorities.size()];
            for (int i = 0; i < priorities.size(); i++) {
                labels[i] = priorities.get(i).getLabel();
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);
    }

    private void setupReminderSpinner() {
        String[] reminderOptions = {"No reminder", "At deadline", "30 min before", "60 min before"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                reminderOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminder.setAdapter(adapter);

        if (NotificationPreferences.areRemindersEnabled(this)) {
            spinnerReminder.setSelection(defaultReminderOptionFromSettings());
        } else {
            spinnerReminder.setSelection(REMINDER_NONE);
        }
    }

    private void setupActions() {
        buttonSaveTask.setOnClickListener(v -> saveTask());
    }

    /**
     * Deadline: picker-only field (professional UX).
     * Tap = pick date + time. Long press = clear.
     */
    private void setupDeadlinePicker() {
        // Make it picker-only
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
                true // 24-hour format
        );

        dialog.show();
    }

    private void saveTask() {
        String title = getTrimmedText(editTextTitle);
        String description = getOptionalText(editTextDescription);

        // If user never picked, store null (clean DB)
        String deadline = hasDeadline ? getTrimmedText(editTextDeadline) : null;

        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("Title is required");
            editTextTitle.requestFocus();
            return;
        }

        hideKeyboard();

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setDeadline(deadline);
        task.setStatus(DEFAULT_STATUS);
        task.setCategoryId(DEFAULT_CATEGORY_ID);
        task.setPriorityId(getSelectedPriorityId());
        long currentUserId = sessionManager.getLoggedInUserId();
        task.setUserId(currentUserId > 0 ? currentUserId : 1L);

        long insertedId = taskDao.insertTask(task);
        if (insertedId != -1) {
            saveReminder(insertedId, deadline);
            Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(0, R.anim.fade_out);
        } else {
            Toast.makeText(this, "Unable to save task", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveReminder(long taskId, @Nullable String deadline) {

        if (!NotificationPreferences.areRemindersEnabled(this)) {
            taskNotificationDao.deleteNotificationByTaskId(taskId);

            // ✅ ensure worker stops if no reminders
            NotificationStartup.updateReminderWorkerSchedule(getApplicationContext());
            return;
        }

        Long reminderTimeMillis =
                getReminderTimeMillis(deadline, spinnerReminder.getSelectedItemPosition());

        if (reminderTimeMillis == null) {
            taskNotificationDao.deleteNotificationByTaskId(taskId);

            NotificationStartup.updateReminderWorkerSchedule(getApplicationContext());
            return;
        }
        taskNotificationDao.upsertNotificationForTask(taskId, reminderTimeMillis);
        NotificationStartup.updateReminderWorkerSchedule(getApplicationContext());
    }

    @Nullable
    private Long getReminderTimeMillis(@Nullable String deadline, int reminderOption) {
        if (reminderOption == REMINDER_NONE || TextUtils.isEmpty(deadline)) return null;

        try {
            long deadlineMillis = deadlineFormat.parse(deadline).getTime();
            long reminderMillis = deadlineMillis;

            if (reminderOption == REMINDER_30_MIN_BEFORE) {
                reminderMillis = deadlineMillis - (30L * 60L * 1000L);
            } else if (reminderOption == REMINDER_60_MIN_BEFORE) {
                reminderMillis = deadlineMillis - (60L * 60L * 1000L);
            }

            // ✅ don't schedule in the past
            if (reminderMillis <= System.currentTimeMillis()) return null;

            return reminderMillis;

        } catch (Exception ignored) {
            return null;
        }
    }



    private long getSelectedPriorityId() {
        if (priorities == null || priorities.isEmpty()) {
            return 1L;
        }

        int pos = spinnerPriority.getSelectedItemPosition();
        if (pos < 0 || pos >= priorities.size()) return priorities.get(0).getId();
        return priorities.get(pos).getId();
    }


    private int defaultReminderOptionFromSettings() {
        int leadMinutes = NotificationPreferences.getDefaultLeadTimeMinutes(this);
        if (leadMinutes == NotificationPreferences.LEAD_TIME_AT_DEADLINE) {
            return REMINDER_AT_DEADLINE;
        }
        if (leadMinutes == NotificationPreferences.LEAD_TIME_60_MIN) {
            return REMINDER_60_MIN_BEFORE;
        }
        return REMINDER_30_MIN_BEFORE;
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception ignored) {
        }
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
}
