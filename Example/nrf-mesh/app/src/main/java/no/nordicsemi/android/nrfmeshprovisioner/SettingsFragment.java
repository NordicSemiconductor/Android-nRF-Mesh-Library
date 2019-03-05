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

package no.nordicsemi.android.nrfmeshprovisioner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentFlags;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentGlobalNetworkName;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentGlobalTtl;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentIvIndex;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentKeyIndex;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentMeshExportMsg;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentMeshImport;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentMeshImportMsg;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentNetworkKey;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentPermissionRationale;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentResetNetwork;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentSourceAddress;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentUnicastAddress;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SharedViewModel;

import static android.app.Activity.RESULT_OK;
import static no.nordicsemi.android.nrfmeshprovisioner.ManageAppKeysActivity.RESULT_APP_KEY_LIST_SIZE;
import static no.nordicsemi.android.nrfmeshprovisioner.viewmodels.NrfMeshRepository.EXPORT_PATH;

public class SettingsFragment extends Fragment implements Injectable,
        DialogFragmentGlobalNetworkName.DialogFragmentNetworkNameListener,
        DialogFragmentGlobalTtl.DialogFragmentGlobalTtlListener,
        DialogFragmentNetworkKey.DialogFragmentNetworkKeyListener,
        DialogFragmentKeyIndex.DialogFragmentKeyIndexListener,
        DialogFragmentFlags.DialogFragmentFlagsListener,
        DialogFragmentIvIndex.DialogFragmentIvIndexListener,
        DialogFragmentUnicastAddress.DialogFragmentUnicastAddressListener,
        DialogFragmentSourceAddress.DialogFragmentSourceAddressListener,
        DialogFragmentResetNetwork.DialogFragmentResetNetworkListener,
        DialogFragmentMeshImport.DialogFragmentNetworkImportListener,
DialogFragmentPermissionRationale.StoragePermissionListener {

    private static final int REQUEST_STORAGE_PERMISSION = 2023; // random number
    private static final int READ_FILE_REQUEST_CODE = 42;
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private SharedViewModel mViewModel;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private TextView manageAppKeysView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View rootView = inflater.inflate(R.layout.fragment_settings, null);

        mViewModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(SharedViewModel.class);

        // Set up views
        final View containerNetworkName = rootView.findViewById(R.id.container_network_name);
        containerNetworkName.findViewById(R.id.image).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_lan_black_alpha_24dp));
        final TextView networkNameTitle = containerNetworkName.findViewById(R.id.title);
        networkNameTitle.setText(R.string.summary_global_network_name);
        final TextView networkNameView = containerNetworkName.findViewById(R.id.text);
        containerNetworkName.setOnClickListener(v -> {
            final DialogFragmentGlobalNetworkName dialogFragmentNetworkKey = DialogFragmentGlobalNetworkName.newInstance(networkNameView.getText().toString());
            dialogFragmentNetworkKey.show(getChildFragmentManager(), null);
        });

        final View containerGlobalTtl = rootView.findViewById(R.id.container_global_ttl);
        containerGlobalTtl.findViewById(R.id.image).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_timer));
        final TextView globalTtlTitle = containerGlobalTtl.findViewById(R.id.title);
        globalTtlTitle.setText(R.string.summary_global_ttl);
        final TextView globalTtlView = containerGlobalTtl.findViewById(R.id.text);
        containerGlobalTtl.setOnClickListener(v -> {
            final DialogFragmentGlobalTtl dialogFragmentGlobalTtl = DialogFragmentGlobalTtl.newInstance(globalTtlView.getText().toString());
            dialogFragmentGlobalTtl.show(getChildFragmentManager(), null);
        });

        final View containerSourceAddress = rootView.findViewById(R.id.container_src_address);
        containerSourceAddress.findViewById(R.id.image).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_lan_black_alpha_24dp));
        final TextView containerSource = containerSourceAddress.findViewById(R.id.title);
        containerSource.setText(R.string.summary_src_address);
        final TextView sourceAddressView = containerSourceAddress.findViewById(R.id.text);
        containerSourceAddress.setOnClickListener(v -> {
            final byte[] configuratorSrc = mViewModel.getMeshNetworkLiveData().getProvisionerAddress();
            final int src = (configuratorSrc[0] & 0xFF) << 8 | (configuratorSrc[1] & 0xFF);
            final DialogFragmentSourceAddress dialogFragmentSrc = DialogFragmentSourceAddress.newInstance(src);
            dialogFragmentSrc.show(getChildFragmentManager(), null);
        });

        final View containerKey = rootView.findViewById(R.id.container_key);
        containerKey.findViewById(R.id.image).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_vpn_key_black_alpha_24dp));
        final TextView keyTitle = containerKey.findViewById(R.id.title);
        keyTitle.setText(R.string.summary_key);
        final TextView keyView = containerKey.findViewById(R.id.text);
        containerKey.setOnClickListener(v -> {
            final NetworkKey networkKey = mViewModel.getMeshNetworkLiveData().getPrimaryNetworkKey();
            final DialogFragmentNetworkKey dialogFragmentNetworkKey = DialogFragmentNetworkKey.newInstance(networkKey);
            dialogFragmentNetworkKey.show(getChildFragmentManager(), null);
        });

        final View containerKeyIndex = rootView.findViewById(R.id.container_index);
        containerKeyIndex.findViewById(R.id.image).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_numeric));
        final TextView keyIndexTitle = containerKeyIndex.findViewById(R.id.title);
        keyIndexTitle.setText(R.string.summary_index);
        final TextView keyIndexView = containerKeyIndex.findViewById(R.id.text);
        containerKeyIndex.setOnClickListener(v -> {
            final int keyIndex = mViewModel.getMeshNetworkLiveData().getKeyIndex();
            final DialogFragmentKeyIndex dialogFragmentNetworkKey = DialogFragmentKeyIndex.newInstance(keyIndex);
            dialogFragmentNetworkKey.show(getChildFragmentManager(), null);
        });

        final View containerFlags = rootView.findViewById(R.id.container_flags);
        containerFlags.findViewById(R.id.image).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_flag));
        final TextView flagsTitle = containerFlags.findViewById(R.id.title);
        flagsTitle.setText(R.string.summary_flags);
        final TextView flagsView = containerFlags.findViewById(R.id.text);
        containerFlags.setOnClickListener(v -> {
            final int flags = mViewModel.getMeshNetworkLiveData().getFlags();
            final int keyRefreshFlag = MeshParserUtils.getBitValue(flags, 0);
            final int ivUpdateFlag = MeshParserUtils.getBitValue(flags, 1);
            final DialogFragmentFlags dialogFragmentFlags = DialogFragmentFlags.newInstance(keyRefreshFlag, ivUpdateFlag);
            dialogFragmentFlags.show(getChildFragmentManager(), null);
        });

        final View containerIVIndex = rootView.findViewById(R.id.container_iv_index);
        containerIVIndex.findViewById(R.id.image).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_list));
        final TextView ivIndexTitle = containerIVIndex.findViewById(R.id.title);
        ivIndexTitle.setText(R.string.title_iv_index);
        final TextView ivIndexView = containerIVIndex.findViewById(R.id.text);
        containerIVIndex.setOnClickListener(v -> {
            final int ivIndex = mViewModel.getMeshNetworkLiveData().getIvIndex();
            final DialogFragmentIvIndex dialogFragmentFlags = DialogFragmentIvIndex.newInstance(ivIndex);
            dialogFragmentFlags.show(getChildFragmentManager(), null);
        });

        final View containerUnicastAddress = rootView.findViewById(R.id.container_supported_algorithm);
        containerUnicastAddress.findViewById(R.id.image).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_lan_black_alpha_24dp));
        final TextView unicastAddressTitle = containerUnicastAddress.findViewById(R.id.title);
        unicastAddressTitle.setText(R.string.summary_unicast_address);
        final TextView unicastAddressView = containerUnicastAddress.findViewById(R.id.text);
        containerUnicastAddress.setOnClickListener(v -> {
            final int unicastAddress = mViewModel.getMeshNetworkLiveData().getValue().getUnicastAddress();
            final DialogFragmentUnicastAddress dialogFragmentFlags = DialogFragmentUnicastAddress.newInstance(unicastAddress);
            dialogFragmentFlags.show(getChildFragmentManager(), null);
        });

        final View containerManageAppKeys = rootView.findViewById(R.id.container_app_keys);
        containerManageAppKeys.findViewById(R.id.image).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_folder_key_black_24dp_alpha));
        final TextView manageAppKeys = containerManageAppKeys.findViewById(R.id.title);
        manageAppKeys.setText(R.string.summary_app_keys);
        manageAppKeysView = containerManageAppKeys.findViewById(R.id.text);
        containerManageAppKeys.setOnClickListener(v -> {
            final Intent intent = new Intent(getActivity(), ManageAppKeysActivity.class);
            intent.putExtra(Utils.EXTRA_DATA, Utils.MANAGE_APP_KEY);
            startActivityForResult(intent, ManageAppKeysActivity.MANAGE_APP_KEYS);
        });

        final View containerAbout = rootView.findViewById(R.id.container_version);
        containerAbout.findViewById(R.id.image).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_puzzle));
        final TextView versionTitle = containerAbout.findViewById(R.id.title);
        versionTitle.setText(R.string.summary_verion);
        final TextView version = containerAbout.findViewById(R.id.text);
        try {
            version.setText(getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mViewModel.getMeshNetworkLiveData().observe(this, meshNetworkLiveData -> {
            if (meshNetworkLiveData != null) {
                networkNameView.setText(meshNetworkLiveData.getNetworkName());
                globalTtlView.setText(String.valueOf(meshNetworkLiveData.getGlobalTtl()));
                final NetworkKey key = meshNetworkLiveData.getPrimaryNetworkKey();
                keyView.setText(MeshParserUtils.bytesToHex(key.getKey(), false));
                keyIndexView.setText(getString(R.string.hex_format, String.format(Locale.US, "%03X", key.getKeyIndex())));
                flagsView.setText(parseFlagsMessage(meshNetworkLiveData.getFlags()));
                ivIndexView.setText(getString(R.string.hex_format, String.format(Locale.US, "%08X", meshNetworkLiveData.getIvIndex())));
                unicastAddressView.setText(getString(R.string.hex_format, String.format(Locale.US, "%04X", meshNetworkLiveData.getUnicastAddress())));
                manageAppKeysView.setText(getString(R.string.app_key_count, meshNetworkLiveData.getAppKeys().size()));
                sourceAddressView.setText(MeshParserUtils.bytesToHex(meshNetworkLiveData.getProvisionerAddress(), true));
            }
        });

        mViewModel.getNetworkLoadState().observe(this, networkImportState -> {
            final String title = getString(R.string.title_network_import);
            final DialogFragmentMeshImportMsg fragment =
                    DialogFragmentMeshImportMsg.newInstance(R.drawable.ic_info_outline_black_alpha,
                            title, networkImportState);
            fragment.show(getChildFragmentManager(), null);
        });

        mViewModel.getNetworkExportState().observe(this, networkExportState -> {
            final String title = getString(R.string.title_network_export);
            final DialogFragmentMeshExportMsg fragment =
                    DialogFragmentMeshExportMsg.newInstance(R.drawable.ic_info_outline_black_alpha,
                            title, networkExportState);
            fragment.show(getChildFragmentManager(), null);
        });

        return rootView;

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.network_settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_import_network:
                final String title = getString(R.string.title_network_import);
                final String message = getString(R.string.network_import_rationale);
                final DialogFragmentMeshImport fragment = DialogFragmentMeshImport.newInstance(title, message);
                fragment.show(getChildFragmentManager(), null);
                return true;
            case R.id.action_export_network:
                handleNetworkExport();
                break;
            case R.id.action_reset_network:
                final DialogFragmentResetNetwork dialogFragmentResetNetwork = DialogFragmentResetNetwork.
                        newInstance(getString(R.string.title_reset_network), getString(R.string.message_reset_network));
                dialogFragmentResetNetwork.show(getChildFragmentManager(), null);
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ManageAppKeysActivity.MANAGE_APP_KEYS:
                if (resultCode == RESULT_OK) {
                    final int size = data.getExtras().getInt(RESULT_APP_KEY_LIST_SIZE);
                    manageAppKeysView.setText(getString(R.string.app_key_count, size));
                }
                break;
            case READ_FILE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        final Uri uri = data.getData();
                        mViewModel.importMeshNetwork(uri);
                    }
                } else {
                    Log.e(TAG, "Error while opening file browser");
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_STORAGE_PERMISSION:
                if(PackageManager.PERMISSION_GRANTED != grantResults[0]){
                    Toast.makeText(getContext(), getString(R.string.ext_storage_permission_denied), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private String parseFlagsMessage(final int flags) {
        final int keyRefreshFlag = MeshParserUtils.getBitValue(flags, 0);
        final int ivUpdateFlag = MeshParserUtils.getBitValue(flags, 1);
        final StringBuilder flagsText = new StringBuilder();

        if (keyRefreshFlag == 0)
            flagsText.append(getString(R.string.key_refresh_phase_0)).append(", ");
        else
            flagsText.append(getString(R.string.key_refresh_phase_2)).append(", ");

        if (ivUpdateFlag == 0)
            flagsText.append(getString(R.string.normal_operation));
        else
            flagsText.append(getString(R.string.iv_update_active));

        return flagsText.toString();
    }

    @Override
    public void onNetworkNameEntered(final String networkName) {
        mViewModel.getMeshNetworkLiveData().setNetworkName(networkName);
    }

    @Override
    public void onGlobalTtlEntered(final int globalTtl) {
        mViewModel.getMeshNetworkLiveData().setGlobalTtl(globalTtl);
    }

    @Override
    public void onNetworkKeyGenerated(final String networkKey) {
        mViewModel.getMeshNetworkLiveData().setPrimaryNetworkKey(networkKey);
    }

    @Override
    public void onKeyIndexGenerated(final int keyIndex) {
        mViewModel.getMeshNetworkLiveData().setKeyIndex(keyIndex);
    }

    @Override
    public void onFlagsSelected(final int keyRefreshFlag, final int ivUpdateFlag) {
        mViewModel.getMeshNetworkLiveData().setFlags(MeshParserUtils.parseUpdateFlags(keyRefreshFlag, ivUpdateFlag));
    }

    @Override
    public void setIvIndex(final int ivIndex) {
        mViewModel.getMeshNetworkLiveData().setIvIndex(ivIndex);
    }

    @Override
    public void setUnicastAddress(final int unicastAddress) {
        mViewModel.getMeshNetworkLiveData().setUnicastAddress(unicastAddress);
    }

    @Override
    public boolean setSourceAddress(final int sourceAddress) {
        return mViewModel.getMeshNetworkLiveData().setProvisionerAddress(sourceAddress);
    }

    @Override
    public void onNetworkReset() {
        mViewModel.resetMeshNetwork();
    }

    @Override
    public void onNetworkImportConfirmed() {
        performFileSearch();
    }

    @Override
    public void requestPermission() {
        Utils.markWriteStoragePermissionRequested(getContext());
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
    }

    /**
     * Fires an intent to spin up the "file chooser" UI to select a file
     */
    public void performFileSearch() {
        final Intent intent;
        if (Utils.isKitkatOrAbove()) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_FILE_REQUEST_CODE);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void handleNetworkExport() {
        if (!Utils.isWriteExternalStoragePermissionsGranted(getContext())
                || Utils.isWriteExternalStoragePermissionDeniedForever(getActivity())) {
            final DialogFragmentPermissionRationale fragmentPermissionRationale = DialogFragmentPermissionRationale.
                    newInstance(Utils.isWriteExternalStoragePermissionDeniedForever(getActivity()),
                            getString(R.string.title_permission_required),
                            getString(R.string.external_storage_permission_required));
            fragmentPermissionRationale.show(getChildFragmentManager(), null);
        } else {
            final File f = new File(EXPORT_PATH);
            if(!f.exists()) {
                f.mkdirs();
            }
            mViewModel.getMeshManagerApi().exportMeshNetwork(EXPORT_PATH);
        }
    }
}
