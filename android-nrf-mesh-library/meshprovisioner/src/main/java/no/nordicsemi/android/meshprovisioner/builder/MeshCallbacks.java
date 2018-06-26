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

package no.nordicsemi.android.meshprovisioner.builder;

import android.support.annotation.NonNull;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshConfigurationStatusCallbacks;

/**
 * Created by RoshanRajaratnam on 26/05/2018.
 */
public class MeshCallbacks {

    private final InternalTransportCallbacks transportCallbacks;
    private final MeshConfigurationStatusCallbacks meshConfigurationStatusCallbacks;

    public MeshCallbacks(final MeshCallbacksBuilder builder) {
        this.transportCallbacks = builder.transportCallbacks;
        this.meshConfigurationStatusCallbacks = builder.meshConfigurationStatusCallbacks;
    }

    public static class MeshCallbacksBuilder {

        private final InternalTransportCallbacks transportCallbacks;
        private final MeshConfigurationStatusCallbacks meshConfigurationStatusCallbacks;

        /**
         * Builds a MeshCallbacksBuilder
         *
         * @param transportCallbacks               internal transport callbacks to propogate the mesh pdu to the application
         * @param meshConfigurationStatusCallbacks set configuration status callbacks to receive the configuration status.
         */
        public MeshCallbacksBuilder(@NonNull final InternalTransportCallbacks transportCallbacks,
                                    final MeshConfigurationStatusCallbacks meshConfigurationStatusCallbacks) {
            this.transportCallbacks = transportCallbacks;
            this.meshConfigurationStatusCallbacks = meshConfigurationStatusCallbacks;
        }

        public MeshCallbacksBuilder build() {
            if (transportCallbacks == null)
                throw new IllegalArgumentException("Transport callbacks cannot be null");
            return this;
        }


    }

}
