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

package no.nordicsemi.android.nrfmesh.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import no.nordicsemi.android.mesh.utils.AddressArray;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.mesh.utils.ProxyFilterType;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.adapter.FilterAddressAdapter1;
import no.nordicsemi.android.nrfmesh.databinding.DialogFragmentFilterAddressBinding;
import no.nordicsemi.android.nrfmesh.utils.HexKeyListener;
import no.nordicsemi.android.nrfmesh.utils.Utils;

public class DialogFragmentFilterAddAddress extends DialogFragment {

    private static final String PROXY_FILTER_KEY = "PROXY_FILTER";
    private DialogFragmentFilterAddressBinding binding;

    private ArrayList<AddressArray> addresses = new ArrayList<>();
    private ProxyFilterType filterType;

    public interface DialogFragmentFilterAddressListener {
        void addAddresses(final List<AddressArray> addresses);
    }

    public static DialogFragmentFilterAddAddress newInstance(final ProxyFilterType filterType) {
        final DialogFragmentFilterAddAddress fragment = new DialogFragmentFilterAddAddress();
        final Bundle bundle = new Bundle();
        bundle.putParcelable(PROXY_FILTER_KEY, filterType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filterType = getArguments().getParcelable(PROXY_FILTER_KEY);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        binding = DialogFragmentFilterAddressBinding.inflate(getLayoutInflater());

        if (savedInstanceState != null) {
            filterType = savedInstanceState.getParcelable(PROXY_FILTER_KEY);
            addresses = savedInstanceState.getParcelableArrayList("AddressList");
        }

        final FilterAddressAdapter1 adapter = new FilterAddressAdapter1(requireContext(), addresses);
        binding.recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewAddresses.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerViewAddresses.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        binding.actionAdd.setOnClickListener(v -> {
            final String addressVal = binding.textInput.getEditableText().toString().trim();
            if (validateInput(addressVal)) {
                binding.textInput.getEditableText().clear();
                final byte[] address = MeshParserUtils.toByteArray(addressVal);
                addresses.add(new AddressArray(address[0], address[1]));
                adapter.notifyDataSetChanged();
            }
        });

        final KeyListener hexKeyListener = new HexKeyListener();
        binding.textInputLayout.setHint(getString((R.string.hint_filter_address)));
        binding.textInput.setKeyListener(hexKeyListener);
        binding.textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                binding.textInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        binding.summary.setText(getString(R.string.dialog_summary_filter_address, filterType.getFilterTypeName()));

        return new AlertDialog.Builder(requireContext()).setView(binding.getRoot())
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    if (!addresses.isEmpty()) {
                        ((DialogFragmentFilterAddressListener) requireParentFragment()).addAddresses(addresses);
                    } else {
                        Toast.makeText(requireContext(), R.string.error_empty_filter_address, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setIcon(R.drawable.ic_lan_24dp)
                .setTitle(R.string.title_add_address).create();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PROXY_FILTER_KEY, filterType);
        outState.putParcelableArrayList("AddressList", addresses);
    }

    private boolean validateInput(final String input) {
        try {
            if (input.length() % 4 != 0 || !input.matches(Utils.HEX_PATTERN)) {
                binding.textInputLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }

            final byte[] address = MeshParserUtils.toByteArray(input);
            if (!MeshAddress.isValidProxyFilterAddress(address)) {
                binding.textInputLayout.setError(getString(R.string.invalid_filter_address));
                return false;
            }
        } catch (IllegalArgumentException ex) {
            binding.textInputLayout.setError(ex.getMessage());
            return false;
        }

        return true;
    }
}
