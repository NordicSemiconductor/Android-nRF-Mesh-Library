package no.nordicsemi.android.mesh.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.annotation.RestrictTo;

import java.util.List;

import androidx.room.Update;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
public interface ProvisionedMeshNodesDao {

    @Query("SELECT * from nodes WHERE mesh_uuid == :meshUuid")
    List<ProvisionedMeshNode> loadMeshNodes(final String meshUuid);

    @Query("SELECT * from nodes WHERE mesh_uuid IS :meshUuid")
    List<ProvisionedMeshNode> getNodes(final String meshUuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<ProvisionedMeshNode> provisionedMeshNode);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<ProvisionedMeshNode> nodes);

    @Query("DELETE from nodes")
    void deleteAll();
}
