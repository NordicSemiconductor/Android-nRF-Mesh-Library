package no.nordicsemi.android.meshprovisioner.transport;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

public final class NodeDeserializer implements JsonSerializer<ProvisionedMeshNode>, JsonDeserializer<ProvisionedMeshNode> {
    @Override
    public ProvisionedMeshNode deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final ProvisionedMeshNode node = new ProvisionedMeshNode();
        JsonObject jsonObject = json.getAsJsonObject();
        final byte[] deviceKey = MeshParserUtils.toByteArray(jsonObject.get("deviceKey").getAsString());
        node.deviceKey = deviceKey;
        final int unicastAddress = Integer.parseInt(jsonObject.get("unicastAddress").getAsString(), 16);
        final boolean security = jsonObject.get("security").getAsString().equals("high");
        final boolean configComplete = jsonObject.get("configComplete").getAsBoolean();
        node.isConfigured = configComplete;

        if (configComplete) {
            final int cid = Integer.parseInt(jsonObject.get("cid").getAsString(), 16);
            node.companyIdentifier = cid;
            final int pid = Integer.parseInt(jsonObject.get("pid").getAsString(), 16);
            node.productIdentifier = pid;
            final int vid =  Integer.parseInt(jsonObject.get("vid").getAsString(), 16);
            node.versionIdentifier = vid;
            final int crpl =  Integer.parseInt(jsonObject.get("crpl").getAsString(), 16);
            node.crpl = crpl;

            final JsonObject featuresJson = jsonObject.get("features").getAsJsonObject();
            node.relayFeatureSupported = isSupported(featuresJson.get("relay").getAsInt());
            node.proxyFeatureSupported = isSupported(featuresJson.get("proxy").getAsInt());
            node.friendFeatureSupported = isSupported(featuresJson.get("friend").getAsInt());
            node.lowPowerFeatureSupported = isSupported(featuresJson.get("lowPower").getAsInt());
        }

        return null;
    }

    @Override
    public JsonElement serialize(final ProvisionedMeshNode src, final Type typeOfSrc, final JsonSerializationContext context) {
        return null;
    }

    private boolean isSupported(final int value) {
        switch (value) {
            case 0:
                return false;
            case 1:
                return true;
            case 2:
            default:
                return false;
        }
    }
}
