package no.nordicsemi.android.nrfmesh.node;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.SensorCadenceStatus;
import no.nordicsemi.android.mesh.transport.SensorDescriptorGet;
import no.nordicsemi.android.mesh.transport.SensorDescriptorStatus;
import no.nordicsemi.android.mesh.transport.SensorGet;
import no.nordicsemi.android.mesh.transport.SensorStatus;

@AndroidEntryPoint
public class GenericModelConfigurationActivity extends ModelConfigurationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        mViewModel.getSelectedModel().observe(this, model -> {
            if (model != null) {
                updateAppStatusUi(model);
                updatePublicationUi(model);
                updateSubscriptionUi(model);
            }
        });
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        if (meshMessage instanceof SensorDescriptorStatus) {
            mViewModel.removeMessage();
            handleStatuses();
        } else if (meshMessage instanceof SensorCadenceStatus) {
            mViewModel.removeMessage();
            handleStatuses();
        } else if (meshMessage instanceof SensorStatus) {
            mViewModel.removeMessage();
            handleStatuses();
        }
        hideProgressBar();
    }

    @Override
    public void onRefresh() {
        final ApplicationKey applicationKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKeys().get(0);
        mViewModel.getMessageQueue().add(new SensorDescriptorGet(applicationKey, null));
        //mViewModel.getMessageQueue().add(new SensorCadenceGet(applicationKey, MOTION_SENSED));
        mViewModel.getMessageQueue().add(new SensorGet(applicationKey, null));
        sendMessage(mViewModel.getMessageQueue().peek());
    }
}
