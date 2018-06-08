package no.nordicsemi.android.meshprovisioner;

import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;


public abstract class BaseMeshNode implements Parcelable {

    protected static final String TAG = BaseMeshNode.class.getSimpleName();

    protected final byte[] mConfigurationSrc = {0x7F, (byte) 0xFF};
    protected byte[] ivIndex;
    protected boolean isProvisioned;
    protected boolean isConfigured;
    protected String nodeName = "My Node";
    protected byte[] provisionerPublicKeyXY;
    protected byte[] provisioneePublicKeyXY;
    protected byte[] sharedECDHSecret;
    protected byte[] provisionerRandom;
    protected byte[] provisioneeConfirmation;
    protected byte[] authenticationValue;
    protected byte[] provisioneeRandom;
    protected byte[] networkKey;
    protected byte[] identityKey;
    protected byte[] keyIndex;
    protected byte[] mFlags;
    protected byte[] unicastAddress;
    protected byte[] deviceKey;
    protected int ttl = 5;
    protected int mReceivedSequenceNumber;
    protected String bluetoothAddress;
    protected String nodeIdentifier;
    protected int companyIdentifier;
    protected int productIdentifier;
    protected int versionIdentifier;
    protected int crpl;
    protected int features;
    protected boolean relayFeatureSupported;
    protected boolean proxyFeatureSupported;
    protected boolean friendFeatureSupported;
    protected boolean lowPowerFeatureSupported;
    protected final Map<Integer, Element> mElements = new LinkedHashMap<>();
    protected List<Integer> mAddedAppKeyIndexes = new ArrayList<>();
    protected Map<Integer, String> mAddedAppKeys = new HashMap<>(); //Map containing the key as the app key index and the app key as the value
    protected byte[] generatedNetworkId;
    private String bluetoothDeviceAddress;
    protected long mTimeStampInMillis;

    protected BaseMeshNode() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isProvisioned() {
        return isProvisioned;
    }

    public final void setIsProvisioned(final boolean isProvisioned) {
        identityKey = SecureUtils.calculateIdentityKey(networkKey);
        this.isProvisioned = isProvisioned;
    }

    public final boolean isConfigured() {
        return isConfigured;
    }

    public final void setConfigured(final boolean configured) {
        isConfigured = configured;
    }

    public final String getNodeName() {
        return nodeName;
    }

    protected final void setNodeName(final String nodeName) {
        if (!TextUtils.isEmpty(nodeName))
            this.nodeName = nodeName;
    }

    public final byte[] getUnicastAddress() {
        return unicastAddress;
    }

    public final void setUnicastAddress(final byte[] unicastAddress) {
        this.unicastAddress = unicastAddress;
    }

    public byte[] getDeviceKey() {
        return deviceKey;
    }

    public int getTtl() {
        return ttl;
    }

    public final byte[] getIdentityKey() {
        return identityKey;
    }

    public final byte[] getKeyIndex() {
        return keyIndex;
    }

    public final void setKeyIndex(final byte[] keyIndex) {
        this.keyIndex = keyIndex;
    }

    public final byte[] getFlags() {
        return mFlags;
    }

    public final void setFlags(final byte[] flags) {
        this.mFlags = flags;
    }

    public final byte[] getIvIndex() {
        return ivIndex;
    }

    public final void setIvIndex(final byte[] ivIndex) {
        this.ivIndex = ivIndex;
    }

    public void setBluetoothDeviceAddress(final String bluetoothDeviceAddress) {
        this.bluetoothDeviceAddress = bluetoothDeviceAddress;
    }

    public void setTtl(final int ttl) {
        this.ttl = ttl;
    }

    public long getTimeStamp() {
        return mTimeStampInMillis;
    }
}
