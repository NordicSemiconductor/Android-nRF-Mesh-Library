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

package no.nordicsemi.android.nrfmeshprovisioner.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import javax.inject.Inject;

import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.service.MeshService;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_CONNECTION_STATE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_CONNECT_TO_MESH_NETWORK;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_IS_CONNECTED;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.ACTION_ON_DEVICE_READY;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DATA;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DEVICE;

public class ReconnectRepository {

    private final Context mContext;

    /** Connection states Connecting, Connected, Disconnecting, Disconnected etc. **/
    private final MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>();

    /** Flag to determine if the device is ready **/
    private final MutableLiveData<Boolean> mOnDeviceReady = new MutableLiveData<>();

    /** Updates the connection state while connecting to a peripheral **/
    private final MutableLiveData<String> mConnectionState = new MutableLiveData<>();

    /** Mesh service binder, that gives access to the communication channel to the service**/
    private MeshService.MeshServiceBinder mBinder;

    /**
     * Service connection
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mBinder = (MeshService.MeshServiceBinder) service;
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mBinder = null;
        }
    };

    /**
     * Broadcast receiver listening to mesh events
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_IS_CONNECTED:
                    final boolean isConnected = intent.getExtras().getBoolean(EXTRA_DATA);
                    mIsConnected.postValue(isConnected);
                    break;
                case ACTION_CONNECTION_STATE:
                    mConnectionState.postValue(intent.getExtras().getString(EXTRA_DATA));
                    break;
                case ACTION_ON_DEVICE_READY:
                    mOnDeviceReady.postValue(intent.getExtras().getBoolean(EXTRA_DATA));
                    break;
            }
        }
    };

    @Inject
    public ReconnectRepository(final Context context) {
        final Intent intent = new Intent(context, MeshService.class);
        context.startService(intent);
        context.bindService(intent, mServiceConnection, 0);
        this.mContext = context;
    }

    public LiveData<Boolean> isDeviceReady() {
        return mOnDeviceReady;
    }

    public LiveData<String> getConnectionState() {
        return mConnectionState;
    }

    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }


    /**
     * Registers a broadcast receiver to receive events from the {@link MeshService}
     */
    public void registerBroadcastReceiver(){
        final IntentFilter intentFilter = Utils.createIntentFilters();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    public void unregisterBroadcastReceiver(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * Connect to peripheral
     * @param device bluetooth device
     */
    public void connect(final ExtendedBluetoothDevice device) {
        final Intent intent = new Intent(mContext, MeshService.class);
        intent.setAction(ACTION_CONNECT_TO_MESH_NETWORK);
        intent.putExtra(EXTRA_DEVICE, device);
        mContext.startService(intent);
    }

    public void disconnect() {
        if(mBinder != null) {
            mBinder.disconnect();
        }
    }
}
