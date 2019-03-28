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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.GroupAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentCreateGroup;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SharedViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

public class GroupsFragment extends Fragment implements Injectable,
        ItemTouchHelperAdapter,
        GroupAdapter.OnItemClickListener,
        DialogFragmentCreateGroup.DialogFragmentCreateGroupListener {

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        final View rootView = inflater.inflate(R.layout.fragment_groups, null);
        mViewModel = ViewModelProviders.of(requireActivity(), mViewModelFactory).get(SharedViewModel.class);
        ButterKnife.bind(this, rootView);

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
                if (meshNetworkLiveData.getMeshNetwork().getGroups().isEmpty()) {
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
    public void onItemClick(final int address) {
        mViewModel.setSelectedGroup(address);
        startActivity(new Intent(requireContext(), GroupControlsActivity.class));
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        final MeshNetwork network = mViewModel.getMeshNetworkLiveData().getMeshNetwork();
        final Group group = network.getGroups().get(position);
        if (network.getModels(group).size() == 0) {
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

    @Override
    public boolean createGroup(@NonNull final String name, final int address) {
        final MeshNetwork network = mViewModel.getMeshNetworkLiveData().getMeshNetwork();
        return network.addGroup(address, name);
    }

    private void displaySnackBar(final String message){
        Snackbar.make(container, message, Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.colorPrimaryDark ))
                .show();
    }
}
