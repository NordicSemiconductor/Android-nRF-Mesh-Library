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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import no.nordicsemi.android.nrfmeshprovisioner.livedata.ScannerLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.repository.MeshProvisionerRepository;
import no.nordicsemi.android.nrfmeshprovisioner.repository.ProvisionedNodesScannerRepository;

public class ProvisionedNodesScannerViewModel extends ViewModel {

	private final ProvisionedNodesScannerRepository mScannerRepository;
	private final MeshProvisionerRepository mMeshProvisionerRepository;

	@Inject
	public ProvisionedNodesScannerViewModel(final ProvisionedNodesScannerRepository scannerRepository, final MeshProvisionerRepository meshProvisionerRepository) {
		this.mScannerRepository = scannerRepository;
		this.mMeshProvisionerRepository = meshProvisionerRepository;
		mScannerRepository.registerBroadcastReceivers();
		mMeshProvisionerRepository.registerBroadcastReceiver();
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		mScannerRepository.unregisterBroadcastReceivers();
		mMeshProvisionerRepository.unregisterBroadcastReceiver();
	}

	public ScannerLiveData getScannerState() {
		return mScannerRepository.getScannerState();
	}

	public LiveData<Boolean> isDeviceReady(){
		return mMeshProvisionerRepository.isDeviceReady();
	}

	public void refresh() {
		mScannerRepository.getScannerState().refresh();
	}

	public void startScan(final String networkId){
		mScannerRepository.startScanning(networkId);
	}

	/**
	 * stop scanning for bluetooth devices.
	 */
	public void stopScan() {
		mScannerRepository.stopScanning();
	}

}
