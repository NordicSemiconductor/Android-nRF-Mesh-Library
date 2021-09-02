package no.nordicsemi.android.mesh.transport;

import org.junit.Test;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

import static org.junit.Assert.assertEquals;

public class SchedulerActionGetTest {

    final ApplicationKey applicationKey = new ApplicationKey(MeshParserUtils.hexToInt("0456"), MeshParserUtils.toByteArray("63964771734fbd76e3b40519d1d94a48"));

    @Test
    public void test_reading_data(){
        int index = 3;

        SchedulerActionGet schedulerActionGet = new SchedulerActionGet(applicationKey, index);

        assertEquals(1, schedulerActionGet.mParameters.length);
    }
}
