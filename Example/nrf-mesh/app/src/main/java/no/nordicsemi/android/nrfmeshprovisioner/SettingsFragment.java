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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
import java.io.FileNotFoundException;
import java.io.OutputStream;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentMeshExportMsg;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentMeshImport;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentMeshImportMsg;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentNetworkName;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentPermissionRationale;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentResetNetwork;
import no.nordicsemi.android.nrfmeshprovisioner.keys.AppKeysActivity;
import no.nordicsemi.android.nrfmeshprovisioner.keys.NetKeysActivity;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.ProvisionersActivity;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SharedViewModel;

import static android.app.Activity.RESULT_OK;
import static no.nordicsemi.android.nrfmeshprovisioner.viewmodels.NrfMeshRepository.EXPORT_PATH;

public class SettingsFragment extends Fragment implements Injectable,
        DialogFragmentNetworkName.DialogFragmentNetworkNameListener,
        DialogFragmentResetNetwork.DialogFragmentResetNetworkListener,
        DialogFragmentMeshImport.DialogFragmentNetworkImportListener,
        DialogFragmentPermissionRationale.StoragePermissionListener {

    private static final int REQUEST_STORAGE_PERMISSION = 2023; // random number
    private static final int READ_FILE_REQUEST_CODE = 42;
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private SharedViewModel mViewModel;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View rootView = inflater.inflate(R.layout.fragment_settings, null);
        mViewModel = new ViewModelProvider(requireActivity(), mViewModelFactory).get(SharedViewModel.class);

        // Set up views
        final View containerNetworkName = rootView.findViewById(R.id.container_network_name);
        containerNetworkName.findViewById(R.id.image)
                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_label_black_alpha_24dp));
        final TextView networkNameTitle = containerNetworkName.findViewById(R.id.title);
        networkNameTitle.setText(R.string.title_network_name);
        final TextView networkNameView = containerNetworkName.findViewById(R.id.text);
        networkNameView.setVisibility(View.VISIBLE);
        containerNetworkName.setOnClickListener(v -> {
            final DialogFragmentNetworkName fragment = DialogFragmentNetworkName.
                    newInstance(networkNameView.getText().toString());
            fragment.show(getChildFragmentManager(), null);
        });

        final View containerProvisioner = rootView.findViewById(R.id.container_provisioners);
        containerProvisioner.findViewById(R.id.image)
                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_folder_provisioner_black_alpha_24dp));
        final TextView provisionerTitle = containerProvisioner.findViewById(R.id.title);
        final TextView provisionerSummary = containerProvisioner.findViewById(R.id.text);
        provisionerSummary.setVisibility(View.VISIBLE);
        provisionerTitle.setText(R.string.title_provisioners);
        containerProvisioner.setOnClickListener(v -> {
            final Intent intent = new Intent(requireContext(), ProvisionersActivity.class);
            startActivity(intent);
        });

        final View containerNetKey = rootView.findViewById(R.id.container_net_keys);
        containerNetKey.findViewById(R.id.image)
                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_folder_key_black_24dp_alpha));
        final TextView keyTitle = containerNetKey.findViewById(R.id.title);
        keyTitle.setText(R.string.title_net_keys);
        final TextView netKeySummary = containerNetKey.findViewById(R.id.text);
        netKeySummary.setVisibility(View.VISIBLE);
        containerNetKey.setOnClickListener(v -> {
            final Intent intent = new Intent(requireContext(), NetKeysActivity.class);
            intent.putExtra(Utils.EXTRA_DATA, Utils.MANAGE_NET_KEY);
            startActivity(intent);
        });

        final View containerAppKey = rootView.findViewById(R.id.container_app_keys);
        containerAppKey.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_folder_key_black_24dp_alpha));
        ((TextView) containerAppKey.findViewById(R.id.title)).setText(R.string.title_app_keys);
        final TextView appKeySummary = containerAppKey.findViewById(R.id.text);
        appKeySummary.setVisibility(View.VISIBLE);
        containerAppKey.setOnClickListener(v -> {
            final Intent intent = new Intent(requireContext(), AppKeysActivity.class);
            startActivity(intent);
        });

        final View containerAbout = rootView.findViewById(R.id.container_version);
        containerAbout.setClickable(false);
        containerAbout.findViewById(R.id.image).
                setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_puzzle));
        final TextView versionTitle = containerAbout.findViewById(R.id.title);
        versionTitle.setText(R.string.summary_version);
        final TextView version = containerAbout.findViewById(R.id.text);
        version.setVisibility(View.VISIBLE);
        try {
            version.setText(getContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mViewModel.getNetworkLiveData().observe(getViewLifecycleOwner(), meshNetworkLiveData -> {
            if (meshNetworkLiveData != null) {
                networkNameView.setText(meshNetworkLiveData.getNetworkName());
                netKeySummary.setText(String.valueOf(meshNetworkLiveData.getNetworkKeys().size()));
                provisionerSummary.setText(String.valueOf(meshNetworkLiveData.getProvisioners().size()));
                appKeySummary.setText(String.valueOf(meshNetworkLiveData.getAppKeys().size()));
            }
        });

        mViewModel.getNetworkLoadState().observe(getViewLifecycleOwner(), networkImportState -> {
            final String title = getString(R.string.title_network_import);
            final DialogFragmentMeshImportMsg fragment =
                    DialogFragmentMeshImportMsg.newInstance(R.drawable.ic_info_outline_black_alpha,
                            title, networkImportState);
            fragment.show(getChildFragmentManager(), null);
        });

        mViewModel.getNetworkExportState().observe(getViewLifecycleOwner(), networkExportState -> {
            final String title = getString(R.string.title_network_export);
            final DialogFragmentMeshExportMsg fragment =
                    DialogFragmentMeshExportMsg.newInstance(R.drawable.ic_info_outline_black_alpha,
                            title, networkExportState);
            fragment.show(getChildFragmentManager(), null);
        });

        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.network_settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_FILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    //Disconnect from network before importing
                    mViewModel.disconnect();
                    final Uri uri = data.getData();
                    mViewModel.getMeshManagerApi().importMeshNetwork(uri);
                }
            } else {
                Log.e(TAG, "Error while opening file browser");
            }
        } else if (requestCode == 2011) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    try {
                        final OutputStream stream = requireContext().getContentResolver().openOutputStream(uri);
                        mViewModel.exportMeshNetwork(stream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (PackageManager.PERMISSION_GRANTED != grantResults[0]) {
                Toast.makeText(getContext(), getString(R.string.ext_storage_permission_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onNetworkNameEntered(@NonNull final String name) {
        mViewModel.getNetworkLiveData().setNetworkName(name);
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
    private void performFileSearch() {
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

    private void handleNetworkExport() {
        if (!Utils.isWriteExternalStoragePermissionsGranted(getContext())
                || Utils.isWriteExternalStoragePermissionDeniedForever(requireActivity())) {
            final DialogFragmentPermissionRationale fragmentPermissionRationale = DialogFragmentPermissionRationale.
                    newInstance(Utils.isWriteExternalStoragePermissionDeniedForever(requireActivity()),
                            getString(R.string.title_permission_required),
                            getString(R.string.external_storage_permission_required));
            fragmentPermissionRationale.show(getChildFragmentManager(), null);
        } else {
            final String networkName = mViewModel.getNetworkLiveData().getNetworkName();
            if (Utils.isKitkatOrAbove()) {
                final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_TITLE, networkName);
                startActivityForResult(intent, 2011);
            } else {
                mViewModel.exportMeshNetwork();
            }
        }
    }
}
