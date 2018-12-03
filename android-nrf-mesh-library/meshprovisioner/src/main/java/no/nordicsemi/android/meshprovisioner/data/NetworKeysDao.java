package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Query;
import android.support.annotation.RestrictTo;

import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("ALL")
public interface NetworKeysDao {

    @Query("SELECT * from network_key WHERE uuid = :meshUuid")
    List<NetworkKeys> loadNetworkKeys(final String meshUuid);
}
