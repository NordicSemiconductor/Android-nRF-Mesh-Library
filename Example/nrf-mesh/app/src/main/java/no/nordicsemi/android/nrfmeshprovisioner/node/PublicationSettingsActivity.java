package no.nordicsemi.android.nrfmeshprovisioner.node;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

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
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelPublicationSet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelPublicationVirtualAddressSet;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.PublicationSettings;
import no.nordicsemi.android.meshprovisioner.utils.AddressType;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.GroupCallbacks;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmeshprovisioner.keys.AppKeysActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.dialog.DialogFragmentPublishAddress;
import no.nordicsemi.android.nrfmeshprovisioner.node.dialog.DialogFragmentPublishTtl;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.PublicationViewModel;

public class PublicationSettingsActivity extends AppCompatActivity implements Injectable,
        GroupCallbacks,
        DialogFragmentPublishAddress.DialogFragmentPublicationListener,
        DialogFragmentPublishTtl.DialogFragmentPublishTtlListener {

    public static final int SET_PUBLICATION_SETTINGS = 2021;
    public static final String RESULT_LABEL_UUID = "RESULT_LABEL_UUID";
    public static final String RESULT_PUBLISH_ADDRESS = "RESULT_PUBLISH_ADDRESS";
    public static final String RESULT_APP_KEY_INDEX = "RESULT_APP_KEY_INDEX";
    public static final String RESULT_CREDENTIAL_FLAG = "RESULT_CREDENTIAL_FLAG";
    public static final String RESULT_PUBLISH_TTL = "RESULT_PUBLISH_TTL";
    public static final String RESULT_PUBLICATION_STEPS = "RESULT_PUBLICATION_STEPS";
    public static final String RESULT_PUBLICATION_RESOLUTION = "RESULT_PUBLICATION_RESOLUTION";
    public static final String RESULT_PUBLISH_RETRANSMIT_COUNT = "RESULT_PUBLISH_RETRANSMIT_COUNT";
    public static final String RESULT_PUBLISH_RETRANSMIT_INTERVAL_STEPS = "RESULT_PUBLISH_RETRANSMIT_INTERVAL_STEPS";

    private static final int DEFAULT_PUB_RETRANSMIT_COUNT = 1;
    private static final int DEFAULT_PUB_RETRANSMIT_INTERVAL_STEPS = 1;
    private static final int DEFAULT_PUBLICATION_STEPS = 0;
    @SuppressWarnings("unused")
    private static final int DEFAULT_PUBLICATION_RESOLUTION = MeshParserUtils.RESOLUTION_100_MS;

    private PublicationViewModel mViewModel;
    private MeshModel mMeshModel;
    private UUID mLabelUUID;
    private int mPublishAddress;
    private Integer mAppKeyIndex;
    private int mPublishTtl = MeshParserUtils.USE_DEFAULT_TTL;
    private int mPublicationSteps = DEFAULT_PUBLICATION_STEPS;
    private int mPublicationResolution;
    private int mRetransmitCount = DEFAULT_PUB_RETRANSMIT_COUNT;
    private int mRetransmitIntervalSteps = DEFAULT_PUB_RETRANSMIT_INTERVAL_STEPS;

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
    @BindView(R.id.retransmission_seek_bar)
    SeekBar mRetransmissionCountSeekBar;
    @BindView(R.id.interval_steps_seek_bar)
    SeekBar mRetransmitIntervalSeekBar;
    @BindView(R.id.publish_interval_seek_bar)
    SeekBar mPublicationIntervalSeekBar;
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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
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

        actionPublishTtl.setOnClickListener(v -> {
            if (meshModel != null) {
                if (meshModel.getPublicationSettings() != null) {
                    final DialogFragmentPublishTtl fragmentPublishTtl = DialogFragmentPublishTtl
                            .newInstance(meshModel.getPublicationSettings().getPublishTtl());
                    fragmentPublishTtl.show(getSupportFragmentManager(), null);
                } else {
                    final DialogFragmentPublishTtl fragmentPublishTtl = DialogFragmentPublishTtl
                            .newInstance(MeshParserUtils.USE_DEFAULT_TTL);
                    fragmentPublishTtl.show(getSupportFragmentManager(), null);
                }
            }
        });

        mPublicationIntervalSeekBar.setMax(234);
        mPublicationIntervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int lastValue = 0;

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                if (progress == 0) {
                    lastValue = progress;
                    mPublicationSteps = progress;
                    mPublicationResolution = 0;
                    mPublicationInterval.setText(R.string.disabled);
                } else if (progress >= 1 && progress <= 63) {
                    lastValue = progress;
                    mPublicationResolution = 0;
                    mPublicationSteps = progress;
                    mPublicationInterval.setText(getString(R.string.time_ms, PublicationSettings.getPublishPeriod(mPublicationResolution, mPublicationSteps)));
                } else if (progress >= 64 && progress <= 120) {
                    if (progress > lastValue) {
                        mPublicationSteps = progress - 57;
                        lastValue = progress;
                    } else if (progress < lastValue) {
                        mPublicationSteps = -(57 - progress);
                    }
                    mPublicationResolution = 1;
                    mPublicationInterval.setText(getString(R.string.time_s, PublicationSettings.getPublishPeriod(mPublicationResolution, mPublicationSteps)));
                } else if (progress >= 121 && progress <= 177) {
                    if (progress > lastValue) {
                        mPublicationSteps = progress - 114;
                        lastValue = progress;
                    } else if (progress < lastValue) {
                        mPublicationSteps = -(114 - progress);
                    }
                    mPublicationResolution = 2;
                    mPublicationInterval.setText(getString(R.string.time_s, PublicationSettings.getPublishPeriod(mPublicationResolution, mPublicationSteps)));
                } else if (progress >= 178 && progress <= 234) {
                    if (progress >= lastValue) {
                        mPublicationSteps = progress - 171;
                        lastValue = progress;
                    } else {
                        mPublicationSteps = -(171 - progress);
                    }
                    mPublicationResolution = 3;
                    mPublicationInterval.setText(getString(R.string.time_m, PublicationSettings.getPublishPeriod(mPublicationResolution, mPublicationSteps)));
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {

            }
        });

        mRetransmissionCountSeekBar.setMax(PublicationSettings.MAX_PUBLICATION_RETRANSMIT_COUNT);
        mRetransmissionCountSeekBar.incrementProgressBy(1);
        mRetransmissionCountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                if (fromUser) {
                    mRetransmitCount = progress;
                }
                if (progress == 0) {
                    mRetransmitCountView.setText(R.string.disabled);
                    mRetransmitInterval.setText(R.string.disabled);
                    mRetransmitIntervalSeekBar.setEnabled(false);
                } else {
                    if (!mRetransmitIntervalSeekBar.isEnabled())
                        mRetransmitIntervalSeekBar.setEnabled(true);
                    mRetransmitInterval.setText(getString(R.string.time_ms, PublicationSettings.
                            parseRetransmitIntervalSteps(mRetransmitIntervalSeekBar.getProgress())));
                    mRetransmitCountView.setText(getResources().getQuantityString(R.plurals.retransmit_count,
                            progress, mRetransmitCount));
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });

        mRetransmitIntervalSeekBar.setMax(PublicationSettings.getMaxRetransmissionInterval());
        mRetransmitIntervalSeekBar.incrementProgressBy(1);
        mRetransmitIntervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                mRetransmitInterval.setText(getString(R.string.time_ms, progress));
                if (fromUser) {
                    mRetransmitIntervalSteps = PublicationSettings.parseRetransmitIntervalSteps(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
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
            updatePublicationValues(meshModel);
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.SELECT_KEY) {
            if (resultCode == RESULT_OK) {
                final ApplicationKey appKey = data.getParcelableExtra(AppKeysActivity.RESULT_APP_KEY);
                if (appKey != null) {
                    mAppKeyIndex = appKey.getKeyIndex();
                    mAppKeyView.setText(getString(R.string.app_key_index, appKey.getKeyIndex()));
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(RESULT_LABEL_UUID, mLabelUUID);
        outState.putInt(RESULT_PUBLISH_ADDRESS, mPublishAddress);
        outState.putInt(RESULT_APP_KEY_INDEX, mAppKeyIndex);
        outState.putBoolean(RESULT_CREDENTIAL_FLAG, mActionFriendshipCredentialSwitch.isChecked());
        outState.putInt(RESULT_PUBLISH_TTL, mPublishTtl);
        outState.putInt(RESULT_PUBLICATION_STEPS, mPublicationSteps);
        outState.putInt(RESULT_PUBLICATION_RESOLUTION, mPublicationResolution);
        outState.putInt(RESULT_PUBLISH_RETRANSMIT_COUNT, mRetransmitCount);
        outState.putInt(RESULT_PUBLISH_RETRANSMIT_INTERVAL_STEPS, mRetransmitIntervalSteps);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mLabelUUID = (UUID) savedInstanceState.getSerializable(RESULT_LABEL_UUID);
        mPublishAddress = savedInstanceState.getInt(RESULT_PUBLISH_ADDRESS);
        mAppKeyIndex = savedInstanceState.getInt(RESULT_APP_KEY_INDEX);
        mPublishTtl = savedInstanceState.getInt(RESULT_PUBLISH_TTL);
        mPublicationSteps = savedInstanceState.getInt(RESULT_PUBLICATION_STEPS);
        mPublicationResolution = savedInstanceState.getInt(RESULT_PUBLICATION_RESOLUTION);
        mRetransmitCount = savedInstanceState.getInt(RESULT_PUBLISH_RETRANSMIT_COUNT);
        mRetransmitIntervalSteps = savedInstanceState.getInt(RESULT_PUBLISH_RETRANSMIT_INTERVAL_STEPS);
        updateUi(savedInstanceState.getBoolean(RESULT_CREDENTIAL_FLAG));
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
                onPublishAddressSet(group);
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
            if (group != null) {
                if (network.addGroup(group)) {
                    onPublishAddressSet(group);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onPublishAddressSet(final int address) {
        mLabelUUID = null;
        mPublishAddress = address;
        mPublishAddressView.setText(MeshAddress.formatAddress(address, true));
    }

    @Override
    public void onPublishAddressSet(@NonNull final Group group) {
        mLabelUUID = group.getAddressLabel();
        mPublishAddress = group.getAddress();
        mPublishAddressView.setText(MeshAddress.formatAddress(group.getAddress(), true));
    }

    @Override
    public void setPublishTtl(final int ttl) {
        mPublishTtl = ttl;
        updateTtlUi(ttl);
    }

    private void updatePublicationValues(@NonNull final MeshModel model) {
        final PublicationSettings publicationSettings = model.getPublicationSettings();

        //Default app key index to the 0th key in the list of bound app keys
        if (!model.getBoundAppKeyIndexes().isEmpty()) {
            mAppKeyIndex = mMeshModel.getBoundAppKeyIndexes().get(0);
        }

        if (publicationSettings != null) {
            mPublishAddress = publicationSettings.getPublishAddress();
            mLabelUUID = publicationSettings.getLabelUUID();

            mActionFriendshipCredentialSwitch.setChecked(publicationSettings.getCredentialFlag());
            mPublishTtl = publicationSettings.getPublishTtl();

            mPublicationSteps = publicationSettings.getPublicationSteps();
            mPublicationResolution = publicationSettings.getPublicationResolution();

            mRetransmitCount = publicationSettings.getPublishRetransmitCount();
            mRetransmitIntervalSteps = publicationSettings.getPublishRetransmitIntervalSteps();

            if (!model.getBoundAppKeyIndexes().isEmpty()) {
                mAppKeyIndex = publicationSettings.getAppKeyIndex();
            }
            updateUi(publicationSettings.getCredentialFlag());
        }
        updateTtlUi(mPublishTtl);
    }

    private void updateUi(final boolean credentialFlag) {
        if (mLabelUUID == null) {
            mPublishAddressView.setText(MeshAddress.formatAddress(mPublishAddress, true));
        } else {
            mPublishAddressView.setText(mLabelUUID.toString().toUpperCase(Locale.US));
        }

        if (MeshAddress.isValidUnassignedAddress(mPublishAddress)) {
            mPublishAddressView.setText(R.string.not_assigned);
        } else {
            mPublishAddressView.setText(MeshAddress.formatAddress(mPublishAddress, true));
        }
        final MeshNetwork network = mViewModel.getNetworkLiveData().getMeshNetwork();
        mAppKeyView.setText(getString(R.string.app_key_index, mAppKeyIndex));
        if (network != null) {
            final ApplicationKey key = network.getAppKey(mAppKeyIndex);
            if (key != null) {
                mAppKeyView.setText(getString(R.string.app_key_name_and_index, key.getName(), mAppKeyIndex));
            } else {
                mAppKeyView.setText(getString(R.string.unavailable));
            }
        } else {
            mAppKeyView.setText(getString(R.string.unavailable));
        }

        mActionFriendshipCredentialSwitch.setChecked(credentialFlag);

        final int period = PublicationSettings.getPublishPeriod(mPublicationResolution, mPublicationSteps);
        mPublicationIntervalSeekBar.setProgress(mPublicationSteps);
        mPublicationInterval.setText(getString(R.string.time_ms, period));

        mRetransmissionCountSeekBar.setProgress(mRetransmitCount);
        final int retransmissionInterval = PublicationSettings.getRetransmissionInterval(mRetransmitIntervalSteps);
        mRetransmitIntervalSeekBar.setProgress(retransmissionInterval);

    }

    private void updateTtlUi(final int ttl) {
        if (MeshParserUtils.isDefaultPublishTtl(ttl)) {
            mPublishTtlView.setText(getString(R.string.uses_default_ttl));
        } else {
            mPublishTtlView.setText(String.valueOf(ttl));
        }
    }

    private void setPublication() {
        final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
        final Element element = mViewModel.getSelectedElement().getValue();
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        final MeshMessage configModelPublicationSet;
        if (node != null && element != null && model != null) {
            final AddressType type = MeshAddress.getAddressType(mPublishAddress);
            if (type != null && type != AddressType.VIRTUAL_ADDRESS) {
                configModelPublicationSet = new ConfigModelPublicationSet(element.getElementAddress(),
                        mPublishAddress, mAppKeyIndex, mActionFriendshipCredentialSwitch.isChecked(), mPublishTtl,
                        mPublicationSteps, mPublicationResolution, mRetransmitCount, mRetransmitIntervalSteps, model.getModelId());
            } else {
                configModelPublicationSet = new ConfigModelPublicationVirtualAddressSet(element.getElementAddress(),
                        mLabelUUID, mAppKeyIndex, mActionFriendshipCredentialSwitch.isChecked(), mPublishTtl,
                        mPublicationSteps, mPublicationResolution, mRetransmitCount, mRetransmitIntervalSteps, model.getModelId());
            }
            try {
                mViewModel.getMeshManagerApi().createMeshPdu(node.getUnicastAddress(), configModelPublicationSet);
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
}
