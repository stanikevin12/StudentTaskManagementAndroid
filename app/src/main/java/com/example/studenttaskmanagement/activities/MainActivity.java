package com.example.studenttaskmanagement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.auth.SessionManager;
import com.example.studenttaskmanagement.database.dao.StudySessionDao;
import com.example.studenttaskmanagement.database.dao.TaskDao;
import com.example.studenttaskmanagement.notifications.NotificationStartup;
import com.example.studenttaskmanagement.presentation.dashboard.DashboardKpiCard;
import com.example.studenttaskmanagement.presentation.dashboard.DashboardUiState;
import com.example.studenttaskmanagement.presentation.dashboard.DashboardViewModel;
import com.example.studenttaskmanagement.presentation.dashboard.ProjectCompletionForecast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private MaterialToolbar toolbarMain;
    private TextView textDebug;
    private ProgressBar progressDashboard;
    private TextView textDashboardState;
    private LinearLayout layoutDashboardCards;
    private TextView[] dashboardLabelViews;
    private TextView[] dashboardValueViews;
    private TextView[] dashboardTrendViews;
    private MaterialButton buttonSeeAllTasks;

    private TextView textForecastCompletion;
    private TextView textForecastEta;
    private TextView textForecastRisk;

    private DashboardViewModel dashboardViewModel;
    private SessionManager sessionManager;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationStartup.initialize(getApplicationContext());
        sessionManager = new SessionManager(this);

        bindViews();
        setupToolbar();
        setupActions();

        showDashboardLoading("Loading dashboard...");
        initDashboard();
    }

    private void bindViews() {
        toolbarMain = findViewById(R.id.toolbarMain);
        textDebug = findViewById(R.id.textDebug);

        progressDashboard = findViewById(R.id.progressDashboard);
        textDashboardState = findViewById(R.id.textDashboardState);
        layoutDashboardCards = findViewById(R.id.layoutDashboardCards);
        buttonSeeAllTasks = findViewById(R.id.buttonSeeAllTasks);

        textForecastCompletion = findViewById(R.id.textForecastCompletion);
        textForecastEta = findViewById(R.id.textForecastEta);
        textForecastRisk = findViewById(R.id.textForecastRisk);

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
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarMain);
        toolbarMain.setTitle("Dashboard");
    }

    private void setupActions() {
        buttonSeeAllTasks.setOnClickListener(v -> startActivity(new Intent(this, TasksActivity.class)));
    }

    private void initDashboard() {
        dbExecutor.execute(() -> {
            try {
                dashboardViewModel = new DashboardViewModel(
                        new StudySessionDao(getApplicationContext()),
                        new TaskDao(getApplicationContext())
                );
                DashboardUiState dashboardUiState = dashboardViewModel.loadWeeklySummary(sessionManager.getLoggedInUserId());

                mainHandler.post(() -> {
                    renderDashboardState(dashboardUiState);
                    setDebug("Dashboard loaded");
                });
            } catch (Throwable t) {
                Log.e(TAG, "Dashboard load failed", t);
                mainHandler.post(() -> {
                    showDashboardError("Could not load weekly dashboard.");
                    setDebug("Dashboard error: " + t.getClass().getSimpleName());
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dashboardViewModel != null) {
            showDashboardLoading("Refreshing dashboard...");
            initDashboard();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuTasks) {
            startActivity(new Intent(this, TasksActivity.class));
            return true;
        } else if (id == R.id.menuSettings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.menuAbout) {
            new AlertDialog.Builder(this)
                    .setTitle("Student Task Management")
                    .setMessage("A simple app to manage student tasks and study sessions.")
                    .setPositiveButton("OK", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void renderDashboardState(DashboardUiState state) {
        if (state == null) {
            showDashboardError("Could not load weekly dashboard.");
            return;
        }

        renderProjectForecast(state.getCompletionForecast());

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

    private void renderProjectForecast(ProjectCompletionForecast forecast) {
        if (forecast == null) {
            textForecastCompletion.setText("Current completion: --");
            textForecastEta.setText("Estimated completion date: --");
            textForecastRisk.setText("Risk: --");
            textForecastRisk.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            return;
        }

        textForecastCompletion.setText("Current completion: " + forecast.getCompletionPercentText());
        textForecastEta.setText("Estimated completion date: " + forecast.getEstimatedCompletionDateText());

        if (forecast.isAtRisk()) {
            textForecastRisk.setText("At risk");
            textForecastRisk.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else {
            textForecastRisk.setText("On track");
            textForecastRisk.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }
    }

    private void showDashboardLoading(String message) {
        textDashboardState.setVisibility(View.VISIBLE);
        textDashboardState.setText(message == null ? "Loading dashboard..." : message);
        progressDashboard.setVisibility(View.VISIBLE);
        layoutDashboardCards.setVisibility(View.GONE);
    }

    private void showDashboardEmpty(String message) {
        textDashboardState.setVisibility(View.VISIBLE);
        textDashboardState.setText(message == null ? "No study sessions yet this week." : message);
        progressDashboard.setVisibility(View.GONE);
        layoutDashboardCards.setVisibility(View.GONE);
    }

    private void showDashboardError(String message) {
        textDashboardState.setVisibility(View.VISIBLE);
        textDashboardState.setText(message == null ? "Could not load weekly dashboard." : message);
        progressDashboard.setVisibility(View.GONE);
        layoutDashboardCards.setVisibility(View.GONE);
    }

    private void showDashboardContent(List<DashboardKpiCard> cards) {
        if (cards == null || cards.isEmpty()) {
            showDashboardEmpty("No study sessions yet this week.");
            return;
        }

        textDashboardState.setVisibility(View.GONE);
        progressDashboard.setVisibility(View.GONE);
        layoutDashboardCards.setVisibility(View.VISIBLE);

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

    private void setDebug(String msg) {
        Log.d(TAG, msg);
        textDebug.setText(msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}
