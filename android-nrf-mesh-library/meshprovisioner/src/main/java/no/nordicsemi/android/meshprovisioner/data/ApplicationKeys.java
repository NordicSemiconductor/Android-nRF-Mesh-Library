package no.nordicsemi.android.meshprovisioner.data;

import androidx.room.Relation;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.ApplicationKey;

@SuppressWarnings("unused")
class ApplicationKeys {

    public String uuid;

    @Relation(entity = MeshNetwork.class, parentColumn = "mesh_uuid", entityColumn = "mesh_uuid")
    public List<ApplicationKey> applicationKeys;

}
