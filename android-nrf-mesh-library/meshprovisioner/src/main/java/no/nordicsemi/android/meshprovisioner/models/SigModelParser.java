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

package no.nordicsemi.android.meshprovisioner.models;

import android.util.Log;

import java.util.Locale;

public class SigModelParser {
    private static final String TAG = SigModelParser.class.getSimpleName();

    private static final short CONFIGURATION_SERVER = 0x0000;
    private static final short CONFIGURATION_CLIENT = 0x0001;
    private static final short HEALTH_SERVER_MODEL = 0x0002;
    private static final short HEALTH_CLIENT_MODEL = 0x0003;

    private static final short GENERIC_ON_OFF_SERVER = 0x1000;
    private static final short GENERIC_ON_OFF_CLIENT = 0x1001;
    private static final short GENERIC_LEVEL_SERVER = 0x1002;
    private static final short GENERIC_LEVEL_CLIENT = 0x1003;

    private static final short GENERIC_DEFAULT_TRANSITION_TIME_SERVER = 0x1004;
    private static final short GENERIC_DEFAULT_TRANSITION_TIME_CLIENT = 0x1005;
    private static final short GENERIC_POWER_ON_OFF_SERVER = 0x1006;
    private static final short GENERIC_POWER_ON_OFF_SETUP_SERVER = 0x1007;
    private static final short GENERIC_POWER_ON_OFF_CLIENT = 0x1008;
    private static final short GENERIC_POWER_LEVEL_SERVER = 0x1009;
    private static final short GENERIC_POWER_LEVEL_SETUP_SERVER = 0x100A;
    private static final short GENERIC_POWER_LEVEL_CLIENT = 0x100B;
    private static final short GENERIC_BATTERY_SERVER = 0x100C;
    private static final short GENERIC_BATTERY_CLIENT = 0x100D;
    private static final short GENERIC_LOCATION_SERVER = 0x100E;
    private static final short GENERIC_LOCATION_SETUP_SERVER = 0x100F;
    private static final short GENERIC_LOCATION_CLIENT = 0x1010;
    private static final short GENERIC_ADMIN_PROPERTY_SERVER = 0x1011;
    private static final short GENERIC_MANUFACTURER_PROPERTY_SERVER = 0x1012;
    private static final short GENERIC_USER_PROPERTY_SERVER = 0x1013;
    private static final short GENERIC_CLIENT_PROPERTY_SERVER = 0x1014;
    private static final short GENERIC_PROPERTY_CLIENT = 0x1015;

    // SIG Sensors, Mesh Model Spec
    private static final short SENSOR_SERVER = 0x1100;
    private static final short SENSOR_SETUP_SERVER = 0x1101;
    private static final short SENSOR_CLIENT = 0x1102;

    //SIG Time and Scenes, Mesh Model Spec;
    private static final short TIME_SERVER = 0x1200;
    private static final short TIME_SETUP_SERVER = 0x1201;
    private static final short TIME_CLIENT = 0x1202;
    private static final short SCENE_SERVER = 0x1203;
    private static final short SCENE_SETUP_SERVER = 0x1204;
    private static final short SCENE_CLIENT = 0x1205;
    private static final short SCHEDULER_SERVER = 0x1206;
    private static final short SCHEDULER_SETUP_SERVER = 0x1207;
    private static final short SCHEDULER_CLIENT = 0x1208;

    // SIG Lightning, Mesh Model Spec
    private static final short LIGHT_LIGHTNESS_SERVER = 0x1300;
    private static final short LIGHT_LIGHTNESS_SETUP_SERVER = 0x1301;
    private static final short LIGHT_LIGHTNESS_CLIENT = 0x1302;
    private static final short LIGHT_CTL_SERVER = 0x1303;
    private static final short LIGHT_CTL_SETUP_SERVER = 0x1304;
    private static final short LIGHT_CTL_CLIENT = 0x1305;
    private static final short LIGHT_CTL_TEMPERATURE_SERVER = 0x1306;
    private static final short LIGHT_HSL_SERVER = 0x1307;
    private static final short LIGHT_HSL_SETUP_SERVER = 0x1308;
    private static final short LIGHT_HSL_CLIENT = 0x1309;
    private static final short LIGHT_HSL_HUE_SERVER = 0x130A;
    private static final short LIGHT_HSL_SATURATION_SERVER = 0x130B;
    private static final short LIGHT_XYL_SERVER = 0x130C;
    private static final short LIGHT_XYL_SETUP_SERVER = 0x130D;
    private static final short LIGHT_XYL_CLIENT = 0x130E;
    private static final short LIGHT_LC_SERVER = 0x130F;
    private static final short LIGHT_LC_SETUP_SERVER = 0x1310;
    private static final short LIGHT_LC_CLIENT = 0x1311;

    /**
     * Returns the Bluetooth sig model based on the model id.
     *
     * @param sigModelId bluetooth sig model id
     * @return SigModel
     */
    public static SigModel getSigModel(final int sigModelId) {
        switch (sigModelId) {
            case CONFIGURATION_SERVER:
                return new ConfigurationServerModel(sigModelId);
            case CONFIGURATION_CLIENT:
                return new ConfigurationClientModel(sigModelId);
            case HEALTH_SERVER_MODEL:
                return new HealthServerModel(sigModelId);
            case HEALTH_CLIENT_MODEL:
                return new HealthClientModel(sigModelId);
            case GENERIC_ON_OFF_SERVER:
                return new GenericOnOffServerModel(sigModelId);
            case GENERIC_ON_OFF_CLIENT:
                return new GenericOnOffClientModel(sigModelId);
            case GENERIC_LEVEL_SERVER:
                return new GenericLevelServerModel(sigModelId);
            case GENERIC_LEVEL_CLIENT:
                return new GenericLevelClientModel(sigModelId);
            case GENERIC_DEFAULT_TRANSITION_TIME_SERVER:
                return new GenericDefaultTransitionTimeServer(sigModelId);
            case GENERIC_DEFAULT_TRANSITION_TIME_CLIENT:
                return new GenericDefaultTransitionTimeClient(sigModelId);
            case GENERIC_POWER_ON_OFF_SERVER:
                return new GenericPowerOnOffServer(sigModelId);
            case GENERIC_POWER_ON_OFF_SETUP_SERVER:
                return new GenericPowerOnOffSetupServer(sigModelId);
            case GENERIC_POWER_ON_OFF_CLIENT:
                return new GenericPowerOnOffClient(sigModelId);
            case GENERIC_POWER_LEVEL_SERVER:
                return new GenericPowerLevelServer(sigModelId);
            case GENERIC_POWER_LEVEL_SETUP_SERVER:
                return new GenericPowerLevelSetupServer(sigModelId);
            case GENERIC_POWER_LEVEL_CLIENT:
                return new GenericPowerLevelClient(sigModelId);
            case GENERIC_BATTERY_SERVER:
                return new GenericBatteryServer(sigModelId);
            case GENERIC_BATTERY_CLIENT:
                return new GenericBatteryClient(sigModelId);
            case GENERIC_LOCATION_SERVER:
                return new GenericLocationServer(sigModelId);
            case GENERIC_LOCATION_SETUP_SERVER:
                return new GenericLocationSetupServer(sigModelId);
            case GENERIC_LOCATION_CLIENT:
                return new GenericLocationClient(sigModelId);
            case GENERIC_ADMIN_PROPERTY_SERVER:
                return new GenericAdminPropertyServer(sigModelId);
            case GENERIC_MANUFACTURER_PROPERTY_SERVER:
                return new GenericManufacturerPropertyServer(sigModelId);
            case GENERIC_USER_PROPERTY_SERVER:
                return new GenericUserPropertyServer(sigModelId);
            case GENERIC_CLIENT_PROPERTY_SERVER:
                return new GenericClientPropertyServer(sigModelId);
            case GENERIC_PROPERTY_CLIENT:
                return new GenericPropertyClient(sigModelId);
            case SENSOR_SERVER:
                return new SensorServer(sigModelId);
            case SENSOR_SETUP_SERVER:
                return new SensorSetupServer(sigModelId);
            case SENSOR_CLIENT:
                return new SensorClient(sigModelId);
            case TIME_SERVER:
                return new TimeServer(sigModelId);
            case TIME_SETUP_SERVER:
                return new TimeSetupServer(sigModelId);
            case TIME_CLIENT:
                return new TimeClient(sigModelId);
            case SCENE_SERVER:
                return new SceneServer(sigModelId);
            case SCENE_SETUP_SERVER:
                return new SceneSetupServer(sigModelId);
            case SCENE_CLIENT:
                return new SceneClient(sigModelId);
            case SCHEDULER_SERVER:
                return new SchedulerServer(sigModelId);
            case SCHEDULER_SETUP_SERVER:
                return new SchedulerSetupServer(sigModelId);
            case SCHEDULER_CLIENT:
                return new SchedulerClient(sigModelId);
            case LIGHT_LIGHTNESS_SERVER:
                return new LightLightnessServer(sigModelId);
            case LIGHT_LIGHTNESS_SETUP_SERVER:
                return new LightLightnessSetupServer(sigModelId);
            case LIGHT_LIGHTNESS_CLIENT:
                return new LightLightnessClient(sigModelId);
            case LIGHT_CTL_SERVER:
                return new LightCtlServer(sigModelId);
            case LIGHT_CTL_SETUP_SERVER:
                return new LightCtlSetupServer(sigModelId);
            case LIGHT_CTL_CLIENT:
                return new LightCtlClient(sigModelId);
            case LIGHT_CTL_TEMPERATURE_SERVER:
                return new LightCtlTemperatureServer(sigModelId);
            case LIGHT_HSL_SERVER:
                return new LightHslServer(sigModelId);
            case LIGHT_HSL_SETUP_SERVER:
                return new LightHslSetupServer(sigModelId);
            case LIGHT_HSL_CLIENT:
                return new LightHslClient(sigModelId);
            case LIGHT_HSL_HUE_SERVER:
                return new LightHslHueServer(sigModelId);
            case LIGHT_HSL_SATURATION_SERVER:
                return new LightHslSaturationServer(sigModelId);
            case LIGHT_XYL_SERVER:
                return new LightXylServer(sigModelId);
            case LIGHT_XYL_SETUP_SERVER:
                return new LightXylSetupServer(sigModelId);
            case LIGHT_XYL_CLIENT:
                return new LightXylClient(sigModelId);
            case LIGHT_LC_SERVER:
                return new LightLcServer(sigModelId);
            case LIGHT_LC_SETUP_SERVER:
                return new LightLcSetupServer(sigModelId);
            case LIGHT_LC_CLIENT:
                return new LightLightnessClient(sigModelId);
            default:
                Log.v(TAG, "Model ID: " + String.format(Locale.US, "%04X", sigModelId));
        }
        return null;
    }
}
