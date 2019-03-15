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

package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.InternalTransportCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.MeshStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

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
    private SparseArray<MeshTransport> transportSparseArray = new SparseArray<>();
    private SparseArray<MeshMessageState> stateSparseArray = new SparseArray<>();

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
     * Handle mesh States on receiving mesh message notifications
     * <p>
     * This method will jump to the current state and switch the state depending on the expected and the next message received.
     * </p>
     *
     * @param pdu     mesh pdu that was sent
     * @param network {@link MeshNetwork}
     */
    protected void parseNetworkPduNotifications(@NonNull final byte[] pdu, @NonNull final MeshNetwork network) {
        final byte[] networkHeader = deObfuscateNetworkHeader(pdu, network.getPrimaryNetworkKey(), MeshParserUtils.intToBytes(network.getIvIndex()));
        if (networkHeader != null) {
            final int src = MeshParserUtils.unsignedBytesToInt(networkHeader[5], networkHeader[4]);
            final MeshMessageState state = getState(src);
            if (state != null) {
                ((DefaultNoOperationMessageState) state).parseMeshPdu(pdu);
            }
        }
    }

    /**
     * Handle mesh States on receiving mesh message notifications
     * <p>
     * This method will jump to the current state and switch the state depending on the expected and the next message received.
     * </p>
     *
     * @param pdu mesh pdu that was sent
     */
    protected void parseProxyPduNotifications(@NonNull final byte[] pdu) {
        final MeshMessageState state = getState(MeshAddress.UNASSIGNED_ADDRESS);
        if (state instanceof DefaultNoOperationMessageState) {
            ((DefaultNoOperationMessageState) state).parseMeshPdu(pdu);
        }
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
        final DefaultNoOperationMessageState state = new DefaultNoOperationMessageState(mContext, meshMessage, transport, this);
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
            state = new DefaultNoOperationMessageState(mContext, null, getTransport(address), this);
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
    public void sendMeshMessage(final int src, final int dst, @NonNull final MeshMessage meshMessage) {
        if (meshMessage instanceof ProxyConfigMessage) {
            sendProxyConfigMeshMessage(src, dst, (ProxyConfigMessage) meshMessage);
        } else if (meshMessage instanceof ConfigMessage) {
            sendConfigMeshMessage(src, dst, (ConfigMessage) meshMessage);
        } else if (meshMessage instanceof GenericMessage) {
            sendAppMeshMessage(src, dst, (GenericMessage) meshMessage);
        }
    }

    /**
     * Sends a mesh message specified within the {@link MeshMessage} object
     *
     * @param configurationMessage {@link ProxyConfigMessage} Mesh message containing the message opcode and message parameters
     */
    private void sendProxyConfigMeshMessage(final int src, final int dst, @NonNull final ProxyConfigMessage configurationMessage) {
        final ProxyConfigMessageState currentState = new ProxyConfigMessageState(mContext, src, dst, configurationMessage, getTransport(dst), this);
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
    private void sendConfigMeshMessage(final int src, final int dst, @NonNull final ConfigMessage configurationMessage) {
        final ProvisionedMeshNode node = mInternalTransportCallbacks.getProvisionedNode(dst);
        if (node == null) {
            return;
        }

        final ConfigMessageState currentState = new ConfigMessageState(mContext, src, dst, node.getDeviceKey(), configurationMessage, getTransport(dst), this);
        currentState.setTransportCallbacks(mInternalTransportCallbacks);
        currentState.setStatusCallbacks(mStatusCallbacks);
        if (MeshAddress.isValidUnicastAddress(dst)) {
            stateSparseArray.put(dst, toggleState(getTransport(dst), configurationMessage));
        }
        currentState.executeSend();
    }


    /**
     * Sends a mesh message specified within the {@link GenericMessage} object
     * <p> This method can be used specifically when sending an application message with a unicast address or a group address.
     * Application messages currently supported in the library are {@link GenericOnOffGet},{@link GenericOnOffSet}, {@link GenericOnOffSetUnacknowledged},
     * {@link GenericLevelGet},  {@link GenericLevelSet},  {@link GenericLevelSetUnacknowledged},
     * {@link VendorModelMessageAcked} and {@link VendorModelMessageUnacked}</p>
     *
     * @param src            source address where the message is originating from
     * @param dst            Destination to which the message must be sent to, this could be a unicast address or a group address.
     * @param genericMessage Mesh message containing the message opcode and message parameters.
     */
    private void sendAppMeshMessage(final int src, final int dst, @NonNull final GenericMessage genericMessage) {
        final GenericMessageState currentState = new GenericMessageState(mContext, src, dst, genericMessage, getTransport(dst), this);
        currentState.setTransportCallbacks(mInternalTransportCallbacks);
        currentState.setStatusCallbacks(mStatusCallbacks);
        if (MeshAddress.isValidUnicastAddress(dst)) {
            stateSparseArray.put(dst, toggleState(getTransport(dst), genericMessage));
        }
        currentState.executeSend();
    }

    /**
     * De-obfuscates the network header
     *
     * @param pdu received from the node
     * @return obfuscted network header
     */
    private byte[] deObfuscateNetworkHeader(@NonNull final byte[] pdu, @NonNull final NetworkKey key, @NonNull final byte[] ivIndex) {
        final SecureUtils.K2Output k2Output = SecureUtils.calculateK2(key.getKey(), SecureUtils.K2_MASTER_INPUT);
        final byte[] privacyKey = k2Output.getPrivacyKey();
        final ByteBuffer obfuscatedNetworkBuffer = ByteBuffer.allocate(6);
        obfuscatedNetworkBuffer.order(ByteOrder.BIG_ENDIAN);
        obfuscatedNetworkBuffer.put(pdu, 2, 6);
        final byte[] obfuscatedData = obfuscatedNetworkBuffer.array();

        final ByteBuffer privacyRandomBuffer = ByteBuffer.allocate(7);
        privacyRandomBuffer.order(ByteOrder.BIG_ENDIAN);
        privacyRandomBuffer.put(pdu, 8, 7);
        final byte[] privacyRandom = createPrivacyRandom(privacyRandomBuffer.array());

        final byte[] pecb = createPECB(ivIndex, privacyRandom, privacyKey);
        final byte[] deObfuscatedData = new byte[6];

        for (int i = 0; i < 6; i++)
            deObfuscatedData[i] = (byte) (obfuscatedData[i] ^ pecb[i]);

        return deObfuscatedData;
    }

    /**
     * Creates the privacy random.
     *
     * @param encryptedUpperTransportPDU encrypted transport pdu
     * @return Privacy random
     */
    private byte[] createPrivacyRandom(final byte[] encryptedUpperTransportPDU) {
        final byte[] privacyRandom = new byte[7];
        System.arraycopy(encryptedUpperTransportPDU, 0, privacyRandom, 0, privacyRandom.length);
        return privacyRandom;
    }

    private byte[] createPECB(final byte[] ivIndex, final byte[] privacyRandom, final byte[] privacyKey) {
        final ByteBuffer buffer = ByteBuffer.allocate(5 + privacyRandom.length + ivIndex.length);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00});
        buffer.put(ivIndex);
        buffer.put(privacyRandom);
        final byte[] temp = buffer.array();
        Log.v(TAG, "Privacy Random: " + MeshParserUtils.bytesToHex(temp, false));
        return SecureUtils.encryptWithAES(temp, privacyKey);
    }
}
