package no.nordicsemi.android.meshprovisioner.data;

import androidx.room.Relation;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.Scene;

@SuppressWarnings("unused")
class Scenes {

    public String uuid;

    @Relation(entity = MeshNetwork.class, parentColumn = "mesh_uuid", entityColumn = "mesh_uuid")
    public List<Scene> scenes;

}
