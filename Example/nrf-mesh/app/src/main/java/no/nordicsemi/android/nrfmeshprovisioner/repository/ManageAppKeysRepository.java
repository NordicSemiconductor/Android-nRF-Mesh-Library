package no.nordicsemi.android.nrfmeshprovisioner.repository;

import android.content.Context;
import android.content.Intent;

public class ManageAppKeysRepository extends BaseMeshRepository {


    ManageAppKeysRepository(final Context context) {
        super(context);
    }

    @Override
    public void onConnectionStateChanged(final String connectionState) {

    }

    @Override
    public void isDeviceConnected(final boolean isConnected) {

    }

    @Override
    public void onDeviceReady(final boolean isReady) {

    }

    @Override
    public void isReconnecting(final boolean isReconnecting) {

    }

    @Override
    public void onProvisioningStateChanged(final Intent intent) {

    }

    @Override
    public void onConfigurationMessageStateChanged(final Intent intent) {

    }
}
