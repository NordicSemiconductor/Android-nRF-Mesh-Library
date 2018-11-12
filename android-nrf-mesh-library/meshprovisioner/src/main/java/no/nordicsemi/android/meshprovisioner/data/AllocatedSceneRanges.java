package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Relation;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.AllocatedGroupRange;
import no.nordicsemi.android.meshprovisioner.AllocatedSceneRange;
import no.nordicsemi.android.meshprovisioner.Provisioner;

@SuppressWarnings("unused")
class AllocatedSceneRanges {

    public String uuid;

    @Relation(entity = AllocatedSceneRange.class, parentColumn = "provisioner_uuid", entityColumn = "provisioner_uuid")
    public List<AllocatedGroupRange> allocatedGroupRanges;

}
