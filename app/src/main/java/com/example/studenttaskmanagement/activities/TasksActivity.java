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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.adapter.TaskAdapter;
import com.example.studenttaskmanagement.auth.SessionManager;
import com.example.studenttaskmanagement.database.dao.TaskDao;
import com.example.studenttaskmanagement.model.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TasksActivity extends AppCompatActivity {

    private static final String TAG = "TasksActivity";

    private MaterialToolbar toolbarTasks;
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
    private SessionManager sessionManager;
    private final List<Task> allTasks = new ArrayList<>();

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        bindViews();
        setupToolbar();
        setupRecycler();
        setupActions();
        setupSearch();

        sessionManager = new SessionManager(this);

        showLoadingState("Loading tasks...");
        initDaoAndLoad();
    }

    private void bindViews() {
        toolbarTasks = findViewById(R.id.toolbarTasks);
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
        setSupportActionBar(toolbarTasks);
        toolbarTasks.setTitle("My Tasks");
        toolbarTasks.setNavigationOnClickListener(v -> finish());
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
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s == null ? "" : s.toString());
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void initDaoAndLoad() {
        dbExecutor.execute(() -> {
            try {
                taskDao = new TaskDao(getApplicationContext());
                List<Task> tasks = taskDao.getAllTasks(sessionManager.getLoggedInUserId());
                if (tasks == null) tasks = new ArrayList<>();

                List<Task> finalTasks = tasks;
                mainHandler.post(() -> {
                    allTasks.clear();
                    allTasks.addAll(finalTasks);
                    String q = editTextSearch.getText() == null ? "" : editTextSearch.getText().toString();
                    applyFilter(q);
                    setDebug("Loaded tasks: " + allTasks.size());
                });

            } catch (Throwable t) {
                Log.e(TAG, "DB load failed", t);
                mainHandler.post(() -> {
                    setDebug("DB error: " + t.getClass().getSimpleName() + " - " + t.getMessage());
                    showEmptyState("Database error", "Could not load tasks.");
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadTasksAsync();
    }

    private void reloadTasksAsync() {
        if (taskDao == null) {
            initDaoAndLoad();
            return;
        }

        showLoadingState("Refreshing...");
        dbExecutor.execute(() -> {
            List<Task> tasks;
            try {
                tasks = taskDao.getAllTasks(sessionManager.getLoggedInUserId());
                if (tasks == null) tasks = new ArrayList<>();
            } catch (Throwable t) {
                Log.e(TAG, "reloadTasksAsync failed", t);
                tasks = new ArrayList<>();
            }

            List<Task> finalTasks = tasks;
            mainHandler.post(() -> {
                allTasks.clear();
                allTasks.addAll(finalTasks);

                String q = editTextSearch.getText() == null ? "" : editTextSearch.getText().toString();
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
        progressLoading.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        textEmptyTitle.setText(message);
        textEmptySubtitle.setText("Please wait...");
        buttonCreateFirstTask.setVisibility(View.GONE);
        recyclerViewTasks.setVisibility(View.GONE);
    }

    private void showEmptyState(String title, String subtitle) {
        progressLoading.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        textEmptyTitle.setText(title);
        textEmptySubtitle.setText(subtitle);
        buttonCreateFirstTask.setVisibility(View.VISIBLE);
        recyclerViewTasks.setVisibility(View.GONE);
    }

    private void showListState() {
        progressLoading.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        recyclerViewTasks.setVisibility(View.VISIBLE);
    }

    private void openAddTask() {
        startActivity(new Intent(this, AddTaskActivity.class));
    }

    private void setDebug(String msg) {
        Log.d(TAG, msg);
        textDebug.setText(msg);
    }

    private String safeLower(@Nullable String s) {
        return s == null ? "" : s.toLowerCase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tasks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuAddTask) {
            openAddTask();
            return true;
        } else if (id == R.id.menuSettings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.menuLogout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}
