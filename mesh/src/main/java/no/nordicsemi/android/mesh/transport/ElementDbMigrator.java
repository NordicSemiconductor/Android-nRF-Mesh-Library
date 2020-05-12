package no.nordicsemi.android.mesh.transport;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

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
            final String name;
            if (jsonElement.has("name")) {
                name = jsonElement.get("name").getAsString();
            } else {
                name = "Element: " + MeshAddress.formatAddress(address, true);
            }
            return new Element(address, location, deserializeModels(context, jsonElement), name);
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
}
