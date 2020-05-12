package no.nordicsemi.android.mesh.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;
import androidx.annotation.RestrictTo;

import no.nordicsemi.android.mesh.Group;

@SuppressWarnings("unused")
@Dao
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final Group group);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final Group group);

    @Delete
    void delete(final Group group);
}
