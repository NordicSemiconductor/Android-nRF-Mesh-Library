package no.nordicsemi.android.nrfmesh.node;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.Features;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.NodeKey;
import no.nordicsemi.android.mesh.models.ConfigurationServerModel;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatPublicationSet;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.HeartbeatPublication;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.GroupCallbacks;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityHeartbeatPublicationBinding;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.keys.NetKeysActivity;
import no.nordicsemi.android.nrfmesh.node.dialog.DestinationAddressCallbacks;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentHeartbeatDestination;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentHeartbeatPublishTtl;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentTtl;
import no.nordicsemi.android.nrfmesh.viewmodels.HeartbeatViewModel;

import static no.nordicsemi.android.mesh.Features.DISABLED;
import static no.nordicsemi.android.mesh.Features.ENABLED;
import static no.nordicsemi.android.mesh.utils.Heartbeat.COUNT_MIN;
import static no.nordicsemi.android.mesh.utils.Heartbeat.DEFAULT_PUBLICATION_TTL;
import static no.nordicsemi.android.mesh.utils.Heartbeat.PERIOD_LOG_MIN;
import static no.nordicsemi.android.mesh.utils.Heartbeat.calculateHeartbeatCount;
import static no.nordicsemi.android.mesh.utils.Heartbeat.calculateHeartbeatPeriod;
import static no.nordicsemi.android.mesh.utils.Heartbeat.periodToTime;
import static no.nordicsemi.android.nrfmesh.utils.Utils.CONNECT_TO_NETWORK;
import static no.nordicsemi.android.nrfmesh.utils.Utils.EXTRA_DATA;
import static no.nordicsemi.android.nrfmesh.utils.Utils.HEARTBEAT_PUBLICATION_NET_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.RESULT_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.SELECT_KEY;

@AndroidEntryPoint
public class HeartbeatPublicationActivity extends AppCompatActivity implements
        GroupCallbacks,
        DestinationAddressCallbacks,
        DialogFragmentTtl.DialogFragmentTtlListener {

    private static final String ADDRESS = "ADDRESS";
    private static final String COUNT_LOG = "COUNT_LOG";
    private static final String PERIOD_LOG = "PERIOD_LOG";
    private static final String RELAY = "RELAY";
    private static final String PROXY = "PROXY";
    private static final String FRIEND = "FRIEND";
    private static final String LOW_POWER = "LOW_POWER";
    private static final String TTL = "TTL";
    private static final String NET_KEY = "NET_KEY";

    private ActivityHeartbeatPublicationBinding binding;
    private HeartbeatViewModel mViewModel;

    private boolean mIsConnected;
    private int mDestination;
    private NetworkKey mNetKey;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHeartbeatPublicationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mViewModel = new ViewModelProvider(this).get(HeartbeatViewModel.class);

        final ConfigurationServerModel meshModel = (ConfigurationServerModel) mViewModel.getSelectedModel().getValue();
        if (meshModel == null)
            finish();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setTitle(R.string.title_heartbeat_publication);

        final NestedScrollView scrollView = binding.scrollView;
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (scrollView.getScrollY() == 0) {
                binding.fabApply.extend();
            } else {
                binding.fabApply.shrink();
            }
        });

        mViewModel.isConnectedToProxy().observe(this, isConnected -> {
            if (isConnected != null) {
                mIsConnected = isConnected;
            }
            invalidateOptionsMenu();
        });

        binding.containerPublishAddress.setOnClickListener(v -> {
            final ArrayList<Group> groups = new ArrayList<>();
            for (Group group : mViewModel.getNetworkLiveData().getMeshNetwork().getGroups()) {
                if (MeshAddress.isValidGroupAddress(group.getAddress()))
                    groups.add(group);
            }
            final DialogFragmentHeartbeatDestination destination = DialogFragmentHeartbeatDestination.
                    newInstance(2, groups);
            destination.show(getSupportFragmentManager(), null);
        });

        binding.countSlider.addOnChangeListener((slider, value, fromUser) -> {
            switch ((int) value) {
                case 0:
                    binding.count.setText(getString(R.string.disabled));
                    binding.periodSlider.setEnabled(false);
                    break;
                case 0x12:
                    binding.count.setText(getString(R.string.indefinitely));
                    binding.periodSlider.setEnabled(true);
                    break;
                default:
                    binding.count.setText(String.valueOf(calculateHeartbeatCount((int) value)));
                    binding.periodSlider.setEnabled(true);
                    break;
            }
        });

        binding.periodSlider.addOnChangeListener((slider, value, fromUser) ->
                binding.period.setText(periodToTime(calculateHeartbeatPeriod((short) value))));

        binding.containerPublicationTtl.setOnClickListener(v -> {
            final DialogFragmentTtl fragmentPublishTtl;
            final HeartbeatPublication publication = meshModel.getHeartbeatPublication();
            if (publication != null && publication.isEnabled()) {
                fragmentPublishTtl = DialogFragmentHeartbeatPublishTtl
                        .newInstance(meshModel.getHeartbeatPublication().getTtl());
            } else {
                fragmentPublishTtl = DialogFragmentHeartbeatPublishTtl
                        .newInstance(DEFAULT_PUBLICATION_TTL);
            }
            fragmentPublishTtl.show(getSupportFragmentManager(), null);
        });

        binding.containerNetKeyIndex.setOnClickListener(v -> {
            final Intent netKeysIntent = new Intent(this, NetKeysActivity.class);
            netKeysIntent.putExtra(EXTRA_DATA, HEARTBEAT_PUBLICATION_NET_KEY);
            startActivityForResult(netKeysIntent, SELECT_KEY);
        });

        binding.fabApply.setOnClickListener(v -> {
            if (!checkConnectivity()) return;
            setPublication();
        });

        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        if (meshModel.getHeartbeatPublication() != null) {
            final HeartbeatPublication publication = meshModel.getHeartbeatPublication();
            updateCountLog(publication.getCountLog());
            updatePeriodLog(publication.getPeriodLog());
            updateDestinationAddress(publication.getDst());
            updateTtl(publication.getTtl());
            updateNetKeyIndex(mNetKey = mViewModel.getNetworkLiveData().getMeshNetwork().getNetKey(publication.getNetKeyIndex()));
            final Features features = publication.getFeatures();
            updateFeatures(features.isRelayFeatureSupported(), features.getRelay(),
                    features.isProxyFeatureSupported(), features.getProxy(),
                    features.isFriendFeatureSupported(), features.getFriend(),
                    features.getFriend());
        } else {
            updateCountLog(COUNT_MIN);
            updatePeriodLog(PERIOD_LOG_MIN);
            updateDestinationAddress(mDestination);
            updateTtl(5);
            updateNetKeyIndex(mNetKey = mViewModel.getNetworkLiveData().getMeshNetwork().getPrimaryNetworkKey());
            if (node != null) {
                final Features features = node.getNodeFeatures();
                updateFeatures(features.isRelayFeatureSupported(), features.getRelay(),
                        features.isProxyFeatureSupported(), features.getProxy(),
                        features.isFriendFeatureSupported(), features.getFriend(), features.getFriend());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (mIsConnected) {
            getMenuInflater().inflate(R.menu.disconnect, menu);
        } else {
            getMenuInflater().inflate(R.menu.connect, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_connect) {
            mViewModel.navigateToScannerActivity(this, false, CONNECT_TO_NETWORK, false);
            return true;
        } else if (id == R.id.action_disconnect) {
            mViewModel.disconnect();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_KEY) {
            if (resultCode == RESULT_OK) {
                final NetworkKey netKey = data.getParcelableExtra(RESULT_KEY);
                if (netKey != null) {
                    updateNetKeyIndex(mNetKey = netKey);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ADDRESS, mDestination);
        outState.putByte(COUNT_LOG, getCountLog());
        outState.putByte(PERIOD_LOG, getPeriodLog());
        outState.putBoolean(RELAY, binding.checkRelay.isChecked());
        outState.putBoolean(PROXY, binding.checkProxy.isChecked());
        outState.putBoolean(FRIEND, binding.checkFriend.isChecked());
        outState.putBoolean(LOW_POWER, binding.checkLowPower.isChecked());
        outState.putInt(TTL, Integer.parseInt(binding.publicationTtl.getText().toString()));
        outState.putParcelable(NET_KEY, mNetKey);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        updateDestinationAddress(mDestination = savedInstanceState.getInt(ADDRESS, 0));
        updatePeriodLog(savedInstanceState.getByte(PERIOD_LOG));
        updateCountLog(savedInstanceState.getByte(COUNT_LOG));
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        if (node != null) {
            final Features features = node.getNodeFeatures();
            updateFeatures(features.isRelayFeatureSupported(), savedInstanceState.getBoolean(RELAY) ? 1 : 0,
                    features.isProxyFeatureSupported(), savedInstanceState.getBoolean(PROXY) ? 1 : 0,
                    features.isFriendFeatureSupported(), savedInstanceState.getBoolean(FRIEND) ? 1 : 0,
                    savedInstanceState.getBoolean(LOW_POWER) ? 1 : 0);
            mNetKey = savedInstanceState.getParcelable(NET_KEY);
            if (mNetKey == null) {
                final NodeKey key = node.getAddedNetKeys().get(0);
                mNetKey = mViewModel.getNetworkLiveData().getMeshNetwork().getNetKey(key.getIndex());
            }
            updateNetKeyIndex(mNetKey);
        }
        updateTtl(savedInstanceState.getInt(TTL, DEFAULT_PUBLICATION_TTL));
    }

    @Override
    public Group createGroup(@NonNull final String name) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        if (network != null) {
            return network.createGroup(network.getSelectedProvisioner(), name);
        }
        return null;
    }

    @Override
    public Group createGroup(@NonNull final UUID uuid, final String name) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        if (network != null) {
            return network.createGroup(uuid, null, name);
        }
        return null;
    }

    @Override
    public boolean onGroupAdded(@NonNull final Group group) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        if (network != null) {
            if (network.addGroup(group)) {
                onDestinationAddressSet(group);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onGroupAdded(@NonNull final String name, final int address) {
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        if (network != null) {
            final Group group = network.createGroup(network.getSelectedProvisioner(), address, name);
            if (network.addGroup(group)) {
                onDestinationAddressSet(group);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestinationAddressSet(final int address) {
        updateDestinationAddress(mDestination = address);
    }

    @Override
    public void onDestinationAddressSet(@NonNull final Group group) {
        updateDestinationAddress(mDestination = group.getAddress());
    }

    @Override
    public void setPublishTtl(final int ttl) {
        updateTtl(ttl);
    }

    private void updateDestinationAddress(final int address) {
        if (address == 0) {
            binding.publishAddress.setText(getString(R.string.not_assigned));
        } else
            binding.publishAddress.setText(MeshAddress.formatAddress(address, true));
    }

    private void updateCountLog(final int countLog) {
        try {
            binding.countSlider.setValue(countLog);
        } catch (Exception ex) {
            Log.d("TAG", "Weird crash");
        }
    }

    private void updatePeriodLog(final int periodLog) {
        try {
            binding.periodSlider.setValue(periodLog == 0 ? 1 : periodLog);
        } catch (Exception ex) {
            Log.d("TAG", "Weird crash");
        }
    }

    private void updateFeatures(final boolean relaySupported,
                                final int relay,
                                final boolean proxySupported,
                                final int proxy,
                                final boolean friendSupported,
                                final int friend,
                                final int lowPower) {
        binding.checkRelay.setEnabled(relaySupported);
        if (relaySupported)
            binding.checkRelay.setChecked(relay == ENABLED);
        binding.checkProxy.setEnabled(proxySupported);
        if (proxySupported)
            binding.checkProxy.setChecked(proxy == ENABLED);
        binding.checkFriend.setEnabled(friendSupported);
        if (friendSupported)
            binding.checkFriend.setChecked(friend == ENABLED);
        binding.checkLowPower.setEnabled(friendSupported);
        if (friendSupported)
            binding.checkLowPower.setChecked(lowPower == ENABLED);
    }

    private void updateTtl(final int ttl) {
        binding.publicationTtl.setText(String.valueOf(ttl));
    }

    private void updateNetKeyIndex(final NetworkKey key) {
        if (key != null) {
            binding.netKey.setText(getString(R.string.key_name_and_index, key.getName(), key.getKeyIndex()));
        }
    }

    private byte getCountLog() {
        return (byte) binding.countSlider.getValue();
    }

    private byte getPeriodLog() {
        return (byte) binding.periodSlider.getValue();
    }

    private int getDefaultTtl() {
        return Integer.parseInt(binding.publicationTtl.getText().toString());
    }

    public Features getFeatures() {
        final int relay = (!binding.checkRelay.isEnabled() || !binding.checkRelay.isChecked()) ? DISABLED : ENABLED;
        final int proxy = (!binding.checkProxy.isEnabled() || !binding.checkProxy.isChecked()) ? DISABLED : ENABLED;
        final int friend = (!binding.checkFriend.isEnabled() || !binding.checkFriend.isChecked()) ? DISABLED : ENABLED;
        final int lowPower = (!binding.checkLowPower.isEnabled() || !binding.checkLowPower.isChecked()) ? DISABLED : ENABLED;
        return new Features(friend, lowPower, proxy, relay);
    }

    private void setPublication() {
        if (mDestination == 0) {
            mViewModel.displaySnackBar(this, binding.container, getString(R.string.error_set_dst), Snackbar.LENGTH_SHORT);
            return;
        }
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        final Element element = mViewModel.getSelectedElement().getValue();
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        final MeshMessage configHeartbeatPublicationSet;
        if (node != null && element != null && model != null) {
            try {
                configHeartbeatPublicationSet = new ConfigHeartbeatPublicationSet(mDestination,
                        getCountLog(), getPeriodLog(), getDefaultTtl(),
                        getFeatures(), mNetKey.getKeyIndex());
                mViewModel.getMeshManagerApi().createMeshPdu(node.getUnicastAddress(), configHeartbeatPublicationSet);
            } catch (IllegalArgumentException ex) {
                final DialogFragmentError message = DialogFragmentError.
                        newInstance(getString(R.string.title_error), ex.getMessage());
                message.show(getSupportFragmentManager(), null);
                return;
            }
        }
        final Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    protected final boolean checkConnectivity() {
        if (!mIsConnected) {
            mViewModel.displayDisconnectedSnackBar(this, binding.container);
            return false;
        }
        return true;
    }

    protected void sendMessage(final int address, @NonNull final MeshMessage meshMessage) {
        try {
            if (!checkConnectivity())
                return;
            mViewModel.getMeshManagerApi().createMeshPdu(address, meshMessage);
        } catch (IllegalArgumentException ex) {
            final DialogFragmentError message = DialogFragmentError.
                    newInstance(getString(R.string.title_error), ex.getMessage());
            message.show(getSupportFragmentManager(), null);
        }
    }
}
