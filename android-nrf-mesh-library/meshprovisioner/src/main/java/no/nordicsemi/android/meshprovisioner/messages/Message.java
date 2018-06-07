package no.nordicsemi.android.meshprovisioner.messages;

import java.util.HashMap;
import java.util.Map;

public abstract class Message {

    /**
     * ctl, if ctl = 0 access message and ctl = 1 control message
     **/
    protected int ctl;
    protected HashMap<Integer, byte[]> lowerTransportAccessPdu;
    protected HashMap<Integer, byte[]> lowerTransportControlPdu;
    protected HashMap<Integer, byte[]> networkPdu;
    /**
     * pdu type
     **/
    private int pduType;
    /**
     * ttl, time to live
     **/
    private int ttl = 100;
    /**
     * src, source address
     **/
    private byte[] src;
    /**
     * dst, destination address
     **/
    private byte[] dst;
    /**
     * sequence number, which is unique 24-bit value for each message
     **/
    private byte[] mSequenceNumber;
    /**
     * key, used for encryption in transport layer which could be application key or device key
     **/
    private byte[] key;
    /**
     * encryption key, derived from k2 using network key
     **/
    private byte[] encryptionKey;
    /**
     * privacy key, derived from k2 using network key
     **/
    private byte[] privacyKey;
    /**
     * akf if akf = 0 device key to be used for encryption in the transport layer if not use application key
     **/
    private int akf;
    /**
     * aid, if akf = 0 aid is also 0 if not aid is the identifier for the key used for encrytpion
     **/
    private int aid;
    /**
     * aszmic, if aszmic = 0 the transmic is 32-bits, if aszmic = 1 transmic 64-bits this is usually for a segmented message
     **/
    private int aszmic;
    /**
     * opcode, operation code for the message
     **/
    private int opCode;
    /**
     * parameters, opcode parameters
     **/
    private byte[] parameters;
    private int companyIdentifier;
    private byte[] ivIndex;
    private boolean segmented;

    public abstract Map<Integer, byte[]> getNetworkPdu();

    public abstract void setNetworkPdu(final HashMap<Integer, byte[]> pdu);

    public int getPduType() {
        return pduType;
    }

    public void setPduType(final int pduType) {
        this.pduType = pduType;
    }

    public int getCtl() {
        return ctl;
    }

    public void setCtl(final int ctl) {
        this.ctl = ctl;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(final int ttl) {
        this.ttl = ttl;
    }

    public byte[] getSrc() {
        return src;
    }

    public void setSrc(final byte[] src) {
        this.src = src;
    }

    public byte[] getDst() {
        return dst;
    }

    public void setDst(final byte[] dst) {
        this.dst = dst;
    }

    public byte[] getSequenceNumber() {
        return mSequenceNumber;
    }

    public void setSequenceNumber(final byte[] sequenceNumber) {
        this.mSequenceNumber = sequenceNumber;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(final byte[] key) {
        this.key = key;
    }

    public byte[] getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(final byte[] encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public byte[] getPrivacyKey() {
        return privacyKey;
    }

    public void setPrivacyKey(final byte[] privacyKey) {
        this.privacyKey = privacyKey;
    }

    public int getAkf() {
        return akf;
    }

    public void setAkf(final int akf) {
        this.akf = akf;
    }

    public int getAid() {
        return aid;
    }

    public void setAid(final int aid) {
        this.aid = aid;
    }

    public int getAszmic() {
        return aszmic;
    }

    public void setAszmic(final int aszmic) {
        this.aszmic = aszmic;
    }

    public int getOpCode() {
        return opCode;
    }

    public void setOpCode(final int opCode) {
        this.opCode = opCode;
    }

    public byte[] getParameters() {
        return parameters;
    }

    public void setParameters(final byte[] parameters) {
        this.parameters = parameters;
    }

    public int getCompanyIdentifier() {
        return companyIdentifier;
    }

    public void setCompanyIdentifier(final int companyIdentifier) {
        this.companyIdentifier = companyIdentifier;
    }

    public byte[] getIvIndex() {
        return ivIndex;
    }

    public void setIvIndex(final byte[] ivIndex) {
        this.ivIndex = ivIndex;
    }

    public boolean isSegmented() {
        return segmented;
    }

    public void setSegmented(final boolean segmented) {
        this.segmented = segmented;
    }

    public HashMap<Integer, byte[]> getLowerTransportAccessPdu() {
        return lowerTransportAccessPdu;
    }

    public void setLowerTransportAccessPdu(final HashMap<Integer, byte[]> lowerTansportAccessPdu) {
        this.lowerTransportAccessPdu = lowerTansportAccessPdu;
    }

    public HashMap<Integer, byte[]> getLowerTransportControlPdu() {
        return lowerTransportControlPdu;
    }

    public void setLowerTransportControlPdu(final HashMap<Integer, byte[]> segmentedAccessMessages) {
        this.lowerTransportControlPdu = segmentedAccessMessages;
    }
}
