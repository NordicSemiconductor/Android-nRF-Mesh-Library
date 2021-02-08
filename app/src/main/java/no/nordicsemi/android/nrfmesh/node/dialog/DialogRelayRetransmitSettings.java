package no.nordicsemi.android.nrfmesh.node.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import no.nordicsemi.android.mesh.utils.RelaySettings;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.DialogFragmentRelaySettingsBinding;


public class DialogRelayRetransmitSettings extends DialogFragment {

    private static final String RELAY = "RELAY";
    private static final String TRANSMIT_COUNT = "TRANSMIT_COUNT";
    private static final String TRANSMIT_INTERVAL_STEPS = "TRANSMIT_INTERVAL_STEPS";

    private static final int MIN_RETRANSMIT_COUNT = 0;
    private static final int MAX_RETRANSMIT_COUNT = 0b111;

    private static final int MIN_RETRANSMIT_INTERVAL_STEPS = 0;
    private static final int MAX_RETRANSMIT_INTERVAL_STEPS = 0b11111;

    private DialogFragmentRelaySettingsBinding binding;

    private int mRelay = 0;
    private int mTransmitCount = 0;
    private int mTransmitIntervalSteps = 0;

    public static DialogRelayRetransmitSettings newInstance(final int relay, final int transmitCount, final int transmitIntervalSteps) {
        final DialogRelayRetransmitSettings fragment = new DialogRelayRetransmitSettings();
        final Bundle args = new Bundle();
        args.putInt(RELAY, relay);
        args.putInt(TRANSMIT_COUNT, transmitCount);
        args.putInt(TRANSMIT_INTERVAL_STEPS, transmitIntervalSteps);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mRelay = args.getInt(RELAY);
            mTransmitCount = args.getInt(TRANSMIT_COUNT);
            mTransmitIntervalSteps = args.getInt(TRANSMIT_INTERVAL_STEPS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        binding = DialogFragmentRelaySettingsBinding.inflate(getLayoutInflater());

        setRelay(mRelay);
        setRelayRetransmitCount(mTransmitCount);
        setRelayRetransmitIntervalSteps(mTransmitIntervalSteps);

        binding.switchRelayState.setOnCheckedChangeListener((buttonView, isChecked) -> mRelay = isChecked ? 1 : 0);

        binding.dialogRelayRetransmitCountSlider.setValueFrom(MIN_RETRANSMIT_COUNT);
        binding.dialogRelayRetransmitCountSlider.setValueTo(MAX_RETRANSMIT_COUNT);
        binding.dialogRelayRetransmitCountSlider.setValue(mTransmitCount);
        binding.dialogRelayRetransmitCountSlider.setStepSize(1);
        binding.dialogRelayRetransmitCountSlider.addOnChangeListener((slider, value, fromUser) -> setRelayRetransmitCount((int) value));

        binding.dialogRelayIntervalStepsSlider.setValueFrom(MIN_RETRANSMIT_INTERVAL_STEPS);
        binding.dialogRelayIntervalStepsSlider.setValueTo(MAX_RETRANSMIT_INTERVAL_STEPS);
        binding.dialogRelayIntervalStepsSlider.setValue(mTransmitIntervalSteps);
        binding.dialogRelayIntervalStepsSlider.addOnChangeListener((slider, value, fromUser) -> setRelayRetransmitIntervalSteps((int) value));

        return new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    if (getParentFragment() == null) {
                        ((DialogFragmentRelaySettingsListener) requireActivity())
                                .onRelayRetransmitSet(mRelay, mTransmitCount, mTransmitIntervalSteps);
                    } else {
                        ((DialogFragmentRelaySettingsListener) getParentFragment())
                                .onRelayRetransmitSet(mRelay, mTransmitCount, mTransmitIntervalSteps);
                    }
                }).setNegativeButton(R.string.cancel, null)
                .setIcon(R.drawable.ic_repeat)
                .setTitle(R.string.title_relay_retransmit).create();
    }

    private void setRelay(final int relay) {
        mRelay = relay;
        binding.switchRelayState.setChecked(RelaySettings.isRelaySupported(relay));
    }

    private void setRelayRetransmitCount(final int relayRetransmitCount) {
        mTransmitCount = relayRetransmitCount;
        binding.dialogRelayRetransmitCount.setText(getResources().getQuantityString(
                R.plurals.transmit_count, relayRetransmitCount, relayRetransmitCount));
    }

    private void setRelayRetransmitIntervalSteps(final int transmitIntervalSteps) {
        mTransmitIntervalSteps = transmitIntervalSteps;
        final int transmitIntervalMilliseconds = transmitIntervalSteps * 10;
        binding.dialogRelayIntervalSteps.setText(getResources().getString(
                R.string.time_ms, transmitIntervalMilliseconds));
    }

    public interface DialogFragmentRelaySettingsListener {
        void onRelayRetransmitSet(final int relay, final int transmitCount, final int transmitIntervalSteps);
    }
}
