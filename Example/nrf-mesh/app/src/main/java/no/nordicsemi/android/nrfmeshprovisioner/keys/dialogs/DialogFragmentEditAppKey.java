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

package no.nordicsemi.android.nrfmeshprovisioner.keys.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.keys.MeshKeyListener;
import no.nordicsemi.android.nrfmeshprovisioner.utils.HexKeyListener;

public class DialogFragmentEditAppKey extends DialogFragment {

    private static final String POSITION = "POSITION";
    private static final String APP_KEY = "APP_KEY";

    //UI Bindings
    @BindView(R.id.text_input_layout)
    TextInputLayout appKeysInputLayout;
    @BindView(R.id.text_input)
    TextInputEditText appKeyInput;

    private int mPosition;
    private ApplicationKey mAppKey;

    public static DialogFragmentEditAppKey newInstance(final int position, @NonNull final ApplicationKey appKey) {
        DialogFragmentEditAppKey fragmentNetworkKey = new DialogFragmentEditAppKey();
        final Bundle args = new Bundle();
        args.putInt(POSITION, position);
        args.putParcelable(APP_KEY, appKey);
        fragmentNetworkKey.setArguments(args);
        return fragmentNetworkKey;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPosition = getArguments().getInt(POSITION);
            mAppKey = getArguments().getParcelable(APP_KEY);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_key_input, null);

        ButterKnife.bind(this, rootView);
        final TextView summary = rootView.findViewById(R.id.summary);
        //Bind ui
        final KeyListener hexKeyListener = new HexKeyListener();
        appKeysInputLayout.setHint(getString(R.string.hint_app_key));
        appKeyInput.setText(MeshParserUtils.bytesToHex(mAppKey.getKey(), false));
        appKeyInput.setKeyListener(hexKeyListener);
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

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext())
                .setView(rootView)
                .setIcon(R.drawable.ic_vpn_key_black_alpha_24dp)
                .setTitle(R.string.title_edit_key)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.generate_new_key, null);
        final AlertDialog alertDialog = alertDialogBuilder.show();

        summary.setText(R.string.summary_edit_key);

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String appKey = appKeyInput.getEditableText().toString().trim();
            try {
                if (((MeshKeyListener) requireContext()).onKeyUpdated(mPosition, appKey))
                    dismiss();
            } catch (IllegalArgumentException ex) {
                appKeysInputLayout.setError(ex.getMessage());
            }

        });
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).
                setOnClickListener(v -> appKeyInput.setText(SecureUtils.generateRandomNetworkKey()));

        return alertDialog;
    }
}
