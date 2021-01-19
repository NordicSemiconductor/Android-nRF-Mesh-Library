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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.Scene;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityScenesBinding;
import no.nordicsemi.android.nrfmesh.scenes.adapter.ManageScenesAdapter;
import no.nordicsemi.android.nrfmesh.scenes.dialog.DialogFragmentCreateScene;
import no.nordicsemi.android.nrfmesh.scenes.dialog.DialogFragmentEditScene;
import no.nordicsemi.android.nrfmesh.viewmodels.ScenesViewModel;
import no.nordicsemi.android.nrfmesh.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmesh.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

import static no.nordicsemi.android.nrfmesh.utils.Utils.EXTRA_DATA;
import static no.nordicsemi.android.nrfmesh.utils.Utils.SELECT_SCENE;

@AndroidEntryPoint
public class ScenesActivity extends AppCompatActivity implements
        ManageScenesAdapter.OnItemClickListener,
        SceneCallbacks,
        ItemTouchHelperAdapter {

    private ActivityScenesBinding binding;
    private ScenesViewModel mViewModel;

    private ManageScenesAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScenesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mViewModel = new ViewModelProvider(this).get(ScenesViewModel.class);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.recyclerViewScenes.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(binding.recyclerViewScenes.getContext(), DividerItemDecoration.VERTICAL);
        binding.recyclerViewScenes.addItemDecoration(dividerItemDecoration);
        binding.recyclerViewScenes.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerViewScenes.setAdapter(mAdapter = new ManageScenesAdapter(this, mViewModel.getNetworkLiveData()));
        mAdapter.setOnItemClickListener(this);

        binding.fabAdd.setOnClickListener(v -> DialogFragmentCreateScene.newInstance(createScene()).show(getSupportFragmentManager(), null));

        binding.recyclerViewScenes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final LinearLayoutManager m = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (m != null) {
                    if (m.findFirstCompletelyVisibleItemPosition() == 0) {
                        binding.fabAdd.extend();
                    } else {
                        binding.fabAdd.shrink();
                    }
                }
            }
        });


        final Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getInt(EXTRA_DATA) == SELECT_SCENE) {
            binding.fabAdd.setVisibility(View.GONE);
            getSupportActionBar().setTitle(R.string.title_select_scene);
        } else {
            final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
            final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
            itemTouchHelper.attachToRecyclerView(binding.recyclerViewScenes);
            getSupportActionBar().setTitle(R.string.title_manage_scenes);
        }
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
                    binding.emptyScenes.getRoot().setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception ex) {
            mAdapter.notifyDataSetChanged();
            mViewModel.displaySnackBar(this, binding.container, ex.getMessage(), Snackbar.LENGTH_LONG);
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {
        //Do nothing
    }

    private void displaySnackBar(@NonNull final Scene scene) {
        Snackbar.make(binding.container, getString(R.string.scene_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), view -> {
                    binding.emptyScenes.getRoot().setVisibility(View.INVISIBLE);
                    mViewModel.getNetworkLiveData().getMeshNetwork().addScene(scene);
                })
                .setActionTextColor(getResources().getColor(R.color.colorSecondary))
                .show();
    }

    @Override
    public void onItemClick(final int position, @NonNull final Scene scene) {
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getInt(EXTRA_DATA) == SELECT_SCENE) {
            final Intent returnIntent = new Intent();
            returnIntent.putExtra(EXTRA_DATA, scene);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
