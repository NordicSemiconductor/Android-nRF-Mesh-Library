package no.nordicsemi.android.mesh.data;

import androidx.room.Relation;

import java.util.List;

import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.MeshNetwork;

@SuppressWarnings("unused")
class Groups {

    public String uuid;

    @Relation(entity = MeshNetwork.class, parentColumn = "mesh_uuid", entityColumn = "mesh_uuid")
    public List<Group> groups;

}
