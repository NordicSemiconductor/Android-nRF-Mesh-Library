/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.meshprovisioner;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.support.annotation.NonNull;

import java.nio.ByteBuffer;

import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningCapabilities;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningCapabilitiesState;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningCompleteState;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningConfirmationState;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningDataState;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningFailedState;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningInviteState;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningPublicKeyState;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningRandomConfirmationState;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningStartState;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningState;
import no.nordicsemi.android.meshprovisioner.states.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.ParseOutputOOBActions;
import no.nordicsemi.android.meshprovisioner.utils.ParseProvisioningAlgorithm;

public class MeshProvisioningHandler {

    private static final String TAG = MeshProvisioningHandler.class.getSimpleName();
    private final InternalTransportCallbacks mInternalTransportCallbacks;
    private final Context mContext;
    private MeshProvisioningStatusCallbacks mProvisoningStatusCallbacks;
    private UnprovisionedMeshNode mUnprovisionedMeshNode;

    private int attentionTimer;
    private int numberOfElements;
    private int algorithm;
    private int publicKeyType;
    private int staticOOBType;
    private int outputOOBSize;
    private int outputOOBAction;
    private int inputOOBSize;
    private int inputOOBAction;

    private ProvisioningState provisioningState;
    private boolean isProvisioningPublicKeySent;
    private boolean isProvisioneePublicKeyReceived;
    private InternalMeshManagerCallbacks mInternalMeshManagerCallbacks;

    MeshProvisioningHandler(final Context context, final InternalTransportCallbacks mInternalTransportCallbacks, final InternalMeshManagerCallbacks internalMeshManagerCallbacks) {
        this.mContext = context;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
        this.mInternalMeshManagerCallbacks = internalMeshManagerCallbacks;
    }

    void parseProvisioningNotifications(final UnprovisionedMeshNode unprovisionedMeshNode, final byte[] data) {
        switch (provisioningState.getState()) {
            case PROVISIONING_INVITE:
                break;
            case PROVISIONING_CAPABILITIES:
                if (validateMessage(data)) {
                    if (validateProvisioningCapabilitiesMessage(unprovisionedMeshNode, data)) {
                    }
                } else {
                    parseProvisioningState(unprovisionedMeshNode, data);
                }
                break;
            case PROVISIONING_START:
                break;
            case PROVISIONING_PUBLIC_KEY:
                if (validateMessage(data)) {
                    parseProvisioneePublicKeyXY(unprovisionedMeshNode, data);
                } else {
                    parseProvisioningState(unprovisionedMeshNode, data);
                }
                break;
            case PROVISINING_INPUT_COMPLETE:
                break;
            case PROVISIONING_CONFIRMATION:
                if (validateMessage(data)) {
                    if (parseProvisioneeConfirmation(data)) {
                        sendRandomConfirmationPDU(unprovisionedMeshNode);
                    }
                } else {
                    parseProvisioningState(unprovisionedMeshNode, data);
                }
                break;
            case PROVISINING_RANDOM:
                if (validateMessage(data)) {
                    if (parseProvisioneeRandom(data)) {
                        sendProvisioningData(unprovisionedMeshNode);
                    }
                } else {
                    parseProvisioningState(unprovisionedMeshNode, data);
                }
                break;
            case PROVISINING_DATA:
            case PROVISINING_COMPLETE:
            case PROVISINING_FAILED:
                parseProvisioningState(unprovisionedMeshNode, data);
                break;

        }
    }

    void handleProvisioningWriteCallbacks(final UnprovisionedMeshNode unprovisionedMeshNode) {
        switch (provisioningState.getState()) {
            case PROVISIONING_INVITE:
                provisioningState = new ProvisioningCapabilitiesState(unprovisionedMeshNode, mProvisoningStatusCallbacks);
                break;
            case PROVISIONING_CAPABILITIES:
                break;
            case PROVISIONING_START:
            case PROVISIONING_PUBLIC_KEY:
                //Devices with lower mtu have to send the key in multiple segments
                sendProvisionerPublicKey(unprovisionedMeshNode);
                break;
            case PROVISINING_INPUT_COMPLETE:
                break;
            case PROVISIONING_CONFIRMATION:
                break;
            case PROVISINING_RANDOM:
                break;
            case PROVISINING_DATA:
                break;
        }
    }

    private void parseProvisioningState(final UnprovisionedMeshNode unprovisionedMeshNode, final byte[] data) {
        if (data[1] == ProvisioningState.State.PROVISINING_COMPLETE.getState()) {
            provisioningState = new ProvisioningCompleteState(unprovisionedMeshNode);
            isProvisioningPublicKeySent = false;
            isProvisioneePublicKeyReceived = false;
            //Generate the network id and store it in the mesh node, this is needed to reconnect to the device at a later stage.
            final ProvisionedMeshNode provisionedMeshNode = new ProvisionedMeshNode(unprovisionedMeshNode);
            mInternalMeshManagerCallbacks.onNodeProvisioned(provisionedMeshNode);
            mProvisoningStatusCallbacks.onProvisioningComplete(provisionedMeshNode);
        } else {
            isProvisioningPublicKeySent = false;
            isProvisioneePublicKeyReceived = false;
            final ProvisioningFailedState provisioningFailedState = new ProvisioningFailedState(mContext, unprovisionedMeshNode);
            provisioningState = provisioningFailedState;
            if (provisioningFailedState.parseData(data)) {
                unprovisionedMeshNode.setIsProvisioned(false);
                mProvisoningStatusCallbacks.onProvisioningFailed(unprovisionedMeshNode, provisioningFailedState.getErrorCode());
            }
        }
    }

    /**
     * Initializes a mesh node object to be provisioned
     *
     * @param address         bluetooth address of hte node to be provisioned
     * @param nodeName        a friendly node name
     * @param networkKeyValue 16 byte network key
     * @param keyIndex        12-bit key index
     * @param flags           2 byte flags
     * @param ivIndex         1 byte ivIndex - starts at 1
     * @param unicastAddress  2 byte unicast address
     * @param srcAddress      source address for the configurator
     * @return {@link MeshModel} to be provisioned
     */
    private UnprovisionedMeshNode initializeMeshNode(@NonNull final String address, final String nodeName, @NonNull final String networkKeyValue, final int keyIndex, final int flags, final int ivIndex, final int unicastAddress, final int globalTtl, final byte[] srcAddress) throws IllegalArgumentException {
        UnprovisionedMeshNode unprovisionedMeshNode = null;
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new IllegalArgumentException(mContext.getString(R.string.invalid_bluetooth_address));
        }

        if (validateProvisioningDataInput(networkKeyValue, keyIndex, flags, ivIndex, unicastAddress)) {
            byte[] networkKey = null;
            if (MeshParserUtils.validateNetworkKeyInput(mContext, networkKeyValue)) {
                networkKey = MeshParserUtils.toByteArray(networkKeyValue);
            }

            byte[] keyIndexBytes = null;
            if (MeshParserUtils.validateKeyIndexInput(mContext, keyIndex)) {
                keyIndexBytes = MeshParserUtils.addKeyIndexPadding(keyIndex);
            }

            final byte[] flagBytes = ByteBuffer.allocate(1).put((byte) flags).array();

            byte[] ivIndexBytes = null;
            if (MeshParserUtils.validateIvIndexInput(mContext, ivIndex)) {
                ivIndexBytes = ByteBuffer.allocate(4).putInt(ivIndex).array();
            }

            byte[] unicastBytes = null;
            if (MeshParserUtils.validateUnicastAddressInput(mContext, unicastAddress)) {
                unicastBytes = new byte[]{(byte) ((unicastAddress >> 8) & 0xFF), (byte) (unicastAddress & 0xFF)};
            }

            unprovisionedMeshNode = new UnprovisionedMeshNode();
            unprovisionedMeshNode.setBluetoothDeviceAddress(address);
            unprovisionedMeshNode.setNodeName(nodeName);
            unprovisionedMeshNode.setNetworkKey(networkKey);
            unprovisionedMeshNode.setKeyIndex(keyIndexBytes);
            unprovisionedMeshNode.setFlags(flagBytes);
            unprovisionedMeshNode.setIvIndex(ivIndexBytes);
            unprovisionedMeshNode.setUnicastAddress(unicastBytes);
            unprovisionedMeshNode.setTtl(globalTtl);
            unprovisionedMeshNode.setConfigurationSrc(srcAddress);
        }
        return unprovisionedMeshNode;
    }

    private boolean validateProvisioningDataInput(final String networkKeyValue, final Integer keyIndex, final Integer flags, final Integer ivIndex, final Integer unicastAddress) {
        String error;

        if (networkKeyValue == null || networkKeyValue.isEmpty()) {
            error = "Network key cannot be null or empty!";
            throw new IllegalArgumentException(error);
        }

        if (keyIndex == null) {
            error = "Key index cannot be null!";
            throw new IllegalArgumentException(error);
        }

        if (flags == null) {
            error = "Flags cannot be null!";
            throw new IllegalArgumentException(error);
        }

        if (ivIndex == null) {
            error = "IV Index cannot be null!";
            throw new IllegalArgumentException(error);
        }

        if (unicastAddress == null) {
            error = "Unicast Address cannot be null!";
            throw new IllegalArgumentException(error);
        }

        return true;
    }

    /**
     * Identifies the node that is to be provisioned.
     * <p>
     * This method will send a provisioning invite to the connected peripheral. This will help users to identify a particular node before starting the provisioning process.
     * This method must be invoked before calling {@link #startProvisioning(UnprovisionedMeshNode)}
     * </p
     *
     * @param address         Bluetooth address of the node
     * @param nodeName        Friendly node name
     * @param networkKeyValue Network key
     * @param keyIndex        Index of the network key
     * @param flags           Flag containing the key refresh or the iv update operations
     * @param ivIndex         32-bit value shared across the network
     * @param unicastAddress  Unicast address to be assigned to the node
     * @param globalTtl       Global ttl which is also the number of hops to be used for a message
     * @param configuratorSrc Source address of the configurator
     *
     */
    protected void identify(@NonNull final String address, final String nodeName, @NonNull final String networkKeyValue,
                            final int keyIndex, final int flags, final int ivIndex, final int unicastAddress,
                            final int globalTtl, final byte[] configuratorSrc) throws IllegalArgumentException {
        final UnprovisionedMeshNode unprovisionedMeshNode = initializeMeshNode(address, nodeName, networkKeyValue, keyIndex, flags, ivIndex, unicastAddress, globalTtl, configuratorSrc);
        sendProvisioningInvite(unprovisionedMeshNode);
    }

    /**
     * Starts provisioning an unprovisioned mesh node
     * <p>
     * This method will continue the provisioning process that was started by invoking {@link #identify(String, String, String, int, int, int, int, int, byte[])} .
     * </p>
     *
     * @param unprovisionedMeshNode         Bluetooth address of the node
     */
    protected void startProvisioning(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException {
        this.attentionTimer = 0x05;
        sendProvisioningStart(unprovisionedMeshNode);
    }

    private void sendProvisioningInvite(final UnprovisionedMeshNode unprovisionedMeshNode) {
        isProvisioningPublicKeySent = false;
        isProvisioneePublicKeyReceived = false;
        attentionTimer = 0x05;//0x0A;
        final ProvisioningInviteState invite = new ProvisioningInviteState(unprovisionedMeshNode, attentionTimer, mInternalTransportCallbacks, mProvisoningStatusCallbacks);
        provisioningState = invite;
        invite.executeSend();
    }

    /**
     * Read provisioning capabilities of node
     *
     * @param capabilities provisioning capabilities of the node
     * @return true if the message is valid
     */
    private boolean validateProvisioningCapabilitiesMessage(final UnprovisionedMeshNode unprovisionedMeshNode, final byte[] capabilities) {
        final ProvisioningCapabilitiesState provisioningCapabilitiesState = new ProvisioningCapabilitiesState(unprovisionedMeshNode, mProvisoningStatusCallbacks);
        provisioningState = provisioningCapabilitiesState;
        provisioningCapabilitiesState.parseData(capabilities);
        return true;
    }

    private void sendProvisioningStart(final UnprovisionedMeshNode unprovisionedMeshNode) {
        final ProvisioningCapabilitiesState capabilitiesState = (ProvisioningCapabilitiesState) provisioningState;
        final ProvisioningCapabilities capabilities = capabilitiesState.getCapabilities();
        numberOfElements = capabilities.getNumberOfElements();
        algorithm = capabilities.getSupportedAlgorithm();
        publicKeyType = capabilities.getPublicKeyType();
        staticOOBType = capabilities.getStaticOOBType();
        outputOOBSize = capabilities.getOutputOOBSize();
        outputOOBAction = capabilities.getOutputOOBAction();
        inputOOBSize = capabilities.getInputOOBSize();
        inputOOBAction = capabilities.getInputOOBAction();

        final ProvisioningStartState startProvisioning = new ProvisioningStartState(unprovisionedMeshNode, mInternalTransportCallbacks, mProvisoningStatusCallbacks);
        startProvisioning.setProvisioningCapabilities(numberOfElements, algorithm, publicKeyType, staticOOBType, outputOOBSize, outputOOBAction, inputOOBSize, inputOOBAction);
        provisioningState = startProvisioning;
        startProvisioning.executeSend();
    }

    private void sendProvisionerPublicKey(final UnprovisionedMeshNode unprovisionedMeshNode) {
        if (!isProvisioningPublicKeySent) {
            if (provisioningState instanceof ProvisioningPublicKeyState) {
                isProvisioningPublicKeySent = true;
                provisioningState.executeSend();
            } else {
                final ProvisioningPublicKeyState provisioningPublicKeyState = new ProvisioningPublicKeyState(unprovisionedMeshNode, mInternalTransportCallbacks, mProvisoningStatusCallbacks);
                provisioningState = provisioningPublicKeyState;
                isProvisioningPublicKeySent = true;
                provisioningPublicKeyState.executeSend();
            }
        }
    }

    private void parseProvisioneePublicKeyXY(final UnprovisionedMeshNode unprovisionedMeshNode, final byte[] data) {
        if (provisioningState instanceof ProvisioningPublicKeyState) {
            final ProvisioningPublicKeyState provisioningPublicKeyState = ((ProvisioningPublicKeyState) provisioningState);
            isProvisioneePublicKeyReceived = provisioningPublicKeyState.parseData(data);

            if (isProvisioningPublicKeySent && isProvisioneePublicKeyReceived) {
                provisioningState = new ProvisioningConfirmationState(this, unprovisionedMeshNode, mInternalTransportCallbacks, mProvisoningStatusCallbacks);
                if (outputOOBAction == 0 && inputOOBAction == 0) {
                    setProvisioningConfirmation("");
                } else {
                    mProvisoningStatusCallbacks.onProvisioningAuthenticationInputRequested(unprovisionedMeshNode);
                }
            }
        }
    }

    public void setProvisioningConfirmation(final String pin) {
        if (pin != null /*&& pin.length() > 0*/) {
            final ProvisioningConfirmationState provisioningConfirmationState = (ProvisioningConfirmationState) provisioningState;
            provisioningConfirmationState.setPin(pin);
            provisioningConfirmationState.executeSend();
        }
    }

    private boolean parseProvisioneeConfirmation(final byte[] data) {
        final ProvisioningConfirmationState provisioningConfirmationState = (ProvisioningConfirmationState) provisioningState;
        return provisioningConfirmationState.parseData(data);
    }

    private void sendRandomConfirmationPDU(final UnprovisionedMeshNode unprovisionedMeshNode) {
        final ProvisioningRandomConfirmationState provisioningRandomConfirmation = new ProvisioningRandomConfirmationState(this, unprovisionedMeshNode, mInternalTransportCallbacks, mProvisoningStatusCallbacks);
        provisioningState = provisioningRandomConfirmation;
        provisioningRandomConfirmation.executeSend();
    }

    private boolean parseProvisioneeRandom(final byte[] data) {
        final ProvisioningRandomConfirmationState provisioningRandomConfirmation = (ProvisioningRandomConfirmationState) provisioningState;
        return provisioningRandomConfirmation.parseData(data);
    }

    private void sendProvisioningData(final UnprovisionedMeshNode unprovisionedMeshNode) {
        final ProvisioningDataState provisioningDataState = new ProvisioningDataState(this, unprovisionedMeshNode, mInternalTransportCallbacks, mProvisoningStatusCallbacks);
        provisioningState = provisioningDataState;
        provisioningDataState.executeSend();
    }

    private boolean validateMessage(final byte[] data) {
        final ProvisioningState state = provisioningState;
        return data[1] == state.getState().ordinal();

    }

    public String getCurrentState() {
        String msg = "";
        switch (provisioningState.getState()) {
            case PROVISIONING_INVITE:
                msg = "Sending provisioning invite";
                break;
            case PROVISIONING_CAPABILITIES:
                msg = "Waiting for provisioning capabilities";
                break;
            case PROVISIONING_START:
                msg = "Sending for provisioning start";
                break;
            case PROVISIONING_PUBLIC_KEY:
                if (isProvisioningPublicKeySent) {
                    msg = "Sending provsioner public key xy";
                } else if (isProvisioneePublicKeyReceived) {
                    msg = "Waiting for provsionee public key xy";
                }
                break;
            case PROVISINING_INPUT_COMPLETE:
                break;
            case PROVISIONING_CONFIRMATION:
                msg = "Sending provisioning confirmation";
                break;
            case PROVISINING_RANDOM:
                msg = "Sending provisioning random";
                break;
            case PROVISINING_DATA:
            case PROVISINING_COMPLETE:
            case PROVISINING_FAILED:
                break;

        }

        return msg;
    }

    public final byte[] generateConfirmationInputs(final byte[] provisionerKeyXY, final byte[] provisioneeKeyXY) {
        //invite: 1 bytes, capabilities: 11 bytes, start: 5 bytes, provisionerKey: 64 bytes, deviceKey: 64 bytes
        //Append all the raw data together
        final byte[] invite = new byte[]{(byte) attentionTimer};
        final byte[] capabilities = generateCapabilities();
        final byte[] startData = generateStartData();
        /*final byte[] provisionerKeyXY = mUnprovisionedMeshNode.getProvisionerPublicKeyXY();
        final byte[] provisioneeKeyXY = mUnprovisionedMeshNode.getProvisioneePublicKeyXY();*/

        final int length = invite.length +
                capabilities.length +
                startData.length +
                provisionerKeyXY.length +
                provisioneeKeyXY.length;

        final ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(invite);
        buffer.put(capabilities);
        buffer.put(startData);
        buffer.put(provisionerKeyXY);
        buffer.put(provisioneeKeyXY);

        return buffer.array();
    }

    private byte[] generateCapabilities() {
        final byte[] capabilities = new byte[11];

        capabilities[0] = (byte) numberOfElements;
        capabilities[1] = (byte) ((algorithm >> 8) & 0xFF);
        capabilities[2] = (byte) (algorithm & 0xFF);
        capabilities[3] = (byte) publicKeyType;
        capabilities[4] = (byte) staticOOBType;
        capabilities[5] = (byte) outputOOBSize;
        capabilities[6] = (byte) ((outputOOBAction >> 8) & 0xFF);
        capabilities[7] = (byte) (outputOOBAction & 0xFF);
        capabilities[8] = (byte) inputOOBSize;
        capabilities[9] = (byte) ((inputOOBAction >> 8) & 0xFF);
        capabilities[10] = (byte) (inputOOBAction & 0xFF);

        return capabilities;
    }


    private byte[] generateStartData() {
        final byte[] startData = new byte[5];
        startData[0] = ParseProvisioningAlgorithm.getAlgorithmValue(algorithm);
        startData[1] = 0;//(byte) publicKeyType;
        final short outputOobActionType = (byte) ParseOutputOOBActions.selectOutputActionsFromBitMask(outputOOBAction);
        if (outputOobActionType == ParseOutputOOBActions.NO_OUTPUT) {
            startData[2] = 0;
            //prefer no oob
            startData[3] = 0;
            startData[4] = 0;
        } else {
            startData[2] = 0x02;
            startData[3] = (byte) ParseOutputOOBActions.getOuputOOBActionValue(outputOobActionType);//(byte) ParseOutputOOBActions.getOuputOOBActionValue(outputOOBAction);
            startData[4] = (byte) outputOOBSize;
        }

        return startData;
    }

    public UnprovisionedMeshNode getMeshNode() {
        return mUnprovisionedMeshNode;
    }

    public void setProvisioningCallbacks(MeshProvisioningStatusCallbacks provisioningCallbacks) {
        this.mProvisoningStatusCallbacks = provisioningCallbacks;
    }
}
