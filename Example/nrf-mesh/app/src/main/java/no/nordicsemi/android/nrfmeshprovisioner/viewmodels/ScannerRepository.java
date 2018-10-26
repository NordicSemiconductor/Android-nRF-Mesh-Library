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

package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager.MESH_PROXY_UUID;

/**
 * Repository for scanning for bluetooth mesh devices
 */
public class ScannerRepository {

    private static final String TAG = ScannerRepository.class.getSimpleName();
    private final Context mContext;
    private final MeshManagerApi mMeshManagerApi;
    private String mNetworkId;

    /**
     * MutableLiveData containing the scanner state to notify MainActivity.
     */
    private final ScannerLiveData mScannerLiveData;

    private UUID mFilterUuid;

    private final ScanCallback mScanCallbacks = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            if (mFilterUuid.equals(BleMeshManager.MESH_PROVISIONING_UUID)) {
                // If the packet has been obtained while Location was disabled, mark Location as not required
                if (Utils.isLocationRequired(mContext) && !Utils.isLocationEnabled(mContext))
                    Utils.markLocationNotRequired(mContext);

                if (!mScannerLiveData.isStopScanRequested()) {
                    mScannerLiveData.deviceDiscovered(result);
                }
            } else if (mFilterUuid.equals(BleMeshManager.MESH_PROXY_UUID)) {
                final ScanRecord scanRecord = result.getScanRecord();
                if (scanRecord != null) {
                    final byte[] serviceData = scanRecord.getServiceData(new ParcelUuid((MESH_PROXY_UUID)));
                    if (serviceData != null) {
                        if (mMeshManagerApi != null) {
                            if (mMeshManagerApi.isAdvertisingWithNetworkIdentity(serviceData)) {
                                if (mMeshManagerApi.networkIdMatches(mNetworkId, serviceData)) {
                                    mScannerLiveData.deviceDiscovered(result);
                                }
                            } else if (mMeshManagerApi.isAdvertisedWithNodeIdentity(serviceData)) {
                                if (checkIfNodeIdentityMatches(serviceData)) {
                                    mScannerLiveData.deviceDiscovered(result);
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            // Batch scan is disabled (report delay = 0)
        }

        @Override
        public void onScanFailed(final int errorCode) {
            try {
                // TODO This should be handled
                mScannerLiveData.scanningStopped();
            } catch (Exception ex) {
                Log.v(TAG, ex.getMessage() + " : Error code: " + errorCode);
            }
        }
    };

    /**
     * Broadcast receiver to monitor the changes in the location provider
     */
    private final BroadcastReceiver mLocationProviderChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final boolean enabled = Utils.isLocationEnabled(context);
            mScannerLiveData.setLocationEnabled(enabled);
        }
    };
    /**
     * Broadcast receiver to monitor the changes in the bluetooth adapter
     */
    private final BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            final int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    mScannerLiveData.bluetoothEnabled();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                case BluetoothAdapter.STATE_OFF:
                    if (previousState != BluetoothAdapter.STATE_TURNING_OFF && previousState != BluetoothAdapter.STATE_OFF) {
                        stopScan();
                        mScannerLiveData.bluetoothDisabled();
                    }
                    break;
            }
        }
    };

    @Inject
    public ScannerRepository(final Context context, final MeshManagerApi meshManagerApi) {
        this.mContext = context;
        this.mMeshManagerApi = meshManagerApi;
        mScannerLiveData = new ScannerLiveData(Utils.isBleEnabled(), Utils.isLocationEnabled(context));
    }

    public ScannerLiveData getScannerState() {
        return mScannerLiveData;
    }

    /**
     * Register for required broadcast receivers.
     */
    void registerBroadcastReceivers() {
        mContext.registerReceiver(mBluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        if (Utils.isMarshmallowOrAbove()) {
            mContext.registerReceiver(mLocationProviderChangedReceiver, new IntentFilter(LocationManager.MODE_CHANGED_ACTION));
        }
    }

    /**
     * Unregister for required broadcast receivers.
     */
    void unregisterBroadcastReceivers() {
        mContext.unregisterReceiver(mBluetoothStateBroadcastReceiver);
        if (Utils.isMarshmallowOrAbove()) {
            mContext.unregisterReceiver(mLocationProviderChangedReceiver);
        }
    }

    /**
     * Start scanning for Bluetooth devices.
     *
     * @param filterUuid UUID to filter scan results with
     */
    public void startScan(final UUID filterUuid) {
        mFilterUuid = filterUuid;

        if (mScannerLiveData.isScanRequested()) {
            if (mScannerLiveData.isScanning()) {
                return;
            }
        }

        if (mScannerLiveData.isStopScanRequested()) {
            return;
        }

        if (mFilterUuid.equals(BleMeshManager.MESH_PROXY_UUID)) {
            final byte[] networkKey = MeshParserUtils.toByteArray(mMeshManagerApi.getProvisioningSettings().getNetworkKey());
            mNetworkId = mMeshManagerApi.generateNetworkId(networkKey);
        }

        mScannerLiveData.scanningStarted();
        // Scanning settings
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                // Refresh the devices list every second
                .setReportDelay(0)
                // Hardware filtering has some issues on selected devices
                .setUseHardwareFilteringIfSupported(false)
                // Samsung S6 and S6 Edge report equal value of RSSI for all devices. In this app we ignore the RSSI.
                /*.setUseHardwareBatchingIfSupported(false)*/
                .build();

        // Let's use the filter to scan only for unprovisioned mesh nodes.
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid((filterUuid))).build());

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.startScan(filters, settings, mScanCallbacks);
    }

    /**
     * stop scanning for bluetooth devices.
     */
    public void stopScan() {
        mScannerLiveData.stopScanning();
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(mScanCallbacks);
        mScannerLiveData.scanningStopped();
    }

    /**
     * Check if node identity matches
     *
     * @param serviceData service data received from the advertising data
     * @return true if the node identity matches or false otherwise
     */
    private boolean checkIfNodeIdentityMatches(final byte[] serviceData) {
        for (Map.Entry<Integer, ProvisionedMeshNode> node : mMeshManagerApi.getProvisionedNodes().entrySet()) {
            if (mMeshManagerApi != null) {
                if (mMeshManagerApi.nodeIdentityMatches(node.getValue(), serviceData)) {
                    return true;
                }
            }
        }
        return false;

    }
}