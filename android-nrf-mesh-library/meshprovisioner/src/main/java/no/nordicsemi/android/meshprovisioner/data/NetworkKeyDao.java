package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;

@SuppressWarnings("unused")
@Dao
public interface NetworkKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final NetworkKey networkKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final NetworkKey...networkKeys);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<NetworkKey> networkKeys);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final NetworkKey networkKey);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<NetworkKey> netKeys);

    @Query("SELECT * from network_key WHERE mesh_uuid = :meshUuid")
    List<NetworkKey> loadNetworkKeys(final String meshUuid);

    @Delete
    void delete(final NetworkKey networkKey);

    @Query("DELETE FROM network_key")
    void deleteAll();
}
