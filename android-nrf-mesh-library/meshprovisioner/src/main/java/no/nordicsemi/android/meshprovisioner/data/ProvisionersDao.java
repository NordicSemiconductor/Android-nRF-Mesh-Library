package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.support.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.Provisioner;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
interface ProvisionersDao {

    @Query("SELECT * from provisioner WHERE mesh_uuid = :meshUuid")
    List<Provisioner> loadProvisioners(final String meshUuid);
}
