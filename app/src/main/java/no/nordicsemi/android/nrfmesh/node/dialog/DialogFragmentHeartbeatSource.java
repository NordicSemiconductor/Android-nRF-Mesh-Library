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

package no.nordicsemi.android.nrfmesh.node.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import no.nordicsemi.android.mesh.utils.HeartbeatSubscription;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.DialogFragmentAddressInputBinding;
import no.nordicsemi.android.nrfmesh.utils.HexKeyListener;
import no.nordicsemi.android.nrfmesh.utils.Utils;


public class DialogFragmentHeartbeatSource extends DialogFragment {

    private static final String HEARTBEAT_SOURCE = "HEARTBEAT_SOURCE";
    private DialogFragmentAddressInputBinding binding;

    private int mAddress = 2;

    public static DialogFragmentHeartbeatSource newInstance(@Nullable final HeartbeatSubscription subscription) {
        final DialogFragmentHeartbeatSource fragment = new DialogFragmentHeartbeatSource();
        if (subscription != null) {
            final Bundle args = new Bundle();
            args.putInt(HEARTBEAT_SOURCE, subscription.getSrc());
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAddress = getArguments().getInt(HEARTBEAT_SOURCE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        binding = DialogFragmentAddressInputBinding.inflate(getLayoutInflater());

        final KeyListener hexKeyListener = new HexKeyListener();
        binding.textInputLayout.setHint(getString((R.string.hint_heartbeat_subscription_source)));
        final String unicastAddress = MeshAddress.formatAddress(mAddress, false);
        binding.textInput.setText(unicastAddress);
        binding.textInput.setSelection(unicastAddress.length());
        binding.textInput.setKeyListener(hexKeyListener);
        binding.textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    binding.textInputLayout.setError(getString(R.string.error_empty_unicast_address));
                } else {
                    binding.textInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .setIcon(R.drawable.ic_lan_24dp)
                .setTitle(R.string.source_address)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null);

        binding.summary.setText(R.string.dialog_summary_heartbeat_source);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                final String unicast = binding.textInput.getEditableText().toString().trim();
                if (validateInput(unicast)) {
                    ((SubscriptionAddressCallbacks) requireActivity()).setSubscriptionSource(Integer.parseInt(unicast, 16));
                    dismiss();
                }
            } catch (IllegalArgumentException ex) {
                binding.textInputLayout.setError(ex.getMessage());
            }
        });

        return alertDialog;
    }

    private boolean validateInput(final String input) {
        try {
            if (input.length() % 4 != 0 || !input.matches(Utils.HEX_PATTERN)) {
                binding.textInputLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }
            final int address = Integer.parseInt(input, 16);
            if(!MeshAddress.isValidUnicastAddress(address)){
                binding.textInputLayout.setError(getString(R.string.invalid_heartbeat_subscription));
                return false;
            }
        } catch (IllegalArgumentException ex) {
            binding.textInputLayout.setError(ex.getMessage());
            return false;
        }
        return true;
    }

    public interface SubscriptionAddressCallbacks {
        void setSubscriptionSource(final int sourceAddress);
    }
}
