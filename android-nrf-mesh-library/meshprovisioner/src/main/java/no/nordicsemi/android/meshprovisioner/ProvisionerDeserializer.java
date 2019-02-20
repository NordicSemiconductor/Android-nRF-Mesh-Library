package no.nordicsemi.android.meshprovisioner;

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
import java.util.List;

public class ProvisionerDeserializer implements JsonSerializer<List<Provisioner>>, JsonDeserializer<List<Provisioner>> {
    @Override
    public List<Provisioner> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        List<Provisioner> provisioners = new ArrayList<>();
        final JsonArray jsonProvisioners = json.getAsJsonArray();
        for (int i = 0; i < jsonProvisioners.size(); i++) {
            final JsonObject jsonProvisioner = jsonProvisioners.get(i).getAsJsonObject();
            final String name = jsonProvisioner.get("provisionerName").getAsString();
            final String provisionerUuid = jsonProvisioner.get("UUID").getAsString();
            final List<AllocatedUnicastRange> unicastRanges = deserializeAllocatedUnicastRange(context, jsonProvisioner.get("allocatedUnicastRange").getAsJsonArray());
            List<AllocatedGroupRange> groupRanges = null;
            if (jsonProvisioner.has("allocatedGroupRange"))
                groupRanges = deserializeAllocatedGroupRange(context, jsonProvisioner.get("allocatedGroupRange").getAsJsonArray());

            List<AllocatedSceneRange> sceneRanges = null;
            if (jsonProvisioner.has("allocatedSceneRange"))
                sceneRanges = deserializeAllocatedSceneRange(context, jsonProvisioner);

            final Provisioner provisioner = new Provisioner(provisionerUuid, unicastRanges, groupRanges, sceneRanges, "");
            provisioner.setProvisionerName(name);
            provisioners.add(provisioner);
        }
        return provisioners;
    }

    @Override
    public JsonElement serialize(final List<Provisioner> provisioners, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonArray jsonArray = new JsonArray();
        for (Provisioner provisioner : provisioners) {
            final JsonObject provisionerJson = new JsonObject();
            provisionerJson.addProperty("provisionerName", provisioner.getProvisionerName());
            provisionerJson.addProperty("UUID", provisioner.getProvisionerUuid());
            provisionerJson.add("allocatedUnicastRange",
                    serializeAllocatedUnicastRanges(context, provisioner.getAllocatedUnicastRanges()));

            if (provisioner.getAllocatedGroupRanges() != null &&
                    !provisioner.getAllocatedGroupRanges().isEmpty())
                provisionerJson.add("allocatedGroupRange",
                        serializeAllocatedGroupRanges(context, provisioner.getAllocatedGroupRanges()));

            if (provisioner.getAllocatedSceneRanges() != null &&
                    !provisioner.getAllocatedSceneRanges().isEmpty())
                provisionerJson.add("allocatedSceneRange",
                        serializeAllocatedSceneRanges(context, provisioner.getAllocatedSceneRanges()));
            jsonArray.add(provisionerJson);
        }
        return jsonArray;
    }

    /**
     * Returns serialized json element containing the allocated unicast ranges
     *
     * @param context Serializer context
     * @param ranges  allocated group range
     */
    private JsonElement serializeAllocatedUnicastRanges(final JsonSerializationContext context, final List<AllocatedUnicastRange> ranges) {
        final Type allocatedUnicastRanges = new TypeToken<List<AllocatedUnicastRange>>() {
        }.getType();
        return context.serialize(ranges, allocatedUnicastRanges);
    }

    /**
     * Returns a list of allocated unicast ranges allocated to a provisioner
     *
     * @param context deserializer context
     * @param json    json network object containing the provisioners
     */
    private List<AllocatedUnicastRange> deserializeAllocatedUnicastRange(final JsonDeserializationContext context, final JsonArray json) {
        final Type unicastRangeList = new TypeToken<List<AllocatedUnicastRange>>() {
        }.getType();
        return context.deserialize(json, unicastRangeList);
    }

    /**
     * Returns serialized json element containing the allocated group ranges
     *
     * @param context Serializer context
     * @param ranges  allocated group range
     */
    private JsonElement serializeAllocatedGroupRanges(final JsonSerializationContext context, final List<AllocatedGroupRange> ranges) {
        final Type allocatedGroupRanges = new TypeToken<List<AllocatedGroupRange>>() {
        }.getType();
        return context.serialize(ranges, allocatedGroupRanges);
    }

    /**
     * Returns a list of nodes de-serializing the json array containing the allocated unicast range list
     *
     * @param context deserializer context
     * @param json    json network object containing the provisioners
     */
    private List<AllocatedGroupRange> deserializeAllocatedGroupRange(final JsonDeserializationContext context, final JsonArray json) {
        final Type groupRangeList = new TypeToken<List<AllocatedGroupRange>>() {
        }.getType();
        return context.deserialize(json, groupRangeList);
    }

    /**
     * Returns serialized json element containing the allocated scene ranges
     *
     * @param context Serializer context
     * @param ranges  Allocated scene range
     */
    private JsonElement serializeAllocatedSceneRanges(final JsonSerializationContext context, final List<AllocatedSceneRange> ranges) {
        final Type allocatedSceneRanges = new TypeToken<List<AllocatedSceneRange>>() {
        }.getType();
        return context.serialize(ranges, allocatedSceneRanges);
    }

    /**
     * Returns a list of nodes de-serializing the json array containing the allocated unicast range list
     *
     * @param context deserializer context
     * @param json    json network object containing the provisioners
     */
    private List<AllocatedSceneRange> deserializeAllocatedSceneRange(final JsonDeserializationContext context, final JsonObject json) {
        if (!json.has("allocatedSceneRange"))
            return new ArrayList<>();
        final Type sceneRangeList = new TypeToken<List<AllocatedSceneRange>>() {
        }.getType();
        return context.deserialize(json.getAsJsonArray("allocatedSceneRange"), sceneRangeList);
    }
}
