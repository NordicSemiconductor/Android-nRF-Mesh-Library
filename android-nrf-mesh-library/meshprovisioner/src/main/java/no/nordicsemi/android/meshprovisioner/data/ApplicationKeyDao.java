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

    @Insert
    void insert(final ApplicationKey applicationKey);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<ApplicationKey> applicationKeys);

    @Update
    void update(final ApplicationKey applicationKey);

    @Update
    void update(List<ApplicationKey> appKeys);
    
    @Delete
    void delete(final ApplicationKey applicationKey);

    @Query("DELETE FROM application_key")
    void deleteAll();
}
