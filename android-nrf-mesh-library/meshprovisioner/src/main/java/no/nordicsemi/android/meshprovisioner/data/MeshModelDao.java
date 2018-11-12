package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.MeshModel;

@SuppressWarnings("unused")
@Dao
public interface MeshModelDao {

    @Insert
    void insert(final MeshModel meshModel);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<MeshModel> models);

    @Update
    void update(final MeshModel meshModel);

    @Delete
    void delete(final MeshModel meshModel);

    @Query("DELETE FROM models")
    void deleteAll();

}
