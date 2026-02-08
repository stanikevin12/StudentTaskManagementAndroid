package com.example.studenttaskmanagement.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.adapter.StudySessionAdapter;
import com.example.studenttaskmanagement.database.dao.StudySessionDao;
import com.example.studenttaskmanagement.model.StudySession;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity responsible for managing study sessions for a single task.
 * It starts/stops sessions via DAO, and displays session history + total study time.
 */
public class StudySessionActivity extends AppCompatActivity {

    private Button buttonStartSession;
    private Button buttonStopSession;
    private TextView textViewTotalStudyTime;
    private RecyclerView recyclerViewStudySessions;

    private StudySessionDao studySessionDao;
    private StudySessionAdapter studySessionAdapter;

    private long taskId = -1L;
    private long activeSessionId = -1L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_session);

        setTitle("Study Sessions");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        studySessionDao = new StudySessionDao(this);

        bindViews();
        readTaskId();
        setupRecyclerView();
        setupActions();
        refreshSessionData();
        updateButtonsState();
    }

    private void bindViews() {
        buttonStartSession = findViewById(R.id.buttonStartStudySession);
        buttonStopSession = findViewById(R.id.buttonStopStudySession);
        textViewTotalStudyTime = findViewById(R.id.textViewTotalStudyTime);
        recyclerViewStudySessions = findViewById(R.id.recyclerViewStudySessions);
    }

    private void readTaskId() {
        taskId = getIntent().getLongExtra(TaskDetailActivity.EXTRA_TASK_ID, -1L);
        if (taskId <= 0L) {
            Toast.makeText(this, "Invalid task", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        recyclerViewStudySessions.setLayoutManager(new LinearLayoutManager(this));
        studySessionAdapter = new StudySessionAdapter(new ArrayList<>());
        recyclerViewStudySessions.setAdapter(studySessionAdapter);
    }

    private void setupActions() {
        buttonStartSession.setOnClickListener(v -> startSession());
        buttonStopSession.setOnClickListener(v -> stopSession());
    }

    private void startSession() {
        if (taskId <= 0L) {
            return;
        }

        if (activeSessionId != -1L) {
            Toast.makeText(this, "A study session is already running", Toast.LENGTH_SHORT).show();
            return;
        }

        long sessionId = studySessionDao.startSession(taskId, System.currentTimeMillis());
        if (sessionId != -1L) {
            activeSessionId = sessionId;
            Toast.makeText(this, "Study session started", Toast.LENGTH_SHORT).show();
            refreshSessionData();
            updateButtonsState();
        } else {
            Toast.makeText(this, "Unable to start session", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopSession() {
        if (activeSessionId == -1L) {
            Toast.makeText(this, "No active session", Toast.LENGTH_SHORT).show();
            return;
        }

        int updatedRows = studySessionDao.endSession(activeSessionId, System.currentTimeMillis());
        if (updatedRows > 0) {
            activeSessionId = -1L;
            Toast.makeText(this, "Study session stopped", Toast.LENGTH_SHORT).show();
            refreshSessionData();
            updateButtonsState();
        } else {
            Toast.makeText(this, "Unable to stop session", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Refreshes study session history and recomputes total study duration for this task.
     */
    private void refreshSessionData() {
        if (taskId <= 0L) {
            return;
        }

        List<StudySession> sessions = studySessionDao.getSessionsForTask(taskId);
        studySessionAdapter.setSessions(sessions);

        long totalDurationMs = 0L;
        long runningSessionId = -1L;

        for (StudySession session : sessions) {
            totalDurationMs += Math.max(0L, session.getDuration());
            if (session.getEndTime() <= 0L) {
                runningSessionId = session.getId();
            }
        }

        activeSessionId = runningSessionId;
        textViewTotalStudyTime.setText(formatTotalDuration(totalDurationMs));
    }

    private String formatTotalDuration(long durationMillis) {
        long totalMinutes = Math.max(0L, durationMillis / 60000L);
        long hours = totalMinutes / 60L;
        long minutes = totalMinutes % 60L;
        return String.format(Locale.getDefault(), "Total study time: %dh %dm", hours, minutes);
    }

    private void updateButtonsState() {
        boolean isSessionActive = activeSessionId != -1L;
        buttonStartSession.setEnabled(!isSessionActive);
        buttonStopSession.setEnabled(isSessionActive);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
