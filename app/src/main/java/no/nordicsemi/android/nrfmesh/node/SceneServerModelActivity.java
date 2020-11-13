package no.nordicsemi.android.nrfmesh.node;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import no.nordicsemi.android.mesh.models.SigModelParser;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.SceneRegisterStatus;
import no.nordicsemi.android.mesh.transport.SceneStatus;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.scenes.adapter.StoredScenesAdapter;

public abstract class SceneServerModelActivity extends ModelConfigurationActivity {

    private static final String TAG = SceneServerModelActivity.class.getSimpleName();
    protected MeshModel model;
    protected StoredScenesAdapter mScenesAdapter;

    protected abstract void updateScenesUi();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = mViewModel.getSelectedModel().getValue();
        if (model != null && model.getModelId() == SigModelParser.SCENE_SERVER) {
            mSwipe.setOnRefreshListener(this);
            mContainerPublication.setVisibility(View.GONE);
            final ConstraintLayout container = findViewById(R.id.node_controls_container);
            final View layoutSceneServer = LayoutInflater.from(this).inflate(R.layout.layout_scene_setup_server, container);

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
        if (meshMessage instanceof SceneStatus) {
            //TODO
        } else if (meshMessage instanceof SceneRegisterStatus) {
            final SceneRegisterStatus status = (SceneRegisterStatus) meshMessage;
            mViewModel.removeMessage();
            if (status.isSuccessful()) {
                handleStatuses();
                updateScenesUi();
            } else {
                displayStatusDialogFragment(getString(R.string.title_sig_model_subscription_list), status.getStatusMessage());
            }
        }
        hideProgressBar();
    }
}
