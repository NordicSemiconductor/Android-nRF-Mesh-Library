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

import android.content.Context;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import dagger.Module;
import dagger.Provides;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.NrfMeshRepository;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ScannerRepository;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ViewModelFactory;

@Module(subcomponents = ViewModelSubComponent.class)
class ViewModelModule {

    @Provides
    static ScannerRepository provideScannerRepository(@NonNull final Context context, @NonNull final MeshManagerApi meshManagerApi) {
        return new ScannerRepository(context, meshManagerApi);
    }

    @Provides
    @Singleton
    static NrfMeshRepository provideNrfMeshRepository(@NonNull final MeshManagerApi meshManagerApi,
                                                      @NonNull final BleMeshManager bleMeshManager) {
        return new NrfMeshRepository(meshManagerApi, bleMeshManager);
    }

    @Provides
    @Singleton
    static ViewModelProvider.Factory provideViewModelFactory(@NonNull final ViewModelSubComponent.Builder viewModelSubComponent) {
        return new ViewModelFactory(viewModelSubComponent.build());
    }
}
