package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Relation;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.Subscription;

@SuppressWarnings("unused")
class MeshSubscriptions {

    public int modelId;

    @Relation(parentColumn = "address", entityColumn = "parent_address", projection = {"model_id"})
    public List<Subscription> subscriptions;

}
