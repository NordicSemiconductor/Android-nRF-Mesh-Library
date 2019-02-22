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

import android.content.Context;
import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.UUID;

import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningCapabilities;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningCapabilitiesState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningCompleteState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningConfirmationState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningDataState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningFailedState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningInputCompleteState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningInviteState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningPublicKeyState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningRandomConfirmationState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningStartState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.AuthenticationOOBMethods;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.ParseInputOOBActions;
import no.nordicsemi.android.meshprovisioner.utils.ParseOutputOOBActions;
import no.nordicsemi.android.meshprovisioner.utils.ParseProvisioningAlgorithm;

class MeshProvisioningHandler implements InternalProvisioningCallbacks {

    private static final String TAG = MeshProvisioningHandler.class.getSimpleName();
    private final InternalTransportCallbacks mInternalTransportCallbacks;
    private final Context mContext;
    private MeshProvisioningStatusCallbacks mStatusCallbacks;
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
    private AuthenticationOOBMethods usedAuthenticationOOBType;
    private InternalMeshManagerCallbacks mInternalMeshManagerCallbacks;
    private String randomOOBInput;
    private byte[] confirmationInputs;
    private byte[] staticOOBValue;

    /**
     * Constructs the mesh provisioning handler
     * <p>
     * This will handle the provisioning process through each step
     * </p>
     *
     * @param context                      context
     * @param mInternalTransportCallbacks  {@link InternalTransportCallbacks} callbacks
     * @param internalMeshManagerCallbacks {@link InternalMeshManagerCallbacks} callbacks
     */
    MeshProvisioningHandler(final Context context, final InternalTransportCallbacks mInternalTransportCallbacks, final InternalMeshManagerCallbacks internalMeshManagerCallbacks) {
        this.mContext = context;
        this.mInternalTransportCallbacks = mInternalTransportCallbacks;
        this.mInternalMeshManagerCallbacks = internalMeshManagerCallbacks;
    }

    /**
     * Returns the unprovisioned mesh node
     */
    public UnprovisionedMeshNode getMeshNode() {
        return mUnprovisionedMeshNode;
    }

    /**
     * Sets the provisioning callbacks
     *
     * @param provisioningCallbacks {@link MeshProvisioningStatusCallbacks} callbacks
     */
    void setProvisioningCallbacks(MeshProvisioningStatusCallbacks provisioningCallbacks) {
        this.mStatusCallbacks = provisioningCallbacks;
    }

    void parseProvisioningNotifications(final byte[] data) {
        final UnprovisionedMeshNode unprovisionedMeshNode = mUnprovisionedMeshNode;
        switch (provisioningState.getState()) {
            case PROVISIONING_INVITE:
                break;
            case PROVISIONING_CAPABILITIES:
                if (validateMessage(data)) {
                    if (validateProvisioningCapabilitiesMessage(unprovisionedMeshNode, data)) {
                        //TODO
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
            case PROVISIONING_INPUT_COMPLETE:
                if (validateMessage(data)) {
                    provisioningState = new ProvisioningConfirmationState(this, unprovisionedMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
                    setProvisioningConfirmation(this.randomOOBInput);
//                    if (parseProvisioneeConfirmation(data)) {
//                        sendRandomConfirmationPDU(unprovisionedMeshNode);
//                    }
                } else {
                    parseProvisioningState(unprovisionedMeshNode, data);
                }
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
            case PROVISIONING_RANDOM:
                if (validateMessage(data)) {
                    if (parseProvisioneeRandom(data)) {
                        sendProvisioningData(unprovisionedMeshNode);
                    }
                } else {
                    parseProvisioningState(unprovisionedMeshNode, data);
                }
                break;
            case PROVISIONING_DATA:
            case PROVISIONING_COMPLETE:
            case PROVISIONING_FAILED:
                parseProvisioningState(unprovisionedMeshNode, data);
                break;

        }
    }

    void handleProvisioningWriteCallbacks() {
        final UnprovisionedMeshNode unprovisionedMeshNode = mUnprovisionedMeshNode;
        switch (provisioningState.getState()) {
            case PROVISIONING_INVITE:
                provisioningState = new ProvisioningCapabilitiesState(unprovisionedMeshNode, mStatusCallbacks);
                break;
            case PROVISIONING_CAPABILITIES:
                break;
            case PROVISIONING_START:
            case PROVISIONING_PUBLIC_KEY:
                //Devices with lower mtu have to send the key in multiple segments
                sendProvisionerPublicKey(unprovisionedMeshNode);
                break;
            case PROVISIONING_INPUT_COMPLETE:
                break;
            case PROVISIONING_CONFIRMATION:
                break;
            case PROVISIONING_RANDOM:
                break;
            case PROVISIONING_DATA:
                break;
        }
    }

    private void parseProvisioningState(final UnprovisionedMeshNode unprovisionedMeshNode, final byte[] data) {
        if (data[1] == ProvisioningState.State.PROVISIONING_COMPLETE.getState()) {
            provisioningState = new ProvisioningCompleteState(unprovisionedMeshNode);
            isProvisioningPublicKeySent = false;
            isProvisioneePublicKeyReceived = false;
            //Generate the network id and store it in the mesh node, this is needed to reconnect to the device at a later stage.
            final ProvisionedMeshNode provisionedMeshNode = new ProvisionedMeshNode(unprovisionedMeshNode);
            mInternalMeshManagerCallbacks.onNodeProvisioned(provisionedMeshNode);
            mStatusCallbacks.onProvisioningCompleted(provisionedMeshNode, ProvisioningState.States.PROVISIONING_COMPLETE, data);
        } else {
            isProvisioningPublicKeySent = false;
            isProvisioneePublicKeyReceived = false;
            final ProvisioningFailedState provisioningFailedState = new ProvisioningFailedState(mContext, unprovisionedMeshNode);
            provisioningState = provisioningFailedState;
            if (provisioningFailedState.parseData(data)) {
                mStatusCallbacks.onProvisioningFailed(unprovisionedMeshNode, ProvisioningState.States.PROVISIONING_FAILED, data);
            }
        }
    }

    /**
     * Initializes a mesh node object to be provisioned
     *
     * @param uuid               Device UUID of unprovisioned node
     * @param nodeName           Friendly node name
     * @param networkKey         Network key
     * @param flags              Flag containing the key refresh or the iv update operations
     * @param ivIndex            32-bit value shared across the network
     * @param unicastAddress     Unicast address to be assigned to the node
     * @param globalTtl          Global ttl which is also the number of hops to be used for a message
     * @param provisionerAddress Address of the provisioner
     * @return {@link MeshModel} to be provisioned
     */
    private UnprovisionedMeshNode initializeMeshNode(@NonNull final UUID uuid,
                                                     final String nodeName, @NonNull final NetworkKey networkKey,
                                                     final int flags, final int ivIndex,
                                                     final int unicastAddress, final int globalTtl, final int provisionerAddress) throws IllegalArgumentException {
        UnprovisionedMeshNode unprovisionedMeshNode = null;

        if (validateProvisioningDataInput(networkKey, flags, ivIndex)) {
            final byte[] flagBytes = ByteBuffer.allocate(1).put((byte) flags).array();

            byte[] ivIndexBytes = null;
            if (MeshParserUtils.validateIvIndexInput(mContext, ivIndex)) {
                ivIndexBytes = ByteBuffer.allocate(4).putInt(ivIndex).array();
            }

            unprovisionedMeshNode = new UnprovisionedMeshNode(uuid);
            unprovisionedMeshNode.setNodeName(nodeName);
            unprovisionedMeshNode.setNetworkKey(networkKey.getKey());
            unprovisionedMeshNode.setKeyIndex(networkKey.getKeyIndex());
            unprovisionedMeshNode.setFlags(flagBytes);
            unprovisionedMeshNode.setIvIndex(ivIndexBytes);
            unprovisionedMeshNode.setUnicastAddress(unicastAddress);
            unprovisionedMeshNode.setTtl(globalTtl);
            unprovisionedMeshNode.setConfigurationSrc(provisionerAddress);
            mUnprovisionedMeshNode = unprovisionedMeshNode;
        }
        return unprovisionedMeshNode;
    }

    private UUID getDeviceUuid(final byte[] serviceData) {
        if (serviceData != null && serviceData.length == 23) {

        }
        return null;
    }

    private boolean validateProvisioningDataInput(final NetworkKey networkKey, final Integer flags, final Integer ivIndex) {
        String error;

        if (networkKey == null) {
            error = "Network key cannot be null or empty!";
            throw new IllegalArgumentException(error);
        }

        if (networkKey.getKey() == null && networkKey.getKey().length != 16) {
            error = "Network key length must be 16 octets!";
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

        return true;
    }

    /**
     * Identifies the node that is to be provisioned.
     * <p>
     * This method will send a provisioning invite to the connected peripheral. This will help users to identify a particular node before starting the provisioning process.
     * This method must be invoked before calling {@link #startProvisioningNoOOB(UnprovisionedMeshNode)}
     * </p
     *
     * @param uuid               Device UUID of unprovisioned node
     * @param nodeName           Friendly node name
     * @param networkKey         Network key
     * @param flags              Flag containing the key refresh or the iv update operations
     * @param ivIndex            32-bit value shared across the network
     * @param unicastAddress     Unicast address to be assigned to the node
     * @param globalTtl          Global ttl which is also the number of hops to be used for a message
     * @param provisionerAddress Address of the provisioner
     */
    void identify(@NonNull final UUID uuid, final String nodeName, @NonNull final NetworkKey networkKey,
                  final int flags, final int ivIndex, final int unicastAddress,
                  final int globalTtl, final int provisionerAddress) throws IllegalArgumentException {
        confirmationInputs = null;
        staticOOBValue = null;
        final UnprovisionedMeshNode unprovisionedMeshNode = initializeMeshNode(uuid, nodeName, networkKey, flags, ivIndex, unicastAddress, globalTtl, provisionerAddress);
        sendProvisioningInvite(unprovisionedMeshNode);
    }

    /**
     * Starts provisioning an unprovisioned mesh node using No OOB
     * <p>
     * This method will continue the provisioning process that was started by invoking {@link #identify(UUID, String, NetworkKey, int, int, int, int, int)}.
     * </p>
     *
     * @param unprovisionedMeshNode {@link UnprovisionedMeshNode}
     */
    void startProvisioningNoOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException {
        this.attentionTimer = 0x05;
        this.usedAuthenticationOOBType = AuthenticationOOBMethods.NO_OOB_AUTHENTICATION;
        sendProvisioningStart(unprovisionedMeshNode);
    }

    /**
     * Starts provisioning an unprovisioned mesh node using Output OOB
     * <p>
     * This method will continue the provisioning process that was started by invoking {@link #identify(UUID, String, NetworkKey, int, int, int, int, int)}.
     * </p>
     *
     * @param unprovisionedMeshNode {@link UnprovisionedMeshNode}
     */
    void startProvisioningWithOutputOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException {
        this.attentionTimer = 0x05;
        this.usedAuthenticationOOBType = AuthenticationOOBMethods.OUTPUT_OOB_AUTHENTICATION;
        sendProvisioningStartWithOutputOOB(unprovisionedMeshNode);
    }

    /**
     * Starts provisioning an unprovisioned mesh node using Input OOB
     * <p>
     * This method will continue the provisioning process that was started by invoking {@link #identify(UUID, String, NetworkKey, int, int, int, int, int)}.
     * </p>
     *
     * @param unprovisionedMeshNode {@link UnprovisionedMeshNode}
     */
    void startProvisioningWithInputOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode, @NonNull final String randomInput) throws IllegalArgumentException {
        this.attentionTimer = 0x05;
        this.usedAuthenticationOOBType = AuthenticationOOBMethods.INPUT_OOB_AUTHENTICATION;
        this.randomOOBInput = randomInput;
        sendProvisioningStartWithInputOOB(unprovisionedMeshNode, randomInput);
    }

    /**
     * Starts provisioning an unprovisioned mesh node usign Static OOB
     * <p>
     * This method will continue the provisioning process that was started by invoking {@link #identify(UUID, String, NetworkKey, int, int, int, int, int)}.
     * </p>
     *
     * @param unprovisionedMeshNode {@link UnprovisionedMeshNode}
     */
    void startProvisioningWithStaticOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException {
        this.attentionTimer = 0x05;
        this.usedAuthenticationOOBType = AuthenticationOOBMethods.STATIC_OOB_AUTHENTICATION;
        //this.staticOOBValue = staticOOB;
        sendProvisioningStartWithStaticOOB(unprovisionedMeshNode);
    }

    private void sendProvisioningInvite(final UnprovisionedMeshNode unprovisionedMeshNode) {
        isProvisioningPublicKeySent = false;
        isProvisioneePublicKeyReceived = false;
        attentionTimer = 0x05;//0x0A;
        final ProvisioningInviteState invite = new ProvisioningInviteState(unprovisionedMeshNode, attentionTimer, mInternalTransportCallbacks, mStatusCallbacks);
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
        final ProvisioningCapabilitiesState provisioningCapabilitiesState = new ProvisioningCapabilitiesState(unprovisionedMeshNode, mStatusCallbacks);
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

        final ProvisioningStartState startProvisioning = new ProvisioningStartState(unprovisionedMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
        startProvisioning.setProvisioningCapabilities(numberOfElements, algorithm, publicKeyType, staticOOBType, outputOOBSize, outputOOBAction, inputOOBSize, inputOOBAction);
        //final short outputOobActionType = (byte) ParseOutputOOBActions.selectOutputActionsFromBitMask(outputOOBAction);
        //startProvisioning.setUseOutputOOB(outputOobActionType);
        provisioningState = startProvisioning;
        startProvisioning.executeSend();
    }

    private void sendProvisioningStartWithOutputOOB(final UnprovisionedMeshNode unprovisionedMeshNode) {
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

        final ProvisioningStartState startProvisioning = new ProvisioningStartState(unprovisionedMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
        startProvisioning.setProvisioningCapabilities(numberOfElements, algorithm, publicKeyType, staticOOBType, outputOOBSize, outputOOBAction, inputOOBSize, inputOOBAction);
        final short outputOobActionType = (byte) ParseOutputOOBActions.selectOutputActionsFromBitMask(outputOOBAction);
        startProvisioning.setUseOutputOOB(outputOobActionType);
        provisioningState = startProvisioning;
        startProvisioning.executeSend();
    }

    private void sendProvisioningStartWithInputOOB(final UnprovisionedMeshNode unprovisionedMeshNode, final String randomInput) {
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

        final ProvisioningStartState startProvisioning = new ProvisioningStartState(unprovisionedMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
        startProvisioning.setProvisioningCapabilities(numberOfElements, algorithm, publicKeyType, staticOOBType, outputOOBSize, outputOOBAction, inputOOBSize, inputOOBAction);
        startProvisioning.setUseInputOOB(ParseInputOOBActions.selectInputActionsFromBitMask(inputOOBAction));
        provisioningState = startProvisioning;
        startProvisioning.executeSend();
    }

    private void sendProvisioningStartWithStaticOOB(final UnprovisionedMeshNode unprovisionedMeshNode) {
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

        final ProvisioningStartState startProvisioning = new ProvisioningStartState(unprovisionedMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
        startProvisioning.setProvisioningCapabilities(numberOfElements, algorithm, publicKeyType, staticOOBType, outputOOBSize, outputOOBAction, inputOOBSize, inputOOBAction);
        startProvisioning.setUseStaticOOB();
        provisioningState = startProvisioning;
        startProvisioning.executeSend();
    }

    private void sendProvisionerPublicKey(final UnprovisionedMeshNode unprovisionedMeshNode) {
        if (!isProvisioningPublicKeySent) {
            if (provisioningState instanceof ProvisioningPublicKeyState) {
                isProvisioningPublicKeySent = true;
                provisioningState.executeSend();
            } else {
                final ProvisioningPublicKeyState provisioningPublicKeyState = new ProvisioningPublicKeyState(unprovisionedMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
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
                switch (this.usedAuthenticationOOBType) {
                    case INPUT_OOB_AUTHENTICATION:
                        provisioningState = new ProvisioningInputCompleteState(unprovisionedMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
                        mStatusCallbacks.onProvisioningStateChanged(mUnprovisionedMeshNode, ProvisioningState.States.PROVISIONING_AUTHENTICATION_INPUT_OOB_WAITING, data);
                        break;
                    case OUTPUT_OOB_AUTHENTICATION:
                        provisioningState = new ProvisioningConfirmationState(this, unprovisionedMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
                        mStatusCallbacks.onProvisioningStateChanged(mUnprovisionedMeshNode, ProvisioningState.States.PROVISIONING_AUTHENTICATION_OUTPUT_OOB_WAITING, data);
                        break;
                    case STATIC_OOB_AUTHENTICATION:
                        provisioningState = new ProvisioningConfirmationState(this, unprovisionedMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
                        mStatusCallbacks.onProvisioningStateChanged(mUnprovisionedMeshNode, ProvisioningState.States.PROVISIONING_AUTHENTICATION_STATIC_OOB_WAITING, data);
                        //setProvisioningStaticConfirmation(staticOOBValue);
                        break;
                    default:
                        provisioningState = new ProvisioningConfirmationState(this, unprovisionedMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
                        setProvisioningConfirmation("");
                        break;
                }
            }
        }
    }

    void setProvisioningConfirmation(final String pin) {
        if (pin != null /*&& pin.length() > 0*/) {
            final ProvisioningConfirmationState provisioningConfirmationState = (ProvisioningConfirmationState) provisioningState;
            provisioningConfirmationState.setPin(pin);
            provisioningConfirmationState.executeSend();
        }
    }

    void setProvisioningStaticConfirmation(final byte[] authenticationValue) {
        if (authenticationValue != null /*&& pin.length() > 0*/) {
            final ProvisioningConfirmationState provisioningConfirmationState = (ProvisioningConfirmationState) provisioningState;
            provisioningConfirmationState.setStaticAuthenticationValue(authenticationValue);
            provisioningConfirmationState.executeSend();
        }
    }

    private boolean parseProvisioneeConfirmation(final byte[] data) {
        final ProvisioningConfirmationState provisioningConfirmationState = (ProvisioningConfirmationState) provisioningState;
        return provisioningConfirmationState.parseData(data);
    }

    private void sendRandomConfirmationPDU(final UnprovisionedMeshNode unprovisionedMeshNode) {
        final ProvisioningRandomConfirmationState provisioningRandomConfirmation = new ProvisioningRandomConfirmationState(this, unprovisionedMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
        provisioningState = provisioningRandomConfirmation;
        provisioningRandomConfirmation.executeSend();
    }

    private boolean parseProvisioneeRandom(final byte[] data) {
        final ProvisioningRandomConfirmationState provisioningRandomConfirmation = (ProvisioningRandomConfirmationState) provisioningState;
        return provisioningRandomConfirmation.parseData(data);
    }

    private void sendProvisioningData(final UnprovisionedMeshNode unprovisionedMeshNode) {
        final ProvisioningDataState provisioningDataState = new ProvisioningDataState(this, unprovisionedMeshNode, mInternalTransportCallbacks, mStatusCallbacks);
        provisioningState = provisioningDataState;
        provisioningDataState.executeSend();
    }

    private boolean validateMessage(final byte[] data) {
        final ProvisioningState state = provisioningState;
        return data[1] == state.getState().ordinal();

    }

    /**
     * Generates the confirmation inputs for a provisionee
     *
     * @param provisionerKeyXY xy components of the provisioner public key
     * @param provisioneeKeyXY xy components of the provisionee public key
     */
    public final byte[] generateConfirmationInputs(final byte[] provisionerKeyXY, final byte[] provisioneeKeyXY) {
        //invite: 1 bytes, capabilities: 11 bytes, start: 5 bytes, provisionerKey: 64 bytes, deviceKey: 64 bytes
        //Append all the raw data together
        if (confirmationInputs != null) {
            return confirmationInputs;
        }

        //We must remove the first two bytes which is the pdu type and the provisioning pdu type
        final int offset = 2;
        final int inviteLength = mUnprovisionedMeshNode.getProvisioningInvitePdu().length - offset;
        final ByteBuffer inviteBuffer = ByteBuffer.allocate(inviteLength).
                put(mUnprovisionedMeshNode.getProvisioningInvitePdu(), offset, inviteLength);
        final byte[] invite = inviteBuffer.array();

        //We must remove the first two bytes which is the pdu type and the provisioning pdu type
        final int capabilitiesLength = mUnprovisionedMeshNode.getProvisioningCapabilitiesPdu().length - offset;
        final ByteBuffer capabilitiesBuffer = ByteBuffer.allocate(capabilitiesLength)
                .put(mUnprovisionedMeshNode.getProvisioningCapabilitiesPdu(), offset, capabilitiesLength);
        final byte[] capabilities = capabilitiesBuffer.array();

        //We must remove the first two bytes which is the pdu type and the provisioning pdu type
        final int startDataLength = mUnprovisionedMeshNode.getProvisioningStartPdu().length - offset;
        final ByteBuffer startDataBuffer = ByteBuffer.allocate(startDataLength).
                put(mUnprovisionedMeshNode.getProvisioningStartPdu(), offset, startDataLength);
        final byte[] startData = startDataBuffer.array();//get(startData, 2, startDataLength);

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
        confirmationInputs = buffer.array();
        return confirmationInputs;
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
            startData[3] = (byte) ParseOutputOOBActions.getOutputOOBActionValue(outputOobActionType);//(byte) ParseOutputOOBActions.getOutputOOBActionValue(outputOOBAction);
            startData[4] = (byte) outputOOBSize;
        }

        return startData;
    }
}
