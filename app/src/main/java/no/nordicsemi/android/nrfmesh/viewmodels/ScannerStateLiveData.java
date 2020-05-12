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

package no.nordicsemi.android.nrfmesh.viewmodels;

import androidx.lifecycle.LiveData;

public class ScannerStateLiveData extends LiveData<ScannerStateLiveData> {
    private boolean mScanningStarted;
    private boolean mBluetoothEnabled;
    private boolean mLocationEnabled;
    private boolean mDeviceFound;

    ScannerStateLiveData(final boolean bluetoothEnabled, final boolean locationEnabled) {
        mScanningStarted = false;
        mDeviceFound = false;
        mBluetoothEnabled = bluetoothEnabled;
        mLocationEnabled = locationEnabled;
        postValue(this);
    }

    public void startScanning() {
        postValue(this);
    }

    void scanningStarted() {
        mScanningStarted = true;
    }

    void scanningStopped() {
        mScanningStarted = false;
        mDeviceFound = false;
        postValue(this);
    }

    void bluetoothEnabled() {
        mBluetoothEnabled = true;
        postValue(this);
    }

    void bluetoothDisabled() {
        mBluetoothEnabled = false;
        postValue(this);
    }

    void deviceFound() {
        if (!mDeviceFound) {
            mDeviceFound = true;
            postValue(this);
        }
    }

    public boolean isEmpty() {
        return !mDeviceFound;
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

    void setLocationEnabled(final boolean enabled) {
        mLocationEnabled = enabled;
        postValue(this);
    }
}
