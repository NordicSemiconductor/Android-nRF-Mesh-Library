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

package no.nordicsemi.android.nrfmeshprovisioner.ble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.nrfmeshprovisioner.ProvisioningActivity;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.ble.adapter.DevicesAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ScannerLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ScannerViewModel;

public class ScannerActivity extends AppCompatActivity implements Injectable,
        DevicesAdapter.OnItemClickListener {
    private static final int REQUEST_ENABLE_BLUETOOTH = 1021; // random number
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 1022; // random number

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.state_scanning)
    View mScanningView;
    @BindView(R.id.no_devices)
    View mEmptyView;
    @BindView(R.id.no_location_permission)
    View mNoLocationPermissionView;
    @BindView(R.id.action_grant_location_permission)
    Button mGrantPermissionButton;
    @BindView(R.id.action_permission_settings)
    Button mPermissionSettingsButton;
    @BindView(R.id.no_location)
    View mNoLocationView;
    @BindView(R.id.bluetooth_off)
    View mNoBluetoothView;

    private ScannerViewModel mViewModel;
    private boolean mScanWithProxyService;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        ButterKnife.bind(this);

        // Create view model containing utility methods for scanning
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(ScannerViewModel.class);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_scanner);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() != null) {
            mScanWithProxyService = getIntent().getBooleanExtra(Utils.EXTRA_DATA_PROVISIONING_SERVICE, true);
            if (mScanWithProxyService) {
                getSupportActionBar().setSubtitle(R.string.sub_title_scanning_nodes);
            } else {
                getSupportActionBar().setSubtitle(R.string.sub_title_scanning_proxy_node);
            }
        }

        // Configure the recycler view
        final RecyclerView recyclerViewDevices = findViewById(R.id.recycler_view_ble_devices);
        recyclerViewDevices.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewDevices.getContext(), DividerItemDecoration.VERTICAL);
        recyclerViewDevices.addItemDecoration(dividerItemDecoration);

        final SimpleItemAnimator itemAnimator = (SimpleItemAnimator) recyclerViewDevices.getItemAnimator();
        if (itemAnimator != null) itemAnimator.setSupportsChangeAnimations(false);

        final DevicesAdapter adapter = new DevicesAdapter(this, mViewModel.getScannerRepository().getScannerState());
        adapter.setOnItemClickListener(this);
        recyclerViewDevices.setAdapter(adapter);

        mViewModel.getScannerRepository().getScannerState().observe(this, this::startScan);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.getScannerRepository().getScannerState().startScanning();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScan();
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
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ReconnectActivity.REQUEST_DEVICE_READY) {
            if (resultCode == RESULT_OK) {
                final boolean isDeviceReady = data.getBooleanExtra(Utils.ACTIVITY_RESULT, false);
                if (isDeviceReady) {
                    finish();
                }
            }
        } else if (requestCode == Utils.PROVISIONING_SUCCESS) {
            if (resultCode == RESULT_OK) {
                setResultIntent(data);
            }
        } else if (requestCode == Utils.CONNECT_TO_NETWORK) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        } else if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                startScan(mViewModel.getScannerRepository().getScannerState());
            }
        }
    }

    @Override
    public void onItemClick(final ExtendedBluetoothDevice device) {
        //We must disconnect from any nodes that we are connected to before we start scanning.
        mViewModel.disconnect();
        final Intent intent;
        stopScan();
        if (mScanWithProxyService) {
            intent = new Intent(this, ProvisioningActivity.class);
            intent.putExtra(Utils.EXTRA_DEVICE, device);
            startActivityForResult(intent, Utils.PROVISIONING_SUCCESS);
        } else {
            intent = new Intent(this, ReconnectActivity.class);
            intent.putExtra(Utils.EXTRA_DEVICE, device);
            startActivityForResult(intent, Utils.CONNECT_TO_NETWORK);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_COARSE_LOCATION) {
            mViewModel.getScannerRepository().getScannerState().refresh();
        }
    }

    @OnClick(R.id.action_enable_location)
    public void onEnableLocationClicked() {
        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    @OnClick(R.id.action_enable_bluetooth)
    public void onEnableBluetoothClicked() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
    }

    @OnClick(R.id.action_grant_location_permission)
    public void onGrantLocationPermissionClicked() {
        Utils.markLocationPermissionRequested(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
    }

    @OnClick(R.id.action_permission_settings)
    public void onPermissionSettingsClicked() {
        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    /**
     * Start scanning for Bluetooth devices or displays a message based on the scanner state.
     */
    private void startScan(final ScannerLiveData state) {
        // First, check the Location permission. This is required on Marshmallow onwards in order to scan for Bluetooth LE devices.
        if (Utils.isLocationPermissionsGranted(this)) {
            mNoLocationPermissionView.setVisibility(View.GONE);

            // Bluetooth must be enabled
            if (state.isBluetoothEnabled()) {
                mNoBluetoothView.setVisibility(View.GONE);

                // We are now OK to start scanning
                if (mScanWithProxyService) {
                    mViewModel.getScannerRepository().startScan(BleMeshManager.MESH_PROVISIONING_UUID);
                } else {
                    mViewModel.getScannerRepository().startScan(BleMeshManager.MESH_PROXY_UUID);
                }
                mScanningView.setVisibility(View.VISIBLE);

                if (state.isEmpty()) {
                    mEmptyView.setVisibility(View.VISIBLE);

                    if (!Utils.isLocationRequired(this) || Utils.isLocationEnabled(this)) {
                        mNoLocationView.setVisibility(View.INVISIBLE);
                    } else {
                        mNoLocationView.setVisibility(View.VISIBLE);
                    }
                } else {
                    mEmptyView.setVisibility(View.GONE);
                }
            } else {
                mNoBluetoothView.setVisibility(View.VISIBLE);
                mScanningView.setVisibility(View.INVISIBLE);
                mEmptyView.setVisibility(View.GONE);
            }
        } else {
            mNoLocationPermissionView.setVisibility(View.VISIBLE);
            mNoBluetoothView.setVisibility(View.GONE);
            mScanningView.setVisibility(View.INVISIBLE);
            mEmptyView.setVisibility(View.GONE);

            final boolean deniedForever = Utils.isLocationPermissionDeniedForever(this);
            mGrantPermissionButton.setVisibility(deniedForever ? View.GONE : View.VISIBLE);
            mPermissionSettingsButton.setVisibility(deniedForever ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * stop scanning for bluetooth devices.
     */
    private void stopScan() {
        mViewModel.getScannerRepository().stopScan();
    }

    private void setResultIntent(final Intent data) {
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
