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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.AppKeyAdapter;

public class DialogFragmentAddAppKey extends DialogFragment implements AppKeyAdapter.OnItemClickListener {

    private static final String APP_KEY = "APP_KEY";

    //UI Bindings
    @BindView(R.id.text_input_layout)
    TextInputLayout appKeysInputLayout;
    @BindView(R.id.text_input)
    TextInputEditText appKeyInput;

    private String mAppKey;

    public static DialogFragmentAddAppKey newInstance(final String appKey) {
        DialogFragmentAddAppKey fragmentNetworkKey = new DialogFragmentAddAppKey();
        final Bundle args = new Bundle();
        args.putString(APP_KEY, appKey);
        fragmentNetworkKey.setArguments(args);
        return fragmentNetworkKey;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAppKey = getArguments().getString(APP_KEY);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_key_input, null);
        ButterKnife.bind(this, rootView);

        //Bind ui
        appKeysInputLayout.setHint(getString(R.string.hint_app_key));
        appKeyInput.setText(mAppKey);
        appKeyInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    appKeysInputLayout.setError(getString(R.string.error_empty_app_key));
                } else {
                    appKeysInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext()).setView(rootView)
                .setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.generate_app_key, null);

        alertDialogBuilder.setIcon(R.drawable.ic_vpn_key_black_alpha_24dp);
        alertDialogBuilder.setTitle(R.string.title_manage_app_keys);
        alertDialogBuilder.setMessage(R.string.summary_app_keys);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String appKey = appKeyInput.getText().toString();
            if (validateInput(appKey)) {
                try {
                    ((DialogFragmentAddAppKeysListener) getContext()).onAppKeyAdded(appKey);
                    dismiss();
                } catch (IllegalArgumentException ex) {
                    appKeysInputLayout.setError(ex.getMessage());
                }
            }
        });
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> appKeyInput.setText(SecureUtils.generateRandomNetworkKey()));
        return alertDialog;
    }

    private boolean validateInput(final String appKey) {
        try {
            if(MeshParserUtils.validateAppKeyInput(getContext(), appKey)) {
                return true;
            }
        } catch (IllegalArgumentException ex) {
            appKeysInputLayout.setError(ex.getMessage());
        }
        return false;
    }

    @Override
    public void onItemClick(final int position, final ApplicationKey appKey) {

    }

    public interface DialogFragmentAddAppKeysListener {
        void onAppKeyAdded(final String appKey);
    }
}
