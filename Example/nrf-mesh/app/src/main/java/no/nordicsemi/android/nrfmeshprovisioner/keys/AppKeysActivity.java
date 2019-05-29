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

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ManageAppKeyAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.AppKeysViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class AppKeysActivity extends AppCompatActivity implements Injectable,
        ManageAppKeyAdapter.OnItemClickListener,
        ItemTouchHelperAdapter {

    public static final String RESULT_APP_KEY = "RESULT_APP_KEY";
    public static final String RESULT_APP_KEY_INDEX = "RESULT_APP_KEY_INDEX";
    public static final String RESULT_APP_KEY_LIST_SIZE = "RESULT_APP_KEY_LIST_SIZE";
    public static final String EDIT_APP_KEY = "EDIT_APP_KEY";
    public static final int SELECT_APP_KEY = 2011; //Random number
    public static final int MANAGE_APP_KEYS = 2012; //Random number

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    //UI Bindings
    @BindView(android.R.id.empty)
    View mEmptyView;
    @BindView(R.id.container)
    View container;

    private AppKeysViewModel mViewModel;
    private ManageAppKeyAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_keys);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(AppKeysViewModel.class);

        //Bind ui
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.title_manage_app_keys);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ExtendedFloatingActionButton fab = findViewById(R.id.fab);

        final RecyclerView appKeysRecyclerView = findViewById(R.id.recycler_view_keys);
        appKeysRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(appKeysRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        appKeysRecyclerView.addItemDecoration(dividerItemDecoration);
        appKeysRecyclerView.setItemAnimator(new DefaultItemAnimator());

        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(appKeysRecyclerView);
        mAdapter = new ManageAppKeyAdapter(this, mViewModel.getMeshNetworkLiveData());
        mAdapter.setOnItemClickListener(this);
        appKeysRecyclerView.setAdapter(mAdapter);
        setUpObserver();

        fab.setOnClickListener(v -> {
            final Intent intent = new Intent(this, AddAppKeyActivity.class);
            startActivity(intent);
        });

        appKeysRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final LinearLayoutManager m = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (m != null) {
                    if (m.findFirstCompletelyVisibleItemPosition() == 0) {
                        fab.extend(true);
                    } else {
                        fab.shrink(true);
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
    public void onItemClick(final int position, @NonNull final ApplicationKey appKey) {
        final Intent intent = new Intent(this, EditAppKeyActivity.class);
        intent.putExtra(EDIT_APP_KEY, appKey.getKeyIndex());
        startActivity(intent);
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final ApplicationKey key = (ApplicationKey) viewHolder.getSwipeableView().getTag();
        try {
            if (mViewModel.getMeshNetworkLiveData().removeAppKey(key)) {
                displaySnackBar(key);
                // Show the empty view
                final boolean empty = mAdapter.getItemCount() == 0;
                if (empty) {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception ex) {
            mAdapter.notifyDataSetChanged();
            displaySnackBar(ex.getMessage());
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {
        //Do nothing
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

    private void displaySnackBar(final ApplicationKey appKey) {
        Snackbar.make(container, getString(R.string.app_key_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), view -> {
                    mEmptyView.setVisibility(View.INVISIBLE);
                    mViewModel.getMeshNetworkLiveData().addAppKey(appKey);
                })
                .setActionTextColor(getResources().getColor(R.color.colorPrimaryDark))
                .show();
    }

    private void displaySnackBar(final String message) {
        Snackbar.make(container, message, Snackbar.LENGTH_LONG)
                .show();
    }
}
