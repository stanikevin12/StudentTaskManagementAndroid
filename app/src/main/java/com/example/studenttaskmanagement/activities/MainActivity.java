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
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.adapter.TaskAdapter;
import com.example.studenttaskmanagement.database.dao.StudySessionDao;
import com.example.studenttaskmanagement.database.dao.TaskDao;
import com.example.studenttaskmanagement.model.Task;
import com.example.studenttaskmanagement.notifications.NotificationStartup;
import com.example.studenttaskmanagement.presentation.dashboard.DashboardKpiCard;
import com.example.studenttaskmanagement.presentation.dashboard.DashboardUiState;
import com.example.studenttaskmanagement.presentation.dashboard.DashboardViewModel;
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

    private ProgressBar progressDashboard;
    private TextView textDashboardState;
    private LinearLayout layoutDashboardCards;
    private TextView[] dashboardLabelViews;
    private TextView[] dashboardValueViews;
    private TextView[] dashboardTrendViews;

    private ExtendedFloatingActionButton fabAddTask;

    private TaskAdapter taskAdapter;
    private TaskDao taskDao;
    private StudySessionDao studySessionDao;
    private DashboardViewModel dashboardViewModel;

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

        showLoadingState("Loading tasks...");
        showDashboardLoading("Loading dashboard...");

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

        progressDashboard = findViewById(R.id.progressDashboard);
        textDashboardState = findViewById(R.id.textDashboardState);
        layoutDashboardCards = findViewById(R.id.layoutDashboardCards);

        dashboardLabelViews = new TextView[]{
                findViewById(R.id.textCard1Label),
                findViewById(R.id.textCard2Label),
                findViewById(R.id.textCard3Label),
                findViewById(R.id.textCard4Label)
        };

        dashboardValueViews = new TextView[]{
                findViewById(R.id.textCard1Value),
                findViewById(R.id.textCard2Value),
                findViewById(R.id.textCard3Value),
                findViewById(R.id.textCard4Value)
        };

        dashboardTrendViews = new TextView[]{
                findViewById(R.id.textCard1Trend),
                findViewById(R.id.textCard2Trend),
                findViewById(R.id.textCard3Trend),
                findViewById(R.id.textCard4Trend)
        };

        fabAddTask = findViewById(R.id.fabAddTask);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarMain);
        toolbarMain.setTitle("My Tasks");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initDaoAndLoad() {
        dbExecutor.execute(() -> {
            try {
                taskDao = new TaskDao(getApplicationContext());
                studySessionDao = new StudySessionDao(getApplicationContext());
                dashboardViewModel = new DashboardViewModel(studySessionDao);

                List<Task> tasks = taskDao.getAllTasks();
                if (tasks == null) tasks = new ArrayList<>();

                DashboardUiState dashboardUiState = dashboardViewModel.loadWeeklySummary();

                List<Task> finalTasks = tasks;
                mainHandler.post(() -> {
                    allTasks.clear();
                    allTasks.addAll(finalTasks);

                    String q = (editTextSearch != null && editTextSearch.getText() != null)
                            ? editTextSearch.getText().toString()
                            : "";

                    applyFilter(q);
                    renderDashboardState(dashboardUiState);
                    setDebug("Loaded tasks: " + allTasks.size());
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
                    showDashboardError("Could not load weekly dashboard.");
                    invalidateOptionsMenu();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadTasksAndDashboardAsync();
    }

    private void reloadTasksAndDashboardAsync() {
        if (taskDao == null || dashboardViewModel == null) {
            showLoadingState("Loading tasks...");
            showDashboardLoading("Loading dashboard...");
            initDaoAndLoad();
            return;
        }

        showLoadingState("Refreshing...");
        showDashboardLoading("Refreshing dashboard...");
        dbExecutor.execute(() -> {
            List<Task> tasks;
            DashboardUiState dashboardUiState;
            try {
                tasks = taskDao.getAllTasks();
                if (tasks == null) tasks = new ArrayList<>();
                dashboardUiState = dashboardViewModel.loadWeeklySummary();
            } catch (Throwable t) {
                Log.e(TAG, "reloadTasksAndDashboardAsync failed", t);
                tasks = new ArrayList<>();
                dashboardUiState = DashboardUiState.error("Could not refresh weekly dashboard.");
            }

            List<Task> finalTasks = tasks;
            DashboardUiState finalDashboardUiState = dashboardUiState;
            mainHandler.post(() -> {
                allTasks.clear();
                allTasks.addAll(finalTasks);

                String q = (editTextSearch != null && editTextSearch.getText() != null)
                        ? editTextSearch.getText().toString()
                        : "";

                applyFilter(q);
                renderDashboardState(finalDashboardUiState);
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

    private void renderDashboardState(DashboardUiState state) {
        if (state == null) {
            showDashboardError("Could not load weekly dashboard.");
            return;
        }

        switch (state.getStatus()) {
            case LOADING:
                showDashboardLoading(state.getMessage());
                break;
            case EMPTY:
                showDashboardEmpty(state.getMessage());
                break;
            case ERROR:
                showDashboardError(state.getMessage());
                break;
            case CONTENT:
            default:
                showDashboardContent(state.getCards());
                break;
        }
    }

    private void showDashboardLoading(String message) {
        if (textDashboardState != null) {
            textDashboardState.setVisibility(View.VISIBLE);
            textDashboardState.setText(message == null ? "Loading dashboard..." : message);
        }
        if (progressDashboard != null) progressDashboard.setVisibility(View.VISIBLE);
        if (layoutDashboardCards != null) layoutDashboardCards.setVisibility(View.GONE);
    }

    private void showDashboardEmpty(String message) {
        if (textDashboardState != null) {
            textDashboardState.setVisibility(View.VISIBLE);
            textDashboardState.setText(message == null ? "No study sessions yet this week." : message);
        }
        if (progressDashboard != null) progressDashboard.setVisibility(View.GONE);
        if (layoutDashboardCards != null) layoutDashboardCards.setVisibility(View.GONE);
    }

    private void showDashboardError(String message) {
        if (textDashboardState != null) {
            textDashboardState.setVisibility(View.VISIBLE);
            textDashboardState.setText(message == null ? "Could not load weekly dashboard." : message);
        }
        if (progressDashboard != null) progressDashboard.setVisibility(View.GONE);
        if (layoutDashboardCards != null) layoutDashboardCards.setVisibility(View.GONE);
    }

    private void showDashboardContent(List<DashboardKpiCard> cards) {
        if (cards == null || cards.isEmpty()) {
            showDashboardEmpty("No study sessions yet this week.");
            return;
        }

        if (textDashboardState != null) textDashboardState.setVisibility(View.GONE);
        if (progressDashboard != null) progressDashboard.setVisibility(View.GONE);
        if (layoutDashboardCards != null) layoutDashboardCards.setVisibility(View.VISIBLE);

        int count = Math.min(cards.size(), dashboardLabelViews.length);
        for (int i = 0; i < count; i++) {
            DashboardKpiCard card = cards.get(i);
            dashboardLabelViews[i].setText(card.getLabel());
            dashboardValueViews[i].setText(card.getValue());
            dashboardTrendViews[i].setText(trendArrow(card.getTrend()) + " " + card.getDeltaText());
            dashboardTrendViews[i].setTextColor(getTrendColor(card.getTrend()));
        }
    }

    private int getTrendColor(DashboardKpiCard.Trend trend) {
        if (trend == DashboardKpiCard.Trend.UP) {
            return ContextCompat.getColor(this, android.R.color.holo_green_dark);
        } else if (trend == DashboardKpiCard.Trend.DOWN) {
            return ContextCompat.getColor(this, android.R.color.holo_red_dark);
        }
        return ContextCompat.getColor(this, android.R.color.darker_gray);
    }

    private String trendArrow(DashboardKpiCard.Trend trend) {
        if (trend == DashboardKpiCard.Trend.UP) return "↑";
        if (trend == DashboardKpiCard.Trend.DOWN) return "↓";
        return "→";
    }

    private void showLoadingState(String message) {
        if (progressLoading != null) progressLoading.setVisibility(View.VISIBLE);

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
