package no.nordicsemi.android.mesh.transport.schedulerEntry;

import org.junit.Test;

import no.nordicsemi.android.mesh.data.ScheduleEntry;

import static org.junit.Assert.assertEquals;

public class SecondSpecTest {

    @Test
    public void second_should_set_correct_value() {
        assertEquals(0x3C, ScheduleEntry.Second.Any.getValue());
        assertEquals(0x3D, ScheduleEntry.Second.Every15.getValue());
        assertEquals(0x3E, ScheduleEntry.Second.Every20.getValue());
        assertEquals(0x3F, ScheduleEntry.Second.Random.getValue());
        assertEquals(0x14, ScheduleEntry.Second.Value(20).getValue());
    }

    @Test
    public void create_specific_second_value_max_value_is59() {
        assertEquals(0x3B, ScheduleEntry.Second.Value(59).getValue());
        assertEquals(0x3B, ScheduleEntry.Second.Value(60).getValue());
    }
}
