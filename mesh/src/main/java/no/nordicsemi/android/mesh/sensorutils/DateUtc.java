package no.nordicsemi.android.mesh.sensorutils;

import java.text.DateFormat;
import java.util.Date;

import androidx.annotation.NonNull;

/**
 * Date as days elapsed since the Epoch (Jan 1, 1970) in the Coordinated Universal Time (UTC) time zone.
 */
public class DateUtc extends DevicePropertyCharacteristic<Integer> {

    public DateUtc(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        value = (int) parse(data, offset, getLength(), 0, 16777214, 0x000000);
    }

    @NonNull
    @Override
    public String toString() {
        return DateFormat.getDateInstance().format(new Date(value));
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
