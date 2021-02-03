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

package no.nordicsemi.android.nrfmesh.keys.dialogs;

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
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.utils.SecureUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.DialogFragmentKeyInputBinding;
import no.nordicsemi.android.nrfmesh.keys.MeshKeyListener;
import no.nordicsemi.android.nrfmesh.utils.HexKeyListener;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.bytesToHex;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.validateKeyInput;

public class DialogFragmentEditAppKey extends DialogFragment {

    private static final String APP_KEY = "APP_KEY";

    private DialogFragmentKeyInputBinding binding;
    private ApplicationKey mAppKey;

    public static DialogFragmentEditAppKey newInstance(final ApplicationKey appKey) {
        DialogFragmentEditAppKey fragmentNetworkKey = new DialogFragmentEditAppKey();
        final Bundle args = new Bundle();
        args.putParcelable(APP_KEY, appKey);
        fragmentNetworkKey.setArguments(args);
        return fragmentNetworkKey;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAppKey = getArguments().getParcelable(APP_KEY);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        binding = DialogFragmentKeyInputBinding.inflate(getLayoutInflater());
        final KeyListener hexKeyListener = new HexKeyListener();
        binding.textInputLayout.setHint(getString(R.string.hint_app_key));
        binding.textInput.setText(bytesToHex(mAppKey.getKey(), false));
        binding.textInput.setKeyListener(hexKeyListener);
        binding.textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    binding.textInputLayout.setError(getString(R.string.error_empty_key));
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
                .setIcon(R.drawable.ic_vpn_key_24dp)
                .setTitle(R.string.title_edit_key)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.generate_new_key, null);
        final AlertDialog alertDialog = alertDialogBuilder.show();

        binding.summary.setText(R.string.summary_edit_key);

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String appKey = binding.textInput.getEditableText().toString().trim();
            try {
                if (validateKeyInput(appKey) && ((MeshKeyListener) requireContext()).onKeyUpdated(appKey))
                    dismiss();
            } catch (IllegalArgumentException ex) {
                binding.textInputLayout.setError(ex.getMessage());
            }

        });
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).
                setOnClickListener(v -> binding.textInput.setText(SecureUtils.generateRandomNetworkKey()));

        return alertDialog;
    }
}
