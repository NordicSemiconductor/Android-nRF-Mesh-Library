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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.Provisioner;
import no.nordicsemi.android.nrfmesh.R;
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

public class ExportNetworkActivity extends AppCompatActivity implements
        SelectableProvisionerAdapter.OnItemCheckedChangedListener,
        SelectableNetworkKeyAdapter.OnItemCheckedChangedListener {

    private static final int WRITE_TO_FILE = 2011;



    @BindView(R.id.coordinator)
    CoordinatorLayout mContainer;
    @BindView(R.id.switch_export)
    SwitchMaterial mSwitchExportEverything;
    @BindView(R.id.rationale_export_full_config)
    View mExportEverything;
    @BindView(R.id.partial_configuration_container)
    View mPartialConfigContainer;
    @BindView(R.id.switch_export_device_keys)
    SwitchMaterial mSwitchExportDeviceKeys;

    private SelectableProvisionerAdapter provisionerAdapter;
    private SelectableNetworkKeyAdapter networkKeyAdapter;

    private ExportNetworkViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.export);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        provisionerAdapter = new SelectableProvisionerAdapter(this, mViewModel.getNetworkLiveData());
        provisionerAdapter.setOnItemCheckedChangedListener(this);
        networkKeyAdapter = new SelectableNetworkKeyAdapter(this, mViewModel.getNetworkLiveData());
        networkKeyAdapter.setOnItemCheckedChangedListener(this);

        final NestedScrollView nestedScrollView = findViewById(R.id.scroll_view);
        final RecyclerView recyclerViewProvisioners = mPartialConfigContainer.findViewById(R.id.provisioners);
        final RecyclerView recyclerNetworkKeys = mPartialConfigContainer.findViewById(R.id.network_keys);
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

        mSwitchExportEverything.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mViewModel.setExportEverything(isChecked);
            if (isChecked) {
                mExportEverything.setVisibility(VISIBLE);
                mPartialConfigContainer.setVisibility(GONE);
            } else {
                mExportEverything.setVisibility(GONE);
                mPartialConfigContainer.setVisibility(VISIBLE);
            }
        });

        mSwitchExportDeviceKeys.setOnCheckedChangeListener((buttonView, isChecked) -> mViewModel.setExportDeviceKeys(isChecked));

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

    private void handleNetworkExport() {
        try {
            if (!Utils.isWriteExternalStoragePermissionsGranted(this)
                    || Utils.isWriteExternalStoragePermissionDeniedForever(this)) {
                final DialogFragmentPermissionRationale fragmentPermissionRationale = DialogFragmentPermissionRationale.
                        newInstance(Utils.isWriteExternalStoragePermissionDeniedForever(this),
                                getString(R.string.title_permission_required),
                                getString(R.string.external_storage_permission_required));
                fragmentPermissionRationale.show(getSupportFragmentManager(), null);
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
        mViewModel.displaySnackBar(this, mContainer, message, Snackbar.LENGTH_LONG);
    }

    private void displayExportErrorDialog(final String message) {
        final String title = getString(R.string.title_network_export);
        final DialogFragmentError fragment =
                DialogFragmentError.newInstance(title, message);
        fragment.show(getSupportFragmentManager(), null);
    }
}
