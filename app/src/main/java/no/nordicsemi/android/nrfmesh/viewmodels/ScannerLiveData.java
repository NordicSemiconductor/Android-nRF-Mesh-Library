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

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import no.nordicsemi.android.mesh.MeshBeacon;
import no.nordicsemi.android.nrfmesh.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * This class keeps the current list of discovered Bluetooth LE devices matching filter.
 * If a new device has been found it is added to the list and the LiveData in observers are
 * notified. If a packet from a device that's already in the list is found, the RSSI and name
 * are updated and observers are also notified. Observer may check {@link #getUpdatedDeviceIndex()}
 * to find out the index of the updated device.
 */
public class ScannerLiveData extends LiveData<ScannerLiveData> {
    private final List<ExtendedBluetoothDevice> mDevices = new ArrayList<>();
    private Integer mUpdatedDeviceIndex;

    ScannerLiveData() {
    }

    void deviceDiscovered(final ScanResult result) {
        ExtendedBluetoothDevice device;

        final int index = indexOf(result);
        if (index == -1) {
            device = new ExtendedBluetoothDevice(result);
            mDevices.add(device);
            mUpdatedDeviceIndex = null;
        } else {
            device = mDevices.get(index);
            mUpdatedDeviceIndex = index;
        }
        // Update RSSI and name
        device.setRssi(result.getRssi());
        device.setName(getDeviceName(result));

        postValue(this);
    }

    void deviceDiscovered(final ScanResult result, final MeshBeacon beacon) {
        ExtendedBluetoothDevice device;

        final int index = indexOf(result);
        if (index == -1) {
            device = new ExtendedBluetoothDevice(result, beacon);
            mDevices.add(device);
            mUpdatedDeviceIndex = null;
        } else {
            device = mDevices.get(index);
            mUpdatedDeviceIndex = index;
        }
        // Update RSSI and name
        device.setRssi(result.getRssi());
        device.setName(getDeviceName(result));

        postValue(this);
    }

    /**
     * Returns the device name from a scan record
     *
     * @param result ScanResult
     * @return Device name found in the scan record or unknown
     */
    private String getDeviceName(final ScanResult result) {
        if (result.getScanRecord() != null)
            return result.getScanRecord().getDeviceName();
        return "Unknown";
    }

    /**
     * Clears the list of devices found.
     */
    void clear() {
        mDevices.clear();
        mUpdatedDeviceIndex = null;
        postValue(this);
    }

    /**
     * Returns the list of devices.
     *
     * @return current list of devices discovered
     */
    @NonNull
    public List<ExtendedBluetoothDevice> getDevices() {
        return mDevices;
    }

    /**
     * Returns null if a new device was added, or an index of the updated device.
     */
    @Nullable
    public Integer getUpdatedDeviceIndex() {
        final Integer i = mUpdatedDeviceIndex;
        mUpdatedDeviceIndex = null;
        return i;
    }

    /**
     * Returns whether the list is empty.
     */
    public boolean isEmpty() {
        return mDevices.isEmpty();
    }

    /**
     * Finds the index of existing devices on the scan results list.
     *
     * @param result scan result
     * @return index of -1 if not found
     */
    private int indexOf(final ScanResult result) {
        int i = 0;
        for (final ExtendedBluetoothDevice device : mDevices) {
            if (device.matches(result))
                return i;
            i++;
        }
        return -1;
    }
}
