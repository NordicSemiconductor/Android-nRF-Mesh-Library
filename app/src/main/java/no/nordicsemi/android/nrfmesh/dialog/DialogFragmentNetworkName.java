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
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.DialogFragmentNameBinding;

public class DialogFragmentNetworkName extends DialogFragment {

    private static final String NETWORK_NAME = "NETWORK_NAME";

    private DialogFragmentNameBinding binding;
    private String mNetworkName;

    public static DialogFragmentNetworkName newInstance(final String networkName) {
        DialogFragmentNetworkName fragmentNetworkKey = new DialogFragmentNetworkName();
        final Bundle args = new Bundle();
        args.putString(NETWORK_NAME, networkName);
        fragmentNetworkKey.setArguments(args);
        return fragmentNetworkKey;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNetworkName = getArguments().getString(NETWORK_NAME);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        binding = DialogFragmentNameBinding.inflate(getLayoutInflater());

        final TextView summary = binding.summary;
        binding.textInputLayout.setHint(getString(R.string.hint_global_network_name));
        binding.textInput.setText(mNetworkName);
        binding.textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    binding.textInputLayout.setError(getString(R.string.error_empty_name));
                } else {
                    binding.textInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext()).setView(binding.getRoot())
                .setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null);

        alertDialogBuilder.setIcon(R.drawable.ic_label);
        alertDialogBuilder.setTitle(R.string.title_network_name);
        summary.setText(R.string.summary_network_name);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String networkKey = binding.textInput.getEditableText().toString().trim();
            if (validateInput(networkKey)) {
                if(getParentFragment() == null) {
                    ((DialogFragmentNetworkNameListener) requireActivity()).onNetworkNameEntered(networkKey);
                } else {
                    ((DialogFragmentNetworkNameListener) getParentFragment()).onNetworkNameEntered(networkKey);
                }
                dismiss();
            }
        });

        return alertDialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean validateInput(final String input) {
        if(TextUtils.isEmpty(input)){
            binding.textInputLayout.setError(getString(R.string.error_empty_name));
            return false;
        }

        return true;
    }

    public interface DialogFragmentNetworkNameListener {
        void onNetworkNameEntered(@NonNull final String name);
    }
}
