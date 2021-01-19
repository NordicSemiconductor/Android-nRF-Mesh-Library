package no.nordicsemi.android.mesh;

import android.util.SparseIntArray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.room.TypeConverter;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.ElementDbMigrator;
import no.nordicsemi.android.mesh.transport.InternalMeshModelDeserializer;
import no.nordicsemi.android.mesh.transport.MeshModel;

@SuppressWarnings("WeakerAccess")
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class MeshTypeConverters {

    @TypeConverter
    public static Map<Integer, ApplicationKey> fromJsonToAddedAppKeys(final String appKeyJson) {
        final Type addedAppKeys = new TypeToken<Map<Integer, ApplicationKey>>() {
        }.getType();
        return new Gson().fromJson(appKeyJson, addedAppKeys);
    }

    @TypeConverter
    public static List<NetworkKey> fromJsonToAddedNetKeys(final String networkKeyJson) {
        final Type addedNetKeys = new TypeToken<List<NetworkKey>>() {
        }.getType();
        return new Gson().fromJson(networkKeyJson, addedNetKeys);
    }

    @TypeConverter
    public static String elementsToJson(final Map<Integer, Element> elements) {
        return new Gson().toJson(elements);
    }

    @TypeConverter
    public Map<Integer, Element> fromJsonToElements(final String elementsJson) {
        final Type elements = new TypeToken<Map<Integer, Element>>() {
        }.getType();
        return new GsonBuilder().
                excludeFieldsWithoutExposeAnnotation().
                registerTypeAdapter(Element.class, new ElementDbMigrator()).
                registerTypeAdapter(MeshModel.class, new InternalMeshModelDeserializer()).
                create().fromJson(elementsJson, elements);
    }

    @TypeConverter
    public static String allocatedGroupRangeToJson(final List<AllocatedGroupRange> ranges) {
        return new Gson().toJson(ranges);
    }

    @TypeConverter
    public static List<AllocatedGroupRange> fromJsonToAllocatedGroupRanges(final String rangesJson) {
        final Type ranges = new TypeToken<List<AllocatedGroupRange>>() {
        }.getType();
        return new Gson().fromJson(rangesJson, ranges);
    }

    @TypeConverter
    public static String allocatedSceneRangeToJson(final List<AllocatedSceneRange> ranges) {
        return new Gson().toJson(ranges);
    }

    @TypeConverter
    public static List<AllocatedSceneRange> fromJsonToAllocatedSceneRanges(final String rangesJson) {
        final Type ranges = new TypeToken<List<AllocatedSceneRange>>() {
        }.getType();
        return new Gson().fromJson(rangesJson, ranges);
    }

    @TypeConverter
    public static String allocatedUnicastRangeToJson(final List<AllocatedUnicastRange> ranges) {
        return new Gson().toJson(ranges);
    }

    @TypeConverter
    public static List<AllocatedUnicastRange> fromJsonToAllocatedUnicastRanges(final String rangesJson) {
        Type ranges = new TypeToken<List<AllocatedUnicastRange>>() {
        }.getType();
        return new Gson().fromJson(rangesJson, ranges);
    }

    @TypeConverter
    public static String integerToJson(final List<Integer> integerList) {
        return new Gson().toJson(integerList);
    }

    @TypeConverter
    public static List<Integer> fromJsonToIntegerList(final String integerListJson) {
        final Type addresses = new TypeToken<List<Integer>>() {
        }.getType();
        return new Gson().fromJson(integerListJson, addresses);
    }

    @TypeConverter
    public String uuidToJson(final UUID uuid) {
        if (uuid == null)
            return null;
        return new Gson().toJson(uuid.toString());
    }

    @TypeConverter
    public UUID fromJsonToUuid(final String addressesJson) {
        final Type uuid = new TypeToken<UUID>() {
        }.getType();
        return new Gson().fromJson(addressesJson, uuid);
    }

    @TypeConverter
    public static String sparseIntArrayToJson(@NonNull final SparseIntArray array) {
        return new Gson().toJson(array);
    }

    @TypeConverter
    public static SparseIntArray fromJsonToSparseIntArray(final String integerListJson) {
        final Type addresses = new TypeToken<SparseIntArray>() {
        }.getType();
        return new Gson().fromJson(integerListJson, addresses);
    }

    @TypeConverter
    public static String nodeKeysToJson(@NonNull final List<NodeKey> nodeKeys) {
        return new Gson().toJson(nodeKeys);
    }

    @TypeConverter
    public static List<NodeKey> fromJsonToNodeKeys(final String nodeKeys) {
        final Type keys = new TypeToken<List<NodeKey>>() {
        }.getType();
        return new Gson().fromJson(nodeKeys, keys);
    }

    @TypeConverter
    public static String ivIndexToJson(@NonNull final IvIndex ivIndex) {
        return new Gson().toJson(ivIndex);
    }

    @TypeConverter
    public static IvIndex fromJsonToIvIndex(final String ivIndex) {
        final Type newIvIndex = new TypeToken<IvIndex>() {
        }.getType();
        return new Gson().fromJson(ivIndex, newIvIndex);
    }

    @TypeConverter
    public static String networkExclusionsToJson(@NonNull final Map<Integer, ArrayList<Integer>> networkExclusions) {
        return new Gson().toJson(networkExclusions);
    }

    @TypeConverter
    public static Map<Integer, ArrayList<Integer>> fromJsonToNetworkExclusions(@NonNull final String networkExclusions) {
        final Type newNetworkExclusions = new TypeToken<Map<Integer, ArrayList<Integer>>>() {
        }.getType();
        return new Gson().fromJson(networkExclusions, newNetworkExclusions);
    }
}
