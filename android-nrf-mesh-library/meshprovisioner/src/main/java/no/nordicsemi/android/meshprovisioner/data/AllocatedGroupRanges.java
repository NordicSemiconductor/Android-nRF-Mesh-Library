package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;
import android.support.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.AllocatedGroupRange;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;

@SuppressWarnings("unused")
class AllocatedGroupRanges {

    @Embedded
    public Provisioner provisioner;

    @Relation(entity = AllocatedGroupRange.class, parentColumn = "provisioner_uuid", entityColumn = "provisioner_uuid")
    public List<AllocatedGroupRange> allocatedGroupRanges;

}
