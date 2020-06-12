package no.nordicsemi.android.nrfmesh.node;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.transport.PublicationSettings;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.GroupCallbacks;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.di.Injectable;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;
import no.nordicsemi.android.nrfmesh.node.dialog.DestinationAddressCallbacks;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentPublishAddress;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentPublishTtl;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentTtl;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.viewmodels.PublicationViewModel;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.RESOLUTION_100_MS;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.USE_DEFAULT_TTL;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.isDefaultPublishTtl;
import static no.nordicsemi.android.nrfmesh.utils.Utils.RESULT_KEY;

public class PublicationSettingsActivity extends AppCompatActivity implements Injectable,
        GroupCallbacks, DestinationAddressCallbacks,
        DialogFragmentTtl.DialogFragmentTtlListener {

    public static final int SET_PUBLICATION_SETTINGS = 2021;
    private static final int MIN_PUBLICATION_INTERVAL = 0;
    private static final int MAX_PUBLICATION_INTERVAL = 234;
    @SuppressWarnings("unused")
    private static final int DEFAULT_PUBLICATION_RESOLUTION = RESOLUTION_100_MS;

    private PublicationViewModel mViewModel;
    private MeshModel mMeshModel;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.container)
    CoordinatorLayout mContainer;
    @BindView(R.id.publish_address)
    TextView mPublishAddressView;
    @BindView(R.id.retransmit_count)
    TextView mRetransmitCountView;
    @BindView(R.id.retransmit_interval)
    TextView mRetransmitInterval;
    @BindView(R.id.app_key)
    TextView mAppKeyView;
    @BindView(R.id.publication_ttl)
    TextView mPublishTtlView;
    @BindView(R.id.friendship_credential_flag)
    Switch mActionFriendshipCredentialSwitch;
    @BindView(R.id.retransmission_slider)
    Slider mRetransmissionCountSlider;
    @BindView(R.id.interval_steps_slider)
    Slider mRetransmitIntervalSlider;
    @BindView(R.id.publish_interval_slider)
    Slider mPublicationIntervalSlider;
    @BindView(R.id.pub_interval)
    TextView mPublicationInterval;
    @BindView(R.id.fab_apply)
    ExtendedFloatingActionButton fabApply;

    private boolean mIsConnected;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publication_settings);
        ButterKnife.bind(this);

        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(PublicationViewModel.class);

        final MeshModel meshModel = mMeshModel = mViewModel.getSelectedModel().getValue();
        if (meshModel == null)
            finish();

        //Setup views
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setTitle(R.string.title_publication_settings);

        final ScrollView scrollView = findViewById(R.id.scroll_view);
        final View actionPublishAddress = findViewById(R.id.container_publish_address);
        final View actionKeyIndex = findViewById(R.id.container_app_key_index);
        final View actionPublishTtl = findViewById(R.id.container_publication_ttl);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (scrollView.getScrollY() == 0) {
                fabApply.extend();
            } else {
                fabApply.shrink();
            }
        });

        actionPublishAddress.setOnClickListener(v -> {
            List<Group> groups = mViewModel.getNetworkLiveData().getMeshNetwork().getGroups();
            final DialogFragmentPublishAddress fragmentPublishAddress = DialogFragmentPublishAddress.
                    newInstance(meshModel.getPublicationSettings(), new ArrayList<>(groups));
            fragmentPublishAddress.show(getSupportFragmentManager(), null);
        });

        actionKeyIndex.setOnClickListener(v -> {
            final Intent bindAppKeysIntent = new Intent(this, AppKeysActivity.class);
            bindAppKeysIntent.putExtra(Utils.EXTRA_DATA, Utils.PUBLICATION_APP_KEY);
            startActivityForResult(bindAppKeysIntent, Utils.SELECT_KEY);
        });

        mActionFriendshipCredentialSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                mViewModel.setFriendshipCredentialsFlag(isChecked));

        actionPublishTtl.setOnClickListener(v -> {
            if (meshModel != null) {
                if (meshModel.getPublicationSettings() != null) {
                    final DialogFragmentTtl fragmentPublishTtl = DialogFragmentPublishTtl
                            .newInstance(meshModel.getPublicationSettings().getPublishTtl());
                    fragmentPublishTtl.show(getSupportFragmentManager(), null);
                } else {
                    final DialogFragmentTtl fragmentPublishTtl = DialogFragmentPublishTtl
                            .newInstance(USE_DEFAULT_TTL);
                    fragmentPublishTtl.show(getSupportFragmentManager(), null);
                }
            }
        });

        mPublicationIntervalSlider.setValueFrom(MIN_PUBLICATION_INTERVAL);
        mPublicationIntervalSlider.setValueTo(MAX_PUBLICATION_INTERVAL);
        mPublicationIntervalSlider.addOnChangeListener((slider, value, fromUser) -> {
            final int resource = mViewModel
                    .getPublicationPeriodResolutionResource((int) value);
            if (value == 0) {
                mPublicationInterval.setText(resource);
            } else {
                mPublicationInterval.setText(getString(resource, mViewModel.getPublishPeriod()));
            }
        });

        mRetransmitIntervalSlider.setValueFrom(PublicationSettings.MIN_PUBLICATION_RETRANSMIT_COUNT);
        mRetransmissionCountSlider.setValueTo(PublicationSettings.MAX_PUBLICATION_RETRANSMIT_COUNT);
        mRetransmissionCountSlider.setStepSize(1);
        mRetransmissionCountSlider.addOnChangeListener((slider, progress, fromUser) -> {
            mViewModel.setRetransmitCount((int) progress);
            if (progress == 0) {
                mRetransmitCountView.setText(R.string.disabled);
                mRetransmitInterval.setText(R.string.disabled);
                mRetransmitIntervalSlider.setEnabled(false);
            } else {
                if (!mRetransmitIntervalSlider.isEnabled())
                    mRetransmitIntervalSlider.setEnabled(true);
                mRetransmitInterval.setText(getString(R.string.time_ms, mViewModel.getRetransmissionInterval()));
                mRetransmitCountView.setText(getResources().getQuantityString(R.plurals.retransmit_count,
                        (int) progress, (int) progress));
            }
        });

        mRetransmitIntervalSlider.setValueFrom(PublicationSettings.getMinRetransmissionInterval());
        mRetransmitIntervalSlider.setValueTo(PublicationSettings.getMaxRetransmissionInterval());
        mRetransmitIntervalSlider.setStepSize(50);
        mRetransmitIntervalSlider.addOnChangeListener((slider, value, fromUser) -> {
            mRetransmitInterval.setText(getString(R.string.time_ms, (int) value));
            mViewModel.setRetransmitIntervalSteps((int) value);
        });

        fabApply.setOnClickListener(v -> {
            if (!checkConnectivity()) return;
            setPublication();
        });

        mViewModel.isConnectedToProxy().observe(this, isConnected -> {
            if (isConnected != null) {
                mIsConnected = isConnected;
            }
            invalidateOptionsMenu();
        });

        if (savedInstanceState == null) {
            //noinspection ConstantConditions
            mViewModel.setPublicationValues(meshModel.getPublicationSettings(), meshModel.getBoundAppKeyIndexes());
        }
        updatePublicationValues();
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
                mViewModel.navigateToScannerActivity(this, false, Utils.CONNECT_TO_NETWORK, false);
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
        if (requestCode == Utils.SELECT_KEY) {
            if (resultCode == RESULT_OK) {
                final ApplicationKey appKey = data.getParcelableExtra(RESULT_KEY);
                if (appKey != null) {
                    mViewModel.setAppKeyIndex(appKey.getKeyIndex());
                    mAppKeyView.setText(getString(R.string.app_key_index, appKey.getKeyIndex()));
                }
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
    public void onDestinationAddressSet(final int address) {
        mViewModel.setLabelUUID(null);
        mViewModel.setPublishAddress(address);
        mPublishAddressView.setText(MeshAddress.formatAddress(address, true));
    }

    @Override
    public void onDestinationAddressSet(@NonNull final Group group) {
        mViewModel.setLabelUUID(group.getAddressLabel());
        mViewModel.setPublishAddress(group.getAddress());
        mPublishAddressView.setText(MeshAddress.formatAddress(group.getAddress(), true));
    }

    @Override
    public void setPublishTtl(final int ttl) {
        mViewModel.setPublishTtl(ttl);
        updateTtlUi();
    }

    private void updatePublicationValues() {
        updateUi();
        updateTtlUi();
    }

    private void updateUi() {
        final UUID labelUUID = mViewModel.getLabelUUID();
        if (labelUUID == null) {
            mPublishAddressView.setText(MeshAddress.formatAddress(mViewModel.getPublishAddress(), true));
        } else {
            mPublishAddressView.setText(labelUUID.toString().toUpperCase(Locale.US));
        }

        if (MeshAddress.isValidUnassignedAddress(mViewModel.getPublishAddress())) {
            mPublishAddressView.setText(R.string.not_assigned);
        } else {
            mPublishAddressView.setText(MeshAddress.formatAddress(mViewModel.getPublishAddress(), true));
        }
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        mAppKeyView.setText(getString(R.string.app_key_index, mViewModel.getAppKeyIndex()));
        if (network != null) {
            final ApplicationKey key = network.getAppKey(mViewModel.getAppKeyIndex());
            if (key != null) {
                mAppKeyView.setText(getString(R.string.key_name_and_index, key.getName(), mViewModel.getAppKeyIndex()));
            } else {
                mAppKeyView.setText(getString(R.string.unavailable));
            }
        } else {
            mAppKeyView.setText(getString(R.string.unavailable));
        }

        mActionFriendshipCredentialSwitch.setChecked(mViewModel.getFriendshipCredentialsFlag());
        updatePublishPeriodUi();

        mRetransmissionCountSlider.setValue(mViewModel.getRetransmitCount());
        mRetransmitIntervalSlider.setValue(mViewModel.getRetransmissionInterval());

    }

    private void updateTtlUi() {
        final int ttl = mViewModel.getPublishTtl();
        if (isDefaultPublishTtl(ttl)) {
            mPublishTtlView.setText(getString(R.string.uses_default_ttl));
        } else {
            mPublishTtlView.setText(String.valueOf(ttl));
        }
    }

    private void setPublication() {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        if (node != null) {
            final MeshMessage configModelPublicationSet = mViewModel.createMessage();
            if (configModelPublicationSet != null) {
                try {
                    mViewModel
                            .getMeshManagerApi()
                            .createMeshPdu(node.getUnicastAddress(),
                                    configModelPublicationSet);
                } catch (IllegalArgumentException ex) {
                    final DialogFragmentError message = DialogFragmentError.
                            newInstance(getString(R.string.title_error), ex.getMessage());
                    message.show(getSupportFragmentManager(), null);
                    return;
                }
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

    private void updatePublishPeriodUi() {
        final int sliderValue;
        final int stringResource;
        switch (mViewModel.getPublicationResolution()) {
            default:
            case 0:
                sliderValue = mViewModel.getPublicationSteps();
                stringResource = R.string.time_ms;
                break;
            case 1:
                sliderValue = 57 + mViewModel.getPublicationSteps();
                stringResource = R.string.time_s;
                break;
            case 2:
                sliderValue = 114 + mViewModel.getPublicationSteps();
                stringResource = R.string.time_s;
                break;
            case 3:
                sliderValue = 171 + mViewModel.getPublicationSteps();
                stringResource = R.string.time_m;
                break;
        }
        mPublicationIntervalSlider.setValue(sliderValue);
        final int period = mViewModel.getPublishPeriod();
        if (sliderValue == 0) {
            mPublicationInterval.setText(R.string.disabled);
        } else {
            mPublicationInterval.setText(getString(stringResource, period));
        }
    }
}
