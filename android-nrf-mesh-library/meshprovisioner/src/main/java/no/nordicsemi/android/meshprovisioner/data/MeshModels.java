package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Relation;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;

@SuppressWarnings("unused")
class MeshModels {

    public byte[] address;

    @Relation(entity = Element.class, parentColumn = "address", entityColumn = "parent_address")
    public List<MeshModel> meshModels;

}
