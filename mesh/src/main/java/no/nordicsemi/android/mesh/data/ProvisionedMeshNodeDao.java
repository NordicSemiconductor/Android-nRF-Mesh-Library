package no.nordicsemi.android.mesh.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;
import androidx.annotation.RestrictTo;

import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
public interface ProvisionedMeshNodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final ProvisionedMeshNode provisionedMeshNode);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final ProvisionedMeshNode meshNode);

    @Delete
    void delete(final ProvisionedMeshNode meshNode);
}
