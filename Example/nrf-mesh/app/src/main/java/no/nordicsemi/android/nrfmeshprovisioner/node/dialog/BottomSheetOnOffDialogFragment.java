package no.nordicsemi.android.nrfmeshprovisioner.node.dialog;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class BottomSheetOnOffDialogFragment extends BottomSheetDialogFragment {
    private static final String KEY_INDEX = "KEY_INDEX";
    private int mKeyIndex;
    private int mTransitionStepResolution;
    private int mTransitionSteps;

    public interface BottomSheetOnOffListener {
        void toggle(final int mKeyIndex, final boolean state, final int mTransitionSteps, final int mTransitionStepResolution, final int progress);
    }

    public static BottomSheetOnOffDialogFragment getInstance(final int appKeyIndex) {
        final BottomSheetOnOffDialogFragment fragment = new BottomSheetOnOffDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(KEY_INDEX, appKeyIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mKeyIndex = getArguments().getInt(KEY_INDEX);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View nodeControlsContainer = inflater.inflate(R.layout.layout_generic_on_off_bottom_sheet, container, false);

        final TextView time = nodeControlsContainer.findViewById(R.id.transition_time);
        final SeekBar transitionTimeSeekBar = nodeControlsContainer.findViewById(R.id.transition_seekbar);
        transitionTimeSeekBar.setProgress(0);
        transitionTimeSeekBar.incrementProgressBy(1);
        transitionTimeSeekBar.setMax(230);

        final SeekBar delaySeekBar = nodeControlsContainer.findViewById(R.id.delay_seekbar);
        delaySeekBar.setProgress(0);
        delaySeekBar.setMax(255);
        final TextView delayTime = nodeControlsContainer.findViewById(R.id.delay_time);

        final Button actionOn = nodeControlsContainer.findViewById(R.id.action_on);
        actionOn.setOnClickListener(v -> {
            try {
                ((BottomSheetOnOffListener) requireActivity()).toggle(mKeyIndex, true, mTransitionSteps, mTransitionStepResolution, delaySeekBar.getProgress());
            } catch (IllegalArgumentException ex) {
                Toast.makeText(requireContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        final Button actionOff = nodeControlsContainer.findViewById(R.id.action_off);
        actionOff.setOnClickListener(v -> {
            try {
                ((BottomSheetOnOffListener) requireActivity()).toggle(mKeyIndex, false, mTransitionSteps, mTransitionStepResolution, delaySeekBar.getProgress());
            } catch (IllegalArgumentException ex) {
                Toast.makeText(requireContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        transitionTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int lastValue = 0;
            double res = 0.0;

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

                if (progress >= 0 && progress <= 62) {
                    lastValue = progress;
                    mTransitionStepResolution = 0;
                    mTransitionSteps = progress;
                    res = progress / 10.0;
                    time.setText(getString(R.string.transition_time_interval, String.valueOf(res), "s"));
                } else if (progress >= 63 && progress <= 118) {
                    if (progress > lastValue) {
                        mTransitionSteps = progress - 56;
                        lastValue = progress;
                    } else if (progress < lastValue) {
                        mTransitionSteps = -(56 - progress);
                    }
                    mTransitionStepResolution = 1;
                    time.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionSteps), "s"));

                } else if (progress >= 119 && progress <= 174) {
                    if (progress > lastValue) {
                        mTransitionSteps = progress - 112;
                        lastValue = progress;
                    } else if (progress < lastValue) {
                        mTransitionSteps = -(112 - progress);
                    }
                    mTransitionStepResolution = 2;
                    time.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionSteps * 10), "s"));
                } else if (progress >= 175 && progress <= 230) {
                    if (progress >= lastValue) {
                        mTransitionSteps = progress - 168;
                        lastValue = progress;
                    } else {
                        mTransitionSteps = -(168 - progress);
                    }
                    mTransitionStepResolution = 3;
                    time.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionSteps * 10), "min"));
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {

            }
        });

        delaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                delayTime.setText(getString(R.string.transition_time_interval, String.valueOf(progress * MeshParserUtils.GENERIC_ON_OFF_5_MS), "ms"));
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {

            }
        });
        return nodeControlsContainer;
    }
}
