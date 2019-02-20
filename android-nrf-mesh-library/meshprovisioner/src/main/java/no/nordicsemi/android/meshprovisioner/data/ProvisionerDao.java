package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.Provisioner;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
public interface ProvisionerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final Provisioner provisioner);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<Provisioner> provisioners);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final Provisioner provisioner);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<Provisioner> provisioners);

    @Query("SELECT * from provisioner WHERE mesh_uuid IS :meshUuid AND last_selected IS :lastSelected")
    Provisioner getProvisioner(final String meshUuid, final boolean lastSelected);

    @Delete
    void delete(final Provisioner provisioner);

    @Query("DELETE FROM provisioner")
    void deleteAll();
}
