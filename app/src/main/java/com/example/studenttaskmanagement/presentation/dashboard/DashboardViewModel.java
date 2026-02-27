package com.example.studenttaskmanagement.presentation.dashboard;

import com.example.studenttaskmanagement.database.dao.StudySessionDao;
import com.example.studenttaskmanagement.utils.WeekTimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardViewModel {

    private final StudySessionDao studySessionDao;

    public DashboardViewModel(StudySessionDao studySessionDao) {
        this.studySessionDao = studySessionDao;
    }

    public DashboardUiState loadWeeklySummary() {
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
            return DashboardUiState.empty("No study sessions yet this week.");
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

        return DashboardUiState.content(cards);
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
}
