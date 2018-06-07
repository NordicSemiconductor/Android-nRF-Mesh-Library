package no.nordicsemi.android.meshprovisioner.states;

import android.content.Context;

import no.nordicsemi.android.meshprovisioner.R;

public class ProvisioningFailed extends ProvisioningState {

    private final Context mContext;
    private final UnprovisionedMeshNode mUnprovisionedMeshNode;
    public ProvisioningFailed(final Context context, final UnprovisionedMeshNode unprovisionedMeshNode) {
        super();
        this.mContext = context;
        this.mUnprovisionedMeshNode = unprovisionedMeshNode;
    }

    @Override
    public State getState() {
        return State.PROVISINING_FAILED;
    }

    @Override
    public void executeSend() {

    }

    @Override
    public boolean parseData(final byte[] data) {
        error = parseProvisioningFailure(data);
        return true;
    }

    public String parseProvisioningFailure(final byte[] pdu) {
        final int errorCode = pdu[2];
        switch (ProvisioningFailureCode.fromErrorCode(errorCode)) {
            case PROHIBITED:
                return mContext.getString(R.string.error_prohibited);
            case INVALID_PDU:
                return mContext.getString(R.string.error_invalid_pdu);
            case INVALID_FORMAT:
                return mContext.getString(R.string.error_invalid_format);
            case UNEXPECTED_PDU:
                return mContext.getString(R.string.error_prohibited);
            case CONFIRMATION_FAILED:
                return mContext.getString(R.string.error_confirmation_failed);
            case OUT_OF_RESOURCES:
                return mContext.getString(R.string.error_prohibited);
            case DECRYPTION_FAILED:
                return mContext.getString(R.string.error_decryption_failed);
            case UNEXPECTED_ERROR:
                return mContext.getString(R.string.error_unexpected_error);
            case CANNOT_ASSIGN_ADDRESSES:
                return mContext.getString(R.string.error_cannot_assign_addresses);
            case UNKNOWN_ERROR_CODE:
            default:
                return mContext.getString(R.string.error_rfu);
        }
    }

    public enum ProvisioningFailureCode {
        PROHIBITED(0x00),
        INVALID_PDU(0x01),
        INVALID_FORMAT(0x02),
        UNEXPECTED_PDU(0x03),
        CONFIRMATION_FAILED(0x04),
        OUT_OF_RESOURCES(0x05),
        DECRYPTION_FAILED(0x06),
        UNEXPECTED_ERROR(0x07),
        CANNOT_ASSIGN_ADDRESSES(0x08),
        UNKNOWN_ERROR_CODE(0x09);

        private final int errorCode;

        ProvisioningFailureCode(final int errorCode) {
            this.errorCode = errorCode;
        }

        public static ProvisioningFailureCode fromErrorCode(final int errorCode) {
            for (ProvisioningFailureCode failureCode : ProvisioningFailureCode.values()) {
                if (failureCode.getErrorCode() == errorCode) {
                    return failureCode;
                }
            }
            return UNKNOWN_ERROR_CODE;
            //throw new RuntimeException("Enum not found");
        }

        public final int getErrorCode() {
            return errorCode;
        }
    }

}
