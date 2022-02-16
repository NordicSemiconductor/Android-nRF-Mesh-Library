package no.nordicsemi.android.mesh.transport.schedulerEntry;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import no.nordicsemi.android.mesh.data.ScheduleEntry;

import static org.junit.Assert.assertEquals;

public class DayOfWeekSpecTest {

    @Test
    public void single_day_of_weeks_should_have_correct_values() {
        assertEquals(0x0001, ScheduleEntry.DayOfWeek.MONDAY.getValue());
        assertEquals(0x0002, ScheduleEntry.DayOfWeek.TUESDAY.getValue());
        assertEquals(0x0004, ScheduleEntry.DayOfWeek.WEDNESDAY.getValue());
        assertEquals(0x0008, ScheduleEntry.DayOfWeek.THURSDAY.getValue());
        assertEquals(0x0010, ScheduleEntry.DayOfWeek.FRIDAY.getValue());
        assertEquals(0x0020, ScheduleEntry.DayOfWeek.SATURDAY.getValue());
        assertEquals(0x0040, ScheduleEntry.DayOfWeek.SUNDAY.getValue());
    }

    @Test
    public void combines_day_of_weeks_should_have_correct_values() {
        assertEquals(0xB, ScheduleEntry.DayOfWeek.Any(Arrays.asList(ScheduleEntry.DayOfWeek.MONDAY, ScheduleEntry.DayOfWeek.TUESDAY, ScheduleEntry.DayOfWeek.THURSDAY)).getValue());
        assertEquals(0xC, ScheduleEntry.DayOfWeek.Any(Arrays.asList(ScheduleEntry.DayOfWeek.THURSDAY, ScheduleEntry.DayOfWeek.WEDNESDAY)).getValue());
        assertEquals(0x0001, ScheduleEntry.DayOfWeek.Any(Collections.singletonList(ScheduleEntry.DayOfWeek.MONDAY)).getValue());
    }
}
