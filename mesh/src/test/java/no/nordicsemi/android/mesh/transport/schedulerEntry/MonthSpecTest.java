package no.nordicsemi.android.mesh.transport.schedulerEntry;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import no.nordicsemi.android.mesh.data.ScheduleEntry;

import static org.junit.Assert.assertEquals;

public class MonthSpecTest {

    @Test
    public void single_month_should_have_correct_values() {
        assertEquals(0x0001, ScheduleEntry.Month.JANUARY.getValue());
        assertEquals(0x0002, ScheduleEntry.Month.FEBRUARY.getValue());
        assertEquals(0x0004, ScheduleEntry.Month.MARCH.getValue());
        assertEquals(0x0008, ScheduleEntry.Month.APRIL.getValue());
        assertEquals(0x0010, ScheduleEntry.Month.MAY.getValue());
        assertEquals(0x0020, ScheduleEntry.Month.JUNE.getValue());
        assertEquals(0x0040, ScheduleEntry.Month.JULY.getValue());
        assertEquals(0x0080, ScheduleEntry.Month.AUGUST.getValue());
        assertEquals(0x0100, ScheduleEntry.Month.SEPTEMBER.getValue());
        assertEquals(0x0200, ScheduleEntry.Month.OCTOBER.getValue());
        assertEquals(0x0400, ScheduleEntry.Month.NOVEMBER.getValue());
        assertEquals(0x0800, ScheduleEntry.Month.DECEMBER.getValue());
    }

    @Test
    public void combines_months_should_have_correct_values() {
        assertEquals(0x0007, ScheduleEntry.Month.Any(Arrays.asList(ScheduleEntry.Month.JANUARY, ScheduleEntry.Month.FEBRUARY, ScheduleEntry.Month.MARCH)).getValue());
        assertEquals(0x0804, ScheduleEntry.Month.Any(Arrays.asList(ScheduleEntry.Month.MARCH, ScheduleEntry.Month.DECEMBER)).getValue());
        assertEquals(0x0001, ScheduleEntry.Month.Any(Collections.singletonList(ScheduleEntry.Month.JANUARY)).getValue());
    }
}


