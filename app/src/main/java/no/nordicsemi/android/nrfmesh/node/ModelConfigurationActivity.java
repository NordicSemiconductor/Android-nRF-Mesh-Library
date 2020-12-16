package no.nordicsemi.android.nrfmesh.node;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.transport.ConfigModelAppStatus;
import no.nordicsemi.android.mesh.transport.ConfigModelPublicationStatus;
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionStatus;
import no.nordicsemi.android.mesh.transport.ConfigSigModelAppList;
import no.nordicsemi.android.mesh.transport.ConfigSigModelSubscriptionList;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.nrfmesh.R;

@AndroidEntryPoint
public abstract class ModelConfigurationActivity extends BaseModelConfigurationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        if (meshMessage instanceof ConfigModelAppStatus) {
            final ConfigModelAppStatus status = (ConfigModelAppStatus) meshMessage;
            if (status.isSuccessful()) {
                mViewModel.displaySnackBar(this, mContainer, getString(R.string.operation_success), Snackbar.LENGTH_SHORT);
            } else {
                displayStatusDialogFragment(getString(R.string.title_appkey_status), status.getStatusCodeName());
            }
        } else if (meshMessage instanceof ConfigSigModelAppList) {
            final ConfigSigModelAppList status = (ConfigSigModelAppList) meshMessage;
            mViewModel.removeMessage();
            if (status.isSuccessful()) {
                handleStatuses();
            } else {
                displayStatusDialogFragment(getString(R.string.title_sig_model_subscription_list), status.getStatusCodeName());
            }
        } else if (meshMessage instanceof ConfigModelPublicationStatus) {
            final ConfigModelPublicationStatus status = (ConfigModelPublicationStatus) meshMessage;
            mViewModel.removeMessage();
            if (status.isSuccessful()) {
                handleStatuses();
            } else {
                displayStatusDialogFragment(getString(R.string.title_publication_status), status.getStatusCodeName());
            }
        } else if (meshMessage instanceof ConfigModelSubscriptionStatus) {
            final ConfigModelSubscriptionStatus status = (ConfigModelSubscriptionStatus) meshMessage;
            mViewModel.removeMessage();
            if (status.isSuccessful()) {
                handleStatuses();
            } else {
                displayStatusDialogFragment(getString(R.string.title_subscription_status), status.getStatusCodeName());
            }
        } else if (meshMessage instanceof ConfigSigModelSubscriptionList) {
            final ConfigSigModelSubscriptionList status = (ConfigSigModelSubscriptionList) meshMessage;
            mViewModel.removeMessage();
            if (status.isSuccessful()) {
                handleStatuses();
            } else {
                displayStatusDialogFragment(getString(R.string.title_sig_model_subscription_list), status.getStatusCodeName());
            }
        }
    }
}
