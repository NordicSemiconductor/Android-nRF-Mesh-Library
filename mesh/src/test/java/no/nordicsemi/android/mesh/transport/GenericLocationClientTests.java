package no.nordicsemi.android.mesh.transport;

import org.junit.Test;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.data.GlobalAltitude;
import no.nordicsemi.android.mesh.data.GlobalLatitude;
import no.nordicsemi.android.mesh.data.GlobalLongitude;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;

public class GenericLocationClientTests {

    final ApplicationKey applicationKey = new ApplicationKey(
            0,
            MeshParserUtils.toByteArray("DEADBEEFDEADBEEFDEADBEEFDEADBEEF"));

    @Test
    public void testGlobalLatitude() {
        int[] encodedValues = {0, 0x80000001, 0x7FFFFFFF};
        double[] values = {0, -90, 90};

        for (int n = 0; n < encodedValues.length; n++) {
            assertEquals(
                    GlobalLatitude.of(encodedValues[n]),
                    GlobalLatitude.encode(values[n]));
            assertEquals(
                    GlobalLatitude.of(encodedValues[n]).getDecodedPosition(),
                    Double.valueOf(values[n]));
            assertEquals(
                    GlobalLatitude.of(encodedValues[n]).getClass(),
                    GlobalLatitude.Coordinate.class);
        }

        assertEquals(
                GlobalLatitude.of(0x80000000).getClass(),
                GlobalLatitude.NotConfigured.class);


        assertNull(GlobalLatitude.of(0x80000000).getDecodedPosition());

        assertThrows(
                IllegalArgumentException.class,
                () -> GlobalLatitude.encode(Math.nextDown(-90)));

        assertThrows(
                IllegalArgumentException.class,
                () -> GlobalLatitude.encode(Math.nextUp(90)));
    }

    @Test
    public void testGlobalLongitude() {
        int[] encodedValues = {0, 0x80000001, 0x7FFFFFFF};
        double[] values = {0, -180, 180};

        for (int n = 0; n < encodedValues.length; n++) {
            assertEquals(
                    GlobalLongitude.of(encodedValues[n]),
                    GlobalLongitude.encode(values[n]));
            assertEquals(
                    GlobalLongitude.of(encodedValues[n]).getDecodedPosition(),
                    Double.valueOf(values[n]));
            assertEquals(
                    GlobalLongitude.of(encodedValues[n]).getClass(),
                    GlobalLongitude.Coordinate.class);
        }

        assertEquals(
                GlobalLongitude.of(0x80000000).getClass(),
                GlobalLongitude.NotConfigured.class);


        assertNull(GlobalLongitude.of(0x80000000).getDecodedPosition());

        assertThrows(
                IllegalArgumentException.class,
                () -> GlobalLongitude.encode(Math.nextDown(-180)));

        assertThrows(
                IllegalArgumentException.class,
                () -> GlobalLongitude.encode(Math.nextUp(180)));
    }

    @Test
    public void testGlobalAltitude() {
        short[] encodedValues = {0, (short) 0x8000, 0x7FFD};
        int[] values = {0, -32768, 32765};

        for (int n = 0; n < encodedValues.length; n++) {
            assertEquals(
                    GlobalAltitude.encode(values[n]),
                    GlobalAltitude.of(encodedValues[n]));
            assertEquals(
                    Integer.valueOf(values[n]),
                    GlobalAltitude.of(encodedValues[n]).getDecodedPosition());
            assertEquals(
                    GlobalAltitude.Coordinate.class,
                    GlobalAltitude.of(encodedValues[n]).getClass());
        }

        assertEquals(
                GlobalAltitude.of((short) 0x7FFF).getClass(),
                GlobalAltitude.NotConfigured.class);
        assertNull(GlobalAltitude.of((short) 0x7FFF).getDecodedPosition());

        assertEquals(
                GlobalAltitude.of((short) 0x7FFE).getClass(),
                GlobalAltitude.GreaterThanOrEqualTo32766.class);
        assertNull(GlobalAltitude.of((short) 0x7FFE).getDecodedPosition());

        assertThrows(
                IllegalArgumentException.class,
                () -> GlobalAltitude.encode(-32769));

        assertThrows(
                IllegalArgumentException.class,
                () -> GlobalAltitude.encode(32766));
    }

    @Test
    public void testGlobalGet() {
        GenericLocationGlobalGet message = new GenericLocationGlobalGet(applicationKey);
        assertNull(message.mParameters);
    }

    @Test
    public void testGlobalStatus() {
        ArrayList<byte[]> encodedValues = new ArrayList<>();
        encodedValues.add(new byte[]{});

        encodedValues.add(new byte[]{
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00});

        encodedValues.add(new byte[]{
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F,
                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x80,
                (byte) 0x01, (byte) 0x00});

        ArrayList<Double> latitudes = new ArrayList<>();
        latitudes.add(null);
        latitudes.add(0.0);
        latitudes.add(90.0);

        ArrayList<Double> longitudes = new ArrayList<>();
        longitudes.add(null);
        longitudes.add(0.0);
        longitudes.add(-180.0);

        ArrayList<Integer> altitudes = new ArrayList<>();
        altitudes.add(null);
        altitudes.add(0);
        altitudes.add(1);

        for (int n = 0; n < encodedValues.size(); n++) {
            AccessMessage accessMessage = new AccessMessage();
            accessMessage.setParameters(encodedValues.get(n));
            GenericLocationGlobalStatus message = new GenericLocationGlobalStatus(accessMessage);
            assertEquals(latitudes.get(n), message.getLatitude().getDecodedPosition());
            assertEquals(longitudes.get(n), message.getLongitude().getDecodedPosition());
            assertEquals(altitudes.get(n), message.getAltitude().getDecodedPosition());
        }
    }

    @Test
    public void testGlobalSet() {
        ArrayList<Double> latitudes = new ArrayList<>();
        latitudes.add(0.0);
        latitudes.add(90.0);

        ArrayList<Double> longitudes = new ArrayList<>();
        longitudes.add(0.0);
        longitudes.add(-180.0);

        ArrayList<Integer> altitudes = new ArrayList<>();
        altitudes.add(0);
        altitudes.add(1);

        ArrayList<byte[]> encodedValues = new ArrayList<>();
        encodedValues.add(new byte[]{
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00});

        encodedValues.add(new byte[]{
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F,
                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x80,
                (byte) 0x01, (byte) 0x00});


        for (int n = 0; n < encodedValues.size(); n++) {
            GenericLocationGlobalSet message = new GenericLocationGlobalSet(
                    applicationKey,
                    GlobalLatitude.encode(latitudes.get(n)),
                    GlobalLongitude.encode(longitudes.get(n)),
                    GlobalAltitude.encode(altitudes.get(n)));
            assertArrayEquals(encodedValues.get(n), message.mParameters);
            GenericLocationGlobalSetUnacknowledged messageUnacknowledged =
                    new GenericLocationGlobalSetUnacknowledged(
                            applicationKey,
                            GlobalLatitude.encode(latitudes.get(n)),
                            GlobalLongitude.encode(longitudes.get(n)),
                            GlobalAltitude.encode(altitudes.get(n)));
            assertArrayEquals(encodedValues.get(n), messageUnacknowledged.mParameters);
        }
    }
}
