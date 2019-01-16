package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.RestrictTo;

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
