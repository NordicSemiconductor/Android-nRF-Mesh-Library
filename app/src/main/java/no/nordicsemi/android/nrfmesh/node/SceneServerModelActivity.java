package no.nordicsemi.android.nrfmesh.node;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.Scene;
import no.nordicsemi.android.mesh.models.SceneServer;
import no.nordicsemi.android.mesh.models.SigModelParser;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.SceneGet;
import no.nordicsemi.android.mesh.transport.SceneRecall;
import no.nordicsemi.android.mesh.transport.SceneRegisterStatus;
import no.nordicsemi.android.mesh.transport.SceneStatus;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutSceneServerBinding;
import no.nordicsemi.android.nrfmesh.scenes.adapter.SceneUiState;
import no.nordicsemi.android.nrfmesh.scenes.adapter.StoredScenesAdapter;
import no.nordicsemi.android.nrfmesh.scenes.dialog.BottomSheetSceneRecallDialogFragment;
import no.nordicsemi.android.nrfmesh.viewmodels.ModelConfigurationViewModel;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static no.nordicsemi.android.mesh.models.SigModelParser.SCENE_SERVER;

@AndroidEntryPoint
public class SceneServerModelActivity extends ModelConfigurationActivity implements
        BottomSheetSceneRecallDialogFragment.SceneRecallListener,
        StoredScenesAdapter.OnItemListener {

    private LayoutSceneServerBinding layoutSceneServerBinding;
    protected final StoredScenesAdapter mScenesAdapter = new StoredScenesAdapter();

    protected void updateUi(final MeshModel model) {
        updateAppStatusUi(model);
        updatePublicationUi(model);
        updateSubscriptionUi(model);
        updateScenesUi(model);
    }

    protected void updateScenesUi(final MeshModel model) {
        if (model != null) {
            final SceneServer sceneServer = (SceneServer) model;
            if (!sceneServer.getScenesNumbers().isEmpty()) {
                layoutSceneServerBinding.noCurrentSceneAvailable.setVisibility(GONE);
                layoutSceneServerBinding.recyclerViewScenes.setVisibility(VISIBLE);
            } else {
                layoutSceneServerBinding.noCurrentSceneAvailable.setVisibility(VISIBLE);
                layoutSceneServerBinding.recyclerViewScenes.setVisibility(GONE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null && model.getModelId() == SigModelParser.SCENE_SERVER) {
            mSwipe.setOnRefreshListener(this);
            mContainerPublication.setVisibility(GONE);
            layoutSceneServerBinding = LayoutSceneServerBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);
            layoutSceneServerBinding.actionRead.setOnClickListener(v -> sendGetCurrentScene());

            final RecyclerView recyclerView = layoutSceneServerBinding.recyclerViewScenes;
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mScenesAdapter);
            mScenesAdapter.setOnItemClickListener(this);
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
        if (layoutSceneServerBinding != null)
            layoutSceneServerBinding.actionRead.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        if (layoutSceneServerBinding != null)
            layoutSceneServerBinding.actionRead.setEnabled(false);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        ((ModelConfigurationViewModel) mViewModel).prepareMessageQueue();
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        mSwipe.setOnRefreshListener(this);
        if (meshMessage instanceof SceneStatus) {
            final SceneStatus status = (SceneStatus) meshMessage;
            mViewModel.removeMessage();
            if (status.isSuccessful()) {
                handleStatuses();
                updateScenesUi(mViewModel.getSelectedModel().getValue());
            } else {
                displayStatusDialogFragment(getString(R.string.title_sig_model_subscription_list), status.getStatusMessage(status.getStatus()));
            }
        } else if (meshMessage instanceof SceneRegisterStatus) {
            final SceneRegisterStatus status = (SceneRegisterStatus) meshMessage;
            mViewModel.removeMessage();
            if (status.isSuccessful()) {
                handleStatuses();
                updateScenesUi(mViewModel.getSelectedModel().getValue());
            } else {
                displayStatusDialogFragment(getString(R.string.title_sig_model_subscription_list), status.getStatusMessage(status.getStatus()));
            }
        }
        hideProgressBar();
    }

    private void sendGetCurrentScene() {
        final ApplicationKey key = ((ModelConfigurationViewModel) mViewModel).getDefaultApplicationKey();
        if (key != null) {
            sendMessage(new SceneGet(key));
        }
    }

    @Override
    public void recallScene(final int sceneNumber, final int transitionSteps, final int transitionStepResolution, final int delay) {
        sendSceneRecall(sceneNumber, transitionSteps, transitionStepResolution, delay);
    }

    private void sendSceneRecall(final int sceneNumber, final int transitionSteps, final int transitionStepResolution, final int delay) {
        final ApplicationKey key = ((ModelConfigurationViewModel) mViewModel).getDefaultApplicationKey();
        if (key != null) {
            final SceneRecall recall;
            if (transitionSteps == 0 && transitionStepResolution == 0 && delay == 0) {
                recall = new SceneRecall(key, sceneNumber, new Random().nextInt());
            } else {
                recall = new SceneRecall(key, transitionSteps, transitionStepResolution, delay, sceneNumber, new Random().nextInt());
            }
            sendMessage(recall);
        }
    }

    @Override
    public void onItemClick(@NonNull final SceneUiState sceneUiState) {
        if (checkConnectivity(mContainer))
            BottomSheetSceneRecallDialogFragment.instantiate(sceneUiState).show(getSupportFragmentManager(), null);
    }

    protected final List<SceneUiState> populateAdapter(@NonNull final SceneServer sceneServer){
        final List<SceneUiState> scenes = new ArrayList<>();
        final List<Integer> scenesNumbers = sceneServer.getScenesNumbers();
        for (int sceneNumber : scenesNumbers) {
            final Scene scene = mViewModel.getNetworkLiveData().getMeshNetwork().getScene(sceneNumber);
            if (scene != null) {
                scenes.add(new SceneUiState(scene.getNumber(), scene.getName(), scene.getNumber() == sceneServer.getCurrentScene()));
            }
        }
        return scenes;
    }
}
