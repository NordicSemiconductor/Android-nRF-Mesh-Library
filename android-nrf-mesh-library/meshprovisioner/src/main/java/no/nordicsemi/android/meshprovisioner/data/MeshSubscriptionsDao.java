package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.Subscription;

@SuppressWarnings("unused")
@Dao
interface MeshSubscriptionsDao {

    @Query("SELECT parent_address from subscription")
    List<Subscription> loadSubscriptions();

}
