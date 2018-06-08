package no.nordicsemi.android.meshprovisioner.utils;

import java.util.HashMap;
import java.util.Map;

public abstract class NetworkSettings {

    protected String networkKey;
    protected Map<Integer, String> appKeys = new HashMap<>();
    protected int keyIndex = 0;
    protected int ivIndex = 0;
    protected int unicastAddress = 1;
    protected int flags = 0;
    protected int globalTtl = 5; //Random value

    public NetworkSettings() {

    }

    protected abstract void generateProvisioningData();
}
