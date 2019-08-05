package no.nordicsemi.android.meshprovisioner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import no.nordicsemi.android.meshprovisioner.data.ApplicationKeyDao;
import no.nordicsemi.android.meshprovisioner.data.ApplicationKeysDao;
import no.nordicsemi.android.meshprovisioner.data.GroupDao;
import no.nordicsemi.android.meshprovisioner.data.GroupsDao;
import no.nordicsemi.android.meshprovisioner.data.MeshNetworkDao;
import no.nordicsemi.android.meshprovisioner.data.NetworkKeyDao;
import no.nordicsemi.android.meshprovisioner.data.NetworkKeysDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionedMeshNodeDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionedMeshNodesDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionerDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionersDao;
import no.nordicsemi.android.meshprovisioner.data.SceneDao;
import no.nordicsemi.android.meshprovisioner.data.ScenesDao;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Database(entities = {
        MeshNetwork.class,
        NetworkKey.class,
        ApplicationKey.class,
        Provisioner.class,
        ProvisionedMeshNode.class,
        Group.class,
        Scene.class},
        version = 7)
abstract class MeshNetworkDb extends RoomDatabase {

    private static String TAG = MeshNetworkDb.class.getSimpleName();

    abstract MeshNetworkDao meshNetworkDao();

    abstract NetworkKeyDao networkKeyDao();

    abstract NetworkKeysDao networkKeysDao();

    abstract ApplicationKeyDao applicationKeyDao();

    abstract ApplicationKeysDao applicationKeysDao();

    abstract ProvisionerDao provisionerDao();

    abstract ProvisionersDao provisionersDao();

    abstract ProvisionedMeshNodeDao provisionedMeshNodeDao();

    abstract ProvisionedMeshNodesDao provisionedMeshNodesDao();

    abstract GroupsDao groupsDao();

    abstract GroupDao groupDao();

    abstract ScenesDao scenesDao();

    abstract SceneDao sceneDao();

    private static volatile MeshNetworkDb INSTANCE;

    /**
     * Returns the mesh database
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    static MeshNetworkDb getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MeshNetworkDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MeshNetworkDb.class, "mesh_network_database.db")
                            .addCallback(sRoomDatabaseCallback)
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
                            .addMigrations(MIGRATION_4_5)
                            .addMigrations(MIGRATION_5_6)
                            .addMigrations(MIGRATION_6_7)
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

    void insertNetwork(@NonNull final MeshNetworkDao dao,
                       @NonNull final NetworkKeysDao netKeysDao,
                       @NonNull final ApplicationKeysDao appKeysDao,
                       @NonNull final ProvisionersDao provisionersDao,
                       @NonNull final ProvisionedMeshNodesDao nodesDao,
                       @NonNull final GroupsDao groupsDao,
                       @NonNull final ScenesDao scenesDao,
                       @NonNull final MeshNetwork meshNetwork) {
        new InsertNetworkAsyncTask(dao,
                netKeysDao,
                appKeysDao,
                provisionersDao,
                nodesDao,
                groupsDao,
                scenesDao,
                meshNetwork).execute();
    }

    void loadNetwork(@NonNull final MeshNetworkDao dao,
                     @NonNull final NetworkKeysDao netKeysDao,
                     @NonNull final ApplicationKeysDao appKeysDao,
                     @NonNull final ProvisionersDao provisionersDao,
                     @NonNull final ProvisionedMeshNodesDao nodesDao,
                     @NonNull final GroupsDao groupsDao,
                     @NonNull final ScenesDao scenesDao,
                     @NonNull final LoadNetworkCallbacks listener) {
        new LoadNetworkAsyncTask(dao,
                netKeysDao,
                appKeysDao,
                provisionersDao,
                nodesDao,
                groupsDao,
                scenesDao,
                listener).execute();
    }

    void updateNetwork(@NonNull final MeshNetworkDao dao, @NonNull final MeshNetwork meshNetwork) {
        new UpdateNetworkAsyncTask(dao).execute(meshNetwork);
    }

    void updateNetwork1(@NonNull final MeshNetwork meshNetwork,
                        @NonNull final MeshNetworkDao dao,
                        @NonNull final NetworkKeysDao netKeyDao,
                        @NonNull final ApplicationKeysDao appKeyDao,
                        @NonNull final ProvisionersDao provisionerDao,
                        @NonNull final ProvisionedMeshNodesDao nodeDao,
                        @NonNull final GroupsDao groupsDao,
                        @NonNull final ScenesDao sceneDao) {
        new UpdateNetworkAsyncTask1(dao,
                netKeyDao,
                appKeyDao,
                provisionerDao,
                nodeDao,
                groupsDao,
                sceneDao).execute(meshNetwork);
    }

    void deleteNetwork(@NonNull final MeshNetworkDao dao, @NonNull final MeshNetwork meshNetwork) {
        new DeleteNetworkAsyncTask(dao).execute(meshNetwork);
    }

    void insertNetKey(@NonNull final NetworkKeyDao dao, @NonNull final NetworkKey networkKey) {
        new InsertNetKeyAsyncTask(dao).execute(networkKey);
    }

    void updateNetKey(@NonNull final NetworkKeyDao dao, @NonNull final NetworkKey networkKey) {
        new UpdateNetKeyAsyncTask(dao).execute(networkKey);
    }

    void deleteNetKey(@NonNull final NetworkKeyDao dao, @NonNull final NetworkKey networkKey) {
        new DeleteNetKeyAsyncTask(dao).execute(networkKey);
    }

    void insertAppKey(@NonNull final ApplicationKeyDao dao, @NonNull final ApplicationKey applicationKey) {
        new InsertAppKeyAsyncTask(dao).execute(applicationKey);
    }

    void updateAppKey(@NonNull final ApplicationKeyDao dao, @NonNull final ApplicationKey applicationKey) {
        new UpdateAppKeyAsyncTask(dao).execute(applicationKey);
    }

    void deleteAppKey(@NonNull final ApplicationKeyDao dao, @NonNull final ApplicationKey applicationKey) {
        new DeleteAppKeyAsyncTask(dao).execute(applicationKey);
    }

    void insertProvisioner(@NonNull final ProvisionerDao dao, @NonNull final Provisioner provisioner) {
        new InsertProvisionerAsyncTask(dao).execute(provisioner);
    }

    void updateProvisioner(@NonNull final ProvisionerDao dao, @NonNull final Provisioner provisioner) {
        new UpdateProvisionerAsyncTask(dao).execute(provisioner);
    }

    void updateProvisioners(@NonNull final ProvisionerDao dao, @NonNull final List<Provisioner> provisioners) {
        new UpdateProvisionersAsyncTask(dao, provisioners).execute();
    }

    void deleteProvisioner(@NonNull final ProvisionerDao dao, @NonNull final Provisioner provisioner) {
        new DeleteProvisionerAsyncTask(dao).execute(provisioner);
    }

    void insertNode(@NonNull final ProvisionedMeshNodeDao dao, @NonNull final ProvisionedMeshNode node) {
        new InsertNodeAsyncTask(dao).execute(node);
    }

    void updateNode(@NonNull final ProvisionedMeshNodeDao dao, @NonNull final ProvisionedMeshNode node) {
        new UpdateNodeAsyncTask(dao).execute(node);
    }

    void updateNodes(@NonNull final ProvisionedMeshNodesDao dao, @NonNull final List<ProvisionedMeshNode> nodes) {
        new UpdateNodesAsyncTask(dao, nodes).execute();
    }

    void deleteNode(@NonNull final ProvisionedMeshNodeDao dao, @NonNull final ProvisionedMeshNode node) {
        new DeleteNodeAsyncTask(dao).execute(node);
    }

    void insertGroup(@NonNull final GroupDao dao, @NonNull final Group group) {
        new InsertGroupAsyncTask(dao).execute(group);
    }

    void updateGroup(@NonNull final GroupDao dao, @NonNull final Group group) {
        new UpdateGroupAsyncTask(dao).execute(group);
    }

    void updateGroups(@NonNull final GroupsDao dao, @NonNull final List<Group> groups) {
        new UpdateGroupsAsyncTask(dao, groups).execute();
    }

    void deleteGroup(@NonNull final GroupDao dao, @NonNull final Group group) {
        new DeleteGroupAsyncTask(dao).execute(group);
    }

    void insertScene(@NonNull final SceneDao dao, @NonNull final Scene scene) {
        new InsertSceneAsyncTask(dao).execute(scene);
    }

    void updateScene(@NonNull final SceneDao dao, @NonNull final Scene scene) {
        new UpdateSceneKeyAsyncTask(dao).execute(scene);
    }

    void deleteScene(@NonNull final SceneDao dao, @NonNull final Scene scene) {
        new DeleteSceneKeyAsyncTask(dao).execute(scene);
    }

    private static class InsertNetworkAsyncTask extends AsyncTask<Void, Void, Void> {

        private final MeshNetwork meshNetwork;
        private final MeshNetworkDao meshNetworkDao;
        private final NetworkKeysDao netKeysDao;
        private final ApplicationKeysDao appKeysDao;
        private final ProvisionersDao provisionersDao;
        private final ProvisionedMeshNodesDao nodesDao;
        private final GroupsDao groupsDao;
        private final ScenesDao scenesDao;

        InsertNetworkAsyncTask(@NonNull final MeshNetworkDao meshNetworkDao,
                               @NonNull final NetworkKeysDao netKeysDao,
                               @NonNull final ApplicationKeysDao appKeysDao,
                               @NonNull final ProvisionersDao provisionersDao,
                               @NonNull final ProvisionedMeshNodesDao nodesDao,
                               @NonNull final GroupsDao groupsDao,
                               @NonNull final ScenesDao scenesDao,
                               @NonNull final MeshNetwork meshNetwork) {
            this.meshNetworkDao = meshNetworkDao;
            this.netKeysDao = netKeysDao;
            this.appKeysDao = appKeysDao;
            this.provisionersDao = provisionersDao;
            this.nodesDao = nodesDao;
            this.groupsDao = groupsDao;
            this.scenesDao = scenesDao;
            this.meshNetwork = meshNetwork;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            meshNetworkDao.insert(meshNetwork);
            netKeysDao.insert(meshNetwork.netKeys);
            appKeysDao.insert(meshNetwork.appKeys);
            provisionersDao.insert(meshNetwork.provisioners);
            if (!meshNetwork.nodes.isEmpty()) {
                nodesDao.insert(meshNetwork.nodes);
            }

            if (meshNetwork.groups != null) {
                groupsDao.insert(meshNetwork.groups);
            }

            if (meshNetwork.scenes != null) {
                scenesDao.insert(meshNetwork.scenes);
            }
            return null;
        }
    }

    private static class LoadNetworkAsyncTask extends AsyncTask<Void, Void, MeshNetwork> {

        private final LoadNetworkCallbacks listener;
        private final MeshNetworkDao meshNetworkDao;
        private final NetworkKeysDao netKeysDao;
        private final ApplicationKeysDao appKeysDao;
        private final ProvisionersDao provisionersDao;
        private final ProvisionedMeshNodesDao nodesDao;
        private final GroupsDao groupsDao;
        private final ScenesDao sceneDao;

        LoadNetworkAsyncTask(@NonNull final MeshNetworkDao meshNetworkDao,
                             @NonNull final NetworkKeysDao netKeysDao,
                             @NonNull final ApplicationKeysDao appKeysDao,
                             @NonNull final ProvisionersDao provisionersDao,
                             @NonNull final ProvisionedMeshNodesDao nodesDao,
                             @NonNull final GroupsDao groupsDao,
                             @NonNull final ScenesDao sceneDao,
                             @NonNull final LoadNetworkCallbacks listener) {
            this.meshNetworkDao = meshNetworkDao;
            this.netKeysDao = netKeysDao;
            this.appKeysDao = appKeysDao;
            this.provisionersDao = provisionersDao;
            this.nodesDao = nodesDao;
            this.groupsDao = groupsDao;
            this.sceneDao = sceneDao;
            this.listener = listener;
        }

        @Override
        protected MeshNetwork doInBackground(final Void... params) {
            final MeshNetwork meshNetwork = meshNetworkDao.getMeshNetwork(true);
            if (meshNetwork != null) {
                meshNetwork.netKeys = netKeysDao.loadNetworkKeys(meshNetwork.getMeshUUID());
                meshNetwork.appKeys = appKeysDao.loadApplicationKeys(meshNetwork.getMeshUUID());
                meshNetwork.nodes = nodesDao.getNodes(meshNetwork.getMeshUUID());
                meshNetwork.provisioners = provisionersDao.getProvisioners(meshNetwork.getMeshUUID());
                meshNetwork.groups = groupsDao.loadGroups(meshNetwork.getMeshUUID());
            }
            return meshNetwork;
        }

        @Override
        protected void onPostExecute(final MeshNetwork meshNetwork) {
            super.onPostExecute(meshNetwork);
            listener.onNetworkLoadedFromDb(meshNetwork);
        }
    }

    private static class UpdateNetworkAsyncTask extends AsyncTask<MeshNetwork, Void, Void> {

        private MeshNetworkDao mAsyncTaskDao;

        UpdateNetworkAsyncTask(@NonNull final MeshNetworkDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final MeshNetwork... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class UpdateNetworkAsyncTask1 extends AsyncTask<MeshNetwork, Void, Void> {

        private final MeshNetworkDao meshNetworkDao;
        private final NetworkKeysDao netKeyDao;
        private final ApplicationKeysDao appKeyDao;
        private final ProvisionersDao provisionersDao;
        private final ProvisionedMeshNodesDao nodesDao;
        private final GroupsDao groupsDao;
        private final ScenesDao sceneDao;

        UpdateNetworkAsyncTask1(@NonNull final MeshNetworkDao meshNetworkDao,
                                @NonNull final NetworkKeysDao netKeysDao,
                                @NonNull final ApplicationKeysDao appKeysDao,
                                @NonNull final ProvisionersDao provisionersDao,
                                @NonNull final ProvisionedMeshNodesDao nodesDao,
                                @NonNull final GroupsDao groupsDao,
                                @NonNull final ScenesDao scenesDao) {
            this.meshNetworkDao = meshNetworkDao;
            this.netKeyDao = netKeysDao;
            this.appKeyDao = appKeysDao;
            this.provisionersDao = provisionersDao;
            this.nodesDao = nodesDao;
            this.groupsDao = groupsDao;
            this.sceneDao = scenesDao;
        }

        @Override
        protected Void doInBackground(@NonNull final MeshNetwork... params) {
            final MeshNetwork network = params[0];
            meshNetworkDao.update(network);
            netKeyDao.update(network.getNetKeys());
            appKeyDao.update(network.getAppKeys());
            provisionersDao.update(network.getProvisioners());
            nodesDao.update(network.getNodes());
            groupsDao.update(network.getGroups());
            sceneDao.update(network.getScenes());
            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private static class DeleteNetworkAsyncTask extends AsyncTask<MeshNetwork, Void, Void> {

        private MeshNetworkDao mAsyncTaskDao;

        DeleteNetworkAsyncTask(@NonNull final MeshNetworkDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final MeshNetwork... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class InsertNetKeyAsyncTask extends AsyncTask<NetworkKey, Void, Void> {

        private NetworkKeyDao mAsyncTaskDao;

        InsertNetKeyAsyncTask(@NonNull final NetworkKeyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final NetworkKey... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateNetKeyAsyncTask extends AsyncTask<NetworkKey, Void, Void> {

        private NetworkKeyDao mAsyncTaskDao;

        UpdateNetKeyAsyncTask(@NonNull final NetworkKeyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final NetworkKey... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class DeleteNetKeyAsyncTask extends AsyncTask<NetworkKey, Void, Void> {

        private NetworkKeyDao mAsyncTaskDao;

        DeleteNetKeyAsyncTask(@NonNull final NetworkKeyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final NetworkKey... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class InsertAppKeyAsyncTask extends AsyncTask<ApplicationKey, Void, Void> {

        private ApplicationKeyDao mAsyncTaskDao;

        InsertAppKeyAsyncTask(@NonNull final ApplicationKeyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ApplicationKey... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateAppKeyAsyncTask extends AsyncTask<ApplicationKey, Void, Void> {

        private ApplicationKeyDao mAsyncTaskDao;

        UpdateAppKeyAsyncTask(@NonNull final ApplicationKeyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ApplicationKey... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class DeleteAppKeyAsyncTask extends AsyncTask<ApplicationKey, Void, Void> {

        private ApplicationKeyDao mAsyncTaskDao;

        DeleteAppKeyAsyncTask(@NonNull final ApplicationKeyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ApplicationKey... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class InsertProvisionerAsyncTask extends AsyncTask<Provisioner, Void, Void> {

        private final ProvisionerDao mAsyncTaskDao;

        InsertProvisionerAsyncTask(@NonNull final ProvisionerDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Provisioner... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateProvisionerAsyncTask extends AsyncTask<Provisioner, Void, Void> {

        private final ProvisionerDao mAsyncTaskDao;

        UpdateProvisionerAsyncTask(@NonNull final ProvisionerDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Provisioner... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class UpdateProvisionersAsyncTask extends AsyncTask<Void, Void, Void> {

        private final ProvisionerDao mAsyncTaskDao;
        private final List<Provisioner> provisioners;

        UpdateProvisionersAsyncTask(@NonNull final ProvisionerDao dao,
                                    @NonNull final List<Provisioner> provisioners) {
            mAsyncTaskDao = dao;
            this.provisioners = provisioners;
        }

        @Override
        protected Void doInBackground(final Void... voids) {
            mAsyncTaskDao.update(provisioners);
            return null;
        }
    }

    private static class DeleteProvisionerAsyncTask extends AsyncTask<Provisioner, Void, Void> {

        private final ProvisionerDao mAsyncTaskDao;

        DeleteProvisionerAsyncTask(@NonNull final ProvisionerDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Provisioner... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class InsertNodeAsyncTask extends AsyncTask<ProvisionedMeshNode, Void, Void> {

        private ProvisionedMeshNodeDao mAsyncTaskDao;

        InsertNodeAsyncTask(@NonNull final ProvisionedMeshNodeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ProvisionedMeshNode... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateNodeAsyncTask extends AsyncTask<ProvisionedMeshNode, Void, Void> {

        private ProvisionedMeshNodeDao mAsyncTaskDao;

        UpdateNodeAsyncTask(@NonNull final ProvisionedMeshNodeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ProvisionedMeshNode... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class UpdateNodesAsyncTask extends AsyncTask<Void, Void, Void> {

        private ProvisionedMeshNodesDao mAsyncTaskDao;
        private List<ProvisionedMeshNode> nodes;

        UpdateNodesAsyncTask(@NonNull final ProvisionedMeshNodesDao dao,
                             @NonNull final List<ProvisionedMeshNode> nodes) {
            mAsyncTaskDao = dao;
            this.nodes = nodes;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mAsyncTaskDao.update(nodes);
            return null;
        }
    }

    private static class DeleteNodeAsyncTask extends AsyncTask<ProvisionedMeshNode, Void, Void> {

        private ProvisionedMeshNodeDao mAsyncTaskDao;

        DeleteNodeAsyncTask(@NonNull final ProvisionedMeshNodeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ProvisionedMeshNode... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class InsertGroupAsyncTask extends AsyncTask<Group, Void, Void> {

        private final GroupDao mAsyncTaskDao;

        InsertGroupAsyncTask(@NonNull final GroupDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Group... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateGroupAsyncTask extends AsyncTask<Group, Void, Void> {

        private final GroupDao mAsyncTaskDao;

        UpdateGroupAsyncTask(@NonNull final GroupDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Group... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class UpdateGroupsAsyncTask extends AsyncTask<Void, Void, Void> {

        private final GroupsDao mAsyncTaskDao;
        private final List<Group> mGroups;

        UpdateGroupsAsyncTask(@NonNull final GroupsDao dao, @NonNull final List<Group> groups) {
            mAsyncTaskDao = dao;
            mGroups = groups;
        }

        @Override
        protected Void doInBackground(final Void... voids) {
            mAsyncTaskDao.update(mGroups);
            return null;
        }
    }

    private static class DeleteGroupAsyncTask extends AsyncTask<Group, Void, Void> {

        private GroupDao mAsyncTaskDao;

        DeleteGroupAsyncTask(@NonNull final GroupDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Group... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class InsertSceneAsyncTask extends AsyncTask<Scene, Void, Void> {

        private SceneDao mAsyncTaskDao;

        InsertSceneAsyncTask(@NonNull final SceneDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Scene... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateSceneKeyAsyncTask extends AsyncTask<Scene, Void, Void> {

        private final SceneDao mAsyncTaskDao;

        UpdateSceneKeyAsyncTask(@NonNull final SceneDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Scene... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class DeleteSceneKeyAsyncTask extends AsyncTask<Scene, Void, Void> {

        private final SceneDao mAsyncTaskDao;

        DeleteSceneKeyAsyncTask(@NonNull final SceneDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Scene... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateMeshNetwork(database);
            migrateNodes(database);
            migrateProvisioner(database);
            migrateGroup(database);
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateGroup2_3(database);
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateNodes3_4(database);
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateProvisioner4_5(database);
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateMeshNetwork5_6(database);
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            migrateKeyIndexes6_7(database);
        }
    };

    private static void migrateMeshNetwork(final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `mesh_network_temp` " +
                "(`mesh_uuid` TEXT NOT NULL, " +
                "`mesh_name` TEXT, " +
                "`timestamp` INTEGER NOT NULL, " +
                "`iv_index` INTEGER NOT NULL, " +
                "`iv_update_state` INTEGER NOT NULL, " +
                "`unicast_address` INTEGER NOT NULL DEFAULT 0x0001, " +
                "`last_selected` INTEGER NOT NULL, " +
                "PRIMARY KEY(`mesh_uuid`))");

        database.execSQL(
                "INSERT INTO mesh_network_temp (mesh_uuid, mesh_name, timestamp, iv_index, iv_update_state, last_selected) " +
                        "SELECT mesh_uuid, mesh_name, timestamp, iv_index, iv_update_state, last_selected FROM mesh_network");
        final Cursor cursor = database.query("SELECT * FROM mesh_network");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String uuid = cursor.getString(cursor.getColumnIndex("mesh_uuid"));
                final byte[] unicast = cursor.getBlob(cursor.getColumnIndex("unicast_address"));
                final int address = MeshAddress.addressBytesToInt(unicast);
                final ContentValues values = new ContentValues();
                values.put("unicast_address", address);
                database.update("mesh_network_temp", SQLiteDatabase.CONFLICT_REPLACE, values, "mesh_uuid = ?", new String[]{uuid});
            } while (cursor.moveToNext());
            cursor.close();
        }
        database.execSQL("DROP TABLE mesh_network");
        database.execSQL("ALTER TABLE mesh_network_temp RENAME TO mesh_network");
    }

    private static void migrateNodes(final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `nodes_temp` " +
                "(`timestamp` INTEGER NOT NULL, " +
                "`mAddedNetworkKeys` TEXT, " +
                "`name` TEXT, `ttl` INTEGER, " +
                "`blacklisted` INTEGER NOT NULL, " +
                "`secureNetworkBeacon` INTEGER, " +
                "`mesh_uuid` TEXT, `uuid` TEXT NOT NULL, " +
                "`security` INTEGER NOT NULL, " +
                "`unicast_address` INTEGER NOT NULL DEFAULT 1, " +
                "`configured` INTEGER NOT NULL, " +
                "`device_key` BLOB, " +
                "`seq_number` INTEGER NOT NULL, " +
                "`cid` INTEGER, " +
                "`pid` INTEGER, " +
                "`vid` INTEGER, " +
                "`crpl` INTEGER, " +
                "`mElements` TEXT, " +
                "`mAddedApplicationKeys` TEXT, " +
                "`networkTransmitCount` INTEGER, " +
                "`networkIntervalSteps` INTEGER, " +
                "`relayTransmitCount` INTEGER, " +
                "`relayIntervalSteps` INTEGER, " +
                "`friend` INTEGER, " +
                "`lowPower` INTEGER, " +
                "`proxy` INTEGER, " +
                "`relay` INTEGER, " +
                "PRIMARY KEY(`uuid`), " +
                "FOREIGN KEY(`mesh_uuid`) REFERENCES `mesh_network`(`mesh_uuid`) ON UPDATE CASCADE ON DELETE CASCADE )");

        database.execSQL(
                "INSERT INTO nodes_temp (timestamp, mAddedNetworkKeys, name, blacklisted, secureNetworkBeacon, mesh_uuid, " +
                        "security, configured, device_key, seq_number, cid, pid, vid, crpl, mElements, " +
                        "mAddedApplicationKeys, networkTransmitCount, networkIntervalSteps, relayTransmitCount, relayIntervalSteps, " +
                        "friend, lowPower, proxy, relay, uuid, mesh_uuid) " +
                        "SELECT timestamp, mAddedNetworkKeys, name, blacklisted, secureNetworkBeacon, mesh_uuid, " +
                        "security, configured, device_key, seq_number, cid, pid, vid, crpl, mElements, " +
                        "mAddedApplicationKeys, networkTransmitCount, networkIntervalSteps, relayTransmitCount, relayIntervalSteps," +
                        "friend, lowPower, proxy, relay, uuid, mesh_uuid FROM nodes");

        final Cursor cursor = database.query("SELECT * FROM nodes");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String uuid = cursor.getString(cursor.getColumnIndex("uuid"));
                final byte[] unicast = cursor.getBlob(cursor.getColumnIndex("unicast_address"));
                final int address = MeshAddress.addressBytesToInt(unicast);
                final ContentValues values = new ContentValues();
                values.put("unicast_address", address);
                database.update("nodes_temp", SQLiteDatabase.CONFLICT_REPLACE, values, "uuid = ?", new String[]{uuid});
            } while (cursor.moveToNext());
            cursor.close();
        }
        database.execSQL("DROP TABLE nodes");
        database.execSQL("ALTER TABLE nodes_temp RENAME TO nodes");
        database.execSQL("CREATE INDEX index_nodes_mesh_uuid ON `nodes` (mesh_uuid)");
    }

    private static void migrateProvisioner(final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `provisioner_temp` " +
                "(`mesh_uuid` TEXT NOT NULL, " +
                "`provisioner_uuid` TEXT NOT NULL, " +
                "`name` TEXT, " +
                "`allocatedGroupRanges` TEXT, " +
                "`allocatedUnicastRanges` TEXT, " +
                "`allocatedSceneRanges` TEXT, " +
                "`sequence_number` INTEGER NOT NULL, " +
                "`provisioner_address` INTEGER NOT NULL DEFAULT 32767," +
                "`global_ttl` INTEGER NOT NULL, " +
                "`last_selected` INTEGER NOT NULL, PRIMARY KEY(`provisioner_uuid`), " +
                "FOREIGN KEY(`mesh_uuid`) REFERENCES `mesh_network`(`mesh_uuid`) ON UPDATE CASCADE ON DELETE CASCADE )");

        database.execSQL(
                "INSERT INTO provisioner_temp (mesh_uuid, provisioner_uuid, name, allocatedGroupRanges, allocatedUnicastRanges, allocatedSceneRanges, sequence_number, global_ttl, last_selected) " +
                        "SELECT mesh_uuid, provisioner_uuid, name, allocatedGroupRanges, allocatedUnicastRanges, allocatedSceneRanges, sequence_number, global_ttl, last_selected FROM provisioner");

        final Cursor cursor = database.query("SELECT * FROM provisioner");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String uuid = cursor.getString(cursor.getColumnIndex("provisioner_uuid"));
                final byte[] unicast = cursor.getBlob(cursor.getColumnIndex("provisioner_address"));
                final int address = MeshAddress.addressBytesToInt(unicast);
                final ContentValues values = new ContentValues();
                values.put("provisioner_address", address);
                database.update("provisioner_temp", SQLiteDatabase.CONFLICT_REPLACE, values, "provisioner_uuid = ?", new String[]{uuid});
            } while (cursor.moveToNext());
            cursor.close();
        }
        database.execSQL("DROP TABLE provisioner");
        database.execSQL("ALTER TABLE provisioner_temp RENAME TO provisioner");
        database.execSQL("CREATE INDEX index_provisioner_mesh_uuid ON `provisioner` (mesh_uuid)");
    }

    private static void migrateGroup(final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `groups_temp` " +
                "(`id` INTEGER PRIMARY KEY NOT NULL," +
                "`mesh_uuid` TEXT, " +
                "`name` TEXT, " +
                "`group_address` INTEGER NOT NULL DEFAULT 49152, " +
                "`parent_address` INTEGER NOT NULL DEFAULT 49152, " +
                "FOREIGN KEY(`mesh_uuid`) REFERENCES `mesh_network`(`mesh_uuid`) ON UPDATE CASCADE ON DELETE CASCADE )");

        final Cursor cursor = database.query("SELECT * FROM groups");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String uuid = cursor.getString(cursor.getColumnIndex("mesh_uuid"));
                final String name = cursor.getString(cursor.getColumnIndex("name"));
                final byte[] grpAddress = cursor.getBlob(cursor.getColumnIndex("group_address"));
                final byte[] pAddress = cursor.getBlob(cursor.getColumnIndex("parent_address"));
                final int groupAddress = MeshParserUtils.unsignedBytesToInt(grpAddress[1], grpAddress[0]);
                final ContentValues values = new ContentValues();
                values.put("mesh_uuid", uuid);
                values.put("name", name);
                values.put("group_address", groupAddress);
                if (pAddress != null) {
                    final int parentAddress = MeshParserUtils.unsignedBytesToInt(pAddress[1], pAddress[0]);
                    values.put("parent_address", parentAddress);
                }
                database.insert("groups_temp", SQLiteDatabase.CONFLICT_REPLACE, values);
            } while (cursor.moveToNext());
            cursor.close();
        }

        database.execSQL("DROP TABLE groups");
        database.execSQL("ALTER TABLE groups_temp RENAME TO groups");
        database.execSQL("CREATE INDEX index_groups_mesh_uuid ON `groups` (mesh_uuid)");
    }

    private static void migrateGroup2_3(final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `groups_temp` " +
                "(`id` INTEGER PRIMARY KEY NOT NULL," +
                "`mesh_uuid` TEXT, " +
                "`name` TEXT, " +
                "`group_address` INTEGER NOT NULL DEFAULT 49152, " +
                "`parent_address` INTEGER NOT NULL DEFAULT 0, " +
                "`group_address_label` TEXT, " +
                "`parent_address_label` TEXT, " +
                "FOREIGN KEY(`mesh_uuid`) REFERENCES `mesh_network`(`mesh_uuid`) ON UPDATE CASCADE ON DELETE CASCADE )");

        database.execSQL(
                "INSERT INTO groups_temp (id, mesh_uuid, name, group_address, parent_address) " +
                        "SELECT id, mesh_uuid, name, group_address, parent_address FROM groups");

        database.execSQL("DROP TABLE groups");
        database.execSQL("ALTER TABLE groups_temp RENAME TO groups");
        database.execSQL("CREATE INDEX index_groups_mesh_uuid ON `groups` (mesh_uuid)");
    }

    private static void migrateNodes3_4(final SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `nodes_temp` " +
                "(`timestamp` INTEGER NOT NULL, " +
                "`name` TEXT, `ttl` INTEGER, " +
                "`blacklisted` INTEGER NOT NULL, " +
                "`secureNetworkBeacon` INTEGER, " +
                "`mesh_uuid` TEXT, `uuid` TEXT NOT NULL, " +
                "`security` INTEGER NOT NULL, " +
                "`unicast_address` INTEGER NOT NULL DEFAULT 1, " +
                "`configured` INTEGER NOT NULL, " +
                "`device_key` BLOB, " +
                "`seq_number` INTEGER NOT NULL, " +
                "`cid` INTEGER, " +
                "`pid` INTEGER, " +
                "`vid` INTEGER, " +
                "`crpl` INTEGER, " +
                "`mElements` TEXT, " +
                "`netKeys` TEXT, " +
                "`appKeys` TEXT, " +
                "`networkTransmitCount` INTEGER, " +
                "`networkIntervalSteps` INTEGER, " +
                "`relayTransmitCount` INTEGER, " +
                "`relayIntervalSteps` INTEGER, " +
                "`friend` INTEGER, " +
                "`lowPower` INTEGER, " +
                "`proxy` INTEGER, " +
                "`relay` INTEGER, " +
                "PRIMARY KEY(`uuid`), " +
                "FOREIGN KEY(`mesh_uuid`) REFERENCES `mesh_network`(`mesh_uuid`) ON UPDATE CASCADE ON DELETE CASCADE )");

        database.execSQL(
                "INSERT INTO nodes_temp (timestamp, name, blacklisted, secureNetworkBeacon, mesh_uuid, " +
                        "security, unicast_address, configured, device_key, seq_number, cid, pid, vid, crpl, mElements, " +
                        "networkTransmitCount, networkIntervalSteps, relayTransmitCount, relayIntervalSteps, " +
                        "friend, lowPower, proxy, relay, uuid, mesh_uuid) " +
                        "SELECT timestamp, name, blacklisted, secureNetworkBeacon, mesh_uuid, " +
                        "security, unicast_address, configured, device_key, seq_number, cid, pid, vid, crpl, mElements, " +
                        "networkTransmitCount, networkIntervalSteps, relayTransmitCount, relayIntervalSteps," +
                        "friend, lowPower, proxy, relay, uuid, mesh_uuid FROM nodes");

        final Cursor cursor = database.query("SELECT * FROM nodes");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final ContentValues values = new ContentValues();
                final String uuid = cursor.getString(cursor.getColumnIndex("uuid"));

                final String netKeysJson = cursor.getString(cursor.getColumnIndex("mAddedNetworkKeys"));
                final List<NetworkKey> netKeys = MeshTypeConverters.fromJsonToAddedNetKeys(netKeysJson);
                final List<Integer> keyIndexes = new ArrayList<>();
                for (NetworkKey networkKey : netKeys) {
                    if (networkKey != null) {
                        keyIndexes.add(networkKey.getKeyIndex());
                    }
                }
                values.put("netKeys", MeshTypeConverters.integerToJson(keyIndexes));

                keyIndexes.clear();
                final String appKeysJson = cursor.getString(cursor.getColumnIndex("mAddedApplicationKeys"));
                final Map<Integer, ApplicationKey> appKeyMap = MeshTypeConverters.fromJsonToAddedAppKeys(appKeysJson);
                for (Map.Entry<Integer, ApplicationKey> applicationKeyEntry : appKeyMap.entrySet()) {
                    final ApplicationKey key = applicationKeyEntry.getValue();
                    if (key != null) {
                        keyIndexes.add(key.getKeyIndex());
                    }
                }
                values.put("appKeys", MeshTypeConverters.integerToJson(keyIndexes));
                database.update("nodes_temp", SQLiteDatabase.CONFLICT_REPLACE, values, "uuid = ?", new String[]{uuid});
            } while (cursor.moveToNext());
            cursor.close();
        }
        database.execSQL("DROP TABLE nodes");
        database.execSQL("ALTER TABLE nodes_temp RENAME TO nodes");
        database.execSQL("CREATE INDEX index_nodes_mesh_uuid ON `nodes` (mesh_uuid)");
    }

    private static void migrateProvisioner4_5(final SupportSQLiteDatabase database) {
        final List<AllocatedUnicastRange> unicastRange = new ArrayList<>();
        final List<AllocatedGroupRange> groupRange = new ArrayList<>();
        final List<AllocatedSceneRange> sceneRange = new ArrayList<>();
        unicastRange.add(new AllocatedUnicastRange(0x0001, 0x199A));
        groupRange.add(new AllocatedGroupRange(0xC000, 0xCC9A));
        sceneRange.add(new AllocatedSceneRange(0x0001, 0x3333));

        database.execSQL("CREATE TABLE `provisioner_temp` " +
                "(`mesh_uuid` TEXT NOT NULL, " +
                "`provisioner_uuid` TEXT NOT NULL, " +
                "`name` TEXT, " +
                "`allocated_unicast_ranges` TEXT NOT NULL, " +
                "`allocated_group_ranges` TEXT NOT NULL, " +
                "`allocated_scene_ranges` TEXT NOT NULL, " +
                "`sequence_number` INTEGER NOT NULL, " +
                "`provisioner_address` INTEGER," +
                "`global_ttl` INTEGER NOT NULL, " +
                "`last_selected` INTEGER NOT NULL, PRIMARY KEY(`provisioner_uuid`), " +
                "FOREIGN KEY(`mesh_uuid`) REFERENCES `mesh_network`(`mesh_uuid`) ON UPDATE CASCADE ON DELETE CASCADE )");

        database.execSQL(
                "INSERT INTO provisioner_temp (mesh_uuid, provisioner_uuid, name, " +
                        "allocated_unicast_ranges, allocated_group_ranges, allocated_scene_ranges, " +
                        "sequence_number, global_ttl, last_selected) " +
                        "SELECT mesh_uuid, provisioner_uuid, name, " +
                        "allocatedUnicastRanges, allocatedGroupRanges, allocatedSceneRanges," +
                        "sequence_number, global_ttl, last_selected FROM provisioner");

        final List<Provisioner> provisioners = new ArrayList<>();
        Cursor cursor = database.query("SELECT * FROM provisioner");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String meshUuid = cursor.getString(cursor.getColumnIndex("mesh_uuid"));
                final String uuid = cursor.getString(cursor.getColumnIndex("provisioner_uuid"));
                final String name = cursor.getString(cursor.getColumnIndex("name"));
                final String unicastRanges = cursor.getString(cursor.getColumnIndex("allocatedUnicastRanges"));
                final String groupRanges = cursor.getString(cursor.getColumnIndex("allocatedGroupRanges"));
                final String sceneRanges = cursor.getString(cursor.getColumnIndex("allocatedSceneRanges"));
                final int sequenceNumber = cursor.getInt(cursor.getColumnIndex("sequence_number"));
                final int globalTtl = cursor.getInt(cursor.getColumnIndex("global_ttl"));
                final boolean lastSelected = cursor.getInt(cursor.getColumnIndex("last_selected")) == 1;
                final int unicast = cursor.getInt(cursor.getColumnIndex("provisioner_address"));
                final ContentValues values = new ContentValues();
                values.put("mesh_uuid", meshUuid);
                values.put("provisioner_uuid", uuid);
                values.put("name", name);
                values.put("sequence_number", sequenceNumber);
                values.put("global_ttl", globalTtl);
                values.put("last_selected", lastSelected);
                if (unicast == 0) {
                    final Integer t = null;
                    values.put("provisioner_address", t);
                } else {
                    values.put("provisioner_address", unicast);
                }
                values.put("allocated_unicast_ranges", unicastRanges.equalsIgnoreCase("null") ?
                        MeshTypeConverters.allocatedUnicastRangeToJson(unicastRange) : unicastRanges);
                values.put("allocated_group_ranges", groupRanges.equalsIgnoreCase("null") ?
                        MeshTypeConverters.allocatedGroupRangeToJson(groupRange) : groupRanges);
                values.put("allocated_scene_ranges", sceneRanges.equalsIgnoreCase("null") ?
                        MeshTypeConverters.allocatedSceneRangeToJson(sceneRange) : sceneRanges);
                database.update("provisioner_temp", SQLiteDatabase.CONFLICT_REPLACE, values, "provisioner_uuid = ?", new String[]{uuid});
                final Provisioner provisioner = new Provisioner(uuid,
                        unicastRanges.equalsIgnoreCase("null") ? unicastRange : MeshTypeConverters.fromJsonToAllocatedUnicastRanges(unicastRanges),
                        groupRanges.equalsIgnoreCase("null") ? groupRange : MeshTypeConverters.fromJsonToAllocatedGroupRanges(groupRanges),
                        sceneRanges.equalsIgnoreCase("null") ? sceneRange : MeshTypeConverters.fromJsonToAllocatedSceneRanges(sceneRanges),
                        meshUuid);
                provisioner.setProvisionerName(name);
                provisioner.setProvisionerAddress(unicast);
                provisioner.setSequenceNumber(sequenceNumber);
                provisioner.setLastSelected(lastSelected);
                provisioner.setGlobalTtl(globalTtl);
                provisioners.add(provisioner);
            } while (cursor.moveToNext());
            cursor.close();
        }

        database.execSQL("DROP TABLE provisioner");
        database.execSQL("ALTER TABLE provisioner_temp RENAME TO provisioner");
        database.execSQL("CREATE INDEX index_provisioner_mesh_uuid ON `provisioner` (mesh_uuid)");
        addProvisionerNodes(database, provisioners);
    }

    private static HashMap<UUID, ArrayList<Integer>> getKeyIndexes(@NonNull final SupportSQLiteDatabase database, final String tableName) {
        Cursor cursor = database.query("SELECT * FROM " + tableName);
        final HashMap<UUID, ArrayList<Integer>> netKeyIndexMap = new HashMap<>();
        if (cursor != null && cursor.moveToFirst()) {
            final UUID meshUuid = UUID.fromString(cursor.getString(cursor.getColumnIndex("mesh_uuid")).toUpperCase(Locale.US));
            do {
                final int index = cursor.getInt(cursor.getColumnIndex("index"));
                ArrayList<Integer> indexes = netKeyIndexMap.get(meshUuid);
                if (indexes != null) {
                    indexes.add(index);
                } else {
                    indexes = new ArrayList<>();
                    indexes.add(index);
                }
                netKeyIndexMap.put(meshUuid, indexes);
            } while (cursor.moveToNext());
        }
        return netKeyIndexMap;
    }

    private static List<NetworkKey> getNetKeys(@NonNull final SupportSQLiteDatabase database) {
        final List<NetworkKey> keys = new ArrayList<>();
        final Cursor cursor = database.query("SELECT * FROM network_key");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String meshUuid = cursor.getString(cursor.getColumnIndex("mesh_uuid")).toUpperCase(Locale.US);
                final int index = cursor.getInt(cursor.getColumnIndex("index"));
                final byte[] key = cursor.getBlob(cursor.getColumnIndex("key"));
                final NetworkKey networkKey = new NetworkKey(index, key);
                keys.add(networkKey);
            } while (cursor.moveToNext());
        }
        return keys;
    }

    private static List<ApplicationKey> getAppKeys(@NonNull final SupportSQLiteDatabase database) {
        final List<ApplicationKey> keys = new ArrayList<>();
        final Cursor cursor = database.query("SELECT * FROM application_key");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String meshUuid = cursor.getString(cursor.getColumnIndex("mesh_uuid")).toUpperCase(Locale.US);
                final int index = cursor.getInt(cursor.getColumnIndex("index"));
                final byte[] key = cursor.getBlob(cursor.getColumnIndex("key"));
                final ApplicationKey applicationKey = new ApplicationKey(index, key);
                keys.add(applicationKey);
            } while (cursor.moveToNext());
        }
        return keys;
    }

    private static void addProvisionerNodes(@NonNull final SupportSQLiteDatabase database, @NonNull List<Provisioner> provisioners) {
        if (!provisioners.isEmpty()) {
            final List<NetworkKey> netKeys = getNetKeys(database);
            final List<ApplicationKey> appKeys = getAppKeys(database);
            final List<ProvisionedMeshNode> nodes = new ArrayList<>();
            for (Provisioner provisioner : provisioners) {
                final ProvisionedMeshNode node = new ProvisionedMeshNode(provisioner, netKeys, appKeys);
                final ContentValues values = new ContentValues();
                values.put("timestamp", node.getTimeStamp());
                values.put("name", node.getNodeName());
                values.put("mesh_uuid", node.getMeshUuid());
                values.put("uuid", node.getUuid());
                values.put("ttl", node.getTtl());
                values.put("blacklisted", node.isBlackListed());
                values.put("security", node.getSecurity());
                values.put("unicast_address", node.getUnicastAddress());
                values.put("configured", node.isConfigured());
                values.put("device_key", node.getDeviceKey());
                values.put("seq_number", node.getSequenceNumber());
                values.put("mElements", MeshTypeConverters.elementsToJson(node.getElements()));
                final List<Integer> networkKeys = new ArrayList<>();
                for (NetworkKey networkKey : netKeys) {
                    networkKeys.add(networkKey.getKeyIndex());
                }
                final List<Integer> applicationKeys = new ArrayList<>();
                for (ApplicationKey applicationKey : appKeys) {
                    applicationKeys.add(applicationKey.getKeyIndex());
                }
                if (!netKeys.isEmpty()) {
                    values.put("netKeys", MeshTypeConverters.integerToJson(networkKeys));
                }
                if (!appKeys.isEmpty()) {
                    values.put("appKeys", MeshTypeConverters.integerToJson(applicationKeys));
                }
                database.insert("nodes", SQLiteDatabase.CONFLICT_REPLACE, values);
            }
        }
    }

    private static void migrateMeshNetwork5_6(final SupportSQLiteDatabase database) {

        final HashMap<UUID, SparseIntArray> nodesMap = new HashMap<>();
        final Cursor cursor1 = database.query("SELECT mesh_uuid, unicast_address, seq_number FROM nodes");
        if (cursor1 != null && cursor1.moveToFirst()) {
            final UUID meshUuid = UUID.fromString(cursor1.getString(cursor1.getColumnIndex("mesh_uuid")).toUpperCase(Locale.US));
            do {
                final int unicast = cursor1.getInt(cursor1.getColumnIndex("unicast_address"));
                final int seqNumber = cursor1.getInt(cursor1.getColumnIndex("seq_number"));
                SparseIntArray sparseIntArray = nodesMap.get(meshUuid);
                if (sparseIntArray != null) {
                    sparseIntArray.put(unicast, seqNumber);
                } else {
                    sparseIntArray = new SparseIntArray();
                    sparseIntArray.put(unicast, seqNumber);
                }
                nodesMap.put(meshUuid, sparseIntArray);
            } while (cursor1.moveToNext());
            cursor1.close();
        }

        database.execSQL("ALTER TABLE mesh_network RENAME TO mesh_network_temp");
        database.execSQL("CREATE TABLE `mesh_network` " +
                "(`mesh_uuid` TEXT NOT NULL, " +
                "`mesh_name` TEXT, " +
                "`timestamp` INTEGER NOT NULL, " +
                "`iv_index` INTEGER NOT NULL, " +
                "`iv_update_state` INTEGER NOT NULL, " +
                "`sequence_numbers` TEXT NOT NULL, " +
                "`last_selected` INTEGER NOT NULL, " +
                "PRIMARY KEY(`mesh_uuid`))");
        final Cursor cursor = database.query("SELECT * FROM mesh_network_temp");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                final String meshUuid = cursor.getString(cursor.getColumnIndex("mesh_uuid")).toUpperCase(Locale.US);
                final String meshName = cursor.getString(cursor.getColumnIndex("mesh_name"));
                final long timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
                final int ivIndex = cursor.getInt(cursor.getColumnIndex("iv_index"));
                final int ivUpdateState = cursor.getInt(cursor.getColumnIndex("iv_update_state"));
                final boolean lastSelected = cursor.getInt(cursor.getColumnIndex("last_selected")) == 1;
                final SparseIntArray sequenceNumbersArray = nodesMap.get(UUID.fromString(meshUuid));
                final ContentValues values = new ContentValues();
                values.put("mesh_uuid", meshUuid);
                values.put("mesh_name", meshName);
                values.put("timestamp", timestamp);
                values.put("iv_index", ivIndex);
                values.put("iv_update_state", ivUpdateState);
                if (sequenceNumbersArray != null) {
                    values.put("sequence_numbers", MeshTypeConverters.sparseIntArrayToJson(sequenceNumbersArray));
                }
                values.put("last_selected", lastSelected);
                database.insert("mesh_network", SQLiteDatabase.CONFLICT_REPLACE, values);
            } while (cursor.moveToNext());
            cursor.close();
        }
        database.execSQL("DROP TABLE mesh_network_temp");
    }

    private static void migrateKeyIndexes6_7(@NonNull final SupportSQLiteDatabase database) {
        final Cursor cursor = database.query("SELECT uuid, netKeys, appKeys FROM nodes");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    final ContentValues values = new ContentValues();
                    final String uuid = cursor.getString(cursor.getColumnIndex("uuid"));
                    final String netKeysJson = cursor.getString(cursor.getColumnIndex("netKeys"));
                    final List<Integer> netKeys = MeshTypeConverters.fromJsonToIntegerList(netKeysJson);
                    final List<NodeKey> netKeyIndexes = new ArrayList<>();
                    for (Integer keyIndex : netKeys) {
                        if (keyIndex != null) {
                            netKeyIndexes.add(new NodeKey(keyIndex, false));
                        }
                    }
                    values.put("netKeys", MeshTypeConverters.nodeKeysToJson(netKeyIndexes));

                    final List<NodeKey> appKeyIndexes = new ArrayList<>();
                    final String appKeysJson = cursor.getString(cursor.getColumnIndex("appKeys"));
                    final List<Integer> appKeys = MeshTypeConverters.fromJsonToIntegerList(appKeysJson);
                    for (Integer keyIndex : appKeys) {
                        appKeyIndexes.add(new NodeKey(keyIndex, false));
                    }
                    values.put("appKeys", MeshTypeConverters.nodeKeysToJson(appKeyIndexes));
                    database.update("nodes", SQLiteDatabase.CONFLICT_REPLACE, values, "uuid = ?", new String[]{uuid});
                } catch (Exception ex) {
                    Log.v(TAG, "Something went wrong while migrating data");
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}
