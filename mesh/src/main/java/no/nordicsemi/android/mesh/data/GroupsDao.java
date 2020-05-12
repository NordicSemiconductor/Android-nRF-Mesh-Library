package no.nordicsemi.android.mesh.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.annotation.RestrictTo;

import java.util.List;

import androidx.room.Update;
import no.nordicsemi.android.mesh.Group;

@SuppressWarnings("unused")
@Dao
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface GroupsDao {

    @Query("SELECT * from groups WHERE mesh_uuid == :uuid")
    List<Group> loadGroups(final String uuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<Group> groups);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<Group> groups);

    @Query("DELETE FROM groups")
    void deleteAll();
}
