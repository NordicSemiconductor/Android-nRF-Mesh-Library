package no.nordicsemi.android.meshprovisioner.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
public interface ProvisionedMeshNodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final ProvisionedMeshNode provisionedMeshNode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<ProvisionedMeshNode> provisionedMeshNode);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final ProvisionedMeshNode meshNode);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<ProvisionedMeshNode> nodes);

    @Query("SELECT * from nodes WHERE mesh_uuid IS :meshUuid")
    List<ProvisionedMeshNode> getNodes(final String meshUuid);

    @Delete
    void delete(final ProvisionedMeshNode meshNode);

    @Query("DELETE from nodes")
    void deleteAll();
}
