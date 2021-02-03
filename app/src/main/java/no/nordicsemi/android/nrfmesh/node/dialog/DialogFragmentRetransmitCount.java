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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.DialogFragmentPublicationParametersBinding;

public class DialogFragmentRetransmitCount extends DialogFragment {

    private static final String RETRANSMIT_COUNT = "RETRANSMIT_COUNT";
    private DialogFragmentPublicationParametersBinding binding;

    private int mRetransmitCount;

    public static DialogFragmentRetransmitCount newInstance(final int ivIndex) {
        DialogFragmentRetransmitCount fragmentIvIndex = new DialogFragmentRetransmitCount();
        final Bundle args = new Bundle();
        args.putInt(RETRANSMIT_COUNT, ivIndex);
        fragmentIvIndex.setArguments(args);
        return fragmentIvIndex;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRetransmitCount = getArguments().getInt(RETRANSMIT_COUNT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        binding = DialogFragmentPublicationParametersBinding.inflate(getLayoutInflater());

        binding.summary.setText(R.string.dialog_summary_retransmit_count);

        final String ivIndex = String.valueOf(mRetransmitCount);
        binding.textInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.textInputLayout.setHint(getString(R.string.hint_retransmit_count));
        binding.textInput.setText(ivIndex);
        binding.textInput.setSelection(ivIndex.length());
        binding.textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    binding.textInputLayout.setError(getString(R.string.error_empty_pub_retransmit_count));
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

        alertDialogBuilder.setIcon(R.drawable.ic_numeric);
        alertDialogBuilder.setTitle(R.string.title_retransmit_count);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String ivIndexInput = this.binding.textInput.getEditableText().toString().trim();
            if (validateInput(ivIndexInput)) {
                if (getParentFragment() == null) {
                    ((DialogFragmentRetransmitCountListener) requireActivity()).setRetransmitCount(Integer.parseInt(ivIndexInput, 16));
                } else {
                    ((DialogFragmentRetransmitCountListener) getParentFragment()).setRetransmitCount(Integer.parseInt(ivIndexInput, 16));
                }
                dismiss();
            }
        });

        return alertDialog;
    }

    private boolean validateInput(final String input) {

        try {

            if (TextUtils.isEmpty(input)) {
                binding.textInputLayout.setError(getString(R.string.error_empty_pub_retransmit_count));
                return false;
            }
            if (!MeshParserUtils.validateRetransmitCount(Integer.parseInt(input))) {
                binding.textInputLayout.setError(getString(R.string.error_invalid_pub_retransmit_count));
                return false;
            }
        } catch (NumberFormatException ex) {
            binding.textInputLayout.setError(getString(R.string.error_invalid_pub_retransmit_count));
            return false;
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    public interface DialogFragmentRetransmitCountListener {

        void setRetransmitCount(final int retransmitCount);

    }
}
