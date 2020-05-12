package no.nordicsemi.android.mesh.transport;

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

/**
 * Class for de-serializing a list of elements stored in the Mesh Configuration Database
 */
public final class InternalElementListDeserializer implements JsonSerializer<List<Element>>, JsonDeserializer<List<Element>>, Type {
    @Override
    public List<Element> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final List<Element> elements = new ArrayList<>();
        if (json.isJsonArray()) {
            final JsonArray jsonArray = json.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                final JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                final int location = Integer.parseInt(jsonObject.get("location").getAsString(), 16);
                final List<MeshModel> models = deserializeModels(context, jsonObject);
                final Element element;
                if (jsonObject.has("name")) {
                    final String name = jsonObject.get("name").getAsString();
                    element = new Element(location, populateModels(models));
                    element.setName(name);
                } else {
                    element = new Element(location, populateModels(models));
                }
                elements.add(element);
            }
        }
        return elements;
    }

    @Override
    public JsonElement serialize(final List<Element> elements, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonArray jsonArray = new JsonArray();
        int i = 0;
        for (Element element : elements) {
            final JsonObject elementJson = new JsonObject();
            elementJson.addProperty("name", element.getName());
            elementJson.addProperty("index", i);
            elementJson.addProperty("location", String.format(Locale.US, "%04X", element.getLocationDescriptor()));
            elementJson.add("models", serializeModels(context, element.getMeshModels()));

            jsonArray.add(elementJson);
            i++;
        }
        return jsonArray;
    }

    /**
     * Returns serialized json element containing the mesh models
     *
     * @param context    Serializer context
     * @param meshModels models map
     * @return JsonElement
     */
    private JsonElement serializeModels(final JsonSerializationContext context, final Map<Integer, MeshModel> meshModels) {
        final Type meshModelList = new TypeToken<List<MeshModel>>() {
        }.getType();
        return context.serialize(populateModels(meshModels), meshModelList);
    }

    /**
     * Deserialize the mesh models
     *
     * @param context Json deserializer context
     * @param json    models json object
     * @return list of {@link MeshModel}
     */
    private List<MeshModel> deserializeModels(final JsonDeserializationContext context, final JsonObject json) {
        Type modelsList = new TypeToken<List<MeshModel>>() {
        }.getType();
        return context.deserialize(json.getAsJsonArray("models"), modelsList);
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

    private List<MeshModel> populateModels(final Map<Integer, MeshModel> meshModelMap) {
        final List<MeshModel> meshModels = new ArrayList<>();
        for (Map.Entry<Integer, MeshModel> modelEntry : meshModelMap.entrySet()) {
            meshModels.add(modelEntry.getValue());
        }
        return meshModels;
    }
}
