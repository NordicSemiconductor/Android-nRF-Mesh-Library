package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.support.annotation.RestrictTo;

import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Dao
public interface ApplicationKeysDao {

    @Query("SELECT * from application_key WHERE mesh_uuid = :meshUuid")
    List<ApplicationKey> loadApplicationKeys(final String meshUuid);
}
