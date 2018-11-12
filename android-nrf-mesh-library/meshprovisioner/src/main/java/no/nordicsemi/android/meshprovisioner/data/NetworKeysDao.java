package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.room.Query;

import java.util.List;

interface NetworKeysDao {

    @Query("SELECT mesh_uuid from mesh_network")
    List<NetworkKeys> loadNetworkKeys();
}
