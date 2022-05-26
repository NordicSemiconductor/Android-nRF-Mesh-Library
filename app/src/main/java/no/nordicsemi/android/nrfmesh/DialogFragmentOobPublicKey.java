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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.databinding.DialogFragmentPublicKeyInputBinding;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.validatePublicKeyInput;

public class DialogFragmentOobPublicKey extends DialogFragment {

    private DialogFragmentPublicKeyInputBinding binding;

    public interface DialogFragmentOobPublicKeysListener {
        void onPublicKeyAdded(@Nullable final byte[] publicKey);
    }

    public static DialogFragmentOobPublicKey newInstance() {
        DialogFragmentOobPublicKey oobPublicKey = new DialogFragmentOobPublicKey();
        final Bundle args = new Bundle();
        oobPublicKey.setArguments(args);
        return oobPublicKey;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        binding = DialogFragmentPublicKeyInputBinding.inflate(getLayoutInflater());
        //Bind ui
        binding.textInputLayout.setHint(getString(R.string.hint_public_key));
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

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext()).setView(binding.getRoot())
                .setPositiveButton(R.string.ok, null).setNegativeButton(R.string.skip, null);

        alertDialogBuilder.setIcon(R.drawable.ic_vpn_key_24dp);
        alertDialogBuilder.setTitle(R.string.title_public_key);
        binding.summary.setText(R.string.message_public_key);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String key = binding.textInput.getEditableText().toString().trim();
            try {
                if (validatePublicKeyInput(key)) {
                    ((DialogFragmentOobPublicKeysListener) requireContext()).onPublicKeyAdded(MeshParserUtils.toByteArray(key));
                    dismiss();
                }
            } catch (IllegalArgumentException ex) {
                binding.textInputLayout.setError(ex.getMessage());
            }
        });
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> {
            ((DialogFragmentOobPublicKeysListener) requireContext()).onPublicKeyAdded(null);
            dismiss();
        });
        return alertDialog;
    }
}
