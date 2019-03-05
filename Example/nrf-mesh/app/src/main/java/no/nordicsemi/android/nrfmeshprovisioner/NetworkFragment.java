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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.NodeAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigError;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SharedViewModel;

import static android.app.Activity.RESULT_OK;

public class NetworkFragment extends Fragment implements Injectable,
        NodeAdapter.OnItemClickListener {

    private static final String TAG_SCANNER_FRAGMENT = "SCANNER_FRAGMENT";
    private SharedViewModel mViewModel;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.main_content)
    View container;
    @BindView(R.id.recycler_view_provisioned_nodes)
    RecyclerView mRecyclerViewNodes;

    private NodeAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_network, null);
        ButterKnife.bind(this, rootView);

        final FloatingActionButton fab = rootView.findViewById(R.id.fab_add_node);
        final View noNetworksConfiguredView = rootView.findViewById(R.id.no_networks_configured);

        mViewModel = ViewModelProviders.of(requireActivity(), mViewModelFactory).get(SharedViewModel.class);

        // Configure the recycler view
        mAdapter = new NodeAdapter(getActivity(), mViewModel.getProvisionedNodes());
        mAdapter.setOnItemClickListener(this);
        mRecyclerViewNodes.setLayoutManager(new LinearLayoutManager(getContext()));
        final DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        mRecyclerViewNodes.addItemDecoration(decoration);
        mRecyclerViewNodes.setAdapter(mAdapter);

        // Create view model containing utility methods for scanning
        mViewModel.getProvisionedNodes().observe(this, nodes -> {
            if (!nodes.isEmpty()) {
                noNetworksConfiguredView.setVisibility(View.GONE);
            } else {
                noNetworksConfiguredView.setVisibility(View.VISIBLE);
            }
            mAdapter.notifyDataSetChanged();
        });

        mViewModel.getProvisionedNodes().observe(this, provisionedNodes -> requireActivity().invalidateOptionsMenu());

        mViewModel.isConnectedToProxy().observe(this, isConnected -> {
            if (isConnected != null) {
                requireActivity().invalidateOptionsMenu();
            }
        });

        mViewModel.getConnectedMeshNodeAddress().observe(this, unicastAddress -> mAdapter.selectConnectedMeshNode(unicastAddress));

        fab.setOnClickListener(v -> {
            final Intent intent = new Intent(requireActivity(), ScannerActivity.class);
            intent.putExtra(Utils.EXTRA_DATA_PROVISIONING_SERVICE, true);
            startActivityForResult(intent, Utils.PROVISIONING_SUCCESS);
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
                startActivityForResult(intent, Utils.CONNECT_TO_NETWORK);
                return true;
            case R.id.action_disconnect:
                mViewModel.disconnect();
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == Utils.PROVISIONING_SUCCESS) {
            if (resultCode == RESULT_OK) {
                final boolean provisioningSuccess = data.getBooleanExtra(Utils.PROVISIONING_COMPLETED, false);
                if (provisioningSuccess) {
                    final boolean compositionDataReceived = data.getBooleanExtra(Utils.COMPOSITION_DATA_COMPLETED, false);
                    final boolean appKeyAddCompleted = data.getBooleanExtra(Utils.APP_KEY_ADD_COMPLETED, false);
                    final DialogFragmentConfigError fragmentConfigError;
                    if(compositionDataReceived){
                        if(!appKeyAddCompleted){
                            fragmentConfigError =
                                    DialogFragmentConfigError.newInstance(getString(R.string.title_init_config_error)
                                            , getString(R.string.init_config_error_app_key_msg));
                            fragmentConfigError.show(getChildFragmentManager(), null);
                        }
                    } else {
                        fragmentConfigError =
                                DialogFragmentConfigError.newInstance(getString(R.string.title_init_config_error)
                                        , getString(R.string.init_config_error_all));
                        fragmentConfigError.show(getChildFragmentManager(), null);
                    }
                }
                requireActivity().invalidateOptionsMenu();
            }
        }
    }

    @Override
    public void onConfigureClicked(final ProvisionedMeshNode node) {
        final Boolean isConnectedToProxy = mViewModel.isConnectedToProxy().getValue();
        if (isConnectedToProxy != null && isConnectedToProxy) {
            mViewModel.setSelectedMeshNode(node);
            final Intent meshConfigurationIntent = new Intent(getActivity(), NodeConfigurationActivity.class);
            requireActivity().startActivity(meshConfigurationIntent);
        } else {
            displaySnackBar(getString(R.string.disconnected_network_rationale));
        }
    }

    @Override
    public void onDetailsClicked(final ProvisionedMeshNode node) {
        final Intent meshConfigurationIntent = new Intent(getActivity(), NodeDetailsActivity.class);
        meshConfigurationIntent.putExtra(Utils.EXTRA_DEVICE, node);
        requireActivity().startActivity(meshConfigurationIntent);
    }


    private void displaySnackBar(final String message){
        Snackbar.make(container, message, Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.colorPrimaryDark ))
                .show();
    }
}
