
package no.nordicsemi.android.meshprovisioner;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import no.nordicsemi.android.meshprovisioner.transport.Element;

public final class Node {

    @SerializedName("UUID")
    @Expose
    private String uUID;
    @SerializedName("appKeys")
    @Expose
    private List<Integer> appKeys = null;
    @SerializedName("cid")
    @Expose
    private String cid;
    @SerializedName("configComplete")
    @Expose
    private boolean configComplete;
    @SerializedName("crpl")
    @Expose
    private int crpl;
    @SerializedName("deviceKey")
    @Expose
    private String deviceKey;
    @SerializedName("elements")
    @Expose
    private List<Element> elements = null;
    @SerializedName("features")
    @Expose
    private Features features;
    @SerializedName("netKeys")
    @Expose
    private List<Integer> netKeys = null;
    @SerializedName("pid")
    @Expose
    private String pid;
    @SerializedName("security")
    @Expose
    private String security;
    @SerializedName("unicastAddress")
    @Expose
    private int unicastAddress;
    @SerializedName("vid")
    @Expose
    private String vid;

    public String getUUID() {
        return uUID;
    }

    public void setUUID(String uUID) {
        this.uUID = uUID;
    }

    public List<Integer> getAppKeys() {
        return appKeys;
    }

    public void setAppKeys(List<Integer> appKeys) {
        this.appKeys = appKeys;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public boolean isConfigComplete() {
        return configComplete;
    }

    public void setConfigComplete(boolean configComplete) {
        this.configComplete = configComplete;
    }

    public int getCrpl() {
        return crpl;
    }

    public void setCrpl(int crpl) {
        this.crpl = crpl;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public Features getFeatures() {
        return features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public List<Integer> getNetKeys() {
        return netKeys;
    }

    public void setNetKeys(List<Integer> netKeys) {
        this.netKeys = netKeys;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public int getUnicastAddress() {
        return unicastAddress;
    }

    public void setUnicastAddress(int unicastAddress) {
        this.unicastAddress = unicastAddress;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

}
