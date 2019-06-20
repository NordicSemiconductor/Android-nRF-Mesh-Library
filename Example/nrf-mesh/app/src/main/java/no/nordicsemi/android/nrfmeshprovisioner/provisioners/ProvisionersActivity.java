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

package no.nordicsemi.android.nrfmeshprovisioner.provisioners;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.adapter.ProvisionerAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ProvisionersViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class ProvisionersActivity extends AppCompatActivity implements Injectable,
        ProvisionerAdapter.OnItemClickListener,
        ItemTouchHelperAdapter {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    //UI Bindings
    @BindView(R.id.container)
    View container;
    @BindView(R.id.scroll_container)
    ScrollView scrollView;
    @BindView(R.id.provisioners_card)
    CardView mProvisionersCard;

    private ProvisionersViewModel mViewModel;
    private ProvisionerAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provisioners);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ProvisionersViewModel.class);

        //Bind ui
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.title_manage_provisioners);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final View containerProvisioner = findViewById(R.id.container_current_provisioner);
        containerProvisioner.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_account_key_black_alpha_24dp));
        final TextView provisionerTitle = containerProvisioner.findViewById(R.id.title);
        final TextView provisionerView = containerProvisioner.findViewById(R.id.text);
        provisionerView.setVisibility(View.VISIBLE);

        final ExtendedFloatingActionButton fab = findViewById(R.id.fab_add);

        final RecyclerView provisionersRecyclerView = findViewById(R.id.recycler_view_provisioners);
        provisionersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        provisionersRecyclerView.setItemAnimator(new DefaultItemAnimator());

        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(provisionersRecyclerView);
        mAdapter = new ProvisionerAdapter(this, mViewModel.getMeshNetworkLiveData());
        mAdapter.setOnItemClickListener(this);
        provisionersRecyclerView.setAdapter(mAdapter);

        mViewModel.getMeshNetworkLiveData().observe(this, meshNetworkLiveData -> {
            final MeshNetwork network = meshNetworkLiveData.getMeshNetwork();
            if (network != null) {
                final Provisioner provisioner = network.getSelectedProvisioner();
                provisionerTitle.setText(provisioner.getProvisionerName());
                if(MeshAddress.isValidUnicastAddress(provisioner.getProvisionerAddress())) {
                    provisionerView.setText(getString(R.string.unicast_address, MeshAddress.formatAddress(provisioner.getProvisionerAddress(), true)));
                } else {
                    provisionerView.setText(R.string.not_assigned);
                }

                if (network.getProvisioners().size() > 1) {
                    mProvisionersCard.setVisibility(View.VISIBLE);
                } else {
                    mProvisionersCard.setVisibility(View.GONE);
                }
            }
        });

        containerProvisioner.setOnClickListener(v -> {
            final MeshNetwork network = mViewModel.getMeshNetworkLiveData().getMeshNetwork();
            if (network != null) {
                final Provisioner provisioner = network.getSelectedProvisioner();
                mViewModel.setSelectedProvisioner(provisioner);
                final Intent intent = new Intent(this, EditProvisionerActivity.class);
                startActivity(intent);
            }
        });

        fab.setOnClickListener(v -> {
            final Intent intent = new Intent(this, AddProvisionerActivity.class);
            startActivity(intent);
        });

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (scrollView.getScrollY() == 0) {
                fab.extend(true);
            } else {
                fab.shrink(true);
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
    public void onItemClick(final int position, @NonNull final Provisioner provisioner) {
        mViewModel.setSelectedProvisioner(provisioner);
        final Intent intent = new Intent(this, EditProvisionerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        final Provisioner provisioner = mAdapter.getItem(position);
        final MeshNetwork network = mViewModel.getMeshNetworkLiveData().getMeshNetwork();

        try {
            if(network != null) {
                if (network.removeProvisioner(provisioner)) {
                    displaySnackBar(provisioner);
                }
            }
        } catch (Exception ex) {
            mAdapter.notifyDataSetChanged();
            displaySnackBar(ex.getMessage());
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {

    }

    private void displaySnackBar(@NonNull final Provisioner provisioner) {
        Snackbar.make(container, getString(R.string.provisioner_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), view -> {
                    final MeshNetwork network = mViewModel.getMeshNetworkLiveData().getMeshNetwork();
                        if(network != null) {
                            network.addProvisioner(provisioner);
                        }
                })
                .setActionTextColor(getResources().getColor(R.color.colorPrimaryDark))
                .show();
    }

    private void displaySnackBar(final String message) {
        Snackbar.make(container, message, Snackbar.LENGTH_LONG)
                .show();
    }
}
