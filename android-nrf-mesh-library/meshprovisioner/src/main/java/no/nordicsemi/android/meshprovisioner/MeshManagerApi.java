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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import no.nordicsemi.android.meshprovisioner.data.ApplicationKeyDao;
import no.nordicsemi.android.meshprovisioner.data.GroupDao;
import no.nordicsemi.android.meshprovisioner.data.GroupsDao;
import no.nordicsemi.android.meshprovisioner.data.MeshNetworkDao;
import no.nordicsemi.android.meshprovisioner.data.NetworkKeyDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionedMeshNodeDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionerDao;
import no.nordicsemi.android.meshprovisioner.data.SceneDao;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.InternalMeshModelDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkLayerCallbacks;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.UpperTransportLayerCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.InputOOBAction;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.OutputOOBAction;
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

    //According to the spec the proxy protocol must contain an SAR timeout of 20 seconds.
    private static final long PROXY_SAR_TRANSFER_TIME_OUT = 20 * 1000;
    /**
     * Length of the random number required to calculate the hash containing the node id
     */
    private final static int HASH_RANDOM_NUMBER_LENGTH = 64; //in bits
    private static final int ADVERTISEMENT_TYPE_NETWORK_ID = 0x00;
    private static final int ADVERTISEMENT_TYPE_NODE_IDENTITY = 0x01;
    /**
     * Offset of the hash contained in the advertisement service data
     */
    private final static int ADVERTISED_HASH_OFFSET = 1;
    /**
     * Length of the hash contained in the advertisement service data
     */
    private final static int ADVERTISED_HASH_LENGTH = 8;
    /**
     * Offset of the hash contained in the advertisement service data
     */
    private final static int ADVERTISED_RANDOM_OFFSET = 9;
    /**
     * Length of the hash contained in the advertisement service data
     */
    private final static int ADVERTISED_RANDOM_LENGTH = 8;
    /**
     * Offset of the network id contained in the advertisement service data
     */
    private final static int ADVERTISED_NETWORK_ID_OFFSET = 1;
    /**
     * Length of the network id contained in the advertisement service data
     */
    private final static int ADVERTISED_NETWORK_ID_LENGTH = 8;
    private Context mContext;
    private final Handler mHanlder;
    private MeshManagerCallbacks mTransportCallbacks;
    private MeshProvisioningHandler mMeshProvisioningHandler;
    private MeshMessageHandler mMeshMessageHandler;
    private byte[] mIncomingBuffer;
    private int mIncomingBufferOffset;
    private byte[] mOutgoingBuffer;
    private int mOutgoingBufferOffset;
    private MeshNetwork mMeshNetwork;
    private Gson mGson;

    private MeshNetworkDb mMeshNetworkDb;
    private MeshNetworkDao mMeshNetworkDao;
    private NetworkKeyDao mNetworkKeyDao;
    private ApplicationKeyDao mApplicationKeyDao;
    private ProvisionerDao mProvisionerDao;
    private ProvisionedMeshNodeDao mProvisionedNodeDao;
    private GroupsDao mGroupsDao;
    private GroupDao mGroupDao;
    private SceneDao mSceneDao;

    private final Runnable mProxyProtocolTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            mMeshMessageHandler.onIncompleteTimerExpired(true);
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
        mHanlder = new Handler();
        mMeshProvisioningHandler = new MeshProvisioningHandler(context, internalTransportCallbacks, internalMeshMgrCallbacks);
        mMeshMessageHandler = new MeshMessageHandler(context, internalTransportCallbacks);
        mMeshMessageHandler.getMeshTransport().setNetworkLayerCallbacks(networkLayerCallbacks);
        mMeshMessageHandler.getMeshTransport().setUpperTransportLayerCallbacks(upperTransportLayerCallbacks);

        initBouncyCastle();
        //Init database
        initDb(context);
        initGson();
        migrateMeshNetwork(context);

    }

    /**
     * Sets the {@link MeshManagerCallbacks} listener
     *
     * @param callbacks callbacks
     */
    public void setMeshManagerCallbacks(final MeshManagerCallbacks callbacks) {
        mTransportCallbacks = callbacks;
    }

    /**
     * Sets the {@link MeshProvisioningStatusCallbacks} listener to return provisioning status callbacks.
     *
     * @param callbacks callbacks
     */
    public void setProvisioningStatusCallbacks(final MeshProvisioningStatusCallbacks callbacks) {
        mMeshProvisioningHandler.setProvisioningCallbacks(callbacks);
    }

    /**
     * Sets the {@link MeshManagerCallbacks} listener to return mesh status callbacks.
     *
     * @param callbacks callbacks
     */
    public void setMeshStatusCallbacks(final MeshStatusCallbacks callbacks) {
        mMeshMessageHandler.setMeshStatusCallbacks(callbacks);
    }

    /**
     * Loads the mesh network from the local database.
     * <p>
     * This will start an AsyncTask that will load the network from the database.
     * {@link MeshManagerCallbacks#onNetworkLoaded(MeshNetwork) will return the mesh network
     * </p>
     */
    public void loadMeshNetwork() {
        mMeshNetworkDb.loadNetwork(mMeshNetworkDao,
                mNetworkKeyDao,
                mApplicationKeyDao,
                mProvisionerDao,
                mProvisionedNodeDao,
                mGroupsDao, mSceneDao,
                networkLoadCallbacks);
    }

    /**
     * Returns an already loaded mesh network, make sure to call {@link #loadMeshNetwork()} before calling this
     *
     * @return {@link MeshNetwork}
     */
    public MeshNetwork getMeshNetwork() {
        return mMeshNetwork;
    }

    private void initBouncyCastle(){
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private void initDb(final Context context) {
        mMeshNetworkDb = MeshNetworkDb.getDatabase(context);
        mMeshNetworkDao = mMeshNetworkDb.meshNetworkDao();
        mNetworkKeyDao = mMeshNetworkDb.networkKeyDao();
        mApplicationKeyDao = mMeshNetworkDb.applicationKeyDao();
        mProvisionerDao = mMeshNetworkDb.provisionerDao();
        mProvisionedNodeDao = mMeshNetworkDb.provisionedMeshNodeDao();
        mGroupsDao = mMeshNetworkDb.groupsDao();
        mGroupDao = mMeshNetworkDb.groupDao();
        mSceneDao = mMeshNetworkDb.sceneDao();
    }

    private void initGson() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        gsonBuilder.enableComplexMapKeySerialization();
        gsonBuilder.registerTypeAdapter(MeshModel.class, new InternalMeshModelDeserializer());
        gsonBuilder.setPrettyPrinting();
        mGson = gsonBuilder.create();
    }

    /**
     * Migrates the old network data and loads a new mesh network object
     *
     * @param context context
     */
    private void migrateMeshNetwork(final Context context) {
        final MeshNetwork meshNetwork = DataMigrator.migrateData(context, mGson);
        if (meshNetwork != null) {
            this.mMeshNetwork = meshNetwork;
            meshNetwork.setCallbacks(callbacks);
            insertNetwork(meshNetwork);
        }
    }

    private void insertNetwork(final MeshNetwork meshNetwork) {
        meshNetwork.setLastSelected(true);
        //If there is only one provisioner we default to the zeroth
        if (meshNetwork.provisioners.size() == 1) {
            meshNetwork.provisioners.get(0).setLastSelected(true);
        }
        mMeshNetworkDb.insertNetwork(mMeshNetworkDao,
                mNetworkKeyDao,
                mApplicationKeyDao,
                mProvisionerDao,
                mProvisionedNodeDao,
                mGroupDao, mSceneDao,
                meshNetwork);
    }

    /**
     * Increments the unicast address by 1
     *
     * @param currentAddress current unicast address
     * @param elementCount   number of elements
     */
    private void incrementUnicastAddress(final int currentAddress, final int elementCount) {
        //Since we know the number of elements this node contains we can predict the next available address for the next node.
        final int unicastAdd = currentAddress + elementCount;
        //We check if the incremented unicast address is already taken by the app/configurator
        final int tempSrc = mMeshNetwork.getProvisionerAddress();

        if (unicastAdd == tempSrc) {
            mMeshNetwork.assignUnicastAddress(unicastAdd + 1);
        } else {
            mMeshNetwork.assignUnicastAddress(unicastAdd);
        }
    }

    /**
     * Handles notifications received by the client.
     * <p>
     * This method will check if the library should wait for more data in case of a gatt layer segmentation.
     * If its required the method will remove the segmentation bytes and combine the data together.
     * </p>
     *
     * @param data pdu received by the client
     */
    public final void handleNotifications(final int mtuSize, final byte[] data) {
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
            mHanlder.postDelayed(mProxyProtocolTimeoutRunnable, PROXY_SAR_TRANSFER_TIME_OUT);
        } else if (pduType == ((GATT_SAR_END << SAR_BIT_OFFSET) | MeshManagerApi.PDU_TYPE_PROXY_CONFIGURATION)) {
            mHanlder.removeCallbacks(mProxyProtocolTimeoutRunnable);
        }
    }

    /**
     * Parses notifications received by the client.
     *
     * @param unsegmentedPdu pdu received by the client.
     */
    private void parseNotifications(final byte[] unsegmentedPdu) {
        switch (unsegmentedPdu[0]) {
            case PDU_TYPE_NETWORK:
                //MeshNetwork PDU
                Log.v(TAG, "Received network pdu: " + MeshParserUtils.bytesToHex(unsegmentedPdu, true));
                mMeshMessageHandler.parseMeshMsgNotifications(unsegmentedPdu);
                break;
            case PDU_TYPE_MESH_BEACON:
                //Mesh beacon
                final byte[] n = mMeshNetwork.getPrimaryNetworkKey().getKey();
                final byte[] flags = {(byte) mMeshNetwork.getProvisioningFlags()};
                final byte[] networkId = SecureUtils.calculateK3(n);
                final byte[] ivIndex = ByteBuffer.allocate(4).putInt(mMeshNetwork.getIvIndex()).array();
                Log.v(TAG, "Generated mesh beacon: " +
                        MeshParserUtils.bytesToHex(SecureUtils.calculateSecureNetworkBeacon(n, 1, flags, networkId, ivIndex), true));
                Log.v(TAG, "Received mesh beacon: " + MeshParserUtils.bytesToHex(unsegmentedPdu, true));
                break;
            case PDU_TYPE_PROXY_CONFIGURATION:
                //Proxy configuration
                Log.v(TAG, "Received proxy configuration message: " + MeshParserUtils.bytesToHex(unsegmentedPdu, true));
                mMeshMessageHandler.parseMeshMsgNotifications(unsegmentedPdu);
                break;
            case PDU_TYPE_PROVISIONING:
                //Provisioning PDU
                Log.v(TAG, "Received provisioning message: " + MeshParserUtils.bytesToHex(unsegmentedPdu, true));
                mMeshProvisioningHandler.parseProvisioningNotifications(unsegmentedPdu);
                break;
        }
    }

    public final void handleWriteCallbacks(final int mtuSize, final byte[] data) {
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
            case PDU_TYPE_NETWORK:
                //MeshNetwork PDU
                Log.v(TAG, "MeshNetwork pdu sent: " + MeshParserUtils.bytesToHex(data, true));
                mMeshMessageHandler.handleMeshMsgWriteCallbacks(data);
                break;
            case PDU_TYPE_MESH_BEACON:
                //Mesh beacon
                Log.v(TAG, "Mesh beacon pdu sent: " + MeshParserUtils.bytesToHex(data, true));
                break;
            case PDU_TYPE_PROXY_CONFIGURATION:
                //Proxy configuration
                Log.v(TAG, "Proxy configuration pdu sent: " + MeshParserUtils.bytesToHex(data, true));
                mMeshMessageHandler.handleMeshMsgWriteCallbacks(data);
                break;
            case PDU_TYPE_PROVISIONING:
                //Provisioning PDU
                Log.v(TAG, "Provisioning pdu sent: " + MeshParserUtils.bytesToHex(data, true));
                mMeshProvisioningHandler.handleProvisioningWriteCallbacks();
                break;
        }
    }

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
                final byte packet[] = mIncomingBuffer;
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
                final byte packet[] = mOutgoingBuffer;
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
    public void identifyNode(@NonNull final UUID deviceUUID, @Nullable final String nodeName) throws IllegalArgumentException {
        mMeshProvisioningHandler.identify(deviceUUID, nodeName,
                mMeshNetwork.getPrimaryNetworkKey(),
                mMeshNetwork.getProvisioningFlags(),
                mMeshNetwork.getIvIndex(),
                mMeshNetwork.getUnicastAddress(),
                mMeshNetwork.getGlobalTtl(), mMeshNetwork.getProvisionerAddress(), MeshProvisioningHandler.ATTENTION_TIMER);
    }

    @Override
    public void identifyNode(@NonNull final UUID deviceUuid,
                             final String nodeName,
                             final int attentionTimer) throws IllegalArgumentException {
        mMeshProvisioningHandler.identify(deviceUuid, nodeName,
                mMeshNetwork.getPrimaryNetworkKey(),
                mMeshNetwork.getProvisioningFlags(),
                mMeshNetwork.getIvIndex(),
                mMeshNetwork.getUnicastAddress(),
                mMeshNetwork.getGlobalTtl(), mMeshNetwork.getProvisionerAddress(), attentionTimer);
    }

    @Override
    public void startProvisioning(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException {
        mMeshProvisioningHandler.startProvisioningNoOOB(unprovisionedMeshNode);
    }

    @Override
    public void startProvisioningWithStaticOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException {
        mMeshProvisioningHandler.startProvisioningWithStaticOOB(unprovisionedMeshNode);
    }

    @Override
    public void startProvisioningWithOutputOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode, @NonNull final OutputOOBAction oobAction) throws IllegalArgumentException {
        mMeshProvisioningHandler.startProvisioningWithOutputOOB(unprovisionedMeshNode, oobAction);
    }

    @Override
    public void startProvisioningWithInputOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode, @NonNull final InputOOBAction oobAction) throws IllegalArgumentException {
        mMeshProvisioningHandler.startProvisioningWithInputOOB(unprovisionedMeshNode, oobAction);
    }

    @Override
    public final void setProvisioningConfirmation(@NonNull final String authentication) {
        setProvisioningAuthentication(authentication);
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

        //if generated hash is null return false
        final byte[] generatedHash = SecureUtils.calculateHash(meshNode.getIdentityKey(), random, AddressUtils.getUnicastAddressBytes(meshNode.getUnicastAddress()));

        return Arrays.equals(advertisedHash, generatedHash);
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
            final String advertisedNetworkIdString = MeshParserUtils.bytesToHex(advertisedNetworkId, false).toUpperCase();
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
        final ByteBuffer advertisedNetowrkID = ByteBuffer.allocate(ADVERTISED_NETWORK_ID_LENGTH).order(ByteOrder.BIG_ENDIAN);
        advertisedNetowrkID.put(serviceData, ADVERTISED_NETWORK_ID_OFFSET, ADVERTISED_HASH_LENGTH);
        return advertisedNetowrkID.array();
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
        mTransportCallbacks.onNetworkLoaded(newMeshNetwork);
    }

    private MeshNetwork generateMeshNetwork() {
        final String meshUuid = UUID.randomUUID().toString().toUpperCase(Locale.US);

        final MeshNetwork network = new MeshNetwork(meshUuid);
        network.netKeys = generateNetKeys(meshUuid);
        network.appKeys = generateAppKeys(meshUuid);
        network.provisioners = generateProvisioners(meshUuid);
        network.lastSelected = true;

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

    private List<Provisioner> generateProvisioners(final String meshUuid) {
        final String provisionerUuid = UUID.randomUUID().toString().toUpperCase(Locale.US);
        final AllocatedUnicastRange unicastRange = new AllocatedUnicastRange(0x0001, 0x7FFF);
        final List<AllocatedUnicastRange> ranges = new ArrayList<>();
        ranges.add(unicastRange);
        final Provisioner provisioner = new Provisioner(provisionerUuid, ranges, null, null, meshUuid);
        provisioner.setLastSelected(true);
        final List<Provisioner> provisioners = new ArrayList<>();
        provisioners.add(provisioner);
        return provisioners;
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
    public final void sendMeshMessage(@NonNull final byte[] dst, @NonNull final MeshMessage meshMessage) {
        mMeshMessageHandler.sendMeshMessage(mMeshNetwork.getSelectedProvisioner().getProvisionerAddress(), AddressUtils.getUnicastAddressInt(dst), meshMessage);
    }

    @Override
    public void sendMeshMessage(final int dst, @NonNull final MeshMessage meshMessage) {
        if (!MeshAddress.isAddressInRange(dst)) {
            throw new IllegalArgumentException("Invalid address, destination address must be a valid 16-bit value!");
        }
        mMeshMessageHandler.sendMeshMessage(mMeshNetwork.getSelectedProvisioner().getProvisionerAddress(), dst, meshMessage);
    }

    @Override
    public void exportMeshNetwork(@NonNull final String path) {
        final MeshNetwork meshNetwork = mMeshNetwork;
        if (meshNetwork != null) {
            NetworkImportExportUtils.exportMeshNetwork(meshNetwork, path, networkLoadCallbacks);
        }
    }

    @Override
    public void importMeshNetwork(@NonNull final Uri uri) {
        if (uri.getPath() != null) {
            if (uri.getPath().endsWith(".json")) {
                NetworkImportExportUtils.importMeshNetwork(mContext, uri, networkLoadCallbacks);
            } else {
                mTransportCallbacks.onNetworkImportFailed("Invalid file type detected! " +
                        "Network information can be imported only from a valid JSON file that follows the Mesh Provisioning/Configuration Database format!");
            }
        } else {
            mTransportCallbacks.onNetworkImportFailed("URI getPath() returned null!");
        }
    }

    @Override
    public void importMeshNetworkJson(@NonNull String networkJson) {
        NetworkImportExportUtils.importMeshNetworkFromJson(mContext, networkJson, networkLoadCallbacks);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final InternalTransportCallbacks internalTransportCallbacks = new InternalTransportCallbacks() {
        @Override
        public ProvisionedMeshNode getProvisionedNode(final byte[] unicast) {
            return getMeshNode(AddressUtils.getUnicastAddressInt(unicast));
        }

        @Override
        public ProvisionedMeshNode getProvisionedNode(final int unicast) {
            return getMeshNode(unicast);
        }

        @Override
        public void sendProvisioningPdu(final UnprovisionedMeshNode meshNode, final byte[] pdu) {
            final int mtu = mTransportCallbacks.getMtu();
            mTransportCallbacks.sendProvisioningPdu(meshNode, applySegmentation(mtu, pdu));
        }

        @Override
        public void sendMeshPdu(final byte[] dst, final byte[] pdu) {
            //We must save the mesh network state for every message that is being sent out.
            //This will specifically save the sequence number for every message sent.
            final int dstAddress = AddressUtils.getUnicastAddressInt(dst);
            final ProvisionedMeshNode meshNode = mMeshNetwork.getProvisionedNode(dstAddress);
            updateNetwork(meshNode);
            final int mtu = mTransportCallbacks.getMtu();
            mTransportCallbacks.sendMeshPdu(applySegmentation(mtu, pdu));
        }

        @Override
        public void sendMeshPdu(final int dst, final byte[] pdu) {
            final ProvisionedMeshNode meshNode = mMeshNetwork.getProvisionedNode(dst);
            updateNetwork(meshNode);
            final int mtu = mTransportCallbacks.getMtu();
            mTransportCallbacks.sendMeshPdu(applySegmentation(mtu, pdu));
        }

        @Override
        public void updateMeshNetwork(final MeshMessage message) {
            final ProvisionedMeshNode meshNode = mMeshNetwork.getProvisionedNode(message.getSrc());
            updateNetwork(meshNode);
        }

        @Override
        public void onMeshNodeReset(final ProvisionedMeshNode meshNode) {
            if (meshNode != null) {
                if (mMeshNetwork.deleteResetNode(meshNode)) {
                    mMeshNetworkDb.deleteNode(mProvisionedNodeDao, meshNode);
                    mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
                }
            }
        }

        private void updateNetwork(final ProvisionedMeshNode meshNode) {
            if (meshNode != null) {
                for (int i = 0; i < mMeshNetwork.nodes.size(); i++) {
                    if (meshNode.getUnicastAddress() == mMeshNetwork.nodes.get(i).getUnicastAddress()) {
                        mMeshNetwork.nodes.set(i, meshNode);
                        mMeshNetworkDb.updateNode(mProvisionedNodeDao, meshNode);
                        break;
                    }
                }
            }
            mMeshNetworkDb.updateProvisioner(mProvisionerDao, mMeshNetwork.getSelectedProvisioner());
            mMeshNetwork.setTimestamp(MeshParserUtils.getInternationalAtomicTime(System.currentTimeMillis()));
            mMeshNetworkDb.updateNetwork(mMeshNetworkDao, mMeshNetwork);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final InternalMeshManagerCallbacks internalMeshMgrCallbacks = new InternalMeshManagerCallbacks() {
        @Override
        public void onNodeProvisioned(final ProvisionedMeshNode meshNode) {
            updateProvisionedNodeList(meshNode);
            incrementUnicastAddress(meshNode.getUnicastAddress(), meshNode.getNumberOfElements());
            //Set the mesh network uuid to the node so we can identify nodes belonging to a network
            meshNode.setMeshUuid(mMeshNetwork.getMeshUUID());
            mMeshNetworkDb.insertNode(mProvisionedNodeDao, meshNode);
            mMeshNetworkDb.updateProvisioner(mProvisionerDao,
                    mMeshNetwork.getSelectedProvisioner());
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
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

    private ProvisionedMeshNode getMeshNode(final int unicast) {
        return mMeshNetwork.getProvisionedNode(unicast);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final NetworkLayerCallbacks networkLayerCallbacks = new NetworkLayerCallbacks() {
        @Override
        public ProvisionedMeshNode getProvisionedNode(final int unicastAddress) {
            return getMeshNode(unicastAddress);
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
            }
            network.setCallbacks(callbacks);
            mMeshNetwork = network;
            mTransportCallbacks.onNetworkLoaded(network);
        }

        @Override
        public void onNetworkLoadFailed(final String error) {
            mTransportCallbacks.onNetworkLoadFailed(error);
        }

        @Override
        public void onNetworkImportedFromJson(final MeshNetwork meshNetwork) {
            meshNetwork.setCallbacks(callbacks);
            insertNetwork(meshNetwork);
            mMeshNetwork = meshNetwork;
            mTransportCallbacks.onNetworkImported(meshNetwork);
        }

        @Override
        public void onNetworkImportFailed(final String error) {
            mTransportCallbacks.onNetworkImportFailed(error);
        }

        @Override
        public void onNetworkExportedJson(MeshNetwork meshNetwork, String meshNetworkJson) {
            mTransportCallbacks.onNetworkExportedJson(meshNetwork, meshNetworkJson);
        }

        @Override
        public void onNetworkExported(final MeshNetwork meshNetwork) {
            mTransportCallbacks.onNetworkExported(meshNetwork);
        }

        @Override
        public void onNetworkExportFailed(final String error) {
            mTransportCallbacks.onNetworkExportFailed(error);
        }
    };

    /**
     * Callbacks observing user updates on the mesh network object
     */
    private final MeshNetworkCallbacks callbacks = new MeshNetworkCallbacks() {
        @Override
        public void onMeshNetworkUpdated() {
            mMeshNetwork.setTimestamp(MeshParserUtils.getInternationalAtomicTime(System.currentTimeMillis()));
            mMeshNetworkDb.updateNetwork(mMeshNetworkDao, mMeshNetwork);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onNetworkKeyAdded(final NetworkKey networkKey) {
            mMeshNetworkDb.insertNetKey(mNetworkKeyDao, networkKey);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onNetworkKeyUpdated(final NetworkKey networkKey) {
            mMeshNetworkDb.updateNetKey(mNetworkKeyDao, networkKey);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onNetworkKeyDeleted(final NetworkKey networkKey) {
            mMeshNetworkDb.deleteNetKey(mNetworkKeyDao, networkKey);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onApplicationKeyAdded(final ApplicationKey applicationKey) {
            mMeshNetworkDb.insertAppKey(mApplicationKeyDao, applicationKey);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onApplicationKeyUpdated(final ApplicationKey applicationKey) {
            mMeshNetworkDb.updateAppKey(mApplicationKeyDao, applicationKey);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onApplicationKeyDeleted(final ApplicationKey applicationKey) {
            mMeshNetworkDb.deleteAppKey(mApplicationKeyDao, applicationKey);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onProvisionerUpdated(final Provisioner provisioner) {
            mMeshNetworkDb.updateProvisioner(mProvisionerDao, provisioner);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onProvisionerUpdated(final List<Provisioner> provisioners) {
            mMeshNetworkDb.updateProvisioner(mProvisionerDao, provisioners);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onNodeDeleted(final ProvisionedMeshNode meshNode) {
            mMeshNetworkDb.deleteNode(mProvisionedNodeDao, meshNode);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onNodesUpdated() {
            mMeshNetworkDb.updateNodes(mProvisionedNodeDao, mMeshNetwork.nodes);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onGroupAdded(final Group group) {
            mMeshNetworkDb.insertGroup(mGroupDao, group);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onGroupUpdated(final Group group) {
            mMeshNetworkDb.updateGroup(mGroupDao, group);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onGroupDeleted(final Group group) {
            mMeshNetworkDb.deleteGroup(mGroupDao, group);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onSceneAdded(final Scene scene) {
            mMeshNetworkDb.insertScene(mSceneDao, scene);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onSceneUpdated(final Scene scene) {
            mMeshNetworkDb.updateScene(mSceneDao, scene);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }

        @Override
        public void onSceneDeleted(final Scene scene) {
            mMeshNetworkDb.deleteScene(mSceneDao, scene);
            mTransportCallbacks.onNetworkUpdated(mMeshNetwork);
        }
    };
}