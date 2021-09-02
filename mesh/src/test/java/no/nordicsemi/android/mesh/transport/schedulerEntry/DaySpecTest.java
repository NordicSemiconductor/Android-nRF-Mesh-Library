package no.nordicsemi.android.mesh.transport.schedulerEntry;

import org.junit.Test;

import no.nordicsemi.android.mesh.data.ScheduleEntry;

import static org.junit.Assert.assertEquals;

public class DaySpecTest {

    @Test
    public void day_should_set_correct_value() {
        assertEquals(0x00, ScheduleEntry.Day.Any.getValue());
        assertEquals(0x14, ScheduleEntry.Day.Value(20).getValue());
    }

    @Test
    public void create_specific_day_value_max_value_is_31() {
        assertEquals(0x1F, ScheduleEntry.Day.Value(31).getValue());
        assertEquals(0x1F, ScheduleEntry.Day.Value(50).getValue());
    }
}
