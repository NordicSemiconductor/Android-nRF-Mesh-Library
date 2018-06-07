package no.nordicsemi.android.meshprovisioner.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class NetworkSettings {

    protected String networkName = "nRF Mesh Network";
    protected String networkKey;
    protected Map<Integer, String> appKeys = new HashMap<>();
    protected ArrayList<Integer> selectedAppKeyIndexes = new ArrayList<>();
    protected int keyIndex = 0;
    protected int ivIndex = 0;
    protected int unicastAddress = 1;
    protected int flags = 0;
    protected int globalTtl = 5; //Random value

    public NetworkSettings() {

    }

    public abstract void setNetworkName(final String networkName);
}
