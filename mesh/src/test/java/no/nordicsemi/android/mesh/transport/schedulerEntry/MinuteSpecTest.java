package no.nordicsemi.android.mesh.transport.schedulerEntry;

import org.junit.Test;

import no.nordicsemi.android.mesh.data.ScheduleEntry;

import static org.junit.Assert.assertEquals;

public class MinuteSpecTest {

    @Test
    public void minute_should_set_correct_value() {
        assertEquals(0x3C, ScheduleEntry.Minute.Any.getValue());
        assertEquals(0x3D, ScheduleEntry.Minute.Every15.getValue());
        assertEquals(0x3E, ScheduleEntry.Minute.Every20.getValue());
        assertEquals(0x3F, ScheduleEntry.Minute.Random.getValue());
        assertEquals(0x14, ScheduleEntry.Minute.Value(20).getValue());
    }

    @Test
    public void create_specific_minute_value_max_value_is59() {
        assertEquals(0x3B, ScheduleEntry.Minute.Value(59).getValue());
        assertEquals(0x3B, ScheduleEntry.Minute.Value(60).getValue());
    }
}
