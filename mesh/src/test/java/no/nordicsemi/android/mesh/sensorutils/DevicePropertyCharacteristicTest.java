package no.nordicsemi.android.mesh.sensorutils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

public class DevicePropertyCharacteristicTest {

    @Test
    public void testPercentage8() {
        final ArrayList<Float> expectedSamples = new ArrayList<>();
        expectedSamples.add(Float.valueOf("0.0"));
        expectedSamples.add(Float.valueOf("0.5"));
        expectedSamples.add(Float.valueOf("50.0"));
        final ArrayList<byte[]> samples = new ArrayList<>();
        samples.add(new byte[]{0});
        samples.add(new byte[]{1});
        samples.add(new byte[]{100});
        int counter = 0;
        for (byte[] sample : samples) {
            final DevicePropertyCharacteristic<?> temperature = DeviceProperty.
                    getCharacteristic(DeviceProperty.MOTION_SENSED, sample, 0, 1);
            Assert.assertEquals(expectedSamples.get(counter), temperature.getValue());
            counter++;
        }
    }

    @Test
    public void testTemperature8() {
        final ArrayList<Float> expectedSamples = new ArrayList<>();
        expectedSamples.add(Float.valueOf("-64.0"));
        expectedSamples.add(Float.valueOf("0.0"));
        expectedSamples.add(Float.valueOf("0.5"));
        expectedSamples.add(Float.valueOf("63.0"));
        final ArrayList<byte[]> samples = new ArrayList<>();
        samples.add(new byte[]{(byte) 0x80});
        samples.add(new byte[]{(byte) 0x00});
        samples.add(new byte[]{(byte) 0x01});
        samples.add(new byte[]{126});
        int counter = 0;
        for (byte[] sample : samples) {
            final DevicePropertyCharacteristic<?> temperature = DeviceProperty.
                    getCharacteristic(DeviceProperty.DESIRED_AMBIENT_TEMPERATURE, sample, 0, 1);
            Assert.assertEquals(expectedSamples.get(counter), temperature.getValue());
            counter++;
        }
    }

    @Test
    public void testPerceivedLightness() {
        final ArrayList<Integer> expectedSamples = new ArrayList<>();
        expectedSamples.add(0x0000);
        expectedSamples.add(0x0001);
        expectedSamples.add(0xABCD);
        expectedSamples.add(0xFFFF);
        final ArrayList<byte[]> samples = new ArrayList<>();
        samples.add(new byte[]{0x00, 0x00});
        samples.add(new byte[]{0x01, 0x00});
        samples.add(new byte[]{(byte) 0xCD, (byte) 0xAB});
        samples.add(new byte[]{(byte) 0xFF, (byte) 0xFF});
        int counter = 0;
        for (byte[] sample : samples) {
            final DevicePropertyCharacteristic<?> perceivedLightness = DeviceProperty.
                    getCharacteristic(DeviceProperty.LIGHT_CONTROL_LIGHTNESS_ON, sample, 0, 2);
            Assert.assertEquals(expectedSamples.get(counter), perceivedLightness.getValue());
            counter++;
        }
    }

    @Test
    public void testCount16() {
        final ArrayList<Integer> expectedSamples = new ArrayList<>();
        expectedSamples.add(0x0000);
        expectedSamples.add(0x0001);
        expectedSamples.add(0xABCD);
        expectedSamples.add(0xFFFE);
        final ArrayList<byte[]> samples = new ArrayList<>();
        samples.add(new byte[]{0x00, 0x00});
        samples.add(new byte[]{0x01, 0x00});
        samples.add(new byte[]{(byte) 0xCD, (byte) 0xAB});
        samples.add(new byte[]{(byte) 0xFE, (byte) 0xFF});
        int counter = 0;
        for (byte[] sample : samples) {
            final DevicePropertyCharacteristic<?> count16 = DeviceProperty.
                    getCharacteristic(DeviceProperty.PEOPLE_COUNT, sample, 0, 2);
            Assert.assertEquals(expectedSamples.get(counter), count16.getValue());
            counter++;
        }
    }

    @Test
    public void testHumidity() {
        final ArrayList<Float> expectedSamples = new ArrayList<>();
        expectedSamples.add(0.0f);
        expectedSamples.add(0.01f);
        expectedSamples.add(12.34f);
        expectedSamples.add(100.0f);
        final ArrayList<byte[]> samples = new ArrayList<>();
        samples.add(new byte[]{0x00, 0x00});
        samples.add(new byte[]{0x01, 0x00});
        samples.add(new byte[]{(byte) 0xD2, (byte) 0x04});
        samples.add(new byte[]{(byte) 0x10, (byte) 0x27});
        int counter = 0;
        for (byte[] sample : samples) {
            final DevicePropertyCharacteristic<?> count16 = DeviceProperty.
                    getCharacteristic(DeviceProperty.PRESENT_INDOOR_RELATIVE_HUMIDITY, sample, 0, 2);
            Assert.assertEquals(expectedSamples.get(counter), count16.getValue());
            counter++;
        }
    }

    @Test
    public void testTemperature() {
        final ArrayList<Float> expectedSamples = new ArrayList<>();
        expectedSamples.add(Float.valueOf("-273.15"));
        expectedSamples.add(Float.valueOf("0.01"));
        expectedSamples.add(Float.valueOf("12.34"));
        expectedSamples.add(Float.valueOf("327.67"));
        final ArrayList<byte[]> samples = new ArrayList<>();
        samples.add(new byte[]{(byte) 0x4D, (byte) 0x95});
        samples.add(new byte[]{(byte) 0x01, 0x00});
        samples.add(new byte[]{(byte) 0xD2, 0x04});
        samples.add(new byte[]{(byte) 0xFF, 0x7F});
        int counter = 0;
        for (byte[] sample : samples) {
            final DevicePropertyCharacteristic<?> temperature = DeviceProperty.
                    getCharacteristic(DeviceProperty.PRESENT_DEVICE_OPERATING_TEMPERATURE, sample, 0, 2);
            Assert.assertEquals(expectedSamples.get(counter), temperature.getValue());
            counter++;
        }
    }

    @Test
    public void testCoefficient() {
        final ArrayList<Float> expectedSamples = new ArrayList<>();
        expectedSamples.add(Float.valueOf("12.34"));
        expectedSamples.add(Float.valueOf("-0.0001"));
        expectedSamples.add(Float.valueOf("0.00"));
        expectedSamples.add(Float.valueOf("-0.0"));
        expectedSamples.add(Float.POSITIVE_INFINITY);
        expectedSamples.add(Float.NEGATIVE_INFINITY);
        expectedSamples.add(Float.NaN);

        final ArrayList<byte[]> samples = new ArrayList<>();
        samples.add(new byte[]{(byte) 0xA4, 0x70, 0x45, 0x41});
        samples.add(new byte[]{0x17, (byte) 0xB7, (byte) 0xD1, (byte) 0xB8});
        samples.add(new byte[]{0x00, 0x00, 0x00, 0x00});
        samples.add(new byte[]{0x00, 0x00, 0x00, (byte) 0x80});
        samples.add(new byte[]{0x00, 0x00, (byte) 0x80, (byte) 0x7F});
        samples.add(new byte[]{0x00, 0x00, (byte) 0x80, (byte) 0xFF});
        samples.add(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F});
        int counter = 0;
        for (byte[] sample : samples) {
            final DevicePropertyCharacteristic<?> coefficient = DeviceProperty.
                    getCharacteristic(DeviceProperty.SENSOR_GAIN, sample, 0, 4);
            Assert.assertEquals(expectedSamples.get(counter), coefficient.getValue());
            counter++;
        }
    }

    @Test
    public void testDateUTC() {
        final long daysInSeconds = 86400000L;
        final ArrayList<Date> expectedSamples = new ArrayList<>();
        expectedSamples.add(new Date(0x123456 * daysInSeconds));
        expectedSamples.add(new Date(0x00000F * daysInSeconds));
        expectedSamples.add(new Date(0x123456 * daysInSeconds));

        final ArrayList<byte[]> samples = new ArrayList<>();
        samples.add(new byte[]{0x56, 0x34, 0x12});
        samples.add(new byte[]{0x0F, 0x00, 0x00});
        samples.add(new byte[]{0x56, 0x34, 0x12});
        int counter = 0;
        for (byte[] sample : samples) {
            final DevicePropertyCharacteristic<?> dateUtc = DeviceProperty.
                    getCharacteristic(DeviceProperty.DEVICE_DATE_OF_MANUFACTURE, sample, 0, 3);
            Assert.assertEquals(expectedSamples.get(counter), dateUtc.getValue());
            counter++;
        }
    }
}