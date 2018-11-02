
package no.nordicsemi.android.meshprovisioner;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Model {

    @SerializedName("modelId")
    @Expose
    private String modelId;
    @SerializedName("bind")
    @Expose
    private List<Integer> bind = null;
    @SerializedName("publish")
    @Expose
    private Publish publish;
    @SerializedName("subscribe")
    @Expose
    private List<Integer> subscribe = null;

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public List<Integer> getBind() {
        return bind;
    }

    public void setBind(List<Integer> bind) {
        this.bind = bind;
    }

    public Publish getPublish() {
        return publish;
    }

    public void setPublish(Publish publish) {
        this.publish = publish;
    }

    public List<Integer> getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(List<Integer> subscribe) {
        this.subscribe = subscribe;
    }

}
