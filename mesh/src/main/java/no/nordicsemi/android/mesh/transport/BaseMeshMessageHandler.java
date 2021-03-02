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

package no.nordicsemi.android.mesh.transport;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import org.spongycastle.crypto.InvalidCipherTextException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.InternalTransportCallbacks;
import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.MeshStatusCallbacks;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.utils.ExtendedInvalidCipherTextException;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static no.nordicsemi.android.mesh.transport.NetworkLayer.createNetworkNonce;
import static no.nordicsemi.android.mesh.transport.NetworkLayer.createProxyNonce;
import static no.nordicsemi.android.mesh.transport.NetworkLayer.deObfuscateNetworkHeader;

/**
 * Abstract class that handles mesh messages
 */
public abstract class BaseMeshMessageHandler implements MeshMessageHandlerApi, InternalMeshMsgHandlerCallbacks {

    private static final String TAG = BaseMeshMessageHandler.class.getSimpleName();

    protected final Context mContext;
    protected final InternalTransportCallbacks mInternalTransportCallbacks;
    private final NetworkLayerCallbacks networkLayerCallbacks;
    private final UpperTransportLayerCallbacks upperTransportLayerCallbacks;
    protected MeshStatusCallbacks mStatusCallbacks;
    private final SparseArray<MeshTransport> transportSparseArray = new SparseArray<>();
    private final SparseArray<MeshMessageState> stateSparseArray = new SparseArray<>();

    /**
     * Constructs BaseMessageHandler
     *
     * @param context                      Context
     * @param internalTransportCallbacks   {@link InternalTransportCallbacks} Callbacks
     * @param networkLayerCallbacks        {@link NetworkLayerCallbacks} network layer callbacks
     * @param upperTransportLayerCallbacks {@link UpperTransportLayerCallbacks} upper transport layer callbacks
     */
    protected BaseMeshMessageHandler(@NonNull final Context context,
                                     @NonNull final InternalTransportCallbacks internalTransportCallbacks,
                                     @NonNull final NetworkLayerCallbacks networkLayerCallbacks,
                                     @NonNull final UpperTransportLayerCallbacks upperTransportLayerCallbacks) {
        this.mContext = context;
        this.mInternalTransportCallbacks = internalTransportCallbacks;
        this.networkLayerCallbacks = networkLayerCallbacks;
        this.upperTransportLayerCallbacks = upperTransportLayerCallbacks;
    }

    /**
     * Sets the mesh status callbacks.
     *
     * @param statusCallbacks {@link MeshStatusCallbacks} callbacks
     */
    protected abstract void setMeshStatusCallbacks(@NonNull final MeshStatusCallbacks statusCallbacks);

    /**
     * Parse the mesh network/proxy pdus
     * <p>
     * This method will try to network layer de-obfuscation and decryption using the available network keys
     * </p>
     *
     * @param pdu     mesh pdu that was sent
     * @param network {@link MeshNetwork}
     */
    protected void parseMeshPduNotifications(@NonNull final byte[] pdu, @NonNull final MeshNetwork network) throws ExtendedInvalidCipherTextException {
        final List<NetworkKey> networkKeys = network.getNetKeys();
        final int ivi = ((pdu[1] & 0xFF) >>> 7) & 0x01;
        final int nid = pdu[1] & 0x7F;
        final int acceptedIvIndex = network.getIvIndex().getIvIndex();
        int ivIndex = acceptedIvIndex == 0 ? 0 : acceptedIvIndex - 1;
        NetworkKey networkKey;
        SecureUtils.K2Output k2Output;
        byte[] networkHeader;
        int ctlTtl;
        int ctl;
        int ttl;
        while (ivIndex <= ivIndex + 1) {
            //Here we go through all the network keys and filter out network keys based on the nid.
            for (int i = 0; i < networkKeys.size(); i++) {
                networkKey = networkKeys.get(i);
                k2Output = getMatchingK2Output(networkKey, nid);
                if (k2Output != null) {
                    networkHeader = deObfuscateNetworkHeader(pdu, MeshParserUtils.intToBytes(ivIndex), k2Output.getPrivacyKey());
                    ctlTtl = networkHeader[0];
                    ctl = (ctlTtl >> 7) & 0x01;
                    ttl = ctlTtl & 0x7F;
                    Log.v(TAG, "TTL for received message: " + ttl);
                    final int src = MeshParserUtils.unsignedBytesToInt(networkHeader[5], networkHeader[4]);

                    final ProvisionedMeshNode node = network.getNode(src);
                    if (node == null) {
                        continue;
                    }

                    final byte[] sequenceNumber = ByteBuffer.allocate(3).order(ByteOrder.BIG_ENDIAN).put(networkHeader, 1, 3).array();
                    Log.v(TAG, "Sequence number of received access message: " + MeshParserUtils.convert24BitsToInt(sequenceNumber));
                    //TODO validate ivi
                    byte[] nonce;
                    try {
                        final int networkPayloadLength = pdu.length - (2 + networkHeader.length);
                        final byte[] transportPdu = new byte[networkPayloadLength];
                        System.arraycopy(pdu, 8, transportPdu, 0, networkPayloadLength);
                        final byte[] decryptedPayload;
                        final MeshMessageState state;
                        if (pdu[0] == MeshManagerApi.PDU_TYPE_NETWORK) {
                            nonce = createNetworkNonce((byte) ctlTtl, sequenceNumber, src, MeshParserUtils.intToBytes(ivIndex));
                            decryptedPayload = SecureUtils.decryptCCM(transportPdu, k2Output.getEncryptionKey(), nonce, SecureUtils.getNetMicLength(ctl));
                            state = getState(src);
                        } else {
                            nonce = createProxyNonce(sequenceNumber, src, MeshParserUtils.intToBytes(ivIndex));
                            decryptedPayload = SecureUtils.decryptCCM(transportPdu, k2Output.getEncryptionKey(), nonce, SecureUtils.getNetMicLength(ctl));
                            state = getState(MeshAddress.UNASSIGNED_ADDRESS);
                        }
                        if (state != null) {
                            //TODO look in to proxy filter messages
                            ((DefaultNoOperationMessageState) state).parseMeshPdu(networkKey, node, pdu, networkHeader, decryptedPayload, ivIndex, sequenceNumber);
                            return;
                        }
                    } catch (InvalidCipherTextException ex) {
                        if (i == networkKeys.size() - 1) {
                            throw new ExtendedInvalidCipherTextException(ex.getMessage(), ex.getCause(), TAG);
                        }
                    }
                }
            }
            ivIndex++;
        }
    }

    /**
     * Returns the K2output for a given Network Key matching the received NID
     *
     * @param networkKey NetworkKey
     * @param nid        NID
     * @return {@link SecureUtils.K2Output} or null if the NID doesn't match.
     */
    private SecureUtils.K2Output getMatchingK2Output(@NonNull final NetworkKey networkKey, final int nid) {
        if(nid == networkKey.getDerivatives().getNid())
            return networkKey.getDerivatives();
        else if(networkKey.getOldDerivatives() != null && nid == networkKey.getOldDerivatives().getNid())
            return networkKey.getOldDerivatives();
        return null;
    }

    @Override
    public final void onIncompleteTimerExpired(final int address) {
        //We switch no operation state if the incomplete timer has expired so that we don't wait on the same state if a particular message fails.
        stateSparseArray.put(address, toggleState(getTransport(address), getState(address).getMeshMessage()));
    }

    /**
     * Toggles the current state to default state of a node
     *
     * @param transport   mesh transport of the current state
     * @param meshMessage Mesh message
     */
    private DefaultNoOperationMessageState toggleState(@NonNull final MeshTransport transport, @Nullable final MeshMessage meshMessage) {
        final DefaultNoOperationMessageState state = new DefaultNoOperationMessageState(meshMessage, transport, this);
        state.setTransportCallbacks(mInternalTransportCallbacks);
        state.setStatusCallbacks(mStatusCallbacks);
        return state;
    }

    /**
     * Returns the existing state or a new state if nothing exists for a node
     *
     * @param address address of the node
     */
    protected MeshMessageState getState(final int address) {
        MeshMessageState state = stateSparseArray.get(address);
        if (state == null) {
            state = new DefaultNoOperationMessageState(null, getTransport(address), this);
            state.setTransportCallbacks(mInternalTransportCallbacks);
            state.setStatusCallbacks(mStatusCallbacks);
            stateSparseArray.put(address, state);
        }
        return state;
    }

    /**
     * Returns the existing transport of the node or a new transport if nothing exists
     *
     * @param address address of the node
     */
    private MeshTransport getTransport(final int address) {
        MeshTransport transport = transportSparseArray.get(address);
        if (transport == null) {
            transport = new MeshTransport(mContext);
            transport.setNetworkLayerCallbacks(networkLayerCallbacks);
            transport.setUpperTransportLayerCallbacks(upperTransportLayerCallbacks);
            transportSparseArray.put(address, transport);
        }
        return transport;
    }

    /**
     * Resets the state and transport for a given node address
     *
     * @param address unicast address of the node
     */
    public void resetState(final int address) {
        stateSparseArray.remove(address);
        transportSparseArray.remove(address);
    }

    @Override
    public void createMeshMessage(final int src, final int dst, @Nullable final UUID label, @NonNull final MeshMessage meshMessage) {
        if (meshMessage instanceof ProxyConfigMessage) {
            createProxyConfigMeshMessage(src, dst, (ProxyConfigMessage) meshMessage);
        } else if (meshMessage instanceof ConfigMessage) {
            createConfigMeshMessage(src, dst, (ConfigMessage) meshMessage);
        } else if (meshMessage instanceof ApplicationMessage) {
            if (label == null) {
                createAppMeshMessage(src, dst, (ApplicationMessage) meshMessage);
            } else {
                createAppMeshMessage(src, dst, label, (ApplicationMessage) meshMessage);
            }
        }
    }

    /**
     * Sends a mesh message specified within the {@link MeshMessage} object
     *
     * @param configurationMessage {@link ProxyConfigMessage} Mesh message containing the message opcode and message parameters
     */
    private void createProxyConfigMeshMessage(final int src, final int dst, @NonNull final ProxyConfigMessage configurationMessage) {
        final ProxyConfigMessageState currentState = new ProxyConfigMessageState(src, dst, configurationMessage, getTransport(dst), this);
        currentState.setTransportCallbacks(mInternalTransportCallbacks);
        currentState.setStatusCallbacks(mStatusCallbacks);
        stateSparseArray.put(dst, toggleState(currentState.getMeshTransport(), configurationMessage));
        currentState.executeSend();
    }

    /**
     * Sends a mesh message specified within the {@link MeshMessage} object
     *
     * @param configurationMessage {@link ConfigMessage} Mesh message containing the message opcode and message parameters
     */
    private void createConfigMeshMessage(final int src, final int dst, @NonNull final ConfigMessage configurationMessage) {
        final ProvisionedMeshNode node = mInternalTransportCallbacks.getNode(dst);
        if (node == null) {
            return;
        }

        final ConfigMessageState currentState = new ConfigMessageState(src, dst, node.getDeviceKey(), configurationMessage, getTransport(dst), this);
        currentState.setTransportCallbacks(mInternalTransportCallbacks);
        currentState.setStatusCallbacks(mStatusCallbacks);
        if (MeshAddress.isValidUnicastAddress(dst)) {
            stateSparseArray.put(dst, toggleState(getTransport(dst), configurationMessage));
        }
        currentState.executeSend();
    }

    /**
     * Sends a mesh message specified within the {@link ApplicationMessage} object
     * <p> This method can be used specifically when sending an application message with a unicast address or a group address.
     * Application messages currently supported in the library are {@link GenericOnOffGet},{@link GenericOnOffSet}, {@link GenericOnOffSetUnacknowledged},
     * {@link GenericLevelGet},  {@link GenericLevelSet},  {@link GenericLevelSetUnacknowledged},
     * {@link VendorModelMessageAcked} and {@link VendorModelMessageUnacked}</p>
     *
     * @param src            source address where the message is originating from
     * @param dst            Destination to which the message must be sent to, this could be a unicast address or a group address.
     * @param applicationMessage Mesh message containing the message opcode and message parameters.
     */
    private void createAppMeshMessage(final int src, final int dst, @NonNull final ApplicationMessage applicationMessage) {
        final ApplicationMessageState currentState;
        if (applicationMessage instanceof VendorModelMessageAcked) {
            currentState = new VendorModelMessageAckedState(src, dst, (VendorModelMessageAcked) applicationMessage, getTransport(dst), this);
        } else if (applicationMessage instanceof VendorModelMessageUnacked) {
            currentState = new VendorModelMessageUnackedState(src, dst, (VendorModelMessageUnacked) applicationMessage, getTransport(dst), this);
        } else {
            currentState = new ApplicationMessageState(src, dst, applicationMessage, getTransport(dst), this);
        }
        currentState.setTransportCallbacks(mInternalTransportCallbacks);
        currentState.setStatusCallbacks(mStatusCallbacks);
        if (MeshAddress.isValidUnicastAddress(dst)) {
            stateSparseArray.put(dst, toggleState(getTransport(dst), applicationMessage));
        }
        currentState.executeSend();
    }


    /**
     * Sends a mesh message specified within the {@link ApplicationMessage} object
     * <p> This method can be used specifically when sending an application message with a unicast address or a group address.
     * Application messages currently supported in the library are {@link GenericOnOffGet},{@link GenericOnOffSet}, {@link GenericOnOffSetUnacknowledged},
     * {@link GenericLevelGet},  {@link GenericLevelSet},  {@link GenericLevelSetUnacknowledged},
     * {@link VendorModelMessageAcked} and {@link VendorModelMessageUnacked}</p>
     *
     * @param src            source address where the message is originating from
     * @param dst            Destination to which the message must be sent to, this could be a unicast address or a group address.
     * @param label          Label UUID of destination address
     * @param applicationMessage Mesh message containing the message opcode and message parameters.
     */
    private void createAppMeshMessage(final int src, final int dst, @NonNull UUID label, @NonNull final ApplicationMessage applicationMessage) {
        final ApplicationMessageState currentState;
        if (applicationMessage instanceof VendorModelMessageAcked) {
            currentState = new VendorModelMessageAckedState(src, dst, label, (VendorModelMessageAcked) applicationMessage, getTransport(dst), this);
        } else if (applicationMessage instanceof VendorModelMessageUnacked) {
            currentState = new VendorModelMessageUnackedState(src, dst, label, (VendorModelMessageUnacked) applicationMessage, getTransport(dst), this);
        } else {
            currentState = new ApplicationMessageState(src, dst, label, applicationMessage, getTransport(dst), this);
        }
        currentState.setTransportCallbacks(mInternalTransportCallbacks);
        currentState.setStatusCallbacks(mStatusCallbacks);
        if (MeshAddress.isValidUnicastAddress(dst)) {
            stateSparseArray.put(dst, toggleState(getTransport(dst), applicationMessage));
        }
        currentState.executeSend();
    }
}
