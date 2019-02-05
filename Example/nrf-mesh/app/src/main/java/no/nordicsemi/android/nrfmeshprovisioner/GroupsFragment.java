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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.GroupAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SharedViewModel;

public class GroupsFragment extends Fragment implements Injectable, FragmentManager.OnBackStackChangedListener, GroupAdapter.OnItemClickListener {

    private static final String TAG = GroupsFragment.class.getSimpleName();

    private SharedViewModel mViewModel;
    private FloatingActionButton fab;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;


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

        requireActivity().getSupportFragmentManager().addOnBackStackChangedListener(this);

        final View noGroupsConfiguredView = rootView.findViewById(R.id.no_groups_configured);

        // Configure the recycler view
        final RecyclerView recyclerViewDevices = rootView.findViewById(R.id.recycler_view_groups);
        recyclerViewDevices.setLayoutManager(new LinearLayoutManager(requireContext()));
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewDevices.getContext(), DividerItemDecoration.VERTICAL);
        recyclerViewDevices.addItemDecoration(dividerItemDecoration);
        final MeshNetwork network = mViewModel.getMeshNetworkLiveData().getMeshNetwork();
        final GroupAdapter adapter = new GroupAdapter(requireActivity(), network, mViewModel.getGroups());
        adapter.setOnItemClickListener(this);
        recyclerViewDevices.setAdapter(adapter);

        mViewModel.getMeshNetworkLiveData().observe(this, meshNetworkLiveData -> {
            if (meshNetworkLiveData != null) {
                if(meshNetworkLiveData.getMeshNetwork().getGroups().isEmpty()){
                    noGroupsConfiguredView.setVisibility(View.VISIBLE);
                } else {
                    noGroupsConfiguredView.setVisibility(View.INVISIBLE);
                }
            }
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
    }

    @Override
    public void onBackStackChanged() {
        if(requireActivity().getSupportFragmentManager().getFragments().size() == 3) {
            fab.show();
        }
    }

    @Override
    public void onItemClick(final byte[] address) {
        mViewModel.setSelectedGroup(address);
        startActivity(new Intent(requireContext(), GroupControlsActivity.class));
    }
}
