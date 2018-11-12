package no.nordicsemi.android.meshprovisioner.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import no.nordicsemi.android.meshprovisioner.AllocatedGroupRange;
import no.nordicsemi.android.meshprovisioner.AllocatedSceneRange;
import no.nordicsemi.android.meshprovisioner.AllocatedUnicastRange;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.Scene;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Database(entities = {
        MeshNetwork.class,
        NetworkKey.class,
        ApplicationKey.class,
        Provisioner.class,
        AllocatedGroupRange.class,
        AllocatedUnicastRange.class,
        AllocatedSceneRange.class,
        ProvisionedMeshNode.class,
        Element.class,
        MeshModel.class,
        Group.class,
        Scene.class},
        version = 1)
public abstract class MeshNetworkDatabase extends RoomDatabase {

    public abstract MeshNetworkDao meshNetworkDao();

    public abstract NetworkKeyDao networkKeyDao();

    public abstract ApplicationKeyDao applicationKeyDao();

    public abstract ProvisionerDao provisionerDao();

    public abstract AllocatedGroupRangeDao allocatedGroupRangeDao();

    public abstract AllocatedUnicastRangeDao allocatedUnicastRangeDao();

    public abstract AllocatedSceneRangeDao allocatedSceneRangeDao();

    public abstract ProvisionedMeshNodesDao provisionedMeshNodesDao();

    public abstract ProvisionedMeshNodeDao provisionedMeshNodeDao();

    public abstract ElementsDao elementsDao();

    public abstract ElementDao elementDao();

    public abstract MeshModelsDao meshModelsDao();

    public abstract MeshModelDao meshModelDao();

    public abstract GroupsDao groupsDao();

    public abstract GroupDao groupDao();

    public abstract ScenesDao scenesDao();

    public abstract SceneDao sceneDao();

    private static volatile MeshNetworkDatabase INSTANCE;

    /**
     * Returns the mesh database
     */
    public static MeshNetworkDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MeshNetworkDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MeshNetworkDatabase.class, "mesh_network_database.db")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }

            }
        }
        return INSTANCE;
    }


    /**
     * Override the onOpen method to populate the database.
     * For this sample, we clear the database every time it is created or opened.
     * <p>
     * If you want to populate the database only when the database is created for the 1st time,
     * override RoomDatabase.Callback()#onCreate
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };
}
