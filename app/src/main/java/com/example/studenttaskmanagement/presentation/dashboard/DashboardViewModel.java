package com.example.studenttaskmanagement.presentation.dashboard;

import com.example.studenttaskmanagement.database.dao.StudySessionDao;
import com.example.studenttaskmanagement.database.dao.TaskDao;
import com.example.studenttaskmanagement.model.Task;
import com.example.studenttaskmanagement.model.TaskStatus;
import com.example.studenttaskmanagement.utils.WeekTimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardViewModel {

    private static final String DEADLINE_FORMAT = "yyyy-MM-dd HH:mm";

    private final StudySessionDao studySessionDao;
    private final TaskDao taskDao;

    public DashboardViewModel(StudySessionDao studySessionDao, TaskDao taskDao) {
        this.studySessionDao = studySessionDao;
        this.taskDao = taskDao;
    }

    public DashboardUiState loadWeeklySummary(long userId) {
        ProjectCompletionForecast forecast = buildProjectForecast(userId);

        WeekTimeUtils.WeekRange currentWeek = WeekTimeUtils.getCurrentWeekRange();
        WeekTimeUtils.WeekRange previousWeek = WeekTimeUtils.getPreviousWeekRange();

        int currentCount = studySessionDao.getSessionsInCurrentWeek().size();
        int previousCount = studySessionDao.getSessionsInPreviousWeek().size();

        long currentFocusedMinutes = studySessionDao.getTotalFocusedMinutes(
                currentWeek.getStartMillis(),
                currentWeek.getEndMillis()
        );
        long previousFocusedMinutes = studySessionDao.getTotalFocusedMinutes(
                previousWeek.getStartMillis(),
                previousWeek.getEndMillis()
        );

        double currentAverageMinutes = studySessionDao.getAverageSessionDurationMinutes(
                currentWeek.getStartMillis(),
                currentWeek.getEndMillis()
        );
        double previousAverageMinutes = studySessionDao.getAverageSessionDurationMinutes(
                previousWeek.getStartMillis(),
                previousWeek.getEndMillis()
        );

        StudySessionDao.SessionPlanCompletion currentPlan = studySessionDao.getPlannedVsCompletedSessionCount(
                currentWeek.getStartMillis(),
                currentWeek.getEndMillis()
        );
        StudySessionDao.SessionPlanCompletion previousPlan = studySessionDao.getPlannedVsCompletedSessionCount(
                previousWeek.getStartMillis(),
                previousWeek.getEndMillis()
        );

        if (currentCount == 0 && previousCount == 0) {
            return DashboardUiState.empty("No study sessions yet this week.", forecast);
        }

        List<DashboardKpiCard> cards = new ArrayList<>();
        cards.add(buildCard("Sessions this week", String.valueOf(currentCount), currentCount, previousCount));
        cards.add(buildCard("Focused minutes", String.valueOf(currentFocusedMinutes), currentFocusedMinutes, previousFocusedMinutes));
        cards.add(buildCard(
                "Avg session duration",
                String.format(Locale.getDefault(), "%.0f min", currentAverageMinutes),
                currentAverageMinutes,
                previousAverageMinutes
        ));

        double currentCompletionRate = percentage(currentPlan.getCompletedCount(), currentPlan.getPlannedCount());
        double previousCompletionRate = percentage(previousPlan.getCompletedCount(), previousPlan.getPlannedCount());
        cards.add(buildCard(
                "Planned vs completed",
                currentPlan.getCompletedCount() + " / " + currentPlan.getPlannedCount(),
                currentCompletionRate,
                previousCompletionRate
        ));

        return DashboardUiState.content(cards, forecast);
    }

    private ProjectCompletionForecast buildProjectForecast(long userId) {
        List<Task> tasks = taskDao.getAllTasks(userId);
        if (tasks == null) tasks = new ArrayList<>();

        int total = tasks.size();
        int completed = 0;
        int remaining = 0;
        int completedWithDeadlineInRecentWindow = 0;

        Date nearestPendingDeadline = null;
        Date now = new Date();
        long recentWindowStartMillis = now.getTime() - (14L * 24L * 60L * 60L * 1000L);

        for (Task task : tasks) {
            boolean isCompleted = task.getStatus() == TaskStatus.COMPLETED;
            if (isCompleted) {
                completed++;
            } else {
                remaining++;
            }

            Date deadlineDate = parseDeadline(task.getDeadline());
            if (deadlineDate == null) {
                continue;
            }

            if (!isCompleted && (nearestPendingDeadline == null || deadlineDate.before(nearestPendingDeadline))) {
                nearestPendingDeadline = deadlineDate;
            }

            if (isCompleted && deadlineDate.getTime() >= recentWindowStartMillis && deadlineDate.getTime() <= now.getTime()) {
                completedWithDeadlineInRecentWindow++;
            }
        }

        double completionPercent = total <= 0 ? 0D : (completed * 100D) / total;
        String completionPercentText = String.format(Locale.getDefault(), "%.0f%% (%d/%d tasks)", completionPercent, completed, total);

        String estimatedCompletionDateText = "Insufficient data";
        Date estimatedCompletionDate = null;
        if (remaining <= 0) {
            estimatedCompletionDate = now;
            estimatedCompletionDateText = "Completed";
        } else {
            double velocityTasksPerDay = completedWithDeadlineInRecentWindow / 14D;
            if (velocityTasksPerDay > 0.01D) {
                int daysToFinish = (int) Math.ceil(remaining / velocityTasksPerDay);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(now);
                calendar.add(Calendar.DAY_OF_YEAR, daysToFinish);
                estimatedCompletionDate = calendar.getTime();
                estimatedCompletionDateText = "~ " + formatDate(estimatedCompletionDate);
            }
        }

        boolean atRisk = estimatedCompletionDate != null
                && nearestPendingDeadline != null
                && estimatedCompletionDate.after(nearestPendingDeadline);

        return new ProjectCompletionForecast(
                completionPercentText,
                estimatedCompletionDateText,
                atRisk
        );
    }

    private DashboardKpiCard buildCard(String label, String value, double current, double previous) {
        double deltaPercent = calculateDeltaPercent(current, previous);
        DashboardKpiCard.Trend trend;
        if (deltaPercent > 0.5D) {
            trend = DashboardKpiCard.Trend.UP;
        } else if (deltaPercent < -0.5D) {
            trend = DashboardKpiCard.Trend.DOWN;
        } else {
            trend = DashboardKpiCard.Trend.NEUTRAL;
        }

        String deltaText = String.format(Locale.getDefault(), "%+.0f%% vs last week", deltaPercent);
        return new DashboardKpiCard(label, value, deltaText, trend);
    }

    private double calculateDeltaPercent(double current, double previous) {
        if (Math.abs(previous) < 0.0001D) {
            return Math.abs(current) < 0.0001D ? 0D : 100D;
        }
        return ((current - previous) / previous) * 100D;
    }

    private double percentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0D;
        }
        return (numerator * 100D) / denominator;
    }

    private Date parseDeadline(String deadline) {
        if (deadline == null || deadline.trim().isEmpty()) {
            return null;
        }

        try {
            return new SimpleDateFormat(DEADLINE_FORMAT, Locale.getDefault()).parse(deadline.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date);
    }
}
