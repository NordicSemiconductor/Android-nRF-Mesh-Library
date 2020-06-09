package no.nordicsemi.android.mesh.transport;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import no.nordicsemi.android.mesh.Features;
import no.nordicsemi.android.mesh.NodeKey;
import no.nordicsemi.android.mesh.models.ConfigurationServerModel;
import no.nordicsemi.android.mesh.models.SigModelParser;
import no.nordicsemi.android.mesh.utils.CompositionDataParser;
import no.nordicsemi.android.mesh.utils.HeartbeatPublication;
import no.nordicsemi.android.mesh.utils.HeartbeatSubscription;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.mesh.utils.NetworkTransmitSettings;
import no.nordicsemi.android.mesh.utils.RelaySettings;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class NodeDeserializer implements JsonSerializer<List<ProvisionedMeshNode>>, JsonDeserializer<List<ProvisionedMeshNode>> {

    @Override
    public List<ProvisionedMeshNode> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final List<ProvisionedMeshNode> nodes = new ArrayList<>();
        final JsonArray jsonArray = json.getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            final ProvisionedMeshNode node = new ProvisionedMeshNode();
            final String uuid = MeshParserUtils.formatUuid(jsonObject.get("UUID").getAsString());
            if (uuid == null)
                throw new IllegalArgumentException("Invalid Mesh Provisioning/Configuration " +
                        "Database, invalid node UUID.");
            node.uuid = uuid;
            node.deviceKey = MeshParserUtils.toByteArray(jsonObject.get("deviceKey").getAsString());
            final int unicastAddress = Integer.parseInt(jsonObject.get("unicastAddress").getAsString(), 16);
            node.unicastAddress = unicastAddress;
            final boolean security = jsonObject.get("security").getAsString().equals("high");
            node.security = security ? 1 : 0;
            node.mAddedNetKeys = deserializeAddedIndexes(jsonObject.get("netKeys").getAsJsonArray());
            node.isConfigured = jsonObject.get("configComplete").getAsBoolean();

            if (jsonObject.has("cid"))
                node.companyIdentifier = Integer.parseInt(jsonObject.get("cid").getAsString(), 16);
            if (jsonObject.has("pid"))
                node.productIdentifier = Integer.parseInt(jsonObject.get("pid").getAsString(), 16);

            if (jsonObject.has("vid"))
                node.versionIdentifier = Integer.parseInt(jsonObject.get("vid").getAsString(), 16);

            if (jsonObject.has("crpl"))
                node.crpl = Integer.parseInt(jsonObject.get("crpl").getAsString(), 16);

            if (jsonObject.has("features")) {
                final JsonObject featuresJson = jsonObject.get("features").getAsJsonObject();
                node.nodeFeatures = new Features(featuresJson.get("friend").getAsInt(),
                        featuresJson.get("lowPower").getAsInt(),
                        featuresJson.get("relay").getAsInt(),
                        featuresJson.get("proxy").getAsInt());
            }

            if (jsonObject.has("secureNetworkBeacon")) {
                node.setSecureNetworkBeaconSupported(jsonObject.get("secureNetworkBeacon").getAsBoolean());
            }

            if (jsonObject.has("defaultTTL") && !jsonObject.get("defaultTTL").isJsonNull()) {
                node.ttl = jsonObject.get("defaultTTL").getAsInt();
            }

            if (jsonObject.has("networkTransmit")) {
                final JsonObject jsonNetTransmit = jsonObject.getAsJsonObject("networkTransmit");
                final NetworkTransmitSettings networkTransmitSettings =
                        new NetworkTransmitSettings(jsonNetTransmit.get("count").getAsInt(), jsonNetTransmit.get("interval").getAsInt());
                node.setNetworkTransmitSettings(networkTransmitSettings);
            }

            if (jsonObject.has("relayRetransmit")) {
                final JsonObject jsonRelay = jsonObject.getAsJsonObject("relayRetransmit");
                final RelaySettings relaySettings =
                        new RelaySettings(jsonRelay.get("count").getAsInt(), jsonRelay.get("interval").getAsInt());
                node.setRelaySettings(relaySettings);
            }

            if (jsonObject.has("appKeys"))
                node.mAddedAppKeys = deserializeAddedIndexes(jsonObject.get("appKeys").getAsJsonArray());

            if (jsonObject.has("elements")) {
                final List<Element> elements = deserializeElements(context, jsonObject);
                final Map<Integer, Element> elementMap = populateElements(unicastAddress, elements);
                node.mElements.clear();
                node.mElements.putAll(elementMap);
            }

            if (jsonObject.has("blacklisted")) {
                node.setBlackListed(jsonObject.get("blacklisted").getAsBoolean());
            }

            if (jsonObject.has("name"))
                node.nodeName = jsonObject.get("name").getAsString();
            deserializeHeartbeat(context, jsonObject, node);
            nodes.add(node);
        }

        return nodes;
    }

    @Override
    public JsonElement serialize(final List<ProvisionedMeshNode> nodes, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonArray jsonArray = new JsonArray();
        for (ProvisionedMeshNode node : nodes) {
            final JsonObject nodeJson = new JsonObject();
            nodeJson.addProperty("UUID", MeshParserUtils.uuidToHex(node.getUuid()));
            nodeJson.addProperty("name", node.getNodeName());
            nodeJson.addProperty("deviceKey", MeshParserUtils.bytesToHex(node.getDeviceKey(), false));
            nodeJson.addProperty("unicastAddress", MeshParserUtils.bytesToHex(MeshAddress.addressIntToBytes(node.getUnicastAddress()), false));
            nodeJson.addProperty("security", (node.getSecurity() == ProvisionedBaseMeshNode.HIGH) ? "high" : "low");
            nodeJson.addProperty("configComplete", node.isConfigured());

            if (node.getCompanyIdentifier() != null)
                nodeJson.addProperty("cid", CompositionDataParser.formatCompanyIdentifier(node.getCompanyIdentifier(), false));
            if (node.getProductIdentifier() != null)
                nodeJson.addProperty("pid", CompositionDataParser.formatProductIdentifier(node.getProductIdentifier(), false));
            if (node.getVersionIdentifier() != null)
                nodeJson.addProperty("vid", CompositionDataParser.formatVersionIdentifier(node.getVersionIdentifier(), false));
            if (node.getCrpl() != null)
                nodeJson.addProperty("crpl", CompositionDataParser.formatReplayProtectionCount(node.getCrpl(), false));

            if (node.getNodeFeatures() != null) {
                final JsonObject json = new JsonObject();
                json.addProperty("friend", node.getNodeFeatures().getFriend());
                json.addProperty("lowPower", node.getNodeFeatures().getLowPower());
                json.addProperty("proxy", node.getNodeFeatures().getProxy());
                json.addProperty("relay", node.getNodeFeatures().getRelay());
                nodeJson.add("features", json);
            }

            if (node.isSecureNetworkBeaconSupported() != null) {
                nodeJson.addProperty("secureNetworkBeacon", node.isSecureNetworkBeaconSupported());
            }

            nodeJson.addProperty("defaultTTL", node.getTtl());

            if (node.getNetworkTransmitSettings() != null) {
                final JsonObject json = new JsonObject();
                json.addProperty("count", node.getNetworkTransmitSettings().getNetworkTransmitCount());
                json.addProperty("interval", node.getNetworkTransmitSettings().getNetworkIntervalSteps());
                nodeJson.add("networkTransmit", json);
            }
            if (node.getRelaySettings() != null) {
                final JsonObject json = new JsonObject();
                json.addProperty("count", node.getRelaySettings().getRelayTransmitCount());
                json.addProperty("interval", node.getRelaySettings().getRelayIntervalSteps());
                nodeJson.add("relayRetransmit", json);
            }

            nodeJson.add("netKeys", serializeAddedIndexes(node.getAddedNetKeys()));
            nodeJson.add("appKeys", serializeAddedIndexes(node.getAddedAppKeys()));
            nodeJson.add("elements", serializeElements(context, node.getElements()));
            nodeJson.addProperty("blacklisted", node.isBlackListed());
            serializeHeartbeat(context, nodeJson, node);
            jsonArray.add(nodeJson);
        }
        return jsonArray;
    }

    /**
     * Returns a json element containing the added netkeys for this node
     *
     * @param addedKeyIndexes added net keys
     * @return JsonElement
     */
    private JsonElement serializeAddedIndexes(final List<NodeKey> addedKeyIndexes) {
        final JsonArray addedKeys = new JsonArray();
        for (NodeKey nodeKey : addedKeyIndexes) {
            final JsonObject keyIndexJson = new JsonObject();
            keyIndexJson.addProperty("index", nodeKey.getIndex());
            keyIndexJson.addProperty("updated", nodeKey.isUpdated());
            addedKeys.add(keyIndexJson);
        }
        return addedKeys;
    }

    private List<NodeKey> deserializeAddedIndexes(final JsonArray jsonNetKeyIndexes) {
        List<NodeKey> addedKeys = new ArrayList<>();
        for (int i = 0; i < jsonNetKeyIndexes.size(); i++) {
            final JsonObject jsonAddedKeys = jsonNetKeyIndexes.get(i).getAsJsonObject();
            final int index = jsonAddedKeys.get("index").getAsInt();
            boolean updated = false;
            if (jsonAddedKeys.has("updated")) {
                updated = jsonAddedKeys.get("updated").getAsBoolean();
            }
            addedKeys.add(new NodeKey(index, updated));
        }
        return addedKeys;
    }

    /**
     * Returns serialized json element containing the elements
     *
     * @param context     Serializer context
     * @param elementsMap elements map
     * @return JsonElement
     */
    private JsonElement serializeElements(final JsonSerializationContext context, final Map<Integer, Element> elementsMap) {
        final Type elementsList = new TypeToken<List<Element>>() {
        }.getType();
        return context.serialize(populateElements(elementsMap), elementsList);
    }

    /**
     * Returns a list of elements that contained in the json object elements
     *
     * @param context Deserializer context
     * @param json    Elements json object
     */
    private List<Element> deserializeElements(final JsonDeserializationContext context, final JsonObject json) {
        Type elementList = new TypeToken<List<Element>>() {
        }.getType();
        return context.deserialize(json.getAsJsonArray("elements"), elementList);
    }

    /**
     * Populates the require map of {@link MeshModel} where key is the model identifier and model is the value
     *
     * @param unicastAddress Unicast address of the node
     * @param elementsList   List of MeshModels
     * @return Map of mesh models
     */
    private Map<Integer, Element> populateElements(final int unicastAddress, final List<Element> elementsList) {
        final Map<Integer, Element> elements = new LinkedHashMap<>();
        int address = 0;
        for (int i = 0; i < elementsList.size(); i++) {
            final Element element = elementsList.get(i);
            if (i == 0) {
                address = unicastAddress;
                element.elementAddress = address;
            } else {
                address = address + 1;
                element.elementAddress = address;
            }
            if (TextUtils.isEmpty(element.name)) {
                element.name = "Element: " + MeshAddress.formatAddress(element.elementAddress, true);
            }
            elements.put(element.elementAddress, element);
        }
        return elements;
    }

    /**
     * Populate the elements
     *
     * @param elementMap Elements map
     */
    private List<Element> populateElements(final Map<Integer, Element> elementMap) {
        final List<Element> elements = new ArrayList<>();
        for (Map.Entry<Integer, Element> elementEntry : elementMap.entrySet()) {
            elements.add(elementEntry.getValue());
        }
        return elements;
    }

    /**
     * Deserialize Heartbeat settings
     *
     * @param context    Deserialization context
     * @param jsonObject JsonObject to deserialize from
     * @param node       MeshNode
     */
    private void deserializeHeartbeat(@NonNull final JsonDeserializationContext context,
                                      @NonNull final JsonObject jsonObject,
                                      @NonNull final ProvisionedMeshNode node) {
        final ConfigurationServerModel model = getConfigurationServerModel(node);
        if (model != null) {
            if (jsonObject.has("heartbeatPub")) {
                model.setHeartbeatPublication(context.deserialize(jsonObject.get("heartbeatPub"), HeartbeatPublication.class));
            }
            if (jsonObject.has("heartbeatSub")) {
                model.setHeartbeatSubscription(context.deserialize(jsonObject.get("heartbeatSub"), HeartbeatSubscription.class));
            }
        }
    }

    /**
     * Serialize Heartbeat settings.
     *
     * @param context    Serialization context
     * @param jsonObject Json object
     * @param node       MeshNode
     */
    private void serializeHeartbeat(@NonNull final JsonSerializationContext context,
                                    @NonNull final JsonObject jsonObject,
                                    @NonNull final ProvisionedMeshNode node) {
        final ConfigurationServerModel model = getConfigurationServerModel(node);
        if (model != null) {
            if (model.getHeartbeatPublication() != null) {
                final JsonObject result = context
                        .serialize(model.getHeartbeatPublication(), HeartbeatPublication.class)
                        .getAsJsonObject();
                result.remove("count");
                jsonObject.add("heartbeatPub", result);
            }
            if (model.getHeartbeatSubscription() != null) {
                final JsonObject result = context
                        .serialize(model.getHeartbeatSubscription(), HeartbeatSubscription.class)
                        .getAsJsonObject();
                result.remove("count");
                result.remove("minHops");
                result.remove("maxHops");
                jsonObject.add("heartbeatSub", result);
            }
        }
    }

    /**
     * Returns the configuration server model in the primary element of the node.
     *
     * @param node Mesh node
     */
    private ConfigurationServerModel getConfigurationServerModel(@NonNull final ProvisionedMeshNode node) {
        final Element element = node.getElements().get(node.getUnicastAddress());
        if (element != null) {
            final MeshModel meshModel = element.getMeshModels().get((int) SigModelParser.CONFIGURATION_SERVER);
            if (meshModel instanceof ConfigurationServerModel) {
                return (ConfigurationServerModel) meshModel;
            }
        }
        return null;
    }
}
