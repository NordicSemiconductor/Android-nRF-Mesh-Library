package no.nordicsemi.android.mesh;

import java.util.List;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

/**
 * Contains the configuration required when exporting a selected number of mesh nodes in a mesh network.
 */
public class NodesConfig extends ExportConfig {

    /**
     * Use this class to configure when exporting some of the Nodes with their device keys.
     */
    public static class ExportWithDeviceKey implements Builder {
        @Override
        public NodesConfig build() {
            return new NodesConfig(this);
        }
    }

    /**
     * Use this class to configure when exporting some of the Nodes without their device keys.
     */
    public static class ExportWithoutDeviceKey implements Builder {
        @Override
        public NodesConfig build() {
            return new NodesConfig(this);
        }
    }

    /**
     * Use this class to configure when exporting some of the Nodes.
     */
    public static class ExportSome implements Builder {

        private final List<ProvisionedMeshNode> withDeviceKey;
        private final List<ProvisionedMeshNode> withoutDeviceKey;

        /**
         * Constructs ExportSome to export only a selected number of Nodes when exporting a mesh network.
         *
         * @param withDeviceKey    List of nodes to be exported with their device keys.
         * @param withoutDeviceKey List of nodes to be exported without their device keys.
         */
        public ExportSome(@NonNull final List<ProvisionedMeshNode> withDeviceKey, @NonNull final List<ProvisionedMeshNode> withoutDeviceKey) {
            this.withDeviceKey = withDeviceKey;
            this.withoutDeviceKey = withoutDeviceKey;
        }

        protected List<ProvisionedMeshNode> getWithDeviceKey() {
            return withDeviceKey;
        }

        protected List<ProvisionedMeshNode> getWithoutDeviceKey() {
            return withoutDeviceKey;
        }

        @Override
        public NodesConfig build() {
            return new NodesConfig(this);
        }
    }

    NodesConfig(@NonNull final Builder config) {
        super(config);
    }
}
