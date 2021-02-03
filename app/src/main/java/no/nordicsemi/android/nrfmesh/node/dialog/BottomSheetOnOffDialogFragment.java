package no.nordicsemi.android.nrfmesh.node.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.Slider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutGenericOnOffBottomSheetBinding;

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
        final LayoutGenericOnOffBottomSheetBinding binding = LayoutGenericOnOffBottomSheetBinding.inflate(getLayoutInflater());

        binding.transitionSlider.setValueFrom(0);
        binding.transitionSlider.setValueTo(230);
        binding.transitionSlider.setStepSize(1);
        binding.transitionSlider.setValue(0);

        binding.delaySlider.setValueFrom(0);
        binding.delaySlider.setValueTo(255);
        binding.delaySlider.setValue(0);

        binding.actionOn.setOnClickListener(v -> {
            try {
                ((BottomSheetOnOffListener) requireActivity()).toggle(mKeyIndex, true, mTransitionSteps, mTransitionStepResolution, (int) binding.delaySlider.getValue());
            } catch (IllegalArgumentException ex) {
                Toast.makeText(requireContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.actionOff.setOnClickListener(v -> {
            try {
                ((BottomSheetOnOffListener) requireActivity()).toggle(mKeyIndex, false, mTransitionSteps, mTransitionStepResolution, (int) binding.delaySlider.getValue());
            } catch (IllegalArgumentException ex) {
                Toast.makeText(requireContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        binding.transitionSlider.addOnChangeListener(new Slider.OnChangeListener() {
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
                    binding.transitionTime.setText(getString(R.string.transition_time_interval, String.valueOf(res), "s"));
                } else if (progress >= 63 && progress <= 118) {
                    if (progress > lastValue) {
                        mTransitionSteps = progress - 56;
                        lastValue = progress;
                    } else if (progress < lastValue) {
                        mTransitionSteps = -(56 - progress);
                    }
                    mTransitionStepResolution = 1;
                    binding.transitionTime.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionSteps), "s"));

                } else if (progress >= 119 && progress <= 174) {
                    if (progress > lastValue) {
                        mTransitionSteps = progress - 112;
                        lastValue = progress;
                    } else if (progress < lastValue) {
                        mTransitionSteps = -(112 - progress);
                    }
                    mTransitionStepResolution = 2;
                    binding.transitionTime.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionSteps * 10), "s"));
                } else if (progress >= 175 && progress <= 230) {
                    if (progress >= lastValue) {
                        mTransitionSteps = progress - 168;
                        lastValue = progress;
                    } else {
                        mTransitionSteps = -(168 - progress);
                    }
                    mTransitionStepResolution = 3;
                    binding.transitionTime.setText(getString(R.string.transition_time_interval, String.valueOf(mTransitionSteps * 10), "min"));
                }
            }
        });

        binding.delaySlider.
                addOnChangeListener((slider, value, fromUser) ->
                        binding.delayTime.
                                setText(getString(R.string.transition_time_interval, String.valueOf((int) value * MeshParserUtils.GENERIC_ON_OFF_5_MS), "ms")));
        return binding.getRoot();
    }
}
