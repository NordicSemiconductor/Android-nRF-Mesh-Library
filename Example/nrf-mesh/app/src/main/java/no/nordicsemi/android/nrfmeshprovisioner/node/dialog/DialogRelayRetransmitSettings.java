package no.nordicsemi.android.nrfmeshprovisioner.node.dialog;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.utils.RelaySettings;
import no.nordicsemi.android.nrfmeshprovisioner.R;


public class DialogRelayRetransmitSettings extends DialogFragment {

    private static final String TAG = DialogRelayRetransmitSettings.class.getSimpleName();

    private static final String RELAY = "RELAY";
    private static final String TRANSMIT_COUNT = "TRANSMIT_COUNT";
    private static final String TRANSMIT_INTERVAL_STEPS = "TRANSMIT_INTERVAL_STEPS";

    private static final int MIN_RETRANSMIT_COUNT = 0;
    private static final int MAX_RETRANSMIT_COUNT = 0b111;

    private static final int MIN_RETRANSMIT_INTERVAL_STEPS = 0;
    private static final int MAX_RETRANSMIT_INTERVAL_STEPS = 0b11111;

    @BindView(R.id.switch_relay_state)
    Switch relaySwitch;
    @BindView(R.id.dialog_relay_retransmit_count)
    TextView relayRetransmitCountText;
    @BindView(R.id.dialog_relay_retransmit_count_seekbar)
    SeekBar retransmitCountSeekBar;
    @BindView(R.id.dialog_relay_interval_steps)
    TextView relayRetransmitIntervalStepsText;
    @BindView(R.id.dialog_relay_interval_steps_seekbar)
    SeekBar retransmitIntervalStepsSeekBar;

    private int mRelay = 0;
    private int mTransmitCount = 0;
    private int mTransmitIntervalSteps = 0;

    public static DialogRelayRetransmitSettings newInstance(final int relay, final int transmitCount, final int transmitIntervalSteps) {
        DialogRelayRetransmitSettings fragment = new DialogRelayRetransmitSettings();
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
        @SuppressLint("InflateParams")
        final View rootView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_fragment_relay_settings, null);
        ButterKnife.bind(this, rootView);

        setRelay(mRelay);
        setRelayRetransmitCount(mTransmitCount);
        setRelayRetransmitIntervalSteps(mTransmitIntervalSteps);

        relaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mRelay = isChecked ? 1 : 0);

        retransmitCountSeekBar.setProgress(mTransmitCount);
        retransmitCountSeekBar.setMax(MAX_RETRANSMIT_COUNT);
        retransmitCountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setRelayRetransmitCount(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        retransmitIntervalStepsSeekBar.setProgress(mTransmitIntervalSteps);
        retransmitIntervalStepsSeekBar.setMax(MAX_RETRANSMIT_INTERVAL_STEPS);
        retransmitIntervalStepsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setRelayRetransmitIntervalSteps(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return new AlertDialog.Builder(requireContext())
                .setView(rootView)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    if (getParentFragment() == null) {
                        ((DialogFragmentRelaySettingsListener) requireActivity())
                                .onRelayRetransmitSet(mRelay, mTransmitCount, mTransmitIntervalSteps);
                    } else {
                        ((DialogFragmentRelaySettingsListener) getParentFragment())
                                .onRelayRetransmitSet(mRelay, mTransmitCount, mTransmitIntervalSteps);
                    }
                }).setNegativeButton(R.string.cancel, null)
                .setIcon(R.drawable.ic_repeat_black_24dp)
                .setTitle(R.string.title_relay_retransmit).create();
    }

    private void setRelay(final int relay) {
        mRelay = relay;
        relaySwitch.setChecked(RelaySettings.isRelaySupported(relay));
    }

    private void setRelayRetransmitCount(final int relayRetransmitCount) {
        mTransmitCount = relayRetransmitCount;
        relayRetransmitCountText.setText(getResources().getQuantityString(
                R.plurals.transmit_count, relayRetransmitCount, relayRetransmitCount));
    }

    private void setRelayRetransmitIntervalSteps(final int transmitIntervalSteps) {
        mTransmitIntervalSteps = transmitIntervalSteps;
        final int transmitIntervalMilliseconds = transmitIntervalSteps * 10;
        relayRetransmitIntervalStepsText.setText(getResources().getString(
                R.string.time_ms, transmitIntervalMilliseconds));
    }

    public interface DialogFragmentRelaySettingsListener {

        void onRelayRetransmitSet(final int relay, final int transmitCount, final int transmitIntervalSteps);

    }
}
