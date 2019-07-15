package no.nordicsemi.android.nrfmeshprovisioner.node;

import android.os.Bundle;

import no.nordicsemi.android.meshprovisioner.models.ConfigurationClientModel;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;


public class ConfigurationClientActivity extends BaseModelConfigurationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof ConfigurationClientModel) {
            disableClickableViews();
        }
    }
}
