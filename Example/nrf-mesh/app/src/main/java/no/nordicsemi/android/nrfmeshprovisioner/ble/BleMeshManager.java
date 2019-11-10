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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.Request;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.InvalidRequestCallback;
import no.nordicsemi.android.ble.callback.MtuCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public class BleMeshManager extends BleManager<BleMeshManagerCallbacks> {

    /**
     * Mesh provisioning service UUID
     */
    public final static UUID MESH_PROVISIONING_UUID = UUID.fromString("00001827-0000-1000-8000-00805F9B34FB");
    /**
     * The maximum packet size is 20 bytes.
     */
    private static final int MAX_PACKET_SIZE = 20;
    public static final int MTU_SIZE_MIN = 23;
    private static final int MTU_SIZE_MAX = 517;
    /**
     * Mesh provisioning data in characteristic UUID
     */
    private final static UUID MESH_PROVISIONING_DATA_IN = UUID.fromString("00002ADB-0000-1000-8000-00805F9B34FB");
    /**
     * Mesh provisioning data out characteristic UUID
     */
    private final static UUID MESH_PROVISIONING_DATA_OUT = UUID.fromString("00002ADC-0000-1000-8000-00805F9B34FB");

    /**
     * Mesh provisioning service UUID
     */
    public final static UUID MESH_PROXY_UUID = UUID.fromString("00001828-0000-1000-8000-00805F9B34FB");

    /**
     * Mesh provisioning data in characteristic UUID
     */
    private final static UUID MESH_PROXY_DATA_IN = UUID.fromString("00002ADD-0000-1000-8000-00805F9B34FB");

    /**
     * Mesh provisioning data out characteristic UUID
     */
    private final static UUID MESH_PROXY_DATA_OUT = UUID.fromString("00002ADE-0000-1000-8000-00805F9B34FB");

    private final String TAG = BleMeshManager.class.getSimpleName();
    private BluetoothGattCharacteristic mMeshProvisioningDataInCharacteristic, mMeshProvisioningDataOutCharacteristic;
    private BluetoothGattCharacteristic mMeshProxyDataInCharacteristic;
    private BluetoothGattCharacteristic mMeshProxyDataOutCharacteristic;

    private int mtuSize = MAX_PACKET_SIZE;

    private boolean isProvisioningComplete;
    private boolean mIsDeviceReady;
    /**
     * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
     */
    private BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {
        @Override
        protected void initialize() {
            requestMtu(MTU_SIZE_MAX)
                    .with(new MtuCallback() {
                        @Override
                        public void onMtuChanged(@NonNull BluetoothDevice device, int mtu) {
                            if (isProvisioningComplete) {
                                // Data Out
                                setNotificationCallback(mMeshProxyDataOutCharacteristic)
                                        .with(new DataReceivedCallback() {
                                            @Override
                                            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                                                mCallbacks.onDataReceived(device, mtu, data.getValue());
                                            }
                                        });
                                enableNotifications(mMeshProxyDataOutCharacteristic).enqueue();
                            } else {
                                // Data Out
                                setNotificationCallback(mMeshProvisioningDataOutCharacteristic)
                                        .with(new DataReceivedCallback() {
                                            @Override
                                            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                                                mCallbacks.onDataReceived(device, mtu, data.getValue());
                                            }
                                        });
                                enableNotifications(mMeshProvisioningDataOutCharacteristic).enqueue();
                            }
                        }
                    })
                    .enqueue();
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            boolean writeRequest;

            BluetoothGattService meshService = gatt.getService(MESH_PROXY_UUID);
            if (meshService != null) {
                isProvisioningComplete = true;
                mMeshProxyDataInCharacteristic = meshService.getCharacteristic(MESH_PROXY_DATA_IN);
                mMeshProxyDataOutCharacteristic = meshService.getCharacteristic(MESH_PROXY_DATA_OUT);

                writeRequest = false;
                if (mMeshProxyDataInCharacteristic != null) {
                    final int rxProperties = mMeshProxyDataInCharacteristic.getProperties();
                    writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0;
                }
                return mMeshProxyDataInCharacteristic != null && mMeshProxyDataOutCharacteristic != null && writeRequest;
            } else {
                meshService = gatt.getService(MESH_PROVISIONING_UUID);
                if (meshService != null) {
                    isProvisioningComplete = false;
                    mMeshProvisioningDataInCharacteristic = meshService.getCharacteristic(MESH_PROVISIONING_DATA_IN);
                    mMeshProvisioningDataOutCharacteristic = meshService.getCharacteristic(MESH_PROVISIONING_DATA_OUT);

                    writeRequest = false;
                    if (mMeshProvisioningDataInCharacteristic != null) {
                        final int rxProperties = mMeshProvisioningDataInCharacteristic.getProperties();
                        writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0;
                    }
                    return mMeshProvisioningDataInCharacteristic != null && mMeshProvisioningDataOutCharacteristic != null && writeRequest;
                }
            }
            return false;
        }

        @Override
        protected void onDeviceDisconnected() {
            mIsDeviceReady = false;
            isProvisioningComplete = false;
            mMeshProvisioningDataInCharacteristic = null;
            mMeshProvisioningDataOutCharacteristic = null;
            mMeshProxyDataInCharacteristic = null;
            mMeshProxyDataOutCharacteristic = null;
        }

        @Override
        protected void onDeviceReady() {
            mIsDeviceReady = true;
            super.onDeviceReady();
        }
    };

    @Inject
    public BleMeshManager(final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    /**
     * Sends the mesh pdu
     * <p>
     * The function will chunk the pdu to fit in to the mtu size supported by the node
     * </p>
     *
     * @param pdu mesh pdu
     */
    public void sendPdu(final byte[] pdu) {
        final int chunks = (pdu.length + (mtuSize - 1)) / mtuSize;
        int srcOffset = 0;
        if (chunks > 1) {
            for (int i = 0; i < chunks; i++) {
                final int length = Math.min(pdu.length - srcOffset, mtuSize);
                final byte[] segmentedBuffer = new byte[length];
                System.arraycopy(pdu, srcOffset, segmentedBuffer, 0, length);
                srcOffset += length;
                send(segmentedBuffer);
            }
        } else {
            send(pdu);
        }
    }

    /**
     * Refreshes the device cache. This is to make sure that Android will discover the services as the the mesh node will change the provisioning service to a proxy service.
     */
    public Request refreshDeviceCache() {
        return super.refreshDeviceCache();
    }

    public boolean isProvisioningComplete() {
        return isProvisioningComplete;
    }

    public final int getMtuSize() {
        return mtuSize;
    }

    private void send(final byte[] data) {
        Log.v(TAG, "Sending data : " + MeshParserUtils.bytesToHex(data, true));
        if (isProvisioningComplete) {
            // Are we connected?
            if (mMeshProxyDataInCharacteristic == null)
                return;
            final BluetoothGattCharacteristic characteristic = mMeshProxyDataInCharacteristic;
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            writeCharacteristic(characteristic, data)
                    .with(new DataSentCallback() {
                        @Override
                        public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
                            mCallbacks.onDataSent(device, mtuSize, data.getValue());
                        }
                    })
                    .enqueue();

        } else {
            // Are we connected?
            if (mMeshProvisioningDataInCharacteristic == null)
                return;
            final BluetoothGattCharacteristic characteristic = mMeshProvisioningDataInCharacteristic;
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            writeCharacteristic(characteristic, data)
                    .with(new DataSentCallback() {
                        @Override
                        public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
                            mCallbacks.onDataSent(device, mtuSize, data.getValue());
                        }
                    })
                    .enqueue();
        }
    }

    public boolean isDeviceReady() {
        return mIsDeviceReady;
    }
}