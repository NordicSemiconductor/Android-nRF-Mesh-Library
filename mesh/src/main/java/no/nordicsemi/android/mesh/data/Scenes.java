package no.nordicsemi.android.mesh.data;

import androidx.room.Relation;

import java.util.List;

import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.Scene;

@SuppressWarnings("unused")
class Scenes {

    public String uuid;

    @Relation(entity = MeshNetwork.class, parentColumn = "mesh_uuid", entityColumn = "mesh_uuid")
    public List<Scene> scenes;

}
