package no.nordicsemi.android.mesh.transport.schedulerEntry;

import org.junit.Test;

import no.nordicsemi.android.mesh.data.ScheduleEntry;

import static org.junit.Assert.assertEquals;

public class SceneSpecTest {

    @Test
    public void scene_should_have_correct_values() {
        assertEquals(0x0000, ScheduleEntry.Scene.NoScene.getValue());
        assertEquals(0x333, ScheduleEntry.Scene.Address(0x333).getValue());
    }
}
