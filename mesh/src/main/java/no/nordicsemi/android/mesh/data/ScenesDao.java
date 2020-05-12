package no.nordicsemi.android.mesh.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.annotation.RestrictTo;

import java.util.List;

import androidx.room.Update;
import no.nordicsemi.android.mesh.Scene;

@SuppressWarnings("unused")
@Dao
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface ScenesDao {

    @Query("SELECT * from scene WHERE mesh_uuid == :uuid")
    List<Scene> loadScenes(final String uuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<Scene> scenes);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<Scene> scenes);

    @Query("DELETE FROM scene")
    void deleteAll();
}
