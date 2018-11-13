package no.nordicsemi.android.meshprovisioner;

import android.os.AsyncTask;

import no.nordicsemi.android.meshprovisioner.data.ApplicationKeyDao;
import no.nordicsemi.android.meshprovisioner.data.GroupDao;
import no.nordicsemi.android.meshprovisioner.data.MeshNetworkDao;
import no.nordicsemi.android.meshprovisioner.data.NetworkKeyDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionedMeshNodeDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionerDao;
import no.nordicsemi.android.meshprovisioner.data.SceneDao;

class InsertNetworkAsycTask extends AsyncTask<MeshNetwork, Void, Void> {

    private MeshNetworkDao mMeshNetworkDao;
    private NetworkKeyDao mNetworkKeyDao;
    private ApplicationKeyDao mApplicationKeyDao;
    private ProvisionerDao mProvisionerDao;
    private ProvisionedMeshNodeDao mProvisionedNodeDao;
    private GroupDao mGroupDao;
    private SceneDao mSceneDao;

    InsertNetworkAsycTask(final MeshNetworkDao networkDao,
                          final NetworkKeyDao networkKeyDao,
                          final ApplicationKeyDao applicationKeyDao,
                          final ProvisionedMeshNodeDao provisionedMeshNodeDao,
                          final ProvisionerDao provisionerDao,
                          final GroupDao groupDao,
                          final SceneDao sceneDao) {
        mMeshNetworkDao = networkDao;
        mNetworkKeyDao = networkKeyDao;
        mApplicationKeyDao = applicationKeyDao;
        mProvisionedNodeDao = provisionedMeshNodeDao;
        mProvisionerDao = provisionerDao;
        mGroupDao = groupDao;
        mSceneDao = sceneDao;
    }

    @Override
    protected Void doInBackground(final MeshNetwork... meshNetworks) {
        final MeshNetwork meshNetwork = meshNetworks[0];
        mMeshNetworkDao.insert(meshNetwork);
        mNetworkKeyDao.insert(meshNetwork.netKeys);
        mApplicationKeyDao.insert(meshNetwork.appKeys);

        for (Provisioner provisioner : meshNetwork.provisioners) {
            mProvisionerDao.insert(provisioner);
        }

        mProvisionedNodeDao.insert(meshNetwork.nodes);

        if (meshNetwork.groups != null) {
            mGroupDao.insert(meshNetwork.groups);
        }

        if (meshNetwork.scenes != null) {
            mSceneDao.insert(meshNetwork.scenes);
        }
        return null;
    }
}
