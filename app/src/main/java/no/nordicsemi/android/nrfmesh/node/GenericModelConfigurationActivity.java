package no.nordicsemi.android.nrfmesh.node;

import android.os.Bundle;

import no.nordicsemi.android.mesh.transport.MeshMessage;


public class GenericModelConfigurationActivity extends BaseModelConfigurationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        // DO nothing
        hideProgressBar();
    }
}
