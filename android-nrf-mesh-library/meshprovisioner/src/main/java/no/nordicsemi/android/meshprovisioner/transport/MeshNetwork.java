
package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.Scene;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class MeshNetwork extends BaseMeshNetwork {

    public MeshNetwork(final Context context){
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMeshUUID() {
        return meshUUID;
    }

    public void setMeshUUID(String meshUUID) {
        this.meshUUID = meshUUID;
    }

    public String getMeshName() {
        return meshName;
    }

    public void setMeshName(String meshName) {
        this.meshName = meshName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<NetworkKey> getNetKeys() {
        return netKeys;
    }

    public void setNetKeys(List<NetworkKey> netKeys) {
        this.netKeys = netKeys;
    }

    public List<ApplicationKey> getAppKeys() {
        return appKeys;
    }

    public void setAppKeys(List<ApplicationKey> appKeys) {
        this.appKeys = appKeys;
    }

    public List<Provisioner> getProvisioners() {
        return provisioners;
    }

    public void setProvisioners(List<Provisioner> provisioners) {
        this.provisioners = provisioners;
    }

    public List<ProvisionedMeshNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<ProvisionedMeshNode> nodes) {
        this.nodes = nodes;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }

}