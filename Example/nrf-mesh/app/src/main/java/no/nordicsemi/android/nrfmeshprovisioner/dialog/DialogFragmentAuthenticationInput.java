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

package no.nordicsemi.android.nrfmeshprovisioner.dialog;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class DialogFragmentAuthenticationInput extends DialogFragment {

    //UI Bindings
    @BindView(R.id.text_input_layout)
    TextInputLayout pinInputLayout;
    @BindView(R.id.text_input)
    TextInputEditText pinInput;

    public interface ProvisionerInputFragmentListener {
        void onPinInputComplete(final String pin);
        void onPinInputCanceled();
    }

    public static DialogFragmentAuthenticationInput newInstance() {
        Bundle args = new Bundle();
        DialogFragmentAuthenticationInput fragment = new DialogFragmentAuthenticationInput();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_auth_input, null);
        ButterKnife.bind(this, rootView);
        pinInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        pinInputLayout.setHint(getString((R.string.hint_pin)));
        pinInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    pinInputLayout.setError(getString(R.string.error_empty_pin));
                } else {
                    pinInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setIcon(R.drawable.ic_lock_open);
        alertDialogBuilder.setTitle(getString(R.string.provisioner_input_title));
        alertDialogBuilder.setMessage(getString(R.string.provisioner_input_summary));

        alertDialogBuilder.setView(rootView).
                setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                    final String pin = pinInput.getText().toString().trim();
                    if (validateInput(pin)) {
                        ((ProvisionerInputFragmentListener) getActivity()).onPinInputComplete(pin);
                    }
                }).
                setNegativeButton(getString(R.string.cancel), (dialog, which) -> ((ProvisionerInputFragmentListener) getActivity()).onPinInputCanceled());
        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    private boolean validateInput(final String input) {
        if (TextUtils.isEmpty(input)) {
            pinInputLayout.setError(getString(R.string.error_empty_pin));
            return false;
        }

        return true;
    }
}
