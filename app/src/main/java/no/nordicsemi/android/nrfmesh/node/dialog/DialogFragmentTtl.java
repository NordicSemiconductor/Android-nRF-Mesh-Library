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
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.DialogFragmentPublishTtlInputBinding;

public class DialogFragmentTtl extends DialogFragment {

    static final String PUBLISH_TTL = "PUBLISH_TTL";
    protected DialogFragmentPublishTtlInputBinding binding;
    private int mPublishTtl;

    public interface DialogFragmentTtlListener {
        void setPublishTtl(final int ttl);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPublishTtl = getArguments().getInt(PUBLISH_TTL);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        binding = DialogFragmentPublishTtlInputBinding.inflate(getLayoutInflater());

        binding.chkDefaultTtl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.textInputLayout.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            if (isChecked) {
                binding.textInputLayout.setError(null);
            }
        });

        binding.textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    binding.textInputLayout.setError(getString(R.string.error_empty_ttl));
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

        alertDialogBuilder.setIcon(R.drawable.ic_timer);
        alertDialogBuilder.setTitle(R.string.title_publish_ttl);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (binding.chkDefaultTtl.isChecked()) {
                ((DialogFragmentTtlListener) requireActivity()).setPublishTtl(MeshParserUtils.USE_DEFAULT_TTL);
                dismiss();
            } else {
                final String publishTtl = binding.textInput.getEditableText().toString().trim();
                if (validateInput(publishTtl)) {
                    ((DialogFragmentTtlListener) requireActivity()).setPublishTtl(Integer.parseInt(publishTtl));
                    dismiss();
                }
            }
        });

        if (savedInstanceState == null) {
            //Update ui
            if (MeshParserUtils.isDefaultPublishTtl(mPublishTtl)) {
                binding.chkDefaultTtl.setChecked(true);
            } else {
                final String ttl = String.valueOf(mPublishTtl);
                binding.textInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                binding.textInput.setText(ttl);
                binding.textInput.setSelection(ttl.length());
            }
            binding.textInputLayout.setHint(getString(R.string.hint_publish_ttl));
        }

        return alertDialog;
    }

    private boolean validateInput(final String input) {
        try {
            if (TextUtils.isEmpty(input)) {
                binding.textInputLayout.setError(getString(R.string.error_empty_ttl));
                return false;
            }
            if (!MeshParserUtils.isValidTtl(Integer.parseInt(input))) {
                binding.textInputLayout.setError(getString(R.string.error_invalid_publish_ttl));
                return false;
            }
        } catch (NumberFormatException ex) {
            binding.textInputLayout.setError(getString(R.string.error_invalid_publish_ttl));
            return false;
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
