package no.nordicsemi.android.mesh;

import androidx.annotation.NonNull;


/**
 * Base class for network export configuration
 */
class ExportConfig {

    protected final Builder config;

    /**
     * Export configuration builder
     */
    protected interface Builder {

        /**
         * Builds the export configuration.
         *
         * @return {@link ExportConfig}
         */
        ExportConfig build();
    }

    /**
     * Constructs the builder configuration
     *
     * @param config configuration {@link Builder}
     */
    ExportConfig(@NonNull final Builder config) {
        this.config = config;
    }

    /**
     * Returns the configuration
     */
    protected final Builder getConfig() {
        return config;
    }
}