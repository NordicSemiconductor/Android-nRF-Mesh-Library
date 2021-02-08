package no.nordicsemi.android.nrfmesh.node;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.models.ConfigurationServerModel;
import no.nordicsemi.android.mesh.transport.ConfigHeartbeatSubscriptionSet;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.Heartbeat;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.GroupCallbacks;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityHeartbeatSubscriptionBinding;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.node.dialog.DestinationAddressCallbacks;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentHeartbeatDestination;
import no.nordicsemi.android.nrfmesh.node.dialog.DialogFragmentHeartbeatSource;
import no.nordicsemi.android.nrfmesh.viewmodels.HeartbeatViewModel;

import static no.nordicsemi.android.mesh.utils.Heartbeat.calculateHeartbeatPeriod;
import static no.nordicsemi.android.nrfmesh.utils.Utils.CONNECT_TO_NETWORK;

@AndroidEntryPoint
public class HeartbeatSubscriptionActivity extends AppCompatActivity implements
        GroupCallbacks,
        DestinationAddressCallbacks,
        DialogFragmentHeartbeatSource.SubscriptionAddressCallbacks {

    private static final String SOURCE = "SOURCE";
    private static final String DESTINATION = "DESTINATION";
    private static final String PERIOD = "PERIOD";

    private ActivityHeartbeatSubscriptionBinding binding;
    private HeartbeatViewModel mViewModel;

    private boolean mIsConnected;
    private int mSource;
    private int mDestination;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHeartbeatSubscriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mViewModel = new ViewModelProvider(this).get(HeartbeatViewModel.class);

        final ConfigurationServerModel meshModel = (ConfigurationServerModel) mViewModel.getSelectedModel().getValue();
        if (meshModel == null)
            finish();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setTitle(R.string.title_heartbeat_subscription);

        binding.scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (binding.scrollView.getScrollY() == 0) {
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

        binding.containerSrcAddress.setOnClickListener(v -> {
            final DialogFragmentHeartbeatSource source = DialogFragmentHeartbeatSource.
                    newInstance(meshModel.getHeartbeatSubscription());
            source.show(getSupportFragmentManager(), null);
        });

        binding.containerDstAddress.setOnClickListener(v -> {
            final ArrayList<Group> groups = new ArrayList<>();
            for (Group group : mViewModel.getNetworkLiveData().getMeshNetwork().getGroups()) {
                if (MeshAddress.isValidGroupAddress(group.getAddress()))
                    groups.add(group);
            }
            final DialogFragmentHeartbeatDestination destination = DialogFragmentHeartbeatDestination.
                    newInstance(2, groups);
            destination.show(getSupportFragmentManager(), null);
        });

        binding.periodSlider.setValueFrom(0x01);
        binding.periodSlider.setValueTo(0x11);
        binding.periodSlider.setStepSize(1);
        binding.periodSlider.addOnChangeListener((slider, value, fromUser) -> binding.period
                .setText(Heartbeat.periodToTime(calculateHeartbeatPeriod((short) value))));

        binding.fabApply.setOnClickListener(v -> {
            if (!checkConnectivity()) return;
            setSubscription();
        });
        if (meshModel.getHeartbeatSubscription() != null) {
            updateSourceAddress(meshModel.getHeartbeatSubscription().getSrc());
            updateDestinationAddress(meshModel.getHeartbeatSubscription().getDst());
        } else {
            updateSourceAddress(mSource);
            updateDestinationAddress(mDestination);
        }
        binding.periodSlider.setValue(1);
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
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_connect) {
            mViewModel.navigateToScannerActivity(this, false, CONNECT_TO_NETWORK, false);
            return true;
        } else if (itemId == R.id.action_disconnect) {
            mViewModel.disconnect();
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SOURCE, mSource);
        outState.putInt(DESTINATION, mDestination);
        outState.putFloat(PERIOD, binding.periodSlider.getValue());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        updateSourceAddress(mSource = savedInstanceState.getInt(SOURCE, 0));
        updateDestinationAddress(mDestination = savedInstanceState.getInt(DESTINATION, 0));
        updatePeriod(savedInstanceState.getInt(PERIOD));
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

    private void updateSourceAddress(final int address) {
        mSource = address;
        if (address == 0)
            binding.sourceAddress.setText(getString(R.string.not_assigned));
        else
            binding.sourceAddress.setText(MeshAddress.formatAddress(address, true));
    }

    private void updateDestinationAddress(final int address) {
        if (address == 0) {
            binding.destinationAddress.setText(getString(R.string.not_assigned));
        } else
            binding.destinationAddress.setText(MeshAddress.formatAddress(address, true));
    }

    private void updatePeriod(final int period) {
        binding.periodSlider.setValue(period == 0 ? 1 : period);
    }

    private void setSubscription() {
        if (mSource == 0) {
            mViewModel.displaySnackBar(this, binding.container, getString(R.string.error_set_src), Snackbar.LENGTH_SHORT);
        } else if (mDestination == 0) {
            mViewModel.displaySnackBar(this, binding.container, getString(R.string.error_set_dst), Snackbar.LENGTH_SHORT);
        } else {
            final ProvisionedMeshNode node = mViewModel.getSelectedMeshNode().getValue();
            final Element element = mViewModel.getSelectedElement().getValue();
            final MeshModel model = mViewModel.getSelectedModel().getValue();
            final MeshMessage heartbeatSubscription;
            if (node != null && element != null && model != null) {
                try {
                    heartbeatSubscription = new ConfigHeartbeatSubscriptionSet(mSource, mDestination, ((byte) binding.periodSlider.getValue()));
                    sendMessage(node.getUnicastAddress(), heartbeatSubscription);
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
