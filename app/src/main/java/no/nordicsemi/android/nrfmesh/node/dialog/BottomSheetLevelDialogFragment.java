package no.nordicsemi.android.nrfmesh.node.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.Slider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutGenericLevelBottomSheetBinding;

public class BottomSheetLevelDialogFragment extends BottomSheetDialogFragment {
    private static final String KEY_INDEX = "KEY_INDEX";
    private int mKeyIndex;
    private int mTransitionStepResolution;
    private int mTransitionSteps;

    public interface BottomSheetLevelListener {
        void toggleLevel(final int keyIndex, final int level, final int mTransitionSteps, final int mTransitionStepResolution, final int progress);
    }

    public static BottomSheetLevelDialogFragment getInstance(final int appKeyIndex) {
        final BottomSheetLevelDialogFragment fragment = new BottomSheetLevelDialogFragment();
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
        final LayoutGenericLevelBottomSheetBinding binding = LayoutGenericLevelBottomSheetBinding.inflate(getLayoutInflater());

        final TextView time = binding.transitionTime;
        final Slider transitionTimeSlider = binding.transitionSlider;
        transitionTimeSlider.setValueFrom(0);
        transitionTimeSlider.setValueTo(230);
        transitionTimeSlider.setValue(0);
        transitionTimeSlider.setStepSize(1);

        final Slider delaySlider = binding.delaySlider;
        delaySlider.setValueFrom(0);
        delaySlider.setValueTo(255);
        delaySlider.setValue(0);
        final TextView delayTime = binding.delayTime;

        final TextView level = binding.level;
        final Slider levelSlider = binding.levelSeekBar;
        levelSlider.setValueFrom(0);
        levelSlider.setValueTo(100);
        levelSlider.setValue(0);

        transitionTimeSlider.addOnChangeListener(new Slider.OnChangeListener() {
            int lastValue = 0;
            double res = 0.0;

            @Override
            public void onValueChange(@NonNull final Slider slider, final float value, final boolean fromUser) {
                final int progress = (int) value;
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
        });
        delaySlider.addOnChangeListener((slider, value, fromUser) -> delayTime.setText(getString(R.string.transition_time_interval, String.valueOf((int) value * MeshParserUtils.GENERIC_ON_OFF_5_MS), "ms")));

        levelSlider.addOnChangeListener((slider, value, fromUser) -> level.setText(getString(R.string.generic_level_percent, (int) value)));
        levelSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull final Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull final Slider slider) {
                final int level = (int) slider.getValue();
                final int delay = (int) levelSlider.getValue();
                final int genericLevel = ((level * 65535) / 100) - 32768;
                ((BottomSheetLevelListener) requireActivity()).toggleLevel(mKeyIndex, genericLevel, mTransitionSteps, mTransitionStepResolution, delay);
            }
        });
        return binding.getRoot();
    }
}
