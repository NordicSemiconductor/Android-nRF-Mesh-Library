package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.Scene;

@SuppressWarnings("unused")
@Dao
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface SceneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final Scene scene);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<Scene> scenes);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final Scene scene);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<Scene> scenes);

    @Delete
    void delete(final Scene scene);

    @Query("DELETE FROM scene")
    void deleteAll();
}
