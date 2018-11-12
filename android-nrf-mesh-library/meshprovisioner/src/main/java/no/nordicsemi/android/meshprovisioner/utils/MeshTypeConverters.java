package no.nordicsemi.android.meshprovisioner.utils;

import android.arch.persistence.room.TypeConverter;
import android.support.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class MeshTypeConverters {

    @TypeConverter
    public String addressesToJson(final List<byte[]> addresses){
        return new Gson().toJson(addresses).toString();
    }

    @TypeConverter
    public List<byte[]> fromJsonToAddresses(final String addressesJson){
        Type addresses = new TypeToken<List<byte[]>>() {
        }.getType();
        return new Gson().fromJson(addressesJson, addresses);
    }
}
