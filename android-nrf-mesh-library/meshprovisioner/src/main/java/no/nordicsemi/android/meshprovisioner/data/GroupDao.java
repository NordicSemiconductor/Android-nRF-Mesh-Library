package no.nordicsemi.android.meshprovisioner.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.Group;

@SuppressWarnings("unused")
@Dao
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final Group group);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<Group> groups);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final Group group);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<Group> groups);

    @Delete
    void delete(final Group group);

    @Query("DELETE FROM groups")
    void deleteAll();
}
