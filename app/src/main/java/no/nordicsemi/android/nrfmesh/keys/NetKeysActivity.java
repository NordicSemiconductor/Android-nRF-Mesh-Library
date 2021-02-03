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
import android.widget.ScrollView;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityKeysBinding;
import no.nordicsemi.android.nrfmesh.databinding.ActivityNetKeysBinding;
import no.nordicsemi.android.nrfmesh.keys.adapter.ManageNetKeyAdapter;
import no.nordicsemi.android.nrfmesh.viewmodels.NetKeysViewModel;
import no.nordicsemi.android.nrfmesh.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmesh.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

import static no.nordicsemi.android.nrfmesh.utils.Utils.ADD_NET_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.EDIT_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.EXTRA_DATA;
import static no.nordicsemi.android.nrfmesh.utils.Utils.HEARTBEAT_PUBLICATION_NET_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.MANAGE_NET_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.RESULT_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.RESULT_KEY_INDEX;

@AndroidEntryPoint
public class NetKeysActivity extends AppCompatActivity implements
        ManageNetKeyAdapter.OnItemClickListener,
        ItemTouchHelperAdapter {

    private NetKeysViewModel mViewModel;
    private ManageNetKeyAdapter mAdapter;

    //UI Bindings
    CoordinatorLayout container;


    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NetKeysViewModel.class);

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            switch (bundle.getInt(EXTRA_DATA)) {
                case MANAGE_NET_KEY:
                    setupManageNetKeyUi();
                    break;
                case ADD_NET_KEY:
                case HEARTBEAT_PUBLICATION_NET_KEY:
                    setupSelectNetKeyUi();
                    break;
            }

        }
    }

    private void setupManageNetKeyUi() {
        final ActivityNetKeysBinding binding = ActivityNetKeysBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        container = binding.container;
        binding.recyclerViewKeys.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewKeys.setItemAnimator(new DefaultItemAnimator());
        binding.containerPrimaryNetKey.image
                .setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_24dp));
        binding.containerPrimaryNetKey.text.setVisibility(View.VISIBLE);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(R.string.title_manage_net_keys);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.containerPrimaryNetKey.getRoot().setOnClickListener(v -> {
            final Intent intent = new Intent(this, EditNetKeyActivity.class);
            intent.putExtra(EDIT_KEY, 0);
            startActivity(intent);
        });
        final CardView mSubNetKeyCard = binding.subNetKeyCard;
        mViewModel.getNetworkLiveData().observe(this, meshNetworkLiveData -> {
            final MeshNetwork network = meshNetworkLiveData.getMeshNetwork();
            if (network != null) {
                final NetworkKey networkKey = network.getPrimaryNetworkKey();
                if (networkKey != null) {
                    binding.containerPrimaryNetKey.title.setText(networkKey.getName());
                    binding.containerPrimaryNetKey.text.setText(MeshParserUtils.bytesToHex(networkKey.getKey(), false));

                    if (network.getNetKeys().size() > 1) {
                        binding.subNetKeyCard.setVisibility(View.VISIBLE);
                    } else {
                        binding.subNetKeyCard.setVisibility(View.GONE);
                    }
                }
            }
        });

        binding.fabAdd.setOnClickListener(v -> {
            final Intent intent = new Intent(this, AddNetKeyActivity.class);
            startActivity(intent);
        });
        final ScrollView scrollView = binding.scrollContainer;
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (scrollView.getScrollY() == 0) {
                binding.fabAdd.extend();
            } else {
                binding.fabAdd.shrink();
            }
        });

        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        mAdapter = new ManageNetKeyAdapter(this, mViewModel.getNetworkLiveData());
        mAdapter.setOnItemClickListener(this);
        binding.recyclerViewKeys.setAdapter(mAdapter);
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewKeys);
    }

    private void setupSelectNetKeyUi() {
        final ActivityKeysBinding binding = ActivityKeysBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        container = binding.container;
        binding.recyclerViewKeys.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewKeys.setItemAnimator(new DefaultItemAnimator());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(R.string.title_manage_net_keys);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_select_net_key);
        binding.fabAdd.hide();
        mAdapter = new ManageNetKeyAdapter(this, mViewModel.getSelectedMeshNode(), mViewModel.getNetworkLiveData().getNetworkKeys());
        mAdapter.setOnItemClickListener(this);
        binding.recyclerViewKeys.setAdapter(mAdapter);
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
    public void onItemClick(final int position, @NonNull final NetworkKey networkKey) {
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            switch (bundle.getInt(EXTRA_DATA)) {
                case ADD_NET_KEY:
                case HEARTBEAT_PUBLICATION_NET_KEY:
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(RESULT_KEY_INDEX, position);
                    returnIntent.putExtra(RESULT_KEY, networkKey);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                    break;
                case MANAGE_NET_KEY:
                    final Intent intent = new Intent(this, EditNetKeyActivity.class);
                    intent.putExtra(EDIT_KEY, networkKey.getKeyIndex());
                    startActivity(intent);
                    break;
            }
        }
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final NetworkKey key = (NetworkKey) viewHolder.getSwipeableView().getTag();
        try {
            if (removeNetKey(key)) {
                displaySnackBar(key);
            }
        } catch (Exception ex) {
            mAdapter.notifyDataSetChanged();
            mViewModel.displaySnackBar(this, container, ex.getMessage(), Snackbar.LENGTH_LONG);
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {

    }

    private void displaySnackBar(@NonNull final NetworkKey networkKey) {
        Snackbar.make(container, getString(R.string.net_key_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), view -> addNetKey(networkKey))
                .setActionTextColor(getResources().getColor(R.color.colorSecondary))
                .show();
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean addNetKey(@NonNull final NetworkKey networkKey) {
        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        if (network != null) {
            return network.addNetKey(networkKey);
        }
        return false;
    }

    private boolean removeNetKey(@NonNull final NetworkKey networkKey) {
        final MeshNetwork network = mViewModel.getMeshManagerApi().getMeshNetwork();
        if (network != null) {
            return network.removeNetKey(networkKey);
        }
        return false;
    }
}
