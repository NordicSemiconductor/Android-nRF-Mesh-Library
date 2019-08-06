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

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final ProvisionedMeshNode meshNode);

    @Delete
    void delete(final ProvisionedMeshNode meshNode);
}
