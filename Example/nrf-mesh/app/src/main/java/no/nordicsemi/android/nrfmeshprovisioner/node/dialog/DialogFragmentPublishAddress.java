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

package no.nordicsemi.android.nrfmeshprovisioner.node.dialog;

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
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.transport.PublicationSettings;
import no.nordicsemi.android.meshprovisioner.utils.AddressType;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.nrfmeshprovisioner.GroupCallbacks;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.AddressTypeAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.GroupAdapterSpinner;
import no.nordicsemi.android.nrfmeshprovisioner.utils.AddressTypes;
import no.nordicsemi.android.nrfmeshprovisioner.utils.HexKeyListener;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.AddressTypes.ALL_FRIENDS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.AddressTypes.ALL_NODES;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.AddressTypes.ALL_PROXIES;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.AddressTypes.ALL_RELAYS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.AddressTypes.GROUP_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.AddressTypes.UNICAST_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.AddressTypes.VIRTUAL_ADDRESS;

public class DialogFragmentPublishAddress extends DialogFragment {

    private static final String PUBLICATION_SETTINGS = "PUBLICATION_SETTINGS";
    private static final String GROUPS = "GROUPS";
    private static final String GROUP = "GROUP";
    private static final String UUID_KEY = "UUID";
    private ArrayList<Group> mGroups = new ArrayList<>();
    private PublicationSettings mPublicationSettings;
    private static final AddressTypes[] addressTypes = {UNICAST_ADDRESS, GROUP_ADDRESS, ALL_PROXIES, ALL_FRIENDS, ALL_RELAYS, ALL_NODES, VIRTUAL_ADDRESS};

    //UI Bindings
    @BindView(R.id.summary)
    TextView summary;
    @BindView(R.id.address_types)
    Spinner addressTypesSpinnerView;
    @BindView(R.id.label_container)
    View labelContainer;
    @BindView(R.id.uuid_label)
    TextView labelUuidView;
    @BindView(R.id.group_container)
    View groupContainer;
    @BindView(R.id.radio_select_group)
    RadioButton selectGroup;
    @BindView(R.id.radio_create_group)
    RadioButton createGroup;
    @BindView(R.id.groups)
    Spinner groups;
    @BindView(R.id.group_name_layout)
    TextInputLayout groupNameInputLayout;
    @BindView(R.id.name_input)
    TextInputEditText groupNameInput;
    @BindView(R.id.group_address_layout)
    TextInputLayout addressInputLayout;
    @BindView(R.id.address_input)
    TextInputEditText addressInput;
    @BindView(R.id.no_groups_configured)
    TextView noGroups;
    private Button mGenerateLabelUUID;

    private AddressTypeAdapter mAdapterSpinner;
    private Group mGroup;

    public interface DialogFragmentPublicationListener {

        void onPublishAddressSet(final int publishAddress);

        void onPublishAddressSet(@NonNull final Group group);

    }

    public static DialogFragmentPublishAddress newInstance(@NonNull final PublicationSettings publicationSettings,
                                                           @NonNull final ArrayList<Group> groups) {
        DialogFragmentPublishAddress fragmentPublishAddress = new DialogFragmentPublishAddress();
        final Bundle args = new Bundle();
        args.putParcelable(PUBLICATION_SETTINGS, publicationSettings);
        args.putParcelableArrayList(GROUPS, groups);
        fragmentPublishAddress.setArguments(args);
        return fragmentPublishAddress;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPublicationSettings = getArguments().getParcelable(PUBLICATION_SETTINGS);
            mGroups = getArguments().getParcelableArrayList(GROUPS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View rootView = LayoutInflater.from(getContext()).
                inflate(R.layout.dialog_fragment_group_subscription, null);

        //Bind ui
        ButterKnife.bind(this, rootView);
        if (savedInstanceState != null) {
            labelUuidView.setText(savedInstanceState.getString(UUID_KEY));
            mGroup = savedInstanceState.getParcelable(GROUP);
        } else {
            mGroup = ((GroupCallbacks) requireActivity()).createGroup();
        }

        setAddressType();
        summary.setText(R.string.publish_address_dialog_summary);
        addressTypesSpinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                updateAddress(mAdapterSpinner.getItem(position));
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {

            }
        });

        selectGroup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            groupNameInputLayout.setEnabled(!isChecked);
            groupNameInputLayout.setError(null);
            addressInputLayout.setEnabled(!isChecked);
            addressInputLayout.setError(null);
            groups.setEnabled(isChecked);
            createGroup.setChecked(!isChecked);
        });

        createGroup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            groupNameInputLayout.setEnabled(isChecked);
            groupNameInputLayout.setError(null);
            addressInputLayout.setEnabled(isChecked);
            addressInputLayout.setError(null);
            groups.setEnabled(!isChecked);
            selectGroup.setChecked(!isChecked);
        });

        final GroupAdapterSpinner adapter = new GroupAdapterSpinner(requireContext(), mGroups);
        groups.setAdapter(adapter);

        if (mGroups.isEmpty()) {
            selectGroup.setEnabled(false);
            groups.setEnabled(false);
            createGroup.setChecked(true);
        } else {
            selectGroup.setChecked(true);
            createGroup.setChecked(false);
        }

        final KeyListener hexKeyListener = new HexKeyListener();
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

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext()).
                setIcon(R.drawable.ic_lan_black_alpha_24dp).
                setTitle(R.string.title_publish_address).
                setView(rootView).
                setPositiveButton(R.string.ok, null).
                setNegativeButton(R.string.cancel, null).
                setNeutralButton(R.string.generate_uuid, null);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> setPublishAddress());

        mGenerateLabelUUID = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        mGenerateLabelUUID.setOnClickListener(v -> {
            final UUID uuid = MeshAddress.generateRandomLabelUUID();
            labelUuidView.setText(uuid.toString().toUpperCase(Locale.US));
            generateVirtualAddress(uuid);
        });

        return alertDialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(UUID_KEY, labelUuidView.getText().toString());
        outState.putParcelable(GROUP, mGroup);
    }

    private void setPublishAddress() {
        final String input = addressInput.getEditableText().toString().trim();
        final int address;
        final AddressTypes type = (AddressTypes) addressTypesSpinnerView.getSelectedItem();
        switch (type) {
            case UNICAST_ADDRESS:
                if (validateInput(input)) {
                    address = Integer.parseInt(input, 16);
                    ((DialogFragmentPublicationListener) requireActivity())
                            .onPublishAddressSet(address);
                    dismiss();
                }
                break;
            case GROUP_ADDRESS:
                try {
                    if (createGroup.isChecked()) {
                        final String name = groupNameInput.getEditableText().toString().trim();
                        final String groupAddress = addressInput.getEditableText().toString().trim();
                        if (validateInput(name, groupAddress)) {
                            if (mGroup != null) {
                                ((DialogFragmentPublicationListener) requireActivity()).
                                        onPublishAddressSet(mGroup);
                                dismiss();
                            } else {
                                if (((GroupCallbacks) requireActivity())
                                        .onGroupAdded(name, Integer.parseInt(groupAddress, 16))) {
                                    dismiss();
                                }
                            }
                        }
                    } else {
                        final Group group = (Group) groups.getSelectedItem();
                        ((DialogFragmentPublicationListener) requireActivity()).onPublishAddressSet(group);
                        dismiss();
                    }
                } catch (IllegalArgumentException ex) {
                    addressInputLayout.setError(ex.getMessage());
                }
                break;
            case ALL_PROXIES:
                ((DialogFragmentPublicationListener) requireActivity()).onPublishAddressSet(MeshAddress.ALL_PROXIES_ADDRESS);
                dismiss();
                break;
            case ALL_FRIENDS:
                ((DialogFragmentPublicationListener) requireActivity()).onPublishAddressSet(MeshAddress.ALL_FRIENDS_ADDRESS);
                dismiss();
                break;
            case ALL_RELAYS:
                ((DialogFragmentPublicationListener) requireActivity()).onPublishAddressSet(MeshAddress.ALL_RELAYS_ADDRESS);
                dismiss();
                break;
            case ALL_NODES:
                ((DialogFragmentPublicationListener) requireActivity()).onPublishAddressSet(MeshAddress.ALL_NODES_ADDRESS);
                dismiss();
                break;
            case VIRTUAL_ADDRESS:
                Group group = null;
                try {
                    final UUID uuid = UUID.fromString(labelUuidView.getText().toString().trim());
                    final String name = groupNameInput.getEditableText().toString().trim();
                    group = ((GroupCallbacks) requireActivity()).createGroup(uuid, name);
                    if (group != null) {
                        if (((GroupCallbacks) requireActivity()).onGroupAdded(group)) {
                            dismiss();
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    if (group != null) {
                        ((DialogFragmentPublicationListener) requireActivity()).onPublishAddressSet(group);
                        dismiss();
                    }
                }
                break;
        }
    }

    private void setAddressType() {
        int address = 0;
        if (mPublicationSettings != null) {
            address = mPublicationSettings.getPublishAddress();
        }

        mAdapterSpinner = new AddressTypeAdapter(requireContext(), addressTypes);
        addressTypesSpinnerView.setAdapter(mAdapterSpinner);
        final AddressType type = MeshAddress.getAddressType(address);
        if (type != null) {
            switch (type) {
                default:
                    if (address == MeshAddress.ALL_PROXIES_ADDRESS) {
                        addressTypesSpinnerView.setSelection(2);
                    } else if (address == MeshAddress.ALL_FRIENDS_ADDRESS) {
                        addressTypesSpinnerView.setSelection(3);
                    } else if (address == MeshAddress.ALL_RELAYS_ADDRESS) {
                        addressTypesSpinnerView.setSelection(4);
                    } else {
                        addressTypesSpinnerView.setSelection(5);
                    }
                    break;
                case UNICAST_ADDRESS:
                    addressTypesSpinnerView.setSelection(0);
                    break;
                case GROUP_ADDRESS:
                    addressTypesSpinnerView.setSelection(1);
                    break;
                case VIRTUAL_ADDRESS:
                    addressTypesSpinnerView.setSelection(addressTypes.length - 1);
                    break;
            }
        }
    }

    private void updateAddress(@NonNull final AddressTypes addressType) {
        int address = 0;
        if (mPublicationSettings != null) {
            address = mPublicationSettings.getPublishAddress();
        }

        switch (addressType) {
            default:
            case UNICAST_ADDRESS:
                addressInput.getEditableText().clear();
                updateFixedGroupAddressVisibility(MeshAddress.ALL_PROXIES_ADDRESS, true);
                break;
            case GROUP_ADDRESS:
                final int index = getGroupIndex(address);
                groups.setSelection(index);
                addressInputLayout.setEnabled(false);
                groupNameInputLayout.setVisibility(View.VISIBLE);
                groupContainer.setVisibility(View.VISIBLE);
                labelContainer.setVisibility(View.GONE);
                mGenerateLabelUUID.setVisibility(View.GONE);
                final Group group = ((GroupCallbacks) requireActivity())
                        .createGroup(groupNameInput.getEditableText().toString().trim());
                if (group != null) {
                    addressInput.setText(MeshAddress.formatAddress(group.getAddress(), false));
                }
                break;
            case ALL_PROXIES:
                updateFixedGroupAddressVisibility(MeshAddress.ALL_PROXIES_ADDRESS, false);
                break;
            case ALL_FRIENDS:
                updateFixedGroupAddressVisibility(MeshAddress.ALL_FRIENDS_ADDRESS, false);
                break;
            case ALL_RELAYS:
                updateFixedGroupAddressVisibility(MeshAddress.ALL_RELAYS_ADDRESS, false);
                break;
            case ALL_NODES:
                updateFixedGroupAddressVisibility(MeshAddress.ALL_NODES_ADDRESS, false);
                break;
            case VIRTUAL_ADDRESS:
                if (mPublicationSettings != null && mPublicationSettings.getLabelUUID() != null) {
                    labelUuidView.setText(mPublicationSettings.getLabelUUID().toString().toUpperCase(Locale.US));
                }
                labelContainer.setVisibility(View.VISIBLE);
                mGenerateLabelUUID.setVisibility(View.VISIBLE);
                addressInputLayout.setEnabled(false);
                groupNameInputLayout.setVisibility(View.VISIBLE);
                groupContainer.setVisibility(View.VISIBLE);
                groupContainer.setVisibility(View.GONE);
                generateVirtualAddress(UUID.fromString(labelUuidView.getText().toString()));
                break;
        }
    }

    private void updateFixedGroupAddressVisibility(final int address, final boolean enabled) {
        addressInput.setText(MeshAddress.formatAddress(address, false));
        addressInputLayout.setEnabled(enabled);
        groupNameInputLayout.setVisibility(View.GONE);
        groupContainer.setVisibility(View.GONE);
        labelContainer.setVisibility(View.GONE);
        mGenerateLabelUUID.setVisibility(View.GONE);
    }

    private void generateVirtualAddress(@NonNull final UUID uuid) {
        final Group group1 = ((GroupCallbacks) requireActivity()).createGroup(uuid, groupNameInput.getEditableText().toString().trim());
        if (group1 != null) {
            addressInput.setText(MeshAddress.formatAddress(group1.getAddress(), false));
        }
    }

    private int getGroupIndex(final int address) {
        for (int i = 0; i < mGroups.size(); i++) {
            if (address == mGroups.get(i).getAddress()) {
                return i;
            }
        }
        return 0;
    }

    private boolean validateInput(@NonNull final String input) {

        try {
            if (input.length() % 4 != 0 || !input.matches(Utils.HEX_PATTERN)) {
                addressInputLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }
            final AddressTypes type = (AddressTypes) addressTypesSpinnerView.getSelectedItem();

            final int address = Integer.parseInt(input, 16);
            switch (type) {
                default:
                    if (!MeshAddress.isValidUnassignedAddress(address)) {
                        addressInputLayout.setError(getString(R.string.invalid_address_value));
                        return false;
                    }
                    return true;
                case UNICAST_ADDRESS:
                    if (!MeshAddress.isValidUnicastAddress(address)) {
                        addressInputLayout.setError(getString(R.string.invalid_unicast_address));
                        return false;
                    }
                    return true;
                case GROUP_ADDRESS:
                    if (!MeshAddress.isValidGroupAddress(address)) {
                        addressInputLayout.setError(getString(R.string.invalid_group_address));
                        return false;
                    }
                    for (Group group : mGroups) {
                        if (address == group.getAddress()) {
                            addressInputLayout.setError(getString(R.string.error_group_address_in_used));
                            return false;
                        }
                    }
                    return true;
                case VIRTUAL_ADDRESS:
                    //do nothing since the library generates it
                    return true;
            }
        } catch (IllegalArgumentException ex) {
            addressInputLayout.setError(ex.getMessage());
            return false;
        }
    }

    private boolean validateInput(@NonNull final String name,
                                  @NonNull final String address) {
        try {
            if (TextUtils.isEmpty(name)) {
                groupNameInputLayout.setError(getString(R.string.error_empty_group_name));
                return false;
            }
            if (address.length() % 4 != 0 || !address.matches(Utils.HEX_PATTERN)) {
                addressInputLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }

            final int groupAddress = Integer.valueOf(address, 16);
            if (!MeshAddress.isValidGroupAddress(groupAddress)) {
                addressInputLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }

            for (Group group : mGroups) {
                if (groupAddress == group.getAddress()) {
                    addressInputLayout.setError(getString(R.string.error_group_address_in_used));
                    return false;
                }
            }
        } catch (IllegalArgumentException ex) {
            addressInputLayout.setError(ex.getMessage());
            return false;
        }

        return true;
    }
}
