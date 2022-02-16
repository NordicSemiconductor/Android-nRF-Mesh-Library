package no.nordicsemi.android.mesh.transport;

import org.junit.Test;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.data.ScheduleEntry;
import no.nordicsemi.android.mesh.utils.BitReader;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

import static org.junit.Assert.assertEquals;

public class SchedulerActionSetTest {

    final ApplicationKey applicationKey = new ApplicationKey(MeshParserUtils.hexToInt("0456"), MeshParserUtils.toByteArray("63964771734fbd76e3b40519d1d94a48"));
    byte[] schedulerData = new byte[]{(byte) 0x33, (byte) 0x33, (byte) 0x48, (byte) 0x1C, (byte) 0x16, (byte) 0xBC, (byte) 0xC4, (byte) 0x50, (byte) 0x11, (byte) 0x40};

    @Test
    public void test_reading_data() {
        int index = 3;
        ScheduleEntry entry = new ScheduleEntry(new BitReader(schedulerData));

        SchedulerActionSet schedulerActionSet = new SchedulerActionSet(applicationKey, index, entry);

        assertEquals(10, schedulerActionSet.mParameters.length);
    }
}
