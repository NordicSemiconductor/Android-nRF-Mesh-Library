package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Relation;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

@SuppressWarnings("unused")
class Elements {

    public byte[] address;

    @Relation(entity = ProvisionedMeshNode.class, parentColumn = "unicast_address", entityColumn = "address")
    public List<Element> elements;

}
