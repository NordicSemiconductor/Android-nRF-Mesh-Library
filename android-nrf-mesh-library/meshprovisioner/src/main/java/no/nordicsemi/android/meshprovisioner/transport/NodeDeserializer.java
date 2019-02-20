package no.nordicsemi.android.meshprovisioner.transport;

import android.support.annotation.RestrictTo;

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
import java.util.Locale;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.Features;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.CompositionDataParser;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.NetworkTransmitSettings;
import no.nordicsemi.android.meshprovisioner.utils.RelaySettings;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class NodeDeserializer implements JsonSerializer<List<ProvisionedMeshNode>>, JsonDeserializer<List<ProvisionedMeshNode>> {

    @Override
    public List<ProvisionedMeshNode> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final List<ProvisionedMeshNode> nodes = new ArrayList<>();
        final JsonArray jsonArray = json.getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            final ProvisionedMeshNode node = new ProvisionedMeshNode();
            node.uuid = jsonObject.get("UUID").getAsString();
            node.deviceKey = MeshParserUtils.toByteArray(jsonObject.get("deviceKey").getAsString());
            final int unicastAddress = Integer.parseInt(jsonObject.get("unicastAddress").getAsString(), 16);
            node.unicastAddress = unicastAddress;
            final boolean security = jsonObject.get("security").getAsString().equals("high");
            node.security = security ? 1 : 0;
            node.mAddedNetworkKeyIndexes = deserializeNetKeyIndexes(jsonObject.get("netKeys").getAsJsonArray());
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

            if (jsonObject.has("defaultTTL")) {
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
                node.mAddedAppKeyIndexes = deserializeAppKeyIndexes(jsonObject.get("appKeys").getAsJsonArray());

            if (jsonObject.has("elements")) {
                final List<Element> elements = deserializeElements(context, jsonObject);
                final Map<Integer, Element> elementMap = populateElements(unicastAddress, elements);
                node.mElements.clear();
                node.mElements.putAll(elementMap);
            }

            if (jsonObject.has("blacklisted")) {
                node.setBlackListed(jsonObject.get("blacklisted").getAsBoolean());
            }

            node.nodeName = jsonObject.get("name").getAsString();
            node.numberOfElements = node.mElements.size();
            nodes.add(node);
        }

        return nodes;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public JsonElement serialize(final List<ProvisionedMeshNode> nodes, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonArray jsonArray = new JsonArray();
        for (ProvisionedMeshNode node : nodes) {
            final JsonObject nodeJson = new JsonObject();
            nodeJson.addProperty("UUID", node.getUuid());
            nodeJson.addProperty("deviceKey", MeshParserUtils.bytesToHex(node.getDeviceKey(), false));
            nodeJson.addProperty("unicastAddress", MeshParserUtils.bytesToHex(AddressUtils.getUnicastAddressBytes(node.getUnicastAddress()), false));
            nodeJson.addProperty("security", (node.getSecurity() == ProvisionedBaseMeshNode.HIGH) ? "high" : "low");
            nodeJson.add("netKeys", serializeNetKeyIndexes(node.getAddedNetworkKeys()));
            nodeJson.addProperty("configComplete", node.isConfigured());
            nodeJson.addProperty("name", node.getNodeName());

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

            if (node.getTtl() != null) {
                nodeJson.addProperty("ttl", node.getTtl());
            }

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
            if (node.getAddedApplicationKeys() != null) {
                nodeJson.add("appKeys", serializeAppKeyIndexes(node.mAddedApplicationKeys));
            }
            if (node.getElements() != null) {
                nodeJson.add("elements", serializeElements(context, node.getElements()));
            }

            nodeJson.addProperty("blacklisted", node.isBlackListed());
            jsonArray.add(nodeJson);
        }
        return jsonArray;
    }

    /**
     * Returns a json element containing the added netkeys for this node
     *
     * @param addedNetKeys added net keys
     * @return JsonElement
     */
    private JsonElement serializeNetKeyIndexes(final List<NetworkKey> addedNetKeys) {
        final JsonArray netKeyIndexes = new JsonArray();
        for (NetworkKey networkKey : addedNetKeys) {
            final JsonObject keyIndexJson = new JsonObject();
            keyIndexJson.addProperty("index", networkKey.getKeyIndex());
            netKeyIndexes.add(keyIndexJson);
            //TODO add updated property when key refresh support is added
        }
        return netKeyIndexes;
    }

    private List<Integer> deserializeNetKeyIndexes(final JsonArray jsonNetKeyIndexes) {
        List<Integer> netKeyIndexes = new ArrayList<>();
        for (int i = 0; i < jsonNetKeyIndexes.size(); i++) {
            final JsonObject jsonIndex = jsonNetKeyIndexes.get(i).getAsJsonObject();
            netKeyIndexes.add(jsonIndex.get("index").getAsInt());
            //TODO add updated property when key refresh support is added
        }
        return netKeyIndexes;
    }

    /**
     * Returns a json element containing the added appkeys for this node
     *
     * @param addedAppKeys added app keys
     * @return JsonElement
     */
    private JsonElement serializeAppKeyIndexes(final Map<Integer, ApplicationKey> addedAppKeys) {
        final JsonArray appKeyIndexes = new JsonArray();

        for (Map.Entry<Integer, ApplicationKey> keyEntry : addedAppKeys.entrySet()) {
            final JsonObject keyIndexJson = new JsonObject();
            keyIndexJson.addProperty("index", String.format(Locale.US, "%04X", keyEntry.getValue().getKeyIndex()));
            appKeyIndexes.add(keyIndexJson);
            //TODO add updated property when key refresh support is added
        }
        return appKeyIndexes;
    }

    private List<Integer> deserializeAppKeyIndexes(final JsonArray jsonAppKeyIndexes) {
        List<Integer> appKeyIndexes = new ArrayList<>();
        for (int i = 0; i < jsonAppKeyIndexes.size(); i++) {
            final JsonObject jsonIndex = jsonAppKeyIndexes.get(i).getAsJsonObject();
            appKeyIndexes.add(jsonIndex.get("index").getAsInt());
            //TODO add updated property when key refresh support is added
        }
        return appKeyIndexes;
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
     * @param json    elements json object
     */
    private List<Element> deserializeElements(final JsonDeserializationContext context, final JsonObject json) {
        Type elementList = new TypeToken<List<Element>>() {
        }.getType();
        return context.deserialize(json.getAsJsonArray("elements"), elementList);
    }

    /**
     * Populates the require map of {@link MeshModel} where key is the model identifier and model is the value
     *
     * @param unicastAddress unicast address of the node
     * @param elementsList   list of MeshModels
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
            elements.put(element.getElementAddress(), element);
        }
        return elements;
    }

    private List<Element> populateElements(final Map<Integer, Element> elementMap) {
        final List<Element> elements = new ArrayList<>();
        for (Map.Entry<Integer, Element> elementEntry : elementMap.entrySet()) {
            elements.add(elementEntry.getValue());
        }
        return elements;
    }
}
