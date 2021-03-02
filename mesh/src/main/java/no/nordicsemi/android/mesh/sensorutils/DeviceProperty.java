package no.nordicsemi.android.mesh.sensorutils;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.utils.SensorFormat;

/**
 * Device Property
 */
public enum DeviceProperty {

    AVERAGE_AMBIENT_TEMPERATURE_IN_A_PERIOD_OF_DAY((short) 0x0001),
    AVERAGE_INPUT_CURRENT((short) 0x0002),
    AVERAGE_INPUT_VOLTAGE((short) 0x0003),
    AVERAGE_OUTPUT_CURRENT((short) 0x0004),
    AVERAGE_OUTPUT_VOLTAGE((short) 0x0005),
    CENTER_BEAM_INTENSITY_AT_FULL_POWER((short) 0x0006),
    CHROMATICITY_TOLERANCE((short) 0x0007),
    COLOR_RENDERING_INDEX_R9((short) 0x0008),
    COLOR_RENDERING_INDEX_RA((short) 0x0009),
    DEVICE_APPEARANCE((short) 0x000A),
    DEVICE_COUNTRY_OF_ORIGIN((short) 0x000B),
    DEVICE_DATE_OF_MANUFACTURE((short) 0x000C),
    DEVICE_ENERGY_USE_SINCE_TURN_ON((short) 0x000D),
    DEVICE_FIRMWARE_REVISION((short) 0x000E),
    DEVICE_GLOBAL_TRADE_ITEM_NUMBER((short) 0x000F),
    DEVICE_HARDWARE_REVISION((short) 0x0010),
    DEVICE_MANUFACTURER_NAME((short) 0x0011),
    DEVICE_MODEL_NUMBER((short) 0x0012),
    DEVICE_OPERATING_TEMPERATURE_RANGE_SPECIFICATION((short) 0x0013),
    DEVICE_OPERATING_TEMPERATURE_STATISTICAL_VALUES((short) 0x0014),
    DEVICE_OVER_TEMPERATURE_EVENT_STATISTICS((short) 0x0015),
    DEVICE_POWER_RANGE_SPECIFICATION((short) 0x0016),
    DEVICE_RUN_TIME_SINCE_TURN_ON((short) 0x0017),
    DEVICE_RUNTIME_WARRANTY((short) 0x0018),
    DEVICE_SERIAL_NUMBER((short) 0x0019),
    DEVICE_SOFTWARE_REVISION((short) 0x001A),
    DEVICE_UNDER_TEMPERATURE_EVENT_STATISTICS((short) 0x001B),
    INDOOR_AMBIENT_TEMPERATURE_STATISTICAL_VALUES((short) 0x001C),
    INITIAL_CIE1931_CHROMATICITY_COORDINATES((short) 0x001D),
    INITIAL_CORRELATED_COLOR_TEMPERATURE((short) 0x001E),
    INITIAL_LUMINOUS_FLUX((short) 0x001F),
    INITIAL_PLANCKIAN_DISTANCE((short) 0x0020),
    INPUT_CURRENT_RANGE_SPECIFICATION((short) 0x0021),
    INPUT_CURRENT_STATISTICS((short) 0x0022),
    INPUT_OVER_CURRENT_EVENT_STATISTICS((short) 0x0023),
    INPUT_OVER_RIPPLE_VOLTAGE_EVENT_STATISTICS((short) 0x0024),
    INPUT_OVER_VOLTAGE_EVENT_STATISTICS((short) 0x0025),
    INPUT_UNDERCURRENT_EVENT_STATISTICS((short) 0x0026),
    INPUT_UNDER_VOLTAGE_EVENT_STATISTICS((short) 0x0027),
    INPUT_VOLTAGE_RANGE_SPECIFICATION((short) 0x0028),
    INPUT_VOLTAGE_RIPPLE_SPECIFICATION((short) 0x0029),
    INPUT_VOLTAGE_STATISTICS((short) 0x002A),
    LIGHT_CONTROL_AMBIENT_LUX_LEVEL_ON((short) 0x002B),
    LIGHT_CONTROL_AMBIENT_LUX_LEVEL_PROLONG((short) 0x002C),
    LIGHT_CONTROL_AMBIENT_LUX_LEVEL_STANDBY((short) 0x002D),
    LIGHT_CONTROL_LIGHTNESS_ON((short) 0x002E),
    LIGHT_CONTROL_LIGHTNESS_PROLONG((short) 0x002F),
    LIGHT_CONTROL_LIGHTNESS_STANDBY((short) 0x0030),
    LIGHT_CONTROL_REGULATOR_ACCURACY((short) 0x0031),
    LIGHT_CONTROL_REGULATOR_KID((short) 0x0032),
    LIGHT_CONTROL_REGULATOR_KIU((short) 0x0033),
    LIGHT_CONTROL_REGULATOR_KPD((short) 0x0034),
    LIGHT_CONTROL_REGULATOR_KPU((short) 0x0035),
    LIGHT_CONTROL_TIME_FADE((short) 0x0036),
    LIGHT_CONTROL_TIME_FADE_ON((short) 0x0037),
    LIGHT_CONTROL_TIME_FADE_STANDBY_AUTO((short) 0x0038),
    LIGHT_CONTROL_TIME_FADE_STANDBY_MANUAL((short) 0x0039),
    LIGHT_CONTROL_TIME_OCCUPANCY_DELAY((short) 0x003A),
    LIGHT_CONTROL_TIME_PROLONG((short) 0x003B),
    LIGHT_CONTROL_TIME_RUN_ON((short) 0x003C),
    LUMEN_MAINTENANCE_FACTOR((short) 0x003D),
    LUMINOUS_EFFICACY((short) 0x003E),
    LUMINOUS_ENERGY_SINCE_TURN_ON((short) 0x003F),
    LUMINOUS_EXPOSURE((short) 0x0040),
    LUMINOUS_FLUX_RANGE((short) 0x0041),
    MOTION_SENSED((short) 0x0042),
    MOTION_THRESHOLD((short) 0x0043),
    OPEN_CIRCUIT_EVENT_STATISTICS((short) 0x0044),
    OUTDOOR_STATISTICAL_VALUES((short) 0x0045),
    OUTPUT_CURRENT_RANGE((short) 0x0046),
    OUTPUT_CURRENT_STATISTICS((short) 0x0047),
    OUTPUT_RIPPLE_VOLTAGE_SPECIFICATION((short) 0x0048),
    OUTPUT_VOLTAGE_RANGE((short) 0x0049),
    OUTPUT_VOLTAGE_STATISTICS((short) 0x004A),
    OVER_OUTPUT_RIPPLE_VOLTAGE_EVENT_STATISTICS((short) 0x004B),
    PEOPLE_COUNT((short) 0x004C),
    PRESENCE_DETECTED((short) 0x004D),
    PRESENT_AMBIENT_LIGHT_LEVEL((short) 0x004E),
    PRESENT_AMBIENT_TEMPERATURE((short) 0x004F),
    PRESENT_CIE1931_CHROMATICITY_COORDINATES((short) 0x0050),
    PRESENT_CORRELATED_COLOR_TEMPERATURE((short) 0x0051),
    PRESENT_DEVICE_INPUT_POWER((short) 0x0052),
    PRESENT_DEVICE_OPERATING_EFFICIENCY((short) 0x0053),
    PRESENT_DEVICE_OPERATING_TEMPERATURE((short) 0x0054),
    PRESENT_ILLUMINANCE((short) 0x0055),
    PRESENT_INDOOR_AMBIENT_TEMPERATURE((short) 0x0056),
    PRESENT_INPUT_CURRENT((short) 0x0057),
    PRESENT_INPUT_RIPPLE_VOLTAGE((short) 0x0058),
    PRESENT_INPUT_VOLTAGE((short) 0x0059),
    PRESENT_LUMINOUS_FLUX((short) 0x005A),
    PRESENT_OUTDOOR_AMBIENT_TEMPERATURE((short) 0x005B),
    PRESENT_OUTPUT_CURRENT((short) 0x005C),
    PRESENT_OUTPUT_VOLTAGE((short) 0x005D),
    PRESENT_PLANCKIAN_DISTANCE((short) 0x005E),
    PRESENT_RELATIVE_OUTPUT_RIPPLE_VOLTAGE((short) 0x005F),
    RELATIVE_DEVICE_ENERGY_USE_IN_A_PERIOD_OF_DAY((short) 0x0060),
    RELATIVE_DEVICE_RUNTIME_IN_A_GENERIC_LEVEL_RANGE((short) 0x0061),
    RELATIVE_EXPOSURE_TIME_IN_AN_ILLUMINANCE_RANGE((short) 0x0062),
    RELATIVE_RUNTIME_IN_A_CORRELATED_COLOR_TEMPERATURE_RANGE((short) 0x0063),
    RELATIVE_RUNTIME_IN_A_DEVICE_OPERATING_TEMPERATURE_RANGE((short) 0x0064),
    RELATIVE_RUNTIME_IN_AN_INPUT_CURRENT_RANGE((short) 0x0065),
    RELATIVE_RUNTIME_IN_AN_INPUT_VOLTAGE_RANGE((short) 0x0066),
    SHORT_CIRCUIT_EVENT_STATISTICS((short) 0x0067),
    TIME_SINCE_MOTION_SENSED((short) 0x0068),
    TIME_SINCE_PRESENCE_DETECTED((short) 0x0069),
    TOTAL_DEVICE_ENERGY_USE((short) 0x006A),
    TOTAL_DEVICE_OFF_ON_CYCLES((short) 0x006B),
    TOTAL_DEVICE_POWER_ON_CYCLES((short) 0x006C),
    TOTAL_DEVICE_POWER_ON_TIME((short) 0x006D),
    TOTAL_DEVICE_RUNTIME((short) 0x006E),
    TOTAL_LIGHT_EXPOSURE_TIME((short) 0x006F),
    TOTAL_LUMINOUS_ENERGY((short) 0x0070),
    DESIRED_AMBIENT_TEMPERATURE((short) 0x0071),
    PRECISE_TOTAL_DEVICE_ENERGY_USE((short) 0x0072),
    POWER_FACTOR((short) 0x0073),
    SENSOR_GAIN((short) 0x0074),
    PRECISE_PRESENT_AMBIENT_TEMPERATURE((short) 0x0075),
    PRESENT_AMBIENT_RELATIVE_HUMIDITY((short) 0x0076),
    PRESENT_AMBIENT_CARBONDIOXIDE_CONCENTRATION((short) 0x0077),
    PRESENT_AMBIENT_VOLATILE_ORGANIC_COMPOUNDS_CONCENTRATION((short) 0x0078),
    PRESENT_AMBIENT_NOISE((short) 0x0079),
    // These are undefined in mesh device properties v2 = (short) 0x007A
    // These are undefined in mesh device properties v2 = (short) 0x007B
    // These are undefined in mesh device properties v2 = (short) 0x007C
    // These are undefined in mesh device properties v2 = (short) 0x007D
    // These are undefined in mesh device properties v2 = (short) 0x007E
    // These are undefined in mesh device properties v2 = (short) 0x007F
    ACTIVE_ENERGY_LOAD_SIDE((short) 0x0080),
    ACTIVE_POWER_LOAD_SIDE((short) 0x0081),
    AIR_PRESSURE((short) 0x0082),
    APPARENT_ENERGY((short) 0x0083),
    APPARENT_POWER((short) 0x0084),
    APPARENT_WIND_DIRECTION((short) 0x0085),
    APPARENT_WIND_SPEED((short) 0x0086),
    DEW_POINT((short) 0x0087),
    EXTERNAL_SUPPLY_VOLTAGE((short) 0x0088),
    EXTERNAL_SUPPLY_VOLTAGE_FREQUENCY((short) 0x0089),
    GUST_FACTOR((short) 0x008A),
    HEAT_INDEX((short) 0x008B),
    LIGHT_DISTRIBUTION((short) 0x008C),
    LIGHT_SOURCE_CURRENT((short) 0x008D),
    LIGHT_SOURCE_ON_TIME_NOT_RESETTABLE((short) 0x008E),
    LIGHT_SOURCE_ON_TIME_RESETTABLE((short) 0x008F),
    LIGHT_SOURCE_OPEN_CIRCUIT_STATISTICS((short) 0x0090),
    LIGHT_SOURCE_OVERALL_FAILURES_STATISTICS((short) 0x0091),
    LIGHT_SOURCE_SHORT_CIRCUIT_STATISTICS((short) 0x0092),
    LIGHT_SOURCE_START_COUNTER_RESETTABLE((short) 0x0093),
    LIGHT_SOURCE_TEMPERATURE((short) 0x0094),
    LIGHT_SOURCE_THERMAL_DERATING_STATISTICS((short) 0x0095),
    LIGHT_SOURCE_THERMAL_SHUTDOWN_STATISTICS((short) 0x0096),
    LIGHT_SOURCE_TOTAL_POWER_ON_CYCLES((short) 0x0097),
    LIGHT_SOURCE_VOLTAGE((short) 0x0098),
    LUMINAIRE_COLOR((short) 0x0099),
    LUMINAIRE_IDENTIFICATION_NUMBER((short) 0x009A),
    LUMINAIRE_MANUFACTURER_GTIN((short) 0x009B),
    LUMINAIRE_NOMINAL_INPUT_POWER((short) 0x009C),
    LUMINAIRE_NOMINAL_MAXIMUM_AC_MAINS_VOLTAGE((short) 0x009D),
    LUMINAIRE_NOMINAL_MINIMUM_AC_MAINS_VOLTAGE((short) 0x009E),
    LUMINAIRE_POWER_AT_MINIMUM_DIM_LEVEL((short) 0x009F),
    LUMINAIRE_TIME_OF_MANUFACTURE((short) 0x00A0),
    MAGNETIC_DECLINATION((short) 0x00A1),
    MAGNETIC_FLUX_DENSITY_2D((short) 0x00A2),
    MAGNETIC_FLUX_DENSITY_3D((short) 0x00A3),
    NOMINAL_LIGHT_OUTPUT((short) 0x00A4),
    OVERALL_FAILURE_CONDITION((short) 0x00A5),
    POLLEN_CONCENTRATION((short) 0x00A6),
    PRESENT_INDOOR_RELATIVE_HUMIDITY((short) 0x00A7),
    PRESENT_OUTDOOR_RELATIVE_HUMIDITY((short) 0x00A8),
    PRESSURE((short) 0x00A9),
    RAINFALL((short) 0x00AA),
    RATED_MEDIAN_USEFUL_LIFE_OF_LUMINAIRE((short) 0x00AB),
    RATED_MEDIAN_USEFUL_LIGHT_SOURCE_STARTS((short) 0x00AC),
    REFERENCE_TEMPERATURE((short) 0x00AD),
    TOTAL_DEVICE_STARTS((short) 0x00AE),
    TRUE_WIND_DIRECTION((short) 0x00AF),
    TRUE_WIND_SPEED((short) 0x00B0),
    UV_INDEX((short) 0x00B1),
    WIND_CHILL((short) 0x00B2),
    LIGHT_SOURCE_TYPE((short) 0x00B3),
    LUMINAIRE_IDENTIFICATION_STRING((short) 0x00B4),
    OUTPUT_POWER_LIMITATION((short) 0x00B5),
    THERMAL_DERATING((short) 0x00B6),
    OUTPUT_CURRENT_PERCENT((short) 0x00B7),
    UNKNOWN((short) 0xFFFF);

    private final short propertyId;

    DeviceProperty(final short property) {
        this.propertyId = property;
    }

    /**
     * Returns the static oob type value
     */
    public short getPropertyId() {
        return propertyId;
    }

    /**
     * Returns the Device Property for a given property id.
     *
     * @param propertyId property id
     */
    public static DeviceProperty from(final short propertyId) {
        switch (propertyId) {
            case 0x0001:
                return AVERAGE_AMBIENT_TEMPERATURE_IN_A_PERIOD_OF_DAY;
            case 0x0002:
                return AVERAGE_INPUT_CURRENT;
            case 0x0003:
                return AVERAGE_INPUT_VOLTAGE;
            case 0x0004:
                return AVERAGE_OUTPUT_CURRENT;
            case 0x0005:
                return AVERAGE_OUTPUT_VOLTAGE;
            case 0x0006:
                return CENTER_BEAM_INTENSITY_AT_FULL_POWER;
            case 0x0007:
                return CHROMATICITY_TOLERANCE;
            case 0x0008:
                return COLOR_RENDERING_INDEX_R9;
            case 0x0009:
                return COLOR_RENDERING_INDEX_RA;
            case 0x000A:
                return DEVICE_APPEARANCE;
            case 0x000B:
                return DEVICE_COUNTRY_OF_ORIGIN;
            case 0x000C:
                return DEVICE_DATE_OF_MANUFACTURE;
            case 0x000D:
                return DEVICE_ENERGY_USE_SINCE_TURN_ON;
            case 0x000E:
                return DEVICE_FIRMWARE_REVISION;
            case 0x000F:
                return DEVICE_GLOBAL_TRADE_ITEM_NUMBER;
            case 0x0010:
                return DEVICE_HARDWARE_REVISION;
            case 0x0011:
                return DEVICE_MANUFACTURER_NAME;
            case 0x0012:
                return DEVICE_MODEL_NUMBER;
            case 0x0013:
                return DEVICE_OPERATING_TEMPERATURE_RANGE_SPECIFICATION;
            case 0x0014:
                return DEVICE_OPERATING_TEMPERATURE_STATISTICAL_VALUES;
            case 0x0015:
                return DEVICE_OVER_TEMPERATURE_EVENT_STATISTICS;
            case 0x0016:
                return DEVICE_POWER_RANGE_SPECIFICATION;
            case 0x0017:
                return DEVICE_RUN_TIME_SINCE_TURN_ON;
            case 0x0018:
                return DEVICE_RUNTIME_WARRANTY;
            case 0x0019:
                return DEVICE_SERIAL_NUMBER;
            case 0x001A:
                return DEVICE_SOFTWARE_REVISION;
            case 0x001B:
                return DEVICE_UNDER_TEMPERATURE_EVENT_STATISTICS;
            case 0x001C:
                return INDOOR_AMBIENT_TEMPERATURE_STATISTICAL_VALUES;
            case 0x001D:
                return INITIAL_CIE1931_CHROMATICITY_COORDINATES;
            case 0x001E:
                return INITIAL_CORRELATED_COLOR_TEMPERATURE;
            case 0x001F:
                return INITIAL_LUMINOUS_FLUX;
            case 0x0020:
                return INITIAL_PLANCKIAN_DISTANCE;
            case 0x0021:
                return INPUT_CURRENT_RANGE_SPECIFICATION;
            case 0x0022:
                return INPUT_CURRENT_STATISTICS;
            case 0x0023:
                return INPUT_OVER_CURRENT_EVENT_STATISTICS;
            case 0x0024:
                return INPUT_OVER_RIPPLE_VOLTAGE_EVENT_STATISTICS;
            case 0x0025:
                return INPUT_OVER_VOLTAGE_EVENT_STATISTICS;
            case 0x0026:
                return INPUT_UNDERCURRENT_EVENT_STATISTICS;
            case 0x0027:
                return INPUT_UNDER_VOLTAGE_EVENT_STATISTICS;
            case 0x0028:
                return INPUT_VOLTAGE_RANGE_SPECIFICATION;
            case 0x0029:
                return INPUT_VOLTAGE_RIPPLE_SPECIFICATION;
            case 0x002A:
                return INPUT_VOLTAGE_STATISTICS;
            case 0x002B:
                return LIGHT_CONTROL_AMBIENT_LUX_LEVEL_ON;
            case 0x002C:
                return LIGHT_CONTROL_AMBIENT_LUX_LEVEL_PROLONG;
            case 0x002D:
                return LIGHT_CONTROL_AMBIENT_LUX_LEVEL_STANDBY;
            case 0x002E:
                return LIGHT_CONTROL_LIGHTNESS_ON;
            case 0x002F:
                return LIGHT_CONTROL_LIGHTNESS_PROLONG;
            case 0x0030:
                return LIGHT_CONTROL_LIGHTNESS_STANDBY;
            case 0x0031:
                return LIGHT_CONTROL_REGULATOR_ACCURACY;
            case 0x0032:
                return LIGHT_CONTROL_REGULATOR_KID;
            case 0x0033:
                return LIGHT_CONTROL_REGULATOR_KIU;
            case 0x0034:
                return LIGHT_CONTROL_REGULATOR_KPD;
            case 0x0035:
                return LIGHT_CONTROL_REGULATOR_KPU;
            case 0x0036:
                return LIGHT_CONTROL_TIME_FADE;
            case 0x0037:
                return LIGHT_CONTROL_TIME_FADE_ON;
            case 0x0038:
                return LIGHT_CONTROL_TIME_FADE_STANDBY_AUTO;
            case 0x0039:
                return LIGHT_CONTROL_TIME_FADE_STANDBY_MANUAL;
            case 0x003A:
                return LIGHT_CONTROL_TIME_OCCUPANCY_DELAY;
            case 0x003B:
                return LIGHT_CONTROL_TIME_PROLONG;
            case 0x003C:
                return LIGHT_CONTROL_TIME_RUN_ON;
            case 0x003D:
                return LUMEN_MAINTENANCE_FACTOR;
            case 0x003E:
                return LUMINOUS_EFFICACY;
            case 0x003F:
                return LUMINOUS_ENERGY_SINCE_TURN_ON;
            case 0x0040:
                return LUMINOUS_EXPOSURE;
            case 0x0041:
                return LUMINOUS_FLUX_RANGE;
            case 0x0042:
                return MOTION_SENSED;
            case 0x0043:
                return MOTION_THRESHOLD;
            case 0x0044:
                return OPEN_CIRCUIT_EVENT_STATISTICS;
            case 0x0045:
                return OUTDOOR_STATISTICAL_VALUES;
            case 0x0046:
                return OUTPUT_CURRENT_RANGE;
            case 0x0047:
                return OUTPUT_CURRENT_STATISTICS;
            case 0x0048:
                return OUTPUT_RIPPLE_VOLTAGE_SPECIFICATION;
            case 0x0049:
                return OUTPUT_VOLTAGE_RANGE;
            case 0x004A:
                return OUTPUT_VOLTAGE_STATISTICS;
            case 0x004B:
                return OVER_OUTPUT_RIPPLE_VOLTAGE_EVENT_STATISTICS;
            case 0x004C:
                return PEOPLE_COUNT;
            case 0x004D:
                return PRESENCE_DETECTED;
            case 0x004E:
                return PRESENT_AMBIENT_LIGHT_LEVEL;
            case 0x004F:
                return PRESENT_AMBIENT_TEMPERATURE;
            case 0x0050:
                return PRESENT_CIE1931_CHROMATICITY_COORDINATES;
            case 0x0051:
                return PRESENT_CORRELATED_COLOR_TEMPERATURE;
            case 0x0052:
                return PRESENT_DEVICE_INPUT_POWER;
            case 0x0053:
                return PRESENT_DEVICE_OPERATING_EFFICIENCY;
            case 0x0054:
                return PRESENT_DEVICE_OPERATING_TEMPERATURE;
            case 0x0055:
                return PRESENT_ILLUMINANCE;
            case 0x0056:
                return PRESENT_INDOOR_AMBIENT_TEMPERATURE;
            case 0x0057:
                return PRESENT_INPUT_CURRENT;
            case 0x0058:
                return PRESENT_INPUT_RIPPLE_VOLTAGE;
            case 0x0059:
                return PRESENT_INPUT_VOLTAGE;
            case 0x005A:
                return PRESENT_LUMINOUS_FLUX;
            case 0x005B:
                return PRESENT_OUTDOOR_AMBIENT_TEMPERATURE;
            case 0x005C:
                return PRESENT_OUTPUT_CURRENT;
            case 0x005D:
                return PRESENT_OUTPUT_VOLTAGE;
            case 0x005E:
                return PRESENT_PLANCKIAN_DISTANCE;
            case 0x005F:
                return PRESENT_RELATIVE_OUTPUT_RIPPLE_VOLTAGE;
            case 0x0060:
                return RELATIVE_DEVICE_ENERGY_USE_IN_A_PERIOD_OF_DAY;
            case 0x0061:
                return RELATIVE_DEVICE_RUNTIME_IN_A_GENERIC_LEVEL_RANGE;
            case 0x0062:
                return RELATIVE_EXPOSURE_TIME_IN_AN_ILLUMINANCE_RANGE;
            case 0x0063:
                return RELATIVE_RUNTIME_IN_A_CORRELATED_COLOR_TEMPERATURE_RANGE;
            case 0x0064:
                return RELATIVE_RUNTIME_IN_A_DEVICE_OPERATING_TEMPERATURE_RANGE;
            case 0x0065:
                return RELATIVE_RUNTIME_IN_AN_INPUT_CURRENT_RANGE;
            case 0x0066:
                return RELATIVE_RUNTIME_IN_AN_INPUT_VOLTAGE_RANGE;
            case 0x0067:
                return SHORT_CIRCUIT_EVENT_STATISTICS;
            case 0x0068:
                return TIME_SINCE_MOTION_SENSED;
            case 0x0069:
                return TIME_SINCE_PRESENCE_DETECTED;
            case 0x006A:
                return TOTAL_DEVICE_ENERGY_USE;
            case 0x006B:
                return TOTAL_DEVICE_OFF_ON_CYCLES;
            case 0x006C:
                return TOTAL_DEVICE_POWER_ON_CYCLES;
            case 0x006D:
                return TOTAL_DEVICE_POWER_ON_TIME;
            case 0x006E:
                return TOTAL_DEVICE_RUNTIME;
            case 0x006F:
                return TOTAL_LIGHT_EXPOSURE_TIME;
            case 0x0070:
                return TOTAL_LUMINOUS_ENERGY;
            case 0x0071:
                return DESIRED_AMBIENT_TEMPERATURE;
            case 0x0072:
                return PRECISE_TOTAL_DEVICE_ENERGY_USE;
            case 0x0073:
                return POWER_FACTOR;
            case 0x0074:
                return SENSOR_GAIN;
            case 0x0075:
                return PRECISE_PRESENT_AMBIENT_TEMPERATURE;
            case 0x0076:
                return PRESENT_AMBIENT_RELATIVE_HUMIDITY;
            case 0x0077:
                return PRESENT_AMBIENT_CARBONDIOXIDE_CONCENTRATION;
            case 0x0078:
                return PRESENT_AMBIENT_VOLATILE_ORGANIC_COMPOUNDS_CONCENTRATION;
            case 0x0079:
                return PRESENT_AMBIENT_NOISE;
            case 0x0080:
                return ACTIVE_ENERGY_LOAD_SIDE;
            case 0x0081:
                return ACTIVE_POWER_LOAD_SIDE;
            case 0x0082:
                return AIR_PRESSURE;
            case 0x0083:
                return APPARENT_ENERGY;
            case 0x0084:
                return APPARENT_POWER;
            case 0x0085:
                return APPARENT_WIND_DIRECTION;
            case 0x0086:
                return APPARENT_WIND_SPEED;
            case 0x0087:
                return DEW_POINT;
            case 0x0088:
                return EXTERNAL_SUPPLY_VOLTAGE;
            case 0x0089:
                return EXTERNAL_SUPPLY_VOLTAGE_FREQUENCY;
            case 0x008A:
                return GUST_FACTOR;
            case 0x008B:
                return HEAT_INDEX;
            case 0x008C:
                return LIGHT_DISTRIBUTION;
            case 0x008D:
                return LIGHT_SOURCE_CURRENT;
            case 0x008E:
                return LIGHT_SOURCE_ON_TIME_NOT_RESETTABLE;
            case 0x008F:
                return LIGHT_SOURCE_ON_TIME_RESETTABLE;
            case 0x0090:
                return LIGHT_SOURCE_OPEN_CIRCUIT_STATISTICS;
            case 0x0091:
                return LIGHT_SOURCE_OVERALL_FAILURES_STATISTICS;
            case 0x0092:
                return LIGHT_SOURCE_SHORT_CIRCUIT_STATISTICS;
            case 0x0093:
                return LIGHT_SOURCE_START_COUNTER_RESETTABLE;
            case 0x0094:
                return LIGHT_SOURCE_TEMPERATURE;
            case 0x0095:
                return LIGHT_SOURCE_THERMAL_DERATING_STATISTICS;
            case 0x0096:
                return LIGHT_SOURCE_THERMAL_SHUTDOWN_STATISTICS;
            case 0x0097:
                return LIGHT_SOURCE_TOTAL_POWER_ON_CYCLES;
            case 0x0098:
                return LIGHT_SOURCE_VOLTAGE;
            case 0x0099:
                return LUMINAIRE_COLOR;
            case 0x009A:
                return LUMINAIRE_IDENTIFICATION_NUMBER;
            case 0x009B:
                return LUMINAIRE_MANUFACTURER_GTIN;
            case 0x009C:
                return LUMINAIRE_NOMINAL_INPUT_POWER;
            case 0x009D:
                return LUMINAIRE_NOMINAL_MAXIMUM_AC_MAINS_VOLTAGE;
            case 0x009E:
                return LUMINAIRE_NOMINAL_MINIMUM_AC_MAINS_VOLTAGE;
            case 0x009F:
                return LUMINAIRE_POWER_AT_MINIMUM_DIM_LEVEL;
            case 0x00A0:
                return LUMINAIRE_TIME_OF_MANUFACTURE;
            case 0x00A1:
                return MAGNETIC_DECLINATION;
            case 0x00A2:
                return MAGNETIC_FLUX_DENSITY_2D;
            case 0x00A3:
                return MAGNETIC_FLUX_DENSITY_3D;
            case 0x00A4:
                return NOMINAL_LIGHT_OUTPUT;
            case 0x00A5:
                return OVERALL_FAILURE_CONDITION;
            case 0x00A6:
                return POLLEN_CONCENTRATION;
            case 0x00A7:
                return PRESENT_INDOOR_RELATIVE_HUMIDITY;
            case 0x00A8:
                return PRESENT_OUTDOOR_RELATIVE_HUMIDITY;
            case 0x00A9:
                return PRESSURE;
            case 0x00AA:
                return RAINFALL;
            case 0x00AB:
                return RATED_MEDIAN_USEFUL_LIFE_OF_LUMINAIRE;
            case 0x00AC:
                return RATED_MEDIAN_USEFUL_LIGHT_SOURCE_STARTS;
            case 0x00AD:
                return REFERENCE_TEMPERATURE;
            case 0x00AE:
                return TOTAL_DEVICE_STARTS;
            case 0x00AF:
                return TRUE_WIND_DIRECTION;
            case 0x00B0:
                return TRUE_WIND_SPEED;
            case 0x00B1:
                return UV_INDEX;
            case 0x00B2:
                return WIND_CHILL;
            case 0x00B3:
                return LIGHT_SOURCE_TYPE;
            case 0x00B4:
                return LUMINAIRE_IDENTIFICATION_STRING;
            case 0x00B5:
                return OUTPUT_POWER_LIMITATION;
            case 0x00B6:
                return THERMAL_DERATING;
            case 0x00B7:
                return OUTPUT_CURRENT_PERCENT;
            default:
                return UNKNOWN;

        }
    }

    public static DeviceProperty from(final SensorFormat sensorFormat, final short propertyId) {
        switch (sensorFormat) {
            case FORMAT_A:
                for (DeviceProperty deviceProperty : values()) {
                    if ((deviceProperty.propertyId & 0x7FF) == propertyId) {
                        return deviceProperty;
                    }
                }
            case FORMAT_B:
                return from(propertyId);
            default:
                return UNKNOWN;
        }
    }

    /**
     * Returns the property name for a given property.
     *
     * @param deviceProperty Device property
     */
    public static String getPropertyName(final DeviceProperty deviceProperty) {
        switch (deviceProperty) {
            case AVERAGE_AMBIENT_TEMPERATURE_IN_A_PERIOD_OF_DAY:
                return "Average Ambient Temperature In A Period Of Day";
            case AVERAGE_INPUT_CURRENT:
                return "Average Input Current";
            case AVERAGE_INPUT_VOLTAGE:
                return "Average Input Voltage";
            case AVERAGE_OUTPUT_CURRENT:
                return "Average Output Current";
            case AVERAGE_OUTPUT_VOLTAGE:
                return "Average Output Voltage";
            case CENTER_BEAM_INTENSITY_AT_FULL_POWER:
                return "Center Beam Intensity At Full Power";
            case CHROMATICITY_TOLERANCE:
                return "Chromaticity Tolerance";
            case COLOR_RENDERING_INDEX_R9:
                return "Color Rendering Index R9";
            case COLOR_RENDERING_INDEX_RA:
                return "Color Rendering Index Ra";
            case DEVICE_APPEARANCE:
                return "Device Appearance";
            case DEVICE_COUNTRY_OF_ORIGIN:
                return "Device Country Of Origin";
            case DEVICE_DATE_OF_MANUFACTURE:
                return "Device Date Of Manufacture";
            case DEVICE_ENERGY_USE_SINCE_TURN_ON:
                return "Device Energy Use Since Turn On";
            case DEVICE_FIRMWARE_REVISION:
                return "Device Firmware Revision";
            case DEVICE_GLOBAL_TRADE_ITEM_NUMBER:
                return "Device Global Trade Item Number";
            case DEVICE_HARDWARE_REVISION:
                return "Device Hardware Revision";
            case DEVICE_MANUFACTURER_NAME:
                return "Device Manufacturer Name";
            case DEVICE_MODEL_NUMBER:
                return "Device Model Number";
            case DEVICE_OPERATING_TEMPERATURE_RANGE_SPECIFICATION:
                return "Device Operating Temperature Range Specification";
            case DEVICE_OPERATING_TEMPERATURE_STATISTICAL_VALUES:
                return "Device Operating Temperature Statistical Values";
            case DEVICE_OVER_TEMPERATURE_EVENT_STATISTICS:
                return "Device Over Temperature Event Statistics";
            case DEVICE_POWER_RANGE_SPECIFICATION:
                return "Device Power Range Specification";
            case DEVICE_RUN_TIME_SINCE_TURN_ON:
                return "Device Runtime Since Turn On";
            case DEVICE_RUNTIME_WARRANTY:
                return "Device Runtime Warranty";
            case DEVICE_SERIAL_NUMBER:
                return "Device Serial Number";
            case DEVICE_SOFTWARE_REVISION:
                return "Device Software Revision";
            case DEVICE_UNDER_TEMPERATURE_EVENT_STATISTICS:
                return "Device Under Temperature Event Statistics";
            case INDOOR_AMBIENT_TEMPERATURE_STATISTICAL_VALUES:
                return "Indoor Ambient Temperature Statistical Values";
            case INITIAL_CIE1931_CHROMATICITY_COORDINATES:
                return "Initial CIE 1931 Chromaticity Coordinates";
            case INITIAL_CORRELATED_COLOR_TEMPERATURE:
                return "Initial Correlated Color Temperature";
            case INITIAL_LUMINOUS_FLUX:
                return "Initial Luminous Flux";
            case INITIAL_PLANCKIAN_DISTANCE:
                return "Initial Planckian Distance";
            case INPUT_CURRENT_RANGE_SPECIFICATION:
                return "Input Current Range Specification";
            case INPUT_CURRENT_STATISTICS:
                return "Input Current Statistics";
            case INPUT_OVER_CURRENT_EVENT_STATISTICS:
                return "Input Over Current Event Statistics";
            case INPUT_OVER_RIPPLE_VOLTAGE_EVENT_STATISTICS:
                return "Input Over Ripple Voltage Event Statistics";
            case INPUT_OVER_VOLTAGE_EVENT_STATISTICS:
                return "Input Over Voltage Event Statistics";
            case INPUT_UNDERCURRENT_EVENT_STATISTICS:
                return "Input Under Current Event Statistics";
            case INPUT_UNDER_VOLTAGE_EVENT_STATISTICS:
                return "Input Under Voltage Event Statistics";
            case INPUT_VOLTAGE_RANGE_SPECIFICATION:
                return "Input Voltage Range Specification";
            case INPUT_VOLTAGE_RIPPLE_SPECIFICATION:
                return "Input Voltage Ripple Specification";
            case INPUT_VOLTAGE_STATISTICS:
                return "Input Voltage Statistics";
            case LIGHT_CONTROL_AMBIENT_LUX_LEVEL_ON:
                return "Light Control Ambient LuxLevel On";
            case LIGHT_CONTROL_AMBIENT_LUX_LEVEL_PROLONG:
                return "Light Control Ambient LuxLevel Prolong";
            case LIGHT_CONTROL_AMBIENT_LUX_LEVEL_STANDBY:
                return "Light Control Ambient LuxLevel Standby";
            case LIGHT_CONTROL_LIGHTNESS_ON:
                return "Light Control Lightness On";
            case LIGHT_CONTROL_LIGHTNESS_PROLONG:
                return "Light Control Lightness Prolong";
            case LIGHT_CONTROL_LIGHTNESS_STANDBY:
                return "Light Control Lightness Standby";
            case LIGHT_CONTROL_REGULATOR_ACCURACY:
                return "Light Control Regulator Accuracy";
            case LIGHT_CONTROL_REGULATOR_KID:
                return "Light Control Regulator Kid";
            case LIGHT_CONTROL_REGULATOR_KIU:
                return "Light Control Regulator Kiu";
            case LIGHT_CONTROL_REGULATOR_KPD:
                return "Light Control Regulator Kpd";
            case LIGHT_CONTROL_REGULATOR_KPU:
                return "Light Control Regulator Kpu";
            case LIGHT_CONTROL_TIME_FADE:
                return "Light Control Time Fade";
            case LIGHT_CONTROL_TIME_FADE_ON:
                return "Light Control Time Fade On";
            case LIGHT_CONTROL_TIME_FADE_STANDBY_AUTO:
                return "Light Control Time Fade Standby Auto";
            case LIGHT_CONTROL_TIME_FADE_STANDBY_MANUAL:
                return "Light Control Time Fade Standby Manual";
            case LIGHT_CONTROL_TIME_OCCUPANCY_DELAY:
                return "Light Control Time Occupancy Delay";
            case LIGHT_CONTROL_TIME_PROLONG:
                return "Light Control Time Prolong";
            case LIGHT_CONTROL_TIME_RUN_ON:
                return "Light Control Time Run On";
            case LUMEN_MAINTENANCE_FACTOR:
                return "Lumen Maintenance Factor";
            case LUMINOUS_EFFICACY:
                return "Luminous Efficacy";
            case LUMINOUS_ENERGY_SINCE_TURN_ON:
                return "Luminous Energy Since Turn On";
            case LUMINOUS_EXPOSURE:
                return "Luminous Exposure";
            case LUMINOUS_FLUX_RANGE:
                return "Luminous Flux Range";
            case MOTION_SENSED:
                return "Motion Sensed";
            case MOTION_THRESHOLD:
                return "Motion Threshold";
            case OPEN_CIRCUIT_EVENT_STATISTICS:
                return "Open Circuit Event Statistics";
            case OUTDOOR_STATISTICAL_VALUES:
                return "Outdoor Statistical Values";
            case OUTPUT_CURRENT_RANGE:
                return "Output Current Range";
            case OUTPUT_CURRENT_STATISTICS:
                return "Output Current Statistics";
            case OUTPUT_RIPPLE_VOLTAGE_SPECIFICATION:
                return "Output Ripple Voltage Specification";
            case OUTPUT_VOLTAGE_RANGE:
                return "Output Voltage Range";
            case OUTPUT_VOLTAGE_STATISTICS:
                return "Output Voltage Statistics";
            case OVER_OUTPUT_RIPPLE_VOLTAGE_EVENT_STATISTICS:
                return "Over Output Ripple Voltage Event Statistics";
            case PEOPLE_COUNT:
                return "People Count";
            case PRESENCE_DETECTED:
                return "Presence Detected";
            case PRESENT_AMBIENT_LIGHT_LEVEL:
                return "Present Ambient Light Level";
            case PRESENT_AMBIENT_TEMPERATURE:
                return "Present Ambient Temperature";
            case PRESENT_CIE1931_CHROMATICITY_COORDINATES:
                return "Present CIE 1931 Chromaticity Coordinates";
            case PRESENT_CORRELATED_COLOR_TEMPERATURE:
                return "Present Correlated Color Temperature";
            case PRESENT_DEVICE_INPUT_POWER:
                return "Present Device Input Power";
            case PRESENT_DEVICE_OPERATING_EFFICIENCY:
                return "Present Device Operating Efficiency";
            case PRESENT_DEVICE_OPERATING_TEMPERATURE:
                return "Present Device Operating Temperature";
            case PRESENT_ILLUMINANCE:
                return "Present Illuminance";
            case PRESENT_INDOOR_AMBIENT_TEMPERATURE:
                return "Present Indoor Ambient Temperature";
            case PRESENT_INPUT_CURRENT:
                return "Present Input Current";
            case PRESENT_INPUT_RIPPLE_VOLTAGE:
                return "Present Input Ripple Voltage";
            case PRESENT_INPUT_VOLTAGE:
                return "Present Input Voltage";
            case PRESENT_LUMINOUS_FLUX:
                return "Present Luminous Flux";
            case PRESENT_OUTDOOR_AMBIENT_TEMPERATURE:
                return "Present Outdoor Ambient Temperature";
            case PRESENT_OUTPUT_CURRENT:
                return "Present Output Current";
            case PRESENT_OUTPUT_VOLTAGE:
                return "Present Output Voltage";
            case PRESENT_PLANCKIAN_DISTANCE:
                return "Present Planckian Distance";
            case PRESENT_RELATIVE_OUTPUT_RIPPLE_VOLTAGE:
                return "Present Relative Output Ripple Voltage";
            case RELATIVE_DEVICE_ENERGY_USE_IN_A_PERIOD_OF_DAY:
                return "Relative Device Energy Use In A Period Of Day";
            case RELATIVE_DEVICE_RUNTIME_IN_A_GENERIC_LEVEL_RANGE:
                return "Relative Device Runtime In A Generic Level Range";
            case RELATIVE_EXPOSURE_TIME_IN_AN_ILLUMINANCE_RANGE:
                return "Relative Exposure Time In An Illuminance Range";
            case RELATIVE_RUNTIME_IN_A_CORRELATED_COLOR_TEMPERATURE_RANGE:
                return "Relative Runtime In A Correlated Color Temperature Range";
            case RELATIVE_RUNTIME_IN_A_DEVICE_OPERATING_TEMPERATURE_RANGE:
                return "Relative Runtime In A Device Operating Temperature Range";
            case RELATIVE_RUNTIME_IN_AN_INPUT_CURRENT_RANGE:
                return "Relative Runtime In An Input Current Range";
            case RELATIVE_RUNTIME_IN_AN_INPUT_VOLTAGE_RANGE:
                return "Relative Runtime In An Input Voltage Range";
            case SHORT_CIRCUIT_EVENT_STATISTICS:
                return "Short Circuit Event Statistics";
            case TIME_SINCE_MOTION_SENSED:
                return "Time Since Motion Sensed";
            case TIME_SINCE_PRESENCE_DETECTED:
                return "Time Since Presence Detected";
            case TOTAL_DEVICE_ENERGY_USE:
                return "Total Device Energy Use";
            case TOTAL_DEVICE_OFF_ON_CYCLES:
                return "Total Device Off On Cycles";
            case TOTAL_DEVICE_POWER_ON_CYCLES:
                return "Total Device Power On Cycles";
            case TOTAL_DEVICE_POWER_ON_TIME:
                return "Total Device Power On Time";
            case TOTAL_DEVICE_RUNTIME:
                return "Total Device Runtime";
            case TOTAL_LIGHT_EXPOSURE_TIME:
                return "Total Light Exposure Time";
            case TOTAL_LUMINOUS_ENERGY:
                return "Total Luminous Energy";
            case DESIRED_AMBIENT_TEMPERATURE:
                return "Desired Ambient Temperature";
            case PRECISE_TOTAL_DEVICE_ENERGY_USE:
                return "Precise Total Device Energy Use";
            case POWER_FACTOR:
                return "Power Factor";
            case SENSOR_GAIN:
                return "Sensor Gain";
            case PRECISE_PRESENT_AMBIENT_TEMPERATURE:
                return "Precise Present Ambient Temperature";
            case PRESENT_AMBIENT_RELATIVE_HUMIDITY:
                return "Present Ambient Relative Humidity";
            case PRESENT_AMBIENT_CARBONDIOXIDE_CONCENTRATION:
                return "Present Ambient Carbon Dioxide Concentration";
            case PRESENT_AMBIENT_VOLATILE_ORGANIC_COMPOUNDS_CONCENTRATION:
                return "Present Ambient Volatile Organic Compounds Concentration";
            case PRESENT_AMBIENT_NOISE:
                return "Present Ambient Noise";
            case ACTIVE_ENERGY_LOAD_SIDE:
                return "Active Energy Loadside";
            case ACTIVE_POWER_LOAD_SIDE:
                return "Active Power Loadside";
            case AIR_PRESSURE:
                return "Air Pressure";
            case APPARENT_ENERGY:
                return "Apparent Energy";
            case APPARENT_POWER:
                return "Apparent Power";
            case APPARENT_WIND_DIRECTION:
                return "Apparent Wind Direction";
            case APPARENT_WIND_SPEED:
                return "Apparent Wind Speed";
            case DEW_POINT:
                return "Dew Point";
            case EXTERNAL_SUPPLY_VOLTAGE:
                return "External Supply Voltage";
            case EXTERNAL_SUPPLY_VOLTAGE_FREQUENCY:
                return "External Supply Voltage Frequency";
            case GUST_FACTOR:
                return "Gust Factor";
            case HEAT_INDEX:
                return "Heat Index";
            case LIGHT_DISTRIBUTION:
                return "Light Distribution";
            case LIGHT_SOURCE_CURRENT:
                return "Light Source Current";
            case LIGHT_SOURCE_ON_TIME_NOT_RESETTABLE:
                return "Light Source On Time Not Resettable";
            case LIGHT_SOURCE_ON_TIME_RESETTABLE:
                return "Light Source On Time Resettable";
            case LIGHT_SOURCE_OPEN_CIRCUIT_STATISTICS:
                return "Light Source Open Circuit Statistics";
            case LIGHT_SOURCE_OVERALL_FAILURES_STATISTICS:
                return "Light Source Overall Failures Statistics";
            case LIGHT_SOURCE_SHORT_CIRCUIT_STATISTICS:
                return "Light Source Short Circuit Statistics";
            case LIGHT_SOURCE_START_COUNTER_RESETTABLE:
                return "Light Source Start Counter Resettable";
            case LIGHT_SOURCE_TEMPERATURE:
                return "Light Source Temperature";
            case LIGHT_SOURCE_THERMAL_DERATING_STATISTICS:
                return "Light Source Thermal Derating Statistics";
            case LIGHT_SOURCE_THERMAL_SHUTDOWN_STATISTICS:
                return "Light Source Thermal Shutdown Statistics";
            case LIGHT_SOURCE_TOTAL_POWER_ON_CYCLES:
                return "Light Source Total Power On Cycles";
            case LIGHT_SOURCE_VOLTAGE:
                return "Light Source Voltage";
            case LUMINAIRE_COLOR:
                return "Luminaire Color";
            case LUMINAIRE_IDENTIFICATION_NUMBER:
                return "Luminaire Identification Number";
            case LUMINAIRE_MANUFACTURER_GTIN:
                return "Luminaire Manufacturer GTIN";
            case LUMINAIRE_NOMINAL_INPUT_POWER:
                return "Luminaire Nominal Input Power";
            case LUMINAIRE_NOMINAL_MAXIMUM_AC_MAINS_VOLTAGE:
                return "Luminaire Nominal Maximum AC Mains Voltage";
            case LUMINAIRE_NOMINAL_MINIMUM_AC_MAINS_VOLTAGE:
                return "Luminaire Nominal Minimum AC Mains Voltage";
            case LUMINAIRE_POWER_AT_MINIMUM_DIM_LEVEL:
                return "Luminaire Power At Minimum Dim Level";
            case LUMINAIRE_TIME_OF_MANUFACTURE:
                return "Luminaire Time Of Manufacture";
            case MAGNETIC_DECLINATION:
                return "Magnetic Declination";
            case MAGNETIC_FLUX_DENSITY_2D:
                return "Magnetic Flux Density - 2D";
            case MAGNETIC_FLUX_DENSITY_3D:
                return "Magnetic Flux Density - 3D";
            case NOMINAL_LIGHT_OUTPUT:
                return "Nominal Light Output";
            case OVERALL_FAILURE_CONDITION:
                return "Overall Failure Condition";
            case POLLEN_CONCENTRATION:
                return "Pollen Concentration";
            case PRESENT_INDOOR_RELATIVE_HUMIDITY:
                return "Present Indoor Relative Humidity";
            case PRESENT_OUTDOOR_RELATIVE_HUMIDITY:
                return "Present Outdoor Relative Humidity";
            case PRESSURE:
                return "Pressure";
            case RAINFALL:
                return "Rainfall";
            case RATED_MEDIAN_USEFUL_LIFE_OF_LUMINAIRE:
                return "Rated Median Useful Life Of Luminaire";
            case RATED_MEDIAN_USEFUL_LIGHT_SOURCE_STARTS:
                return "Rated Median Useful Light Source Starts";
            case REFERENCE_TEMPERATURE:
                return "Reference Temperature";
            case TOTAL_DEVICE_STARTS:
                return "Total Device Starts";
            case TRUE_WIND_DIRECTION:
                return "True Wind Direction";
            case TRUE_WIND_SPEED:
                return "True Wind Speed";
            case UV_INDEX:
                return "UV Index";
            case WIND_CHILL:
                return "Wind Chill";
            case LIGHT_SOURCE_TYPE:
                return "Light Source Type";
            case LUMINAIRE_IDENTIFICATION_STRING:
                return "Luminaire Identification String";
            case OUTPUT_POWER_LIMITATION:
                return "Output Power Limitation";
            case THERMAL_DERATING:
                return "Thermal Derating";
            case OUTPUT_CURRENT_PERCENT:
                return "Output Current Percent";
            case UNKNOWN:
            default:
                return "Unknown Device Property";
        }
    }

    public static DevicePropertyCharacteristic<?> getCharacteristic(@NonNull final DeviceProperty deviceProperty,
                                                                    @NonNull final byte[] data,
                                                                    int offset,
                                                                    final int length) {
        switch (deviceProperty) {
            case PRESENCE_DETECTED:
                return data.length == 0 ? new Bool(false) : new Bool(data[offset] == 0x01);
            case LIGHT_CONTROL_REGULATOR_ACCURACY:
            case OUTPUT_RIPPLE_VOLTAGE_SPECIFICATION:
            case INPUT_VOLTAGE_RIPPLE_SPECIFICATION:
            case OUTPUT_CURRENT_PERCENT:
            case LUMEN_MAINTENANCE_FACTOR:
            case MOTION_SENSED:
            case MOTION_THRESHOLD:
            case PRESENT_DEVICE_OPERATING_EFFICIENCY:
            case PRESENT_RELATIVE_OUTPUT_RIPPLE_VOLTAGE:
            case PRESENT_INPUT_RIPPLE_VOLTAGE:
                return new Percentage8(data, offset);
            case DESIRED_AMBIENT_TEMPERATURE:
            case PRESENT_AMBIENT_TEMPERATURE:
            case PRESENT_INDOOR_AMBIENT_TEMPERATURE:
            case PRESENT_OUTDOOR_AMBIENT_TEMPERATURE:
                return new Temperature(data, offset, 1);
            case PRECISE_PRESENT_AMBIENT_TEMPERATURE:
            case PRESENT_DEVICE_OPERATING_TEMPERATURE:
                return new Temperature(data, offset, 2);
            case PEOPLE_COUNT:
                return new Count(data, offset, 2); //Count16
            case PRESENT_AMBIENT_RELATIVE_HUMIDITY:
            case PRESENT_INDOOR_RELATIVE_HUMIDITY:
            case PRESENT_OUTDOOR_RELATIVE_HUMIDITY:
                return new Humidity(data, offset);
            case LIGHT_CONTROL_LIGHTNESS_ON:
            case LIGHT_CONTROL_LIGHTNESS_PROLONG:
            case LIGHT_CONTROL_LIGHTNESS_STANDBY:
                return new PerceivedLightness(data, offset);
            case TIME_SINCE_MOTION_SENSED:
            case TIME_SINCE_PRESENCE_DETECTED:
                return new TimeSecond(data, offset, 2);
            case LIGHT_SOURCE_START_COUNTER_RESETTABLE:
            case LIGHT_SOURCE_TOTAL_POWER_ON_CYCLES:
            case RATED_MEDIAN_USEFUL_LIGHT_SOURCE_STARTS:
            case TOTAL_DEVICE_OFF_ON_CYCLES:
            case TOTAL_DEVICE_POWER_ON_CYCLES:
            case TOTAL_DEVICE_STARTS:
                return new Count(data, offset, 3);
            case LIGHT_CONTROL_AMBIENT_LUX_LEVEL_ON:
            case LIGHT_CONTROL_AMBIENT_LUX_LEVEL_PROLONG:
            case LIGHT_CONTROL_AMBIENT_LUX_LEVEL_STANDBY:
            case PRESENT_AMBIENT_LIGHT_LEVEL:
            case PRESENT_ILLUMINANCE:
                return new Illuminance(data, offset);
            case DEVICE_RUN_TIME_SINCE_TURN_ON:
            case DEVICE_RUNTIME_WARRANTY:
            case RATED_MEDIAN_USEFUL_LIFE_OF_LUMINAIRE:
            case TOTAL_DEVICE_POWER_ON_TIME:
            case TOTAL_DEVICE_RUNTIME:
            case TOTAL_LIGHT_EXPOSURE_TIME:
                return new TimeHour24(data, offset);
            case LIGHT_CONTROL_TIME_FADE:
            case LIGHT_CONTROL_TIME_FADE_ON:
            case LIGHT_CONTROL_TIME_FADE_STANDBY_AUTO:
            case LIGHT_CONTROL_TIME_FADE_STANDBY_MANUAL:
            case LIGHT_CONTROL_TIME_OCCUPANCY_DELAY:
            case LIGHT_CONTROL_TIME_PROLONG:
            case LIGHT_CONTROL_TIME_RUN_ON:
                return new TimeMillisecond24(data, offset);
            case DEVICE_DATE_OF_MANUFACTURE:
            case LUMINAIRE_TIME_OF_MANUFACTURE:
                return new DateUtc(data, offset);
            case PRESSURE:
            case AIR_PRESSURE:
                return new Pressure(data, offset);
            case LIGHT_CONTROL_REGULATOR_KID:
            case LIGHT_CONTROL_REGULATOR_KIU:
            case LIGHT_CONTROL_REGULATOR_KPD:
            case LIGHT_CONTROL_REGULATOR_KPU:
            case SENSOR_GAIN:
                return new Coefficient(data, offset);
            case DEVICE_FIRMWARE_REVISION:
            case DEVICE_SOFTWARE_REVISION:
                return new FixedString(data, offset, 8);
            case DEVICE_HARDWARE_REVISION:
            case DEVICE_SERIAL_NUMBER:
                return new FixedString(data, offset, 16);
            case DEVICE_MODEL_NUMBER:
            case LUMINAIRE_COLOR:
            case LUMINAIRE_IDENTIFICATION_NUMBER:
                return new FixedString(data, offset, 24);
            case DEVICE_MANUFACTURER_NAME:
                return new FixedString(data, offset, 36);
            case LUMINAIRE_IDENTIFICATION_STRING:
                return new FixedString(data, offset, 64);
            default:
                return new UnknownCharacteristic(data, offset, length);
        }
    }
}
