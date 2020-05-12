package no.nordicsemi.android.mesh;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.mesh.utils.MeshAddress;

final class AllocatedGroupRangeDeserializer implements JsonSerializer<List<AllocatedGroupRange>>, JsonDeserializer<List<AllocatedGroupRange>> {
    private static final String TAG = AllocatedGroupRangeDeserializer.class.getSimpleName();

    @Override
    public List<AllocatedGroupRange> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final List<AllocatedGroupRange> groupRanges = new ArrayList<>();
        try {
            final JsonArray jsonObject = json.getAsJsonArray();
            for (int i = 0; i < jsonObject.size(); i++) {
                final JsonObject unicastRangeJson = jsonObject.get(i).getAsJsonObject();
                final int lowAddress = Integer.parseInt(unicastRangeJson.get("lowAddress").getAsString(), 16);
                final int highAddress = Integer.parseInt(unicastRangeJson.get("highAddress").getAsString(), 16);
                groupRanges.add(new AllocatedGroupRange(lowAddress, highAddress));
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error while de-serializing Allocated group range: " + ex.getMessage());
        }
        return groupRanges;
    }

    @Override
    public JsonElement serialize(final List<AllocatedGroupRange> ranges, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonArray jsonArray = new JsonArray();
        for (AllocatedGroupRange range : ranges) {
            final JsonObject rangeJson = new JsonObject();
            rangeJson.addProperty("lowAddress", MeshAddress.formatAddress(range.getLowAddress(), false));
            rangeJson.addProperty("highAddress", MeshAddress.formatAddress(range.getHighAddress(), false));
            jsonArray.add(rangeJson);
        }
        return jsonArray;
    }
}
