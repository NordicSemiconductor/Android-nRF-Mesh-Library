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

package no.nordicsemi.android.nrfmeshprovisioner.keys;

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
import androidx.coordinatorlayout.widget.CoordinatorLayout;
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
import no.nordicsemi.android.meshprovisioner.NetworkKey;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.keys.adapter.ManageNetKeyAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.NetKeysViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class NetKeysActivity extends AppCompatActivity implements Injectable,
        ManageNetKeyAdapter.OnItemClickListener,
        ItemTouchHelperAdapter {

    public static final String EDIT_NET_KEY = "EDIT_NET_KEY";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    //UI Bindings
    @BindView(R.id.container)
    CoordinatorLayout container;
    @BindView(R.id.scroll_container)
    ScrollView scrollView;
    @BindView(R.id.sub_net_key_card)
    CardView mSubNetKeyCard;
    @BindView(R.id.recycler_view_keys)
    RecyclerView netKeysRecyclerView;
    @BindView(R.id.fab_add)
    ExtendedFloatingActionButton fab;
    @BindView(R.id.container_primary_net_key)
    View containerKey;
    @BindView(R.id.div3)
    View divider;
    @BindView(R.id.delete_hint)
    View deleteHint;

    private NetKeysViewModel mViewModel;
    private ManageNetKeyAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_keys);
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(NetKeysViewModel.class);

        //Bind ui
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.title_manage_net_keys);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        containerKey.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vpn_key_black_alpha_24dp));
        final TextView keyTitle = containerKey.findViewById(R.id.title);
        final TextView keyView = containerKey.findViewById(R.id.text);
        keyView.setVisibility(View.VISIBLE);

        netKeysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        netKeysRecyclerView.setItemAnimator(new DefaultItemAnimator());

        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        mAdapter = new ManageNetKeyAdapter(this, mViewModel.getNetworkLiveData());
        mAdapter.setOnItemClickListener(this);
        netKeysRecyclerView.setAdapter(mAdapter);

        mViewModel.getNetworkLiveData().observe(this, meshNetworkLiveData -> {
            final MeshNetwork network = meshNetworkLiveData.getMeshNetwork();
            if (network != null) {
                final NetworkKey networkKey = network.getPrimaryNetworkKey();
                if (networkKey != null) {
                    keyTitle.setText(networkKey.getName());
                    keyView.setText(MeshParserUtils.bytesToHex(networkKey.getKey(), false));

                    if (network.getNetKeys().size() > 1) {
                        mSubNetKeyCard.setVisibility(View.VISIBLE);
                    } else {
                        mSubNetKeyCard.setVisibility(View.GONE);
                    }
                }
            }
        });

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            switch (bundle.getInt(Utils.EXTRA_DATA)) {
                case Utils.MANAGE_NET_KEY:
                    setUpClickListeners();
                    itemTouchHelper.attachToRecyclerView(netKeysRecyclerView);
                    break;
                case Utils.ADD_NET_KEY:
                    getSupportActionBar().setTitle(R.string.title_select_net_key);
                    divider.setVisibility(View.GONE);
                    deleteHint.setVisibility(View.GONE);
                    fab.hide();
                    mAdapter = new ManageNetKeyAdapter(this, mViewModel.getNetworkLiveData());
                    mAdapter.setOnItemClickListener(this);
                    netKeysRecyclerView.setAdapter(mAdapter);
                    break;
            }

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
    public void onItemClick(final int position, @NonNull final NetworkKey networkKey) {
        final Intent intent = new Intent(this, EditNetKeyActivity.class);
        intent.putExtra(EDIT_NET_KEY, networkKey.getKeyIndex());
        startActivity(intent);
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
                .setActionTextColor(getResources().getColor(R.color.colorPrimaryDark))
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

    private void setUpClickListeners() {
        containerKey.setOnClickListener(v -> {
            final Intent intent = new Intent(this, EditNetKeyActivity.class);
            intent.putExtra(EDIT_NET_KEY, 0);
            startActivity(intent);
        });

        fab.setOnClickListener(v -> {
            final Intent intent = new Intent(this, AddNetKeyActivity.class);
            startActivity(intent);
        });

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (scrollView.getScrollY() == 0) {
                fab.extend();
            } else {
                fab.shrink();
            }
        });
    }
}
