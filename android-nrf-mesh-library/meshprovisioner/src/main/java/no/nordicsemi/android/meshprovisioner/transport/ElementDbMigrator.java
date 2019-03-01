package no.nordicsemi.android.meshprovisioner.transport;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * Class for de-serializing a list of elements stored in the Mesh Configuration Database
 */
public final class ElementDbMigrator implements JsonDeserializer<Element>, Type {
    @Override
    public Element deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonElement = json.getAsJsonObject();
        final int address;
        if (jsonElement.get("elementAddress").isJsonArray()) {
            final JsonArray elementAddress = jsonElement.get("elementAddress").getAsJsonArray();
            if (elementAddress != null) {
                address = MeshParserUtils.unsignedBytesToInt(elementAddress.get(1).getAsByte(), elementAddress.get(0).getAsByte());
                final int location = Integer.parseInt(jsonElement.get("locationDescriptor").getAsString(), 16);
                return new Element(address, location, deserializeModels(context, jsonElement));
            }
        } else {
            address = jsonElement.get("elementAddress").getAsInt();
            final int location = Integer.parseInt(jsonElement.get("locationDescriptor").getAsString(), 16);
            return new Element(address, location, deserializeModels(context, jsonElement));
        }
        return null;
    }

    /**
     * Deserialize the mesh models
     *
     * @param context Json deserializer context
     * @param json    models json object
     * @return list of {@link MeshModel}
     */
    private LinkedHashMap<Integer, MeshModel> deserializeModels(final JsonDeserializationContext context, final JsonObject json) {
        Type models = new TypeToken<LinkedHashMap<Integer, MeshModel>>() {
        }.getType();
        return context.deserialize(json.getAsJsonObject("meshModels"), models);
    }

    /**
     * Populates the require map of {@link MeshModel} where key is the model identifier and model is the value
     *
     * @param models list of MeshModels
     * @return Map of mesh models
     */
    private Map<Integer, MeshModel> populateModels(final List<MeshModel> models) {
        final LinkedHashMap<Integer, MeshModel> meshModels = new LinkedHashMap<>();
        for (MeshModel model : models) {
            meshModels.put(model.getModelId(), model);
        }
        return meshModels;
    }
}
