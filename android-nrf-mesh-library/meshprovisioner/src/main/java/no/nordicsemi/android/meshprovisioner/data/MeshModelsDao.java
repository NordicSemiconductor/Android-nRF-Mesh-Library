package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.MeshModel;

@SuppressWarnings("unused")
@Dao
public interface MeshModelsDao {

    @Query("SELECT * from models WHERE parent_address == :elementAddress")
    List<MeshModel> loadMeshModels(final byte[] elementAddress);
}
