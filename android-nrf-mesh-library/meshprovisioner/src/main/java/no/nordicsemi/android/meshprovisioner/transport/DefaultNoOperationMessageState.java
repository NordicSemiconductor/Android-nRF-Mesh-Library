package no.nordicsemi.android.meshprovisioner.transport;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.control.BlockAcknowledgementMessage;
import no.nordicsemi.android.meshprovisioner.control.TransportControlMessage;
import no.nordicsemi.android.meshprovisioner.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.opcodes.ProxyConfigMessageOpCodes;
import no.nordicsemi.android.meshprovisioner.utils.AddressArray;
import no.nordicsemi.android.meshprovisioner.utils.ExtendedInvalidCipherTextException;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.NetworkTransmitSettings;
import no.nordicsemi.android.meshprovisioner.utils.ProxyFilter;
import no.nordicsemi.android.meshprovisioner.utils.ProxyFilterType;
import no.nordicsemi.android.meshprovisioner.utils.RelaySettings;

class DefaultNoOperationMessageState extends MeshMessageState {

    private static final String TAG = DefaultNoOperationMessageState.class.getSimpleName();

    /**
     * Constructs the DefaultNoOperationMessageState
     *
     * @param meshMessage   {@link MeshMessage} Mesh message to be sent
     * @param meshTransport {@link MeshTransport} Mesh transport
     * @param callbacks     {@link InternalMeshMsgHandlerCallbacks} callbacks
     */
    DefaultNoOperationMessageState(@Nullable final MeshMessage meshMessage,
                                   @NonNull final MeshTransport meshTransport,
                                   @NonNull final InternalMeshMsgHandlerCallbacks callbacks) {
        super(meshMessage, meshTransport, callbacks);
    }

    @Override
    public MessageState getState() {
        return null;
    }

    void parseMeshPdu(@NonNull final ProvisionedMeshNode node, @NonNull final byte[] pdu, @NonNull final byte[] networkHeader, @NonNull final byte[] decryptedNetworkPayload) {
        final Message message;
        try {
            message = mMeshTransport.parsePdu(node, pdu, networkHeader, decryptedNetworkPayload);
            if (message != null) {
                if (message instanceof AccessMessage) {
                    parseAccessMessage((AccessMessage) message);
                } else {
                    parseControlMessage((ControlMessage) message);
                }
            } else {
                Log.v(TAG, "Message reassembly may not be completed yet!");
            }
        } catch (ExtendedInvalidCipherTextException e) {
            Log.e(TAG, "Decryption failed in " + e.getTag() + " : " + e.getMessage());
            mMeshStatusCallbacks.onMessageDecryptionFailed(e.getTag(), e.getMessage());
        }
    }

    /**
     * Parses Access message received
     *
     * @param message access message received by the acccess layer
     */
    private void parseAccessMessage(final AccessMessage message) {
        final byte[] accessPayload = message.getAccessPdu();
        final ProvisionedMeshNode node = mInternalTransportCallbacks.getNode(message.getSrc());
        final int opCodeLength = ((accessPayload[0] & 0xF0) >> 6);
        //OpCode length
        switch (opCodeLength) {
            case 0:
                if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_COMPOSITION_DATA_STATUS) {
                    final ConfigCompositionDataStatus status = new ConfigCompositionDataStatus(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        node.setCompositionData(status);
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                }
                break;
            case 1:
                if (message.getOpCode() == ApplicationMessageOpCodes.SCENE_STATUS) {
                    final SceneStatus sceneStatus = new SceneStatus(message);
                    mInternalTransportCallbacks.updateMeshNetwork(sceneStatus);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), sceneStatus);
                }
                break;
            case 2:
                if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_DEFAULT_TTL_STATUS) {
                    final ConfigDefaultTtlStatus status = new ConfigDefaultTtlStatus(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        node.setTtl(status.getTtl());
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_NETKEY_STATUS) {
                    final ConfigNetKeyStatus status = new ConfigNetKeyStatus(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        if (status.isSuccessful()) {
                            if (mMeshMessage instanceof ConfigNetKeyAdd) {
                                node.setAddedNetKeyIndex(status.getNetKeyIndex());
                            } else if (mMeshMessage instanceof ConfigNetKeyUpdate) {
                                node.updateAddedNetKey(status.getNetKeyIndex());
                            } else if (mMeshMessage instanceof ConfigNetKeyDelete) {
                                node.removeAddedNetKeyIndex(status.getNetKeyIndex());
                            }
                        }
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_NETKEY_LIST) {
                    final ConfigNetKeyList netKeyList = new ConfigNetKeyList(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        if (netKeyList.isSuccessful()) {
                            node.updateNetKeyList(netKeyList.getKeyIndexes());
                        }
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(netKeyList);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), netKeyList);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_APPKEY_STATUS) {
                    final ConfigAppKeyStatus status = new ConfigAppKeyStatus(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        if (status.isSuccessful()) {
                            if (mMeshMessage instanceof ConfigAppKeyAdd) {
                                node.setAddedAppKeyIndex(status.getAppKeyIndex());
                            } else if (mMeshMessage instanceof ConfigAppKeyUpdate) {
                                node.updateAddedNetKey(status.getAppKeyIndex());
                            } else if (mMeshMessage instanceof ConfigAppKeyDelete) {
                                node.removeAddedAppKeyIndex(status.getAppKeyIndex());
                            }
                        }
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_APPKEY_LIST) {
                    final ConfigAppKeyList appKeyList = new ConfigAppKeyList(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        if (appKeyList.isSuccessful()) {
                            node.updateAppKeyList(appKeyList.getNetKeyIndex(), appKeyList.getKeyIndexes(),
                                    mInternalTransportCallbacks.getApplicationKeys(appKeyList.getNetKeyIndex()));
                        }
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(appKeyList);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), appKeyList);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_MODEL_APP_STATUS) {
                    final ConfigModelAppStatus status = new ConfigModelAppStatus(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        if (status.isSuccessful()) {
                            if (mMeshMessage instanceof ConfigModelAppBind) {
                                node.setAppKeyBindStatus(status);
                            } else {
                                node.setAppKeyUnbindStatus(status);
                            }
                        }
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_SIG_MODEL_APP_LIST) {
                    final ConfigSigModelAppList appKeyList = new ConfigSigModelAppList(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        if (appKeyList.isSuccessful()) {
                            final Element element = node.getElements().get(appKeyList.getElementAddress());
                            if (element != null) {
                                final MeshModel model = element.getMeshModels().get(appKeyList.getModelIdentifier());
                                if (model != null) {
                                    model.setBoundAppKeyIndexes(appKeyList.getKeyIndexes());
                                }
                            }
                        }
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(appKeyList);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), appKeyList);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_VENDOR_MODEL_APP_LIST) {
                    final ConfigVendorModelAppList appKeyList = new ConfigVendorModelAppList(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        if (appKeyList.isSuccessful()) {
                            final Element element = node.getElements().get(appKeyList.getElementAddress());
                            if (element != null) {
                                final MeshModel model = element.getMeshModels().get(appKeyList.getModelIdentifier());
                                if (model != null) {
                                    model.setBoundAppKeyIndexes(appKeyList.getKeyIndexes());
                                }
                            }
                        }
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(appKeyList);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), appKeyList);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_MODEL_PUBLICATION_STATUS) {
                    final ConfigModelPublicationStatus status = new ConfigModelPublicationStatus(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        if (status.isSuccessful()) {
                            final Element element = node.getElements().get(status.getElementAddress());
                            if (element != null) {
                                final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                                if (model != null) {
                                    if (mMeshMessage instanceof ConfigModelPublicationGet) {
                                        model.updatePublicationStatus(status);
                                    } else if (mMeshMessage instanceof ConfigModelPublicationSet) {
                                        model.setPublicationStatus(status, null);
                                    } else if (mMeshMessage instanceof ConfigModelPublicationVirtualAddressSet) {
                                        final UUID labelUUID = ((ConfigModelPublicationVirtualAddressSet) mMeshMessage).
                                                getLabelUuid();
                                        model.setPublicationStatus(status, labelUUID);
                                    }
                                }
                            }
                        }
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_STATUS) {
                    final ConfigModelSubscriptionStatus status = new ConfigModelSubscriptionStatus(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        if (status.isSuccessful()) {
                            final Element element = node.getElements().get(status.getElementAddress());
                            if (element != null) {
                                final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                                if (model != null) {
                                    if (mMeshMessage instanceof ConfigModelSubscriptionAdd) {
                                        model.addSubscriptionAddress(status.getSubscriptionAddress());
                                    } else if (mMeshMessage instanceof ConfigModelSubscriptionVirtualAddressAdd) {
                                        model.addSubscriptionAddress(((ConfigModelSubscriptionVirtualAddressAdd) mMeshMessage).
                                                getLabelUuid(), status.getSubscriptionAddress());
                                    } else if (mMeshMessage instanceof ConfigModelSubscriptionOverwrite) {
                                        model.overwriteSubscriptionAddress(status.getSubscriptionAddress());
                                    } else if (mMeshMessage instanceof ConfigModelSubscriptionVirtualAddressOverwrite) {
                                        model.overwriteSubscriptionAddress(((ConfigModelSubscriptionVirtualAddressOverwrite) mMeshMessage).
                                                getLabelUuid(), status.getSubscriptionAddress());
                                    } else if (mMeshMessage instanceof ConfigModelSubscriptionDelete) {
                                        model.removeSubscriptionAddress(status.getSubscriptionAddress());
                                    } else if (mMeshMessage instanceof ConfigModelSubscriptionVirtualAddressDelete) {
                                        model.removeSubscriptionAddress(((ConfigModelSubscriptionVirtualAddressDelete) mMeshMessage).
                                                getLabelUuid(), status.getSubscriptionAddress());
                                    } else if (mMeshMessage instanceof ConfigModelSubscriptionDeleteAll) {
                                        model.removeAllSubscriptionAddresses();
                                    }
                                }
                            }
                        }
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_SIG_MODEL_SUBSCRIPTION_LIST) {
                    final ConfigSigModelSubscriptionList status = new ConfigSigModelSubscriptionList(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        if (status.isSuccessful()) {
                            final Element element = node.getElements().get(status.getElementAddress());
                            if (element != null) {
                                final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                                if (model != null) {
                                    model.updateSubscriptionAddressesList(status.getSubscriptionAddresses());
                                }
                            }
                            createGroups(status.getSubscriptionAddresses());
                        }
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_VENDOR_MODEL_SUBSCRIPTION_LIST) {
                    final ConfigVendorModelSubscriptionList status = new ConfigVendorModelSubscriptionList(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        if (status.isSuccessful()) {
                            final Element element = node.getElements().get(status.getElementAddress());
                            if (element != null) {
                                final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                                if (model != null) {
                                    model.updateSubscriptionAddressesList(status.getSubscriptionAddresses());
                                }
                            }
                            createGroups(status.getSubscriptionAddresses());
                        }
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_NODE_RESET_STATUS) {
                    final ConfigNodeResetStatus status = new ConfigNodeResetStatus(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        mInternalTransportCallbacks.onMeshNodeReset(node);
                    }
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_NETWORK_TRANSMIT_STATUS) {
                    final ConfigNetworkTransmitStatus status = new ConfigNetworkTransmitStatus(message);
                    final NetworkTransmitSettings networkTransmitSettings =
                            new NetworkTransmitSettings(status.getNetworkTransmitCount(), status.getNetworkTransmitIntervalSteps());
                    node.setNetworkTransmitSettings(networkTransmitSettings);
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_RELAY_STATUS) {
                    final ConfigRelayStatus status = new ConfigRelayStatus(message);
                    if (!isReceivedViaProxyFilter(message)) {
                        final RelaySettings relaySettings =
                                new RelaySettings(status.getRelayRetransmitCount(), status.getRelayRetransmitIntervalSteps());
                        node.setRelaySettings(relaySettings);
                    }
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                } else if (message.getOpCode() == ConfigMessageOpCodes.CONFIG_GATT_PROXY_STATUS) {
                    final ConfigProxyStatus status = new ConfigProxyStatus(message);
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                } else if (message.getOpCode() == ApplicationMessageOpCodes.GENERIC_ON_OFF_STATUS) {
                    final GenericOnOffStatus status = new GenericOnOffStatus(message);
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                } else if (message.getOpCode() == ApplicationMessageOpCodes.GENERIC_LEVEL_STATUS) {
                    final GenericLevelStatus genericLevelStatus = new GenericLevelStatus(message);
                    mInternalTransportCallbacks.updateMeshNetwork(genericLevelStatus);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), genericLevelStatus);
                } else if (message.getOpCode() == ApplicationMessageOpCodes.LIGHT_LIGHTNESS_STATUS) {
                    final LightLightnessStatus lightLightnessStatus = new LightLightnessStatus(message);
                    mInternalTransportCallbacks.updateMeshNetwork(lightLightnessStatus);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), lightLightnessStatus);
                } else if (message.getOpCode() == ApplicationMessageOpCodes.LIGHT_CTL_STATUS) {
                    final LightCtlStatus lightCtlStatus = new LightCtlStatus(message);
                    mInternalTransportCallbacks.updateMeshNetwork(lightCtlStatus);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), lightCtlStatus);
                } else if (message.getOpCode() == ApplicationMessageOpCodes.LIGHT_HSL_STATUS) {
                    final LightHslStatus lightHslStatus = new LightHslStatus(message);
                    mInternalTransportCallbacks.updateMeshNetwork(lightHslStatus);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), lightHslStatus);
                } else if (message.getOpCode() == ApplicationMessageOpCodes.SCENE_REGISTER_STATUS) {
                    final SceneRegisterStatus registerStatus = new SceneRegisterStatus(message);
                    registerStatus.parseStatusParameters();
                    mInternalTransportCallbacks.updateMeshNetwork(registerStatus);
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), registerStatus);
                } else {
                    Log.v(TAG, "Unknown Access PDU Received: " + MeshParserUtils.bytesToHex(accessPayload, false));
                    mMeshStatusCallbacks.onUnknownPduReceived(message.getSrc(), message.getAccessPdu());
                }
                break;
            case 3:
                if (mMeshMessage instanceof VendorModelMessageAcked) {
                    final VendorModelMessageAcked vendorModelMessageAcked = (VendorModelMessageAcked) mMeshMessage;
                    final VendorModelMessageStatus status = new VendorModelMessageStatus(message, vendorModelMessageAcked.getModelIdentifier());
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                    Log.v(TAG, "Vendor model Access PDU Received: " + MeshParserUtils.bytesToHex(accessPayload, false));
                } else if (mMeshMessage instanceof VendorModelMessageUnacked) {
                    final VendorModelMessageUnacked vendorModelMessageUnacked = (VendorModelMessageUnacked) mMeshMessage;
                    final VendorModelMessageStatus status = new VendorModelMessageStatus(message, vendorModelMessageUnacked.getModelIdentifier());
                    mMeshStatusCallbacks.onMeshMessageReceived(message.getSrc(), status);
                }
                break;
            default:
                Log.v(TAG, "Unknown Access PDU Received: " + MeshParserUtils.bytesToHex(accessPayload, false));
                mMeshStatusCallbacks.onUnknownPduReceived(message.getSrc(), message.getAccessPdu());
                break;
        }
    }

    /**
     * Parses control message received
     *
     * @param controlMessage control message received by the transport layer
     */
    private void parseControlMessage(final ControlMessage controlMessage) {
        //Get the segment count count of the access message
        final int segmentCount = message.getNetworkLayerPdu().size();
        if (controlMessage.getPduType() == MeshManagerApi.PDU_TYPE_NETWORK) {
            final TransportControlMessage transportControlMessage = controlMessage.getTransportControlMessage();
            if (transportControlMessage.getState() == TransportControlMessage.TransportControlMessageState.LOWER_TRANSPORT_BLOCK_ACKNOWLEDGEMENT) {
                Log.v(TAG, "Acknowledgement payload: " + MeshParserUtils.bytesToHex(controlMessage.getTransportControlPdu(), false));
                final ArrayList<Integer> retransmitPduIndexes = BlockAcknowledgementMessage.getSegmentsToBeRetransmitted(controlMessage.getTransportControlPdu(), segmentCount);
                mMeshStatusCallbacks.onBlockAcknowledgementReceived(controlMessage.getSrc(), controlMessage);
                executeResend(retransmitPduIndexes);
            } else {
                Log.v(TAG, "Unexpected control message received, ignoring message");
                mMeshStatusCallbacks.onUnknownPduReceived(controlMessage.getSrc(), controlMessage.getTransportControlPdu());
            }
        } else if (controlMessage.getPduType() == MeshManagerApi.PDU_TYPE_PROXY_CONFIGURATION) {
            if (controlMessage.getOpCode() == ProxyConfigMessageOpCodes.FILTER_STATUS) {
                final ProxyConfigFilterStatus status = new ProxyConfigFilterStatus(controlMessage);
                final ProxyFilter filter;
                if (mMeshMessage instanceof ProxyConfigSetFilterType) {
                    filter = new ProxyFilter(status.getFilterType());
                    mInternalTransportCallbacks.setProxyFilter(filter);
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(controlMessage.getSrc(), status);
                } else if (mMeshMessage instanceof ProxyConfigAddAddressToFilter) {
                    filter = updateProxyFilter(mInternalTransportCallbacks.getProxyFilter(), status.getFilterType());
                    final ProxyConfigAddAddressToFilter addAddressToFilter = (ProxyConfigAddAddressToFilter) mMeshMessage;
                    for (AddressArray addressArray : addAddressToFilter.getAddresses()) {
                        filter.addAddress(addressArray);
                    }
                    mInternalTransportCallbacks.setProxyFilter(filter);
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(controlMessage.getSrc(), status);

                } else if (mMeshMessage instanceof ProxyConfigRemoveAddressFromFilter) {
                    filter = updateProxyFilter(mInternalTransportCallbacks.getProxyFilter(), status.getFilterType());
                    final ProxyConfigRemoveAddressFromFilter removeAddressFromFilter = (ProxyConfigRemoveAddressFromFilter) mMeshMessage;
                    for (AddressArray addressArray : removeAddressFromFilter.getAddresses()) {
                        filter.removeAddress(addressArray);
                    }
                    mInternalTransportCallbacks.setProxyFilter(filter);
                    mInternalTransportCallbacks.updateMeshNetwork(status);
                    mMeshStatusCallbacks.onMeshMessageReceived(controlMessage.getSrc(), status);
                }
            }
        }
    }

    /**
     * Checks and returns a new filter or the existing filter
     *
     * @param currentFilter Proxy filter that is currently set on this node
     * @param filterType    Type of {@link ProxyFilterType} that was received by the status message
     */
    private ProxyFilter updateProxyFilter(final ProxyFilter currentFilter, final ProxyFilterType filterType) {
        if (currentFilter != null && currentFilter.getFilterType().getType() == filterType.getType()) {
            return currentFilter;
        } else {
            return new ProxyFilter(filterType);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isReceivedViaProxyFilter(@NonNull final Message message) {
        final ProxyFilter filter = mInternalTransportCallbacks.getProxyFilter();
        if (filter != null) {
            if (filter.getFilterType().getType() == ProxyFilterType.WHITE_LIST_FILTER) {
                return filterAddressMatches(filter, message.getDst());
            } else {
                return !filterAddressMatches(filter, message.getDst());
            }
        }
        return false;
    }

    private boolean filterAddressMatches(@NonNull final ProxyFilter filter, final int dst) {
        for (AddressArray addressArray : filter.getAddresses()) {
            final int address = MeshParserUtils.unsignedBytesToInt(addressArray.getAddress()[1], addressArray.getAddress()[0]);
            if (address == dst) {
                return true;
            }
        }
        return false;
    }

    private void createGroups(@NonNull final List<Integer> subscriptionAddresses) {
        final MeshNetwork network = mInternalTransportCallbacks.getMeshNetwork();
        for (Integer groupAddress : subscriptionAddresses) {
            Group group = network.getGroup(groupAddress);
            if (group == null) {
                group = new Group(groupAddress, network.getMeshUUID());
                group.setName("Unknown Group");
                network.getGroups().add(group);
            }
        }
    }
}
