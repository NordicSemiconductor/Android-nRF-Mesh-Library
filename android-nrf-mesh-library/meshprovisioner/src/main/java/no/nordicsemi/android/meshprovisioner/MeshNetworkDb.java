package no.nordicsemi.android.meshprovisioner;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.meshprovisioner.data.ApplicationKeyDao;
import no.nordicsemi.android.meshprovisioner.data.GroupDao;
import no.nordicsemi.android.meshprovisioner.data.GroupsDao;
import no.nordicsemi.android.meshprovisioner.data.MeshNetworkDao;
import no.nordicsemi.android.meshprovisioner.data.NetworkKeyDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionedMeshNodeDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionedMeshNodesDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionerDao;
import no.nordicsemi.android.meshprovisioner.data.SceneDao;
import no.nordicsemi.android.meshprovisioner.data.ScenesDao;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

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
        version = 1)
abstract class MeshNetworkDb extends RoomDatabase {

    abstract MeshNetworkDao meshNetworkDao();

    abstract NetworkKeyDao networkKeyDao();

    abstract ApplicationKeyDao applicationKeyDao();

    abstract ProvisionerDao provisionerDao();

    abstract ProvisionedMeshNodesDao provisionedMeshNodesDao();

    abstract ProvisionedMeshNodeDao provisionedMeshNodeDao();

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

    void insertNetwork(final MeshNetworkDao dao,
                       final NetworkKeyDao netKeyDao,
                       final ApplicationKeyDao appKeyDao,
                       final ProvisionerDao provisionerDao,
                       final ProvisionedMeshNodeDao nodeDao,
                       final GroupDao groupDao,
                       final SceneDao sceneDao,
                       final MeshNetwork meshNetwork) {
        new InsertNetworkAsyncTask(dao,
                netKeyDao,
                appKeyDao,
                provisionerDao,
                nodeDao,
                groupDao,
                sceneDao,
                meshNetwork).execute();
    }

    void loadNetwork(final MeshNetworkDao dao,
                       final NetworkKeyDao netKeyDao,
                       final ApplicationKeyDao appKeyDao,
                       final ProvisionerDao provisionerDao,
                       final ProvisionedMeshNodeDao nodeDao,
                       final GroupDao groupDao,
                       final SceneDao sceneDao,
                       final LoadNetworkCallbacks listener) {
        new LoadNetworkAsyncTask(dao,
                netKeyDao,
                appKeyDao,
                provisionerDao,
                nodeDao,
                groupDao,
                sceneDao,
                listener).execute();
    }

    void updateNetwork(final MeshNetworkDao dao, final MeshNetwork meshNetwork) {
        new UpdateNetworkAsyncTask(dao).execute(meshNetwork);
    }

    void deleteNetwork(final MeshNetworkDao dao, final MeshNetwork meshNetwork) {
        new DeleteNetworkAsyncTask(dao).execute(meshNetwork);
    }

    void insertNetKey(final NetworkKeyDao dao, final NetworkKey networkKey) {
        new InsertNetKeyAsyncTask(dao).execute(networkKey);
    }

    void updateNetKey(final NetworkKeyDao dao, final NetworkKey networkKey) {
        new UpdateNetKeyAsyncTask(dao).execute(networkKey);
    }

    void deleteNetKey(final NetworkKeyDao dao, final NetworkKey networkKey) {
        new DeleteNetKeyAsyncTask(dao).execute(networkKey);
    }

    void insertAppKey(final ApplicationKeyDao dao, final ApplicationKey applicationKey) {
        new InsertAppKeyAsyncTask(dao).execute(applicationKey);
    }

    void updateAppKey(final ApplicationKeyDao dao, final ApplicationKey applicationKey) {
        new UpdateAppKeyAsyncTask(dao).execute(applicationKey);
    }

    void deleteAppKey(final ApplicationKeyDao dao, final ApplicationKey applicationKey) {
        new DeleteAppKeyAsyncTask(dao).execute(applicationKey);
    }

    void insertProvisioner(final ProvisionerDao dao, final Provisioner provisioner) {
        new InsertProvisionerAsyncTask(dao).execute();
    }

    void updateProvisioner(final ProvisionerDao dao, final Provisioner provisioner) {
        new UpdateProvisionerAsyncTask(dao).execute(provisioner);
    }

    void updateProvisioner(final ProvisionerDao dao, final List<Provisioner> provisioners) {
        new UpdateProvisionersAsyncTask(dao, provisioners).execute();
    }

    void deleteProvisioner(final ProvisionerDao dao, final Provisioner provisioner) {
        new DeleteProvisionerAsyncTask(dao).execute(provisioner);
    }

    void insertNode(final ProvisionedMeshNodeDao dao, final ProvisionedMeshNode node) {
        new InsertNodeAsyncTask(dao).execute(node);
    }

    void updateNode(final ProvisionedMeshNodeDao dao, final ProvisionedMeshNode node) {
        new UpdateNodeAsyncTask(dao).execute(node);
    }

    void updateNodes(final ProvisionedMeshNodeDao dao, final List<ProvisionedMeshNode> nodes) {
        new UpdateNodesAsyncTask(dao, nodes).execute();
    }

    void deleteNode(final ProvisionedMeshNodeDao dao, final ProvisionedMeshNode node) {
        new DeleteNodeAsyncTask(dao).execute(node);
    }

    void insertGroup(final GroupDao dao, final Group group) {
        new InsertGroupAsyncTask(dao).execute(group);
    }

    void updateGroup(final GroupDao dao, final Group group) {
        new UpdateGroupAsyncTask(dao).execute(group);
    }

    void deleteGroup(final GroupDao dao, final Group group) {
        new DeleteGroupAsyncTask(dao).execute(group);
    }

    void insertScene(final SceneDao dao, final Scene scene) {
        new InsertSceneAsyncTask(dao).execute(scene);
    }

    void updateScene(final SceneDao dao, final Scene scene) {
        new UpdateSceneKeyAsyncTask(dao).execute(scene);
    }

    void deleteScene(final SceneDao dao, final Scene scene) {
        new DeleteSceneKeyAsyncTask(dao).execute(scene);
    }

    private static class InsertNetworkAsyncTask extends AsyncTask<Void, Void, Void> {

        private final MeshNetwork meshNetwork;
        private final MeshNetworkDao meshNetworkDao;
        private final NetworkKeyDao netKeyDao;
        private final ApplicationKeyDao appKeyDao;
        private final ProvisionerDao provisionerDao;
        private final ProvisionedMeshNodeDao nodeDao;
        private final GroupDao groupDao;
        private final SceneDao sceneDao;

        InsertNetworkAsyncTask(final MeshNetworkDao meshNetworkDao,
                               final NetworkKeyDao netKeyDao,
                               final ApplicationKeyDao appKeyDao,
                               final ProvisionerDao provisionerDao,
                               final ProvisionedMeshNodeDao nodeDao,
                               final GroupDao groupDao,
                               final SceneDao sceneDao,
                               final MeshNetwork meshNetwork) {
            this.meshNetworkDao = meshNetworkDao;
            this.netKeyDao = netKeyDao;
            this.appKeyDao = appKeyDao;
            this.provisionerDao = provisionerDao;
            this.nodeDao = nodeDao;
            this.groupDao = groupDao;
            this.sceneDao = sceneDao;
            this.meshNetwork = meshNetwork;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            meshNetworkDao.insert(meshNetwork);
            netKeyDao.insert(meshNetwork.netKeys);
            appKeyDao.insert(meshNetwork.appKeys);
            provisionerDao.insert(meshNetwork.provisioners);
            if(!meshNetwork.nodes.isEmpty()) {
                nodeDao.insert(meshNetwork.nodes);
            }

            if(meshNetwork.groups != null) {
                groupDao.insert(meshNetwork.groups);
            }

            if(meshNetwork.scenes != null) {
                sceneDao.insert(meshNetwork.scenes);
            }
            return null;
        }
    }

    private static class LoadNetworkAsyncTask extends AsyncTask<Void, Void, MeshNetwork> {

        private final LoadNetworkCallbacks listener;
        private final MeshNetworkDao meshNetworkDao;
        private final NetworkKeyDao netKeyDao;
        private final ApplicationKeyDao appKeyDao;
        private final ProvisionerDao provisionerDao;
        private final ProvisionedMeshNodeDao nodeDao;
        private final GroupDao groupDao;
        private final SceneDao sceneDao;

        LoadNetworkAsyncTask(final MeshNetworkDao meshNetworkDao,
                             final NetworkKeyDao netKeyDao,
                             final ApplicationKeyDao appKeyDao,
                             final ProvisionerDao provisionerDao,
                             final ProvisionedMeshNodeDao nodeDao,
                             final GroupDao groupDao,
                             final SceneDao sceneDao,
                             final LoadNetworkCallbacks listener) {
            this.meshNetworkDao = meshNetworkDao;
            this.netKeyDao = netKeyDao;
            this.appKeyDao = appKeyDao;
            this.provisionerDao = provisionerDao;
            this.nodeDao = nodeDao;
            this.groupDao = groupDao;
            this.sceneDao = sceneDao;
            this.listener = listener;
        }

        @Override
        protected MeshNetwork doInBackground(final Void... params) {
            final MeshNetwork meshNetwork = meshNetworkDao.getMeshNetwork(true);
            if(meshNetwork != null) {
                meshNetwork.netKeys = netKeyDao.loadNetworkKeys(meshNetwork.getMeshUUID());
                meshNetwork.appKeys = appKeyDao.loadApplicationKeys(meshNetwork.getMeshUUID());
                meshNetwork.nodes = nodeDao.getNodes(meshNetwork.getMeshUUID());
                final ArrayList<Provisioner> provisioners = new ArrayList<>();
                provisioners.add(provisionerDao.getProvisioner(meshNetwork.getMeshUUID(), true));
                meshNetwork.provisioners = provisioners;
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

        UpdateNetworkAsyncTask(MeshNetworkDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final MeshNetwork... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class DeleteNetworkAsyncTask extends AsyncTask<MeshNetwork, Void, Void> {

        private MeshNetworkDao mAsyncTaskDao;

        DeleteNetworkAsyncTask(MeshNetworkDao dao) {
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

        InsertNetKeyAsyncTask(NetworkKeyDao dao) {
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

        UpdateNetKeyAsyncTask(NetworkKeyDao dao) {
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

        DeleteNetKeyAsyncTask(NetworkKeyDao dao) {
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

        InsertAppKeyAsyncTask(ApplicationKeyDao dao) {
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

        UpdateAppKeyAsyncTask(ApplicationKeyDao dao) {
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

        DeleteAppKeyAsyncTask(ApplicationKeyDao dao) {
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

        InsertProvisionerAsyncTask(final ProvisionerDao dao) {
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

        UpdateProvisionerAsyncTask(final ProvisionerDao dao) {
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

        UpdateProvisionersAsyncTask(final ProvisionerDao dao, final List<Provisioner> provisioners) {
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

        DeleteProvisionerAsyncTask(final ProvisionerDao dao) {
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

        InsertNodeAsyncTask(ProvisionedMeshNodeDao dao) {
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

        UpdateNodeAsyncTask(ProvisionedMeshNodeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ProvisionedMeshNode... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class UpdateNodesAsyncTask extends AsyncTask<Void, Void, Void> {

        private ProvisionedMeshNodeDao mAsyncTaskDao;
        private List<ProvisionedMeshNode> nodes;

        UpdateNodesAsyncTask(ProvisionedMeshNodeDao dao, final List<ProvisionedMeshNode> nodes) {
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

        DeleteNodeAsyncTask(ProvisionedMeshNodeDao dao) {
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

        InsertGroupAsyncTask(final GroupDao dao) {
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

        UpdateGroupAsyncTask(final GroupDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Group... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class DeleteGroupAsyncTask extends AsyncTask<Group, Void, Void> {

        private GroupDao mAsyncTaskDao;

        DeleteGroupAsyncTask(final GroupDao dao) {
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

        InsertSceneAsyncTask(final SceneDao dao) {
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

        UpdateSceneKeyAsyncTask(final SceneDao dao) {
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

        DeleteSceneKeyAsyncTask(final SceneDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Scene... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }
}
