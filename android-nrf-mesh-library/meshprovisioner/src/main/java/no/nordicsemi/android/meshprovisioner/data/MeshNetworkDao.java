package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.MeshNetwork;

@SuppressWarnings("unused")
@Dao
public interface MeshNetworkDao {

    @Insert
    void insert(final MeshNetwork meshNetwork);

    @Update
    void update(final MeshNetwork meshNetwork);

    @Delete
    void delete(final MeshNetwork meshNetwork);

    @Query("DELETE FROM mesh_network")
    void deleteAll();

    @Query("SELECT * from mesh_network")
    List<MeshNetwork> getAllMeshNetworks();

    @Query("SELECT * from mesh_network WHERE mesh_uuid == :uuid")
    MeshNetwork getMeshNetwork(final String uuid);

}
