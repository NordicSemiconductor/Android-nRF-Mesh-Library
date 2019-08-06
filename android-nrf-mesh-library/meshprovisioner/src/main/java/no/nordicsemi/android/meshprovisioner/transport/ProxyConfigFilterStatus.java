package no.nordicsemi.android.meshprovisioner.transport;

import androidx.annotation.NonNull;
import android.util.Log;

import no.nordicsemi.android.meshprovisioner.opcodes.ProxyConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.ProxyFilterType;

/**
 * To be used as a wrapper class to create the ProxyConfigSetFilterType message.
 */
@SuppressWarnings("WeakerAccess")
public class ProxyConfigFilterStatus extends ProxyConfigStatusMessage {
    private static final String TAG = ProxyConfigFilterStatus.class.getSimpleName();


    private ProxyFilterType mFilterType;
    private int mAddressListSize;

    public ProxyConfigFilterStatus(@NonNull final ControlMessage controlMessage) {
        super(controlMessage);
        this.mParameters = controlMessage.getParameters();
        parseStatusParameters();
    }

    @Override
    int getOpCode() {
        return ProxyConfigMessageOpCodes.FILTER_STATUS;
    }

    @Override
    byte[] getParameters() {
        return mParameters;
    }

    @Override
    void parseStatusParameters() {
        mFilterType = new ProxyFilterType(MeshParserUtils.unsignedByteToInt(mParameters[0]));
        //Note proxy protocol is in big endian
        mAddressListSize = MeshParserUtils.unsignedBytesToInt(mParameters[2], mParameters[1]);
        Log.d(TAG, "Filter type: " + mFilterType.getFilterTypeName());
        Log.d(TAG, "Filter size: " + mAddressListSize);
    }

    /**
     * Returns the {@link ProxyFilterType} set on the proxy
     */
    public ProxyFilterType getFilterType() {
        return mFilterType;
    }

    /**
     * Returns the size of the address list in the proxy filter
     */
    public int getListSize(){
        return mAddressListSize;
    }
}
