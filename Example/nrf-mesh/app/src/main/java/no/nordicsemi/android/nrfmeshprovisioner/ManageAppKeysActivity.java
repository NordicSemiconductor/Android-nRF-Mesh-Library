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
import android.content.ComponentName;
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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.ProvisioningSettings;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ManageAppKeyAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentAddAppKey;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentEditAppKey;
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
    private static final String CALLING_ACTIVITY = ".MainActivity";

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

        //If the component name is not null then we know that the activity requested a result
        final ComponentName componentName = getCallingActivity();

        //Bind ui
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final FloatingActionButton fab = findViewById(R.id.fab);
        if(componentName != null && componentName.getShortClassName().equals(CALLING_ACTIVITY)) {
            getSupportActionBar().setTitle(R.string.title_manage_app_keys);
        } else {
            getSupportActionBar().setTitle(R.string.title_select_app_key);
            fab.setVisibility(View.GONE);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final RecyclerView appKeysRecyclerView = findViewById(R.id.recycler_view_app_keys);
        appKeysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(appKeysRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        appKeysRecyclerView.addItemDecoration(dividerItemDecoration);
        appKeysRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ManageAppKeyAdapter(this, mViewModel.getProvisioningSettingsLiveData());
        mAdapter.setOnItemClickListener(this);
        appKeysRecyclerView.setAdapter(mAdapter);

        fab.setOnClickListener(v -> {
            final DialogFragmentAddAppKey dialogFragmentAddAppKey = DialogFragmentAddAppKey.newInstance(null);
            dialogFragmentAddAppKey.show(getSupportFragmentManager(), null);
        });
        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(appKeysRecyclerView);

        mViewModel.getProvisioningSettingsLiveData().observe(this, provisioningLiveData -> {
            final ProvisioningSettings settings = provisioningLiveData.getProvisioningSettings();
            if(settings != null) {
                mEmptyView.setVisibility(settings.getAppKeys().isEmpty() ? View.VISIBLE : View.GONE);
            }
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
        final ComponentName componentName = getCallingActivity();
        if(componentName != null && componentName.getShortClassName().equals(CALLING_ACTIVITY)) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(RESULT_APP_KEY_LIST_SIZE, mAdapter.getItemCount());
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onItemClick(final int position, final String appKey) {
        final ComponentName componentName = getCallingActivity();
        if(componentName != null && componentName.getShortClassName().equals(CALLING_ACTIVITY)) {
            final DialogFragmentEditAppKey dialogFragmentEditAppKey = DialogFragmentEditAppKey.newInstance(position, appKey);
            dialogFragmentEditAppKey.show(getSupportFragmentManager(), null);
        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(RESULT_APP_KEY_INDEX, position);
            returnIntent.putExtra(RESULT_APP_KEY, appKey);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    @Override
    public void onAppKeysUpdated(final int position, final String appKey) {
        mViewModel.getProvisioningSettingsLiveData().updateAppKey(position, appKey);
    }

    @Override
    public void onAppKeyAdded(final String appKey) {
        mViewModel.getProvisioningSettingsLiveData().addAppKey(appKey);
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final TextView textView = viewHolder.getSwipeableView().findViewById(R.id.app_key);
        final String appKey = textView.getText().toString();
        mViewModel.getProvisioningSettingsLiveData().removeAppKey(appKey);
        displaySnackBar(viewHolder.getAdapterPosition(), appKey);
        // Show the empty view
        final boolean empty = mAdapter.getItemCount() == 0;
        if (empty) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }


    private void displaySnackBar(final int key, final String appKey){

        Snackbar.make(container, getString(R.string.app_key_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), view -> {
                    mEmptyView.setVisibility(View.INVISIBLE);
                    mViewModel.getProvisioningSettingsLiveData().addAppKey(key, appKey);
                })
                .setActionTextColor(getResources().getColor(R.color.colorPrimaryDark ))
                .show();
    }
}
