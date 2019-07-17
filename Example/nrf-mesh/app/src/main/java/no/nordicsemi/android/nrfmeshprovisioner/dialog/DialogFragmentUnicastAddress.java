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
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.utils.HexKeyListener;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;


public class DialogFragmentUnicastAddress extends DialogFragment {

    private static final String UNICAST_ADDRESS = "UNICAST_ADDRESS";
    private static final String ELEMENT_COUNT = "ELEMENT_COUNT";

    //UI Bindings
    @BindView(R.id.text_input_layout)
    TextInputLayout unicastAddressInputLayout;
    @BindView(R.id.text_input)
    TextInputEditText unicastAddressInput;

    private int mUnicastAddress;
    private int mElementCount;

    public static DialogFragmentUnicastAddress newInstance(final int unicastAddress, final int elementCount) {
        DialogFragmentUnicastAddress fragmentIvIndex = new DialogFragmentUnicastAddress();
        final Bundle args = new Bundle();
        args.putInt(UNICAST_ADDRESS, unicastAddress);
        args.putInt(ELEMENT_COUNT, elementCount);
        fragmentIvIndex.setArguments(args);
        return fragmentIvIndex;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUnicastAddress = getArguments().getInt(UNICAST_ADDRESS);
            mElementCount = getArguments().getInt(ELEMENT_COUNT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_address_input, null);

        //Bind ui
        ButterKnife.bind(this, rootView);
        final TextView summary = rootView.findViewById(R.id.summary);
        final KeyListener hexKeyListener = new HexKeyListener();
        final String unicastAddress = MeshAddress.formatAddress(mUnicastAddress, false);
        unicastAddressInputLayout.setHint(getString((R.string.hint_unicast_address)));
        unicastAddressInput.setText(unicastAddress);
        unicastAddressInput.setSelection(unicastAddress.length());
        unicastAddressInput.setKeyListener(hexKeyListener);
        unicastAddressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    unicastAddressInputLayout.setError(getString(R.string.error_empty_unicast_address));
                } else {
                    unicastAddressInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_lan_black_alpha_24dp)
                .setTitle(R.string.title_unicast_address)
                .setView(rootView)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.automatic, null);

        summary.setText(R.string.dialog_summary_unicast_address);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String unicast = unicastAddressInput.getEditableText().toString().trim();
            try {
                if (validateInput(unicast)) {
                    if (((DialogFragmentUnicastAddressListener) requireActivity()).setUnicastAddress(Integer.parseInt(unicast, 16))) {
                        dismiss();
                    }
                }
            } catch (IllegalArgumentException ex) {
                unicastAddressInputLayout.setError(ex.getMessage());
            }
        });

        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> {
            try {
                final int unicast = ((DialogFragmentUnicastAddressListener) requireActivity()).getNextUnicastAddress(mElementCount);
                unicastAddressInput.setText(MeshAddress.formatAddress(unicast, false));
            } catch (IllegalArgumentException ex) {
                unicastAddressInputLayout.setError(ex.getMessage());
            }
        });

        return alertDialog;
    }

    private boolean validateInput(final String input) {

        try {

            if (input.length() % 4 != 0 || !input.matches(Utils.HEX_PATTERN)) {
                unicastAddressInputLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }

            final int unicastAddress = Integer.parseInt(input, 16);
            if (!MeshAddress.isValidUnicastAddress(unicastAddress)) {
                unicastAddressInputLayout.setError("Unicast address must range from 0x0001 - 0x7FFFF");
                return false;
            }
        } catch (IllegalArgumentException ex) {
            unicastAddressInputLayout.setError(ex.getMessage());
        }

        return true;
    }

    public interface DialogFragmentUnicastAddressListener {

        boolean setUnicastAddress(final int unicastAddress);

        int getNextUnicastAddress(final int elementCount);

    }
}
