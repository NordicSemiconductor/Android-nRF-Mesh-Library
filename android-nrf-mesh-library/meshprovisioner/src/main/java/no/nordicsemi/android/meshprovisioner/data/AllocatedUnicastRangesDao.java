package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.support.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.AllocatedSceneRange;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
public interface AllocatedUnicastRangesDao {

    @Query("SELECT * from allocated_scene_range WHERE provisioner_uuid = :provisionerUuid")
    List<AllocatedSceneRange> loadAllocatedSceneRanges(final String provisionerUuid);


}
