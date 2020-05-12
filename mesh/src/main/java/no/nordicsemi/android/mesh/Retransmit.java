
package no.nordicsemi.android.mesh;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Retransmit {

    @SerializedName("count")
    @Expose
    private int count;
    @SerializedName("interval")
    @Expose
    private int interval;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

}
