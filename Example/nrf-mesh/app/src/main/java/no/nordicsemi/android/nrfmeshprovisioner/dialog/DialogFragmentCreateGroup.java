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

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.utils.HexKeyListener;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;


public class DialogFragmentCreateGroup extends DialogFragment {

    private static final String GROUPS = "GROUPS";
    //UI Bindings
    @BindView(R.id.group_name_layout)
    TextInputLayout groupNameInputLayout;
    @BindView(R.id.name_input)
    TextInputEditText groupNameInput;
    @BindView(R.id.group_address_layout)
    TextInputLayout addressInputLayout;
    @BindView(R.id.address_input)
    TextInputEditText addressInput;


    public interface DialogFragmentCreateGroupListener {
        boolean createGroup(@NonNull final String name, final int address);
    }

    public static DialogFragmentCreateGroup newInstance() {
        return new DialogFragmentCreateGroup();
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_create_group, null);

        //Bind ui
        ButterKnife.bind(this, rootView);

        final KeyListener hexKeyListener = new HexKeyListener();
        addressInput.setKeyListener(hexKeyListener);
        addressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    addressInputLayout.setError(getString(R.string.error_empty_group_address));
                } else {
                    addressInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext()).setView(rootView)
                .setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null);

        alertDialogBuilder.setIcon(R.drawable.ic_outline_group_work_black_alpha_24dp);
        alertDialogBuilder.setTitle(R.string.title_create_group);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                final String name = groupNameInput.getEditableText().toString();
                final String address = addressInput.getEditableText().toString();
                if (validateInput(name, address)) {
                    if(getParentFragment() != null) {
                        if(((DialogFragmentCreateGroupListener) getParentFragment()).createGroup(name, Integer.valueOf(address, 16))) {
                            dismiss();
                        } else {
                            addressInputLayout.setError(getString(R.string.error_group_address_in_used));
                        }
                    }
                }
        });

        return alertDialog;
    }

    private boolean validateInput(@NonNull final String name, @NonNull final String address) {
        try {
            if(TextUtils.isEmpty(name)){
                groupNameInputLayout.setError(getString(R.string.error_empty_group_name));
                return false;
            }
            if (address.length() % 4 != 0 || !address.matches(Utils.HEX_PATTERN)) {
                addressInputLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }

            final byte[] groupAddress = MeshParserUtils.toByteArray(address);
            if(!MeshParserUtils.isValidSubscriptionAddress(groupAddress)){
                addressInputLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }
        } catch (IllegalArgumentException ex) {
            addressInputLayout.setError(ex.getMessage());
            return false;
        }

        return true;
    }
}
