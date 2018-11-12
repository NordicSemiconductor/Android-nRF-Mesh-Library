package no.nordicsemi.android.meshprovisioner;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

@SuppressWarnings({"unused", "WeakerAccess"})
abstract class BaseMeshNetwork {

    @Ignore
    @SerializedName("$schema")
    @Expose
    String schema = "http://json-schema.org/draft-04/schema#";

    @Ignore
    @SerializedName("id")
    @Expose
    String id = "TBD";

    @Ignore
    @SerializedName("version")
    @Expose
    String version = "1.0";

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "mesh_uuid")
    @SerializedName("meshUUID")
    @Expose
    String meshUUID;

    @ColumnInfo(name = "mesh_name")
    @SerializedName("meshName")
    @Expose
    String meshName= "nRF Mesh Network";

    @ColumnInfo(name = "timestamp")
    @SerializedName("timestamp")
    @Expose
    String timestamp;

    @Ignore
    @SerializedName("netKeys")
    @Expose
    List<NetworkKey> netKeys;

    @Ignore
    @SerializedName("appKeys")
    @Expose
    List<ApplicationKey> appKeys;

    @Ignore
    @SerializedName("provisioners")
    @Expose
    List<Provisioner> provisioners;

    @Ignore
    @SerializedName("nodes")
    @Expose
    List<ProvisionedMeshNode> nodes;

    @Ignore
    @SerializedName("groups")
    @Expose
    List<Group> groups = null;

    @Ignore
    @SerializedName("scenes")
    @Expose
    List<Scene> scenes = null;

    //Library related attributes
    @ColumnInfo(name = "last_selected")
    @Expose
    boolean lastSelected;

    @Ignore
    private ProvisioningSettings mProvisioningSettings;

    @Ignore
    private Map<Integer, ProvisionedMeshNode> mProvisionedNodes = new LinkedHashMap<>();

    @Ignore
    BaseMeshNetwork(final Context context) {
        this.mProvisioningSettings = new ProvisioningSettings(context);
    }

    BaseMeshNetwork() {

    }

    public final List<ProvisionedMeshNode> getProvisionedNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public ProvisioningSettings getProvisioningSettings() {
        return mProvisioningSettings;
    }
}
