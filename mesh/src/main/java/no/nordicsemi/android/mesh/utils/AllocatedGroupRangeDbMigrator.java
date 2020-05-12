package no.nordicsemi.android.mesh.utils;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.mesh.AllocatedGroupRange;

/**
 * Migrator class to migrate allocated group range data
 */
final class AllocatedGroupRangeDbMigrator implements JsonDeserializer<List<AllocatedGroupRange>> {
    private static final String TAG = AllocatedGroupRangeDbMigrator.class.getSimpleName();

    @Override
    public List<AllocatedGroupRange> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final List<AllocatedGroupRange> groupRanges = new ArrayList<>();
        try {
            if(json.isJsonArray()) {
                final JsonArray jsonObject = json.getAsJsonArray();
                for (int i = 0; i < jsonObject.size(); i++) {
                    final JsonObject unicastRangeJson = jsonObject.get(i).getAsJsonObject();
                    final int lowAddress = unicastRangeJson.get("lowAddress").getAsInt();
                    final int highAddress = unicastRangeJson.get("highAddress").getAsInt();
                    groupRanges.add(new AllocatedGroupRange(lowAddress, highAddress));
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error while de-serializing Allocated group range: " + ex.getMessage());
        }
        return groupRanges;
    }
}
