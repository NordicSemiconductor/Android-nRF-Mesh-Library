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
        for(int i = 0; i < jsonProvisioners.size(); i++) {
            final JsonObject jsonProvisioner = jsonProvisioners.get(i).getAsJsonObject();
            final String name = jsonProvisioner.get("provisionerName").getAsString();
            final String provisionerUuid = jsonProvisioner.get("UUID").getAsString();
            final List<AllocatedUnicastRange> unicastRanges = deserializeAllocatedUnicastRange(context, jsonProvisioner.get("allocatedUnicastRange").getAsJsonArray());
            final List<AllocatedGroupRange> groupRanges = deserializeAllocatedGroupRange(context, jsonProvisioner.get("allocatedGroupRange").getAsJsonArray());
            final List<AllocatedSceneRange> sceneRanges = deserializeAllocatedSceneRange(context, jsonProvisioner);

            final Provisioner provisioner = new Provisioner(provisionerUuid, unicastRanges, groupRanges, sceneRanges, "");
            provisioner.setProvisionerName(name);
            provisioners.add(provisioner);
        }
        return provisioners;
    }

    @Override
    public JsonElement serialize(final List<Provisioner> src, final Type typeOfSrc, final JsonSerializationContext context) {
        return null;
    }

    /**
     * Returns a list of nodes de-serializing the json array containing the allocated unicast range list
     * @param context  deserializer context
     * @param json     json network object containing the provisioners
     */
    private List<AllocatedUnicastRange> deserializeAllocatedUnicastRange(final JsonDeserializationContext context, final JsonArray json) {
        final Type unicastRangeList = new TypeToken<List<AllocatedUnicastRange>>() {
        }.getType();
        return context.deserialize(json, unicastRangeList);
    }

    /**
     * Returns a list of nodes de-serializing the json array containing the allocated unicast range list
     * @param context  deserializer context
     * @param json     json network object containing the provisioners
     */
    private List<AllocatedGroupRange> deserializeAllocatedGroupRange(final JsonDeserializationContext context, final JsonArray json) {
        final Type groupRangeList = new TypeToken<List<AllocatedGroupRange>>() {
        }.getType();
        return context.deserialize(json, groupRangeList);
    }

    /**
     * Returns a list of nodes de-serializing the json array containing the allocated unicast range list
     *
     * @param context  deserializer context
     * @param json     json network object containing the provisioners
     */
    private List<AllocatedSceneRange> deserializeAllocatedSceneRange(final JsonDeserializationContext context, final JsonObject json) {
        if(!json.has("allocatedSceneRange"))
            return new ArrayList<>();
        final Type sceneRangeList = new TypeToken<List<AllocatedSceneRange>>() {
        }.getType();
        return context.deserialize(json.getAsJsonArray("allocatedSceneRange"), sceneRangeList);
    }
}
