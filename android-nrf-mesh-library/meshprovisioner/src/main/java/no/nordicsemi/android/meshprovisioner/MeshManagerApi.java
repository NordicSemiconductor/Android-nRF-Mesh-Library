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
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import no.nordicsemi.android.meshprovisioner.data.ApplicationKeyDao;
import no.nordicsemi.android.meshprovisioner.data.GroupDao;
import no.nordicsemi.android.meshprovisioner.data.MeshNetworkDao;
import no.nordicsemi.android.meshprovisioner.data.NetworkKeyDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionedMeshNodeDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionerDao;
import no.nordicsemi.android.meshprovisioner.data.SceneDao;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelAppBind;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelAppUnbind;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelSubscriptionAdd;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelSubscriptionDelete;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.ElementListDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.GenericOnOffGet;
import no.nordicsemi.android.meshprovisioner.transport.InternalMeshModelDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.MeshModelListDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.NetworkLayerCallbacks;
import no.nordicsemi.android.meshprovisioner.transport.NodeDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.UpperTransportLayerCallbacks;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;


@SuppressWarnings("WeakerAccess")
public class MeshManagerApi implements MeshMngrApi, InternalTransportCallbacks, InternalMeshManagerCallbacks, UpperTransportLayerCallbacks, NetworkLayerCallbacks {

    public final static UUID MESH_PROVISIONING_UUID = UUID.fromString("00001827-0000-1000-8000-00805F9B34FB");
    public final static UUID MESH_PROXY_UUID = UUID.fromString("00001828-0000-1000-8000-00805F9B34FB");

    private static final String PROVISIONED_NODES_FILE = "PROVISIONED_FILES";
    private static final String CONFIGURATION_SRC = "CONFIGURATION_SRC";
    private static final String SRC = "SRC";
    private static final String PREFS_SEQUENCE_NUMBER = "PREFS_SEQUENCE_NUMBER";
    private static final String SEQUENCE_NUMBER_KEY = "NRF_MESH_SEQUENCE_NUMBER";
    public static final byte PDU_TYPE_PROVISIONING = 0x03;

    private static final String TAG = MeshManagerApi.class.getSimpleName();
    //PDU types
    private static final byte PDU_TYPE_NETWORK = 0x00;
    private static final byte PDU_TYPE_MESH_BEACON = 0x01;
    private static final byte PDU_TYPE_PROXY_CONFIGURATION = 0x02;
    //GATT level segmentation
    private static final byte SAR_COMPLETE = 0b00;
    private static final byte GATT_SAR_START = 0b01;
    private static final byte GATT_SAR_CONTINUATION = 0b10;
    private static final byte GATT_SAR_END = 0b11;
    //GATT level segmentation mask
    private static final int GATT_SAR_MASK = 0xC0;
    private static final int GATT_SAR_UNMASK = 0x3F;
    private static final int SAR_BIT_OFFSET = 6;
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
    private final static int ADVERTISED_NETWWORK_ID_OFFSET = 1;
    /**
     * Length of the network id contained in the advertisement service data
     */
    private final static int ADVERTISED_NETWWORK_ID_LENGTH = 8;
    private Context mContext;
    private MeshManagerTransportCallbacks mTransportCallbacks;
    private MeshProvisioningHandler mMeshProvisioningHandler;
    private MeshMessageHandler mMeshMessageHandler;
    private byte[] mIncomingBuffer;
    private int mIncomingBufferOffset;
    private byte[] mOutgoingBuffer;
    private int mOutgoingBufferOffset;
    private MeshNetwork mMeshNetwork;
    private Gson mGson;

    private ProvisioningSettings mProvisioningSettings;
    private Map<Integer, ProvisionedMeshNode> mProvisionedNodes = new LinkedHashMap<>();
    private byte[] mConfigurationSrc = {0x07, (byte) 0xFF}; //0x07FF;

    private MeshNetworkDb mMeshNetworkDb;
    private MeshNetworkDao mMeshNetworkDao;
    private NetworkKeyDao mNetworkKeyDao;
    private ApplicationKeyDao mApplicationKeyDao;
    private ProvisionerDao mProvisionerDao;
    private ProvisionedMeshNodeDao mProvisionedNodeDao;
    private GroupDao mGroupDao;
    private SceneDao mSceneDao;

    public MeshManagerApi(final Context context) {
        this.mContext = context;
        mMeshProvisioningHandler = new MeshProvisioningHandler(context, this, this);
        mMeshMessageHandler = new MeshMessageHandler(context, this);
        mMeshMessageHandler.getMeshTransport().setNetworkLayerCallbacks(this);
        mMeshMessageHandler.getMeshTransport().setUpperTransportLayerCallbacks(this);
        mProvisioningSettings = new ProvisioningSettings(context);

        //Init database
        initDb(context);
        initGson();
        initConfigurationSrc();
        migrateMeshNetwork(context);

    }

    public void setProvisionerManagerTransportCallbacks(final MeshManagerTransportCallbacks transportCallbacks) {
        mTransportCallbacks = transportCallbacks;
    }

    public void setProvisioningStatusCallbacks(final MeshProvisioningStatusCallbacks callbacks) {
        mMeshProvisioningHandler.setProvisioningCallbacks(callbacks);
    }

    public void setMeshStatusCallbacks(final MeshStatusCallbacks callbacks) {
        mMeshMessageHandler.setMeshStatusCallbacks(callbacks);
    }

    /**
     * Loads the mesh network from the local database.
     * <p> This will start an AsyncTask that will load the network from the database.
     * {@link MeshManagerTransportCallbacks#onNetworkLoaded(MeshNetwork) will return the mesh netword}</>
     */
    public void loadMeshNetwork() {
        mMeshNetworkDb.loadNetwork(mMeshNetworkDao,
                mNetworkKeyDao,
                mApplicationKeyDao,
                mProvisionerDao,
                mProvisionedNodeDao,
                mGroupDao, mSceneDao,
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

    private void initDb(final Context context) {
        mMeshNetworkDb = MeshNetworkDb.getDatabase(context);
        mMeshNetworkDao = mMeshNetworkDb.meshNetworkDao();
        mNetworkKeyDao = mMeshNetworkDb.networkKeyDao();
        mApplicationKeyDao = mMeshNetworkDb.applicationKeyDao();
        mProvisionerDao = mMeshNetworkDb.provisionerDao();
        mProvisionedNodeDao = mMeshNetworkDb.provisionedMeshNodeDao();
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

    private void initConfigurationSrc() {
        final SharedPreferences preferences = mContext.getSharedPreferences(CONFIGURATION_SRC, Context.MODE_PRIVATE);
        final int tempSrc = preferences.getInt(SRC, 0);
        if (tempSrc != 0)
            mConfigurationSrc = new byte[]{(byte) ((tempSrc >> 8) & 0xFF), (byte) (tempSrc & 0xFF)};
    }

    /**
     * Migrates the old network data and loads a new mesh network object
     *
     * @param context context
     */
    private void migrateMeshNetwork(final Context context) {
        final MeshNetwork meshNetwork = DataMigrator.migrateData(context, mGson, mProvisioningSettings);
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

    private void updateNetwork(final MeshNetwork meshNetwork) {
        new Thread(() -> {
            mMeshNetworkDao.update(meshNetwork);
            mNetworkKeyDao.update(meshNetwork.netKeys);
            mApplicationKeyDao.update(meshNetwork.appKeys);
            mProvisionerDao.update(meshNetwork.provisioners);
            mProvisionedNodeDao.update(meshNetwork.nodes);
            if (meshNetwork.groups != null) {
                mGroupDao.update(meshNetwork.groups);
            }
            if (meshNetwork.scenes != null) {
                mSceneDao.update(meshNetwork.scenes);
            }
        }).start();
    }

    private void updateProvisionedMeshNode(final ProvisionedMeshNode node) {
        new Thread(() -> mProvisionedNodeDao.update(node)).start();
    }

    void addMeshNode(final ProvisionedMeshNode node) {
        mProvisionedNodes.put(node.getUnicastAddressInt(), node);
    }

    /**
     * Order the keys so that the nodes are read in insertion order
     *
     * @param nodes list containing unordered nodes
     * @return node list
     */
    private List<Integer> reOrderProvisionedNodes(final Map<String, ?> nodes) {
        final Set<String> unorderedKeys = nodes.keySet();
        final List<Integer> orderedKeys = new ArrayList<>();
        for (String k : unorderedKeys) {
            final int key = Integer.decode(k);
            orderedKeys.add(key);
        }
        Collections.sort(orderedKeys);
        return orderedKeys;
    }

    public void incrementUnicastAddress(final ProvisionedMeshNode meshNode) {
        //Since we know the number of elements this node contains we can predict the next available address for the next node.
        int unicastAdd = (meshNode.getUnicastAddressInt() + meshNode.getNumberOfElements());
        //We check if the incremented unicast address is already taken by the app/configurator
        final int tempSrc = (mConfigurationSrc[0] & 0xFF) << 8 | (mConfigurationSrc[1] & 0xFF);
        if (unicastAdd == tempSrc) {
            unicastAdd = unicastAdd + 1;
        }

        mMeshNetwork.setUnicastAddress(unicastAdd);
    }


    @Override
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void onNodeProvisioned(final ProvisionedMeshNode meshNode) {
        mMeshNetwork.nodes.add(meshNode);
        incrementUnicastAddress(meshNode);
        //Set the mesh network uuid to the node so we can identify nodes belonging to a network
        meshNode.setMeshUuid(mMeshNetwork.getMeshUUID());
        mMeshNetworkDb.insertNode(mProvisionedNodeDao, meshNode);
    }

    @Override
    public ProvisionedMeshNode getMeshNode(final int unicastAddress) {
        for (ProvisionedMeshNode node : mMeshNetwork.getProvisionedNodes()) {
            if (unicastAddress == node.getUnicastAddressInt()) {
                return node;
            }
        }
        return null;
    }

    @Override
    public Provisioner getProvisioner(final byte[] unicastAddress) {
        for (Provisioner provisioner : mMeshNetwork.getProvisioners()) {
            if (Arrays.equals(unicastAddress, provisioner.getProvisionerAddress()))
                return provisioner;
        }
        return null;
    }

    @Override
    public NetworkKey getPrimaryNetworkKey() {
        return mMeshNetwork.getPrimaryNetworkKey();
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
            if (combinedPdu == null)
                return;
            else {
                unsegmentedPdu = removeSegmentation(mtuSize, combinedPdu);
            }
        }
        parseNotifications(unsegmentedPdu);
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
                Log.v(TAG, "Received mesh beacon: " + MeshParserUtils.bytesToHex(unsegmentedPdu, true));
                break;
            case PDU_TYPE_PROXY_CONFIGURATION:
                //Proxy configuration
                Log.v(TAG, "Received proxy configuration message: " + MeshParserUtils.bytesToHex(unsegmentedPdu, true));
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
                break;
            case PDU_TYPE_PROVISIONING:
                //Provisioning PDU
                Log.v(TAG, "Provisioning pdu sent: " + MeshParserUtils.bytesToHex(data, true));
                mMeshProvisioningHandler.handleProvisioningWriteCallbacks();
                break;
        }
    }

    @Override
    public void sendProvisioningPdu(final UnprovisionedMeshNode meshNode, final byte[] pdu) {
        final int mtu = mTransportCallbacks.getMtu();
        mTransportCallbacks.sendProvisioningPdu(meshNode, applySegmentation(mtu, pdu));
    }

    @Override
    public void sendMeshPdu(final ProvisionedMeshNode meshNode, final byte[] pdu) {
        final int mtu = mTransportCallbacks.getMtu();
        mTransportCallbacks.sendMeshPdu(meshNode, applySegmentation(mtu, pdu));
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @Override
    public void updateMeshNetwork(final MeshMessage message) {
        final ProvisionedMeshNode meshNode = message.getMeshNode();
        if (meshNode != null) {
            for (int i = 0; i < mMeshNetwork.nodes.size(); i++) {
                if (meshNode.getUnicastAddressInt() == mMeshNetwork.nodes.get(i).getUnicastAddressInt()) {
                    mMeshNetwork.nodes.set(i, meshNode);
                    updateNetwork(mMeshNetwork);
                    break;
                }
            }
        }
    }

    @Override
    public void onMeshNodeReset(final ProvisionedMeshNode meshNode) {
        if (meshNode != null) {
            mMeshNetwork.deleteNode(meshNode);
            mMeshNetworkDb.deleteNode(mProvisionedNodeDao, meshNode);
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
    public void identifyNode(@NonNull final UUID deviceUuid, final String nodeName) throws IllegalArgumentException {
        //We must save all the provisioning data here so that they could be reused when provisioning the next devices

        final ProvisioningSettings provisioningSettings = mProvisioningSettings;
        mMeshProvisioningHandler.identify(deviceUuid, nodeName,
                mMeshNetwork.getPrimaryNetworkKey(),
                0,
                mMeshNetwork.getIvIndex(),
                mMeshNetwork.getUnicastAddress(),
                provisioningSettings.getGlobalTtl(), mMeshNetwork.getProvisioners().get(0).getProvisionerAddress());
    }

    @Override
    public void startProvisioning(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException {
        mMeshProvisioningHandler.startProvisioning(unprovisionedMeshNode);
    }

    @Override
    public final void setProvisioningConfirmation(@NonNull final String pin) {
        mMeshProvisioningHandler.setProvisioningConfirmation(pin);
    }

    @Override
    public MeshBeacon getBeacon(final byte[] serviceData) {
        if (serviceData != null) {
            final int beaconType = serviceData[0];
            if (beaconType == 0x00) {
                return new UnprovisionedBeacon(serviceData);
            } else {
                return new SecureNetworkBeacon(serviceData);
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
        final byte[] generatedHash = SecureUtils.calculateHash(meshNode.getIdentityKey(), random, meshNode.getUnicastAddress());

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
        return advertisedNetworkId != null && networkId.equals(MeshParserUtils.bytesToHex(advertisedNetworkId, false).toUpperCase());
    }

    @Override
    public boolean isAdvertisingWithNetworkIdentity(@Nullable final byte[] serviceData) {
        return serviceData != null && serviceData[ADVERTISED_NETWWORK_ID_OFFSET - 1] == ADVERTISEMENT_TYPE_NETWORK_ID;
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
        final ByteBuffer advertisedNetowrkID = ByteBuffer.allocate(ADVERTISED_NETWWORK_ID_LENGTH).order(ByteOrder.BIG_ENDIAN);
        advertisedNetowrkID.put(serviceData, ADVERTISED_NETWWORK_ID_OFFSET, ADVERTISED_HASH_LENGTH);
        return advertisedNetowrkID.array();
    }

    /**
     * Resets the provisioned mesh network and loads a new one
     * <p>This method will clear the provisioned nodes, reset the sequence number and generate new network with new provisioning data</p>
     */
    public final void resetMeshNetwork() {
        //We delete the existing network as the user has already given the
        final MeshNetwork meshNet = mMeshNetwork;
        mMeshNetworkDb.deleteNetwork(mMeshNetworkDao, meshNet);
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
        final AllocatedUnicastRange unicastRange = new AllocatedUnicastRange(new byte[]{0x00, 0x00}, new byte[]{0x7F, 0x7E});
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
     */
    public final void deleteMeshNetworkFromDb() {
        final MeshNetwork network = mMeshNetwork;
        mMeshNetworkDb.deleteNetwork(mMeshNetworkDao, network);
    }

    /**
     * @deprecated Use {@link #sendMeshConfigurationMessage(MeshMessage)}instead.
     */
    @Deprecated
    @Override
    public void getCompositionData(@NonNull final ProvisionedMeshNode meshNode) {
        final int aszmic = 0;
        mMeshMessageHandler.sendCompositionDataGet(meshNode, aszmic);
    }

    /**
     * @deprecated Use {@link #sendMeshConfigurationMessage(MeshMessage)}instead.
     */
    @Deprecated
    @Override
    public void addAppKey(@NonNull final ProvisionedMeshNode meshNode, final int appKeyIndex, @NonNull final String appKey) {
        if (appKey.isEmpty())
            throw new IllegalArgumentException(mContext.getString(R.string.error_null_key));
        mMeshMessageHandler.sendAppKeyAdd(meshNode, appKeyIndex, appKey, 0);
    }

    /**
     * @deprecated Use {@link #sendMeshConfigurationMessage(MeshMessage)}instead.
     */
    @Deprecated
    @Override
    public void bindAppKey(@NonNull final ProvisionedMeshNode meshNode, @NonNull final byte[] elementAddress, @NonNull final MeshModel model, final int appKeyIndex) {
        final ConfigModelAppBind configModelAppBind = new ConfigModelAppBind(meshNode, elementAddress, model.getModelId(), appKeyIndex, 0);
        sendMeshConfigurationMessage(configModelAppBind);
    }

    /**
     * @deprecated Use {@link #sendMeshConfigurationMessage(MeshMessage)}instead.
     */
    @Deprecated
    @Override
    public void unbindAppKey(@NonNull final ProvisionedMeshNode meshNode, @NonNull final byte[] elementAddress, @NonNull final MeshModel model, final int appKeyIndex) {
        final ConfigModelAppUnbind configModelAppUnbind = new ConfigModelAppUnbind(meshNode, elementAddress, model.getModelId(), appKeyIndex, 0);
        sendMeshConfigurationMessage(configModelAppUnbind);
    }

    /**
     * @deprecated Use {@link #sendMeshConfigurationMessage(MeshMessage)}instead.
     */
    @Deprecated
    @Override
    public void addSubscriptionAddress(@NonNull final ProvisionedMeshNode meshNode, @NonNull final byte[] elementAddress, @NonNull final byte[] subscriptionAddress,
                                       final int modelIdentifier) {
        final ConfigModelSubscriptionAdd configModelSubscriptionAdd = new ConfigModelSubscriptionAdd(meshNode, elementAddress, subscriptionAddress, modelIdentifier, 0);
        sendMeshConfigurationMessage(configModelSubscriptionAdd);
    }

    /**
     * @deprecated Use {@link #sendMeshConfigurationMessage(MeshMessage)}instead.
     */
    @Deprecated
    @Override
    public void deleteSubscriptionAddress(@NonNull final ProvisionedMeshNode meshNode, @NonNull final byte[] elementAddress, @NonNull final byte[] subscriptionAddress,
                                          final int modelIdentifier) {
        final ConfigModelSubscriptionDelete configModelSubscriptionDelete = new ConfigModelSubscriptionDelete(meshNode, elementAddress, subscriptionAddress, modelIdentifier, 0);
        sendMeshConfigurationMessage(configModelSubscriptionDelete);
    }

    /**
     * @deprecated Use {@link #sendMeshConfigurationMessage(MeshMessage)}instead.
     */
    @Deprecated
    @Override
    public void resetMeshNode(@NonNull final ProvisionedMeshNode provisionedMeshNode) {
        mMeshMessageHandler.resetMeshNode(provisionedMeshNode);
    }

    /**
     * @deprecated Use {@link #sendMeshApplicationMessage(byte[], MeshMessage)}instead.
     */
    @Override
    public void getGenericOnOff(@NonNull final ProvisionedMeshNode node, @NonNull final MeshModel model,
                                @NonNull final byte[] dstAddress, final int appKeyIndex) throws IllegalArgumentException {
        mMeshMessageHandler.getGenericOnOff(node, model, dstAddress, false, appKeyIndex);
    }

    /**
     * @deprecated Use {@link #sendMeshApplicationMessage(byte[], MeshMessage)}instead.
     */
    @Override
    public void getGenericOnOff(final byte[] dstAddress, @NonNull final GenericOnOffGet genericOnOffGet) {
        mMeshMessageHandler.getGenericOnOff(dstAddress, genericOnOffGet);
    }

    /**
     * @deprecated Use {@link #sendMeshApplicationMessage(byte[], MeshMessage)}instead.
     */
    @Override
    public void setGenericOnOff(final ProvisionedMeshNode node, final MeshModel model, final byte[] dstAddress, final int appKeyIndex, @Nullable final Integer transitionSteps, @Nullable final Integer transitionResolution, @Nullable final Integer delay, final boolean state) {
        if (!model.getBoundAppKeyIndexes().isEmpty()) {
            if (appKeyIndex >= 0) {
                if (dstAddress == null)
                    throw new IllegalArgumentException("Destination address cannot be null!");
                mMeshMessageHandler.setGenericOnOff(node, model, dstAddress, false, appKeyIndex, transitionSteps, transitionResolution, delay, state);
            } else {
                throw new IllegalArgumentException("Invalid app key index!");
            }
        } else {
            throw new IllegalArgumentException("Please bind an app key to this model to control this model!");
        }
    }

    /**
     * @deprecated Use {@link #sendMeshApplicationMessage(byte[], MeshMessage)}instead.
     */
    @Override
    public void setGenericOnOffUnacknowledged(final ProvisionedMeshNode node, final MeshModel model, final byte[] dstAddress, final int appKeyIndex, @Nullable final Integer transitionSteps, @Nullable final Integer transitionResolution, @Nullable final Integer delay, final boolean state) {
        if (!model.getBoundAppKeyIndexes().isEmpty()) {
            if (appKeyIndex >= 0) {
                if (dstAddress == null)
                    throw new IllegalArgumentException("Destination address cannot be null!");
                mMeshMessageHandler.setGenericOnOffUnacknowledged(node, model, dstAddress, false, appKeyIndex, transitionSteps, transitionResolution, delay, state);
            } else {
                throw new IllegalArgumentException("Invalid app key index!");
            }
        } else {
            throw new IllegalArgumentException("Please bind an app key to this model to control this model!");
        }
    }

    /**
     * @deprecated Use {@link #sendMeshApplicationMessage(byte[], MeshMessage)}instead.
     */
    @Override
    public void getGenericLevel(final ProvisionedMeshNode node, final MeshModel model, final byte[] dstAddress, final int appKeyIndex) {
        if (!model.getBoundAppKeyIndexes().isEmpty()) {
            if (appKeyIndex >= 0) {
                if (dstAddress == null)
                    throw new IllegalArgumentException("Destination address cannot be null!");
                mMeshMessageHandler.getGenericLevel(node, model, dstAddress, false, appKeyIndex);
            } else {
                throw new IllegalArgumentException("Invalid app key index!");
            }
        } else {
            throw new IllegalArgumentException("Please bind an app key to this model to control this model!");
        }
    }

    /**
     * @deprecated Use {@link #sendMeshApplicationMessage(byte[], MeshMessage)}instead.
     */
    @Override
    public void setGenericLevel(final ProvisionedMeshNode node, final MeshModel model, final byte[] dstAddress, final int appKeyIndex, @Nullable final Integer transitionSteps, @Nullable final Integer transitionResolution, @Nullable final Integer delay, final int level) {
        if (!model.getBoundAppKeyIndexes().isEmpty()) {
            if (appKeyIndex >= 0) {
                if (dstAddress == null)
                    throw new IllegalArgumentException("Destination address cannot be null!");
                mMeshMessageHandler.setGenericLevel(node, model, dstAddress, false, appKeyIndex, transitionSteps, transitionResolution, delay, level);
            } else {
                throw new IllegalArgumentException("Invalid app key index!");
            }
        } else {
            throw new IllegalArgumentException("Please bind an app key to this model to control this model!");
        }
    }

    /**
     * @deprecated Use {@link #sendMeshApplicationMessage(byte[], MeshMessage)}instead.
     */
    @Override
    public void setGenericLevelUnacknowledged(final ProvisionedMeshNode node, final MeshModel model, final byte[] dstAddress, final int appKeyIndex, @Nullable final Integer transitionSteps, @Nullable final Integer transitionResolution, @Nullable final Integer delay, final int level) {
        if (!model.getBoundAppKeyIndexes().isEmpty()) {
            if (appKeyIndex >= 0) {
                if (dstAddress == null)
                    throw new IllegalArgumentException("Destination address cannot be null!");
                mMeshMessageHandler.setGenericLevelUnacknowledged(node, model, dstAddress, false, appKeyIndex, transitionSteps, transitionResolution, delay, level);
            } else {
                throw new IllegalArgumentException("Invalid app key index!");
            }
        } else {
            throw new IllegalArgumentException("Please bind an app key to this model to control this model!");
        }
    }

    /**
     * @deprecated Use {@link #sendMeshApplicationMessage(byte[], MeshMessage)}instead.
     */
    @Override
    public void sendVendorModelUnacknowledgedMessage(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final int appKeyIndex, final int opcode, final byte[] parameters) {
        mMeshMessageHandler.sendVendorModelUnacknowledgedMessage(node, (VendorModel) model, address, false, appKeyIndex, opcode, parameters);
    }

    /**
     * @deprecated Use {@link #sendMeshApplicationMessage(byte[], MeshMessage)}instead.
     */
    @Override
    public void sendVendorModelAcknowledgedMessage(final ProvisionedMeshNode node, final MeshModel model, final byte[] address, final int appKeyIndex, final int opcode, final byte[] parameters) {
        mMeshMessageHandler.sendVendorModelAcknowledgedMessage(node, (VendorModel) model, address, false, appKeyIndex, opcode, parameters);
    }

    @Override
    public final void sendMeshConfigurationMessage(@NonNull final MeshMessage configMessage) {
        mMeshMessageHandler.sendMeshMessage(configMessage);
    }

    @Override
    public final void sendMeshApplicationMessage(@NonNull final byte[] dstAddress, @NonNull final MeshMessage meshMessage) {
        mMeshMessageHandler.sendMeshMessage(dstAddress, meshMessage);
    }

    @Override
    public boolean exportMeshNetwork(final String path) {
        exportNetwork();
        return false;
    }

    @Override
    public void importMeshNetwork(final Uri uri) {
        NetworkImportExportUtils.importMeshNetwork(mContext, uri, networkLoadCallbacks);
    }

    private void exportNetwork() {
        BufferedWriter br = null;
        try {

            Type netKeyList = new TypeToken<List<NetworkKey>>() {
            }.getType();
            Type appKeyList = new TypeToken<List<ApplicationKey>>() {
            }.getType();
            Type nodeList = new TypeToken<List<ProvisionedMeshNode>>() {
            }.getType();
            Type meshModelList = new TypeToken<List<MeshModel>>() {
            }.getType();
            Type elementList = new TypeToken<List<Element>>() {
            }.getType();

            final GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(MeshNetwork.class, new MeshNetworkDeserializer());
            gsonBuilder.registerTypeAdapter(netKeyList, new NetKeyDeserializer());
            gsonBuilder.registerTypeAdapter(appKeyList, new AppKeyDeserializer());
            gsonBuilder.registerTypeAdapter(AllocatedGroupRange.class, new AllocatedGroupRangeDeserializer());
            gsonBuilder.registerTypeAdapter(AllocatedUnicastRange.class, new AllocatedUnicastRangeDeserializer());
            gsonBuilder.registerTypeAdapter(AllocatedSceneRange.class, new AllocatedSceneRangeDeserializer());
            gsonBuilder.registerTypeAdapter(nodeList, new NodeDeserializer());
            gsonBuilder.registerTypeAdapter(elementList, new ElementListDeserializer());
            gsonBuilder.registerTypeAdapter(meshModelList, new MeshModelListDeserializer());
            final Gson gson = gsonBuilder.create();

            final File f = new File(mContext.getFilesDir(), "example_database.json");
            br = new BufferedWriter(new FileWriter(f));
            final String network = gson.toJson(MeshNetwork.class);
            final OutputStream outputStream = mContext.openFileOutput(f.getName(), Context.MODE_PRIVATE);
            outputStream.write(network.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

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

    /**
     * Callbacks to notify when the database has been loaded
     */
    private final LoadNetworkCallbacks networkLoadCallbacks = new LoadNetworkCallbacks() {
        @Override
        public void onNetworkLoadedFromDb(final MeshNetwork meshNetwork) {
            final MeshNetwork network;
            //If there is no network we generate a new one
            if(meshNetwork == null) {
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
        public void onNetworkLoadedFromJson(final MeshNetwork meshNetwork) {
            meshNetwork.setCallbacks(callbacks);
            insertNetwork(meshNetwork);
            mMeshNetwork = meshNetwork;
            mTransportCallbacks.onNetworkLoaded(meshNetwork);
        }

        @Override
        public void onNetworkLoadFailed(final String error) {
            mTransportCallbacks.onNetworkLoadFailed(error);
        }
    };

    /**
     * Callbacks observing user updates on the mesh network object
     */
    private final MeshNetworkCallbacks callbacks = new MeshNetworkCallbacks() {
        @Override
        public void onMeshNetworkUpdated() {
            updateNetwork(mMeshNetwork);
        }

        @Override
        public void onNetworkKeyAdded(final NetworkKey networkKey) {
            mMeshNetworkDb.insertNetKey(mNetworkKeyDao, networkKey);
        }

        @Override
        public void onNetworkKeyUpdated(final NetworkKey networkKey) {
            mMeshNetworkDb.updateNetKey(mNetworkKeyDao, networkKey);
        }

        @Override
        public void onNetworkKeyDeleted(final NetworkKey networkKey) {
            mMeshNetworkDb.deleteNetKey(mNetworkKeyDao, networkKey);
        }

        @Override
        public void onApplicationKeyAdded(final ApplicationKey applicationKey) {
            mMeshNetworkDb.insertAppKey(mApplicationKeyDao, applicationKey);
        }

        @Override
        public void onApplicationKeyUpdated(final ApplicationKey applicationKey) {
            mMeshNetworkDb.updateAppKey(mApplicationKeyDao, applicationKey);
        }

        @Override
        public void onApplicationKeyDeleted(final ApplicationKey applicationKey) {
            mMeshNetworkDb.deleteAppKey(mApplicationKeyDao, applicationKey);
        }

        @Override
        public void onProvisionerUpdated(final Provisioner provisioner) {
            mMeshNetworkDb.updateProvisioner(mProvisionerDao, provisioner);
        }

        @Override
        public void onProvisionerUpdated(final List<Provisioner> provisioners) {
            mMeshNetworkDb.updateProvisioner(mProvisionerDao, provisioners);
        }

        @Override
        public void onGroupAdded(final Group group) {
            mMeshNetworkDb.insertGroup(mGroupDao, group);
        }

        @Override
        public void onGroupUpdated(final Group group) {
            mMeshNetworkDb.updateGroup(mGroupDao, group);
        }

        @Override
        public void onGroupDeleted(final Group group) {
            mMeshNetworkDb.deleteGroup(mGroupDao, group);
        }

        @Override
        public void onSceneAdded(final Scene scene) {
            mMeshNetworkDb.insertScene(mSceneDao, scene);
        }

        @Override
        public void onSceneUpdated(final Scene scene) {
            mMeshNetworkDb.updateScene(mSceneDao, scene);
        }

        @Override
        public void onSceneDeleted(final Scene scene) {
            mMeshNetworkDb.deleteScene(mSceneDao, scene);
        }
    };
}