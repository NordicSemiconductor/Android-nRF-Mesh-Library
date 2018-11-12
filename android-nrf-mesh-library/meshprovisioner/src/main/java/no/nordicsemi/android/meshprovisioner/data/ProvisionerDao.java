package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.Provisioner;

@SuppressWarnings("unused")
@Dao
public interface ProvisionerDao {

    @Insert
    void insert(final Provisioner group);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<Provisioner> provisioners);

    @Update
    void update(final Provisioner provisioner);

    @Delete
    void delete(final Provisioner provisioner);

    @Query("DELETE FROM provisioner")
    void deleteAll();

}
