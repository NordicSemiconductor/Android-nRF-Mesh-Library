package no.nordicsemi.android.nrfmesh.node;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.models.ConfigurationServerModel;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatSubscriptionGet;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatSubscriptionSet;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatSubscriptionStatus;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.HeartbeatSubscription;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.GroupCallbacks;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.di.Injectable;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentTransactionStatus;
import no.nordicsemi.android.nrfmesh.node.dialog.DestinationAddressCallbacks;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentHeartbeatDestination;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentHeartbeatSource;
import no.nordicsemi.android.nrfmesh.viewmodels.HeartbeatViewModel;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static no.nordicsemi.android.mesh.utils.Heartbeat.COUNT_MAX;
import static no.nordicsemi.android.mesh.utils.Heartbeat.COUNT_MIN;
import static no.nordicsemi.android.mesh.utils.Heartbeat.DO_NOT_SEND_PERIODICALLY;
import static no.nordicsemi.android.mesh.utils.Heartbeat.PERIOD_MAX;
import static no.nordicsemi.android.mesh.utils.Heartbeat.PERIOD_MIN;
import static no.nordicsemi.android.mesh.utils.Heartbeat.SEND_INDEFINITELY;
import static no.nordicsemi.android.nrfmesh.utils.Utils.CONNECT_TO_NETWORK;

public class HeartbeatSubscriptionActivity extends AppCompatActivity implements Injectable,
        GroupCallbacks,
        DestinationAddressCallbacks,
        DialogFragmentHeartbeatSource.SubscriptionAddressCallbacks,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String SOURCE = "SOURCE";
    private static final String DESTINATION = "DESTINATION";
    private static final String PERIOD_LOG = "PERIOD_LOG";
    private static final String COUNT_LOG = "COUNT_LOG";
    private static final String MIN_HOPS = "MIN_HOPS";
    private static final String MAX_HOPS = "MAX_HOPS";

    private HeartbeatViewModel mViewModel;
    private ConfigurationServerModel mMeshModel;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.container)
    CoordinatorLayout mContainer;
    @BindView(R.id.fab_apply)
    ExtendedFloatingActionButton fabApply;
    @BindView(R.id.source_address)
    TextView sourceAddress;
    @BindView(R.id.destination_address)
    TextView destinationAddress;
    @BindView(R.id.subscription_count_group)
    RadioGroup subscriptionCountGroup;
    @BindView(R.id.subscription_count_container)
    ConstraintLayout subscriptionCountContainer;
    @BindView(R.id.count)
    TextView publicationCount;
    @BindView(R.id.count_slider)
    Slider countSlider;
    @BindView(R.id.publication_period_group)
    RadioGroup subscriptionPeriodGroup;
    @BindView(R.id.period_slider)
    Slider periodSlider;
    @BindView(R.id.subscription_period_container)
    ConstraintLayout subscriptionPeriodContainer;
    @BindView(R.id.period)
    TextView publicationPeriod;
    @BindView(R.id.min_hops)
    TextView minHops;
    @BindView(R.id.max_hops)
    TextView maxHops;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private boolean mIsConnected;
    private int mSource;
    private int mDestination;
    private int mMinHops;
    private int mMaxHops;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartbeat_subscription);
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
        getSupportActionBar().setTitle(R.string.title_heartbeat_subscription);
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

        findViewById(R.id.container_src_address).setOnClickListener(v -> {
            final DialogFragmentHeartbeatSource source = DialogFragmentHeartbeatSource.
                    newInstance(meshModel.getHeartbeatSubscription());
            source.show(getSupportFragmentManager(), null);
        });

        findViewById(R.id.container_dst_address).setOnClickListener(v -> {
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

        subscriptionCountGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.publication_count_2:
                    subscriptionCountContainer.setVisibility(VISIBLE);
                    break;
                case R.id.publication_count_1:
                case R.id.publication_count_3:
                    subscriptionCountContainer.setVisibility(GONE);
                    if (subscriptionPeriodGroup.getCheckedRadioButtonId() == R.id.publication_period_rb_1)
                        subscriptionPeriodContainer.setVisibility(GONE);
                    break;
            }
        });

        periodSlider.setValueFrom(PERIOD_MIN);
        periodSlider.setValueTo(PERIOD_MAX);
        periodSlider.addOnChangeListener((slider, value, fromUser) ->
                publicationPeriod.setText(String.format(getString(R.string.seconds),
                        MeshParserUtils.calculateHeartbeatPublicationPeriod((int) value))));

        subscriptionPeriodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.subscription_period_rb_1:
                    subscriptionPeriodContainer.setVisibility(GONE);
                    break;
                case R.id.subscription_period_rb_2:
                    if (subscriptionPeriodContainer.getVisibility() != VISIBLE)
                        subscriptionPeriodContainer.setVisibility(VISIBLE);
                    break;
            }
        });

        fabApply.setOnClickListener(v -> {
            if (!checkConnectivity()) return;
            setSubscription();
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
            updateSubscriptionValues();
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
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SOURCE, mSource);
        outState.putInt(DESTINATION, mDestination);
        outState.putInt(PERIOD_LOG, getPeriodLog());
        outState.putInt(COUNT_LOG, getCountLog());
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null) {
            final HeartbeatSubscription subscription = ((ConfigurationServerModel) model).getHeartbeatSubscription();
            if (subscription != null) {
                outState.putInt(MIN_HOPS, Integer.parseInt(minHops.getText().toString()));
                outState.putInt(MAX_HOPS, Integer.parseInt(maxHops.getText().toString()));
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        updateSourceAddress(mSource = savedInstanceState.getInt(SOURCE, 0));
        updateDestinationAddress(mDestination = savedInstanceState.getInt(DESTINATION, 0));
        updatePeriodLog(savedInstanceState.getInt(PERIOD_LOG));
        updateCountLog(savedInstanceState.getInt(COUNT_LOG));
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null) {
            final HeartbeatSubscription subscription = ((ConfigurationServerModel) model).getHeartbeatSubscription();
            if (subscription != null) {
                updateHops(mMinHops = savedInstanceState.getInt(MIN_HOPS, 0), mMaxHops = savedInstanceState.getInt(MAX_HOPS, 0));
            }
        }
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
    public void setSubscriptionSource(final int sourceAddress) {
        updateSourceAddress(mSource = sourceAddress);
    }

    @Override
    public void onDestinationAddressSet(final int address) {
        updateDestinationAddress(mDestination = address);
    }

    @Override
    public void onDestinationAddressSet(@NonNull final Group group) {
        updateDestinationAddress(mDestination = group.getAddress());
    }

    protected void updateMeshMessage(final MeshMessage meshMessage) {
        if (meshMessage instanceof ConfigHeartbeatSubscriptionStatus) {
            mViewModel.removeMessage();
            mSwipe.setRefreshing(false);
            progressBar.setVisibility(GONE);
            updateSubscriptionValues();
            mViewModel.displaySnackBar(this, mContainer,
                    getString(R.string.operation_success), Snackbar.LENGTH_SHORT);
        }
    }

    private void updateSubscriptionValues() {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        final HeartbeatSubscription subscription = mMeshModel.getHeartbeatSubscription();
        if (subscription != null) {
            mSource = subscription.getSrc();
            mDestination = subscription.getDst();
            mMinHops = subscription.getMinHops();
            mMaxHops = subscription.getMaxHops();
            updateSourceAddress(mSource);
            updateDestinationAddress(mDestination);
            updateCountLog(subscription.getCountLog());
            updatePeriodLog(subscription.getPeriodLog());
            updateHops(mMinHops, mMaxHops);
        } else {
            if (node != null) {
                updateSourceAddress(MeshAddress.UNASSIGNED_ADDRESS);
                updateDestinationAddress(MeshAddress.UNASSIGNED_ADDRESS);
                updatePeriodLog(DO_NOT_SEND_PERIODICALLY);
                updateCountLog(DO_NOT_SEND_PERIODICALLY);
            }
        }
    }

    private void updateSourceAddress(final int address) {
        mSource = address;
        sourceAddress.setText(MeshAddress.formatAddress(address, true));
    }

    private void updateDestinationAddress(final int address) {
        destinationAddress.setText(MeshAddress.formatAddress(address, true));
    }

    private void updateCountLog(final int countLog) {
        switch (countLog) {
            case DO_NOT_SEND_PERIODICALLY:
                subscriptionCountGroup.check(R.id.subscription_count_1);
                break;
            case SEND_INDEFINITELY:
                subscriptionCountGroup.check(R.id.subscription_count_3);
                break;
            default:
                subscriptionCountGroup.check(R.id.subscription_count_2);
                subscriptionCountContainer.setVisibility(VISIBLE);
                countSlider.setValue(countLog);
                break;
        }
    }

    private void updatePeriodLog(final int periodLog) {
        if (periodLog == DO_NOT_SEND_PERIODICALLY) {
            subscriptionPeriodGroup.check(R.id.subscription_period_rb_1);
        } else {
            subscriptionPeriodGroup.check(R.id.subscription_period_rb_2);
            periodSlider.setValue(periodLog);
        }
    }

    private void updateHops(final int minHops, final int maxHops) {
        this.minHops.setText(String.valueOf(minHops));
        this.maxHops.setText(String.valueOf(maxHops));
    }

    private int getCountLog() {
        switch (subscriptionCountGroup.getCheckedRadioButtonId()) {
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
        switch (subscriptionPeriodGroup.getCheckedRadioButtonId()) {
            default:
            case R.id.subscription_period_rb_1:
                return DO_NOT_SEND_PERIODICALLY;
            case R.id.subscription_period_rb_2:
                return (int) periodSlider.getValue();
        }
    }

    private void setSubscription() {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        final Element element = mViewModel.getSelectedElement().getValue();
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        final MeshMessage heartbeatSubscription;
        if (node != null && element != null && model != null) {
            try {
                heartbeatSubscription = new ConfigHeartbeatSubscriptionSet(mSource,
                        mDestination, getPeriodLog());
                sendMessage(node.getUnicastAddress(), heartbeatSubscription);
                //mViewModel.getMeshManagerApi().createMeshPdu(node.getUnicastAddress(), heartbeatSubscription);
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
            mViewModel.getMessageQueue().add(new ConfigHeartbeatSubscriptionGet());
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
