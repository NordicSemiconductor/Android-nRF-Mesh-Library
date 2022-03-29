package no.nordicsemi.android.mesh.transport;

import static junit.framework.TestCase.assertNull;

import org.junit.Test;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.data.TimeZoneOffset;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

public class TimeZoneTests {

    final ApplicationKey applicationKey = new ApplicationKey(
            0,
            MeshParserUtils.toByteArray("DEADBEEFDEADBEEFDEADBEEFDEADBEEF"));

    @Test
    public void testTimeZoneOffset() {
        byte[] encodedValues = {(byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0xFF, (byte) 0xFF};
        double[] values = {-1000, -16, 0, 47.75, 1000};
        double[] decodedValues = {-16, -16, 0, 47.75, 47.75};

        for (int n = 0; n < encodedValues.length; n++) {
            assertEquals(
                    TimeZoneOffset.of(encodedValues[n]),
                    TimeZoneOffset.encode(values[n])
            );
            assertEquals(
                    decodedValues[n],
                    TimeZoneOffset.of(encodedValues[n]).getHours(),
                    0
            );
        }
    }

    @Test
    public void testTimeZoneGet() {
        TimeZoneGet message = new TimeZoneGet(applicationKey);
        assertNull(message.mParameters);
    }

    @Test
    public void testTimeZoneStatus() {
        ArrayList<byte[]> encodedValues = new ArrayList<>();

        encodedValues.add(new byte[]{
                (byte) 0x00,
                (byte) 0x40,
                (byte) 0x05, (byte) 0x04, (byte) 0x03, (byte) 0x02, (byte) 0x01,
        });

        encodedValues.add(new byte[]{
                (byte) 0x40,
                (byte) 0xFF,
                (byte) 0x05, (byte) 0x04, (byte) 0x03, (byte) 0xFF, (byte) 0xFF,
        });


        ArrayList<Double> currentTimeZoneOffset = new ArrayList<>();
        currentTimeZoneOffset.add(-16.0);
        currentTimeZoneOffset.add(0.0);

        ArrayList<Double> newTimeZoneOffset = new ArrayList<>();
        newTimeZoneOffset.add(0.0);
        newTimeZoneOffset.add(47.75);

        ArrayList<Long> timeOfChange = new ArrayList<>();
        timeOfChange.add(Long.valueOf(0x0102030405l));
        timeOfChange.add(Long.valueOf(0xFFFF030405l));


        for (int n = 0; n < encodedValues.size(); n++) {
            AccessMessage accessMessage = new AccessMessage();
            accessMessage.setParameters(encodedValues.get(n));
            TimeZoneStatus message = new TimeZoneStatus(accessMessage);
            assertEquals(currentTimeZoneOffset.get(n), Double.valueOf(message.getCurrentTimeZoneOffset().getHours()));
            assertEquals(newTimeZoneOffset.get(n), Double.valueOf(message.getNewTimeZoneOffset().getHours()));
            assertEquals(timeOfChange.get(n), Long.valueOf(message.getTimeOfChange()));
        }
    }

    @Test
    public void testTimeZoneSet() {
        ArrayList<TimeZoneOffset> newTimeZoneOffset = new ArrayList<>();
        newTimeZoneOffset.add(TimeZoneOffset.encode(0.0));
        newTimeZoneOffset.add(TimeZoneOffset.encode(1.0));
        newTimeZoneOffset.add(TimeZoneOffset.encode(1.25));

        ArrayList<Long> timeOfChange = new ArrayList<>();
        timeOfChange.add(Long.valueOf(1));
        timeOfChange.add(Long.valueOf(0xFFFFFFFFFFl));
        timeOfChange.add(Long.valueOf(0xFF020304FFl));

        ArrayList<byte[]> encodedValues = new ArrayList<>();
        encodedValues.add(new byte[]{
                (byte) 0x40,
                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        encodedValues.add(new byte[]{
                (byte) 0x44,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        encodedValues.add(new byte[]{
                (byte) 0x45,
                (byte) 0xFF, (byte) 0x04, (byte) 0x03, (byte) 0x02, (byte) 0xFF});

        for (int n = 0; n < encodedValues.size(); n++) {
            TimeZoneSet message = new TimeZoneSet(applicationKey, newTimeZoneOffset.get(n), timeOfChange.get(n));
            assertArrayEquals(encodedValues.get(n), message.mParameters);
        }
    }
}
