package no.nordicsemi.android.mesh.data;

import java.util.List;

import androidx.annotation.RestrictTo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import no.nordicsemi.android.mesh.Provisioner;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
public interface ProvisionerDao {

    @Query("SELECT * from provisioner WHERE mesh_uuid = :meshUuid")
    List<Provisioner> getProvisioners(final String meshUuid);

    @Query("SELECT * from provisioner WHERE mesh_uuid IS :meshUuid AND last_selected IS :lastSelected")
    Provisioner getProvisioner(final String meshUuid, final boolean lastSelected);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final Provisioner provisioner);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<Provisioner> provisioners);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final Provisioner provisioner);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<Provisioner> provisioners);

    @Delete
    void delete(final Provisioner provisioner);

    @Query("DELETE FROM provisioner")
    void deleteAll();
}
