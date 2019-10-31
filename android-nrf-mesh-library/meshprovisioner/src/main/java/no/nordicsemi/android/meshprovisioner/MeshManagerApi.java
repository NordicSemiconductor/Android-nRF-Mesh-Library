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
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.meshprovisioner.data.ApplicationKeyDao;
import no.nordicsemi.android.meshprovisioner.data.ApplicationKeysDao;
import no.nordicsemi.android.meshprovisioner.data.GroupDao;
import no.nordicsemi.android.meshprovisioner.data.GroupsDao;
import no.nordicsemi.android.meshprovisioner.data.MeshNetworkDao;
import no.nordicsemi.android.meshprovisioner.data.NetworkKeyDao;
import no.nordicsemi.android.meshprovisioner.data.NetworkKeysDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionedMeshNodeDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionedMeshNodesDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionerDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionersDao;
import no.nordicsemi.android.meshprovisioner.data.SceneDao;
import no.nordicsemi.android.meshprovisioner.data.ScenesDao;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.NetworkLayerCallbacks;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.UpperTransportLayerCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.ExtendedInvalidCipherTextException;
import no.nordicsemi.android.meshprovisioner.utils.InputOOBAction;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.OutputOOBAction;
import no.nordicsemi.android.meshprovisioner.utils.ProxyFilter;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;


@SuppressWarnings("WeakerAccess")
public class MeshManagerApi implements MeshMngrApi {

    private static final String TAG = MeshManagerApi.class.getSimpleName();
    public final static UUID MESH_PROVISIONING_UUID = UUID.fromString("00001827-0000-1000-8000-00805F9B34FB");
    public final static UUID MESH_PROXY_UUID = UUID.fromString("00001828-0000-1000-8000-00805F9B34FB");
    public static final byte PDU_TYPE_PROVISIONING = 0x03;

    //PDU types
    public static final byte PDU_TYPE_NETWORK = 0x00;
    public static final byte PDU_TYPE_MESH_BEACON = 0x01;
    public static final byte PDU_TYPE_PROXY_CONFIGURATION = 0x02;
    //GATT level segmentation
    private static final byte GATT_SAR_COMPLETE = 0b00;
    private static final byte GATT_SAR_START = 0b01;
    private static final byte GATT_SAR_CONTINUATION = 0b10;
    private static final byte GATT_SAR_END = 0b11;
    //GATT level segmentation mask
    private static final int GATT_SAR_MASK = 0xC0;
    private static final int GATT_SAR_UNMASK = 0x3F;
    private static final int SAR_BIT_OFFSET = 6;

    private static final long PROXY_SAR_TRANSFER_TIME_OUT = 20 * 1000; // According to the spec the proxy protocol must contain an SAR timeout of 20 seconds.
    private final static int HASH_RANDOM_NUMBER_LENGTH = 64; // Length of the random number required to calculate the hash containing the node id in bits
    private static final int ADVERTISEMENT_TYPE_NETWORK_ID = 0x00;
    private static final int ADVERTISEMENT_TYPE_NODE_IDENTITY = 0x01;
    private final static int ADVERTISED_HASH_OFFSET = 1; // Offset of the hash contained in the advertisement service data
    private final static int ADVERTISED_HASH_LENGTH = 8; // Length of the hash contained in the advertisement service data
    private final static int ADVERTISED_RANDOM_OFFSET = 9; // Offset of the hash contained in the advertisement service data
    private final static int ADVERTISED_RANDOM_LENGTH = 8; //Length of the hash contained in the advertisement service data
    private final static int ADVERTISED_NETWORK_ID_OFFSET = 1; //Offset of the network id contained in the advertisement service data
    private final static int ADVERTISED_NETWORK_ID_LENGTH = 8; //Length of the network id contained in the advertisement service data

    private Context mContext;
    private final Handler mHandler;
    private MeshManagerCallbacks mMeshManagerCallbacks;
    private MeshProvisioningHandler mMeshProvisioningHandler;
    private MeshMessageHandler mMeshMessageHandler;
    private byte[] mIncomingBuffer;
    private int mIncomingBufferOffset;
    private byte[] mOutgoingBuffer;
    private int mOutgoingBufferOffset;
    private MeshNetwork mMeshNetwork;

    private MeshNetworkDb mMeshNetworkDb;
    private MeshNetworkDao mMeshNetworkDao;
    private NetworkKeyDao mNetworkKeyDao;
    private NetworkKeysDao mNetworkKeysDao;
    private ApplicationKeyDao mApplicationKeyDao;
    private ApplicationKeysDao mApplicationKeysDao;
    private ProvisionerDao mProvisionerDao;
    private ProvisionersDao mProvisionersDao;
    private ProvisionedMeshNodeDao mProvisionedNodeDao;
    private ProvisionedMeshNodesDao mProvisionedNodesDao;
    private GroupDao mGroupDao;
    private GroupsDao mGroupsDao;
    private SceneDao mSceneDao;
    private ScenesDao mScenesDao;

    private final Runnable mProxyProtocolTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            mMeshMessageHandler.onIncompleteTimerExpired(MeshAddress.UNASSIGNED_ADDRESS);
        }
    };

    /**
     * The mesh manager api constructor.
     * <p>
     * After constructing the manager, the meshProvision following callbacks must be set
     * {@link #setMeshManagerCallbacks(MeshManagerCallbacks)}.
     * {@link #setProvisioningStatusCallbacks(MeshProvisioningStatusCallbacks)}.
     * {@link #setMeshStatusCallbacks(MeshStatusCallbacks)}.
     * <p>
     *
     * @param context context
     */
    public MeshManagerApi(@NonNull final Context context) {
        this.mContext = context;
        mHandler = new Handler();
        mMeshProvisioningHandler = new MeshProvisioningHandler(context, internalTransportCallbacks, internalMeshMgrCallbacks);
        mMeshMessageHandler = new MeshMessageHandler(context, internalTransportCallbacks, networkLayerCallbacks, upperTransportLayerCallbacks);
        initBouncyCastle();
        //Init database
        initDb(context);

    }

    @Override
    public void setMeshManagerCallbacks(@NonNull final MeshManagerCallbacks callbacks) {
        mMeshManagerCallbacks = callbacks;
    }

    @Override
    public void setProvisioningStatusCallbacks(@NonNull final MeshProvisioningStatusCallbacks callbacks) {
        mMeshProvisioningHandler.setProvisioningCallbacks(callbacks);
    }

    @Override
    public void setMeshStatusCallbacks(@NonNull final MeshStatusCallbacks callbacks) {
        mMeshMessageHandler.setMeshStatusCallbacks(callbacks);
    }

    @Override
    public void loadMeshNetwork() {
        mMeshNetworkDb.loadNetwork(mMeshNetworkDao, mNetworkKeysDao, mApplicationKeysDao, mProvisionersDao, mProvisionedNodesDao,
                mGroupsDao, mScenesDao, networkLoadCallbacks);
    }

    @Override
    public MeshNetwork getMeshNetwork() {
        return mMeshNetwork;
    }

    private void initBouncyCastle() {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private void initDb(final Context context) {
        mMeshNetworkDb = MeshNetworkDb.getDatabase(context);
        mMeshNetworkDao = mMeshNetworkDb.meshNetworkDao();
        mNetworkKeyDao = mMeshNetworkDb.networkKeyDao();
        mNetworkKeysDao = mMeshNetworkDb.networkKeysDao();
        mApplicationKeyDao = mMeshNetworkDb.applicationKeyDao();
        mApplicationKeysDao = mMeshNetworkDb.applicationKeysDao();
        mProvisionerDao = mMeshNetworkDb.provisionerDao();
        mProvisionersDao = mMeshNetworkDb.provisionersDao();
        mProvisionedNodeDao = mMeshNetworkDb.provisionedMeshNodeDao();
        mProvisionedNodesDao = mMeshNetworkDb.provisionedMeshNodesDao();
        mGroupDao = mMeshNetworkDb.groupDao();
        mGroupsDao = mMeshNetworkDb.groupsDao();
        mSceneDao = mMeshNetworkDb.sceneDao();
        mScenesDao = mMeshNetworkDb.scenesDao();
    }

    private void insertNetwork(final MeshNetwork meshNetwork) {
        meshNetwork.setLastSelected(true);
        //If there is only one provisioner we default to the zeroth
        if (meshNetwork.provisioners.size() == 1) {
            meshNetwork.provisioners.get(0).setLastSelected(true);
        }
        mMeshNetworkDb.insertNetwork(mMeshNetworkDao,
                mNetworkKeysDao,
                mApplicationKeysDao,
                mProvisionersDao,
                mProvisionedNodesDao,
                mGroupsDao, mScenesDao,
                meshNetwork);
    }

    @Override
    public final void handleNotifications(final int mtuSize, @NonNull final byte[] data) {
        byte[] unsegmentedPdu;
        if (!shouldWaitForMoreData(data)) {
            unsegmentedPdu = data;
        } else {
            final byte[] combinedPdu = appendPdu(mtuSize, data);
            if (combinedPdu == null) {
                //Start the timer
                toggleProxyProtocolSarTimeOut(data);
                return;
            } else {
                toggleProxyProtocolSarTimeOut(data);
                unsegmentedPdu = removeSegmentation(mtuSize, combinedPdu);
            }
        }
        parseNotifications(unsegmentedPdu);
    }

    /**
     * Toggles the Segmentation and Reassembly timeout for proxy configuration messages received via proxy protocol
     *
     * @param data pdu
     */
    private void toggleProxyProtocolSarTimeOut(final byte[] data) {
        final int pduType = MeshParserUtils.unsignedByteToInt(data[0]);
        if (pduType == ((GATT_SAR_START << SAR_BIT_OFFSET) | MeshManagerApi.PDU_TYPE_PROXY_CONFIGURATION)) {
            mHandler.postDelayed(mProxyProtocolTimeoutRunnable, PROXY_SAR_TRANSFER_TIME_OUT);
        } else if (pduType == ((GATT_SAR_END << SAR_BIT_OFFSET) | MeshManagerApi.PDU_TYPE_PROXY_CONFIGURATION)) {
            mHandler.removeCallbacks(mProxyProtocolTimeoutRunnable);
        }
    }

    /**
     * Parses notifications received by the client.
     *
     * @param unsegmentedPdu pdu received by the client.
     */
    private void parseNotifications(final byte[] unsegmentedPdu) {
        try {
            switch (unsegmentedPdu[0]) {
                case PDU_TYPE_NETWORK:
                    //MeshNetwork PDU
                    Log.v(TAG, "Received network pdu: " + MeshParserUtils.bytesToHex(unsegmentedPdu, true));
                    mMeshMessageHandler.parseMeshPduNotifications(unsegmentedPdu, mMeshNetwork);
                    break;
                case PDU_TYPE_MESH_BEACON:
                    //Mesh beacon
                    final NetworkKey networkKey = mMeshNetwork.getPrimaryNetworkKey();
                    if (networkKey != null) {
                        final byte[] n = networkKey.getKey();
                        final byte[] flags = {(byte) mMeshNetwork.getProvisioningFlags()};
                        final byte[] networkId = SecureUtils.calculateK3(n);
                        final byte[] ivIndex = ByteBuffer.allocate(4).putInt(mMeshNetwork.getIvIndex()).array();
                        final byte[] receivedBeaconData = new byte[unsegmentedPdu.length - 1];
                        System.arraycopy(unsegmentedPdu, 1, receivedBeaconData, 0, receivedBeaconData.length);
                        final SecureNetworkBeacon receivedBeacon = new SecureNetworkBeacon(receivedBeaconData);
                        final SecureNetworkBeacon localSecureNetworkBeacon = SecureUtils.createSecureNetworkBeacon(n, flags, networkId, ivIndex);
                        if (Arrays.equals(receivedBeacon.getAuthenticationValue(), localSecureNetworkBeacon.getAuthenticationValue())) {
                            mMeshNetwork.ivIndex = receivedBeacon.getIvIndex();
                            //TODO set iv update state
                            Log.v(TAG, "Generated mesh beacon: " +
                                    MeshParserUtils.bytesToHex(SecureUtils.calculateSecureNetworkBeacon(n, 1, flags, networkId, ivIndex), true));
                            Log.v(TAG, "Received mesh beacon: " + MeshParserUtils.bytesToHex(unsegmentedPdu, true));
                        }
                    }
                    break;
                case PDU_TYPE_PROXY_CONFIGURATION:
                    //Proxy configuration
                    Log.v(TAG, "Received proxy configuration message: " + MeshParserUtils.bytesToHex(unsegmentedPdu, true));
                    mMeshMessageHandler.parseMeshPduNotifications(unsegmentedPdu, mMeshNetwork);
                    break;
                case PDU_TYPE_PROVISIONING:
                    //Provisioning PDU
                    Log.v(TAG, "Received provisioning message: " + MeshParserUtils.bytesToHex(unsegmentedPdu, true));
                    mMeshProvisioningHandler.parseProvisioningNotifications(unsegmentedPdu);
                    break;
            }
        } catch (ExtendedInvalidCipherTextException ex) {
            //TODO handle decryption failure
        }
    }

    @Override
    public final void handleWriteCallbacks(final int mtuSize, @NonNull final byte[] data) {
        byte[] unsegmentedPdu;
        if (!shouldWaitForMoreData(data)) {
            unsegmentedPdu = data;
        } else {
            final byte[] combinedPdu = appendWritePdu(mtuSize, data);
            if (combinedPdu == null)
                return;
            else {
                unsegmentedPdu = removeSegmentation(mtuSize, combinedPdu);
            }
        }
        handleWriteCallbacks(unsegmentedPdu);
    }

    /**
     * Handles callbacks after writing to characteristics to maintain/update the state machine
     *
     * @param data written to the peripheral
     */
    private void handleWriteCallbacks(final byte[] data) {
        switch (data[0]) {
            case PDU_TYPE_NETWORK: // MeshNetwork PDU
                Log.v(TAG, "MeshNetwork pdu sent: " + MeshParserUtils.bytesToHex(data, true));
                break;
            case PDU_TYPE_MESH_BEACON: // MESH BEACON
                Log.v(TAG, "Mesh beacon pdu sent: " + MeshParserUtils.bytesToHex(data, true));
                break;
            case PDU_TYPE_PROXY_CONFIGURATION: // Proxy configuration
                Log.v(TAG, "Proxy configuration pdu sent: " + MeshParserUtils.bytesToHex(data, true));
                break;
            case PDU_TYPE_PROVISIONING: // Provisioning PDU
                Log.v(TAG, "Provisioning pdu sent: " + MeshParserUtils.bytesToHex(data, true));
                mMeshProvisioningHandler.handleProvisioningWriteCallbacks();
                break;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean shouldWaitForMoreData(final byte[] pdu) {
        final int gattSar = (pdu[0] & GATT_SAR_MASK) >> SAR_BIT_OFFSET;
        switch (gattSar) {
            case GATT_SAR_START:
            case GATT_SAR_CONTINUATION:
            case GATT_SAR_END:
                return true;
            default:
                return false;
        }
    }

    /**
     * Appends the PDUs that are segmented at gatt layer.
     *
     * @param mtuSize mtu size supported by the device/node
     * @param pdu     pdu received by the provisioner
     * @return the combine pdu or returns null if not complete.
     */
    private byte[] appendPdu(final int mtuSize, final byte[] pdu) {
        if (mIncomingBuffer == null) {
            final int length = Math.min(pdu.length, mtuSize);
            mIncomingBufferOffset = 0;
            mIncomingBufferOffset += length;
            mIncomingBuffer = pdu;
        } else {
            final int length = Math.min(pdu.length, mtuSize);
            final byte[] buffer = new byte[mIncomingBuffer.length + length];
            System.arraycopy(mIncomingBuffer, 0, buffer, 0, mIncomingBufferOffset);
            System.arraycopy(pdu, 0, buffer, mIncomingBufferOffset, length);
            mIncomingBufferOffset += length;
            mIncomingBuffer = buffer;
            if (length < mtuSize) {
                final byte[] packet = mIncomingBuffer;
                mIncomingBuffer = null;
                return packet;
            }
        }
        return null;
    }

    /**
     * Appends the PDUs that are segmented at gatt layer.
     *
     * @param mtuSize mtu size supported by the device/node
     * @param pdu     pdu received by the provisioner
     * @return the combine pdu or returns null if not complete.
     */
    private byte[] appendWritePdu(final int mtuSize, final byte[] pdu) {
        if (mOutgoingBuffer == null) {
            final int length = Math.min(pdu.length, mtuSize);
            mOutgoingBufferOffset = 0;
            mOutgoingBufferOffset += length;
            mOutgoingBuffer = pdu;
        } else {
            final int length = Math.min(pdu.length, mtuSize);
            final byte[] buffer = new byte[mOutgoingBuffer.length + length];
            System.arraycopy(mOutgoingBuffer, 0, buffer, 0, mOutgoingBufferOffset);
            System.arraycopy(pdu, 0, buffer, mOutgoingBufferOffset, length);
            mOutgoingBufferOffset += length;
            mOutgoingBuffer = buffer;
            if (length < mtuSize) {
                final byte[] packet = mOutgoingBuffer;
                mOutgoingBuffer = null;
                return packet;
            }
        }
        return null;
    }

    private byte[] applySegmentation(final int mtuSize, final byte[] pdu) {
        int srcOffset = 0;
        int dstOffset = 0;
        final int chunks = (pdu.length + (mtuSize - 1)) / mtuSize;

        final int pduType = pdu[0];
        if (chunks > 1) {
            final byte[] segmentedBuffer = new byte[pdu.length + chunks - 1];
            int length;
            for (int i = 0; i < chunks; i++) {
                if (i == 0) {
                    length = Math.min(pdu.length - srcOffset, mtuSize);
                    System.arraycopy(pdu, srcOffset, segmentedBuffer, dstOffset, length);
                    segmentedBuffer[0] = (byte) ((GATT_SAR_START << 6) | pduType);
                } else if (i == chunks - 1) {
                    length = Math.min(pdu.length - srcOffset, mtuSize);
                    segmentedBuffer[dstOffset] = (byte) ((GATT_SAR_END << 6) | pduType);
                    System.arraycopy(pdu, srcOffset, segmentedBuffer, dstOffset + 1, length);
                } else {
                    length = Math.min(pdu.length - srcOffset, mtuSize - 1);
                    segmentedBuffer[dstOffset] = (byte) ((GATT_SAR_CONTINUATION << 6) | pduType);
                    System.arraycopy(pdu, srcOffset, segmentedBuffer, dstOffset + 1, length);
                }
                srcOffset += length;
                dstOffset += mtuSize;
            }
            return segmentedBuffer;
        }
        return pdu;
    }

    private byte[] removeSegmentation(final int mtuSize, final byte[] data) {
        int srcOffset = 0;
        int dstOffset = 0;
        final int chunks = (data.length + (mtuSize - 1)) / mtuSize;
        if (chunks > 1) {
            final byte[] buffer = new byte[data.length - (chunks - 1)];
            int length;
            for (int i = 0; i < chunks; i++) {
                // when removing segmentation bits we only remove the start because the pdu type would be the same for each segment.
                // Therefore we can ignore this pdu type byte as they are already put together in the ble
                if (i == 0) {
                    length = Math.min(buffer.length - dstOffset, mtuSize);
                    System.arraycopy(data, srcOffset, buffer, dstOffset, length);
                    buffer[0] = (byte) (buffer[0] & GATT_SAR_UNMASK);
                } else if (i == chunks - 1) {
                    length = Math.min(buffer.length - dstOffset, mtuSize);
                    System.arraycopy(data, srcOffset + 1, buffer, dstOffset, length);
                } else {
                    length = Math.min(buffer.length - dstOffset, mtuSize) - 1;
                    System.arraycopy(data, srcOffset + 1, buffer, dstOffset, length);
                }
                srcOffset += mtuSize;
                dstOffset += length;
            }
            return buffer;
        }
        return data;
    }

    @Override
    public void identifyNode(@NonNull final UUID deviceUUID) throws IllegalArgumentException {
        identifyNode(deviceUUID, MeshProvisioningHandler.ATTENTION_TIMER);
    }

    @Override
    public void identifyNode(@NonNull final UUID deviceUuid,
                             final int attentionTimer) throws IllegalArgumentException {
        final NetworkKey networkKey = mMeshNetwork.getPrimaryNetworkKey();
        if (networkKey != null) {
            mMeshProvisioningHandler.identify(deviceUuid, networkKey, mMeshNetwork.getProvisioningFlags(),
                    mMeshNetwork.getIvIndex(), mMeshNetwork.getGlobalTtl(), attentionTimer);
        }
    }

    @Override
    public void startProvisioning(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException {
        if (isAddressValid(unprovisionedMeshNode)) {
            mMeshProvisioningHandler.startProvisioningNoOOB(unprovisionedMeshNode);
        }
    }

    @Override
    public void startProvisioningWithStaticOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException {
        if (isAddressValid(unprovisionedMeshNode)) {
            mMeshProvisioningHandler.startProvisioningWithStaticOOB(unprovisionedMeshNode);
        }
    }

    @Override
    public void startProvisioningWithOutputOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode,
                                               @NonNull final OutputOOBAction oobAction) throws IllegalArgumentException {
        if (isAddressValid(unprovisionedMeshNode)) {
            mMeshProvisioningHandler.startProvisioningWithOutputOOB(unprovisionedMeshNode, oobAction);
        }
    }

    @Override
    public void startProvisioningWithInputOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode,
                                              @NonNull final InputOOBAction oobAction) throws IllegalArgumentException {
        if (isAddressValid(unprovisionedMeshNode)) {
            mMeshProvisioningHandler.startProvisioningWithInputOOB(unprovisionedMeshNode, oobAction);
        }
    }

    @Override
    public void setProvisioningAuthentication(@NonNull final String authentication) {
        mMeshProvisioningHandler.sendProvisioningConfirmation(authentication);
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public UUID getDeviceUuid(@NonNull final byte[] serviceData) throws IllegalArgumentException {
        if (serviceData == null || serviceData.length < 18)
            throw new IllegalArgumentException("Service data cannot be null");

        final ByteBuffer buffer = ByteBuffer.wrap(serviceData);
        final long msb = buffer.getLong();
        final long lsb = buffer.getLong();

        return new UUID(msb, lsb);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean isMeshBeacon(@NonNull final byte[] advertisementData) throws IllegalArgumentException {
        if (advertisementData == null)
            throw new IllegalArgumentException("Advertisement data cannot be null");

        for (int i = 0; i < advertisementData.length; i++) {
            final int length = MeshParserUtils.unsignedByteToInt(advertisementData[i]);
            if (length == 0)
                break;
            final int type = MeshParserUtils.unsignedByteToInt(advertisementData[i + 1]);
            if (type == MeshBeacon.MESH_BEACON) {
                return true;
            }
            i = i + length;
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public byte[] getMeshBeaconData(@NonNull final byte[] advertisementData) throws IllegalArgumentException {
        if (advertisementData == null)
            throw new IllegalArgumentException("Advertisement data cannot be null");

        if (isMeshBeacon(advertisementData)) {
            for (int i = 0; i < advertisementData.length; i++) {
                final int length = MeshParserUtils.unsignedByteToInt(advertisementData[i]);
                final int type = MeshParserUtils.unsignedByteToInt(advertisementData[i + 1]);
                if (type == MeshBeacon.MESH_BEACON) {
                    final byte[] beaconData = new byte[length];
                    final ByteBuffer buffer = ByteBuffer.wrap(advertisementData);
                    buffer.position(i + 2);
                    buffer.get(beaconData, 0, length);
                    return beaconData;
                }
                i = i + length;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public MeshBeacon getMeshBeacon(final byte[] beaconData) {
        if (beaconData != null) {
            final int beaconType = beaconData[0];
            if (beaconType == 0x00) {
                return new UnprovisionedBeacon(beaconData);
            } else if (beaconType == 0x01) {
                return new SecureNetworkBeacon(beaconData);
            }
        }
        return null;
    }

    @Override
    public String generateNetworkId(@NonNull final byte[] networkKey) {
        return MeshParserUtils.bytesToHex(SecureUtils.calculateK3(networkKey), false);
    }

    @Override
    public boolean nodeIdentityMatches(@NonNull final ProvisionedMeshNode meshNode, @NonNull final byte[] serviceData) {
        final byte[] advertisedHash = getAdvertisedHash(serviceData);
        //If there is no advertised hash return false as this is used to match against the generated hash
        if (advertisedHash == null) {
            return false;
        }

        //If there is no advertised random return false as this is used to generate the hash to match against the advertised
        final byte[] random = getAdvertisedRandom(serviceData);
        if (random == null) {
            return false;
        }

        for (NetworkKey key : mMeshNetwork.netKeys) {
            //if generated hash is null return false
            final byte[] generatedHash = SecureUtils.
                    calculateHash(key.getIdentityKey(), random, MeshAddress.addressIntToBytes(meshNode.getUnicastAddress()));
            if (Arrays.equals(advertisedHash, generatedHash))
                return true;
        }
        return false;
    }


    @Override
    public boolean isAdvertisedWithNodeIdentity(@Nullable final byte[] serviceData) {
        return serviceData != null &&
                serviceData[ADVERTISED_HASH_OFFSET - 1] == ADVERTISEMENT_TYPE_NODE_IDENTITY;
    }

    /**
     * Returns the advertised hash
     *
     * @param serviceData advertised service data
     * @return returns the advertised hash
     */
    private byte[] getAdvertisedHash(final byte[] serviceData) {
        if (serviceData == null)
            return null;
        final ByteBuffer expectedBufferHash = ByteBuffer.allocate(ADVERTISED_HASH_LENGTH).order(ByteOrder.BIG_ENDIAN);
        expectedBufferHash.put(serviceData, ADVERTISED_HASH_OFFSET, ADVERTISED_HASH_LENGTH);
        return expectedBufferHash.array();
    }

    /**
     * Returns the advertised random
     *
     * @param serviceData advertised service data
     * @return returns the advertised random
     */
    private byte[] getAdvertisedRandom(final byte[] serviceData) {
        if (serviceData == null || serviceData.length <= ADVERTISED_RANDOM_LENGTH)
            return null;
        final ByteBuffer expectedBufferHash = ByteBuffer.allocate(ADVERTISED_RANDOM_LENGTH).order(ByteOrder.BIG_ENDIAN);
        expectedBufferHash.put(serviceData, ADVERTISED_RANDOM_OFFSET, ADVERTISED_RANDOM_LENGTH);
        return expectedBufferHash.array();
    }

    @Override
    public boolean networkIdMatches(@NonNull final String networkId, @Nullable final byte[] serviceData) {
        final byte[] advertisedNetworkId = getAdvertisedNetworkId(serviceData);
        if (advertisedNetworkId != null) {
            final String advertisedNetworkIdString = MeshParserUtils.bytesToHex(advertisedNetworkId, false).toUpperCase(Locale.US);
            return networkId.equals(advertisedNetworkIdString);
        }
        return false;
    }

    @Override
    public boolean isAdvertisingWithNetworkIdentity(@Nullable final byte[] serviceData) {
        return serviceData != null && serviceData[ADVERTISED_NETWORK_ID_OFFSET - 1] == ADVERTISEMENT_TYPE_NETWORK_ID;
    }

    /**
     * Returns the advertised network identity
     *
     * @param serviceData advertised service data
     * @return returns the advertised network identity
     */
    private byte[] getAdvertisedNetworkId(final byte[] serviceData) {
        if (serviceData == null)
            return null;
        final ByteBuffer advertisedNetworkID = ByteBuffer.allocate(ADVERTISED_NETWORK_ID_LENGTH).order(ByteOrder.BIG_ENDIAN);
        advertisedNetworkID.put(serviceData, ADVERTISED_NETWORK_ID_OFFSET, ADVERTISED_HASH_LENGTH);
        return advertisedNetworkID.array();
    }

    /**
     * Resets the provisioned mesh network and will generate a new one
     * <p>
     * This method will clear the provisioned nodes, reset the sequence number and generate new network with new provisioning data.
     * {@link MeshManagerCallbacks#onNetworkLoaded(MeshNetwork)} will return the newly generated network
     * </p>
     */
    public final void resetMeshNetwork() {
        //We delete the existing network as the user has already given the
        final MeshNetwork meshNet = mMeshNetwork;
        deleteMeshNetworkFromDb(meshNet);
        final MeshNetwork newMeshNetwork = generateMeshNetwork();
        newMeshNetwork.setCallbacks(callbacks);
        insertNetwork(newMeshNetwork);
        mMeshNetwork = newMeshNetwork;
        mMeshManagerCallbacks.onNetworkLoaded(newMeshNetwork);
    }

    private MeshNetwork generateMeshNetwork() {
        final String meshUuid = UUID.randomUUID().toString().toUpperCase(Locale.US);

        final MeshNetwork network = new MeshNetwork(meshUuid);
        network.netKeys = generateNetKeys(meshUuid);
        network.appKeys = generateAppKeys(meshUuid);
        final AllocatedUnicastRange unicastRange = new AllocatedUnicastRange(0x0001, 0x199A);
        final AllocatedGroupRange groupRange = new AllocatedGroupRange(0xC000, 0xCC9A);
        final AllocatedSceneRange sceneRange = new AllocatedSceneRange(0x0001, 0x3333);
        final Provisioner provisioner = network.createProvisioner("nRF Mesh Provisioner", unicastRange, groupRange, sceneRange);
        final int unicast = provisioner.getAllocatedUnicastRanges().get(0).getLowAddress();
        provisioner.assignProvisionerAddress(unicast);
        network.selectProvisioner(provisioner);
        network.addProvisioner(provisioner);
        final ProvisionedMeshNode node = network.getNode(unicast);
        if (node != null) {
            network.unicastAddress = node.getUnicastAddress() + (node.getNumberOfElements() - 1);
        } else {
            network.unicastAddress = 1;
        }
        network.lastSelected = true;
        network.sequenceNumbers.clear(); //Clear the sequence numbers first
        network.loadSequenceNumbers();
        return network;
    }

    private List<NetworkKey> generateNetKeys(final String meshUuid) {
        final List<NetworkKey> networkKeys = new ArrayList<>();
        final NetworkKey networkKey = new NetworkKey(0, SecureUtils.generateRandomNumber());
        networkKey.setMeshUuid(meshUuid);
        networkKeys.add(networkKey);
        return networkKeys;
    }

    private List<ApplicationKey> generateAppKeys(final String meshUuid) {
        final List<ApplicationKey> appKeys = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final ApplicationKey appKey = new ApplicationKey(i, SecureUtils.generateRandomNumber());
            appKey.setMeshUuid(meshUuid);
            appKeys.add(appKey);
        }
        return appKeys;
    }

    /**
     * Deletes an existing mesh network from the local database
     *
     * @param meshNetwork mesh network to be deleted
     */
    public final void deleteMeshNetworkFromDb(final MeshNetwork meshNetwork) {
        mMeshNetworkDb.deleteNetwork(mMeshNetworkDao, meshNetwork);
    }

    @Override
    public void createMeshPdu(final int dst, @NonNull final MeshMessage meshMessage) {
        if (!MeshAddress.isAddressInRange(dst)) {
            throw new IllegalArgumentException("Invalid address, destination address must be a valid 16-bit value.");
        }
        final Provisioner provisioner = mMeshNetwork.getSelectedProvisioner();
        if (provisioner != null && provisioner.getProvisionerAddress() != null) {
            UUID label = null;
            if (MeshAddress.isValidVirtualAddress(dst)) {
                label = mMeshNetwork.getLabelUuid(dst);
                if (label == null) {
                    throw new IllegalArgumentException("Label UUID unavailable for the virtual address provided");
                }
            }
            mMeshMessageHandler.createMeshMessage(provisioner.getProvisionerAddress(), dst, label, meshMessage);
        } else {
            throw new IllegalArgumentException("Provisioner address not set, please assign an address to the provisioner.");
        }
    }

    @Override
    public String exportMeshNetwork() {
        final MeshNetwork meshNetwork = mMeshNetwork;
        if (meshNetwork != null) {
            return NetworkImportExportUtils.export(meshNetwork);
        }
        return null;
    }

    @Override
    public void importMeshNetwork(@NonNull final Uri uri) {
        if (uri.getPath() != null) {
            NetworkImportExportUtils.importMeshNetwork(mContext, uri, networkLoadCallbacks);
        } else {
            mMeshManagerCallbacks.onNetworkImportFailed("URI getPath() returned null!");
        }
    }

    @Override
    public void importMeshNetworkJson(@NonNull String networkJson) {
        NetworkImportExportUtils.importMeshNetworkFromJson(mContext, networkJson, networkLoadCallbacks);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final InternalTransportCallbacks internalTransportCallbacks = new InternalTransportCallbacks() {

        @Override
        public List<ApplicationKey> getApplicationKeys(final int boundNetKeyIndex) {
            return mMeshNetwork.getAppKeys(boundNetKeyIndex);
        }

        @Override
        public ProvisionedMeshNode getNode(final int unicast) {
            return mMeshNetwork.getNode(unicast);
        }

        @Override
        public Provisioner getProvisioner(final int unicast) {
            return null;
        }

        @Override
        public void sendProvisioningPdu(final UnprovisionedMeshNode meshNode, final byte[] pdu) {
            final int mtu = mMeshManagerCallbacks.getMtu();
            mMeshManagerCallbacks.sendProvisioningPdu(meshNode, applySegmentation(mtu, pdu));
        }

        @Override
        public void onMeshPduCreated(final int dst, final byte[] pdu) {
            //We must save the mesh network state for every message that is being sent out.
            //This will specifically save the sequence number for every message sent.
            final ProvisionedMeshNode meshNode = mMeshNetwork.getNode(dst);
            updateNetwork(meshNode);
            final int mtu = mMeshManagerCallbacks.getMtu();
            mMeshManagerCallbacks.onMeshPduCreated(applySegmentation(mtu, pdu));
        }

        @Override
        public ProxyFilter getProxyFilter() {
            return mMeshNetwork.getProxyFilter();
        }

        @Override
        public void setProxyFilter(@NonNull final ProxyFilter filter) {
            mMeshNetwork.setProxyFilter(filter);
        }

        @Override
        public void updateMeshNetwork(final MeshMessage message) {
            final ProvisionedMeshNode meshNode = mMeshNetwork.getNode(message.getSrc());
            updateNetwork(meshNode);
        }

        @Override
        public void onMeshNodeReset(final ProvisionedMeshNode meshNode) {
            if (meshNode != null) {
                if (mMeshNetwork.deleteResetNode(meshNode)) {
                    mMeshNetwork.sequenceNumbers.delete(meshNode.getUnicastAddress());
                    mMeshMessageHandler.resetState(meshNode.getUnicastAddress());
                    mMeshNetworkDb.deleteNode(mProvisionedNodeDao, meshNode);
                    mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
                }
            }
        }

        @Override
        public MeshNetwork getMeshNetwork() {
            return mMeshNetwork;
        }

        private void updateNetwork(final ProvisionedMeshNode meshNode) {
            if (meshNode != null) {
                for (int i = 0; i < mMeshNetwork.nodes.size(); i++) {
                    if (meshNode.getUnicastAddress() == mMeshNetwork.nodes.get(i).getUnicastAddress()) {
                        mMeshNetwork.nodes.set(i, meshNode);
                        break;
                    }
                }
            }
            mMeshNetwork.setTimestamp(System.currentTimeMillis());
            mMeshNetworkDb.updateNetwork1(mMeshNetwork, mMeshNetworkDao, mNetworkKeysDao, mApplicationKeysDao, mProvisionersDao, mProvisionedNodesDao,
                    mGroupsDao, mScenesDao);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final InternalMeshManagerCallbacks internalMeshMgrCallbacks = new InternalMeshManagerCallbacks() {
        @Override
        public void onNodeProvisioned(final ProvisionedMeshNode meshNode) {
            updateProvisionedNodeList(meshNode);
            mMeshNetwork.sequenceNumbers.put(meshNode.getUnicastAddress(), meshNode.getSequenceNumber());
            mMeshNetwork.unicastAddress = mMeshNetwork.nextAvailableUnicastAddress(meshNode.getNumberOfElements(), mMeshNetwork.getSelectedProvisioner());
            //Set the mesh network uuid to the node so we can identify nodes belonging to a network
            meshNode.setMeshUuid(mMeshNetwork.getMeshUUID());
            mMeshNetworkDb.insertNode(mProvisionedNodeDao, meshNode);
            mMeshNetworkDb.updateProvisioner(mProvisionerDao,
                    mMeshNetwork.getSelectedProvisioner());
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        private void updateProvisionedNodeList(final ProvisionedMeshNode meshNode) {
            for (int i = 0; i < mMeshNetwork.nodes.size(); i++) {
                final ProvisionedMeshNode node = mMeshNetwork.nodes.get(i);
                if (meshNode.getUuid().equals(node.getUuid())) {
                    mMeshNetwork.nodes.remove(i);
                    break;
                }
            }
            mMeshNetwork.nodes.add(meshNode);
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final NetworkLayerCallbacks networkLayerCallbacks = new NetworkLayerCallbacks() {
        @Override
        public ProvisionedMeshNode getNode(final int unicastAddress) {
            return mMeshNetwork.getNode(unicastAddress);
        }

        @Override
        public Provisioner getProvisioner() {
            return mMeshNetwork.getSelectedProvisioner();
        }

        @Override
        public Provisioner getProvisioner(final int unicastAddress) {
            for (Provisioner provisioner : mMeshNetwork.getProvisioners()) {
                if (provisioner.isLastSelected())
                    return provisioner;
            }
            return null;
        }

        @Override
        public NetworkKey getPrimaryNetworkKey() {
            return mMeshNetwork.getPrimaryNetworkKey();
        }

        @Override
        public NetworkKey getNetworkKey(final int keyIndex) {
            return mMeshNetwork.getNetKey(keyIndex);
        }

        @Override
        public List<NetworkKey> getNetworkKeys() {
            return mMeshNetwork.getNetKeys();
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final UpperTransportLayerCallbacks upperTransportLayerCallbacks = new UpperTransportLayerCallbacks() {


        @Override
        public byte[] getIvIndex() {
            return ByteBuffer.allocate(4).putInt(mMeshNetwork.getIvIndex()).array();
        }

        @Override
        public byte[] getApplicationKey(final int aid) {
            for (ApplicationKey key : mMeshNetwork.getAppKeys()) {
                final byte[] k = key.getKey();
                if (aid == SecureUtils.calculateK4(k)) {
                    return key.getKey();
                }
            }
            return null;
        }

        @Nullable
        @Override
        public UUID getLabel(final int address) {
            return mMeshNetwork.getLabelUuid(address);
        }
    };

    /**
     * Callbacks to notify when the database has been loaded
     */
    private final LoadNetworkCallbacks networkLoadCallbacks = new LoadNetworkCallbacks() {
        @Override
        public void onNetworkLoadedFromDb(final MeshNetwork meshNetwork) {
            final MeshNetwork network;
            //If there is no network we generate a new one
            if (meshNetwork == null) {
                network = generateMeshNetwork();
                insertNetwork(network);
            } else {
                network = meshNetwork;
                network.loadSequenceNumbers();
            }
            network.setCallbacks(callbacks);
            mMeshNetwork = network;
            mMeshManagerCallbacks.onNetworkLoaded(network);
        }

        @Override
        public void onNetworkLoadFailed(final String error) {
            mMeshManagerCallbacks.onNetworkLoadFailed(error);
        }

        @Override
        public void onNetworkImportedFromJson(final MeshNetwork meshNetwork) {
            meshNetwork.setCallbacks(callbacks);
            insertNetwork(meshNetwork);
            mMeshNetwork = meshNetwork;
            mMeshManagerCallbacks.onNetworkImported(meshNetwork);
        }

        @Override
        public void onNetworkImportFailed(final String error) {
            mMeshManagerCallbacks.onNetworkImportFailed(error);
        }
    };

    /**
     * Callbacks observing user updates on the mesh network object
     */
    private final MeshNetworkCallbacks callbacks = new MeshNetworkCallbacks() {
        @Override
        public void onMeshNetworkUpdated() {
            mMeshNetwork.setTimestamp(System.currentTimeMillis());
            mMeshNetworkDb.updateNetwork(mMeshNetworkDao, mMeshNetwork);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onNetworkKeyAdded(@NonNull final NetworkKey networkKey) {
            mMeshNetworkDb.insertNetKey(mNetworkKeyDao, networkKey);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onNetworkKeyUpdated(@NonNull final NetworkKey networkKey) {
            mMeshNetworkDb.updateNetKey(mNetworkKeyDao, networkKey);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onNetworkKeyDeleted(@NonNull final NetworkKey networkKey) {
            mMeshNetworkDb.deleteNetKey(mNetworkKeyDao, networkKey);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onApplicationKeyAdded(@NonNull final ApplicationKey applicationKey) {
            mMeshNetworkDb.insertAppKey(mApplicationKeyDao, applicationKey);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onApplicationKeyUpdated(@NonNull final ApplicationKey applicationKey) {
            mMeshNetworkDb.updateAppKey(mApplicationKeyDao, applicationKey);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onApplicationKeyDeleted(@NonNull final ApplicationKey applicationKey) {
            mMeshNetworkDb.deleteAppKey(mApplicationKeyDao, applicationKey);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onProvisionerAdded(@NonNull final Provisioner provisioner) {
            mMeshNetworkDb.insertProvisioner(mProvisionerDao, provisioner);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onProvisionerUpdated(@NonNull final Provisioner provisioner) {
            mMeshNetworkDb.updateProvisioner(mProvisionerDao, provisioner);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onProvisionersUpdated(@NonNull final List<Provisioner> provisioners) {
            mMeshNetworkDb.updateProvisioners(mProvisionerDao, provisioners);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onProvisionerDeleted(@NonNull Provisioner provisioner) {
            mMeshNetworkDb.deleteProvisioner(mProvisionerDao, provisioner);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onNodeDeleted(@NonNull final ProvisionedMeshNode meshNode) {
            mMeshNetworkDb.deleteNode(mProvisionedNodeDao, meshNode);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onNodeAdded(@NonNull final ProvisionedMeshNode meshNode) {
            mMeshNetworkDb.insertNode(mProvisionedNodeDao, meshNode);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onNodeUpdated(@NonNull final ProvisionedMeshNode meshNode) {
            mMeshNetworkDb.updateNode(mProvisionedNodeDao, meshNode);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onNodesUpdated() {
            mMeshNetworkDb.updateNodes(mProvisionedNodesDao, mMeshNetwork.nodes);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onGroupAdded(@NonNull final Group group) {
            mMeshNetworkDb.insertGroup(mGroupDao, group);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onGroupUpdated(@NonNull final Group group) {
            mMeshNetworkDb.updateGroup(mGroupDao, group);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onGroupDeleted(@NonNull final Group group) {
            mMeshNetworkDb.deleteGroup(mGroupDao, group);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onSceneAdded(@NonNull final Scene scene) {
            mMeshNetworkDb.insertScene(mSceneDao, scene);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onSceneUpdated(@NonNull final Scene scene) {
            mMeshNetworkDb.updateScene(mSceneDao, scene);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onSceneDeleted(@NonNull final Scene scene) {
            mMeshNetworkDb.deleteScene(mSceneDao, scene);
            mMeshManagerCallbacks.onNetworkUpdated(mMeshNetwork);
        }
    };

    private boolean isAddressValid(@NonNull final UnprovisionedMeshNode node) {
        final int unicast = mMeshNetwork.nextAvailableUnicastAddress(node.getNumberOfElements(), mMeshNetwork.getSelectedProvisioner());
        if (!MeshAddress.isValidUnicastAddress(unicast)) {
            throw new IllegalArgumentException("Invalid address");
        }
        if (!mMeshNetwork.getSelectedProvisioner().isAddressWithinAllocatedRange(mMeshNetwork.getUnicastAddress())) {
            throw new IllegalArgumentException("Address assigned to node is outside of provisioner's allocated unicast range.");
        }
        node.setUnicastAddress(mMeshNetwork.getUnicastAddress());
        return true;
    }
}