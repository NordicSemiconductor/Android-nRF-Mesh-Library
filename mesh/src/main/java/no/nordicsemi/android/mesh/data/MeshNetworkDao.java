package no.nordicsemi.android.mesh.data;

import java.util.List;

import androidx.annotation.RestrictTo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import no.nordicsemi.android.mesh.MeshNetwork;

@Dao
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface MeshNetworkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final MeshNetwork meshNetwork);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final MeshNetwork meshNetwork);

    @Query("UPDATE mesh_network SET mesh_name = :meshName, timestamp = :timestamp, partial =:partial, " +
            "iv_index =:ivIndex, last_selected =:lastSelected, networkExclusions =:networkExclusions WHERE mesh_uuid = :meshUUID")
    void update(final String meshUUID, final String meshName, final long timestamp, final boolean partial,
                final String ivIndex, final boolean lastSelected, final String networkExclusions);

    @Delete
    void delete(final MeshNetwork meshNetwork);

    @Query("DELETE FROM mesh_network")
    void deleteAll();

    @Query("SELECT * from mesh_network")
    List<MeshNetwork> getAllMeshNetworks();

    @Query("SELECT * from mesh_network WHERE last_selected IS :lastSelected")
    MeshNetwork getMeshNetwork(final boolean lastSelected);
}
