package no.nordicsemi.android.nrfmesh.node;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textview.MaterialTextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.Scene;
import no.nordicsemi.android.mesh.models.SigModelParser;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.SceneDelete;
import no.nordicsemi.android.mesh.transport.SceneStore;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.scenes.ScenesActivity;
import no.nordicsemi.android.nrfmesh.scenes.adapter.StoredScenesAdapter;
import no.nordicsemi.android.nrfmesh.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmesh.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static no.nordicsemi.android.nrfmesh.utils.Utils.EXTRA_DATA;
import static no.nordicsemi.android.nrfmesh.utils.Utils.SELECT_SCENE;
import static no.nordicsemi.android.nrfmesh.utils.Utils.STORE_SCENE;

public class SceneSetupServerModelActivity extends SceneServerModelActivity
        implements ItemTouchHelperAdapter {

    private RecyclerView mRecyclerViewScenes;
    private Button mActionStoreScene;
    private MaterialTextView noScenesAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null && model.getModelId() == SigModelParser.SCENE_SETUP_SERVER) {
            mSwipe.setOnRefreshListener(this);
            mContainerPublication.setVisibility(GONE);
            final ConstraintLayout container = findViewById(R.id.node_controls_container);
            final View layoutSceneServer = LayoutInflater.from(this).inflate(R.layout.layout_scene_setup_server, container);
            mRecyclerViewScenes = layoutSceneServer.findViewById(R.id.recycler_view_scenes);
            noScenesAvailable = layoutSceneServer.findViewById(R.id.no_current_scene_available);
            mActionStoreScene = layoutSceneServer.findViewById(R.id.action_store);

            mRecyclerViewScenes.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerViewScenes.setItemAnimator(new DefaultItemAnimator());
            final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
            final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
            itemTouchHelper.attachToRecyclerView(mRecyclerViewScenes);
            mRecyclerViewScenes.setAdapter(mScenesAdapter = new StoredScenesAdapter(this, mViewModel.getSelectedElement(), mViewModel.getNetworkLiveData()));

            mActionStoreScene.setOnClickListener(v -> startActivityForResult(new Intent(this, ScenesActivity.class).putExtra(EXTRA_DATA, SELECT_SCENE), STORE_SCENE));
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
        mActionStoreScene.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        mActionStoreScene.setEnabled(false);
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
        final ApplicationKey key = getDefaultApplicationKey();
        if (key != null) {
            final SceneStore sceneStore = new SceneStore(key, scene.getNumber());
            sendMessage(sceneStore);
        }
    }

    private void sendSceneDelete(final Scene scene) {
        final ApplicationKey key = getDefaultApplicationKey();
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
                mRecyclerViewScenes.setVisibility(GONE);
                noScenesAvailable.setVisibility(VISIBLE);
            } else {
                mRecyclerViewScenes.setVisibility(VISIBLE);
                noScenesAvailable.setVisibility(GONE);
            }
        }
    }
}
