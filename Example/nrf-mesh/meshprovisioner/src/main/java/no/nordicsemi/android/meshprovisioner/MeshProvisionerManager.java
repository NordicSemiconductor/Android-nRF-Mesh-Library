/*
 * Copyright (c) 2015, Nordic Semiconductor
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

package no.nordicsemi.android.meshprovisioner;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.log.LogContract;

public class MeshProvisionerManager extends BleManager<MeshProvisionerManagerCallbacks> {
	/**
	 * Nordic Blinky Service UUID
	 */
	public final static UUID MESH_PROVISIONING_UUID = UUID.fromString("00001827-0000-1000-8000-00805F9B34FB");
	/**
	 * BUTTON characteristic UUID
	 */
	private final static UUID MESH_PROVISIONING_DATA_IN = UUID.fromString("00002ADB-0000-1000-8000-00805F9B34FB");
	/**
	 * LED characteristic UUID
	 */
	private final static UUID MESH_PROVISIONING_DATA_OUT = UUID.fromString("00002ADC-0000-1000-8000-00805F9B34FB");

	private BluetoothGattCharacteristic mMeshProvisioningDataInCharacteristic, mMeshProvisioningDataOutCharacteristic;

	public MeshProvisionerManager(final Context context) {
		super(context);
	}

	@Override
	protected BleManagerGattCallback getGattCallback() {
		return mGattCallback;
	}

	@Override
	protected boolean shouldAutoConnect() {
		// If you want to connect to the device using autoConnect flag = true, return true here.
		// Read the documentation of this method.
		return super.shouldAutoConnect();
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
	 */
	private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

		@Override
		protected Deque<Request> initGatt(final BluetoothGatt gatt) {
			final LinkedList<Request> requests = new LinkedList<>();
			requests.push(Request.newReadRequest(mMeshProvisioningDataOutCharacteristic));
			requests.push(Request.newReadRequest(mMeshProvisioningDataInCharacteristic));
			requests.push(Request.newEnableNotificationsRequest(mMeshProvisioningDataInCharacteristic));
			return requests;
		}

		@Override
		public boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(MESH_PROVISIONING_UUID);
			if (service != null) {
				mMeshProvisioningDataInCharacteristic = service.getCharacteristic(MESH_PROVISIONING_DATA_IN);
				mMeshProvisioningDataOutCharacteristic = service.getCharacteristic(MESH_PROVISIONING_DATA_OUT);
			}

			boolean writeRequest = false;
			if (mMeshProvisioningDataInCharacteristic != null) {
				final int rxProperties = mMeshProvisioningDataInCharacteristic.getProperties();
				writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0;
			}

			return mMeshProvisioningDataInCharacteristic != null && mMeshProvisioningDataInCharacteristic != null && writeRequest;
		}

		@Override
		protected void onDeviceDisconnected() {
			mMeshProvisioningDataInCharacteristic = null;
			mMeshProvisioningDataOutCharacteristic = null;
		}

		@Override
		protected void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			/*final int data = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
			if (characteristic == mMeshProvisioningDataOutCharacteristic) {
				final boolean ledOn = data == 0x01;
				log(LogContract.Log.Level.APPLICATION, "LED " + (ledOn ? "ON" : "OFF"));
				//mCallbacks.onDataSent(ledOn);
			} else {
				final boolean buttonPressed = data == 0x01;
				log(LogContract.Log.Level.APPLICATION, "Button " + (buttonPressed ? "pressed" : "released"));
				//mCallbacks.onDataReceived(buttonPressed);
			}*/
		}

		@Override
		public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			// This method is only called for LED characteristic
			final int data = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
			final boolean ledOn = data == 0x01;
			log(LogContract.Log.Level.APPLICATION, "LED " + (ledOn ? "ON" : "OFF"));
			mCallbacks.onDataSent(ledOn);
		}

		@Override
		public void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			// This method is only called for Button characteristic
			final int data = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
			final boolean buttonPressed = data == 0x01;
			log(LogContract.Log.Level.APPLICATION, "Button " + (buttonPressed ? "pressed" : "released"));
			mCallbacks.onDataReceived(buttonPressed);
		}
	};

	public void send(final boolean onOff) {
		// Are we connected?
		if (mMeshProvisioningDataOutCharacteristic == null)
			return;

		final byte[] command = new byte[] {(byte) (onOff ? 1 : 0)};
		mMeshProvisioningDataOutCharacteristic.setValue(command);
		log(LogContract.Log.Level.WARNING, "Turning LED " + (onOff ? "ON" : "OFF") + "...");
		writeCharacteristic(mMeshProvisioningDataOutCharacteristic);
	}
}
