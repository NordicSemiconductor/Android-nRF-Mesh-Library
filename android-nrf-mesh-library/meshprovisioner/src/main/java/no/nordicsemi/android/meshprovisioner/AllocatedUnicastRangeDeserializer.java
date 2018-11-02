package no.nordicsemi.android.meshprovisioner;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

final class AllocatedUnicastRangeDeserializer implements JsonSerializer<AllocatedUnicastRange>, JsonDeserializer<AllocatedUnicastRange> {
    private static final String TAG = AllocatedUnicastRangeDeserializer.class.getSimpleName();

    @Override
    public JsonElement serialize(final AllocatedUnicastRange src, final Type typeOfSrc, final JsonSerializationContext context) {
        return null;
    }

    @Override
    public AllocatedUnicastRange deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        final int lowAddress = Integer.parseInt(jsonObject.get("lowAddress").getAsString(), 16);
        final int highAddress = Integer.parseInt(jsonObject.get("highAddress").getAsString(), 16);
        return new AllocatedUnicastRange(lowAddress, highAddress);
    }
}
