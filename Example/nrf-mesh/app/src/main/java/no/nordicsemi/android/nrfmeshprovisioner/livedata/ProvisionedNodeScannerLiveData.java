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

package no.nordicsemi.android.nrfmeshprovisioner.livedata;

import android.arch.lifecycle.LiveData;

import javax.annotation.Nullable;

import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * This class keeps the discovered provisioned Bluetooth LE mesh node.
 * When the device has been found it is added to stored list and the LiveData observers are
 * notified. If a packet from a device that's already in the list is found, the RSSI and name
 * are updated and observers are also notified. Observer may check {@link #getDevice()}
 * to find out the device that was found during scanning
 */
public class ProvisionedNodeScannerLiveData extends LiveData<ProvisionedNodeScannerLiveData> {
    private ExtendedBluetoothDevice mDevice;
    private boolean mScanningStarted;
    private boolean mBluetoothEnabled;
    private boolean mLocationEnabled;
    private boolean mStartScanning;
    private boolean mStopScanning;

    public ProvisionedNodeScannerLiveData(final boolean bluetoothEnabled, final boolean locationEnabled) {
        mScanningStarted = false;
        mBluetoothEnabled = bluetoothEnabled;
        mLocationEnabled = locationEnabled;
        postValue(this);
    }

    public void refresh() {
        postValue(this);
    }
    /**
     * Updates the flag to notify scanner live data that a stop scan was requested.
     */
    public void startScanning() {
        mStopScanning = false;
        mStartScanning = true;
        postValue(this);
    }

    public boolean isScanRequested(){
        return mStartScanning;
    }

    /**
     * Updates the flag to notify scanner live data that a stop scan was requested.
     */
    public void stopScanning() {
        mScanningStarted = false;
        mStartScanning = false;
        mStopScanning = true;
        postValue(this);
    }

    public boolean isStopScanRequested(){
        return mStopScanning;
    }

    public void scanningStarted() {
        mScanningStarted = true;
        postValue(this);
    }

    public void scanningStopped() {
        mScanningStarted = false;
        postValue(this);
    }

    public void bluetoothEnabled() {
        mBluetoothEnabled = true;
        postValue(this);
    }

    public void bluetoothDisabled() {
        mBluetoothEnabled = false;
        mDevice = null;
        postValue(this);
    }

    public void deviceDiscovered(final ScanResult result) {
        ExtendedBluetoothDevice device;

        if (mDevice == null) {
            device = new ExtendedBluetoothDevice(result);
            mDevice = device;
        } else {
            device = mDevice;
        }
        // Update RSSI and name
        device.setRssi(result.getRssi());
        device.setName(result.getScanRecord().getDeviceName());

        postValue(this);
    }

    /**
     * Returns the bluetooth device
     *
     * @return current list of devices discovered
     */
    @Nullable
    public ExtendedBluetoothDevice getDevice() {
        return mDevice;
    }

    /**
     * Returns whether scanning is in progress.
     */
    public boolean isScanning() {
        return mScanningStarted;
    }

    /**
     * Returns whether Bluetooth adapter is enabled.
     */
    public boolean isBluetoothEnabled() {
        return mBluetoothEnabled;
    }

    /**
     * Returns whether Location is enabled.
     */
    public boolean isLocationEnabled() {
        return mLocationEnabled;
    }

    public void setLocationEnabled(final boolean enabled) {
        mLocationEnabled = enabled;
        postValue(this);
    }
}
