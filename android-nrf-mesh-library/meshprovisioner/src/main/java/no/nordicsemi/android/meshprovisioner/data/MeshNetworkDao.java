package no.nordicsemi.android.meshprovisioner.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.MeshNetwork;

@SuppressWarnings("unused")
@Dao
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface MeshNetworkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final MeshNetwork meshNetwork);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final MeshNetwork meshNetwork);

    @Delete
    void delete(final MeshNetwork meshNetwork);

    @Query("DELETE FROM mesh_network")
    void deleteAll();

    @Query("SELECT * from mesh_network")
    List<MeshNetwork> getAllMeshNetworks();

    @Query("SELECT * from mesh_network WHERE last_selected IS :lastSelected")
    MeshNetwork getMeshNetwork(final boolean lastSelected);

}
