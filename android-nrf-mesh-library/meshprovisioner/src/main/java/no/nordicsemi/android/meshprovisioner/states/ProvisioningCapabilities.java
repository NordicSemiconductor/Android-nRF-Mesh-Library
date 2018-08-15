package no.nordicsemi.android.meshprovisioner.states;

import android.os.Parcel;
import android.os.Parcelable;

public final class ProvisioningCapabilities implements Parcelable {

    private byte [] rawCapabilities;
    private byte numberOfElements;
    private short algorithm;
    private byte publicKeyType;
    private byte staticOOBType;
    private byte outputOOBSize;
    private short outputOOBAction;
    private byte inputOOBSize;
    private short inputOOBAction;

    ProvisioningCapabilities(){

    }

    ProvisioningCapabilities(Parcel in) {
        rawCapabilities = in.createByteArray();
        numberOfElements = in.readByte();
        algorithm = (short) in.readInt();
        publicKeyType = in.readByte();
        staticOOBType = in.readByte();
        outputOOBSize = in.readByte();
        outputOOBAction = (short) in.readInt();
        inputOOBSize = in.readByte();
        inputOOBAction = (short) in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(rawCapabilities);
        dest.writeByte(numberOfElements);
        dest.writeInt((int) algorithm);
        dest.writeByte(publicKeyType);
        dest.writeByte(staticOOBType);
        dest.writeByte(outputOOBSize);
        dest.writeInt((int) outputOOBAction);
        dest.writeByte(inputOOBSize);
        dest.writeInt((int) inputOOBAction);
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

    public byte[] getRawCapabilities() {
        return rawCapabilities;
    }

    void setRawCapabilities(final byte[] rawCapabilities) {
        this.rawCapabilities = rawCapabilities;
    }

    public byte getNumberOfElements() {
        return numberOfElements;
    }

    void setNumberOfElements(final byte numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public short getSupportedAlgorithm() {
        return algorithm;
    }

    void setSupportedAlgorithm(final short algorithm) {
        this.algorithm = algorithm;
    }

    public byte getPublicKeyType() {
        return publicKeyType;
    }

    void setPublicKeyType(final byte publicKeyType) {
        this.publicKeyType = publicKeyType;
    }

    public byte getStaticOOBType() {
        return staticOOBType;
    }

    void setStaticOOBType(final byte staticOOBType) {
        this.staticOOBType = staticOOBType;
    }

    public byte getOutputOOBSize() {
        return outputOOBSize;
    }

    void setOutputOOBSize(final byte outputOOBSize) {
        this.outputOOBSize = outputOOBSize;
    }

    public short getOutputOOBAction() {
        return outputOOBAction;
    }

    void setOutputOOBAction(final short outputOOBAction) {
        this.outputOOBAction = outputOOBAction;
    }

    public byte getInputOOBSize() {
        return inputOOBSize;
    }

    void setInputOOBSize(final byte inputOOBSize) {
        this.inputOOBSize = inputOOBSize;
    }

    public short getInputOOBAction() {
        return inputOOBAction;
    }

    void setInputOOBAction(final short inputOOBAction) {
        this.inputOOBAction = inputOOBAction;
    }
}
