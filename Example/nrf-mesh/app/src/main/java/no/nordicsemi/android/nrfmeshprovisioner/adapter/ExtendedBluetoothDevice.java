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
package no.nordicsemi.android.nrfmeshprovisioner.adapter;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class ExtendedBluetoothDevice implements Parcelable {
    public static final Creator<ExtendedBluetoothDevice> CREATOR = new Creator<ExtendedBluetoothDevice>() {
        @Override
        public ExtendedBluetoothDevice createFromParcel(final Parcel source) {
            return new ExtendedBluetoothDevice(source);
        }

        @Override
        public ExtendedBluetoothDevice[] newArray(final int size) {
            return new ExtendedBluetoothDevice[size];
        }
    };
    private final BluetoothDevice device;
    private String name;
    private int rssi;

    public ExtendedBluetoothDevice(final ScanResult scanResult) {
        this.device = scanResult.getDevice();
        this.name = scanResult.getScanRecord().getDeviceName();
        this.rssi = scanResult.getRssi();
    }

    private ExtendedBluetoothDevice(final Parcel in) {
        this.device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.name = in.readString();
        this.rssi = in.readInt();
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public String getAddress() {
        return device.getAddress();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(final int rssi) {
        this.rssi = rssi;
    }

    // Parcelable implementation

    public boolean matches(final ScanResult scanResult) {
        return device.getAddress().equals(scanResult.getDevice().getAddress());
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof ExtendedBluetoothDevice) {
            final ExtendedBluetoothDevice that = (ExtendedBluetoothDevice) o;
            return device.getAddress().equals(that.device.getAddress());
        }
        return super.equals(o);
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        parcel.writeParcelable(device, flags);
        parcel.writeString(name);
        parcel.writeInt(rssi);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
