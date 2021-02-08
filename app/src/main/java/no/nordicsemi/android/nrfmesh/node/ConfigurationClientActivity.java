package no.nordicsemi.android.nrfmesh.node;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.models.ConfigurationClientModel;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;


@AndroidEntryPoint
public class ConfigurationClientActivity extends BaseModelConfigurationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof ConfigurationClientModel) {
            disableClickableViews();
        }
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        // DO nothing
    }
}
