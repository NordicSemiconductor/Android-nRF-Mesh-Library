package no.nordicsemi.android.nrfmesh.node;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.Scene;
import no.nordicsemi.android.mesh.models.SceneServer;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.SceneDelete;
import no.nordicsemi.android.mesh.transport.SceneStore;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutSceneSetupServerBinding;
import no.nordicsemi.android.nrfmesh.scenes.ScenesActivity;
import no.nordicsemi.android.nrfmesh.scenes.adapter.SceneUiState;
import no.nordicsemi.android.nrfmesh.scenes.adapter.StoredScenesAdapter;
import no.nordicsemi.android.nrfmesh.viewmodels.ModelConfigurationViewModel;
import no.nordicsemi.android.nrfmesh.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static no.nordicsemi.android.mesh.models.SigModelParser.SCENE_SERVER;
import static no.nordicsemi.android.mesh.models.SigModelParser.SCENE_SETUP_SERVER;
import static no.nordicsemi.android.nrfmesh.utils.Utils.EXTRA_DATA;
import static no.nordicsemi.android.nrfmesh.utils.Utils.SELECT_SCENE;

@AndroidEntryPoint
public class SceneSetupServerModelActivity extends SceneServerModelActivity {

    private LayoutSceneSetupServerBinding layoutSceneSetupServerBinding;

    private final ActivityResultLauncher<Intent> sceneSelector = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    sendSceneStore(result.getData().getParcelableExtra(EXTRA_DATA));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null && model.getModelId() == SCENE_SETUP_SERVER) {
            mSwipe.setOnRefreshListener(this);
            mContainerPublication.setVisibility(GONE);
            final ConstraintLayout container = findViewById(R.id.node_controls_container);
            layoutSceneSetupServerBinding = LayoutSceneSetupServerBinding.inflate(getLayoutInflater(), container, true);

            layoutSceneSetupServerBinding.recyclerViewScenes.setLayoutManager(new LinearLayoutManager(this));
            layoutSceneSetupServerBinding.recyclerViewScenes.setItemAnimator(null);
            final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RemovableItemTouchHelperCallback(this));
            itemTouchHelper.attachToRecyclerView(layoutSceneSetupServerBinding.recyclerViewScenes);
            layoutSceneSetupServerBinding.recyclerViewScenes.setAdapter(mScenesAdapter);

            layoutSceneSetupServerBinding.actionStore.setOnClickListener(v ->
                    sceneSelector.launch(new Intent(this, ScenesActivity.class).putExtra(EXTRA_DATA, SELECT_SCENE)));
            mViewModel.getSelectedModel().observe(this, this::updateUi);
            mViewModel.getSelectedElement().observe(this, element -> {
                final SceneServer sceneServer = (SceneServer) element.getMeshModels().get((int) SCENE_SERVER);
                if (sceneServer != null) {
                    mScenesAdapter.update(populateAdapter(sceneServer));
                    updateScenesUi(sceneServer);
                }
            });
        }
    }

    @Override
    protected void enableClickableViews() {
        super.enableClickableViews();
        if (layoutSceneSetupServerBinding != null)
            layoutSceneSetupServerBinding.actionStore.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        if (layoutSceneSetupServerBinding != null)
            layoutSceneSetupServerBinding.actionStore.setEnabled(false);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        if (viewHolder instanceof StoredScenesAdapter.ViewHolder) {
            final int number = ((SceneUiState) viewHolder.getSwipeableView().getTag()).getNumber();
            final Scene scene = mViewModel.getNetworkLiveData().getMeshNetwork().getScene(number);
            if (!checkConnectivity(mContainer)) {
                mScenesAdapter.notifyItemChanged(viewHolder.getAbsoluteAdapterPosition());
                return;
            }
            if (scene != null)
                sendSceneDelete(scene);
        } else {
            super.onItemDismiss(viewHolder);
        }
    }

    private void sendSceneStore(final Scene scene) {
        final ApplicationKey key = ((ModelConfigurationViewModel) mViewModel).getDefaultApplicationKey();
        if (key != null) {
            final SceneStore sceneStore = new SceneStore(key, scene.getNumber());
            sendMessage(sceneStore);
        }
    }

    private void sendSceneDelete(final Scene scene) {
        final ApplicationKey key = ((ModelConfigurationViewModel) mViewModel).getDefaultApplicationKey();
        if (key != null) {
            final SceneDelete sceneStore = new SceneDelete(key, scene.getNumber());
            sendMessage(sceneStore);
        }
    }

    protected void updateUi(final MeshModel model) {
        super.updateUi(model);
        updateScenesUi(model);
    }

    @Override
    protected void updateScenesUi(final MeshModel model) {
        if (model instanceof SceneServer) {
            final SceneServer sceneServer = (SceneServer) model;
            Log.d("AAAA", "Scene numbers: " + sceneServer.getScenesNumbers().size());
            if (sceneServer.getScenesNumbers().isEmpty()) {
                layoutSceneSetupServerBinding.recyclerViewScenes.setVisibility(GONE);
                layoutSceneSetupServerBinding.noCurrentSceneAvailable.setVisibility(VISIBLE);
            } else {
                layoutSceneSetupServerBinding.recyclerViewScenes.setVisibility(VISIBLE);
                layoutSceneSetupServerBinding.noCurrentSceneAvailable.setVisibility(GONE);
            }
        }
    }
}
