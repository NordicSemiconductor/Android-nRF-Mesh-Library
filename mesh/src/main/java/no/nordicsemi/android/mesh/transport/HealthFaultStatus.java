package no.nordicsemi.android.mesh.transport;

import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.logger.MeshLogger;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;

public class HealthFaultStatus extends ApplicationStatusMessage {

    private static final String TAG = HealthFaultStatus.class.getSimpleName();
    private static final int HEALTH_CURRENT_STATUS_MANDATORY_LENGTH = 3;
    private static final int OP_CODE = ApplicationMessageOpCodes.HEALTH_FAULT_STATUS;

    private int mTestId;
    private int mCompanyId;
    private byte[] mFaultArray;


    /**
     * Constructs HealthFaultStatus message
     * @param message access message
     */
    public HealthFaultStatus(@NonNull AccessMessage message) {
        super(message);
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        MeshLogger.verbose(TAG, "Received health fault status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        mTestId = mParameters[0] & 0xFF;
        mCompanyId = mParameters[1] & 0xFF  | ((mParameters[2] & 0xFF) << 8);
        MeshLogger.verbose(TAG, "Test ID: " + mTestId);
        MeshLogger.verbose(TAG, "Company ID: " + mCompanyId);
        if (mParameters.length > HEALTH_CURRENT_STATUS_MANDATORY_LENGTH) {
            mFaultArray = new byte[mParameters.length - HEALTH_CURRENT_STATUS_MANDATORY_LENGTH];
            System.arraycopy(mParameters, HEALTH_CURRENT_STATUS_MANDATORY_LENGTH, mFaultArray, 0, mParameters.length - 3);
        }
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the test id of the health status
     *
     * @return test id
     */
    public int getTestId() {
        return mTestId;
    }

    /**
     * Returns the company id of the health status
     *
     * @return company id
     */
    public int getCompanyId() {
        return mCompanyId;
    }

    /**
     * Returns the fault array of the health status
     *
     * @return fault array
     */
    public byte[] getFaultArray() {
        return mFaultArray;
    }
}
