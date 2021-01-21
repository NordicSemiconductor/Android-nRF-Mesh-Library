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

package no.nordicsemi.android.nrfmesh.keys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

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
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.NodeKey;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityKeysBinding;
import no.nordicsemi.android.nrfmesh.keys.adapter.ManageAppKeyAdapter;
import no.nordicsemi.android.nrfmesh.viewmodels.AppKeysViewModel;
import no.nordicsemi.android.nrfmesh.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmesh.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

import static no.nordicsemi.android.nrfmesh.utils.Utils.ADD_APP_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.BIND_APP_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.EDIT_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.EXTRA_DATA;
import static no.nordicsemi.android.nrfmesh.utils.Utils.MANAGE_APP_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.PUBLICATION_APP_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.RESULT_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.RESULT_KEY_INDEX;
import static no.nordicsemi.android.nrfmesh.utils.Utils.RESULT_KEY_LIST_SIZE;

@AndroidEntryPoint
public class AppKeysActivity extends AppCompatActivity implements
        ManageAppKeyAdapter.OnItemClickListener,
        ItemTouchHelperAdapter {

    private ActivityKeysBinding binding;
    private AppKeysViewModel mViewModel;
    private ManageAppKeyAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityKeysBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mViewModel = new ViewModelProvider(this).get(AppKeysViewModel.class);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.recyclerViewKeys.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(binding.recyclerViewKeys.getContext(), DividerItemDecoration.VERTICAL);
        binding.recyclerViewKeys.addItemDecoration(dividerItemDecoration);
        binding.recyclerViewKeys.setItemAnimator(new DefaultItemAnimator());

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            switch (bundle.getInt(EXTRA_DATA)) {
                case MANAGE_APP_KEY:
                    break;
                case ADD_APP_KEY:
                    getSupportActionBar().setTitle(R.string.title_select_app_key);
                    binding.fabAdd.hide();
                    mAdapter = new ManageAppKeyAdapter(this, mViewModel.getNetworkLiveData());
                    mAdapter.setOnItemClickListener(this);
                    binding.recyclerViewKeys.setAdapter(mAdapter);
                    setUpObserver();
                    break;
                case BIND_APP_KEY:
                case PUBLICATION_APP_KEY:
                    getSupportActionBar().setTitle(R.string.title_select_app_key);
                    binding.fabAdd.hide();
                    //Get selected mesh node
                    final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
                    if (node != null) {
                        final List<NodeKey> applicationKeys = node.getAddedAppKeys();
                        if (!applicationKeys.isEmpty()) {
                            mAdapter = new ManageAppKeyAdapter(mViewModel.getNetworkLiveData().getAppKeys(), applicationKeys);
                            mAdapter.setOnItemClickListener(this);
                            binding.recyclerViewKeys.setAdapter(mAdapter);
                        } else {
                            binding.emptyAppKeys.rationale.setText(R.string.no_added_app_keys_rationale);
                            binding.emptyAppKeys.getRoot().setVisibility(View.VISIBLE);
                        }
                    }
                    break;
            }
        } else {
            getSupportActionBar().setTitle(R.string.title_manage_app_keys);
            final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
            final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
            itemTouchHelper.attachToRecyclerView(binding.recyclerViewKeys);
            mAdapter = new ManageAppKeyAdapter(this, mViewModel.getNetworkLiveData());
            mAdapter.setOnItemClickListener(this);
            binding.recyclerViewKeys.setAdapter(mAdapter);
            setUpObserver();
        }


        binding.fabAdd.setOnClickListener(v -> {
            final Intent intent = new Intent(this, AddAppKeyActivity.class);
            startActivity(intent);
        });

        binding.recyclerViewKeys.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
    public void onBackPressed() {
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getInt(EXTRA_DATA) == MANAGE_APP_KEY) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RESULT_KEY_LIST_SIZE, mAdapter.getItemCount());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onItemClick(final int position, @NonNull final ApplicationKey appKey) {
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            switch (bundle.getInt(EXTRA_DATA)) {
                case ADD_APP_KEY:
                case BIND_APP_KEY:
                case PUBLICATION_APP_KEY:
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(RESULT_KEY_INDEX, position);
                    returnIntent.putExtra(RESULT_KEY, appKey);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();

            }
        } else {
            final Intent intent = new Intent(this, EditAppKeyActivity.class);
            intent.putExtra(EDIT_KEY, appKey.getKeyIndex());
            startActivity(intent);
        }
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final ApplicationKey key = (ApplicationKey) viewHolder.getSwipeableView().getTag();
        try {
            final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
            if (network.removeAppKey(key)) {
                displaySnackBar(key);
                // Show the empty view
                final boolean empty = mAdapter.getItemCount() == 0;
                if (empty) {
                    binding.emptyAppKeys.getRoot().setVisibility(View.VISIBLE);
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

    private void setUpObserver() {
        mViewModel.getNetworkLiveData().observe(this, networkLiveData -> {
            if (networkLiveData != null) {
                final List<ApplicationKey> keys = networkLiveData.getAppKeys();
                if (keys != null) {
                    binding.emptyAppKeys.getRoot().setVisibility(keys.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    private void displaySnackBar(@NonNull final ApplicationKey appKey) {
        Snackbar.make(binding.container, getString(R.string.app_key_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), view -> {
                    binding.emptyAppKeys.getRoot().setVisibility(View.INVISIBLE);
                    mViewModel.getNetworkLiveData().getMeshNetwork().addAppKey(appKey);
                })
                .setActionTextColor(getResources().getColor(R.color.colorSecondary))
                .show();
    }
}
