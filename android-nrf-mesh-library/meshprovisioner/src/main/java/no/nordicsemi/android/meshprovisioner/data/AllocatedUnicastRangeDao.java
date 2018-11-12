package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.AllocatedUnicastRange;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
public interface AllocatedUnicastRangeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final AllocatedUnicastRange allocatedUnicastRange);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<AllocatedUnicastRange> allocatedUnicastRanges);

    @Update
    void update(final AllocatedUnicastRange allocatedUnicastRange);

    @Delete
    void delete(final AllocatedUnicastRange allocatedUnicastRange);

    @Query("DELETE FROM allocated_unicast_range")
    void deleteAll();

}
