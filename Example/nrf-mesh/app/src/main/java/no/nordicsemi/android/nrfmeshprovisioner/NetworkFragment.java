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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.NodeAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SharedViewModel;

public class NetworkFragment extends Fragment implements Injectable,
        NodeAdapter.OnItemClickListener {

    SharedViewModel mViewModel;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private NodeAdapter mAdapter;

    public interface NetworkFragmentListener {
        void onProvisionedMeshNodeSelected();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_network, null);
        // Configure the recycler view
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_provisioned_nodes);
        final View noNetworksConfiguredView = rootView.findViewById(R.id.no_networks_configured);

        mViewModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(SharedViewModel.class);

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if(isTablet){
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); //If its a tablet we use a grid layout with 2 columns
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        mAdapter = new NodeAdapter(getActivity(), mViewModel.getMeshRepository().getProvisionedNodesLiveData());
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);

        // Create view model containing utility methods for scanning
        mViewModel.getMeshRepository().getProvisionedNodesLiveData().observe(this, provisionedNodesLiveData -> {
            if(mAdapter.getItemCount() > 0) {
                noNetworksConfiguredView.setVisibility(View.GONE);
            } else {
                noNetworksConfiguredView.setVisibility(View.VISIBLE);
            }
            mAdapter.notifyDataSetChanged();
        });


        return rootView;

    }

    @Override
    public void onStart() {
        super.onStart();
        mViewModel.refreshProvisionedNodes();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if(!mViewModel.getProvisionedNodesLiveData().getProvisionedNodes().isEmpty()){

            if (!mViewModel.isConenctedToMesh()) {
                inflater.inflate(R.menu.connect, menu);
            } else {
                inflater.inflate(R.menu.disconnect, menu);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_connect:
                final Intent scannerActivity = new Intent(getContext(), ProvisionedNodesScannerActivity.class);
                scannerActivity.putExtra(ProvisionedNodesScannerActivity.NETWORK_ID, mViewModel.getNetworkId());
                startActivity(scannerActivity);
                return true;
            case R.id.action_disconnect:
                mViewModel.disconnect();
                return true;
        }
        return false;
    }

    @Override
    public void onConfigureClicked(final ProvisionedMeshNode node) {
        if(mViewModel.isConenctedToMesh()) {
            ((NetworkFragmentListener) getActivity()).onProvisionedMeshNodeSelected();
            final Intent meshConfigurationIntent = new Intent(getActivity(), NodeConfigurationActivity.class);
            meshConfigurationIntent.putExtra(Utils.EXTRA_DEVICE, node);
            getActivity().startActivity(meshConfigurationIntent);
        } else {
            Toast.makeText(getActivity(), R.string.disconnected_network_rationale, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetailsClicked(final ProvisionedMeshNode node) {
        final Intent meshConfigurationIntent = new Intent(getActivity(), NodeDetailsActivity.class);
        meshConfigurationIntent.putExtra(Utils.EXTRA_DEVICE, node);
        getActivity().startActivity(meshConfigurationIntent);
    }

}
