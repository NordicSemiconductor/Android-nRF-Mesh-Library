
package no.nordicsemi.android.meshprovisioner;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity(tableName = "mesh_network")
public final class MeshNetwork extends BaseMeshNetwork {

    public MeshNetwork() {
        super();
    }

    public String getSchema() {
        return schema;
    }

    void setSchema(String schema) {
        this.schema = schema;
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    void setVersion(String version) {
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

    void setNetKeys(List<NetworkKey> netKeys) {
        this.netKeys = netKeys;
    }

    public List<ApplicationKey> getAppKeys() {
        return appKeys;
    }

    void setAppKeys(List<ApplicationKey> appKeys) {
        this.appKeys = appKeys;
    }

    public List<Provisioner> getProvisioners() {
        return provisioners;
    }

    void setProvisioners(List<Provisioner> provisioners) {
        this.provisioners = provisioners;
    }

    public List<ProvisionedMeshNode> getNodes() {
        return nodes;
    }

    void setNodes(List<ProvisionedMeshNode> nodes) {
        this.nodes = nodes;
    }

    public List<Group> getGroups() {
        return groups;
    }

    void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }

    public boolean isLastSelected() {
        return lastSelected;
    }

    public void setLastSelected(final boolean lastSelected) {
        this.lastSelected = lastSelected;
    }
}