package no.nordicsemi.android.meshprovisioner.data;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.Scene;

@SuppressWarnings("unused")
@Dao
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface ScenesDao {

    @Query("SELECT * from scene WHERE mesh_uuid == :uuid")
    List<Scene> loadScenes(final String uuid);
}
