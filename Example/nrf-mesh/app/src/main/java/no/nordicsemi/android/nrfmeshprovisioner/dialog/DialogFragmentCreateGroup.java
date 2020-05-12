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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.GroupCallbacks;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.AddressTypeAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.utils.AddressTypes;
import no.nordicsemi.android.nrfmeshprovisioner.utils.HexKeyListener;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.AddressTypes.GROUP_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.AddressTypes.VIRTUAL_ADDRESS;


public class DialogFragmentCreateGroup extends DialogFragment {

    private static final AddressTypes[] addressTypes = {GROUP_ADDRESS, VIRTUAL_ADDRESS};
    private static final String GROUP = "GROUP";

    //UI Bindings
    @BindView(R.id.summary)
    TextView summary;
    @BindView(R.id.address_types)
    Spinner addressTypesSpinnerView;
    @BindView(R.id.group_container)
    View groupContainer;
    @BindView(R.id.group_name_layout)
    TextInputLayout groupNameInputLayout;
    @BindView(R.id.name_input)
    TextInputEditText groupNameInput;
    @BindView(R.id.group_address_layout)
    TextInputLayout addressInputLayout;
    @BindView(R.id.address_input)
    TextInputEditText addressInput;
    @BindView(R.id.label_summary)
    TextView labelSummary;
    @BindView(R.id.uuid_label)
    TextView labelUuidView;

    private Button mGenerateLabelUUID;

    private AddressTypeAdapter mAdapterSpinner;
    private Group mGroup;

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
        @SuppressLint("InflateParams") final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_group_subscription, null);

        //Bind ui
        ButterKnife.bind(this, rootView);
        final KeyListener hexKeyListener = new HexKeyListener();
        if (savedInstanceState == null) {
            mGroup = ((GroupCallbacks) requireParentFragment()).createGroup();
        } else {
            mGroup = savedInstanceState.getParcelable(GROUP);
        }

        if (mGroup != null) {
            groupNameInput.setText(mGroup.getName());
            addressInput.setText(MeshAddress.formatAddress(mGroup.getAddress(), false));
        }
        updateGroup();

        mAdapterSpinner = new AddressTypeAdapter(requireContext(), addressTypes);
        addressTypesSpinnerView.setAdapter(mAdapterSpinner);

        groupContainer.setVisibility(View.GONE);
        addressInput.setKeyListener(hexKeyListener);
        addressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                mGroup = null;
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

        addressTypesSpinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                updateAddress(mAdapterSpinner.getItem(position));
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {

            }
        });

        final AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(rootView)
                .setIcon(R.drawable.ic_outline_group_work_black_alpha_24dp)
                .setTitle(R.string.title_create_group)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.generate_uuid, null).show();

        summary.setText(R.string.title_create_group_summary);

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String name = groupNameInput.getEditableText().toString().trim();
            try {
                final AddressTypes type = (AddressTypes) addressTypesSpinnerView.getSelectedItem();
                if (type == GROUP_ADDRESS) {
                    if (mGroup == null) {
                        final String address = addressInput.getEditableText().toString().trim();
                        if (validateInput(name, address)) {
                            if (((GroupCallbacks) requireParentFragment()).onGroupAdded(name, Integer.valueOf(address, 16))) {
                                dismiss();
                            } else {
                                addressInputLayout.setError(getString(R.string.error_group_address_in_used));
                            }
                        }
                    } else {
                        if (((GroupCallbacks) requireParentFragment()).onGroupAdded(mGroup)) {
                            dismiss();
                        }
                    }
                } else {

                    final UUID uuid = UUID.fromString(labelUuidView.getText().toString());
                    final Group group = ((GroupCallbacks) requireParentFragment()).createGroup(uuid, name);
                    if (group != null) {
                        if (((GroupCallbacks) requireParentFragment()).onGroupAdded(group)) {
                            dismiss();
                        }
                    }
                }
            } catch (IllegalArgumentException ex) {
                addressInputLayout.setError(ex.getMessage());
            }
        });

        mGenerateLabelUUID = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        mGenerateLabelUUID.setOnClickListener(v -> {
            final UUID uuid = MeshAddress.generateRandomLabelUUID();
            labelUuidView.setText(uuid.toString().toUpperCase(Locale.US));
            final Integer add = MeshAddress.generateVirtualAddress(uuid);
            addressInput.setText(MeshAddress.formatAddress(add, false));
        });
        return alertDialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(GROUP, mGroup);
    }

    private void updateAddress(final AddressTypes addressType) {
        if (addressType == VIRTUAL_ADDRESS) {
            labelSummary.setVisibility(VISIBLE);
            labelUuidView.setVisibility(VISIBLE);
            mGenerateLabelUUID.setVisibility(VISIBLE);
            groupNameInputLayout.setEnabled(true);
            groupNameInputLayout.setError(null);
            final UUID uuid = UUID.fromString(labelUuidView.getText().toString());
            final Integer add = MeshAddress.generateVirtualAddress(uuid);
            addressInput.setText(String.valueOf(add));
            addressInputLayout.setError(null);
            addressInputLayout.setEnabled(false);
        } else {
            labelSummary.setVisibility(GONE);
            labelUuidView.setVisibility(GONE);
            mGenerateLabelUUID.setVisibility(GONE);
            updateGroup();
        }
    }

    private void updateGroup() {
        if (mGroup == null) {
            mGroup = ((GroupCallbacks) requireParentFragment()).createGroup();
        }
        groupNameInput.setText(mGroup.getName());
        addressInput.setText(MeshAddress.formatAddress(mGroup.getAddress(), false));
    }

    private boolean validateInput(@NonNull final String name, @NonNull final String address) {
        try {
            if (TextUtils.isEmpty(name)) {
                groupNameInputLayout.setError(getString(R.string.error_empty_group_name));
                return false;
            }
            if (address.length() % 4 != 0 || !address.matches(Utils.HEX_PATTERN)) {
                addressInputLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }

            final byte[] groupAddress = MeshParserUtils.toByteArray(address);
            if (!MeshAddress.isValidSubscriptionAddress(groupAddress)) {
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
