package no.nordicsemi.android.mesh.data;

import java.util.List;

import androidx.annotation.RestrictTo;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import no.nordicsemi.android.mesh.ApplicationKey;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
public interface ApplicationKeysDao {

    @Query("SELECT * from application_key WHERE mesh_uuid = :meshUuid")
    List<ApplicationKey> loadApplicationKeys(final String meshUuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<ApplicationKey> applicationKeys);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<ApplicationKey> appKeys);

    @Query("DELETE FROM application_key")
    void deleteAll();
}
