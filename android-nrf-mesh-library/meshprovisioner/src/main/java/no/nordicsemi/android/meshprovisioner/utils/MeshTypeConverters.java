package no.nordicsemi.android.meshprovisioner.utils;

import androidx.room.TypeConverter;
import androidx.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.AllocatedGroupRange;
import no.nordicsemi.android.meshprovisioner.AllocatedSceneRange;
import no.nordicsemi.android.meshprovisioner.AllocatedUnicastRange;
import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.InternalMeshModelDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class MeshTypeConverters {

    @TypeConverter
    public String addedAppKeysToJson(final Map<Integer, ApplicationKey> appKeys){
        return new Gson().toJson(appKeys);
    }

    @TypeConverter
    public Map<Integer, ApplicationKey> fromJsonToAddedAppKeys(final String appKeyJson){
        Type addedAppKeys = new TypeToken<Map<Integer, ApplicationKey>>() {
        }.getType();
        return new Gson().fromJson(appKeyJson, addedAppKeys);
    }

    @TypeConverter
    public String addedNetKeysToJson(final List<NetworkKey> networkKeys){
        return new Gson().toJson(networkKeys);
    }

    @TypeConverter
    public List<NetworkKey> fromJsonToAddedNetKeys(final String networkKeyJson){
        Type addedNetKeys = new TypeToken<List<NetworkKey>>() {
        }.getType();
        return new Gson().fromJson(networkKeyJson, addedNetKeys);
    }

    @TypeConverter
    public String elementsToJson(final Map<Integer, Element> elements){
        return new Gson().toJson(elements);
    }

    @TypeConverter
    public Map<Integer, Element> fromJsonToElements(final String elementsJson){
        Type elements = new TypeToken<Map<Integer, Element>>() {
        }.getType();
        return new GsonBuilder().
                excludeFieldsWithoutExposeAnnotation().
                registerTypeAdapter(MeshModel.class, new InternalMeshModelDeserializer()).
                create().fromJson(elementsJson, elements);
    }

    @TypeConverter
    public String allocatedGroupRangeToJson(final List<AllocatedGroupRange> ranges){
        return new Gson().toJson(ranges);
    }

    @TypeConverter
    public List<AllocatedGroupRange> fromJsonToAllocatedGroupRanges(final String rangesJson){
        Type ranges = new TypeToken<List<AllocatedGroupRange>>() {
        }.getType();
        return new Gson().fromJson(rangesJson, ranges);
    }

    @TypeConverter
    public String allocatedSceneRangeToJson(final List<AllocatedSceneRange> ranges){
        return new Gson().toJson(ranges);
    }

    @TypeConverter
    public List<AllocatedSceneRange> fromJsonToAllocatedSceneRanges(final String rangesJson){
        Type ranges = new TypeToken<List<AllocatedSceneRange>>() {
        }.getType();
        return new Gson().fromJson(rangesJson, ranges);
    }

    @TypeConverter
    public String allocatedUnicastRangeToJson(final List<AllocatedUnicastRange> ranges){
        return new Gson().toJson(ranges);
    }

    @TypeConverter
    public List<AllocatedUnicastRange> fromJsonToAllocatedUnicastRanges(final String rangesJson){
        Type ranges = new TypeToken<List<AllocatedUnicastRange>>() {
        }.getType();
        return new Gson().fromJson(rangesJson, ranges);
    }

    @TypeConverter
    public String addressesToJson(final List<byte[]> addresses){
        return new Gson().toJson(addresses);
    }

    @TypeConverter
    public List<byte[]> fromJsonToAddresses(final String addressesJson){
        Type addresses = new TypeToken<List<byte[]>>() {
        }.getType();
        return new Gson().fromJson(addressesJson, addresses);
    }
}
