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

import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningCapabilities;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningComplete;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningConfirmation;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningData;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningFailed;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningInvite;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningPublicKey;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningRandomConfirmation;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningStart;
import no.nordicsemi.android.meshprovisioner.states.ProvisioningState;
import no.nordicsemi.android.meshprovisioner.states.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.ParseInputOOBActions;
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

    void parseProvisioningNotifications(final byte[] data) {
        switch (provisioningState.getState()) {
            case PROVISIONING_INVITE:
                break;
            case PROVISIONING_CAPABILITIES:
                if (validateMessage(data)) {
                    if (validateProvisioningCapabilitiesMessage(data)) {
                        sendProvisioningStartPDU();
                    }
                } else {
                    parseProvisioningState(data);
                }
                break;
            case PROVISIONING_START:
                break;
            case PROVISIONING_PUBLIC_KEY:
                if (validateMessage(data)) {
                    parseProvisioneePublicKeyXY(data);
                } else {
                    parseProvisioningState(data);
                }
                break;
            case PROVISINING_INPUT_COMPLETE:
                break;
            case PROVISIONING_CONFIRMATION:
                if (validateMessage(data)) {
                    if (parseProvisioneeConfirmation(data)) {
                        sendRandomConfirmationPDU();
                    }
                } else {
                    parseProvisioningState(data);
                }
                break;
            case PROVISINING_RANDOM:
                if (validateMessage(data)) {
                    if (parseProvisioneeRandom(data)) {
                        sendProvisioningData();
                    }
                } else {
                    parseProvisioningState(data);
                }
                break;
            case PROVISINING_DATA:
            case PROVISINING_COMPLETE:
            case PROVISINING_FAILED:
                parseProvisioningState(data);
                break;

        }
    }

    void handleProvisioningWriteCallbacks() {
        switch (provisioningState.getState()) {
            case PROVISIONING_INVITE:
                provisioningState = new ProvisioningCapabilities(mUnprovisionedMeshNode, mProvisoningStatusCallbacks);
                break;
            case PROVISIONING_CAPABILITIES:
                break;
            case PROVISIONING_START:
            case PROVISIONING_PUBLIC_KEY:
                //Devices with lower mtu have to send the key in multiple segments
                sendProvisionerPublicKey();
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

    private void parseProvisioningState(final byte[] data) {
        if (data[1] == ProvisioningState.State.PROVISINING_COMPLETE.getState()) {
            provisioningState = new ProvisioningComplete(mUnprovisionedMeshNode);
            isProvisioningPublicKeySent = false;
            isProvisioneePublicKeyReceived = false;
            //Generate the network id and store it in the mesh node, this is needed to reconnect to the device at a later stage.
            final ProvisionedMeshNode provisionedMeshNode = new ProvisionedMeshNode(mUnprovisionedMeshNode);
            mInternalMeshManagerCallbacks.onNodeProvisioned(provisionedMeshNode);
            mProvisoningStatusCallbacks.onProvisioningComplete(provisionedMeshNode);
        } else {
            isProvisioningPublicKeySent = false;
            isProvisioneePublicKeyReceived = false;
            provisioningState = new ProvisioningFailed(mContext, mUnprovisionedMeshNode);
            if (provisioningState.parseData(data)) {
                mUnprovisionedMeshNode.setIsProvisioned(false);
                mProvisoningStatusCallbacks.onProvisioningFailed(mUnprovisionedMeshNode, provisioningState.getError());
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
     * @param srcAddress
     * @return {@link MeshModel} to be provisioned
     */
    private UnprovisionedMeshNode initializeMeshNode(@NonNull final String address, final String nodeName, @NonNull final String networkKeyValue, final int keyIndex, final int flags, final int ivIndex, final int unicastAddress, final int globalTtl, final byte[] srcAddress) throws IllegalArgumentException {
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

            final UnprovisionedMeshNode unprovisionedMeshNode = new UnprovisionedMeshNode();
            unprovisionedMeshNode.setBluetoothDeviceAddress(address);
            unprovisionedMeshNode.setNodeName(nodeName);
            unprovisionedMeshNode.setNetworkKey(networkKey);
            unprovisionedMeshNode.setKeyIndex(keyIndexBytes);
            unprovisionedMeshNode.setFlags(flagBytes);
            unprovisionedMeshNode.setIvIndex(ivIndexBytes);
            unprovisionedMeshNode.setUnicastAddress(unicastBytes);
            unprovisionedMeshNode.setTtl(globalTtl);
            unprovisionedMeshNode.setConfigurationSrc(srcAddress);
            return unprovisionedMeshNode;
        }
        return null;
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
     * Start provisioning.
     */
    protected void startProvisioning(@NonNull final String address, final String nodeName, @NonNull final String networkKeyValue, final int keyIndex, final int flags, final int ivIndex, final int unicastAddress, final int globalTtl, final byte[] configuratorSrc) throws IllegalArgumentException {
        mUnprovisionedMeshNode = initializeMeshNode(address ,nodeName, networkKeyValue, keyIndex, flags, ivIndex, unicastAddress, globalTtl, configuratorSrc);
        this.attentionTimer = 0x0A;
        sendProvisioningInvite();
    }

    private void sendProvisioningInvite() {
        isProvisioningPublicKeySent = false;
        isProvisioneePublicKeyReceived = false;
        attentionTimer = 0x0A;
        final ProvisioningInvite invite = new ProvisioningInvite(mUnprovisionedMeshNode, attentionTimer, mInternalTransportCallbacks, mProvisoningStatusCallbacks);
        provisioningState = invite;
        invite.executeSend();
    }

    /**
     * Read provisioning capabilities of node
     *
     * @param capabilities provisioning capabilities of the node
     * @return true if the message is valid
     */
    private boolean validateProvisioningCapabilitiesMessage(final byte[] capabilities) {
        final ProvisioningCapabilities provisioningCapabilities = (ProvisioningCapabilities) provisioningState;
        provisioningCapabilities.parseData(capabilities);
        return true;
    }

    private void sendProvisioningStartPDU() {
        final ProvisioningCapabilities capabilities = (ProvisioningCapabilities) provisioningState;
        numberOfElements = capabilities.getNumberOfElements();
        algorithm = capabilities.getAlgorithm();
        publicKeyType = capabilities.getPublicKeyType();
        staticOOBType = capabilities.getStaticOOBType();
        outputOOBSize = capabilities.getOutputOOBSize();
        outputOOBAction = capabilities.getOutputOOBAction();
        inputOOBSize = capabilities.getInputOOBSize();
        inputOOBAction = capabilities.getInputOOBAction();

        final ProvisioningStart startProvisioning = new ProvisioningStart(mUnprovisionedMeshNode, mInternalTransportCallbacks, mProvisoningStatusCallbacks);
        startProvisioning.setProvisioningCapabilities(numberOfElements, algorithm, publicKeyType, staticOOBType, outputOOBSize, outputOOBAction, inputOOBSize, inputOOBAction);
        provisioningState = startProvisioning;
        startProvisioning.executeSend();
    }

    private void sendProvisionerPublicKey() {
        if (!isProvisioningPublicKeySent) {
            if (provisioningState instanceof ProvisioningPublicKey) {
                isProvisioningPublicKeySent = true;
                provisioningState.executeSend();
            } else {
                final ProvisioningPublicKey provisioningPublicKey = new ProvisioningPublicKey(mUnprovisionedMeshNode, mInternalTransportCallbacks, mProvisoningStatusCallbacks);
                provisioningState = provisioningPublicKey;
                isProvisioningPublicKeySent = true;
                provisioningPublicKey.executeSend();
            }
        }
    }

    private void parseProvisioneePublicKeyXY(final byte[] data) {
        if (provisioningState instanceof ProvisioningPublicKey) {
            final ProvisioningPublicKey provisioningPublicKey = ((ProvisioningPublicKey) provisioningState);
            isProvisioneePublicKeyReceived = provisioningPublicKey.parseData(data);

            if (isProvisioningPublicKeySent && isProvisioneePublicKeyReceived) {
                provisioningState = new ProvisioningConfirmation(this, mUnprovisionedMeshNode, mInternalTransportCallbacks, mProvisoningStatusCallbacks);
                if (outputOOBAction == 0 && inputOOBAction == 0) {
                    setProvisioningConfirmation("");
                } else {
                    mProvisoningStatusCallbacks.onProvisioningAuthenticationInputRequested(mUnprovisionedMeshNode);
                }
            }
        }
    }

    public void setProvisioningConfirmation(final String pin) {
        if (pin != null /*&& pin.length() > 0*/) {
            final ProvisioningConfirmation provisioningConfirmation = (ProvisioningConfirmation) provisioningState;
            provisioningConfirmation.setPin(pin);
            provisioningConfirmation.executeSend();
        }
    }

    private boolean parseProvisioneeConfirmation(final byte[] data) {
        //log(LogContract.Log.Level.APPLICATION, mContext.getString(R.string.provisionee_public_key_xy));
        final ProvisioningConfirmation provisioningConfirmation = (ProvisioningConfirmation) provisioningState;
        return provisioningConfirmation.parseData(data);
    }

    private void sendRandomConfirmationPDU() {
        //log(LogContract.Log.Level.APPLICATION, mContext.getString(R.string.sending_provision_confirmation));
        final ProvisioningRandomConfirmation provisioningRandomConfirmation = new ProvisioningRandomConfirmation(this, mUnprovisionedMeshNode, mInternalTransportCallbacks, mProvisoningStatusCallbacks);
        provisioningState = provisioningRandomConfirmation;
        provisioningRandomConfirmation.executeSend();
    }

    private boolean parseProvisioneeRandom(final byte[] data) {
        //log(LogContract.Log.Level.APPLICATION, mContext.getString(R.string.provisionee_public_key_xy));
        final ProvisioningRandomConfirmation provisioningRandomConfirmation = (ProvisioningRandomConfirmation) provisioningState;
        return provisioningRandomConfirmation.parseData(data);
    }

    private void sendProvisioningData() {
        final ProvisioningData provisioningData = new ProvisioningData(this, mUnprovisionedMeshNode, mInternalTransportCallbacks, mProvisoningStatusCallbacks);
        provisioningState = provisioningData;
        provisioningData.executeSend();
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

    public final byte[] generateConfirmationInputs() {
        //invite: 1 bytes, capabilities: 11 bytes, start: 5 bytes, provisionerKey: 64 bytes, deviceKey: 64 bytes
        //Append all the raw data together
        final byte[] invite = new byte[]{(byte) attentionTimer};
        final byte[] capabilities = generateCapabilities();
        final byte[] startData = generateStartData();
        final byte[] provisionerKeyXY = mUnprovisionedMeshNode.getProvisionerPublicKeyXY();
        final byte[] provisioneeKeyXY = mUnprovisionedMeshNode.getProvisioneePublicKeyXY();

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
        startData[2] = getAuthenticationMethod();
        startData[3] = (byte) ParseOutputOOBActions.getOuputOOBActionValue(outputOOBAction);
        startData[4] = (byte) outputOOBSize;

        return startData;
    }

    private byte getAuthenticationMethod() {
        if (ParseOutputOOBActions.parseOuputOOBActionValue(outputOOBAction) == 0 && ParseInputOOBActions.parseInputOOBActionValue(inputOOBAction) > 0) {
            return 3;
        } else if (ParseOutputOOBActions.parseOuputOOBActionValue(outputOOBAction) > 0 && ParseInputOOBActions.parseInputOOBActionValue(inputOOBAction) == 0) {
            return 2;
        } else {
            return 0;
        }
    }

    public UnprovisionedMeshNode getMeshNode() {
        return mUnprovisionedMeshNode;
    }

    public void setMeshNode(final UnprovisionedMeshNode unprovisionedMeshNode) {
        this.mUnprovisionedMeshNode = unprovisionedMeshNode;
    }

    public void setProvisioningCallbacks(MeshProvisioningStatusCallbacks provisioningCallbacks) {
        this.mProvisoningStatusCallbacks = provisioningCallbacks;
    }
}
