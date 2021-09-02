package no.nordicsemi.android.mesh.transport;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


import no.nordicsemi.android.mesh.data.ScheduleEntry;
import no.nordicsemi.android.mesh.utils.ArrayUtils;

import static org.junit.Assert.assertEquals;

public class SchedulerStatusTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    byte[] schedulerData = new byte[]{0x1};

    @Mock
    AccessMessage accessMessage;

    @Test
    public void test_reading_data(){
        Mockito.when(accessMessage.getParameters()).thenReturn(ArrayUtils.reverseArray(schedulerData));

        SchedulerStatus schedulerStatus = new SchedulerStatus(accessMessage);

        assertEquals(schedulerStatus.getSchedules(), ScheduleEntry.Action.TurnOn.getValue());
    }
}
