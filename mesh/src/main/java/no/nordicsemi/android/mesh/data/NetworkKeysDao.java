package no.nordicsemi.android.mesh.data;

import java.util.List;

import androidx.annotation.RestrictTo;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import no.nordicsemi.android.mesh.NetworkKey;

@Dao
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface NetworkKeysDao {

    @Query("SELECT * from network_key WHERE mesh_uuid = :meshUuid")
    List<NetworkKey> loadNetworkKeys(final String meshUuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final NetworkKey... networkKeys);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final List<NetworkKey> networkKeys);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<NetworkKey> netKeys);

    @Query("DELETE FROM network_key")
    void deleteAll();
}
