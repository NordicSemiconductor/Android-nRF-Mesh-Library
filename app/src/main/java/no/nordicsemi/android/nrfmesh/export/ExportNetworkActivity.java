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

package no.nordicsemi.android.nrfmesh.export;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.Provisioner;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityExportBinding;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentMeshExportMsg;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentPermissionRationale;
import no.nordicsemi.android.nrfmesh.export.adapters.SelectableNetworkKeyAdapter;
import no.nordicsemi.android.nrfmesh.export.adapters.SelectableProvisionerAdapter;
import no.nordicsemi.android.nrfmesh.provisioners.AddProvisionerActivity;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.viewmodels.ExportNetworkViewModel;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

@AndroidEntryPoint
public class ExportNetworkActivity extends AppCompatActivity implements
        DialogFragmentPermissionRationale.StoragePermissionListener,
        SelectableProvisionerAdapter.OnItemCheckedChangedListener,
        SelectableNetworkKeyAdapter.OnItemCheckedChangedListener {

    private static final int WRITE_TO_FILE = 2011;
    private static final int REQUEST_STORAGE_PERMISSION = 2023; // random number

    private ActivityExportBinding binding;
    private ExportNetworkViewModel mViewModel;


    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mViewModel = new ViewModelProvider(this).get(ExportNetworkViewModel.class);

        final Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.export);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final SelectableProvisionerAdapter provisionerAdapter = new SelectableProvisionerAdapter(this, mViewModel.getNetworkLiveData());
        provisionerAdapter.setOnItemCheckedChangedListener(this);
        final SelectableNetworkKeyAdapter networkKeyAdapter = new SelectableNetworkKeyAdapter(this, mViewModel.getNetworkLiveData());
        networkKeyAdapter.setOnItemCheckedChangedListener(this);

        final NestedScrollView nestedScrollView = binding.scrollView;
        final RecyclerView recyclerViewProvisioners = binding.partialConfigurationContainer.provisioners;
        final RecyclerView recyclerNetworkKeys = binding.partialConfigurationContainer.networkKeys;
        final MaterialButton addProvisioner = findViewById(R.id.action_add_provisioner);
        final ExtendedFloatingActionButton fab = findViewById(R.id.fab_export);

        recyclerViewProvisioners.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProvisioners.setItemAnimator(new DefaultItemAnimator());
        recyclerViewProvisioners.setAdapter(provisionerAdapter);

        recyclerNetworkKeys.setLayoutManager(new LinearLayoutManager(this));
        recyclerNetworkKeys.setItemAnimator(new DefaultItemAnimator());
        recyclerNetworkKeys.setAdapter(networkKeyAdapter);

        addProvisioner.setOnClickListener(v -> startActivity(new Intent(this, AddProvisionerActivity.class)));

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY == 0) fab.extend();
            else fab.shrink();
        });

        fab.setOnClickListener(v -> handleNetworkExport());

        binding.switchExport.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mViewModel.setExportEverything(isChecked);
            if (isChecked) {
                binding.rationaleExportFullConfig.setVisibility(VISIBLE);
                binding.partialConfigurationContainer.getRoot().setVisibility(GONE);
            } else {
                binding.rationaleExportFullConfig.setVisibility(GONE);
                binding.partialConfigurationContainer.getRoot().setVisibility(VISIBLE);
            }
        });

        binding.partialConfigurationContainer.switchExportDeviceKeys.setOnCheckedChangeListener((buttonView, isChecked) -> mViewModel.setExportDeviceKeys(isChecked));

        mViewModel.getExportStatus().observe(this, aVoid -> {
            if (mViewModel.isExportEverything()) {
                fab.setEnabled(true);
            } else {
                fab.setEnabled(!mViewModel.getProvisioners().isEmpty() && !mViewModel.getNetworkKeys().isEmpty());
            }
        });

        mViewModel.getNetworkExportState().observe(this, networkExportState -> {
            final String title = getString(R.string.title_network_export);
            final DialogFragmentMeshExportMsg fragment =
                    DialogFragmentMeshExportMsg.newInstance(R.drawable.ic_info_outline,
                            title, networkExportState);
            fragment.show(getSupportFragmentManager(), null);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WRITE_TO_FILE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    if (uri != null) {
                        try {
                            final OutputStream stream = getContentResolver().openOutputStream(uri);
                            if (stream != null)
                                if (mViewModel.exportNetwork(stream)) {
                                    displayExportSuccessSnackBar();
                                }
                        } catch (Exception ex) {
                            displayExportErrorDialog(ex.getMessage());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onProvisionerCheckedChanged(@NonNull final Provisioner provisioner, final boolean isChecked) {
        if (isChecked)
            mViewModel.addProvisioner(provisioner);
        else
            mViewModel.removeProvisioner(provisioner);
    }

    @Override
    public void onNetworkKeyChecked(@NonNull final NetworkKey networkKey, final boolean isChecked) {
        if (isChecked)
            mViewModel.addNetworkKey(networkKey);
        else
            mViewModel.removeNetworkKey(networkKey);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (PackageManager.PERMISSION_GRANTED != grantResults[0]) {
                mViewModel.displaySnackBar(this, binding.coordinator, getString(R.string.ext_storage_permission_denied), Snackbar.LENGTH_LONG);
            }
        }
    }

    @Override
    public void requestPermission() {
        Utils.markWriteStoragePermissionRequested(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
    }

    private void handleNetworkExport() {
        try {
            if (!Utils.isWriteExternalStoragePermissionsGranted(this)
                    || Utils.isWriteExternalStoragePermissionDeniedForever(this)) {
                DialogFragmentPermissionRationale
                        .newInstance(Utils.isWriteExternalStoragePermissionDeniedForever(this),
                                getString(R.string.title_permission_required),
                                getString(R.string.external_storage_permission_required))
                        .show(getSupportFragmentManager(), null);
            } else {
                final String networkName = mViewModel.getNetworkLiveData().getNetworkName();
                if (Utils.isKitkatOrAbove()) {
                    final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/json");
                    intent.putExtra(Intent.EXTRA_TITLE, networkName);
                    startActivityForResult(intent, WRITE_TO_FILE);
                } else {
                    if (mViewModel.exportNetwork()) {
                        displayExportSuccessSnackBar();
                    }
                }
            }
        } catch (Exception ex) {
            displayExportErrorDialog(ex.getMessage());
        }
    }

    private void displayExportSuccessSnackBar() {
        final String message = mViewModel.getNetworkLiveData().getMeshNetwork().getMeshName() + " has been successfully exported.";
        mViewModel.displaySnackBar(this, binding.coordinator, message, Snackbar.LENGTH_LONG);
    }

    private void displayExportErrorDialog(final String message) {
        final String title = getString(R.string.title_network_export);
        final DialogFragmentError fragment =
                DialogFragmentError.newInstance(title, message);
        fragment.show(getSupportFragmentManager(), null);
    }
}
