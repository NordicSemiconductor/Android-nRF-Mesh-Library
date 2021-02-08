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

package no.nordicsemi.android.nrfmesh.provisioners;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.Provisioner;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityProvisionersBinding;
import no.nordicsemi.android.nrfmesh.provisioners.adapter.ProvisionerAdapter;
import no.nordicsemi.android.nrfmesh.viewmodels.ProvisionersViewModel;
import no.nordicsemi.android.nrfmesh.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmesh.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

@AndroidEntryPoint
public class ProvisionersActivity extends AppCompatActivity implements
        ProvisionerAdapter.OnItemClickListener,
        ItemTouchHelperAdapter {

    private ActivityProvisionersBinding binding;
    private ProvisionersViewModel mViewModel;
    private ProvisionerAdapter mAdapter;


    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProvisionersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mViewModel = new ViewModelProvider(this).get(ProvisionersViewModel.class);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(R.string.title_manage_provisioners);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.containerCurrentProvisioner.image.
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_account_key));
        binding.containerCurrentProvisioner.text.setVisibility(View.VISIBLE);

        binding.recyclerViewProvisioners.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewProvisioners.setItemAnimator(null);

        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        mItemTouchHelper.attachToRecyclerView(binding.recyclerViewProvisioners);
        mAdapter = new ProvisionerAdapter(this, mViewModel.getNetworkLiveData());
        mAdapter.setOnItemClickListener(this);
        binding.recyclerViewProvisioners.setAdapter(mAdapter);

        mViewModel.getNetworkLiveData().observe(this, meshNetworkLiveData -> {
            final MeshNetwork network = meshNetworkLiveData.getMeshNetwork();
            if (network != null) {
                final Provisioner provisioner = network.getSelectedProvisioner();
                binding.containerCurrentProvisioner.title.setText(provisioner.getProvisionerName());
                if (provisioner.getProvisionerAddress() == null) {
                    binding.containerCurrentProvisioner.text.setText(R.string.unicast_address_unassigned);
                } else {
                    if (MeshAddress.isValidUnicastAddress(provisioner.getProvisionerAddress())) {
                        binding.containerCurrentProvisioner.text.setText(getString(R.string.unicast_address,
                                MeshAddress.formatAddress(provisioner.getProvisionerAddress(), true)));
                    } else {
                        binding.containerCurrentProvisioner.text.setText(R.string.unicast_address_unassigned);
                    }
                }

                if (network.getProvisioners().size() > 1) {
                    binding.provisionersCard.setVisibility(View.VISIBLE);
                } else {
                    binding.provisionersCard.setVisibility(View.GONE);
                }
            }
        });

        binding.containerCurrentProvisioner.getRoot().setOnClickListener(v -> {
            final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
            if (network != null) {
                final Provisioner provisioner = network.getSelectedProvisioner();
                mViewModel.setSelectedProvisioner(provisioner);
                final Intent intent = new Intent(this, EditProvisionerActivity.class);
                startActivity(intent);
            }
        });

        binding.fabAdd.setOnClickListener(v -> {
            final Intent intent = new Intent(this, AddProvisionerActivity.class);
            startActivity(intent);
        });

        binding.scrollContainer.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (binding.scrollContainer.getScrollY() == 0) {
                binding.fabAdd.extend();
            } else {
                binding.fabAdd.shrink();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        return super.onCreateOptionsMenu(menu);
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
    public void onItemClick(final int position, @NonNull final Provisioner provisioner) {
        mViewModel.setSelectedProvisioner(provisioner);
        final Intent intent = new Intent(this, EditProvisionerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        final Provisioner provisioner = mAdapter.getItem(position);
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        try {
            if (network != null) {
                if (network.removeProvisioner(provisioner)) {
                    displaySnackBar(provisioner);
                }
            }
        } catch (Exception ex) {
            mAdapter.notifyDataSetChanged();
            mViewModel.displaySnackBar(this, binding.container, ex.getMessage(), Snackbar.LENGTH_LONG);
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {

    }

    private void displaySnackBar(@NonNull final Provisioner provisioner) {
        Snackbar.make(binding.container, getString(R.string.provisioner_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), view -> {
                    final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
                    if (network != null) {
                        network.addProvisioner(provisioner);
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.colorSecondary))
                .show();
    }
}
