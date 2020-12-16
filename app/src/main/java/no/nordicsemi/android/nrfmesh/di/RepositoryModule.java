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

package no.nordicsemi.android.nrfmesh.di;

import android.content.Context;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.nrfmesh.ble.BleMeshManager;
import no.nordicsemi.android.nrfmesh.viewmodels.NrfMeshRepository;
import no.nordicsemi.android.nrfmesh.viewmodels.ScannerRepository;

// This module tells a Component which are its subcomponents
// Install this module in Hilt-generated ApplicationComponent
@InstallIn(ApplicationComponent.class)
@Module
public class RepositoryModule {

    @Provides
    @Singleton
    public static ScannerRepository provideScannerRepository(@NonNull final Context context,
                                                      @NonNull final MeshManagerApi meshManagerApi) {
        return new ScannerRepository(context, meshManagerApi);
    }

    @Provides
    @Singleton
    public static NrfMeshRepository provideNrfMeshRepository(@NonNull final MeshManagerApi meshManagerApi,
                                                      @NonNull final BleMeshManager bleMeshManager) {
        return new NrfMeshRepository(meshManagerApi, bleMeshManager);
    }
}
