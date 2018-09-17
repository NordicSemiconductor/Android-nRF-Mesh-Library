/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrfmeshprovisioner.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.Map;

import no.nordicsemi.android.nrfmeshprovisioner.service.MeshService;

public class Utils {

    public static final String HEX_PATTERN = "^[0-9a-fA-F]+$";

    public static final String ACTION_CONNECT_TO_UNPROVISIONED_NODE = "ACTION_CONNECT_TO_UNPROVISIONED_NODE";
    public static final String ACTION_CONNECT_TO_MESH_NETWORK = "ACTION_CONNECT_TO_MESH_NETWORK";
    public static final String ACTION_IS_CONNECTED = "ACTION_IS_CONNECTED";
    public static final String ACTION_ON_DEVICE_READY = "ACTION_ON_DEVICE_READY";
    public static final String ACTION_CONNECTION_STATE = "ACTION_CONNECTION_STATE";
    public static final String ACTION_IS_RECONNECTING = "ACTION_IS_RECONNECTING";
    public static final String ACTION_PROVISIONING_STATE = "ACTION_PROVISIONING_STATE";
    public static final String ACTION_CONFIGURATION_STATE = "ACTION_CONFIGURATION_STATE";
    public static final String ACTION_GENERIC_STATE = "ACTION_GENERIC_STATE";
    public static final String ACTION_GENERIC_ON_OFF_STATE = "ACTION_GENERIC_ON_OFF_STATE";

    public static final String ACTION_VENDOR_MODEL_MESSAGE = "ACTION_VENDOR_MODEL_MESSAGE";
    public static final String ACTION_VENDOR_MODEL_MESSAGE_STATE = "ACTION_VENDOR_MODEL_MESSAGE_STATE";

    public static final String ACTION_TRANSACTION_STATE = "ACTION_TRANSACTION_STATE";
    public static final String ACTION_UPDATE_PROVISIONED_NODES = "ACTION_UPDATE_PROVISIONED_NODES";

    public static final String EXTRA_DATA = "EXTRA_DATA";
    public static final String EXTRA_PROVISIONING_STATE = "EXTRA_PROVISIONING_STATE";
    public static final String EXTRA_CONFIGURATION_STATE = "EXTRA_CONFIGURATION_STATE";

    public static final String EXTRA_GENERIC_ON_OFF_GET = "EXTRA_GENERIC_ON_OFF_GET";
    public static final String EXTRA_GENERIC_ON_OFF_SET = "EXTRA_GENERIC_ON_OFF_SET";
    public static final String EXTRA_GENERIC_ON_OFF_SET_UNACK = "EXTRA_GENERIC_ON_OFF_SET_UNACK";
    public static final String EXTRA_GENERIC_ON_OFF_STATE = "EXTRA_GENERIC_ON_OFF_STATE";

    public static final String EXTRA_STATUS = "EXTRA_STATUS";
    public static final String EXTRA_IS_SUCCESS = "EXTRA_IS_SUCCESS";
    public static final String EXTRA_NET_KEY_INDEX = "EXTRA_APP_KEY_INDEX";
    public static final String EXTRA_APP_KEY_INDEX = "EXTRA_APP_KEY_INDEX";
    public static final String EXTRA_PUBLISH_ADDRESS = "EXTRA_PUBLISH_ADDRESS";
    public static final String EXTRA_SUBSCRIPTION_ADDRESS = "EXTRA_SUBSCRIPTION_ADDRESS";
    public static final String EXTRA_MODEL_ID = "EXTRA_MODEL_ID";
    public static final String EXTRA_ELEMENT_ADDRESS = "EXTRA_ELEMENT_ADDRESS";
    public static final String EXTRA_DATA_MODEL_NAME = "EXTRA_DATA_MODEL_NAME";

    public static final String EXTRA_DATA_NODE_RESET = "EXTRA_DATA_NODE_RESET";
    public static final String EXTRA_DATA_NODE_RESET_STATUS = "EXTRA_DATA_NODE_RESET_STATUS";

    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
    public static final String ACTIVITY_RESULT = "RESULT_APP_KEY";
    private static final String PREFS_LOCATION_NOT_REQUIRED = "location_not_required";
    private static final String PREFS_PERMISSION_REQUESTED = "permission_requested";
    public static final int PROVISIONING_SUCCESS = 2112;
    private static final String PROVISIONED_NODES_FILE = "PROVISIONED_FILES";
    private static final String APPLICATION_KEYS = "APPLICATION_KEYS";
    public static String EXTRA_UNICAST_ADDRESS = "EXTRA_UNICAST_ADDRESS";
    public static String ACTION_ADD_APP_KEY = "ACTION_ADD_APP_KEY";
    public static String ACTION_DELETE_APP_KEY = "ACTION_DELETE_APP_KEY";
    public static String ACTION_VIEW_APP_KEY = "ACTION_VIEW_APP_KEY";


    public static final String EXTRA_GENERIC_ON_OFF_PRESENT_STATE = "EXTRA_GENERIC_ON_OFF_PRESENT_STATE";
    public static final String EXTRA_GENERIC_ON_OFF_TARGET_STATE = "EXTRA_GENERIC_ON_OFF_TARGET_STATE";
    public static final String EXTRA_GENERIC_ON_OFF_TRANSITION_STEPS = "EXTRA_GENERIC_ON_OFF_TRANSITION_STEPS";
    public static final String EXTRA_GENERIC_ON_OFF_TRANSITION_RES = "EXTRA_GENERIC_ON_OFF_TRANSITION_RES";

    /**
     * Checks whether Bluetooth is enabled.
     *
     * @return true if Bluetooth is enabled, false otherwise.
     */
    public static boolean isBleEnabled() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    /**
     * Checks for required permissions.
     *
     * @return true if permissions are already granted, false otherwise.
     */
    public static boolean isLocationPermissionsGranted(final Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Returns true if location permission has been requested at least twice and
     * user denied it, and checked 'Don't ask again'.
     *
     * @param activity the activity
     * @return true if permission has been denied and the popup will not come up any more, false otherwise
     */
    public static boolean isLocationPermissionDeniedForever(final Activity activity) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        return !isLocationPermissionsGranted(activity) // Location permission must be denied
                && preferences.getBoolean(PREFS_PERMISSION_REQUESTED, false) // Permission must have been requested before
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION); // This method should return false
    }

    /**
     * On some devices running Android Marshmallow or newer location services must be enabled in order to scan for Bluetooth LE devices.
     * This method returns whether the Location has been enabled or not.
     *
     * @return true on Android 6.0+ if location mode is different than LOCATION_MODE_OFF. It always returns true on Android versions prior to Marshmallow.
     */
    public static boolean isLocationEnabled(final Context context) {
        if (isMarshmallowOrAbove()) {
            int locationMode = Settings.Secure.LOCATION_MODE_OFF;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (final Settings.SettingNotFoundException e) {
                // do nothing
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        return true;
    }

    /**
     * Location enabled is required on some phones running Android Marshmallow or newer (for example on Nexus and Pixel devices).
     *
     * @param context the context
     * @return false if it is known that location is not required, true otherwise
     */
    public static boolean isLocationRequired(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREFS_LOCATION_NOT_REQUIRED, isMarshmallowOrAbove());
    }

    /**
     * When a Bluetooth LE packet is received while Location is disabled it means that Location
     * is not required on this device in order to scan for LE devices. This is a case of Samsung phones, for example.
     * Save this information for the future to keep the Location info hidden.
     *
     * @param context the context
     */
    public static void markLocationNotRequired(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(PREFS_LOCATION_NOT_REQUIRED, false).apply();
    }

    /**
     * The first time an app requests a permission there is no 'Don't ask again' checkbox and
     * {@link ActivityCompat#shouldShowRequestPermissionRationale(Activity, String)} returns false.
     * This situation is similar to a permission being denied forever, so to distinguish both cases
     * a flag needs to be saved.
     *
     * @param context the context
     */
    public static void markLocationPermissionRequested(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(PREFS_PERMISSION_REQUESTED, true).apply();
    }

    public static boolean isMarshmallowOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLollipopOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static void showToast(final Context context, final String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean isValidUint8(final int i) {
        return ((i & 0xFFFFFF00) == 0 || (i & 0xFFFFFF00) == 0xFFFFFF00);
    }

    public static void saveApplicationKeys(final Context context, final Map<Integer, String> appKeys) {
        SharedPreferences preferences = context.getSharedPreferences(APPLICATION_KEYS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0; i < appKeys.size(); i++) {
            editor.putString(String.valueOf(i), appKeys.get(i));
        }
        editor.commit();
    }

    public static int getKey(final Map<Integer, String> appKeys, final String appKey) {
        for (Map.Entry<Integer, String> entry : appKeys.entrySet()) {
            if (entry.getValue().equals(appKey)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    /**
     * Create the intent filters to listen for events on the {@link MeshService}
     *
     * @return intent filter
     */
    public static IntentFilter createIntentFilters() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CONNECTION_STATE);
        intentFilter.addAction(ACTION_IS_CONNECTED);
        intentFilter.addAction(ACTION_IS_RECONNECTING);
        intentFilter.addAction(ACTION_ON_DEVICE_READY);
        intentFilter.addAction(ACTION_PROVISIONING_STATE);
        intentFilter.addAction(ACTION_CONFIGURATION_STATE);
        intentFilter.addAction(ACTION_TRANSACTION_STATE);
        intentFilter.addAction(ACTION_GENERIC_ON_OFF_STATE);
        intentFilter.addAction(ACTION_VENDOR_MODEL_MESSAGE_STATE);
        return intentFilter;
    }

    public static boolean checkIfVersionIsOreoOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
}
