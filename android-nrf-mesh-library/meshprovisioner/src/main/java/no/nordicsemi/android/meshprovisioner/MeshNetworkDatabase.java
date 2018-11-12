package no.nordicsemi.android.meshprovisioner;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import no.nordicsemi.android.meshprovisioner.data.ApplicationKeyDao;
import no.nordicsemi.android.meshprovisioner.data.ElementDao;
import no.nordicsemi.android.meshprovisioner.data.ElementsDao;
import no.nordicsemi.android.meshprovisioner.data.GroupDao;
import no.nordicsemi.android.meshprovisioner.data.GroupsDao;
import no.nordicsemi.android.meshprovisioner.data.MeshModelDao;
import no.nordicsemi.android.meshprovisioner.data.MeshModelsDao;
import no.nordicsemi.android.meshprovisioner.data.MeshNetworkDao;
import no.nordicsemi.android.meshprovisioner.data.NetworkKeyDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionedMeshNodeDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionedMeshNodesDao;
import no.nordicsemi.android.meshprovisioner.data.SceneDao;
import no.nordicsemi.android.meshprovisioner.data.ScenesDao;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
@Database(entities = {
        MeshNetwork.class,
        NetworkKey.class,
        ApplicationKey.class,
        ProvisionedMeshNode.class,
        Element.class,
        MeshModel.class,
        Provisioner.class,
        Group.class,
        Scene.class},
        version = 1)
public abstract class MeshNetworkDatabase extends RoomDatabase {

    public abstract MeshNetworkDao meshNetworkDao();

    public abstract NetworkKeyDao networkKeyDao();

    public abstract ApplicationKeyDao applicationKeyDao();

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

    static MeshNetworkDatabase getDatabase(final Context context) {
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
     *
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
