package no.nordicsemi.android.meshprovisioner;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import no.nordicsemi.android.meshprovisioner.utils.NetworkSettings;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

public class ProvisioningSettings extends NetworkSettings {

    private static final String APPLICATION_KEYS = "APPLICATION_KEYS";
    private static final String PROVISIONING_DATA = "PROVISIONING_DATA";
    private static final String NETWORK_NAME = "NETWORK_NAME";
    private static final String NETWORK_KEY = "NETWORK_KEY";
    private static final String UNICAST_ADDRESS = "UNICAST_ADDRESS";
    private static final String KEY_INDEX = "KEY_INDEX";
    private static final String IV_INDEX = "IV_INDEX";
    private static final String FLAGS = "FLAGS";
    private static final String GLOBAL_TTL = "GLOBAL_TTL";
    private final Context mContext;
    private String selectedAppkey;

    ProvisioningSettings(final Context context) {
        this.mContext = context;
        generateProvisioningData();
        addAppKeys();
    }

    /**
     * Generates initial provisioning data
     */
    protected void generateProvisioningData() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        networkKey = preferences.getString(NETWORK_KEY, SecureUtils.generateRandomNetworkKey());
        unicastAddress = preferences.getInt(UNICAST_ADDRESS, 1);
        keyIndex = preferences.getInt(KEY_INDEX, 0);
        ivIndex = preferences.getInt(IV_INDEX, 0);
        flags = preferences.getInt(FLAGS, 0);
        globalTtl = preferences.getInt(GLOBAL_TTL, 5);
    }

    /**
     * Clear provisioning data
     */
    protected void clearProvisioningData() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    private void addAppKeys() {
        final SharedPreferences preferences = mContext.getSharedPreferences(APPLICATION_KEYS, Context.MODE_PRIVATE);
        final Map<String, ?> keys = preferences.getAll();
        if (!keys.isEmpty()) {
            for (int i = 0; i < keys.size(); i++) {
                appKeys.put(i, String.valueOf(keys.get(String.valueOf(i))));
            }
        } else {
            appKeys.put(0, SecureUtils.generateRandomApplicationKey().toUpperCase());
            appKeys.put(1, SecureUtils.generateRandomApplicationKey().toUpperCase());
            appKeys.put(2, SecureUtils.generateRandomApplicationKey().toUpperCase());
        }
        saveApplicationKeys();
    }

    public String getNetworkKey() {
        return networkKey;
    }

    public void setNetworkKey(final String networkKey) {
        this.networkKey = networkKey;
        saveNetowrkKey();
    }

    public Map<Integer, String> getAppKeys() {
        return appKeys;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(final int keyIndex) {
        this.keyIndex = keyIndex;
        saveKeyIndex();
    }

    public int getIvIndex() {
        return ivIndex;
    }

    public void setIvIndex(final int ivIndex) {
        this.ivIndex = ivIndex;
        saveIvIndex();
    }

    public int getUnicastAddress() {
        return unicastAddress;
    }

    public void setUnicastAddress(final int unicastAddress) {
        //TODO implement a unicast address database to ensure addresses are not missed out or misused
        this.unicastAddress = unicastAddress;
        saveUnicastAddress();
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(final int flags) {
        this.flags = flags;
        saveFlags();
    }

    public int getGlobalTtl() {
        return globalTtl;
    }

    public void setGlobalTtl(final int globalTtl) {
        this.globalTtl = globalTtl;
        saveGlobalTtl();
    }
    private void saveNetowrkKey() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(NETWORK_KEY, networkKey);
        editor.apply();
    }

    private void saveUnicastAddress() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(UNICAST_ADDRESS, unicastAddress);
        editor.apply();
    }

    private void saveKeyIndex() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_INDEX, keyIndex);
        editor.apply();
    }

    private void saveIvIndex() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(IV_INDEX, ivIndex);
        editor.apply();
    }

    private void saveFlags() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(FLAGS, flags);
        editor.apply();
    }

    private void saveGlobalTtl() {
        final SharedPreferences preferences = mContext.getSharedPreferences(PROVISIONING_DATA, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(GLOBAL_TTL, globalTtl);
        editor.apply();
    }

    private void saveApplicationKeys() {
        final SharedPreferences preferences = mContext.getSharedPreferences(APPLICATION_KEYS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0; i < appKeys.size(); i++) {
            editor.putString(String.valueOf(i), appKeys.get(i));
        }
        editor.apply();
    }
}
