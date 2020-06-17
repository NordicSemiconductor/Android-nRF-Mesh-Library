package no.nordicsemi.android.nrfmesh.node;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import no.nordicsemi.android.mesh.models.ConfigurationServerModel;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatPublicationGet;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatPublicationSet;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatPublicationStatus;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatSubscriptionGet;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatSubscriptionSet;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatSubscriptionStatus;
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitGet;
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitSet;
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitStatus;
import no.nordicsemi.android.mesh.transport.ConfigRelayGet;
import no.nordicsemi.android.mesh.transport.ConfigRelaySet;
import no.nordicsemi.android.mesh.transport.ConfigRelayStatus;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.HeartbeatPublication;
import no.nordicsemi.android.mesh.utils.HeartbeatSubscription;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.NetworkTransmitSettings;
import no.nordicsemi.android.mesh.utils.RelaySettings;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentNetworkTransmitSettings;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogRelayRetransmitSettings;
import no.nordicsemi.android.nrfmesh.utils.Utils;

import static no.nordicsemi.android.mesh.utils.Heartbeat.DEFAULT_PUBLICATION_TTL;
import static no.nordicsemi.android.mesh.utils.Heartbeat.DO_NOT_SEND_PERIODICALLY;

public class ConfigurationServerActivity extends BaseModelConfigurationActivity implements
        DialogFragmentNetworkTransmitSettings.DialogFragmentNetworkTransmitSettingsListener,
        DialogRelayRetransmitSettings.DialogFragmentRelaySettingsListener {

    private static final String TAG = ConfigurationServerActivity.class.getSimpleName();

    private static final int NETWORK_TRANSMIT_SETTING_UNKNOWN = -1;
    private static final int RELAY_RETRANSMIT_SETTINGS_UNKNOWN = -1;

    private TextView mHeartbeatPublicationText;
    private Button mClearPublication;
    private Button mSetPublication;
    private TextView mHeartbeatSubscriptionText;
    private Button mClearSubscription;
    private Button mSetSubscription;
    private TextView mRelayRetransmitCountText;
    private TextView mRelayRetransmitIntervalStepsText;
    private TextView mNetworkTransmitCountText;
    private TextView mNetworkTransmitIntervalStepsText;

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
            mContainerAppKeyBinding.setVisibility(View.GONE);
            mContainerPublication.setVisibility(View.GONE);
            mContainerSubscribe.setVisibility(View.GONE);
            final ConstraintLayout view = findViewById(R.id.node_controls_container);
            final View nodeControlsContainer = LayoutInflater.from(this)
                    .inflate(R.layout.layout_config_server_model, view);

            mHeartbeatPublicationText = nodeControlsContainer.findViewById(R.id.heartbeat_publication);
            mHeartbeatSubscriptionText = nodeControlsContainer.findViewById(R.id.heartbeat_subscription);

            final ProvisionedMeshNode meshNode = mViewModel.getSelectedMeshNode().getValue();
            if (meshNode != null) {
                mRelayRetransmitIntervalStepsText = nodeControlsContainer.findViewById(R.id.relay_retransmit_interval_steps);
                mActionSetRelayState = nodeControlsContainer.findViewById(R.id.action_relay_retransmit_configure);
                mRelayRetransmitCountText = nodeControlsContainer.findViewById(R.id.relay_retransmit_count);
                if (meshNode.getNodeFeatures().isRelayFeatureSupported()) {
                    final CardView relayCardView = findViewById(R.id.config_relay_set_card);
                    relayCardView.setVisibility(View.VISIBLE);

                    mActionSetRelayState.setOnClickListener(v -> {
                        if (!checkConnectivity()) return;
                        final RelaySettings relaySettings = meshNode.getRelaySettings();
                        if (relaySettings != null) {
                            mRelayRetransmitCount = meshNode.getRelaySettings().getRelayTransmitCount();
                            mRelayRetransmitIntervalSteps = meshNode.getRelaySettings().getRelayIntervalSteps();
                        }
                        //Assuming relay is enabled by default
                        final DialogRelayRetransmitSettings fragment = DialogRelayRetransmitSettings.newInstance(1, mRelayRetransmitCount, mRelayRetransmitIntervalSteps);
                        fragment.show(getSupportFragmentManager(), null);
                    });
                }
            }
            mClearPublication = nodeControlsContainer.findViewById(R.id.action_clear_heartbeat_publication);
            mClearPublication.setOnClickListener(v -> clearPublication());

            mSetPublication = nodeControlsContainer.findViewById(R.id.action_set_heartbeat_publication);
            mSetPublication.setOnClickListener(v -> {
                final Intent heartbeatPublication = new Intent(this, HeartbeatPublicationActivity.class);
                startActivityForResult(heartbeatPublication, Utils.HEARTBEAT_SETTINGS_SET);
            });

            mClearSubscription = nodeControlsContainer.findViewById(R.id.action_clear_heartbeat_subscription);
            mClearSubscription.setOnClickListener(v -> clearSubscription());

            mSetSubscription = nodeControlsContainer.findViewById(R.id.action_set_heartbeat_subscription);
            mSetSubscription.setOnClickListener(v -> {
                final Intent subscription = new Intent(this, HeartbeatSubscriptionActivity.class);
                startActivityForResult(subscription, Utils.HEARTBEAT_SETTINGS_SET);
            });

            mNetworkTransmitCountText = nodeControlsContainer.findViewById(R.id.network_transmit_count);
            mNetworkTransmitIntervalStepsText = nodeControlsContainer.findViewById(R.id.network_transmit_interval_steps);

            mSetNetworkTransmitStateButton = nodeControlsContainer.findViewById(R.id.action_network_transmit_configure);
            mSetNetworkTransmitStateButton.setOnClickListener(v -> {
                if (!checkConnectivity()) return;
                if (meshNode != null && meshNode.getNetworkTransmitSettings() != null) {
                    mNetworkTransmitCount = meshNode.getNetworkTransmitSettings().getNetworkTransmitCount();
                    mNetworkTransmitIntervalSteps = meshNode.getNetworkTransmitSettings().getNetworkIntervalSteps();
                }

                final DialogFragmentNetworkTransmitSettings fragment = DialogFragmentNetworkTransmitSettings.newInstance(mNetworkTransmitCount, mNetworkTransmitIntervalSteps);
                fragment.show(getSupportFragmentManager(), null);
            });

            mViewModel.getSelectedMeshNode().observe(this, node -> {
                if (node != null) {
                    updateNetworkTransmitUi(node);
                    updateRelayUi(node);
                    updateHeartbeatPublication();
                    updateHeartbeatSubscription();
                }
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
        if (!checkConnectivity() || model == null) {
            mSwipe.setRefreshing(false);
        }
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        final Element element = mViewModel.getSelectedElement().getValue();
        if (node != null && element != null &&
                model instanceof ConfigurationServerModel) {
            mViewModel.displaySnackBar(this, mContainer,
                    getString(R.string.listing_model_configuration), Snackbar.LENGTH_LONG);
            mViewModel.getMessageQueue().add(new ConfigHeartbeatSubscriptionGet());
            mViewModel.getMessageQueue().add(new ConfigHeartbeatPublicationGet());
            mViewModel.getMessageQueue().add(new ConfigHeartbeatSubscriptionGet());
            mViewModel.getMessageQueue().add(new ConfigRelayGet());
            mViewModel.getMessageQueue().add(new ConfigNetworkTransmitGet());
            //noinspection ConstantConditions
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
        mClearPublication.setEnabled(true);
        mSetPublication.setEnabled(true);
        mSetSubscription.setEnabled(true);
        mClearSubscription.setEnabled(true);
        mActionSetRelayState.setEnabled(true);
        mSetNetworkTransmitStateButton.setEnabled(true);
    }

    @Override
    protected void disableClickableViews() {
        super.disableClickableViews();
        mClearPublication.setEnabled(false);
        mSetPublication.setEnabled(false);
        mClearSubscription.setEnabled(false);
        mSetSubscription.setEnabled(false);
        mActionSetRelayState.setEnabled(false);
        mSetNetworkTransmitStateButton.setEnabled(false);
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
            final HeartbeatPublication heartbeatPublication = ((ConfigurationServerModel) model).getHeartbeatPublication();
            if (heartbeatPublication != null) {
                mHeartbeatPublicationText.setText(MeshAddress.formatAddress(heartbeatPublication.getDstAddress(), true));
                mClearPublication.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateHeartbeatSubscription() {
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null) {
            final HeartbeatSubscription subscription = ((ConfigurationServerModel) model).getHeartbeatSubscription();
            if (subscription != null) {
                mHeartbeatSubscriptionText.setText(MeshAddress.formatAddress(subscription.getDst(), true));
                mClearSubscription.setVisibility(View.VISIBLE);
            }
        }
    }

    private void clearPublication() {
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null) {
            final ConfigurationServerModel serverModel = (ConfigurationServerModel) model;
            final HeartbeatPublication publication = serverModel.getHeartbeatPublication();
            final ConfigHeartbeatPublicationSet message;
            if (publication != null) {
                message = new ConfigHeartbeatPublicationSet(MeshAddress.UNASSIGNED_ADDRESS,
                        publication.getCountLog(), publication.getPeriodLog(), publication.getTtl(),
                        publication.getFeatures(), publication.getNetKeyIndex());
                sendMessage(message);
            } else {
                final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
                if (node != null) {
                    message = new ConfigHeartbeatPublicationSet(MeshAddress.UNASSIGNED_ADDRESS,
                            DO_NOT_SEND_PERIODICALLY, DO_NOT_SEND_PERIODICALLY, DEFAULT_PUBLICATION_TTL,
                            node.getNodeFeatures(), node.getAddedNetKeys().get(0).getIndex());
                    sendMessage(message);
                }
            }
        }
    }

    private void clearSubscription() {
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model != null) {
            final ConfigurationServerModel serverModel = (ConfigurationServerModel) model;
            final HeartbeatSubscription subscription = serverModel.getHeartbeatSubscription();
            final ConfigHeartbeatSubscriptionSet message;
            if (subscription != null) {
                message = new ConfigHeartbeatSubscriptionSet(MeshAddress.UNASSIGNED_ADDRESS,
                        MeshAddress.UNASSIGNED_ADDRESS,
                        subscription.getPeriodLog());
            } else {
                message = new ConfigHeartbeatSubscriptionSet(MeshAddress.UNASSIGNED_ADDRESS,
                        MeshAddress.UNASSIGNED_ADDRESS,
                        DO_NOT_SEND_PERIODICALLY);
            }
            sendMessage(message);
        }
    }
}
