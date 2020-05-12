package no.nordicsemi.android.mesh.provisionerstates;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nordicsemi.android.mesh.utils.AlgorithmType;
import no.nordicsemi.android.mesh.utils.AuthenticationOOBMethods;
import no.nordicsemi.android.mesh.utils.InputOOBAction;
import no.nordicsemi.android.mesh.utils.OutputOOBAction;

/**
 * Contains the provisioning capabilities of a device
 */
@SuppressWarnings("unused")
public final class ProvisioningCapabilities implements Parcelable {
    private static final int PUBLIC_KEY_INFORMATION_AVAILABLE = 0x01;
    private static final int STATIC_OOB_INFO_AVAILABLE = 0x01;


    private static final String TAG = ProvisioningCapabilities.class.getSimpleName();
    private byte numberOfElements;
    private short rawAlgorithm;
    private List<AlgorithmType> supportedAlgorithmTypes;
    private byte rawPublicKeyType;
    private boolean publicKeyInformationAvailable;
    private byte rawStaticOOBType;
    private boolean staticOOBInformationAvailable;
    private byte outputOOBSize;
    private short rawOutputOOBAction;
    private List<OutputOOBAction> supportedOutputOOBActions;
    private byte inputOOBSize;
    private short rawInputOOBAction;
    private List<InputOOBAction> supportedInputOOBActions;
    private AuthenticationOOBMethods supportedOOBMethods;
    private final List<AuthenticationOOBMethods> availableOOBTypes = new ArrayList<>();

    /**
     * Constructs the provisioning capabilities received from a mesh node
     *
     * @param capabilities capabilities pdu
     */
    ProvisioningCapabilities(@NonNull final byte[] capabilities) {
        if (capabilities[2] == 0) {
            throw new IllegalArgumentException("Number of elements cannot be zero");
        }

        final byte numberOfElements = (capabilities[2]);
        this.numberOfElements = numberOfElements;
        Log.v(TAG, "Number of elements: " + numberOfElements);

        final short algorithm = (short) (((capabilities[3] & 0xff) << 8) | (capabilities[4] & 0xff));
        this.rawAlgorithm = algorithm;
        this.supportedAlgorithmTypes = AlgorithmType.getAlgorithmTypeFromBitMask(algorithm);

        this.rawPublicKeyType = capabilities[5];
        this.publicKeyInformationAvailable = rawPublicKeyType == PUBLIC_KEY_INFORMATION_AVAILABLE;
        Log.v(TAG, "Public key information available: " + publicKeyInformationAvailable);

        this.rawStaticOOBType = capabilities[6];
        this.staticOOBInformationAvailable = rawStaticOOBType == STATIC_OOB_INFO_AVAILABLE;
        Log.v(TAG, "Static OOB information available: : " + staticOOBInformationAvailable);

        final byte outputOOBSize = capabilities[7];
        this.outputOOBSize = outputOOBSize;
        Log.v(TAG, "Output OOB size: " + outputOOBSize);

        final short outputOOBAction = (short) (((capabilities[8] & 0xff) << 8) | (capabilities[9] & 0xff));
        this.rawOutputOOBAction = outputOOBAction;
        this.supportedOutputOOBActions = outputOOBSize == 0 ? new ArrayList<>() : OutputOOBAction.parseOutputActionsFromBitMask(outputOOBAction);

        final byte inputOOBSize = capabilities[10];
        this.inputOOBSize = inputOOBSize;
        Log.v(TAG, "Input OOB size: " + inputOOBSize);

        final short inputOOBAction = (short) (((capabilities[11] & 0xff) << 8) | (capabilities[12] & 0xff));
        this.rawInputOOBAction = inputOOBAction;
        this.supportedInputOOBActions = inputOOBSize == 0 ? new ArrayList<>() : InputOOBAction.parseInputActionsFromBitMask(inputOOBAction);
        generateAvailableOOBTypes();
    }

    private void generateAvailableOOBTypes() {
        availableOOBTypes.clear();
        availableOOBTypes.add(AuthenticationOOBMethods.NO_OOB_AUTHENTICATION);
        if (isStaticOOBInformationAvailable()) {
            availableOOBTypes.add(AuthenticationOOBMethods.STATIC_OOB_AUTHENTICATION);
        }

        if (!supportedOutputOOBActions.isEmpty()) {
            availableOOBTypes.add(AuthenticationOOBMethods.OUTPUT_OOB_AUTHENTICATION);
        }
        if (!supportedInputOOBActions.isEmpty()) {
            availableOOBTypes.add(AuthenticationOOBMethods.INPUT_OOB_AUTHENTICATION);
        }
    }

    private ProvisioningCapabilities(Parcel in) {
        numberOfElements = in.readByte();
        rawAlgorithm = (short) in.readInt();
        this.supportedAlgorithmTypes = AlgorithmType.getAlgorithmTypeFromBitMask(rawAlgorithm);
        rawPublicKeyType = in.readByte();
        publicKeyInformationAvailable = in.readByte() != 0;
        rawStaticOOBType = in.readByte();
        staticOOBInformationAvailable = in.readByte() != 0;
        outputOOBSize = in.readByte();
        rawOutputOOBAction = (short) in.readInt();
        this.supportedOutputOOBActions = new ArrayList<>(OutputOOBAction.parseOutputActionsFromBitMask(rawOutputOOBAction));
        inputOOBSize = in.readByte();
        rawInputOOBAction = (short) in.readInt();
        this.supportedInputOOBActions = new ArrayList<>(InputOOBAction.parseInputActionsFromBitMask(rawInputOOBAction));
        generateAvailableOOBTypes();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(numberOfElements);
        dest.writeInt((int) rawAlgorithm);
        dest.writeByte(rawPublicKeyType);
        dest.writeByte((byte) (publicKeyInformationAvailable ? 1 : 0));
        dest.writeByte(rawStaticOOBType);
        dest.writeByte((byte) (staticOOBInformationAvailable ? 1 : 0));
        dest.writeByte(outputOOBSize);
        dest.writeInt((int) rawOutputOOBAction);
        dest.writeByte(inputOOBSize);
        dest.writeInt((int) rawInputOOBAction);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProvisioningCapabilities> CREATOR = new Creator<ProvisioningCapabilities>() {
        @Override
        public ProvisioningCapabilities createFromParcel(Parcel in) {
            return new ProvisioningCapabilities(in);
        }

        @Override
        public ProvisioningCapabilities[] newArray(int size) {
            return new ProvisioningCapabilities[size];
        }
    };

    /**
     * Returns the number of elements in the mesh node
     */
    public byte getNumberOfElements() {
        return numberOfElements;
    }

    /**
     * Sets the number of elements in the node
     */
    void setNumberOfElements(final byte numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    /**
     * Returns the raw supported algorithm value received by the node
     */
    public short getRawAlgorithm() {
        return rawAlgorithm;
    }

    void setRawAlgorithm(final short algorithm) {
        this.rawAlgorithm = algorithm;
    }

    /**
     * Returns a list of algorithm types supported by the node
     */
    public List<AlgorithmType> getSupportedAlgorithmTypes() {
        return Collections.unmodifiableList(supportedAlgorithmTypes);
    }

    /**
     * Returns the raw public key type received by the node
     */
    public byte getRawPublicKeyType() {
        return rawPublicKeyType;
    }

    void setRawPublicKeyType(final byte rawPublicKeyType) {
        this.rawPublicKeyType = rawPublicKeyType;
    }

    /**
     * Returns true if public key information is available
     */
    public boolean isPublicKeyInformationAvailable() {
        return publicKeyInformationAvailable;
    }

    /**
     * Returns the raw static OOB type received by the node
     */
    public byte getRawStaticOOBType() {
        return rawStaticOOBType;
    }

    void setRawStaticOOBType(final byte rawStaticOOBType) {
        this.rawStaticOOBType = rawStaticOOBType;
    }

    /**
     * Returns true if Static OOB information is available
     */
    public boolean isStaticOOBInformationAvailable() {
        return staticOOBInformationAvailable;
    }

    /**
     * Returns the output oob size received by the node. This is the length of
     */
    public byte getOutputOOBSize() {
        return outputOOBSize;
    }

    void setOutputOOBSize(final byte outputOOBSize) {
        this.outputOOBSize = outputOOBSize;
    }

    /**
     * Returns the raw output oob action value received by the node
     */
    public short getRawOutputOOBAction() {
        return rawOutputOOBAction;
    }

    void setRawOutputOOBAction(final short rawOutputOOBAction) {
        this.rawOutputOOBAction = rawOutputOOBAction;
    }

    /**
     * Returns the list of supported {@link OutputOOBAction} actions or an empty list if no oob is supported
     */
    public List<OutputOOBAction> getSupportedOutputOOBActions() {
        return Collections.unmodifiableList(supportedOutputOOBActions);
    }

    public byte getInputOOBSize() {
        return inputOOBSize;
    }

    /**
     * Returns the raw input oob size value received by the node
     */
    void setInputOOBSize(final byte inputOOBSize) {
        this.inputOOBSize = inputOOBSize;
    }

    /**
     * Returns the raw input oob action value received by the node
     */
    public short getRawInputOOBAction() {
        return rawInputOOBAction;
    }

    void setRawInputOOBAction(final short rawInputOOBAction) {
        this.rawInputOOBAction = rawInputOOBAction;
    }

    /**
     * Returns the list of supported {@link InputOOBAction} actions or an empty list if no oob is supported
     */
    public List<InputOOBAction> getSupportedInputOOBActions() {
        return Collections.unmodifiableList(supportedInputOOBActions);
    }

    /**
     * Returns a list of available OOB methods that can be used during provisioning
     */
    public List<AuthenticationOOBMethods> getAvailableOOBTypes() {
        return Collections.unmodifiableList(availableOOBTypes);
    }
}
