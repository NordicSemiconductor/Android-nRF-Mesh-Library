package no.nordicsemi.android.meshprovisioner.configuration;

import android.content.Context;

import no.nordicsemi.android.meshprovisioner.InternalMeshMsgHandlerCallbacks;

public abstract class VendorModelMessageState extends MeshMessageState {

    private static final String TAG = VendorModelMessageState.class.getSimpleName();

    public VendorModelMessageState(final Context context, final ProvisionedMeshNode provisionedMeshNode, final InternalMeshMsgHandlerCallbacks callbacks) {
        super(context, provisionedMeshNode, callbacks);
    }

    @Override
    public void executeResend() {
        super.executeResend();
    }
}
