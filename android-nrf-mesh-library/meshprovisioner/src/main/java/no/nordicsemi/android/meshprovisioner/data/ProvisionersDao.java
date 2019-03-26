package no.nordicsemi.android.meshprovisioner.data;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.Provisioner;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
interface ProvisionersDao {

    @Query("SELECT * from provisioner WHERE mesh_uuid = :meshUuid")
    List<Provisioner> loadProvisioners(final String meshUuid);
}
