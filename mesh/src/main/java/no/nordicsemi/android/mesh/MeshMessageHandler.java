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

package no.nordicsemi.android.mesh;

import android.content.Context;
import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.transport.BaseMeshMessageHandler;
import no.nordicsemi.android.mesh.transport.NetworkLayerCallbacks;
import no.nordicsemi.android.mesh.transport.UpperTransportLayerCallbacks;
import no.nordicsemi.android.mesh.utils.ExtendedInvalidCipherTextException;

/**
 * MeshMessageHandler class for handling mesh
 */
final class MeshMessageHandler extends BaseMeshMessageHandler {

    /**
     * Constructs MeshMessageHandler
     *
     * @param context                      Context
     * @param internalTransportCallbacks   {@link InternalTransportCallbacks} Callbacks
     * @param networkLayerCallbacks        {@link NetworkLayerCallbacks} network layer callbacks
     * @param upperTransportLayerCallbacks {@link UpperTransportLayerCallbacks} upper transport layer callbacks
     */
    MeshMessageHandler(@NonNull final Context context,
                       @NonNull final InternalTransportCallbacks internalTransportCallbacks,
                       @NonNull final NetworkLayerCallbacks networkLayerCallbacks,
                       @NonNull final UpperTransportLayerCallbacks upperTransportLayerCallbacks) {
        super(context, internalTransportCallbacks, networkLayerCallbacks, upperTransportLayerCallbacks);
    }

    @Override
    protected final void setMeshStatusCallbacks(@NonNull final MeshStatusCallbacks statusCallbacks) {
        mStatusCallbacks = statusCallbacks;
    }


    @Override
    protected final void parseMeshPduNotifications(@NonNull final byte[] pdu, @NonNull final MeshNetwork network) throws ExtendedInvalidCipherTextException {
        super.parseMeshPduNotifications(pdu, network);
    }
}
