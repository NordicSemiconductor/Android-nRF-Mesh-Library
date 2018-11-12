package no.nordicsemi.android.meshprovisioner.transport;

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

/**
 * Class for deserializing a list of elements stored in the Mesh Configuration Database
 */
public final class ElementListDeserializer implements JsonSerializer<List<Element>>, JsonDeserializer<List<Element>>, Type {
    @Override
    public List<Element> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final List<Element> elements = new ArrayList<>();
        final JsonArray jsonArray = json.getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            final JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            final int index = jsonObject.get("index").getAsInt();
            final int location = Integer.parseInt(jsonObject.get("location").getAsString(), 16);
            final List<MeshModel> models = deserializeModels(context, jsonObject);
            final Element element = new Element(location);
            element.meshModels = populateModels(models);

            elements.add(element);
        }
        return elements;
    }

    @Override
    public JsonElement serialize(final List<Element> src, final Type typeOfSrc, final JsonSerializationContext context) {
        return null;
    }

    /**
     * Deserialize the mesh models
     *
     * @param context Json deserializer context
     * @param json    models json object
     * @return list of {@link MeshModel}
     */
    private List<MeshModel> deserializeModels(final JsonDeserializationContext context, final JsonObject json) {
        Type modelsList = new TypeToken<List<MeshModel>>() {}.getType();
        return context.deserialize(json.getAsJsonArray("models"), modelsList);
    }

    /**
     * Populates the require map of {@link MeshModel} where key is the model identifier and model is the value
     *
     * @param models list of MeshModels
     * @return Map of mesh models
     */
    private Map<Integer, MeshModel> populateModels(final List<MeshModel> models) {
        final Map<Integer, MeshModel> meshModels = new LinkedHashMap<>();
        for (MeshModel model : models) {
            meshModels.put(model.getModelId(), model);
        }
        return meshModels;
    }
}
