package no.nordicsemi.android.nrfmesh.node.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.DialogFragmentNetworkTransmitSettingsBinding;


public class DialogFragmentNetworkTransmitSettings extends DialogFragment {

    private static final String TRANSMIT_COUNT = "TRANSMIT_COUNT";
    private static final String TRANSMIT_INTERVAL_STEPS = "TRANSMIT_INTERVAL_STEPS";

    private static final int MIN_TRANSMIT_COUNT = 0;
    private static final int MAX_TRANSMIT_COUNT = 0b111;

    private static final int MIN_TRANSMIT_INTERVAL_STEPS = 0;
    private static final int MAX_TRANSMIT_INTERVAL_STEPS = 0b11111;
    private DialogFragmentNetworkTransmitSettingsBinding binding;

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
        binding = DialogFragmentNetworkTransmitSettingsBinding.inflate(getLayoutInflater());

        setTransmitCount(mTransmitCount);
        setTransmitIntervalSteps(mTransmitIntervalSteps);

        binding.dialogNetworkTransmitCountSlider.setValueFrom(MIN_TRANSMIT_COUNT);
        binding.dialogNetworkTransmitCountSlider.setValueTo(MAX_TRANSMIT_COUNT);
        binding.dialogNetworkTransmitCountSlider.setStepSize(1);
        binding.dialogNetworkTransmitCountSlider.setValue(mTransmitCount);
        binding.dialogNetworkTransmitCountSlider.addOnChangeListener((slider, value, fromUser) -> setTransmitCount((int) value));

        binding.dialogNetworkTransmitIntervalStepsSlider.setValueFrom(MIN_TRANSMIT_INTERVAL_STEPS);
        binding.dialogNetworkTransmitIntervalStepsSlider.setValueTo(MAX_TRANSMIT_INTERVAL_STEPS);
        binding.dialogNetworkTransmitIntervalStepsSlider.setValue(mTransmitIntervalSteps);
        binding.dialogNetworkTransmitIntervalStepsSlider.addOnChangeListener((slider, value, fromUser) -> setTransmitIntervalSteps((int) value));

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext()).setView(binding.getRoot())
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
        binding.dialogNetworkTransmitCount.setText(getResources().getQuantityString(
                R.plurals.transmit_count, transmitCountActual, transmitCountActual));
    }

    private void setTransmitIntervalSteps(final int transmitIntervalSteps) {
        mTransmitIntervalSteps = transmitIntervalSteps;
        final int transmitIntervalMilliseconds = (mTransmitIntervalSteps + 1) * 10;
        binding.dialogNetworkTransmitIntervalSteps.setText(getResources().getString(
                R.string.time_ms, transmitIntervalMilliseconds));
    }

    public interface DialogFragmentNetworkTransmitSettingsListener {
        void onNetworkTransmitSettingsEntered(final int transmitCount, final int transmitIntervalSteps);
    }
}
