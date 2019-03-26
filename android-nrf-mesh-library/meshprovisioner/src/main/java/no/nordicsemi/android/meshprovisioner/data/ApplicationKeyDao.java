package no.nordicsemi.android.meshprovisioner.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
public interface ApplicationKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(final ApplicationKey applicationKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<ApplicationKey> applicationKeys);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final ApplicationKey applicationKey);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<ApplicationKey> appKeys);

    @Query("SELECT * from application_key WHERE mesh_uuid = :meshUuid")
    List<ApplicationKey> loadApplicationKeys(final String meshUuid);

    @Delete
    void delete(final ApplicationKey applicationKey);

    @Query("DELETE FROM application_key")
    void deleteAll();
}
