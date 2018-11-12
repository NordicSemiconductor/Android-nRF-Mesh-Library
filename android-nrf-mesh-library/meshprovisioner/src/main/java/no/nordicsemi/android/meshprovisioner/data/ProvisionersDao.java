package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.Provisioner;

@SuppressWarnings("unused")
@Dao
interface ProvisionersDao {

    @Query("SELECT mesh_uuid from provisioner")
    List<Provisioner> loadProvisioners();
}
