package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.messages.Message;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public abstract class AccessLayer {

    protected Context mContext;
    protected ProvisionedMeshNode mMeshNode;
    protected int sequenceNumber;
    protected Handler mHandler;

    protected abstract void initHandler();

    /**
     * Creates an access message
     * @param message Access message containing the required opcodes and parameters to create access message pdu.
     */
    void createMeshMessage(final Message message) {
        createAccessMessage((AccessMessage) message);
    }

    /**
     * Creates an access message
     * @param accessMessage Access message containing the required opcodes and parameters to create access message pdu.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public final void createAccessMessage(final AccessMessage accessMessage) {
        final int opCode = accessMessage.getOpCode();
        final byte[] opCodes = MeshParserUtils.getOpCodes(opCode);
        final byte[] parameters = accessMessage.getParameters();
        final ByteBuffer accessMessageBuffer = ByteBuffer.allocate(opCodes.length + parameters.length);
        accessMessageBuffer.put(opCodes).put(parameters);
        accessMessage.setAccessPdu(accessMessageBuffer.array());
    }

    /**
     * Creates an access message
     *
     * @param accessMessage Access message containing the required opcodes and parameters to create access message pdu.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public final void createCustomAccessMessage(final AccessMessage accessMessage) {
        final int opCode = accessMessage.getOpCode();
        final int companyIdentifier = accessMessage.getCompanyIdentifier();
        final byte[] parameters = accessMessage.getParameters();
        final byte[] opCodesCompanyIdentifier = MeshParserUtils.getOpCodes(opCode, companyIdentifier);
        final ByteBuffer accessMessageBuffer = ByteBuffer.allocate(opCodesCompanyIdentifier.length + parameters.length);
        accessMessageBuffer.put(opCodesCompanyIdentifier);
        accessMessageBuffer.put(parameters);
        accessMessage.setAccessPdu(accessMessageBuffer.array());
    }

    /**
     * Parse access pdu
     *
     * @param message underlying message containing the access pdu
     */
    protected final void parseAccessLayerPDU(final AccessMessage message) {
        //MSB of the first octet defines the length of opcodes.
        //if MSB = 0 length is 1 and so forth
        final byte[] accessPayload = message.getAccessPdu();
        final int opCodeLength = ((accessPayload[0] >> 7) & 0x01) + 1;

        final int opcode = MeshParserUtils.getOpCode(accessPayload, opCodeLength);
        message.setOpCode(opcode);
        final int length = accessPayload.length - opCodeLength;
        final ByteBuffer paramsBuffer = ByteBuffer.allocate(length).order(ByteOrder.BIG_ENDIAN);
        paramsBuffer.put(accessPayload, opCodeLength, length);
        message.setParameters(paramsBuffer.array());
        Log.v("AccessLayer", "Access PDU " + MeshParserUtils.bytesToHex(accessPayload, false));
    }
}
