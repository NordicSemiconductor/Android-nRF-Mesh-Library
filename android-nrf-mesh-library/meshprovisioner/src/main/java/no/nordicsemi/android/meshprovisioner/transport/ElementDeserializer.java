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

public final class ElementDeserializer implements JsonSerializer<Element>, JsonDeserializer<Element> {
    @Override
    public Element deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        final int index = jsonObject.get("index").getAsInt();
        final int location =  Integer.parseInt(jsonObject.get("location").getAsString(),16);

        return null;
    }

    @Override
    public JsonElement serialize(final Element src, final Type typeOfSrc, final JsonSerializationContext context) {
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
