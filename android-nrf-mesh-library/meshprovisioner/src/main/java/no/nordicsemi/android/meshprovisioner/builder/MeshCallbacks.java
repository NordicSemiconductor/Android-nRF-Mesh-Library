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
