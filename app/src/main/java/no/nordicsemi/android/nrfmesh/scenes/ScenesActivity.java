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
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.Scene;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.di.Injectable;
import no.nordicsemi.android.nrfmesh.scenes.adapter.ManageScenesAdapter;
import no.nordicsemi.android.nrfmesh.scenes.dialog.DialogFragmentCreateScene;
import no.nordicsemi.android.nrfmesh.scenes.dialog.DialogFragmentEditScene;
import no.nordicsemi.android.nrfmesh.viewmodels.ScenesViewModel;
import no.nordicsemi.android.nrfmesh.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmesh.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

public class ScenesActivity extends AppCompatActivity implements Injectable,
        ManageScenesAdapter.OnItemClickListener,
        SceneCallbacks,
        ItemTouchHelperAdapter {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    //UI Bindings
    @BindView(R.id.empty_scenes)
    View mEmptyView;
    @BindView(R.id.container)
    CoordinatorLayout container;

    private ScenesViewModel mViewModel;
    private ManageScenesAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenes);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(ScenesViewModel.class);

        //Bind ui
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_manage_scenes);

        final ExtendedFloatingActionButton fab = findViewById(R.id.fab_add);
        final RecyclerView scenesRecyclerView = findViewById(R.id.recycler_view_scenes);
        scenesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(scenesRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        scenesRecyclerView.addItemDecoration(dividerItemDecoration);
        scenesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(scenesRecyclerView);
        scenesRecyclerView.setAdapter(mAdapter = new ManageScenesAdapter(this, mViewModel.getNetworkLiveData()));
        mAdapter.setOnItemClickListener(this);

        fab.setOnClickListener(v -> DialogFragmentCreateScene.newInstance(createScene()).show(getSupportFragmentManager(), null));

        scenesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final LinearLayoutManager m = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (m != null) {
                    if (m.findFirstCompletelyVisibleItemPosition() == 0) {
                        fab.extend();
                    } else {
                        fab.shrink();
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final Scene scene = (Scene) viewHolder.getSwipeableView().getTag();
        try {
            final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
            if (network.removeScene(scene)) {
                displaySnackBar(scene);
                // Show the empty view
                final boolean empty = mAdapter.getItemCount() == 0;
                if (empty) {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception ex) {
            mAdapter.notifyDataSetChanged();
            mViewModel.displaySnackBar(this, container, ex.getMessage(), Snackbar.LENGTH_LONG);
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {
        //Do nothing
    }

    private void displaySnackBar(@NonNull final Scene scene) {
        Snackbar.make(container, getString(R.string.scene_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), view -> {
                    mEmptyView.setVisibility(View.INVISIBLE);
                    mViewModel.getNetworkLiveData().getMeshNetwork().addScene(scene);
                })
                .setActionTextColor(getResources().getColor(R.color.colorSecondary))
                .show();
    }

    @Override
    public void onItemClick(final int position, @NonNull final Scene scene) {
        DialogFragmentEditScene.newInstance(scene).show(getSupportFragmentManager(), null);
    }

    @Override
    public Scene createScene() {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        return network.createScene(network.getSelectedProvisioner());
    }

    @Override
    public Scene createScene(@NonNull final String name) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        return mViewModel.getNetworkLiveData().getMeshNetwork().createScene(network.getSelectedProvisioner(), name);
    }

    @Override
    public Scene createScene(@NonNull final String name, final int number) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        return mViewModel.getNetworkLiveData().getMeshNetwork().createScene(network.getSelectedProvisioner(), number, name);
    }

    @Override
    public boolean onSceneAdded(@NonNull final String name, final int number) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        return mViewModel.getNetworkLiveData().getMeshNetwork().addScene(network.createScene(network.getSelectedProvisioner(), number, name));
    }

    @Override
    public boolean onSceneAdded(@NonNull final Scene scene) {
        return mViewModel.getNetworkLiveData().getMeshNetwork().addScene(scene);
    }

    @Override
    public boolean onSceneUpdated(@NonNull final Scene scene) {
        return mViewModel.getNetworkLiveData().getMeshNetwork().updateScene(scene);
    }
}
