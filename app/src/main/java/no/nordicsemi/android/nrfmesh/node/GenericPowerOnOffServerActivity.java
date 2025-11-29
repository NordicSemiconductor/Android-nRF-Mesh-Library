package no.nordicsemi.android.nrfmesh.node;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.data.OnPowerUpState;
import no.nordicsemi.android.mesh.models.GenericPowerOnOffServer;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.GenericOnPowerUpGet;
import no.nordicsemi.android.mesh.transport.GenericOnPowerUpSet;
import no.nordicsemi.android.mesh.transport.GenericOnPowerUpStatus;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutGenericPowerOnOffBinding;

@AndroidEntryPoint
public class GenericPowerOnOffServerActivity extends ModelConfigurationActivity {

    private static final String TAG = GenericPowerOnOffServerActivity.class.getSimpleName();

    private TextView mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof GenericPowerOnOffServer) {
            final LayoutGenericPowerOnOffBinding nodeControlsContainer = LayoutGenericPowerOnOffBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);

            mActionRead = nodeControlsContainer.actionRead;
            mActionRead.setOnClickListener(v -> sendGenericOnPowerUpGet());

            mState = nodeControlsContainer.genericPowerOnOffState;

            mViewModel.getSelectedModel().observe(this, meshModel -> {
                if (meshModel != null) {
                    updateAppStatusUi(meshModel);
                    updatePublicationUi(meshModel);
                    updateSubscriptionUi(meshModel);
                }
            });
        }
    }

    @Override
    protected void enableClickableViews() {
        super.enableClickableViews();
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        mSwipe.setOnRefreshListener(this);

        if (meshMessage instanceof GenericOnPowerUpStatus) {
            final GenericOnPowerUpStatus status = (GenericOnPowerUpStatus) meshMessage;
            final OnPowerUpState presentState = status.getOnPowerUpState();

            if (presentState.getValue() == 0x0) {
                mState.setText(R.string.generic_power_on_off_power_up_off);
            } else if (presentState.getValue() == 0x1) {
                mState.setText(R.string.generic_power_on_off_power_up_on);
            } else if (presentState.getValue() == 0x2) {
                mState.setText(R.string.generic_power_on_off_power_up_restore);
            } else {
                Log.w(TAG, "GenericOnPowerUpStatus unknown. Value was: " + presentState.getValue());
                mState.setText(R.string.generic_power_on_off_power_up_restore);
            }
        }
        hideProgressBar();
    }

    /**
     * Send generic on off get to mesh node
     */
    public void sendGenericOnPowerUpGet() {
        if (!checkConnectivity(mContainer)) return;
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            final MeshModel model = mViewModel.getSelectedModel().getValue();
            if (model != null) {
                if (!model.getBoundAppKeyIndexes().isEmpty()) {
                    final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                    final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);

                    final int address = element.getElementAddress();
                    final GenericOnPowerUpGet genericOnPowerUpGet = new GenericOnPowerUpGet(appKey);
                    sendAcknowledgedMessage(address, genericOnPowerUpGet);
                } else {
                    mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                }
            }
        }
    }

    /**
     * Send generic OnPowerUp Set to mesh node
     *
     * @param state OnPowerUpState
     */
    public void sendGenericOnPowerUpSet(final OnPowerUpState state) {
        if (!checkConnectivity(mContainer)) return;
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        if (node != null) {
            final Element element = mViewModel.getSelectedElement().getValue();
            if (element != null) {
                final MeshModel model = mViewModel.getSelectedModel().getValue();
                if (model != null) {
                    if (!model.getBoundAppKeyIndexes().isEmpty()) {
                        final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                        final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);
                        final int address = element.getElementAddress();
                        final GenericOnPowerUpSet genericOnPowerUpSet = new GenericOnPowerUpSet(appKey, state);
                        sendAcknowledgedMessage(address, genericOnPowerUpSet);
                    } else {
                        mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
                    }
                }
            }
        }
    }
}
