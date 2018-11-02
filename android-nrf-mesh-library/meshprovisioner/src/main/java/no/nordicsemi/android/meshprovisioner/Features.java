
package no.nordicsemi.android.meshprovisioner;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Features {

    @SerializedName("friend")
    @Expose
    private int friend;
    @SerializedName("lowPower")
    @Expose
    private int lowPower;
    @SerializedName("proxy")
    @Expose
    private int proxy;
    @SerializedName("relay")
    @Expose
    private int relay;

    public int getFriend() {
        return friend;
    }

    public void setFriend(int friend) {
        this.friend = friend;
    }

    public int getLowPower() {
        return lowPower;
    }

    public void setLowPower(int lowPower) {
        this.lowPower = lowPower;
    }

    public int getProxy() {
        return proxy;
    }

    public void setProxy(int proxy) {
        this.proxy = proxy;
    }

    public int getRelay() {
        return relay;
    }

    public void setRelay(int relay) {
        this.relay = relay;
    }

}
