package com.example.studenttaskmanagement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.adapter.TaskAdapter;
import com.example.studenttaskmanagement.database.dao.TaskDao;
import com.example.studenttaskmanagement.model.Task;
import com.example.studenttaskmanagement.notifications.NotificationStartup;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private MaterialToolbar toolbarMain;
    private TextView textDebug;

    private TextInputEditText editTextSearch;
    private ProgressBar progressLoading;

    private RecyclerView recyclerViewTasks;
    private LinearLayout layoutEmptyState;
    private TextView textEmptyTitle;
    private TextView textEmptySubtitle;
    private MaterialButton buttonCreateFirstTask;

    private ExtendedFloatingActionButton fabAddTask;

    private TaskAdapter taskAdapter;
    private TaskDao taskDao;

    private final List<Task> allTasks = new ArrayList<>();

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationStartup.initialize(getApplicationContext());

        bindViews();
        setupToolbar();
        setupRecycler();
        setupActions();
        setupSearch();

        // Always show something immediately (never blank/black)
        showLoadingState("Loading tasks...");

        initDaoAndLoad();
    }

    private void bindViews() {
        toolbarMain = findViewById(R.id.toolbarMain);
        textDebug = findViewById(R.id.textDebug);

        editTextSearch = findViewById(R.id.editTextSearch);
        progressLoading = findViewById(R.id.progressLoading);

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        textEmptyTitle = findViewById(R.id.textEmptyTitle);
        textEmptySubtitle = findViewById(R.id.textEmptySubtitle);
        buttonCreateFirstTask = findViewById(R.id.buttonCreateFirstTask);

        fabAddTask = findViewById(R.id.fabAddTask);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarMain);
        toolbarMain.setTitle("My Tasks");
        // IMPORTANT: do NOT inflate menu here; AppCompat will manage it via onCreateOptionsMenu()
    }

    // ✅ This is the correct way when using setSupportActionBar(toolbar)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // ✅ Handle menu clicks here
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuAddTask) {
            openAddTask();
            return true;
        } else if (id == R.id.menuSettings) {
            openSettings();
            return true;
        } else if (id == R.id.menuAbout) {
            showAboutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupRecycler() {
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(this, new ArrayList<>());
        recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupActions() {
        fabAddTask.setOnClickListener(v -> openAddTask());
        buttonCreateFirstTask.setOnClickListener(v -> openAddTask());
    }

    private void setupSearch() {
        if (editTextSearch == null) return;

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s == null ? "" : s.toString());
            }

            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void initDaoAndLoad() {
        dbExecutor.execute(() -> {
            try {
                taskDao = new TaskDao(getApplicationContext());
                Log.d(TAG, "TaskDao initialized");

                List<Task> tasks = taskDao.getAllTasks();
                if (tasks == null) tasks = new ArrayList<>();

                List<Task> finalTasks = tasks;
                mainHandler.post(() -> {
                    allTasks.clear();
                    allTasks.addAll(finalTasks);

                    String q = (editTextSearch != null && editTextSearch.getText() != null)
                            ? editTextSearch.getText().toString()
                            : "";

                    applyFilter(q);
                    setDebug("Loaded tasks: " + allTasks.size());

                    // force toolbar to refresh menu once activity is ready (helps on some devices)
                    invalidateOptionsMenu();
                });

            } catch (Throwable t) {
                Log.e(TAG, "DB init/load failed", t);
                mainHandler.post(() -> {
                    setDebug("DB error: " + t.getClass().getSimpleName() + " - " + t.getMessage());
                    showEmptyState(
                            "Database error",
                            "Could not load tasks. You can still try adding a new task."
                    );
                    invalidateOptionsMenu();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh when returning from Add/Edit
        reloadTasksAsync();
    }

    private void reloadTasksAsync() {
        if (taskDao == null) {
            // If DAO not ready yet, init/load again
            showLoadingState("Loading tasks...");
            initDaoAndLoad();
            return;
        }

        showLoadingState("Refreshing...");
        dbExecutor.execute(() -> {
            List<Task> tasks;
            try {
                tasks = taskDao.getAllTasks();
                if (tasks == null) tasks = new ArrayList<>();
            } catch (Throwable t) {
                Log.e(TAG, "reloadTasksAsync failed", t);
                tasks = new ArrayList<>();
            }

            List<Task> finalTasks = tasks;
            mainHandler.post(() -> {
                allTasks.clear();
                allTasks.addAll(finalTasks);

                String q = (editTextSearch != null && editTextSearch.getText() != null)
                        ? editTextSearch.getText().toString()
                        : "";

                applyFilter(q);
                setDebug("Refreshed tasks: " + allTasks.size());
            });
        });
    }

    private void applyFilter(@NonNull String rawQuery) {
        String query = rawQuery.trim().toLowerCase();

        List<Task> filtered = new ArrayList<>();
        if (query.isEmpty()) {
            filtered.addAll(allTasks);
        } else {
            for (Task t : allTasks) {
                String title = safeLower(t.getTitle());
                String desc = safeLower(t.getDescription());
                String deadline = safeLower(t.getDeadline());

                if (title.contains(query) || desc.contains(query) || deadline.contains(query)) {
                    filtered.add(t);
                }
            }
        }

        taskAdapter.setTasks(filtered);

        if (allTasks.isEmpty() && query.isEmpty()) {
            showEmptyState("No tasks yet", "Tap “Add Task” to create your first task.");
        } else if (filtered.isEmpty()) {
            showEmptyState("No results", "No tasks match your search.");
        } else {
            showListState();
        }
    }

    private void showLoadingState(String message) {
        if (progressLoading != null) progressLoading.setVisibility(View.VISIBLE);

        // Show empty container with a loading message so the screen is NEVER blank
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        if (textEmptyTitle != null) textEmptyTitle.setText(message);
        if (textEmptySubtitle != null) textEmptySubtitle.setText("Please wait...");
        if (buttonCreateFirstTask != null) buttonCreateFirstTask.setVisibility(View.GONE);

        if (recyclerViewTasks != null) recyclerViewTasks.setVisibility(View.GONE);
    }

    private void showEmptyState(String title, String subtitle) {
        if (progressLoading != null) progressLoading.setVisibility(View.GONE);

        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        if (textEmptyTitle != null) textEmptyTitle.setText(title);
        if (textEmptySubtitle != null) textEmptySubtitle.setText(subtitle);
        if (buttonCreateFirstTask != null) buttonCreateFirstTask.setVisibility(View.VISIBLE);

        if (recyclerViewTasks != null) recyclerViewTasks.setVisibility(View.GONE);
    }

    private void showListState() {
        if (progressLoading != null) progressLoading.setVisibility(View.GONE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        if (recyclerViewTasks != null) recyclerViewTasks.setVisibility(View.VISIBLE);
    }

    private void openAddTask() {
        startActivity(new Intent(MainActivity.this, AddTaskActivity.class));
    }

    private void openSettings() {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Student Task Management")
                .setMessage("A simple app to manage student tasks and study sessions.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void setDebug(String msg) {
        Log.d(TAG, msg);
        if (textDebug != null) textDebug.setText(msg);
    }

    private String safeLower(@Nullable String s) {
        return s == null ? "" : s.toLowerCase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}