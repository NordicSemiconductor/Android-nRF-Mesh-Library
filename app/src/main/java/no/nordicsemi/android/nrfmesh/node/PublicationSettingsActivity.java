package no.nordicsemi.android.nrfmesh.node;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
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
import no.nordicsemi.android.nrfmesh.databinding.ActivityPublicationSettingsBinding;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.keys.AppKeysActivity;
import no.nordicsemi.android.nrfmesh.node.dialog.DestinationAddressCallbacks;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentPublishAddress;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentPublishTtl;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentTtl;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.viewmodels.BaseActivity;
import no.nordicsemi.android.nrfmesh.viewmodels.PublicationViewModel;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.USE_DEFAULT_TTL;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.isDefaultPublishTtl;
import static no.nordicsemi.android.nrfmesh.utils.Utils.RESULT_KEY;

@AndroidEntryPoint
public class PublicationSettingsActivity extends BaseActivity implements
        GroupCallbacks, DestinationAddressCallbacks,
        DialogFragmentTtl.DialogFragmentTtlListener {

    public static final int SET_PUBLICATION_SETTINGS = 2021;
    private static final int MIN_PUBLICATION_INTERVAL = 0;
    private static final int MAX_PUBLICATION_INTERVAL = 234;

    private ActivityPublicationSettingsBinding binding;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublicationSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        final PublicationViewModel viewModel = new ViewModelProvider(this).get(PublicationViewModel.class);
        mViewModel = viewModel;
        initialize();

        final MeshModel meshModel = viewModel.getSelectedModel().getValue();
        if (meshModel == null)
            finish();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setTitle(R.string.title_publication_settings);

        binding.scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (binding.scrollView.getScrollY() == 0) {
                binding.fabApply.extend();
            } else {
                binding.fabApply.shrink();
            }
        });

        binding.containerPublishAddress.setOnClickListener(v -> {
            List<Group> groups = viewModel.getNetworkLiveData().getMeshNetwork().getGroups();
            final DialogFragmentPublishAddress fragmentPublishAddress = DialogFragmentPublishAddress.
                    newInstance(meshModel.getPublicationSettings(), new ArrayList<>(groups));
            fragmentPublishAddress.show(getSupportFragmentManager(), null);
        });

        binding.containerAppKeyIndex.setOnClickListener(v -> {
            final Intent bindAppKeysIntent = new Intent(this, AppKeysActivity.class);
            bindAppKeysIntent.putExtra(Utils.EXTRA_DATA, Utils.PUBLICATION_APP_KEY);
            startActivityForResult(bindAppKeysIntent, Utils.SELECT_KEY);
        });

        binding.friendshipCredentialFlag.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setFriendshipCredentialsFlag(isChecked));

        binding.containerPublicationTtl.setOnClickListener(v -> {
            if (meshModel != null) {
                if (meshModel.getPublicationSettings() != null) {
                    DialogFragmentPublishTtl
                            .newInstance(meshModel.getPublicationSettings().getPublishTtl()).show(getSupportFragmentManager(), null);
                } else {
                    DialogFragmentPublishTtl
                            .newInstance(USE_DEFAULT_TTL).show(getSupportFragmentManager(), null);
                }
            }
        });

        binding.publishIntervalSlider.setValueFrom(MIN_PUBLICATION_INTERVAL);
        binding.publishIntervalSlider.setValueTo(MAX_PUBLICATION_INTERVAL);
        binding.publishIntervalSlider.addOnChangeListener((slider, value, fromUser) -> {
            final int resource = viewModel
                    .getPublicationPeriodResolutionResource((int) value);
            if (value == 0) {
                binding.pubInterval.setText(resource);
            } else {
                binding.pubInterval.setText(getString(resource, viewModel.getPublishPeriod()));
            }
        });

        binding.intervalStepsSlider.setValueFrom(PublicationSettings.MIN_PUBLICATION_RETRANSMIT_COUNT);
        binding.retransmissionSlider.setValueTo(PublicationSettings.MAX_PUBLICATION_RETRANSMIT_COUNT);
        binding.retransmissionSlider.setStepSize(1);
        binding.retransmissionSlider.addOnChangeListener((slider, progress, fromUser) -> {
            viewModel.setRetransmitCount((int) progress);
            if (progress == 0) {
                binding.retransmitCount.setText(R.string.disabled);
                binding.retransmitInterval.setText(R.string.disabled);
                binding.intervalStepsSlider.setEnabled(false);
            } else {
                if (!binding.intervalStepsSlider.isEnabled())
                    binding.intervalStepsSlider.setEnabled(true);
                binding.retransmitInterval.setText(getString(R.string.time_ms, viewModel.getRetransmissionInterval()));
                binding.retransmitCount.setText(getResources().getQuantityString(R.plurals.retransmit_count,
                        (int) progress, (int) progress));
            }
        });

        binding.intervalStepsSlider.setValueFrom(PublicationSettings.getMinRetransmissionInterval());
        binding.intervalStepsSlider.setValueTo(PublicationSettings.getMaxRetransmissionInterval());
        binding.intervalStepsSlider.setStepSize(50);
        binding.intervalStepsSlider.addOnChangeListener((slider, value, fromUser) -> {
            binding.retransmitInterval.setText(getString(R.string.time_ms, (int) value));
            viewModel.setRetransmitIntervalSteps((int) value);
        });

        binding.fabApply.setOnClickListener(v -> {
            if (!checkConnectivity(binding.container)) return;
            setPublication();
        });

        if (savedInstanceState == null) {
            viewModel.setPublicationValues(meshModel.getPublicationSettings(), meshModel.getBoundAppKeyIndexes());
        }
        updatePublicationValues();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.SELECT_KEY) {
            if (resultCode == RESULT_OK) {
                final ApplicationKey appKey = data.getParcelableExtra(RESULT_KEY);
                if (appKey != null) {
                    ((PublicationViewModel)mViewModel).setAppKeyIndex(appKey.getKeyIndex());
                    binding.appKey.setText(getString(R.string.app_key_index, appKey.getKeyIndex()));
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
        ((PublicationViewModel)mViewModel).setLabelUUID(null);
        ((PublicationViewModel)mViewModel).setPublishAddress(address);
        binding.publishAddress.setText(MeshAddress.formatAddress(address, true));
    }

    @Override
    public void onDestinationAddressSet(@NonNull final Group group) {
        ((PublicationViewModel)mViewModel).setLabelUUID(group.getAddressLabel());
        ((PublicationViewModel)mViewModel).setPublishAddress(group.getAddress());
        binding.publishAddress.setText(MeshAddress.formatAddress(group.getAddress(), true));
    }

    @Override
    public void setPublishTtl(final int ttl) {
        ((PublicationViewModel)mViewModel).setPublishTtl(ttl);
        updateTtlUi();
    }

    private void updatePublicationValues() {
        updateUi();
        updateTtlUi();
    }

    private void updateUi() {
        final UUID labelUUID = ((PublicationViewModel)mViewModel).getLabelUUID();
        if (labelUUID == null) {
            binding.publishAddress.setText(MeshAddress.formatAddress(((PublicationViewModel)mViewModel).getPublishAddress(), true));
        } else {
            binding.publishAddress.setText(labelUUID.toString().toUpperCase(Locale.US));
        }

        if (MeshAddress.isValidUnassignedAddress(((PublicationViewModel)mViewModel).getPublishAddress())) {
            binding.publishAddress.setText(R.string.not_assigned);
        } else {
            binding.publishAddress.setText(MeshAddress.formatAddress(((PublicationViewModel)mViewModel).getPublishAddress(), true));
        }
        final MeshNetwork network = ((PublicationViewModel)mViewModel).getNetworkLiveData().getMeshNetwork();
        binding.appKey.setText(getString(R.string.app_key_index, ((PublicationViewModel)mViewModel).getAppKeyIndex()));
        if (network != null) {
            final ApplicationKey key = network.getAppKey(((PublicationViewModel)mViewModel).getAppKeyIndex());
            if (key != null) {
                binding.appKey.setText(getString(R.string.key_name_and_index, key.getName(), ((PublicationViewModel)mViewModel).getAppKeyIndex()));
            } else {
                binding.appKey.setText(getString(R.string.unavailable));
            }
        } else {
            binding.appKey.setText(getString(R.string.unavailable));
        }

        binding.friendshipCredentialFlag.setChecked(((PublicationViewModel)mViewModel).getFriendshipCredentialsFlag());
        updatePublishPeriodUi();

        binding.retransmissionSlider.setValue(((PublicationViewModel)mViewModel).getRetransmitCount());
        binding.intervalStepsSlider.setValue(((PublicationViewModel)mViewModel).getRetransmissionInterval());

    }

    private void updateTtlUi() {
        final int ttl = ((PublicationViewModel)mViewModel).getPublishTtl();
        if (isDefaultPublishTtl(ttl)) {
            binding.publicationTtl.setText(getString(R.string.uses_default_ttl));
        } else {
            binding.publicationTtl.setText(String.valueOf(ttl));
        }
    }

    private void setPublication() {
        final ProvisionedMeshNode node = ((PublicationViewModel)mViewModel).getSelectedMeshNode().getValue();
        if (node != null) {
            final MeshMessage configModelPublicationSet = ((PublicationViewModel)mViewModel).createMessage();
            if (configModelPublicationSet != null) {
                try {
                    ((PublicationViewModel)mViewModel)
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

    @Override
    protected void updateClickableViews() {
        //Do nothing
    }

    @Override
    protected void showProgressBar() {
        //Do nothing
    }

    @Override
    protected void hideProgressBar() {
        //Do nothing
    }

    @Override
    protected void enableClickableViews() {
        //Do nothing
    }

    @Override
    protected void disableClickableViews() {
        //Do nothing
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        //Do nothing
    }

    private void updatePublishPeriodUi() {
        final int sliderValue;
        final int stringResource;
        switch (((PublicationViewModel)mViewModel).getPublicationResolution()) {
            default:
            case 0:
                sliderValue = ((PublicationViewModel)mViewModel).getPublicationSteps();
                stringResource = R.string.time_ms;
                break;
            case 1:
                sliderValue = 57 + ((PublicationViewModel)mViewModel).getPublicationSteps();
                stringResource = R.string.time_s;
                break;
            case 2:
                sliderValue = 114 + ((PublicationViewModel)mViewModel).getPublicationSteps();
                stringResource = R.string.time_s;
                break;
            case 3:
                sliderValue = 171 + ((PublicationViewModel)mViewModel).getPublicationSteps();
                stringResource = R.string.time_m;
                break;
        }
        binding.publishIntervalSlider.setValue(sliderValue);
        final int period = ((PublicationViewModel)mViewModel).getPublishPeriod();
        if (sliderValue == 0) {
            binding.pubInterval.setText(R.string.disabled);
        } else {
            binding.pubInterval.setText(getString(stringResource, period));
        }
    }
}
