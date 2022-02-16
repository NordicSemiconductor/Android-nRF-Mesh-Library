package no.nordicsemi.android.mesh.transport.schedulerEntry;

import org.junit.Test;

import no.nordicsemi.android.mesh.data.ScheduleEntry;

import static org.junit.Assert.assertEquals;

public class HourSpecTest {

    @Test
    public void should_have_the_correct_values() {
        assertEquals(0x18, ScheduleEntry.Hour.Any.getValue());
        assertEquals(0x19, ScheduleEntry.Hour.Random.getValue());
        assertEquals(0xE, ScheduleEntry.Hour.Value(14).getValue());
    }

    @Test
    public void create_specific_hour_value_max_value_is_23() {
        assertEquals(0x17, ScheduleEntry.Hour.Value(23).getValue());
        assertEquals(0x17, ScheduleEntry.Hour.Value(30).getValue());
    }
}
