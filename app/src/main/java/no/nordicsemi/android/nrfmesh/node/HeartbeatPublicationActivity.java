package no.nordicsemi.android.nrfmesh.node;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
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
import static no.nordicsemi.android.mesh.utils.Heartbeat.PERIOD_LOG_MAX;
import static no.nordicsemi.android.mesh.utils.Heartbeat.PERIOD_LOG_MIN;
import static no.nordicsemi.android.mesh.utils.Heartbeat.calculateHeartbeatCount;
import static no.nordicsemi.android.mesh.utils.Heartbeat.calculateHeartbeatPeriod;
import static no.nordicsemi.android.mesh.utils.PeriodLogStateRange.periodToTime;
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

    private HeartbeatViewModel mViewModel;
    private ConfigurationServerModel mMeshModel;


    @BindView(R.id.container)
    CoordinatorLayout mContainer;
    @BindView(R.id.fab_apply)
    ExtendedFloatingActionButton fabApply;
    @BindView(R.id.publish_address)
    TextView destinationAddress;
    @BindView(R.id.publication_count_container)
    ConstraintLayout publicationCountContainer;
    @BindView(R.id.count)
    TextView publicationCount;
    @BindView(R.id.count_slider)
    Slider countSlider;
    @BindView(R.id.period_slider)
    Slider periodSlider;
    @BindView(R.id.publication_period_container)
    ConstraintLayout publicationPeriodContainer;
    @BindView(R.id.period)
    TextView publicationPeriod;
    @BindView(R.id.check_relay)
    CheckBox checkBoxRelay;
    @BindView(R.id.check_proxy)
    CheckBox checkBoxProxy;
    @BindView(R.id.check_friend)
    CheckBox checkBoxFriend;
    @BindView(R.id.check_low_power)
    CheckBox checkBoxLowPower;
    @BindView(R.id.container_publication_ttl)
    View actionPublishTtl;
    @BindView(R.id.publication_ttl)
    TextView heartbeatTtl;
    @BindView(R.id.container_net_key_index)
    View actionNetKeyIndex;
    @BindView(R.id.net_key)
    TextView netKeyIndex;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private boolean mIsConnected;
    private int mDestination;
    private static int DEFAULT_TTL = 5;
    private NetworkKey mNetKey;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartbeat_publication);
        ButterKnife.bind(this);

        mViewModel = new ViewModelProvider(this).get(HeartbeatViewModel.class);

        final ConfigurationServerModel meshModel = mMeshModel = (ConfigurationServerModel) mViewModel.getSelectedModel().getValue();
        if (meshModel == null)
            finish();

        //Setup views
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setTitle(R.string.title_heartbeat_publication);

        final NestedScrollView scrollView = findViewById(R.id.scroll_view);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (scrollView.getScrollY() == 0) {
                fabApply.extend();
            } else {
                fabApply.shrink();
            }
        });

        mViewModel.isConnectedToProxy().observe(this, isConnected -> {
            if (isConnected != null) {
                mIsConnected = isConnected;
            }
            invalidateOptionsMenu();
        });

        findViewById(R.id.container_publish_address).setOnClickListener(v -> {
            final ArrayList<Group> groups = new ArrayList<>();
            for (Group group : mViewModel.getNetworkLiveData().getMeshNetwork().getGroups()) {
                if (MeshAddress.isValidGroupAddress(group.getAddress()))
                    groups.add(group);
            }
            final DialogFragmentHeartbeatDestination destination = DialogFragmentHeartbeatDestination.
                    newInstance(2, groups);
            destination.show(getSupportFragmentManager(), null);
        });

        countSlider.setValueFrom(COUNT_MIN);
        countSlider.setValueTo(0x12);
        countSlider.setStepSize(1);
        countSlider.addOnChangeListener((slider, value, fromUser) -> {
            switch ((int) value) {
                case 0:
                    publicationCount.setText(getString(R.string.disabled));
                    periodSlider.setEnabled(false);
                    break;
                case 0x12:
                    publicationCount.setText(getString(R.string.indefinitely));
                    periodSlider.setEnabled(true);
                    break;
                default:
                    publicationCount.setText(String.valueOf(calculateHeartbeatCount((int) value)));
                    periodSlider.setEnabled(true);
            }
        });

        periodSlider.setValueFrom(PERIOD_LOG_MIN);
        periodSlider.setValueTo(PERIOD_LOG_MAX);
        periodSlider.setStepSize(1);
        periodSlider.addOnChangeListener((slider, value, fromUser) ->
                publicationPeriod.setText(periodToTime(calculateHeartbeatPeriod((short) value))));

        actionPublishTtl.setOnClickListener(v -> {
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

        actionNetKeyIndex.setOnClickListener(v -> {
            final Intent netKeysIntent = new Intent(this, NetKeysActivity.class);
            netKeysIntent.putExtra(EXTRA_DATA, HEARTBEAT_PUBLICATION_NET_KEY);
            startActivityForResult(netKeysIntent, SELECT_KEY);
        });

        fabApply.setOnClickListener(v -> {
            if (!checkConnectivity()) return;
            setPublication();
        });

        countSlider.setValue(1);
        periodSlider.setValue(1);
        updateDestinationAddress(mDestination);
        updateTtl(5);
        updateNetKeyIndex(mNetKey = mViewModel.getNetworkLiveData().getMeshNetwork().getPrimaryNetworkKey());
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        if (node != null) {
            final Features features = node.getNodeFeatures();
            updateFeatures(features.isRelayFeatureSupported(), features.getRelay(),
                    features.isProxyFeatureSupported(), features.getProxy(),
                    features.isFriendFeatureSupported(), features.getFriend(),
                    features.isLowPowerFeatureSupported(), features.getLowPower());
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
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_connect:
                mViewModel.navigateToScannerActivity(this, false, CONNECT_TO_NETWORK, false);
                return true;
            case R.id.action_disconnect:
                mViewModel.disconnect();
                return true;
            default:
                return false;
        }
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
        //if (checkBoxRelay.isEnabled())
        outState.putBoolean(RELAY, checkBoxRelay.isChecked());
        //if (checkBoxProxy.isEnabled())
        outState.putBoolean(PROXY, checkBoxProxy.isChecked());
        //if (checkBoxFriend.isEnabled())
        outState.putBoolean(FRIEND, checkBoxFriend.isChecked());
        //if (checkBoxLowPower.isEnabled())
        outState.putBoolean(LOW_POWER, checkBoxLowPower.isChecked());
        outState.putInt(TTL, Integer.parseInt(heartbeatTtl.getText().toString()));
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
                    features.isLowPowerFeatureSupported(), savedInstanceState.getBoolean(LOW_POWER) ? 1 : 0);
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
            destinationAddress.setText(getString(R.string.not_assigned));
        } else
            destinationAddress.setText(MeshAddress.formatAddress(address, true));
    }

    private void updateCountLog(final int countLog) {
        countSlider.setValue(countLog);
    }

    private void updatePeriodLog(final int periodLog) {
        periodSlider.setValue(periodLog == 0 ? 1 : periodLog);
    }

    private void updateFeatures(final boolean relaySupported,
                                final int relay,
                                final boolean proxySupported,
                                final int proxy,
                                final boolean friendSupported,
                                final int friend,
                                final boolean lowPowerSupported,
                                final int lowPower) {
        checkBoxRelay.setEnabled(relaySupported);
        if (relaySupported)
            checkBoxRelay.setChecked(relay == ENABLED);
        checkBoxProxy.setEnabled(proxySupported);
        if (proxySupported)
            checkBoxProxy.setChecked(proxy == ENABLED);
        checkBoxFriend.setEnabled(friendSupported);
        if (friendSupported)
            checkBoxFriend.setChecked(friend == ENABLED);
        checkBoxLowPower.setEnabled(lowPowerSupported);
        if (lowPowerSupported)
            checkBoxLowPower.setChecked(lowPower == ENABLED);
    }

    private void updateTtl(final int ttl) {
        heartbeatTtl.setText(String.valueOf(ttl));
    }

    private void updateNetKeyIndex(final NetworkKey key) {
        if (key != null) {
            netKeyIndex.setText(getString(R.string.key_name_and_index, key.getName(), key.getKeyIndex()));
        }
    }

    private byte getCountLog() {
        return (byte) countSlider.getValue();
    }

    private byte getPeriodLog() {
        return (byte) periodSlider.getValue();
    }

    private int getDefaultTtl() {
        return Integer.parseInt(heartbeatTtl.getText().toString());
    }

    public Features getFeatures() {
        final int relay = (!checkBoxRelay.isEnabled() || !checkBoxRelay.isChecked()) ? DISABLED : ENABLED;
        final int proxy = (!checkBoxProxy.isEnabled() || !checkBoxProxy.isChecked()) ? DISABLED : ENABLED;
        final int friend = (!checkBoxFriend.isEnabled() || !checkBoxFriend.isChecked()) ? DISABLED : ENABLED;
        final int lowPower = (!checkBoxLowPower.isEnabled() || !checkBoxLowPower.isChecked()) ? DISABLED : ENABLED;
        return new Features(friend, lowPower, proxy, relay);
    }

    private void setPublication() {
        if (mDestination == 0) {
            mViewModel.displaySnackBar(this, mContainer, getString(R.string.error_set_dst), Snackbar.LENGTH_SHORT);
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
            mViewModel.displayDisconnectedSnackBar(this, mContainer);
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
