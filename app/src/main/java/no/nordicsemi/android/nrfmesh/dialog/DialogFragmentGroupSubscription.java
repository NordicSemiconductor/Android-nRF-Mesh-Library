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

package no.nordicsemi.android.nrfmesh.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.utils.AddressType;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.GroupCallbacks;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.adapter.AddressTypeAdapter;
import no.nordicsemi.android.nrfmesh.adapter.GroupAdapterSpinner;
import no.nordicsemi.android.nrfmesh.databinding.DialogFragmentGroupSubscriptionBinding;
import no.nordicsemi.android.nrfmesh.utils.HexKeyListener;
import no.nordicsemi.android.nrfmesh.utils.Utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static no.nordicsemi.android.mesh.utils.AddressType.GROUP_ADDRESS;
import static no.nordicsemi.android.mesh.utils.AddressType.VIRTUAL_ADDRESS;

public class DialogFragmentGroupSubscription extends DialogFragment {

    private static final AddressType[] ADDRESS_TYPES = {GROUP_ADDRESS, VIRTUAL_ADDRESS};
    private static final String GROUPS = "GROUPS";
    private static final String GROUP = "GROUP";

    private DialogFragmentGroupSubscriptionBinding binding;
    private Button mGenerateLabelUUID;
    private AddressTypeAdapter mAdapterSpinner;
    private ArrayList<Group> mGroups;
    private Group mGroup;

    public static DialogFragmentGroupSubscription newInstance(final ArrayList<Group> groups) {
        final DialogFragmentGroupSubscription fragment = new DialogFragmentGroupSubscription();
        final Bundle args = new Bundle();
        args.putParcelableArrayList(GROUPS, groups);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGroups = getArguments().getParcelableArrayList(GROUPS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        binding = DialogFragmentGroupSubscriptionBinding.inflate(getLayoutInflater());
        if (savedInstanceState == null) {
            mGroup = ((GroupCallbacks) requireActivity()).createGroup();
        } else {
            mGroup = savedInstanceState.getParcelable(GROUP);
        }

        binding.groupContainer.radioSelectGroup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.groupNameLayout.setEnabled(!isChecked);
            binding.groupNameLayout.setError(null);
            binding.groupAddressLayout.setEnabled(!isChecked);
            binding.groupAddressLayout.setError(null);
            binding.groupContainer.groups.setEnabled(isChecked);
            binding.groupContainer.radioCreateGroup.setChecked(!isChecked);
        });

        binding.groupContainer.radioCreateGroup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.groupNameLayout.setEnabled(isChecked);
            binding.groupNameLayout.setError(null);
            binding.groupAddressLayout.setEnabled(isChecked);
            binding.groupAddressLayout.setError(null);
            binding.groupContainer.groups.setEnabled(!isChecked);
            binding.groupContainer.radioSelectGroup.setChecked(!isChecked);
            if (isChecked) {
                if (mGroup != null) {
                    binding.nameInput.setText(mGroup.getName());
                    binding.addressInput.setText(MeshAddress.formatAddress(mGroup.getAddress(), false));
                }
            }
        });

        mAdapterSpinner = new AddressTypeAdapter(requireContext(), ADDRESS_TYPES);
        binding.addressTypes.setAdapter(mAdapterSpinner);

        binding.groupContainer.groups.setAdapter( new GroupAdapterSpinner(mGroups));

        if (mGroups.isEmpty()) {
            binding.groupContainer.radioSelectGroup.setEnabled(false);
            binding.groupContainer.groups.setEnabled(false);
            binding.groupContainer.radioCreateGroup.setChecked(true);
        } else {
            binding.groupContainer.radioSelectGroup.setChecked(true);
            binding.groupContainer.radioCreateGroup.setChecked(false);
        }

        updateGroup();

        binding.addressTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                updateAddress(mAdapterSpinner.getItem(position));
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {

            }
        });

        final KeyListener hexKeyListener = new HexKeyListener();
        binding.addressInput.setKeyListener(hexKeyListener);
        binding.addressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                mGroup = null;
                if (TextUtils.isEmpty(s.toString())) {
                    binding.groupAddressLayout.setError(getString(R.string.error_empty_group_address));
                } else {
                    binding.groupAddressLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_subscribe)
                .setTitle(R.string.title_subscribe_group)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.generate_uuid, null);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            final AddressType type = (AddressType) binding.addressTypes.getSelectedItem();
            try {
                if (type == GROUP_ADDRESS) {
                    if (binding.groupContainer.radioCreateGroup.isChecked()) {
                        if (mGroup != null) {
                            if (((GroupCallbacks) requireActivity()).onGroupAdded(mGroup)) {
                                dismiss();
                            }
                        } else {
                            final String name = binding.nameInput.getEditableText().toString().trim();
                            final String address = binding.addressInput.getEditableText().toString().trim();
                            if (validateInput(name, address)) {
                                if ((((GroupCallbacks) requireActivity())).
                                        onGroupAdded(name, Integer.valueOf(address, 16))) {
                                    dismiss();
                                }
                            }
                        }
                    } else {
                        final Group group = (Group) binding.groupContainer.groups.getSelectedItem();
                        ((GroupCallbacks) requireActivity()).subscribe(group);
                        dismiss();
                    }
                } else {
                    final UUID uuid = UUID.fromString(binding.uuidLabel.getText().toString().trim());
                    final String name = binding.nameInput.getEditableText().toString().trim();
                    final Group group = ((GroupCallbacks) requireActivity()).createGroup(uuid, name);
                    if (group != null) {
                        if (((GroupCallbacks) requireActivity()).onGroupAdded(group)) {
                            dismiss();
                        }
                    }
                }
            } catch (IllegalArgumentException ex) {
                binding.groupAddressLayout.setError(ex.getMessage());
            }
        });

        mGenerateLabelUUID = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        mGenerateLabelUUID.setOnClickListener(v -> {
            final UUID uuid = MeshAddress.generateRandomLabelUUID();
            binding.uuidLabel.setText(uuid.toString().toUpperCase(Locale.US));
            generateVirtualAddress(uuid);
        });

        return alertDialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(GROUP, mGroup);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateAddress(@NonNull final AddressType addressType) {
        if (addressType == VIRTUAL_ADDRESS) {
            binding.labelSummary.setVisibility(VISIBLE);
            binding.uuidLabel.setVisibility(VISIBLE);
            mGenerateLabelUUID.setVisibility(VISIBLE);
            binding.groupContainer.getRoot().setVisibility(GONE);
            binding.groupNameLayout.setEnabled(true);
            binding.groupNameLayout.setError(null);
            binding.groupAddressLayout.setError(null);
            binding.groupAddressLayout.setEnabled(false);
            generateVirtualAddress(UUID.fromString(binding.uuidLabel.getText().toString()));
        } else {
            binding.groupContainer.getRoot().setVisibility(VISIBLE);
            binding.labelSummary.setVisibility(GONE);
            binding.uuidLabel.setVisibility(GONE);
            mGenerateLabelUUID.setVisibility(GONE);
            updateGroup();
        }
    }

    private void generateVirtualAddress(@NonNull final UUID uuid) {
        final Integer add = MeshAddress.generateVirtualAddress(uuid);
        binding.addressInput.setText(MeshAddress.formatAddress(add, false));
    }

    private void updateGroup() {
        if (mGroup == null) {
            mGroup = ((GroupCallbacks) requireActivity()).createGroup();
        }

        if (mGroup != null) {
            binding.nameInput.setText(mGroup.getName());
            binding.addressInput.setText(MeshAddress.formatAddress(mGroup.getAddress(), false));
        }
    }

    private boolean validateInput(@NonNull final String name, @NonNull final String address) {
        try {
            if (TextUtils.isEmpty(name)) {
                binding.groupNameLayout.setError(getString(R.string.error_empty_group_name));
                return false;
            }
            if (address.length() % 4 != 0 || !address.matches(Utils.HEX_PATTERN)) {
                binding.groupAddressLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }

            final int groupAddress = Integer.valueOf(address, 16);
            if (!MeshAddress.isValidGroupAddress(groupAddress)) {
                binding.groupAddressLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }

            for (Group group : mGroups) {
                if (groupAddress == group.getAddress()) {
                    binding.groupAddressLayout.setError(getString(R.string.error_group_address_in_used));
                    return false;
                }
            }
        } catch (IllegalArgumentException ex) {
            binding.groupAddressLayout.setError(ex.getMessage());
            return false;
        }

        return true;
    }
}
