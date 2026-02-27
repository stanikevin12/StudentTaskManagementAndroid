package com.example.studenttaskmanagement.utils;

import java.util.Calendar;

public final class WeekTimeUtils {

    private WeekTimeUtils() {
    }

    public static WeekRange getCurrentWeekRange() {
        Calendar now = Calendar.getInstance();
        return getWeekRangeFor(now.getTimeInMillis());
    }

    public static WeekRange getPreviousWeekRange() {
        WeekRange current = getCurrentWeekRange();
        Calendar previousAnchor = Calendar.getInstance();
        previousAnchor.setTimeInMillis(current.getStartMillis());
        previousAnchor.add(Calendar.WEEK_OF_YEAR, -1);
        return getWeekRangeFor(previousAnchor.getTimeInMillis());
    }

    public static WeekRange getWeekRangeFor(long epochMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epochMillis);
        calendar.setFirstDayOfWeek(Calendar.getInstance().getFirstDayOfWeek());

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        while (calendar.get(Calendar.DAY_OF_WEEK) != calendar.getFirstDayOfWeek()) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        long startMillis = calendar.getTimeInMillis();
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        long endMillis = calendar.getTimeInMillis();

        return new WeekRange(startMillis, endMillis);
    }

    public static final class WeekRange {
        private final long startMillis;
        private final long endMillis;

        public WeekRange(long startMillis, long endMillis) {
            this.startMillis = startMillis;
            this.endMillis = endMillis;
        }

        public long getStartMillis() {
            return startMillis;
        }

        public long getEndMillis() {
            return endMillis;
        }
    }
}
