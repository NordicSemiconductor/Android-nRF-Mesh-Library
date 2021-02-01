package no.nordicsemi.android.mesh.data;

import androidx.annotation.RestrictTo;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import no.nordicsemi.android.mesh.Group;

@Dao
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final Group group);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(final Group group);

    @Query("DELETE FROM groups WHERE `group_address` = :groupAddress")
    void delete(final int groupAddress);
}
