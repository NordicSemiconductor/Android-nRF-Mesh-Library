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

package no.nordicsemi.android.nrfmesh;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.nrfmesh.adapter.GroupAdapter;
import no.nordicsemi.android.nrfmesh.databinding.FragmentGroupsBinding;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentCreateGroup;
import no.nordicsemi.android.nrfmesh.viewmodels.SharedViewModel;
import no.nordicsemi.android.nrfmesh.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmesh.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

public class GroupsFragment extends Fragment implements
        ItemTouchHelperAdapter,
        GroupAdapter.OnItemClickListener,
        GroupCallbacks {
    private FragmentGroupsBinding binding;
    private SharedViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup viewGroup, @Nullable final Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        binding = FragmentGroupsBinding.inflate(getLayoutInflater());

        final ExtendedFloatingActionButton fab = binding.fabAddGroup;

        // Configure the recycler view
        final RecyclerView recyclerViewGroups = binding.recyclerViewGroups;
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewGroups.getContext(), DividerItemDecoration.VERTICAL);
        recyclerViewGroups.addItemDecoration(dividerItemDecoration);
        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewGroups);
        final GroupAdapter adapter = new GroupAdapter(requireContext());
        adapter.setOnItemClickListener(this);
        recyclerViewGroups.setAdapter(adapter);

        mViewModel.getNetworkLiveData().observe(getViewLifecycleOwner(), meshNetworkLiveData -> {
            if (meshNetworkLiveData != null) {
                final MeshNetwork network = meshNetworkLiveData.getMeshNetwork();
                if (network.getGroups().isEmpty()) {
                    binding.empty.getRoot().setVisibility(View.VISIBLE);
                } else {
                    binding.empty.getRoot().setVisibility(View.INVISIBLE);
                }
                adapter.updateAdapter(network, network.getGroups());
            }
        });

        fab.setOnClickListener(v -> {
            DialogFragmentCreateGroup fragmentCreateGroup = DialogFragmentCreateGroup.newInstance();
            fragmentCreateGroup.show(getChildFragmentManager(), null);
        });

        recyclerViewGroups.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final LinearLayoutManager m = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (m != null) {
                    if (m.findFirstCompletelyVisibleItemPosition() == 0) {
                        fab.extend();
                    } else {
                        fab.shrink();
                    }
                }
            }
        });

        return binding.getRoot();

    }

    @Override
    public void onItemClick(final int address) {
        mViewModel.setSelectedGroup(address);
        startActivity(new Intent(requireContext(), GroupControlsActivity.class));
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        final Group group = network.getGroups().get(position);
        if (network.getModels(group).size() == 0) {
            network.removeGroup(group);
            displaySnackBar(group);
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {
        final String message = getString(R.string.error_group_unsubscribe_to_delete);
        mViewModel.displaySnackBar(requireActivity(), binding.container, message, Snackbar.LENGTH_LONG);
    }

    @Override
    public Group createGroup() {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        return network.createGroup(network.getSelectedProvisioner(), "Mesh Group");
    }

    @Override
    public Group createGroup(@NonNull final String name) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        return network.createGroup(network.getSelectedProvisioner(), name);
    }

    @Override
    public Group createGroup(@NonNull final UUID uuid, final String name) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        return network.createGroup(uuid, null, name);
    }

    @Override
    public boolean onGroupAdded(@NonNull final String name, final int address) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        final Group group = network.createGroup(network.getSelectedProvisioner(), address, name);
        if (group != null) {
            return network.addGroup(group);
        }
        return false;
    }

    @Override
    public boolean onGroupAdded(@NonNull final Group group) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        return network.addGroup(group);
    }

    private void displaySnackBar(final Group group) {
        final String message = getString(R.string.group_deleted, group.getName());
        Snackbar.make(binding.container, message, Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.colorSecondary))
                .setAction(R.string.undo, v -> {
                    binding.empty.getRoot().setVisibility(View.INVISIBLE);
                    final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
                    if (network != null) {
                        network.addGroup(group);
                    }

                })
                .show();
    }
}
