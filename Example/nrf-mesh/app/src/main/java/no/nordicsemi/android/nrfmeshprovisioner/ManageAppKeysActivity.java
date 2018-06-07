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
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.AppKeyAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentAddAppKey;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentEditAppKey;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class ManageAppKeysActivity extends AppCompatActivity implements AppKeyAdapter.OnItemClickListener,
        DialogFragmentAddAppKey.DialogFragmentAddAppKeysListener,
        DialogFragmentEditAppKey.DialogFragmentEditAppKeysListener,
        ItemTouchHelperAdapter {

    public static final String RESULT = "RESULT";
    public static final String APP_KEYS = "APP_KEYS";
    public static final int SELECT_APP_KEY = 2011; //Random number

    //UI Bindings
    @BindView(android.R.id.empty)
    View mEmptyView;
    @BindView(R.id.container)
    View container;

    private SparseArray<String> mAppKeysMap = new SparseArray<>();
    private AppKeyAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_app_keys);
        final ArrayList<String> tempAppKeys = getIntent().getStringArrayListExtra(APP_KEYS);
        populateAppKeysMap(tempAppKeys);
        //If the component name is not null then we know that the activity requested a result
        final ComponentName componentName = getCallingActivity();

        //Bind ui
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(componentName == null) {
            getSupportActionBar().setTitle(R.string.title_manage_app_keys);
        } else {
            getSupportActionBar().setTitle(R.string.title_select_app_key);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final RecyclerView appKeysRecyclerView = findViewById(R.id.recycler_view_app_keys);
        appKeysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(appKeysRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        appKeysRecyclerView.addItemDecoration(dividerItemDecoration);
        appKeysRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new AppKeyAdapter(this, mAppKeysMap);
        mAdapter.setOnItemClickListener(this);
        appKeysRecyclerView.setAdapter(mAdapter);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            final DialogFragmentAddAppKey dialogFragmentAddAppKey = DialogFragmentAddAppKey.newInstance(null);
            dialogFragmentAddAppKey.show(getSupportFragmentManager(), null);
        });
        if(componentName != null) {
            fab.setVisibility(View.GONE);
        }
        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(appKeysRecyclerView);
        final boolean empty = mAdapter.getItemCount() == 0;
        mEmptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
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
        super.onBackPressed();

    }

    @Override
    public void onItemClick(final int position, final String appKey) {
        if(getCallingActivity() == null) {
            final DialogFragmentEditAppKey dialogFragmentEditAppKey = DialogFragmentEditAppKey.newInstance(position, appKey);
            dialogFragmentEditAppKey.show(getSupportFragmentManager(), null);
        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(RESULT, appKey);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    @Override
    public void onAppKeysUpdated(final int position, final String appKey) {
        mAppKeysMap.put(position, appKey);
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void onAppKeyAdded(final String appKey) {
        mAppKeysMap.put(mAppKeysMap.size(), appKey);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        final String appKey = mAppKeysMap.get(position);
        mAppKeysMap.remove(position);
        mAdapter.notifyItemRemoved(position);
        displaySnackBar(position, appKey);
        // Show the empty view
        final boolean empty = mAdapter.getItemCount() == 0;
        if (empty) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private void populateAppKeysMap(final ArrayList<String> tempAppKeys){
        for ( int i=0; i < tempAppKeys.size(); i++ ) {
            mAppKeysMap.put(i,tempAppKeys.get(i));
        }
    }

    private void displaySnackBar(final int position, final String appKey){

        Snackbar.make(container, getString(R.string.app_key_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), view -> {
                    mEmptyView.setVisibility(View.INVISIBLE);
                    mAppKeysMap.put(position, appKey);
                    mAdapter.notifyItemInserted(position);
                })
                .setActionTextColor(getResources().getColor(R.color.colorPrimaryDark ))
                .show();
    }
}
