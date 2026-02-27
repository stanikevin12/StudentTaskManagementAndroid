package com.example.studenttaskmanagement;

import com.example.studenttaskmanagement.utils.WeekTimeUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

public class WeekTimeUtilsTest {

    @Test
    public void weekRange_usesLocalWeekStartAndMidnight() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));

            Calendar sample = Calendar.getInstance();
            sample.set(2025, Calendar.MARCH, 12, 15, 45, 0);
            sample.set(Calendar.MILLISECOND, 500);

            WeekTimeUtils.WeekRange range = WeekTimeUtils.getWeekRangeFor(sample.getTimeInMillis());

            Calendar start = Calendar.getInstance();
            start.setTimeInMillis(range.getStartMillis());

            Assert.assertEquals(start.getFirstDayOfWeek(), start.get(Calendar.DAY_OF_WEEK));
            Assert.assertEquals(0, start.get(Calendar.HOUR_OF_DAY));
            Assert.assertEquals(0, start.get(Calendar.MINUTE));
            Assert.assertEquals(0, start.get(Calendar.SECOND));
            Assert.assertEquals(0, start.get(Calendar.MILLISECOND));
            Assert.assertTrue(range.getEndMillis() > range.getStartMillis());
        } finally {
            TimeZone.setDefault(original);
        }
    }
}
