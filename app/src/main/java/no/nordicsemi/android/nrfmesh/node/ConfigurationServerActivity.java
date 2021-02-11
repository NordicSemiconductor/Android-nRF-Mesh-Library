package no.nordicsemi.android.nrfmesh.node;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.Features;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.models.ConfigurationServerModel;
import no.nordicsemi.android.mesh.transport.ConfigBeaconSet;
import no.nordicsemi.android.mesh.transport.ConfigBeaconStatus;
import no.nordicsemi.android.mesh.transport.ConfigFriendSet;
import no.nordicsemi.android.mesh.transport.ConfigFriendStatus;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatPublicationGet;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatPublicationSet;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatPublicationStatus;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatSubscriptionGet;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatSubscriptionStatus;
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitSet;
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitStatus;
import no.nordicsemi.android.mesh.transport.ConfigNodeIdentitySet;
import no.nordicsemi.android.mesh.transport.ConfigNodeIdentityStatus;
import no.nordicsemi.android.mesh.transport.ConfigRelaySet;
import no.nordicsemi.android.mesh.transport.ConfigRelayStatus;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.HeartbeatPublication;
import no.nordicsemi.android.mesh.utils.HeartbeatSubscription;
import no.nordicsemi.android.mesh.utils.NetworkTransmitSettings;
import no.nordicsemi.android.mesh.utils.RelaySettings;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutConfigServerModelBinding;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentNetworkTransmitSettings;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogRelayRetransmitSettings;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.viewmodels.ModelConfigurationViewModel;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static no.nordicsemi.android.mesh.transport.ProvisionedMeshNode.DISABLED;
import static no.nordicsemi.android.mesh.transport.ProvisionedMeshNode.ENABLED;
import static no.nordicsemi.android.mesh.transport.ProvisionedMeshNode.UNSUPPORTED;
import static no.nordicsemi.android.mesh.utils.MeshAddress.formatAddress;

@AndroidEntryPoint
public class ConfigurationServerActivity extends BaseModelConfigurationActivity implements
        DialogFragmentNetworkTransmitSettings.DialogFragmentNetworkTransmitSettingsListener,
        DialogRelayRetransmitSettings.DialogFragmentRelaySettingsListener {

    private static final String TAG = ConfigurationServerActivity.class.getSimpleName();

    private static final int NETWORK_TRANSMIT_SETTING_UNKNOWN = -1;
    private static final int RELAY_RETRANSMIT_SETTINGS_UNKNOWN = -1;


    private View mContainerHeartbeatPublication;
    private TextView mHeartbeatPublicationDisabled;
    private TextView mHeartbeatPublicationDst;
    private TextView mHeartbeatPublicationPeriod;
    private TextView mHeartbeatPublicationCount;
    private TextView mHeartbeatPublicationFeatures;
    private TextView mHeartbeatPublicationKey;
    private Button mRefreshPublication;
    private Button mClearPublication;
    private Button mSetPublication;

    private View mContainerHeartbeatSubscription;
    private TextView mHeartbeatSubscriptionDisabled;
    private TextView mHeartbeatSubscriptionSrc;
    private TextView mHeartbeatSubscriptionDst;
    private TextView mHeartbeatSubscriptionPeriod;
    private TextView mHeartbeatSubscriptionCount;
    private TextView mHeartbeatSubscriptionMinHops;
    private TextView mHeartbeatSubscriptionMaxHops;
    private Button mRefreshSubscription;
    private Button mClearSubscription;
    private Button mSetSubscription;

    private TextView mRelayRetransmitCountText;
    private TextView mRelayRetransmitIntervalStepsText;
    private TextView mNetworkTransmitCountText;
    private TextView mNetworkTransmitIntervalStepsText;

    private SwitchMaterial switchSnb;
    private SwitchMaterial switchFriend;
    private SwitchMaterial switchNodeIdentity;

    private int mRelayRetransmitCount = RELAY_RETRANSMIT_SETTINGS_UNKNOWN;
    private int mRelayRetransmitIntervalSteps = RELAY_RETRANSMIT_SETTINGS_UNKNOWN;
    private int mNetworkTransmitCount = NETWORK_TRANSMIT_SETTING_UNKNOWN;
    private int mNetworkTransmitIntervalSteps = NETWORK_TRANSMIT_SETTING_UNKNOWN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof ConfigurationServerModel) {
            //Hide the app key binding, publication adn subscription views since the ConfigurationServerModel does not support app key binding
            mContainerAppKeyBinding.setVisibility(GONE);
            mContainerPublication.setVisibility(GONE);
            mContainerSubscribe.setVisibility(GONE);
            final LayoutConfigServerModelBinding nodeControlsContainerBinding = LayoutConfigServerModelBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);

            mContainerHeartbeatPublication = nodeControlsContainerBinding.containerHeartbeatPublication.publicationContainer;
            mHeartbeatPublicationDisabled = nodeControlsContainerBinding.containerHeartbeatPublication.heartbeatPublicationNotSet;
            mHeartbeatPublicationDst = nodeControlsContainerBinding.containerHeartbeatPublication.heartbeatDst;
            mHeartbeatPublicationPeriod = nodeControlsContainerBinding.containerHeartbeatPublication.period;
            mHeartbeatPublicationCount = nodeControlsContainerBinding.containerHeartbeatPublication.remainingCount;
            mHeartbeatPublicationFeatures = nodeControlsContainerBinding.containerHeartbeatPublication.features;
            mHeartbeatPublicationKey = nodeControlsContainerBinding.containerHeartbeatPublication.netKey;

            mContainerHeartbeatSubscription = nodeControlsContainerBinding.containerHeartbeatSubscription.subscriptionContainer;
            mHeartbeatSubscriptionDisabled = nodeControlsContainerBinding.containerHeartbeatSubscription.heartbeatSubscriptionNotSet;
            mHeartbeatSubscriptionSrc = nodeControlsContainerBinding.containerHeartbeatSubscription.heartbeatSrc;
            mHeartbeatSubscriptionDst = nodeControlsContainerBinding.containerHeartbeatSubscription.heartbeatDst;
            mHeartbeatSubscriptionPeriod = nodeControlsContainerBinding.containerHeartbeatSubscription.remainingPeriod;
            mHeartbeatSubscriptionCount = nodeControlsContainerBinding.containerHeartbeatSubscription.count;
            mHeartbeatSubscriptionMinHops = nodeControlsContainerBinding.containerHeartbeatSubscription.minHops;
            mHeartbeatSubscriptionMaxHops = nodeControlsContainerBinding.containerHeartbeatSubscription.maxHops;

            final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
            if (meshNode != null) {
                mRelayRetransmitIntervalStepsText = nodeControlsContainerBinding.relayRetransmitIntervalSteps;
                mActionSetRelayState = nodeControlsContainerBinding.actionRelayRetransmitConfigure;
                mRelayRetransmitCountText = nodeControlsContainerBinding.relayRetransmitCount;
                if (meshNode.getNodeFeatures().isRelayFeatureSupported()) {
                    final CardView relayCardView = findViewById(R.id.config_relay_set_card);
                    relayCardView.setVisibility(VISIBLE);

                    mActionSetRelayState.setOnClickListener(v -> {
                        if (!checkConnectivity(mContainer)) return;
                        final RelaySettings relaySettings = meshNode.getRelaySettings();
                        if (relaySettings != null) {
                            mRelayRetransmitCount = meshNode.getRelaySettings().getRelayTransmitCount();
                            mRelayRetransmitIntervalSteps = meshNode.getRelaySettings().getRelayIntervalSteps();
                        }
                        //Assuming relay is enabled by default
                        final DialogRelayRetransmitSettings fragment = DialogRelayRetransmitSettings
                                .newInstance(1, mRelayRetransmitCount, mRelayRetransmitIntervalSteps);
                        fragment.show(getSupportFragmentManager(), null);
                    });
                }
            }

            mRefreshPublication = nodeControlsContainerBinding.actionRefreshHeartbeatPublication;
            mRefreshPublication.setOnClickListener(v -> sendMessage(new ConfigHeartbeatPublicationGet()));

            mClearPublication = nodeControlsContainerBinding.actionClearHeartbeatPublication;
            mClearPublication.setOnClickListener(v ->
                    sendMessage(new ConfigHeartbeatPublicationSet()));

            mSetPublication = nodeControlsContainerBinding.actionSetHeartbeatPublication;
            mSetPublication.setOnClickListener(v -> {
                final Intent heartbeatPublication = new Intent(this, HeartbeatPublicationActivity.class);
                startActivityForResult(heartbeatPublication, Utils.HEARTBEAT_SETTINGS_SET);
            });

            mRefreshSubscription = nodeControlsContainerBinding.actionRefreshHeartbeatSubscription;
            mRefreshSubscription.setOnClickListener(v -> sendMessage(new ConfigHeartbeatSubscriptionGet()));

            mClearSubscription = nodeControlsContainerBinding.actionClearHeartbeatSubscription;
            mClearSubscription.setOnClickListener(v -> sendMessage(new ConfigHeartbeatPublicationSet()));

            mSetSubscription = nodeControlsContainerBinding.actionSetHeartbeatSubscription;
            mSetSubscription.setOnClickListener(v -> {
                final Intent subscription = new Intent(this, HeartbeatSubscriptionActivity.class);
                startActivityForResult(subscription, Utils.HEARTBEAT_SETTINGS_SET);
            });

            mNetworkTransmitCountText = nodeControlsContainerBinding.networkTransmitCount;
            mNetworkTransmitIntervalStepsText = nodeControlsContainerBinding.networkTransmitIntervalSteps;

            mSetNetworkTransmitStateButton = nodeControlsContainerBinding.actionNetworkTransmitConfigure;
            mSetNetworkTransmitStateButton.setOnClickListener(v -> {
                if (!checkConnectivity(mContainer)) return;
                if (meshNode != null && meshNode.getNetworkTransmitSettings() != null) {
                    mNetworkTransmitCount = meshNode.getNetworkTransmitSettings().getNetworkTransmitCount();
                    mNetworkTransmitIntervalSteps = meshNode.getNetworkTransmitSettings().getNetworkIntervalSteps();
                }

                final DialogFragmentNetworkTransmitSettings fragment = DialogFragmentNetworkTransmitSettings.newInstance(mNetworkTransmitCount, mNetworkTransmitIntervalSteps);
                fragment.show(getSupportFragmentManager(), null);
            });

            switchSnb = nodeControlsContainerBinding.switchSnb;
            switchSnb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(buttonView.isPressed()){
                    if (!checkConnectivity(mContainer)) {
                        switchSnb.toggle();
                        return;
                    }
                    sendMessage(new ConfigBeaconSet(switchSnb.isChecked()));
                }
            });

            switchFriend = nodeControlsContainerBinding.switchFriend;
            switchFriend.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) {
                    if (!checkConnectivity(mContainer)) {
                        buttonView.toggle();
                        return;
                    }
                    sendMessage(new ConfigFriendSet(isChecked));
                }
            });

            switchNodeIdentity = nodeControlsContainerBinding.switchNodeIdentity;
            switchNodeIdentity.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) {
                    if (!checkConnectivity(mContainer)) {
                        buttonView.toggle();
                        return;
                    }
                    final NetworkKey networkKey = mViewModel.getNetworkLiveData().
                            getMeshNetwork().getNetKey(meshNode.getAddedNetKeys().get(0).getIndex());
                    sendMessage(new ConfigNodeIdentitySet(networkKey, switchNodeIdentity.isChecked() ? ENABLED : DISABLED));
                }
            });

            mViewModel.getSelectedMeshNode().observe(this, node -> {
                updateSecureNetworkBeaconStateUi(node);
                updateFriendStateUi(node);
                updateNodeIdentityStateUi(node);
                updateNetworkTransmitUi(node);
                updateRelayUi(node);
                updateHeartbeatPublication();
                updateHeartbeatSubscription();
            });

            if (savedInstanceState == null) {
                updateNetworkTransmitUi(mViewModel.getSelectedMeshNode().getValue());
                updateRelayUi(mViewModel.getSelectedMeshNode().getValue());
                updateHeartbeatPublication();
                updateHeartbeatSubscription();
            }
        }
    }

    @Override
    public void onRefresh() {
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (!checkConnectivity(mContainer) || model == null) {
            mSwipe.setRefreshing(false);
        }
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        final Element element = mViewModel.getSelectedElement().getValue();
        if (node != null && element != null &&
                model instanceof ConfigurationServerModel) {
            mViewModel.displaySnackBar(this, mContainer,
                    getString(R.string.listing_model_configuration), Snackbar.LENGTH_LONG);
            ((ModelConfigurationViewModel) mViewModel).prepareMessageQueue();
            sendMessage(node.getUnicastAddress(), mViewModel.getMessageQueue().peek());
        } else {
            mSwipe.setRefreshing(false);
        }
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        if (meshMessage instanceof ConfigNetworkTransmitStatus) {
            final ConfigNetworkTransmitStatus status = (ConfigNetworkTransmitStatus) meshMessage;
            final ProvisionedMeshNode meshNode = mViewModel.getNetworkLiveData()
                    .getMeshNetwork().getNode(status.getSrc());
            mViewModel.removeMessage();
            handleStatuses();
            updateNetworkTransmitUi(meshNode);
        } else if (meshMessage instanceof ConfigRelayStatus) {
            final ConfigRelayStatus status = (ConfigRelayStatus) meshMessage;
            final ProvisionedMeshNode meshNode = mViewModel.getNetworkLiveData()
                    .getMeshNetwork().getNode(status.getSrc());
            mViewModel.removeMessage();
            handleStatuses();
            updateRelayUi(meshNode);
        } else if (meshMessage instanceof ConfigBeaconStatus) {
            final ConfigBeaconStatus status = (ConfigBeaconStatus) meshMessage;
            final ProvisionedMeshNode meshNode = mViewModel.getNetworkLiveData()
                    .getMeshNetwork().getNode(status.getSrc());
            mViewModel.removeMessage();
            handleStatuses();
            updateSecureNetworkBeaconStateUi(meshNode);
        } else if (meshMessage instanceof ConfigFriendStatus) {
            final ConfigFriendStatus status = (ConfigFriendStatus) meshMessage;
            final ProvisionedMeshNode meshNode = mViewModel.getNetworkLiveData()
                    .getMeshNetwork().getNode(status.getSrc());
            mViewModel.removeMessage();
            handleStatuses();
            updateFriendStateUi(meshNode);
        } else if (meshMessage instanceof ConfigNodeIdentityStatus) {
            final ConfigNodeIdentityStatus status = (ConfigNodeIdentityStatus) meshMessage;
            final ProvisionedMeshNode meshNode = mViewModel.getNetworkLiveData()
                    .getMeshNetwork().getNode(status.getSrc());
            mViewModel.removeMessage();
            if (!status.isSuccessful())
                displayStatusDialogFragment(getString(R.string.node_identity_state), status.getStatusCodeName());
            handleStatuses();
            updateNodeIdentityStateUi(meshNode);
        } else if (meshMessage instanceof ConfigHeartbeatPublicationStatus) {
            final ConfigHeartbeatPublicationStatus status = (ConfigHeartbeatPublicationStatus) meshMessage;
            mViewModel.removeMessage();
            if (status.isSuccessful()) {
                updateHeartbeatPublication();
                if (handleStatuses()) return;
            } else {
                displayStatusDialogFragment(
                        getString(R.string.title_heartbeat_publication_status), status.getStatusCodeName());
            }
        } else if (meshMessage instanceof ConfigHeartbeatSubscriptionStatus) {
            final ConfigHeartbeatSubscriptionStatus status = (ConfigHeartbeatSubscriptionStatus) meshMessage;
            mViewModel.removeMessage();
            if (status.isSuccessful()) {
                updateHeartbeatSubscription();
                if (handleStatuses()) return;
            } else {
                displayStatusDialogFragment(
                        getString(R.string.title_heartbeat_subscription_status), status.getStatusCodeName());
            }
        }
        hideProgressBar();
    }

    @Override
    protected void enableClickableViews() {
        super.enableClickableViews();
        mRefreshPublication.setEnabled(true);
        mClearPublication.setEnabled(true);
        mSetPublication.setEnabled(true);
        mSetSubscription.setEnabled(true);
        mRefreshSubscription.setEnabled(true);
        mClearSubscription.setEnabled(true);
        mActionSetRelayState.setEnabled(true);
        mSetNetworkTransmitStateButton.setEnabled(true);
        switchSnb.setEnabled(true);
        switchFriend.setEnabled(true);
        switchNodeIdentity.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        mRefreshPublication.setEnabled(false);
        mClearPublication.setEnabled(false);
        mSetPublication.setEnabled(false);
        mRefreshSubscription.setEnabled(false);
        mClearSubscription.setEnabled(false);
        mSetSubscription.setEnabled(false);
        mActionSetRelayState.setEnabled(false);
        mSetNetworkTransmitStateButton.setEnabled(false);
        switchSnb.setEnabled(false);
        switchFriend.setEnabled(false);
        switchNodeIdentity.setEnabled(false);
    }

    @Override
    public void onNetworkTransmitSettingsEntered(int transmitCount, int transmitIntervalSteps) {
        setNetworkTransmit(transmitCount, transmitIntervalSteps);
    }

    @Override
    public void onRelayRetransmitSet(final int relay, final int retransmitCount, final int retransmitIntervalSteps) {
        setRelayRetransmit(relay, retransmitCount, retransmitIntervalSteps);
    }

    private void setRelayRetransmit(final int relay, final int relayRetransmit, final int relayRetransmitIntervalSteps) {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        try {
            if (node != null) {
                final ConfigRelaySet message = new ConfigRelaySet(relay, relayRetransmit, relayRetransmitIntervalSteps);
                sendMessage(node.getUnicastAddress(), message);
            }
        } catch (Exception e) {
            hideProgressBar();
            Log.e(TAG, "Exception while ConfigNetworkTransmitSet: " + e.getMessage());
        }
    }

    private void setNetworkTransmit(final int networkTransmitCount, final int networkTransmitIntervalSteps) {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        try {
            if (node != null) {
                final ConfigNetworkTransmitSet message = new ConfigNetworkTransmitSet(networkTransmitCount, networkTransmitIntervalSteps);
                sendMessage(node.getUnicastAddress(), message);
            }
        } catch (Exception e) {
            hideProgressBar();
            Log.e(TAG, "Error ConfigNetworkTransmitSet: " + e.getMessage());
        }
    }

    private void updateSecureNetworkBeaconStateUi(@NonNull final ProvisionedMeshNode meshNode) {
        if (meshNode.isSecureNetworkBeaconSupported() != null)
            switchSnb.setChecked(meshNode.isSecureNetworkBeaconSupported());
    }

    private void updateFriendStateUi(@NonNull final ProvisionedMeshNode meshNode) {
        switchFriend.setEnabled(meshNode.getNodeFeatures().isFriendFeatureSupported());
        switchFriend.setChecked(meshNode.getNodeFeatures().getFriend() == Features.ENABLED);
    }

    private void updateNodeIdentityStateUi(@NonNull final ProvisionedMeshNode meshNode) {
        switchNodeIdentity.setEnabled(meshNode.getNodeIdentityState() != UNSUPPORTED);
        switchNodeIdentity.setChecked(meshNode.getNodeIdentityState() == ENABLED);
    }

    private void updateNetworkTransmitUi(@NonNull final ProvisionedMeshNode meshNode) {
        final NetworkTransmitSettings networkTransmitSettings = meshNode.getNetworkTransmitSettings();
        if (networkTransmitSettings != null) {
            mSetNetworkTransmitStateButton.setEnabled(true);
            mNetworkTransmitCountText.setText(getResources().getQuantityString(R.plurals.transmit_count,
                    networkTransmitSettings.getTransmissionCount(),
                    networkTransmitSettings.getTransmissionCount()));
            mNetworkTransmitIntervalStepsText.setText(getString(R.string.time_ms,
                    networkTransmitSettings.getNetworkTransmissionInterval()));
        } else {
            mSetNetworkTransmitStateButton.setEnabled(false);
            mNetworkTransmitCountText.setText(getResources().getString(R.string.unknown));
            mNetworkTransmitIntervalStepsText.setText(getResources().getString(R.string.unknown));
        }
    }

    private void updateRelayUi(@NonNull final ProvisionedMeshNode meshNode) {
        final RelaySettings relaySettings = meshNode.getRelaySettings();
        if (relaySettings != null) {
            mActionSetRelayState.setEnabled(true);
            mRelayRetransmitCountText.setText(getResources().getQuantityString(R.plurals.summary_network_transmit_count,
                    relaySettings.getRelayTransmitCount(),
                    relaySettings.getRelayTransmitCount(),
                    relaySettings.getTotalTransmissionsCount()));
            mRelayRetransmitIntervalStepsText.setText(getString(R.string.time_ms,
                    relaySettings.getRetransmissionIntervals()));
        } else {
            mActionSetRelayState.setEnabled(false);
            mRelayRetransmitCountText.setText(getResources().getString(R.string.unknown));
            mRelayRetransmitIntervalStepsText.setText(getResources().getString(R.string.unknown));
        }
    }

    private void updateHeartbeatPublication() {
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null) {
            final HeartbeatPublication publication = ((ConfigurationServerModel) model).getHeartbeatPublication();
            if (publication != null && publication.isEnabled()) {
                mContainerHeartbeatPublication.setVisibility(VISIBLE);
                mHeartbeatPublicationDisabled.setVisibility(GONE);
                mClearPublication.setVisibility(VISIBLE);
                mRefreshPublication.setVisibility(VISIBLE);
                mHeartbeatPublicationDst.setText(formatAddress(publication.getDst(), true));
                mHeartbeatPublicationCount.setText(publication.getCountLogDescription());
                mHeartbeatPublicationPeriod.setText((publication.getPeriodLogDescription()));

                final NetworkKey key = mViewModel.getNetworkLiveData().getMeshNetwork().getNetKey(publication.getNetKeyIndex());
                mHeartbeatPublicationFeatures.setText(parseFeatures(publication.getFeatures()));
                mHeartbeatPublicationKey.setText(getString(R.string.key_name_and_index, key.getName(), key.getKeyIndex()));
            } else {
                mContainerHeartbeatPublication.setVisibility(GONE);
                mHeartbeatPublicationDisabled.setVisibility(VISIBLE);
                mClearPublication.setVisibility(GONE);
                mRefreshPublication.setVisibility(GONE);
            }
        }
    }

    private void updateHeartbeatSubscription() {
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null) {
            final HeartbeatSubscription subscription = ((ConfigurationServerModel) model).getHeartbeatSubscription();
            if (subscription != null && subscription.isEnabled()) {
                mContainerHeartbeatSubscription.setVisibility(VISIBLE);
                mHeartbeatSubscriptionDisabled.setVisibility(GONE);
                mClearSubscription.setVisibility(VISIBLE);
                mRefreshSubscription.setVisibility(VISIBLE);
                mHeartbeatSubscriptionSrc.setText(formatAddress(subscription.getSrc(), true));
                mHeartbeatSubscriptionDst.setText(formatAddress(subscription.getDst(), true));
                mHeartbeatSubscriptionPeriod.setText(subscription.getPeriodLogDescription());
                mHeartbeatSubscriptionCount.setText(subscription.getCountLogDescription());
                mHeartbeatSubscriptionMinHops.setText(String.valueOf(subscription.getMinHops()));
                mHeartbeatSubscriptionMaxHops.setText(String.valueOf(subscription.getMaxHops()));
            } else {
                mContainerHeartbeatSubscription.setVisibility(GONE);
                mHeartbeatSubscriptionDisabled.setVisibility(VISIBLE);
                mClearSubscription.setVisibility(GONE);
                mRefreshSubscription.setVisibility(GONE);
            }
        }
    }

    /**
     * Returns a String representation of the features
     */
    private String parseFeatures(final Features features) {
        String result = "";
        if (isEnabled(features.getRelay()))
            result += "Relay";
        if (isEnabled(features.getProxy())) {
            result = append(result) + "Proxy";
        }
        if (isEnabled(features.getFriend())) {
            result = append(result) + "Friend";
        }
        if (isEnabled(features.getLowPower())) {
            result = append(result) + "Low Power";
        }
        return result;
    }

    private boolean isEnabled(final int feature) {
        return feature == Features.ENABLED;
    }

    private String append(final String result) {
        return !result.isEmpty() ? result + ", " : result;
    }
}
