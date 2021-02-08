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

package no.nordicsemi.android.nrfmesh.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.util.Comparator;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.Scene;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class Utils {

    public static final String EXTRA_DATA_PROVISIONING_SERVICE = "EXTRA_DATA_PROVISIONING_SERVICE";
    public static final String HEX_PATTERN = "^[0-9a-fA-F]+$";
    public static final String EXTRA_MODEL_ID = "EXTRA_MODEL_ID";
    public static final String EXTRA_ELEMENT_ADDRESS = "EXTRA_ELEMENT_ADDRESS";
    public static final String EXTRA_DATA_MODEL_NAME = "EXTRA_DATA_MODEL_NAME";

    public static final String RESULT_KEY_INDEX = "RESULT_KEY_INDEX";
    public static final String RESULT_KEY_LIST_SIZE = "RESULT_KEY_LIST_SIZE";
    public static final String EDIT_KEY = "EDIT_KEY";

    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
    public static final String ACTIVITY_RESULT = "RESULT_KEY";
    public static final String PROVISIONING_COMPLETED = "PROVISIONING_COMPLETED";
    public static final String PROVISIONER_UNASSIGNED = "PROVISIONER_UNASSIGNED";
    public static final String COMPOSITION_DATA_COMPLETED = "COMPOSITION_DATA_COMPLETED";
    public static final String DEFAULT_GET_COMPLETED = "DEFAULT_GET_COMPLETED";
    public static final String APP_KEY_ADD_COMPLETED = "APP_KEY_ADD_COMPLETED";
    public static final String NETWORK_TRANSMIT_SET_COMPLETED = "NETWORK_TRANSMIT_SET_COMPLETED";
    public static final String EXTRA_DATA = "EXTRA_DATA";
    private static final String PREFS_LOCATION_NOT_REQUIRED = "location_not_required";
    private static final String PREFS_PERMISSION_REQUESTED = "permission_requested";
    private static final String PREFS_READ_STORAGE_PERMISSION_REQUESTED = "read_storage_permission_requested";
    private static final String PREFS_WRITE_STORAGE_PERMISSION_REQUESTED = "write_storage_permission_requested";
    public static final int PROVISIONING_SUCCESS = 2112;
    public static final int CONNECT_TO_NETWORK = 2113;
    public static final String RESULT_KEY = "RESULT_KEY";
    private static final String APPLICATION_KEYS = "APPLICATION_KEYS";
    public static final String RANGE_TYPE = "RANGE_TYPE";
    public static final String DIALOG_FRAGMENT_KEY_STATUS = "DIALOG_FRAGMENT_KEY_STATUS";

    //Message timeout in case the message fails to lost/received
    public static final int MESSAGE_TIME_OUT = 10000;
    //Manage ranges
    public static final int UNICAST_RANGE = 0;
    public static final int GROUP_RANGE = 1;
    public static final int SCENE_RANGE = 2;

    //Manage net keys
    public static final int MANAGE_NET_KEY = 0;
    public static final int ADD_NET_KEY = 1;

    //Manage app keys
    public static final int MANAGE_APP_KEY = 2;
    public static final int ADD_APP_KEY = 3;
    public static final int BIND_APP_KEY = 4;
    public static final int PUBLICATION_APP_KEY = 5;
    public static final int STORE_SCENE = 6;
    public static final int SELECT_SCENE = 6;

    // Heartbeat publication key
    public static final int HEARTBEAT_SETTINGS_SET = 2022;
    public static final int HEARTBEAT_PUBLICATION_NET_KEY = 6;
    public static final int SELECT_KEY = 2011; //Random number

    public static final Comparator<NetworkKey> netKeyComparator = (key1, key2) -> Integer.compare(key1.getKeyIndex(), key2.getKeyIndex());

    public static final Comparator<ApplicationKey> appKeyComparator = (key1, key2) -> Integer.compare(key1.getKeyIndex(), key2.getKeyIndex());

    public static final Comparator<Scene> sceneComparator = (scene1, scene2) -> Integer.compare(scene1.getNumber(), scene2.getNumber());

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
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION); // This method should return false
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

    /**
     * Checks for required permissions.
     *
     * @return true if permissions are already granted, false otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isWriteExternalStoragePermissionsGranted(final Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * The first time an app requests a permission there is no 'Don't ask again' checkbox and
     * {@link ActivityCompat#shouldShowRequestPermissionRationale(Activity, String)} returns false.
     * This situation is similar to a permission being denied forever, so to distinguish both cases
     * a flag needs to be saved.
     *
     * @param context the context
     */
    public static void markWriteStoragePermissionRequested(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(PREFS_WRITE_STORAGE_PERMISSION_REQUESTED, true).apply();
    }

    /**
     * Returns true if write external permission has been requested at least twice and
     * user denied it, and checked 'Don't ask again'.
     *
     * @param activity the activity
     * @return true if permission has been denied and the popup will not come up any more, false otherwise
     */
    public static boolean isWriteExternalStoragePermissionDeniedForever(final Activity activity) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        return !isWriteExternalStoragePermissionsGranted(activity) // Location permission must be denied
                && preferences.getBoolean(PREFS_WRITE_STORAGE_PERMISSION_REQUESTED, false) // Permission must have been requested before
                && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE); // This method should return false
    }

    public static boolean isMarshmallowOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLollipopOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isKitkatOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isValidUint8(final int i) {
        return ((i & 0xFFFFFF00) == 0 || (i & 0xFFFFFF00) == 0xFFFFFF00);
    }

    public static boolean checkIfVersionIsOreoOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    @Nullable
    public static byte[] getServiceData(@NonNull final ScanResult result, @NonNull final UUID serviceUuid) {
        final ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord != null) {
            return scanRecord.getServiceData(new ParcelUuid((serviceUuid)));
        }
        return null;
    }
}
