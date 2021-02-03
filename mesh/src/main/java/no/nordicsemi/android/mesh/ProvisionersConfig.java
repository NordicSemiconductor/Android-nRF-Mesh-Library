package no.nordicsemi.android.mesh;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Contains the configuration required when exporting a selected number of Provisioners in a mesh network.
 */
public class ProvisionersConfig extends ExportConfig {

    /**
     * Use this class to configure when exporting all the Provisioners.
     */
    public static class ExportAll implements Builder {
        @Override
        public ProvisionersConfig build() {
            return new ProvisionersConfig(this);
        }
    }

    /**
     * Use this class to configure when exporting some of the Provisioners.
     */
    public static class ExportSome implements Builder {

        private final List<Provisioner> provisioners;

        /**
         * Constructs ExportSome to export only a selected number of Provisioners when exporting a mesh network.
         *
         * @param provisioners List of Provisioners to export.
         * @throws IllegalArgumentException if the list does not contain at least one provisioner.
         */
        public ExportSome(@NonNull final List<Provisioner> provisioners) {
            if (provisioners.isEmpty())
                throw new IllegalArgumentException("Error, at least one Provisioner must be selected!");
            this.provisioners = provisioners;
        }

        protected List<Provisioner> getProvisioners() {
            return provisioners;
        }

        @Override
        public ProvisionersConfig build() {
            return new ProvisionersConfig(this);
        }
    }

    ProvisionersConfig(@NonNull final Builder config) {
        super(config);
    }
}
