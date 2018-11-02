
package no.nordicsemi.android.meshprovisioner;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Publish {

    @SerializedName("address")
    @Expose
    private int address;
    @SerializedName("credentials")
    @Expose
    private int credentials;
    @SerializedName("index")
    @Expose
    private int index;
    @SerializedName("period")
    @Expose
    private int period;
    @SerializedName("retransmit")
    @Expose
    private Retransmit retransmit;
    @SerializedName("ttl")
    @Expose
    private int ttl;

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getCredentials() {
        return credentials;
    }

    public void setCredentials(int credentials) {
        this.credentials = credentials;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public Retransmit getRetransmit() {
        return retransmit;
    }

    public void setRetransmit(Retransmit retransmit) {
        this.retransmit = retransmit;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

}
