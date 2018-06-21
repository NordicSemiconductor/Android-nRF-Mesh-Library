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

package no.nordicsemi.android.nrfmeshprovisioner.di;

import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import no.nordicsemi.android.nrfmeshprovisioner.repository.MeshProvisionerRepository;
import no.nordicsemi.android.nrfmeshprovisioner.repository.MeshRepository;
import no.nordicsemi.android.nrfmeshprovisioner.repository.ModelConfigurationRepository;
import no.nordicsemi.android.nrfmeshprovisioner.repository.NodeConfigurationRepository;
import no.nordicsemi.android.nrfmeshprovisioner.repository.ProvisionedNodesScannerRepository;
import no.nordicsemi.android.nrfmeshprovisioner.repository.ReconnectRepository;
import no.nordicsemi.android.nrfmeshprovisioner.repository.ScannerRepository;
import no.nordicsemi.android.nrfmeshprovisioner.service.MeshService;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ViewModelFactory;

@Module(subcomponents = ViewModelSubComponent.class)
public class ViewModelModule {

	@Provides
	static ScannerRepository provideScannerRepository(final Context context) {
		return new ScannerRepository(context);
	}

	@Provides
	static ProvisionedNodesScannerRepository provideProvisionedNodesScannerRepository(final Context context) {
		return new ProvisionedNodesScannerRepository(context);
	}

	@Provides
	static MeshRepository provideMeshRepository(final Context context) {
		return new MeshRepository(context);
	}

	@Provides
	static MeshProvisionerRepository provideMeshProvisionerRepository(final Context context) {
		return new MeshProvisionerRepository(context);
	}

	@Provides
	static NodeConfigurationRepository provideElementConfigurationRepository(final Context context) {
		return new NodeConfigurationRepository(context);
	}


	@Provides
	static ModelConfigurationRepository provideMeshConfigurationRepository(final Context context) {
		return new ModelConfigurationRepository(context);
	}

	@Provides
	static ReconnectRepository provideReconnectRepository(final Context context) {
		return new ReconnectRepository(context);
	}

	@Provides
	static MeshService providerMeshService() {
		return new MeshService();
	}

	@Provides
	@Singleton
	static ViewModelProvider.Factory provideViewModelFactory(final ViewModelSubComponent.Builder viewModelSubComponent) {
		return new ViewModelFactory(viewModelSubComponent.build());
	}
}
