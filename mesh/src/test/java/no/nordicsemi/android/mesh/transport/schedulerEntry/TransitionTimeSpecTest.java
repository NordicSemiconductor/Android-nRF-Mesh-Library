package no.nordicsemi.android.mesh.transport.schedulerEntry;

import org.junit.Test;

import no.nordicsemi.android.mesh.data.ScheduleEntry;

import static org.junit.Assert.assertEquals;

public class TransitionTimeSpecTest {

    @Test
    public void transition_time_should_have_correct_values() {
        assertEquals(0x48, new ScheduleEntry.TransitionTime(1,8).getValue());
    }
}
