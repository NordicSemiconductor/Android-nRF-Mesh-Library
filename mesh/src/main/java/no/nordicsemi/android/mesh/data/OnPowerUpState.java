package no.nordicsemi.android.mesh.data;

/**
 * BT_MESH_ON_POWER_UP_OFF - The OnOff state is initialized to Off.
 * BT_MESH_ON_POWER_UP_ON - The OnOff state is initialized to On. If any other states are bound to the On Power Up state, they are initialized to their default values.
 * BT_MESH_ON_POWER_UP_RESTORE - The OnOff state is initialized to its last known value. If any other states are bound to the On Power Up state, they are initialized to their default values.
 */
public enum OnPowerUpState {
    BT_MESH_ON_POWER_UP_OFF(0x00),
    BT_MESH_ON_POWER_UP_ON(0x01),
    BT_MESH_ON_POWER_UP_RESTORE(0x02),
    BT_MESH_UNKNOWN(0x03);

    private final int value;

    OnPowerUpState(int value) {
        this.value = value;
    }

    public static OnPowerUpState fromValue(Integer value) {
        for (OnPowerUpState state : OnPowerUpState.values()) {
            if (state.value == value) {
                return state;
            }
        }

        return BT_MESH_UNKNOWN;
    }

    public int getValue() {
        return value;
    }
}
