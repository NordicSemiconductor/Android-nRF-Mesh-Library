package no.nordicsemi.android.nrfmesh.node;

import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.Scene;
import no.nordicsemi.android.mesh.models.SigModelParser;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.SceneDelete;
import no.nordicsemi.android.mesh.transport.SceneStore;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutSceneSetupServerBinding;
import no.nordicsemi.android.nrfmesh.scenes.ScenesActivity;
import no.nordicsemi.android.nrfmesh.scenes.adapter.StoredScenesAdapter;
import no.nordicsemi.android.nrfmesh.viewmodels.ModelConfigurationViewModel;
import no.nordicsemi.android.nrfmesh.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmesh.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static no.nordicsemi.android.nrfmesh.utils.Utils.EXTRA_DATA;
import static no.nordicsemi.android.nrfmesh.utils.Utils.SELECT_SCENE;
import static no.nordicsemi.android.nrfmesh.utils.Utils.STORE_SCENE;

@AndroidEntryPoint
public class SceneSetupServerModelActivity extends SceneServerModelActivity
        implements ItemTouchHelperAdapter {

    private LayoutSceneSetupServerBinding layoutSceneSetupServerBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null && model.getModelId() == SigModelParser.SCENE_SETUP_SERVER) {
            mSwipe.setOnRefreshListener(this);
            mContainerPublication.setVisibility(GONE);
            final ConstraintLayout container = findViewById(R.id.node_controls_container);
            layoutSceneSetupServerBinding = LayoutSceneSetupServerBinding.inflate(getLayoutInflater(), container, true);

            layoutSceneSetupServerBinding.recyclerViewScenes.setLayoutManager(new LinearLayoutManager(this));
            layoutSceneSetupServerBinding.recyclerViewScenes.setItemAnimator(new DefaultItemAnimator());
            final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
            final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
            itemTouchHelper.attachToRecyclerView(layoutSceneSetupServerBinding.recyclerViewScenes);
            layoutSceneSetupServerBinding.recyclerViewScenes.setAdapter(mScenesAdapter = new StoredScenesAdapter(this, mViewModel.getSelectedElement(), mViewModel.getNetworkLiveData()));

            layoutSceneSetupServerBinding.actionStore.setOnClickListener(v -> startActivityForResult(new Intent(this, ScenesActivity.class).putExtra(EXTRA_DATA, SELECT_SCENE), STORE_SCENE));
            mViewModel.getSelectedModel().observe(this, meshModel -> {
                if (meshModel != null) {
                    updateUi(meshModel);
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
        super.onItemDismiss(viewHolder);
        final Scene scene = (Scene) viewHolder.getSwipeableView().getTag();
        if (!checkConnectivity(mContainer)) {
            mScenesAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
            return;
        }
        sendSceneDelete(scene);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STORE_SCENE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    sendSceneStore(data.getParcelableExtra(EXTRA_DATA));
                }
            }
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
        if (mScenesAdapter != null) {
            if (mScenesAdapter.getItemCount() == 0) {
                layoutSceneSetupServerBinding.recyclerViewScenes.setVisibility(GONE);
                layoutSceneSetupServerBinding.noCurrentSceneAvailable.setVisibility(VISIBLE);
            } else {
                layoutSceneSetupServerBinding.recyclerViewScenes.setVisibility(VISIBLE);
                layoutSceneSetupServerBinding.noCurrentSceneAvailable.setVisibility(GONE);
            }
        }
    }
}
