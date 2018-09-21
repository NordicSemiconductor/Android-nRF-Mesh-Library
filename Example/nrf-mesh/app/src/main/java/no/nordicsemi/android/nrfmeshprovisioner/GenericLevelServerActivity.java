package no.nordicsemi.android.nrfmeshprovisioner;

import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.models.GenericOnOffServerModel;

public class GenericLevelServerActivity extends BaseModelConfigurationActivity {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Override
    protected final void addControlsUi(final MeshModel model) {
        if(model instanceof GenericOnOffServerModel){

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
