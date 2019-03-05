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

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.GroupAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentCreateGroup;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SharedViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class GroupsFragment extends Fragment implements Injectable,
        ItemTouchHelperAdapter,
        GroupAdapter.OnItemClickListener,
        DialogFragmentCreateGroup.DialogFragmentCreateGroupListener {

    private static final String TAG = GroupsFragment.class.getSimpleName();

    private SharedViewModel mViewModel;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.container)
    View container;
    @BindView(R.id.fab_add_group)
    FloatingActionButton fab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_groups, null);
        ButterKnife.bind(this, rootView);

        mViewModel = ViewModelProviders.of(requireActivity(), mViewModelFactory).get(SharedViewModel.class);

        final View noGroupsConfiguredView = rootView.findViewById(R.id.no_groups_configured);

        // Configure the recycler view
        final RecyclerView recyclerViewGroups = rootView.findViewById(R.id.recycler_view_groups);
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewGroups.getContext(), DividerItemDecoration.VERTICAL);
        recyclerViewGroups.addItemDecoration(dividerItemDecoration);
        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewGroups);
        final GroupAdapter adapter = new GroupAdapter(requireContext());
        adapter.setOnItemClickListener(this);
        recyclerViewGroups.setAdapter(adapter);

        mViewModel.getMeshNetworkLiveData().observe(this, meshNetworkLiveData -> {
            if (meshNetworkLiveData != null) {
                if(meshNetworkLiveData.getMeshNetwork().getGroups().isEmpty()){
                    noGroupsConfiguredView.setVisibility(View.VISIBLE);
                } else {
                    noGroupsConfiguredView.setVisibility(View.INVISIBLE);
                }
            }
        });

        mViewModel.getGroups().observe(this, groups -> {
            final MeshNetwork network = mViewModel.getMeshNetworkLiveData().getMeshNetwork();
            adapter.updateAdapter(network, groups);
        });

        fab.setOnClickListener(v -> {
            DialogFragmentCreateGroup fragmentCreateGroup = DialogFragmentCreateGroup.newInstance();
            fragmentCreateGroup.show(getChildFragmentManager(), null);
        });

        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (mViewModel.getProvisionedNodes().getValue() != null && !mViewModel.getProvisionedNodes().getValue().isEmpty()) {
            final Boolean isConnectedToNetwork = mViewModel.isConnectedToProxy().getValue();
            if (isConnectedToNetwork != null && isConnectedToNetwork) {
                inflater.inflate(R.menu.disconnect, menu);
            } else {
                inflater.inflate(R.menu.connect, menu);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_connect:
                final Intent intent = new Intent(requireActivity(), ScannerActivity.class);
                intent.putExtra(Utils.EXTRA_DATA_PROVISIONING_SERVICE, false);
                startActivity(intent);
                return true;
            case R.id.action_disconnect:
                mViewModel.disconnect();
                return true;
        }
        return false;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
    }

    @Override
    public void onItemClick(final int address) {
        mViewModel.setSelectedGroup(address);
        startActivity(new Intent(requireContext(), GroupControlsActivity.class));
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        final MeshNetwork network = mViewModel.getMeshNetworkLiveData().getMeshNetwork();
        final Group group = network.getGroups().get(position);
        if(network.getModels(group).size() == 0) {
            network.removeGroup(group);
            final String message = getString(R.string.group_deleted, group.getName());
            displaySnackBar(message);
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {
        final String message = getString(R.string.error_group_unsubscribe_to_delete);
        displaySnackBar(message);
    }


    private void displaySnackBar(final String message){
        Snackbar.make(container, message, Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.colorPrimaryDark ))
                .show();
    }

    @Override
    public boolean createGroup(@NonNull final String name, final int address) {
        final MeshNetwork network = mViewModel.getMeshNetworkLiveData().getMeshNetwork();
        return network.addGroup(address, name);
    }
}
