package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.AllocatedSceneRange;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
public interface AllocatedSceneRangeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final AllocatedSceneRange allocatedSceneRange);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<AllocatedSceneRange> allocatedSceneRanges);

    @Update
    void update(final AllocatedSceneRange allocatedSceneRange);

    @Delete
    void delete(final AllocatedSceneRange allocatedSceneRange);

    @Query("DELETE FROM allocated_scene_range")
    void deleteAll();

}
