package no.nordicsemi.android.mesh.sensorutils;

import java.text.DateFormat;
import java.util.Date;

import androidx.annotation.NonNull;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.convertIntTo24Bits;

/**
 * Date as days elapsed since the Epoch (Jan 1, 1970) in the Coordinated Universal Time (UTC) time zone.
 */
public class DateUtc extends DevicePropertyCharacteristic<Date> {

    public DateUtc(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        final int tempDate = ((((data[offset + 2] & 0xFF) << 16) | ((data[offset + 1] & 0xFF) << 8) | data[offset] & 0xFF));
        if (tempDate < 1 || tempDate > 16777214) {
            value = null;
        } else {
            value = new Date((long) (tempDate * 86400000L));
        }
    }

    /**
     * DateUtc characteristic.
     *
     * @param date Date
     */
    public DateUtc(final Date date) {
        value = date;
    }

    @NonNull
    @Override
    public String toString() {
        return value == null ? null : DateFormat.getDateInstance().format(value);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public byte[] getBytes() {
        return convertIntTo24Bits((int) (value.getTime() / 86400000L));
    }
}
