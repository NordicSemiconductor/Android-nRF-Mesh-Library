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

package no.nordicsemi.android.nrfmeshprovisioner;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ManageAppKeyAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentAddAppKey;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentEditAppKey;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ManageAppKeysViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class ManageAppKeysActivity extends AppCompatActivity implements Injectable, ManageAppKeyAdapter.OnItemClickListener,
        DialogFragmentAddAppKey.DialogFragmentAddAppKeysListener,
        DialogFragmentEditAppKey.DialogFragmentEditAppKeysListener,
        ItemTouchHelperAdapter {

    public static final String RESULT_APP_KEY = "RESULT_APP_KEY";
    public static final String RESULT_APP_KEY_INDEX = "RESULT_APP_KEY_INDEX";
    public static final String RESULT_APP_KEY_LIST_SIZE = "RESULT_APP_KEY_LIST_SIZE";
    public static final String APP_KEYS = "APP_KEYS";
    public static final int SELECT_APP_KEY = 2011; //Random number
    public static final int MANAGE_APP_KEYS = 2012; //Random number
    private static final String MAIN_ACTIVITY = ".MainActivity";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    //UI Bindings
    @BindView(android.R.id.empty)
    View mEmptyView;
    @BindView(R.id.container)
    View container;

    private ManageAppKeysViewModel mViewModel;
    private ManageAppKeyAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_app_keys);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ManageAppKeysViewModel.class);

        //Bind ui
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final FloatingActionButton fab = findViewById(R.id.fab);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final RecyclerView appKeysRecyclerView = findViewById(R.id.recycler_view_app_keys);
        appKeysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(appKeysRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        appKeysRecyclerView.addItemDecoration(dividerItemDecoration);
        appKeysRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //noinspection ConstantConditions
        switch (getIntent().getExtras().getInt(Utils.EXTRA_DATA)) {
            case Utils.MANAGE_APP_KEY:
                getSupportActionBar().setTitle(R.string.title_manage_app_keys);
                final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
                final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
                itemTouchHelper.attachToRecyclerView(appKeysRecyclerView);
                mAdapter = new ManageAppKeyAdapter(this, mViewModel.getMeshNetworkLiveData());
                mAdapter.setOnItemClickListener(this);
                appKeysRecyclerView.setAdapter(mAdapter);
                setUpObserver();
                break;
            case Utils.ADD_APP_KEY:
                getSupportActionBar().setTitle(R.string.title_select_app_key);
                fab.hide();
                mAdapter = new ManageAppKeyAdapter(this, mViewModel.getMeshNetworkLiveData());
                mAdapter.setOnItemClickListener(this);
                appKeysRecyclerView.setAdapter(mAdapter);
                setUpObserver();
                break;
            case Utils.BIND_APP_KEY:
            case Utils.PUBLICATION_APP_KEY:
                getSupportActionBar().setTitle(R.string.title_select_app_key);
                fab.hide();
                //Get selected mesh node
                final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
                if (node != null) {
                    final List<ApplicationKey> applicationKeys = new ArrayList<>(node.getAddedApplicationKeys().values());
                    if (!applicationKeys.isEmpty()) {
                        mAdapter = new ManageAppKeyAdapter(this, applicationKeys);
                        mAdapter.setOnItemClickListener(this);
                        appKeysRecyclerView.setAdapter(mAdapter);
                    } else {
                        final TextView textView = mEmptyView.findViewById(R.id.rationale);
                        textView.setText(R.string.no_added_app_keys_rationale);
                        mEmptyView.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }

        fab.setOnClickListener(v -> {
            final DialogFragmentAddAppKey dialogFragmentAddAppKey = DialogFragmentAddAppKey.newInstance(null);
            dialogFragmentAddAppKey.show(getSupportFragmentManager(), null);
        });
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        final int extras = getIntent().getExtras().getInt(Utils.EXTRA_DATA);
        switch (extras) {
            case Utils.MANAGE_APP_KEY:
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RESULT_APP_KEY_LIST_SIZE, mAdapter.getItemCount());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                break;
            default:
                super.onBackPressed();
                break;
        }
    }

    @Override
    public void onItemClick(final int position, final ApplicationKey appKey) {
        final int extras = getIntent().getExtras().getInt(Utils.EXTRA_DATA);
        switch (extras) {
            case Utils.MANAGE_APP_KEY:
                final DialogFragmentEditAppKey dialogFragmentEditAppKey = DialogFragmentEditAppKey.newInstance(position, appKey);
                dialogFragmentEditAppKey.show(getSupportFragmentManager(), null);
                break;
            default:
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RESULT_APP_KEY_INDEX, position);
                returnIntent.putExtra(RESULT_APP_KEY, appKey);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                break;
        }
    }

    @Override
    public void onAppKeysUpdated(final int position, final String appKey) {
        mViewModel.getMeshNetworkLiveData().updateAppKey(position, appKey);
    }

    @Override
    public void onAppKeyAdded(final String appKey) {
        mViewModel.getMeshNetworkLiveData().addAppKey(appKey);
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final ApplicationKey key = (ApplicationKey) viewHolder.getSwipeableView().getTag();
        mViewModel.getMeshNetworkLiveData().removeAppKey(key);
        displaySnackBar(viewHolder.getAdapterPosition(), key);
        // Show the empty view
        final boolean empty = mAdapter.getItemCount() == 0;
        if (empty) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {

    }

    private void setUpObserver() {
        mViewModel.getMeshNetworkLiveData().observe(this, networkLiveData -> {
            if (networkLiveData != null) {
                final List<ApplicationKey> keys = networkLiveData.getAppKeys();
                if (keys != null) {
                    mEmptyView.setVisibility(keys.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    private void displaySnackBar(final int key, final ApplicationKey appKey) {

        Snackbar.make(container, getString(R.string.app_key_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), view -> {
                    mEmptyView.setVisibility(View.INVISIBLE);
                    mViewModel.getMeshNetworkLiveData().addAppKey(appKey);
                })
                .setActionTextColor(getResources().getColor(R.color.colorPrimaryDark))
                .show();
    }
}
