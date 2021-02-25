package no.nordicsemi.android.mesh.sensorutils;

import java.text.DateFormat;
import java.util.Date;

import androidx.annotation.NonNull;

/**
 * Date as days elapsed since the Epoch (Jan 1, 1970) in the Coordinated Universal Time (UTC) time zone.
 */
public class DateUtc extends DevicePropertyCharacteristic<Integer> {
    private final int length;

    public DateUtc(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
        this.length = length;
        if (length == 3) {
            value = (int) parse(data, offset, length, 0,16777214, 0x000000);
        } else {
            throw new IllegalArgumentException("Invalid length");
        }
    }

    @NonNull
    @Override
    public String toString() {
        return DateFormat.getDateInstance().format(new Date(value));
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
