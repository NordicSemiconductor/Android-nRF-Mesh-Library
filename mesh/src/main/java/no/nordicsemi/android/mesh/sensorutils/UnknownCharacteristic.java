package no.nordicsemi.android.mesh.sensorutils;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

public class UnknownCharacteristic extends DevicePropertyCharacteristic<byte[]> {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public UnknownCharacteristic(@NonNull final byte[] data, final int offset, final int length) {
        super(data, offset, length);
        value = Arrays.copyOfRange(data, offset, offset + length);
    }

    /**
     * UnknownCharacteristic for unsupported characteristics
     *
     * @param data byte array
     */
    public UnknownCharacteristic(@NonNull final byte[] data) {
        value = data;
    }

    @NonNull
    @Override
    public String toString() {
        return value.length == 0 ? "Empty" : MeshParserUtils.bytesToHex(value, true);
    }

    @Override
    public int getLength() {
        return value.length;
    }

    @Override
    public byte[] getBytes() {
        return value;
    }

}
