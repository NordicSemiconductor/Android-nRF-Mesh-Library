
package no.nordicsemi.android.meshprovisioner;

import android.content.Context;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

@SuppressWarnings("WeakerAccess")
public final class MeshNetwork extends BaseMeshNetwork {

    MeshNetwork(final Context context){
        super(context);
    }

    MeshNetwork() {
        super();
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public List<ApplicationKey> getAppKeys() {
        return appKeys;
    }

    public void setAppKeys(List<ApplicationKey> appKeys) {
        this.appKeys = appKeys;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public int getIvIndex() {
        return ivIndex;
    }

    public void setIvIndex(int ivIndex) {
        this.ivIndex = ivIndex;
    }

    public int getIvUpdate() {
        return ivUpdate;
    }

    public void setIvUpdate(int ivUpdate) {
        this.ivUpdate = ivUpdate;
    }

    public String getMeshName() {
        return meshName;
    }

    public void setMeshName(String meshName) {
        this.meshName = meshName;
    }

    public String getMeshUUID() {
        return meshUUID;
    }

    public void setMeshUUID(String meshUUID) {
        this.meshUUID = meshUUID;
    }

    public List<NetworkKey> getNetKeys() {
        return netKeys;
    }

    public void setNetKeys(List<NetworkKey> netKeys) {
        this.netKeys = netKeys;
    }

    public List<ProvisionedMeshNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<ProvisionedMeshNode> nodes) {
        this.nodes = nodes;
    }

    public List<Provisioner> getProvisioners() {
        return provisioners;
    }

    public void setProvisioners(List<Provisioner> provisioners) {
        this.provisioners = provisioners;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
