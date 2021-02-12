package no.nordicsemi.android.mesh.utils;

/**
 * Device Property
 */
@SuppressWarnings("unused")
public enum DeviceProperty {
    AVERAGE_AMBIENT_TEMPERATURE_IN_A_PERIOD_OF_DAY((short) 0X0001),
    AVERAGE_INPUT_CURRENT((short) 0X0002),
    AVERAGE_INPUT_VOLTAGE((short) 0X0003),
    AVERAGE_OUTPUT_CURRENT((short) 0X0004),
    AVERAGE_OUTPUT_VOLTAGE((short) 0X0005),
    CENTER_BEAM_INTENSITY_AT_FULL_POWER((short) 0X0006),
    CHROMATICITY_TOLERANCE((short) 0X0007),
    COLOR_RENDERING_INDEX_R9((short) 0X0008),
    COLOR_RENDERING_INDEX_RA((short) 0X0009),
    DEVICE_APPEARANCE((short) 0X000A),
    DEVICE_COUNTRY_OF_ORIGIN((short) 0X000B),
    DEVICE_DATE_OF_MANUFACTURE((short) 0X000C),
    DEVICE_ENERGY_USE_SINCE_TURN_ON((short) 0X000D),
    DEVICE_FIRMWARE_REVISION((short) 0X000E),
    DEVICE_GLOBAL_TRADE_ITEM_NUMBER((short) 0X000F),
    DEVICE_HARDWARE_REVISION((short) 0X0010),
    DEVICE_MANUFACTURER_NAME((short) 0X0011),
    DEVICE_MODEL_NUMBER((short) 0X0012),
    DEVICE_OPERATING_TEMPERATURE_RANGE_SPECIFICATION((short) 0X0013),
    DEVICE_OPERATING_TEMPERATURE_STATISTICAL_VALUES((short) 0X0014),
    DEVICE_OVER_TEMPERATURE_EVENT_STATISTICS((short) 0X0015),
    DEVICE_POWER_RANGE_SPECIFICATION((short) 0X0016),
    DEVICE_RUN_TIME_SINCE_TURN_ON((short) 0X0017),
    DEVICE_RUNTIME_WARRANTY((short) 0X0018),
    DEVICE_SERIAL_NUMBER((short) 0X0019),
    DEVICE_SOFTWARE_REVISION((short) 0X001A),
    DEVICE_UNDER_TEMPERATURE_EVENT_STATISTICS((short) 0X001B),
    INDOOR_AMBIENT_TEMPERATURE_STATISTICAL_VALUES((short) 0X001C),
    INITIAL_CIE1931_CHROMATICITY_COORDINATES((short) 0X001D),
    INITIAL_CORRELATED_COLOR_TEMPERATURE((short) 0X001E),
    INITIAL_LUMINOUS_FLUX((short) 0X001F),
    INITIAL_PLANCKIAN_DISTANCE((short) 0X0020),
    INPUT_CURRENT_RANGE_SPECIFICATION((short) 0X0021),
    INPUT_CURRENT_STATISTICS((short) 0X0022),
    INPUT_OVER_CURRENT_EVENT_STATISTICS((short) 0X0023),
    INPUT_OVER_RIPPLE_VOLTAGE_EVENT_STATISTICS((short) 0X0024),
    INPUT_OVER_VOLTAGE_EVENT_STATISTICS((short) 0X0025),
    INPUT_UNDERCURRENT_EVENT_STATISTICS((short) 0X0026),
    INPUT_UNDER_VOLTAGE_EVENT_STATISTICS((short) 0X0027),
    INPUT_VOLTAGE_RANGE_SPECIFICATION((short) 0X0028),
    INPUT_VOLTAGE_RIPPLE_SPECIFICATION((short) 0X0029),
    INPUT_VOLTAGE_STATISTICS((short) 0X002A),
    LIGHT_CONTROL_AMBIENT_LUX_LEVEL_ON((short) 0X002B),
    LIGHT_CONTROL_AMBIENT_LUX_LEVEL_PROLONG((short) 0X002C),
    LIGHT_CONTROL_AMBIENT_LUX_LEVEL_STANDBY((short) 0X002D),
    LIGHT_CONTROL_LIGHTNESS_ON((short) 0X002E),
    LIGHT_CONTROL_LIGHTNESS_PROLONG((short) 0X002F),
    LIGHT_CONTROL_LIGHTNESS_STANDBY((short) 0X0030),
    LIGHT_CONTROL_REGULATOR_ACCURACY((short) 0X0031),
    LIGHT_CONTROL_REGULATOR_KID((short) 0X0032),
    LIGHT_CONTROL_REGULATOR_KIU((short) 0X0033),
    LIGHT_CONTROL_REGULATOR_KPD((short) 0X0034),
    LIGHT_CONTROL_REGULATOR_KPU((short) 0X0035),
    LIGHT_CONTROL_TIME_FADE((short) 0X0036),
    LIGHT_CONTROL_TIME_FADE_ON((short) 0X0037),
    LIGHT_CONTROL_TIME_FADE_STANDBY_AUTO((short) 0X0038),
    LIGHT_CONTROL_TIME_FADE_STANDBY_MANUAL((short) 0X0039),
    LIGHT_CONTROL_TIME_OCCUPANCY_DELAY((short) 0X003A),
    LIGHT_CONTROL_TIME_PROLONG((short) 0X003B),
    LIGHT_CONTROL_TIME_RUN_ON((short) 0X003C),
    LUMEN_MAINTENANCE_FACTOR((short) 0X003D),
    LUMINOUS_EFFICACY((short) 0X003E),
    LUMINOUS_ENERGY_SINCE_TURN_ON((short) 0X003F),
    LUMINOUS_EXPOSURE((short) 0X0040),
    LUMINOUS_FLUX_RANGE((short) 0X0041),
    MOTION_SENSED((short) 0X0042),
    MOTION_THRESHOLD((short) 0X0043),
    OPEN_CIRCUIT_EVENT_STATISTICS((short) 0X0044),
    OUTDOOR_STATISTICAL_VALUES((short) 0X0045),
    OUTPUT_CURRENT_RANGE((short) 0X0046),
    OUTPUT_CURRENT_STATISTICS((short) 0X0047),
    OUTPUT_RIPPLE_VOLTAGE_SPECIFICATION((short) 0X0048),
    OUTPUT_VOLTAGE_RANGE((short) 0X0049),
    OUTPUT_VOLTAGE_STATISTICS((short) 0X004A),
    OVER_OUTPUT_RIPPLE_VOLTAGE_EVENT_STATISTICS((short) 0X004B),
    PEOPLE_COUNT((short) 0X004C),
    PRESENCE_DETECTED((short) 0X004D),
    PRESENT_AMBIENT_LIGHT_LEVEL((short) 0X004E),
    PRESENT_AMBIENT_TEMPERATURE((short) 0X004F),
    PRESENT_CIE1931_CHROMATICITY_COORDINATES((short) 0X0050),
    PRESENT_CORRELATED_COLOR_TEMPERATURE((short) 0X0051),
    PRESENT_DEVICE_INPUT_POWER((short) 0X0052),
    PRESENT_DEVICE_OPERATING_EFFICIENCY((short) 0X0053),
    PRESENT_DEVICE_OPERATING_TEMPERATURE((short) 0X0054),
    PRESENT_ILLUMINANCE((short) 0X0055),
    PRESENT_INDOOR_AMBIENT_TEMPERATURE((short) 0X0056),
    PRESENT_INPUT_CURRENT((short) 0X0057),
    PRESENT_INPUT_RIPPLE_VOLTAGE((short) 0X0058),
    PRESENT_INPUT_VOLTAGE((short) 0X0059),
    PRESENT_LUMINOUS_FLUX((short) 0X005A),
    PRESENT_OUTDOOR_AMBIENT_TEMPERATURE((short) 0X005B),
    PRESENT_OUTPUT_CURRENT((short) 0X005C),
    PRESENT_OUTPUT_VOLTAGE((short) 0X005D),
    PRESENT_PLANCKIAN_DISTANCE((short) 0X005E),
    PRESENT_RELATIVE_OUTPUT_RIPPLE_VOLTAGE((short) 0X005F),
    RELATIVE_DEVICE_ENERGY_USE_IN_A_PERIOD_OF_DAY((short) 0X0060),
    RELATIVE_DEVICE_RUNTIME_IN_A_GENERIC_LEVEL_RANGE((short) 0X0061),
    RELATIVE_EXPOSURE_TIME_IN_AN_ILLUMINANCE_RANGE((short) 0X0062),
    RELATIVE_RUNTIME_IN_A_CORRELATED_COLOR_TEMPERATURE_RANGE((short) 0X0063),
    RELATIVE_RUNTIME_IN_A_DEVICE_OPERATING_TEMPERATURE_RANGE((short) 0X0064),
    RELATIVE_RUNTIME_IN_AN_INPUT_CURRENT_RANGE((short) 0X0065),
    RELATIVE_RUNTIME_IN_AN_INPUT_VOLTAGE_RANGE((short) 0X0066),
    SHORT_CIRCUIT_EVENT_STATISTICS((short) 0X0067),
    TIME_SINCE_MOTION_SENSED((short) 0X0068),
    TIME_SINCE_PRESENCE_DETECTED((short) 0X0069),
    TOTAL_DEVICE_ENERGY_USE((short) 0X006A),
    TOTAL_DEVICE_OFF_ON_CYCLES((short) 0X006B),
    TOTAL_DEVICE_POWER_ON_CYCLES((short) 0X006C),
    TOTAL_DEVICE_POWER_ON_TIME((short) 0X006D),
    TOTAL_DEVICE_RUNTIME((short) 0X006E),
    TOTAL_LIGHT_EXPOSURE_TIME((short) 0X006F),
    TOTAL_LUMINOUS_ENERGY((short) 0X0070),
    DESIRED_AMBIENT_TEMPERATURE((short) 0X0071),
    PRECISE_TOTAL_DEVICE_ENERGY_USE((short) 0X0072),
    POWER_FACTOR((short) 0X0073),
    SENSOR_GAIN((short) 0X0074),
    PRECISE_PRESENT_AMBIENT_TEMPERATURE((short) 0X0075),
    PRESENT_AMBIENT_RELATIVE_HUMIDITY((short) 0X0076),
    PRESENT_AMBIENT_CARBONDIOXIDE_CONCENTRATION((short) 0X0077),
    PRESENT_AMBIENT_VOLATILE_ORGANIC_COMPOUNDS_CONCENTRATION((short) 0X0078),
    PRESENT_AMBIENT_NOISE((short) 0X0079),
    // These are undefined in mesh device properties v2 = (short) 0X007A
    // These are undefined in mesh device properties v2 = (short) 0X007B
    // These are undefined in mesh device properties v2 = (short) 0X007C
    // These are undefined in mesh device properties v2 = (short) 0X007D
    // These are undefined in mesh device properties v2 = (short) 0X007E
    // These are undefined in mesh device properties v2 = (short) 0X007F
    ACTIVE_ENERGY_LOAD_SIDE((short) 0X0080),
    ACTIVE_POWER_LOAD_SIDE((short) 0X0081),
    AIR_PRESSURE((short) 0X0082),
    APPARENT_ENERGY((short) 0X0083),
    APPARENT_POWER((short) 0X0084),
    APPARENT_WIND_DIRECTION((short) 0X0085),
    APPARENT_WIND_SPEED((short) 0X0086),
    DEW_POINT((short) 0X0087),
    EXTERNAL_SUPPLY_VOLTAGE((short) 0X0088),
    EXTERNAL_SUPPLY_VOLTAGE_FREQUENCY((short) 0X0089),
    GUST_FACTOR((short) 0X008A),
    HEAT_INDEX((short) 0X008B),
    LIGHT_DISTRIBUTION((short) 0X008C),
    LIGHT_SOURCE_CURRENT((short) 0X008D),
    LIGHT_SOURCE_ON_TIME_NOT_RESETTABLE((short) 0X008E),
    LIGHT_SOURCE_ON_TIME_RESETTABLE((short) 0X008F),
    LIGHT_SOURCE_OPEN_CIRCUIT_STATISTICS((short) 0X0090),
    LIGHT_SOURCE_OVERALL_FAILURES_STATISTICS((short) 0X0091),
    LIGHT_SOURCE_SHORT_CIRCUIT_STATISTICS((short) 0X0092),
    LIGHT_SOURCE_START_COUNTER_RESETTABLE((short) 0X0093),
    LIGHT_SOURCE_TEMPERATURE((short) 0X0094),
    LIGHT_SOURCE_THERMAL_DERATING_STATISTICS((short) 0X0095),
    LIGHT_SOURCE_THERMAL_SHUTDOWN_STATISTICS((short) 0X0096),
    LIGHT_SOURCE_TOTAL_POWER_ON_CYCLES((short) 0X0097),
    LIGHT_SOURCE_VOLTAGE((short) 0X0098),
    LUMINAIRE_COLOR((short) 0X0099),
    LUMINAIRE_IDENTIFICATION_NUMBER((short) 0X009A),
    LUMINAIRE_MANUFACTURER_GTIN((short) 0X009B),
    LUMINAIRE_NOMINAL_INPUT_POWER((short) 0X009C),
    LUMINAIRE_NOMINAL_MAXIMUM_AC_MAINS_VOLTAGE((short) 0X009D),
    LUMINAIRE_NOMINAL_MINIMUM_AC_MAINS_VOLTAGE((short) 0X009E),
    LUMINAIRE_POWER_AT_MINIMUM_DIM_LEVEL((short) 0X009F),
    LUMINAIRE_TIME_OF_MANUFACTURE((short) 0X00A0),
    MAGNETIC_DECLINATION((short) 0X00A1),
    MAGNETIC_FLUX_DENSITY_2D((short) 0X00A2),
    MAGNETIC_FLUX_DENSITY_3D((short) 0X00A3),
    NOMINAL_LIGHT_OUTPUT((short) 0X00A4),
    OVERALL_FAILURE_CONDITION((short) 0X00A5),
    POLLEN_CONCENTRATION((short) 0X00A6),
    PRESENT_INDOOR_RELATIVE_HUMIDITY((short) 0X00A7),
    PRESENT_OUTDOOR_RELATIVE_HUMIDITY((short) 0X00A8),
    PRESSURE((short) 0X00A9),
    RAINFALL((short) 0X00AA),
    RATED_MEDIAN_USEFUL_LIFE_OF_LUMINAIRE((short) 0X00AB),
    RATED_MEDIAN_USEFUL_LIGHT_SOURCE_STARTS((short) 0X00AC),
    REFERENCE_TEMPERATURE((short) 0X00AD),
    TOTAL_DEVICE_STARTS((short) 0X00AE),
    TRUE_WIND_DIRECTION((short) 0X00AF),
    TRUE_WIND_SPEED((short) 0X00B0),
    UV_INDEX((short) 0X00B1),
    WIND_CHILL((short) 0X00B2),
    LIGHT_SOURCE_TYPE((short) 0X00B3),
    LUMINAIRE_IDENTIFICATION_STRING((short) 0X00B4),
    OUTPUT_POWER_LIMITATION((short) 0X00B5),
    THERMAL_DERATING((short) 0X00B6),
    OUTPUT_CURRENT_PERCENT((short) 0X00B7);

    private static final String TAG = DeviceProperty.class.getSimpleName();
    private final short deviceProperty;

    DeviceProperty(final short property) {
        this.deviceProperty = property;
    }

    /**
     * Returns the static oob type value
     */
    public short getDeviceProperty() {
        return deviceProperty;
    }

    /**
     * Returns the property name for a given property.
     *
     * @param deviceProperty Device property
     */
    public String getPropertyName(final DeviceProperty deviceProperty) {
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
                return "Chromaticity T olerance";
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
            default:
                return "Unknown";
        }
    }
}
