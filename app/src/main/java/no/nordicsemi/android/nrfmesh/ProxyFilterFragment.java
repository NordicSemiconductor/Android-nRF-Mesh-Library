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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.ProxyConfigAddAddressToFilter;
import no.nordicsemi.android.mesh.transport.ProxyConfigRemoveAddressFromFilter;
import no.nordicsemi.android.mesh.transport.ProxyConfigSetFilterType;
import no.nordicsemi.android.mesh.utils.AddressArray;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.ProxyFilter;
import no.nordicsemi.android.mesh.utils.ProxyFilterType;
import no.nordicsemi.android.nrfmesh.adapter.FilterAddressAdapter;
import no.nordicsemi.android.nrfmesh.databinding.FragmentProxyFilterBinding;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentFilterAddAddress;
import no.nordicsemi.android.nrfmesh.viewmodels.SharedViewModel;
import no.nordicsemi.android.nrfmesh.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmesh.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

import static android.view.View.VISIBLE;

@AndroidEntryPoint
public class ProxyFilterFragment extends Fragment implements
        DialogFragmentFilterAddAddress.DialogFragmentFilterAddressListener,
        ItemTouchHelperAdapter {

    private static final String CLEAR_ADDRESS_PRESSED = "CLEAR_ADDRESS_PRESSED";
    private static final String PROXY_FILTER_DISABLED = "PROXY_FILTER_DISABLED";

    private SharedViewModel mViewModel;

    private ProxyFilter mFilter;
    private boolean clearAddressPressed;
    private boolean isProxyFilterDisabled;
    private FilterAddressAdapter addressAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup viewGroup, @Nullable final Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        final FragmentProxyFilterBinding binding = FragmentProxyFilterBinding.inflate(getLayoutInflater());
        final Button actionEnableWhiteList = binding.actionWhiteList;
        final Button actionEnableBlackList = binding.actionBlackList;
        final Button actionDisable = binding.actionDisable;
        final Button actionAddFilterAddress = binding.actionAddAddress;
        final Button actionClearFilterAddress = binding.actionClearAddresses;

        if (savedInstanceState != null) {
            clearAddressPressed = savedInstanceState.getBoolean(CLEAR_ADDRESS_PRESSED);
            isProxyFilterDisabled = savedInstanceState.getBoolean(PROXY_FILTER_DISABLED);
        }

        final TextView noAddressesAdded = binding.noAddresses;
        final RecyclerView recyclerViewAddresses = binding.recyclerViewFilterAddresses;
        actionEnableWhiteList.setEnabled(false);
        actionEnableBlackList.setEnabled(false);
        actionDisable.setEnabled(false);

        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewAddresses.setItemAnimator(new DefaultItemAnimator());
        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewAddresses);
        addressAdapter = new FilterAddressAdapter();
        recyclerViewAddresses.setAdapter(addressAdapter);

        mViewModel.isConnectedToProxy().observe(getViewLifecycleOwner(), isConnected -> {
            if (!isConnected) {
                clearAddressPressed = false;
                final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
                if (network != null) {
                    mFilter = network.getProxyFilter();
                    if (mFilter == null) {
                        addressAdapter.clearData();
                        noAddressesAdded.setVisibility(View.VISIBLE);
                        recyclerViewAddresses.setVisibility(View.GONE);
                    }
                }

                actionEnableWhiteList.setSelected(isConnected);
                actionEnableBlackList.setSelected(isConnected);
                actionDisable.setSelected(isConnected);
                actionAddFilterAddress.setEnabled(isConnected);
                actionClearFilterAddress.setVisibility(View.GONE);
            }
            actionDisable.setEnabled(false);
            actionEnableWhiteList.setEnabled(isConnected);
            actionEnableBlackList.setEnabled(isConnected);
        });

        mViewModel.getNetworkLiveData().observe(getViewLifecycleOwner(), meshNetworkLiveData -> {
            final MeshNetwork network = meshNetworkLiveData.getMeshNetwork();
            if (network == null) {
                return;
            }

            final ProxyFilter filter = mFilter = network.getProxyFilter();
            if (filter == null) {
                addressAdapter.clearData();
                return;
            } else if (clearAddressPressed) {
                clearAddressPressed = false;
                return;
            } else if (isProxyFilterDisabled) {
                actionDisable.setSelected(true);
            }

            actionEnableWhiteList.setSelected(mFilter.getFilterType().getType() == ProxyFilterType.INCLUSION_LIST_FILTER && !actionDisable.isSelected());
            actionEnableBlackList.setSelected(mFilter.getFilterType().getType() == ProxyFilterType.EXCLUSION_LIST_FILTER);

            if (!mFilter.getAddresses().isEmpty()) {
                noAddressesAdded.setVisibility(View.GONE);
                actionClearFilterAddress.setVisibility(View.VISIBLE);
                recyclerViewAddresses.setVisibility(VISIBLE);
            } else {
                recyclerViewAddresses.setVisibility(View.GONE);
                noAddressesAdded.setVisibility(View.VISIBLE);
                actionClearFilterAddress.setVisibility(View.GONE);
            }
            actionAddFilterAddress.setEnabled(!actionDisable.isSelected());
            addressAdapter.updateData(filter);
        });

        actionEnableWhiteList.setOnClickListener(v -> {
            isProxyFilterDisabled = false;
            v.setSelected(true);
            actionEnableBlackList.setSelected(false);
            actionDisable.setSelected(false);
            actionDisable.setEnabled(true);
            setFilter(new ProxyFilterType(ProxyFilterType.INCLUSION_LIST_FILTER));
        });

        actionEnableBlackList.setOnClickListener(v -> {
            isProxyFilterDisabled = false;
            v.setSelected(true);
            actionEnableWhiteList.setSelected(false);
            actionDisable.setSelected(false);
            actionDisable.setEnabled(true);
            setFilter(new ProxyFilterType(ProxyFilterType.EXCLUSION_LIST_FILTER));
        });

        actionDisable.setOnClickListener(v -> {
            v.setSelected(true);
            isProxyFilterDisabled = true;
            actionEnableWhiteList.setSelected(false);
            actionEnableBlackList.setSelected(false);
            addressAdapter.clearData();
            actionDisable.setEnabled(false);
            setFilter(new ProxyFilterType(ProxyFilterType.INCLUSION_LIST_FILTER));
        });

        actionAddFilterAddress.setOnClickListener(v -> {
            final ProxyFilterType filterType;
            if (mFilter == null) {
                filterType = new ProxyFilterType(ProxyFilterType.INCLUSION_LIST_FILTER);
            } else {
                filterType = mFilter.getFilterType();
            }
            final DialogFragmentFilterAddAddress filterAddAddress = DialogFragmentFilterAddAddress.newInstance(filterType);
            filterAddAddress.show(getChildFragmentManager(), null);
        });

        actionClearFilterAddress.setOnClickListener(v -> removeAddresses());

        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(CLEAR_ADDRESS_PRESSED, clearAddressPressed);
        outState.putBoolean(PROXY_FILTER_DISABLED, isProxyFilterDisabled);
    }

    @Override
    public void addAddresses(final List<AddressArray> addresses) {
        final ProxyConfigAddAddressToFilter addAddressToFilter = new ProxyConfigAddAddressToFilter(addresses);
        sendMessage(addAddressToFilter);
    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        if (viewHolder instanceof FilterAddressAdapter.ViewHolder) {
            removeAddress(position);
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {

    }

    private void removeAddress(final int position) {
        final MeshNetwork meshNetwork = mViewModel.getNetworkLiveData().getMeshNetwork();
        if (meshNetwork != null) {
            final ProxyFilter proxyFilter = meshNetwork.getProxyFilter();
            if (proxyFilter != null) {
                clearAddressPressed = true;
                final AddressArray addressArr = proxyFilter.getAddresses().get(position);
                final List<AddressArray> addresses = new ArrayList<>();
                addresses.add(addressArr);
                addressAdapter.clearRow(position);
                final ProxyConfigRemoveAddressFromFilter removeAddressFromFilter = new ProxyConfigRemoveAddressFromFilter(addresses);
                sendMessage(removeAddressFromFilter);
            }
        }
    }

    private void removeAddresses() {
        final MeshNetwork meshNetwork = mViewModel.getNetworkLiveData().getMeshNetwork();
        if (meshNetwork != null) {
            final ProxyFilter proxyFilter = meshNetwork.getProxyFilter();
            if (proxyFilter != null) {
                if (!proxyFilter.getAddresses().isEmpty()) {
                    final ProxyConfigRemoveAddressFromFilter removeAddressFromFilter = new ProxyConfigRemoveAddressFromFilter(proxyFilter.getAddresses());
                    sendMessage(removeAddressFromFilter);
                }
            }
        }
    }

    private void setFilter(final ProxyFilterType filterType) {
        final ProxyConfigSetFilterType setFilterType = new ProxyConfigSetFilterType(filterType);
        sendMessage(setFilterType);
    }

    private void sendMessage(final MeshMessage meshMessage) {
        try {
            mViewModel.getMeshManagerApi().createMeshPdu(MeshAddress.UNASSIGNED_ADDRESS, meshMessage);
        } catch (IllegalArgumentException ex) {
            final DialogFragmentError message = DialogFragmentError.
                    newInstance(getString(R.string.title_error), ex.getMessage());
            message.show(getChildFragmentManager(), null);
        }
    }
}
