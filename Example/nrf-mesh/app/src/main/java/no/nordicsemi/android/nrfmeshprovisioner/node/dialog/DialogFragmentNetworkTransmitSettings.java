package no.nordicsemi.android.nrfmeshprovisioner.node.dialog;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.nrfmeshprovisioner.R;


public class DialogFragmentNetworkTransmitSettings extends DialogFragment {

    private static final String TAG = DialogFragmentNetworkTransmitSettings.class.getSimpleName();

    private static final String TRANSMIT_COUNT = "TRANSMIT_COUNT";
    private static final String TRANSMIT_INTERVAL_STEPS = "TRANSMIT_INTERVAL_STEPS";

    private static final int MIN_TRANSMIT_COUNT = 0;
    private static final int MAX_TRANSMIT_COUNT = 0b111;

    private static final int MIN_TRANSMIT_INTERVAL_STEPS = 0;
    private static final int MAX_TRANSMIT_INTERVAL_STEPS = 0b11111;

    @BindView(R.id.dialog_network_transmit_count)
    TextView networkTransmitCountText;
    @BindView(R.id.dialog_network_transmit_count_seekbar)
    SeekBar transmitCountBar;
    @BindView(R.id.dialog_network_transmit_interval_steps)
    TextView networkTransmitIntervalStepsText;
    @BindView(R.id.dialog_network_transmit_interval_steps_seekbar)
    SeekBar transmitIntervalStepsBar;

    private int mTransmitCount = 0;
    private int mTransmitIntervalSteps = 0;

    public static DialogFragmentNetworkTransmitSettings newInstance(final int transmitCount, final int transmitIntervalSteps) {
        DialogFragmentNetworkTransmitSettings fragment = new DialogFragmentNetworkTransmitSettings();
        final Bundle args = new Bundle();
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
            mTransmitCount = args.getInt(TRANSMIT_COUNT);
            mTransmitIntervalSteps = args.getInt(TRANSMIT_INTERVAL_STEPS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_network_transmit_settings, null);
        ButterKnife.bind(this, rootView);

        setTransmitCount(mTransmitCount);
        setTransmitIntervalSteps(mTransmitIntervalSteps);

        transmitCountBar.setProgress(mTransmitCount);
        transmitCountBar.setMax(MAX_TRANSMIT_COUNT);
        transmitCountBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setTransmitCount(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        transmitIntervalStepsBar.setProgress(mTransmitIntervalSteps);
        transmitIntervalStepsBar.setMax(MAX_TRANSMIT_INTERVAL_STEPS);
        transmitIntervalStepsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setTransmitIntervalSteps(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext()).setView(rootView)
                .setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null);

        alertDialogBuilder.setIcon(R.drawable.ic_repeat_black_24dp);
        alertDialogBuilder.setTitle(R.string.title_network_transmit);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            ((DialogFragmentNetworkTransmitSettings.DialogFragmentNetworkTransmitSettingsListener) requireActivity())
                    .onNetworkTransmitSettingsEntered(mTransmitCount, mTransmitIntervalSteps);
            dismiss();
        });

        return alertDialog;
    }

    private void setTransmitCount(final int transmitCount) {
        mTransmitCount = transmitCount;
        final int transmitCountActual = mTransmitCount + 1;
        networkTransmitCountText.setText(getResources().getQuantityString(
                R.plurals.transmit_count, transmitCountActual, transmitCountActual));
    }

    private void setTransmitIntervalSteps(final int transmitIntervalSteps) {
        mTransmitIntervalSteps = transmitIntervalSteps;
        final int transmitIntervalMilliseconds = (mTransmitIntervalSteps + 1) * 10;
        networkTransmitIntervalStepsText.setText(getResources().getString(
                R.string.time_ms, transmitIntervalMilliseconds));
    }

    public interface DialogFragmentNetworkTransmitSettingsListener {

        void onNetworkTransmitSettingsEntered(final int transmitCount, final int transmitIntervalSteps);

    }
}
