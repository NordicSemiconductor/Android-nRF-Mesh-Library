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
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.Features;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
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
            final int unicastAddress = Integer.parseInt(jsonObject.get("unicastAddress").getAsString(), 16);
            node.deviceKey = MeshParserUtils.toByteArray(jsonObject.get("deviceKey").getAsString());
            final boolean security = jsonObject.get("security").getAsString().equals("high");
            node.security = security ? 1 : 0;
            node.mAddedNetworkKeyIndexes = deserializeNetKeyIndexes(jsonObject.get("netKeys").getAsJsonArray());
            final boolean configComplete = jsonObject.get("configComplete").getAsBoolean();
            node.isConfigured = configComplete;
            if (configComplete) {
                node.companyIdentifier = Integer.parseInt(jsonObject.get("cid").getAsString(), 16);
                node.productIdentifier = Integer.parseInt(jsonObject.get("pid").getAsString(), 16);
                node.versionIdentifier = Integer.parseInt(jsonObject.get("vid").getAsString(), 16);
                node.crpl = Integer.parseInt(jsonObject.get("crpl").getAsString(), 16);

                if (jsonObject.has("features")) {
                    final JsonObject featuresJson = jsonObject.get("features").getAsJsonObject();
                    node.nodeFeatures = new Features(featuresJson.get("relay").getAsInt(),
                            featuresJson.get("proxy").getAsInt(),
                            featuresJson.get("friend").getAsInt(),
                            featuresJson.get("lowPower").getAsInt());
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

                node.mAddedAppKeyIndexes = deserializeAppKeyIndexes(jsonObject.get("appKeys").getAsJsonArray());

                final List<Element> elements = deserializeElements(context, jsonObject);
                final Map<Integer, Element> elementMap = populateElements(unicastAddress, elements);
                node.mElements.clear();
                node.mElements.putAll(elementMap);

                if (jsonObject.has("blacklisted")) {
                    node.setBlackListed(jsonObject.get("blacklisted").getAsBoolean());
                }
            }

            node.nodeName = jsonObject.get("name").getAsString();

            nodes.add(node);
        }

        return nodes;
    }

    @Override
    public JsonElement serialize(final List<ProvisionedMeshNode> src, final Type typeOfSrc, final JsonSerializationContext context) {
        return null;
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

    private List<Integer> deserializeAppKeyIndexes(final JsonArray jsonAppKeyIndexes) {
        List<Integer> appKeyIndexes = new ArrayList<>();
        for (int i = 0; i < jsonAppKeyIndexes.size(); i++) {
            final JsonObject jsonIndex = jsonAppKeyIndexes.get(i).getAsJsonObject();
            appKeyIndexes.add(jsonIndex.get("index").getAsInt());
            //TODO add updated property when key refresh support is added
        }
        return appKeyIndexes;
    }

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
        for (int i = 0; i < elementsList.size(); i++) {
            final Element element = elementsList.get(i);
            if (i == 0) {
                element.elementAddress = AddressUtils.getUnicastAddressBytes(unicastAddress);
            } else {
                element.elementAddress = AddressUtils.getUnicastAddressBytes(unicastAddress + 1);
            }
            elements.put(element.getElementAddressInt(), element);
        }
        return elements;
    }
}
