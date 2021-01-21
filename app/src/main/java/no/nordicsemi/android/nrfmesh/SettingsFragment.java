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

package no.nordicsemi.android.nrfmesh;

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

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.nrfmesh.databinding.FragmentSettingsBinding;
import no.nordicsemi.android.nrfmesh.databinding.LayoutContainerBinding;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentMeshExportMsg;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentMeshImport;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentMeshImportMsg;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentNetworkName;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentResetNetwork;
import no.nordicsemi.android.nrfmesh.export.ExportNetworkActivity;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;
import no.nordicsemi.android.nrfmesh.keys.NetKeysActivity;
import no.nordicsemi.android.nrfmesh.provisioners.ProvisionersActivity;
import no.nordicsemi.android.nrfmesh.scenes.ScenesActivity;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.viewmodels.SharedViewModel;

import static android.app.Activity.RESULT_OK;

@AndroidEntryPoint
public class SettingsFragment extends Fragment implements
        DialogFragmentNetworkName.DialogFragmentNetworkNameListener,
        DialogFragmentResetNetwork.DialogFragmentResetNetworkListener,
        DialogFragmentMeshImport.DialogFragmentNetworkImportListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static final int READ_FILE_REQUEST_CODE = 42;
    private SharedViewModel mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup viewGroup, @Nullable final Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        final FragmentSettingsBinding binding = FragmentSettingsBinding.inflate(getLayoutInflater());

        // Set up views
        final LayoutContainerBinding containerNetworkName = binding.containerNetworkName;
        containerNetworkName.image
                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_label));
        final TextView networkNameTitle = binding.containerNetworkName.title;
        networkNameTitle.setText(R.string.title_network_name);
        final TextView networkNameView = binding.containerNetworkName.title;
        networkNameView.setVisibility(View.VISIBLE);
        containerNetworkName.getRoot().setOnClickListener(v -> {
            final DialogFragmentNetworkName fragment = DialogFragmentNetworkName.
                    newInstance(networkNameView.getText().toString());
            fragment.show(getChildFragmentManager(), null);
        });

        final LayoutContainerBinding containerProvisioner = binding.containerProvisioners;
        containerProvisioner.image
                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_folder_provisioner_24dp));
        final TextView provisionerTitle = containerProvisioner.title;
        final TextView provisionerSummary = containerProvisioner.text;
        provisionerSummary.setVisibility(View.VISIBLE);
        provisionerTitle.setText(R.string.title_provisioners);
        containerProvisioner.getRoot().setOnClickListener(v -> startActivity(new Intent(requireContext(), ProvisionersActivity.class)));

        final LayoutContainerBinding containerNetKey = binding.containerNetKeys;
        containerNetKey.image
                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_folder_key_24dp));
        containerNetKey.title.setText(R.string.title_net_keys);
        final TextView netKeySummary = containerNetKey.text;
        netKeySummary.setVisibility(View.VISIBLE);
        containerNetKey.getRoot().setOnClickListener(v -> {
            final Intent intent = new Intent(requireContext(), NetKeysActivity.class);
            intent.putExtra(Utils.EXTRA_DATA, Utils.MANAGE_NET_KEY);
            startActivity(intent);
        });

        final LayoutContainerBinding containerAppKey = binding.containerAppKeys;
        containerAppKey.image.
                setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_folder_key_24dp));
        containerAppKey.title.setText(R.string.title_app_keys);
        final TextView appKeySummary = containerAppKey.text;
        appKeySummary.setVisibility(View.VISIBLE);
        containerAppKey.getRoot().setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AppKeysActivity.class)));

        final LayoutContainerBinding containerScenes = binding.containerScenes;
        containerScenes.image.
                setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_palette_24dp));
        containerScenes.title.setText(R.string.title_scenes);
        final TextView scenesSummary = containerScenes.text;
        scenesSummary.setVisibility(View.VISIBLE);
        containerScenes.getRoot().setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ScenesActivity.class)));

        final LayoutContainerBinding containerIvTestMode = binding.containerIvTestMode;
        containerIvTestMode.image.
                setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_folder_key_24dp));
        containerIvTestMode.title.setText(R.string.title_iv_test_mode);
        final TextView ivTestModeSummary = containerIvTestMode.text;
        ivTestModeSummary.setText(R.string.iv_test_mode_summary);
        ivTestModeSummary.setVisibility(View.VISIBLE);
        final SwitchMaterial actionChangeIvTestMode = containerIvTestMode.actionChangeTestMode;
        actionChangeIvTestMode.setVisibility(View.VISIBLE);
        actionChangeIvTestMode.setChecked(mViewModel.getMeshManagerApi().isIvUpdateTestModeActive());
        actionChangeIvTestMode.setOnClickListener(v ->
                mViewModel.getMeshManagerApi().setIvUpdateTestModeActive(actionChangeIvTestMode.isChecked()));
        containerIvTestMode.getRoot().setOnClickListener(v ->
                DialogFragmentError.newInstance(getString(R.string.info), getString(R.string.iv_test_mode_info))
                        .show(getChildFragmentManager(), null)
        );

        final LayoutContainerBinding containerVersion = binding.containerVersion;
        containerVersion.getRoot().setClickable(false);
        containerVersion.image.
                setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_puzzle));
        containerVersion.title.setText(R.string.summary_version);
        final TextView version = containerVersion.text;
        version.setVisibility(View.VISIBLE);
        try {
            version.setText(requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mViewModel.getNetworkLiveData().observe(getViewLifecycleOwner(), meshNetworkLiveData -> {
            if (meshNetworkLiveData != null) {
                networkNameView.setText(meshNetworkLiveData.getNetworkName());
                netKeySummary.setText(String.valueOf(meshNetworkLiveData.getNetworkKeys().size()));
                provisionerSummary.setText(String.valueOf(meshNetworkLiveData.getProvisioners().size()));
                appKeySummary.setText(String.valueOf(meshNetworkLiveData.getAppKeys().size()));
                scenesSummary.setText(String.valueOf(meshNetworkLiveData.getScenes().size()));
            }
        });

        mViewModel.getNetworkLoadState().observe(getViewLifecycleOwner(), networkImportState -> {
            final String title = getString(R.string.title_network_import);
            final DialogFragmentMeshImportMsg fragment =
                    DialogFragmentMeshImportMsg.newInstance(R.drawable.ic_info_outline,
                            title, networkImportState);
            fragment.show(getChildFragmentManager(), null);
        });

        mViewModel.getNetworkExportState().observe(getViewLifecycleOwner(), networkExportState -> {
            final String title = getString(R.string.title_network_export);
            final DialogFragmentMeshExportMsg fragment =
                    DialogFragmentMeshExportMsg.newInstance(R.drawable.ic_info_outline,
                            title, networkExportState);
            fragment.show(getChildFragmentManager(), null);
        });

        return binding.getRoot();

    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.network_settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        if(id == R.id.action_import_network){
            final String title = getString(R.string.title_network_import);
            final String message = getString(R.string.network_import_rationale);
            final DialogFragmentMeshImport fragment = DialogFragmentMeshImport.newInstance(title, message);
            fragment.show(getChildFragmentManager(), null);
            return true;
        } else if (id == R.id.action_export_network){
            startActivity(new Intent(requireContext(), ExportNetworkActivity.class));
            return true;
        } else if (id == R.id.action_reset_network){
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
        if (requestCode == READ_FILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
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
                if (data != null && data.getData() != null) {
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
}
