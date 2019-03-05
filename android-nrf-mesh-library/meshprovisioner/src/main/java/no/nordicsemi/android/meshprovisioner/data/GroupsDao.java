package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.support.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.Group;

@SuppressWarnings("unused")
@Dao
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface GroupsDao {

    @Query("SELECT * from groups WHERE mesh_uuid == :uuid")
    List<Group> loadGroups(final String uuid);
}
