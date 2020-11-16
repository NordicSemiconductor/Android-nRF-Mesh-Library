/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrfmesh.scenes;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.Scene;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.transport.SceneGet;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.di.Injectable;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.scenes.adapter.StoredScenesAdapter;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.viewmodels.AddedScenesViewModel;
import no.nordicsemi.android.nrfmesh.viewmodels.BaseActivity;

public class StoredScenesActivity extends BaseActivity implements Injectable,
        StoredScenesAdapter.OnItemListener, SwipeRefreshLayout.OnRefreshListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    //UI Bindings
    @BindView(R.id.container)
    CoordinatorLayout mContainer;
    @BindView(R.id.empty_scenes)
    View mEmptyView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.recycler_view_scenes)
    RecyclerView mRecyclerviewScenes;
    @BindView(R.id.configuration_progress_bar)
    ProgressBar mProgressbar;

    private StoredScenesAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenes);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(AddedScenesViewModel.class);
        init();

        //Bind ui
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_stored_scenes);

        findViewById(R.id.fab_add).setVisibility(View.GONE);
        mRecyclerviewScenes = findViewById(R.id.recycler_view_scenes);
        mRecyclerviewScenes.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mRecyclerviewScenes.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerviewScenes.addItemDecoration(dividerItemDecoration);
        mRecyclerviewScenes.setItemAnimator(new DefaultItemAnimator());
        mSwipe.setOnRefreshListener(this);

    }

    @Override
    public void onItemClick(final int position, @NonNull final Scene scene) {
        //TODO bottom sheet
    }

    @Override
    protected void updateClickableViews() {

    }

    @Override
    protected void showProgressBar() {
        mHandler.postDelayed(mRunnableOperationTimeout, Utils.MESSAGE_TIME_OUT);
        disableClickableViews();
        mProgressbar.setVisibility(View.VISIBLE);
    }

    @Override
    protected final void hideProgressBar() {
        mSwipe.setRefreshing(false);
        enableClickableViews();
        mProgressbar.setVisibility(View.INVISIBLE);
        mHandler.removeCallbacks(mRunnableOperationTimeout);
    }

    @Override
    protected void enableClickableViews() {
        enableAdapterClickListener(true);
        mRecyclerviewScenes.setEnabled(true);
        mRecyclerviewScenes.setClickable(true);
    }

    @Override
    protected void disableClickableViews() {
        enableAdapterClickListener(false);
        mRecyclerviewScenes.setEnabled(false);
        mRecyclerviewScenes.setClickable(false);
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {

    }

    private void handleStatuses() {
        final MeshMessage message = mViewModel.getMessageQueue().peek();
        if (message != null) {
            sendMessage(message);
        } else {
            mViewModel.displaySnackBar(this, mContainer, getString(R.string.operation_success), Snackbar.LENGTH_SHORT);
        }
    }

    protected void sendMessage(final MeshMessage meshMessage) {
        try {
            if (!checkConnectivity(mContainer))
                return;
            showProgressBar();
            final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
            if (node != null) {
                mViewModel.getMeshManagerApi().createMeshPdu(node.getUnicastAddress(), meshMessage);
            }
        } catch (IllegalArgumentException ex) {
            hideProgressBar();
            final DialogFragmentError message = DialogFragmentError.
                    newInstance(getString(R.string.title_error), ex.getMessage());
            message.show(getSupportFragmentManager(), null);
        }
    }

    private void displaySnackBar(@NonNull final Scene scene) {
        Snackbar.make(mContainer, getString(R.string.scene_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), view -> {
                    mEmptyView.setVisibility(View.INVISIBLE);
                    mViewModel.getNetworkLiveData().getMeshNetwork().addScene(scene);
                })
                .setActionTextColor(getResources().getColor(R.color.colorSecondary))
                .show();
    }

    void enableAdapterClickListener(final boolean enable) {
        mAdapter.setOnItemClickListener(enable ? this : null);
    }

    @Override
    public void onRefresh() {
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null && !model.getBoundAppKeyIndexes().isEmpty()) {
            final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
            final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);
            mViewModel.getMessageQueue().add(new SceneGet(appKey));
            sendMessage(mViewModel.getMessageQueue().peek());
        } else {
            mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_no_app_keys_bound), Snackbar.LENGTH_LONG);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isFinishing()) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
