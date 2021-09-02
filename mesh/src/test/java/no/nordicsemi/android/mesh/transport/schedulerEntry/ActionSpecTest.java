package no.nordicsemi.android.mesh.transport.schedulerEntry;

import org.junit.Test;

import no.nordicsemi.android.mesh.data.ScheduleEntry;

import static org.junit.Assert.assertEquals;

public class ActionSpecTest {

    @Test
    public void action_should_have_the_correct_value() {
        assertEquals(0x0, ScheduleEntry.Action.TurnOff.getValue());
        assertEquals(0x1, ScheduleEntry.Action.TurnOn.getValue());
        assertEquals(0x2, ScheduleEntry.Action.SceneRecall.getValue());
        assertEquals(0xF, ScheduleEntry.Action.NoAction.getValue());
    }
}
