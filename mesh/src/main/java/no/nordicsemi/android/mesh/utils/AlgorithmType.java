package no.nordicsemi.android.mesh.utils;

import no.nordicsemi.android.mesh.logger.MeshLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Supported algorithm type
 */
@SuppressWarnings("unused")
public enum AlgorithmType {

    FIPS_P_256_ELLIPTIC_CURVE("FIPS P-256 Elliptic Curve", (short) 0x0001, (byte) 0x00);

    private static final String TAG = AlgorithmType.class.getSimpleName();
    private final short bitMask;
    private final String name;
    private final byte value;

    /**
     * Algorithm Type
     *
     * @param name    Name of the algorithm.
     * @param bitMask Bitmask of value received from provisioning capabilities.
     * @param value   Algorithm value.
     */
    AlgorithmType(final String name, final short bitMask, final byte value) {
        this.name = name;
        this.bitMask = bitMask;
        this.value = value;
    }

    /**
     * Returns the algorithm type value.
     */
    public short getBitMask() {
        return bitMask;
    }

    /**
     * Returns the algorithm name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the algorithm value.
     */
    public byte getValue() {
        return value;
    }

    /**
     * Parses the output oob action value
     *
     * @param algorithmTypeValue algorithm type
     * @return selected output action type
     */
    public static ArrayList<AlgorithmType> getAlgorithmTypeFromBitMask(final short algorithmTypeValue) {
        final AlgorithmType[] algorithmTypes = AlgorithmType.values();
        final ArrayList<AlgorithmType> supportedAlgorithms = new ArrayList<>();
        for (AlgorithmType algorithmType : algorithmTypes) {
            if ((algorithmTypeValue & algorithmType.bitMask) == algorithmType.bitMask) {
                supportedAlgorithms.add(algorithmType);
                MeshLogger.verbose(TAG, "Supported output oob action type: " + algorithmType.name);
            }
        }
        return supportedAlgorithms;
    }

    /**
     * Returns the supported algorithm value
     *
     * @param supportedAlgorithmByNode List of supported algorithms by the provisioner
     * @return supported algorithm value
     */
    @SuppressWarnings("RedundantCollectionOperation")
    public static byte getSupportedAlgorithmValue(final List<AlgorithmType> supportedAlgorithmByNode) {
        final List<AlgorithmType> provisionerSupportedAlgorithms = Arrays.asList(AlgorithmType.values());
        Collections.reverse(supportedAlgorithmByNode);
        for (AlgorithmType algorithmType : supportedAlgorithmByNode) {
            for (AlgorithmType provisionerSupportedAlgorithm : provisionerSupportedAlgorithms) {
                if (provisionerSupportedAlgorithm == algorithmType)
                    return provisionerSupportedAlgorithm.value;
            }
        }
        throw new IllegalArgumentException("Unsupported algorithm!");
    }
}
