package no.nordicsemi.android.nrfmesh.node.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.slider.Slider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.nrfmesh.R;


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
    @BindView(R.id.dialog_network_transmit_count_slider)
    Slider transmitCountSlider;
    @BindView(R.id.dialog_network_transmit_interval_steps)
    TextView networkTransmitIntervalStepsText;
    @BindView(R.id.dialog_network_transmit_interval_steps_slider)
    Slider transmitIntervalStepsSlider;

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
        @SuppressLint("InflateParams") final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_network_transmit_settings, null);
        ButterKnife.bind(this, rootView);

        setTransmitCount(mTransmitCount);
        setTransmitIntervalSteps(mTransmitIntervalSteps);

        transmitCountSlider.setValueFrom(MIN_TRANSMIT_COUNT);
        transmitCountSlider.setValueTo(MAX_TRANSMIT_COUNT);
        transmitCountSlider.setStepSize(1);
        transmitCountSlider.setValue(mTransmitCount);
        transmitCountSlider.addOnChangeListener((slider, value, fromUser) -> setTransmitCount((int) value));

        transmitIntervalStepsSlider.setValueFrom(MIN_TRANSMIT_INTERVAL_STEPS);
        transmitIntervalStepsSlider.setValueTo(MAX_TRANSMIT_INTERVAL_STEPS);
        transmitIntervalStepsSlider.setValue(mTransmitIntervalSteps);
        transmitIntervalStepsSlider.addOnChangeListener((slider, value, fromUser) -> setTransmitIntervalSteps((int) value));

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext()).setView(rootView)
                .setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null);

        alertDialogBuilder.setIcon(R.drawable.ic_repeat);
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
