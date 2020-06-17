package no.nordicsemi.android.nrfmesh.node;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.mesh.Features;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.NodeKey;
import no.nordicsemi.android.mesh.models.ConfigurationServerModel;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatPublicationGet;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatPublicationSet;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatPublicationStatus;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.HeartbeatPublication;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.GroupCallbacks;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.di.Injectable;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentTransactionStatus;
import no.nordicsemi.android.nrfmesh.keys.NetKeysActivity;
import no.nordicsemi.android.nrfmesh.node.dialog.DestinationAddressCallbacks;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentHeartbeatDestination;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentHeartbeatPublishTtl;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentTtl;
import no.nordicsemi.android.nrfmesh.viewmodels.HeartbeatViewModel;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static no.nordicsemi.android.mesh.Features.DISABLED;
import static no.nordicsemi.android.mesh.Features.ENABLED;
import static no.nordicsemi.android.mesh.Features.UNSUPPORTED;
import static no.nordicsemi.android.mesh.utils.Heartbeat.COUNT_MAX;
import static no.nordicsemi.android.mesh.utils.Heartbeat.COUNT_MIN;
import static no.nordicsemi.android.mesh.utils.Heartbeat.DEFAULT_PUBLICATION_TTL;
import static no.nordicsemi.android.mesh.utils.Heartbeat.DO_NOT_SEND_PERIODICALLY;
import static no.nordicsemi.android.mesh.utils.Heartbeat.PERIOD_MAX;
import static no.nordicsemi.android.mesh.utils.Heartbeat.PERIOD_MIN;
import static no.nordicsemi.android.mesh.utils.Heartbeat.SEND_INDEFINITELY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.CONNECT_TO_NETWORK;
import static no.nordicsemi.android.nrfmesh.utils.Utils.EXTRA_DATA;
import static no.nordicsemi.android.nrfmesh.utils.Utils.HEARTBEAT_PUBLICATION_NET_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.RESULT_KEY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.SELECT_KEY;

public class HeartbeatPublicationActivity extends AppCompatActivity implements Injectable,
        GroupCallbacks,
        DestinationAddressCallbacks,
        DialogFragmentTtl.DialogFragmentTtlListener, SwipeRefreshLayout.OnRefreshListener {

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
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.container)
    CoordinatorLayout mContainer;
    @BindView(R.id.fab_apply)
    ExtendedFloatingActionButton fabApply;
    @BindView(R.id.publish_address)
    TextView destinationAddress;
    @BindView(R.id.publication_count_group)
    RadioGroup publicationCountGroup;
    @BindView(R.id.publication_count_container)
    ConstraintLayout publicationCountContainer;
    @BindView(R.id.count)
    TextView publicationCount;
    @BindView(R.id.count_slider)
    Slider countSlider;
    @BindView(R.id.publication_period_group)
    RadioGroup publicationPeriodGroup;
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
    @BindView(R.id.net_key_index)
    TextView netKeyIndex;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private boolean mIsConnected;
    private int mPublishAddress;
    private int mTtl = 5;
    private NetworkKey mNetKey;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartbeat_publication);
        ButterKnife.bind(this);

        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(HeartbeatViewModel.class);

        final ConfigurationServerModel meshModel = mMeshModel = (ConfigurationServerModel) mViewModel.getSelectedModel().getValue();
        if (meshModel == null)
            finish();

        //Setup views
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setTitle(R.string.title_heartbeat_publication);
        mSwipe.setOnRefreshListener(this);

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
            List<Group> groups = mViewModel.getNetworkLiveData().getMeshNetwork().getGroups();
            final DialogFragmentHeartbeatDestination destination = DialogFragmentHeartbeatDestination.
                    newInstance(meshModel.getHeartbeatPublication(), (ArrayList<Group>) groups);
            destination.show(getSupportFragmentManager(), null);
        });

        countSlider.setValueFrom(COUNT_MIN);
        countSlider.setValueTo(COUNT_MAX);
        countSlider.addOnChangeListener((slider, value, fromUser) ->
                publicationCount.setText(String.format(getString(R.string.messages),
                        MeshParserUtils.calculateHeartbeatPublicationCount((int) value))));

        publicationCountGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.publication_count_2:
                    enableDisableRadioGroup(true);
                    enableDisableViews(publicationPeriodContainer, true);
                    publicationCountContainer.setVisibility(VISIBLE);
                    break;
                case R.id.publication_count_1:
                case R.id.publication_count_3:
                    enableDisableRadioGroup(false);
                    publicationCountContainer.setVisibility(GONE);
                    if (publicationPeriodGroup.getCheckedRadioButtonId() == R.id.publication_period_rb_1)
                        publicationPeriodContainer.setVisibility(GONE);
                    else enableDisableViews(publicationPeriodContainer, false);
                    break;
            }
        });

        periodSlider.setValueFrom(PERIOD_MIN);
        periodSlider.setValueTo(PERIOD_MAX);
        periodSlider.addOnChangeListener((slider, value, fromUser) ->
                publicationPeriod.setText(String.format(getString(R.string.seconds),
                        MeshParserUtils.calculateHeartbeatPublicationPeriod((int) value))));

        publicationPeriodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.publication_period_rb_1:
                    publicationPeriodContainer.setVisibility(GONE);
                    break;
                case R.id.publication_period_rb_2:
                    enableDisableViews(publicationPeriodContainer, true);
                    if (publicationPeriodContainer.getVisibility() != VISIBLE)
                        publicationPeriodContainer.setVisibility(VISIBLE);
                    break;
            }
        });

        actionPublishTtl.setOnClickListener(v -> {
            if (meshModel != null && meshModel.getHeartbeatPublication() != null) {
                final DialogFragmentTtl fragmentPublishTtl = DialogFragmentHeartbeatPublishTtl
                        .newInstance(meshModel.getHeartbeatPublication().getTtl());
                fragmentPublishTtl.show(getSupportFragmentManager(), null);
            } else {
                final DialogFragmentTtl fragmentPublishTtl = DialogFragmentHeartbeatPublishTtl
                        .newInstance(DEFAULT_PUBLICATION_TTL);
                fragmentPublishTtl.show(getSupportFragmentManager(), null);
            }
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

        mViewModel.getMeshMessage().observe(this, this::updateMeshMessage);

        mViewModel.getTransactionStatus().observe(this, transactionStatus -> {
            if (transactionStatus != null) {
                mSwipe.setRefreshing(false);
                progressBar.setVisibility(GONE);
                final String message = getString(R.string.operation_timed_out);
                DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.newInstance("Transaction Failed", message);
                fragmentMessage.show(getSupportFragmentManager(), null);
            }
        });
        countSlider.setValue(1);
        periodSlider.setValue(1);
        if (savedInstanceState == null) {
            updatePublicationValues();
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
        outState.putInt(ADDRESS, mPublishAddress);
        outState.putInt(COUNT_LOG, getCountLog());
        outState.putInt(PERIOD_LOG, getPeriodLog());
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
        updateDestinationAddress(mPublishAddress = savedInstanceState.getInt(ADDRESS, 0));
        updatePeriodLog(savedInstanceState.getInt(PERIOD_LOG));
        updateCountLog(savedInstanceState.getInt(COUNT_LOG));
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
        updateTtl(mTtl = savedInstanceState.getInt(TTL, DEFAULT_PUBLICATION_TTL));
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
        updateDestinationAddress(mPublishAddress = address);
    }

    @Override
    public void onDestinationAddressSet(@NonNull final Group group) {
        updateDestinationAddress(mPublishAddress = group.getAddress());
    }

    @Override
    public void setPublishTtl(final int ttl) {
        updateTtl(mTtl = ttl);
    }

    protected void updateMeshMessage(final MeshMessage meshMessage) {
        if (meshMessage instanceof ConfigHeartbeatPublicationStatus) {
            mViewModel.removeMessage();
            mSwipe.setRefreshing(false);
            progressBar.setVisibility(GONE);
            updatePublicationValues();
            mViewModel.displaySnackBar(this, mContainer,
                    getString(R.string.operation_success), Snackbar.LENGTH_SHORT);
        }
    }

    private void updatePublicationValues() {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        final HeartbeatPublication publication = mMeshModel.getHeartbeatPublication();
        final Features features;
        if (publication != null) {
            mPublishAddress = publication.getDstAddress();
            mTtl = publication.getTtl();
            final int keyIndex = publication.getNetKeyIndex();
            mNetKey = mViewModel.getNetworkLiveData().getMeshNetwork().getNetKey(keyIndex);
            updateDestinationAddress(mPublishAddress);
            updateCountLog(publication.getCountLog());
            updatePeriodLog(publication.getPeriodLog());
            features = publication.getFeatures();
            updateFeatures(features.isRelayFeatureSupported(), features.getRelay(),
                    features.isProxyFeatureSupported(), features.getProxy(),
                    features.isFriendFeatureSupported(), features.getFriend(),
                    features.isLowPowerFeatureSupported(), features.getLowPower());
            updateTtl(mTtl);
            updateNetKeyIndex(mNetKey);
        } else {
            if (node != null) {
                updateDestinationAddress(MeshAddress.UNASSIGNED_ADDRESS);
                features = node.getNodeFeatures();
                updateCountLog(DO_NOT_SEND_PERIODICALLY);
                updatePeriodLog(DO_NOT_SEND_PERIODICALLY);
                updateFeatures(features.isRelayFeatureSupported(), features.getRelay(),
                        features.isProxyFeatureSupported(), features.getProxy(),
                        features.isFriendFeatureSupported(), features.getFriend(),
                        features.isLowPowerFeatureSupported(), features.getLowPower());
                updateTtl(mTtl);
                final NodeKey nodeKey = node.getAddedNetKeys().get(0);
                mNetKey = mViewModel.getNetworkLiveData().getMeshNetwork().getNetKey(nodeKey.getIndex());
                updateNetKeyIndex(mNetKey);
            }
        }
    }

    private void updateDestinationAddress(final int address) {
        destinationAddress.setText(MeshAddress.formatAddress(address, true));
    }

    private void updateCountLog(final int countLog) {
        switch (countLog) {
            case DO_NOT_SEND_PERIODICALLY:
                publicationCountGroup.check(R.id.publication_count_1);
                break;
            case SEND_INDEFINITELY:
                publicationCountGroup.check(R.id.publication_count_3);
                break;
            default:
                publicationCountGroup.check(R.id.publication_count_2);
                publicationCountContainer.setVisibility(VISIBLE);
                countSlider.setValue(countLog);
                break;
        }
    }

    private void updatePeriodLog(final int periodLog) {
        if (periodLog == DO_NOT_SEND_PERIODICALLY) {
            publicationPeriodGroup.check(R.id.publication_period_rb_1);
        } else {
            publicationPeriodGroup.check(R.id.publication_period_rb_2);
            periodSlider.setValue(periodLog);
        }
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

    private int getCountLog() {
        switch (publicationCountGroup.getCheckedRadioButtonId()) {
            default:
            case R.id.publication_count_1:
                return DO_NOT_SEND_PERIODICALLY;
            case R.id.publication_count_2:
                return (int) countSlider.getValue();
            case R.id.publication_count_3:
                return SEND_INDEFINITELY;
        }
    }

    private int getPeriodLog() {
        switch (publicationPeriodGroup.getCheckedRadioButtonId()) {
            default:
            case R.id.publication_period_rb_1:
                return DO_NOT_SEND_PERIODICALLY;
            case R.id.publication_period_rb_2:
                return (int) periodSlider.getValue();
        }
    }

    public Features getFeatures() {
        final int relay = checkBoxRelay.isEnabled() ? checkBoxRelay.isChecked() ? ENABLED : DISABLED : UNSUPPORTED;
        final int proxy = checkBoxProxy.isEnabled() ? checkBoxProxy.isChecked() ? ENABLED : DISABLED : UNSUPPORTED;
        final int friend = checkBoxFriend.isEnabled() ? checkBoxFriend.isChecked() ? ENABLED : DISABLED : UNSUPPORTED;
        final int lowPower = checkBoxLowPower.isEnabled() ? checkBoxLowPower.isChecked() ? ENABLED : DISABLED : UNSUPPORTED;
        return new Features(friend, lowPower, proxy, relay);
    }

    private void setPublication() {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        final Element element = mViewModel.getSelectedElement().getValue();
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        final MeshMessage configHeartbeatPublicationSet;
        if (node != null && element != null && model != null) {
            try {
                configHeartbeatPublicationSet = new ConfigHeartbeatPublicationSet(mPublishAddress,
                        getCountLog(), getPeriodLog(), mTtl,
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

    private void enableDisableRadioGroup(final boolean flag) {
        for (int i = 0; i < publicationPeriodGroup.getChildCount(); i++) {
            publicationPeriodGroup.getChildAt(i).setEnabled(flag);
        }
    }

    private void enableDisableViews(final ConstraintLayout view, final boolean flag) {
        for (int i = 0; i < view.getChildCount(); i++) {
            view.getChildAt(i).setEnabled(flag);
        }
    }

    @Override
    public void onRefresh() {
        progressBar.setVisibility(VISIBLE);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (!checkConnectivity() || model == null) {
            mSwipe.setRefreshing(false);
            progressBar.setVisibility(GONE);
        }
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        final Element element = mViewModel.getSelectedElement().getValue();
        if (node != null && element != null &&
                model instanceof ConfigurationServerModel) {
            mViewModel.displaySnackBar(this, mContainer, getString(R.string.listing_model_configuration), Snackbar.LENGTH_LONG);
            mViewModel.getMessageQueue().add(new ConfigHeartbeatPublicationGet());
            //noinspection ConstantConditions
            sendMessage(node.getUnicastAddress(), mViewModel.getMessageQueue().peek());
        } else {
            mSwipe.setRefreshing(false);
            progressBar.setVisibility(GONE);
        }
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
