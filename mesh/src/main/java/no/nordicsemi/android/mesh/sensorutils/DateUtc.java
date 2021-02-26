package no.nordicsemi.android.mesh.sensorutils;

import java.text.DateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * Date as days elapsed since the Epoch (Jan 1, 1970) in the Coordinated Universal Time (UTC) time zone.
 */
public class DateUtc extends DevicePropertyCharacteristic<Date> {

    public DateUtc(@NonNull final byte[] data, final int offset) {
        super(data, offset);
        value = new Date((long) parse(data, offset, getLength(), 0, 16777214, 0x000000) * 86400000L);
    }

    public DateUtc(final Date date) {
        value = date;
    }

    @NonNull
    @Override
    public String toString() {
        return DateFormat.getDateInstance().format(value);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public byte[] getBytes() {
        return MeshParserUtils.convertIntTo24Bits((int) (value.getTime() / 86400000L));
    }
}
