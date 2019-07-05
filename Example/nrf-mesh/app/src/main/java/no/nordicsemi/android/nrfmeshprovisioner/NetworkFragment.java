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

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentConfigError;
import no.nordicsemi.android.nrfmeshprovisioner.node.NodeConfigurationActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.adapter.NodeAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SharedViewModel;

import static android.app.Activity.RESULT_OK;

public class NetworkFragment extends Fragment implements Injectable,
        NodeAdapter.OnItemClickListener {

    private SharedViewModel mViewModel;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.main_content)
    View container;
    @BindView(R.id.recycler_view_provisioned_nodes)
    RecyclerView mRecyclerViewNodes;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View rootView = inflater.inflate(R.layout.fragment_network, null);
        mViewModel = ViewModelProviders.of(requireActivity(), mViewModelFactory).get(SharedViewModel.class);
        ButterKnife.bind(this, rootView);

        final ExtendedFloatingActionButton fab = rootView.findViewById(R.id.fab_add_node);
        final View noNetworksConfiguredView = rootView.findViewById(R.id.no_networks_configured);

        // Configure the recycler view
        final NodeAdapter nodeAdapter = new NodeAdapter(requireContext(), mViewModel.getNodes());
        nodeAdapter.setOnItemClickListener(this);
        mRecyclerViewNodes.setLayoutManager(new LinearLayoutManager(getContext()));
        final DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        mRecyclerViewNodes.addItemDecoration(decoration);
        mRecyclerViewNodes.setAdapter(nodeAdapter);

        // Create view model containing utility methods for scanning
        mViewModel.getNodes().observe(this, nodes -> {
            if (nodes != null && !nodes.isEmpty()) {
                noNetworksConfiguredView.setVisibility(View.GONE);
            } else {
                noNetworksConfiguredView.setVisibility(View.VISIBLE);
            }
            requireActivity().invalidateOptionsMenu();
        });

        mViewModel.isConnectedToProxy().observe(this, isConnected -> {
            if (isConnected != null) {
                requireActivity().invalidateOptionsMenu();
            }
        });

        mRecyclerViewNodes.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        fab.setOnClickListener(v ->
                mViewModel.navigateToScannerActivity(requireActivity(), true, Utils.PROVISIONING_SUCCESS, true));

        return rootView;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConfigureClicked(final ProvisionedMeshNode node) {
        mViewModel.setSelectedMeshNode(node);
        final Intent meshConfigurationIntent = new Intent(getActivity(), NodeConfigurationActivity.class);
        requireActivity().startActivity(meshConfigurationIntent);
    }

    private void handleActivityResult(final int requestCode, final int resultCode, @NonNull final Intent data) {
        if (requestCode == Utils.PROVISIONING_SUCCESS) {
            if (resultCode == RESULT_OK) {
                final boolean provisioningSuccess = data.getBooleanExtra(Utils.PROVISIONING_COMPLETED, false);
                final DialogFragmentConfigError fragmentConfigError;
                if (provisioningSuccess) {
                    final boolean provisionerUnassigned = data.getBooleanExtra(Utils.PROVISIONER_UNASSIGNED, false);
                    if (provisionerUnassigned) {
                        fragmentConfigError =
                                DialogFragmentConfigError.newInstance(getString(R.string.title_init_config_error)
                                        , getString(R.string.provisioner_unassigned_msg));
                        fragmentConfigError.show(getChildFragmentManager(), null);
                    } else {
                        final boolean compositionDataReceived = data.getBooleanExtra(Utils.COMPOSITION_DATA_COMPLETED, false);
                        final boolean appKeyAddCompleted = data.getBooleanExtra(Utils.APP_KEY_ADD_COMPLETED, false);
                        final boolean networkRetransmitSetCompleted = data.getBooleanExtra(Utils.NETWORK_TRANSMIT_SET_COMPLETED, false);
                        if (compositionDataReceived) {
                            if (appKeyAddCompleted) {
                                if (!networkRetransmitSetCompleted) {
                                    fragmentConfigError =
                                            DialogFragmentConfigError.newInstance(getString(R.string.title_init_config_error)
                                                    , getString(R.string.init_config_error_net_transmit_msg));
                                    fragmentConfigError.show(getChildFragmentManager(), null);
                                }
                            } else {
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
                }
                requireActivity().invalidateOptionsMenu();
            }
        }
    }
}
