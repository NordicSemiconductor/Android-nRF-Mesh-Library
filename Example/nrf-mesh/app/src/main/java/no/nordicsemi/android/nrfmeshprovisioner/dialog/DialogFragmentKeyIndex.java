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
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.utils.HexKeyListener;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;


public class DialogFragmentKeyIndex extends DialogFragment {

    private static final String KEY_INDEX = "KEY_INDEX";
    //UI Bindings
    @BindView(R.id.text_input_layout)
    TextInputLayout keyIndexInputLayout;
    @BindView(R.id.text_input)
    TextInputEditText keyIndexInput;

    private int mKeyIndex;

    public static DialogFragmentKeyIndex newInstance(final int networkKeyIndex) {
        DialogFragmentKeyIndex fragmentNetworkKey = new DialogFragmentKeyIndex();
        final Bundle args = new Bundle();
        args.putInt(KEY_INDEX, networkKeyIndex);
        fragmentNetworkKey.setArguments(args);
        return fragmentNetworkKey;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mKeyIndex = getArguments().getInt(KEY_INDEX);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_key_index_input, null);

        //Bind ui
        ButterKnife.bind(this, rootView);

        final KeyListener hexKeyListener = new HexKeyListener();
        keyIndexInputLayout.setHint(getString((R.string.hint_key_index)));
        final String netKeyIndex = String.format(Locale.US, "%03X", mKeyIndex);
        keyIndexInput.setKeyListener(hexKeyListener);
        keyIndexInput.setText(netKeyIndex);
        keyIndexInput.setSelection(netKeyIndex.length());
        keyIndexInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    keyIndexInputLayout.setError(getString(R.string.error_empty_key_index));
                } else {
                    keyIndexInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext()).setView(rootView)
                .setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null);

        alertDialogBuilder.setIcon(R.drawable.ic_numeric);
        alertDialogBuilder.setTitle(R.string.title_key_index);
        alertDialogBuilder.setMessage(R.string.dialog_summary_key_index);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String keyIndex = keyIndexInput.getText().toString();
            if (validateInput(keyIndex)) {
                if ((getParentFragment()) == null) {
                    ((DialogFragmentKeyIndexListener) getActivity()).onKeyIndexGenerated(Integer.valueOf(keyIndex));
                } else {
                    ((DialogFragmentKeyIndexListener) getParentFragment()).onKeyIndexGenerated(Integer.valueOf(keyIndex));
                }
                dismiss();
            }
        });

        return alertDialog;
    }

    private boolean validateInput(final String input) {
        try {

            if(!input.matches(Utils.HEX_PATTERN)) {
                keyIndexInputLayout.setError(getString(R.string.invalid_hex_value));
                return false;
            }

            if (MeshParserUtils.validateKeyIndexInput(getContext(), input)) {
                return true;
            }
        } catch (IllegalArgumentException ex) {
            keyIndexInputLayout.setError(ex.getMessage());
        }
        return false;
    }

    public interface DialogFragmentKeyIndexListener {

        void onKeyIndexGenerated(final int keyIndex);

    }
}
