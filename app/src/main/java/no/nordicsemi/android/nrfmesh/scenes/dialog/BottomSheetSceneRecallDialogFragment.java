package no.nordicsemi.android.nrfmesh.scenes.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.Slider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.Scene;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutRecallSceneBottomSheetBinding;

public class BottomSheetSceneRecallDialogFragment extends BottomSheetDialogFragment {
    private static final String SCENE = "SCENE";
    private int mTransitionStepResolution;
    private int mTransitionSteps;
    private Scene mScene;

    public interface SceneRecallListener {
        void recallScene(@NonNull final Scene scene, final int transitionSteps, final int transitionStepResolution, final int delay);
    }

    public static BottomSheetSceneRecallDialogFragment instantiate(@NonNull final Scene scene) {
        final BottomSheetSceneRecallDialogFragment fragment = new BottomSheetSceneRecallDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(SCENE, scene);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mScene = getArguments().getParcelable(SCENE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final LayoutRecallSceneBottomSheetBinding binding = LayoutRecallSceneBottomSheetBinding.inflate(getLayoutInflater(), container, false);
        binding.sceneRecallToolbar.setTitle(mScene.getName());

        binding.transitionSlider.setValueFrom(0);
        binding.transitionSlider.setValueTo(230);
        binding.transitionSlider.setStepSize(1);
        binding.transitionSlider.setValue(0);

        binding.delaySlider.setValueFrom(0);
        binding.delaySlider.setValueTo(255);
        binding.delaySlider.setValue(0);

        final Button actionRecallScene = binding.actionRecall;
        actionRecallScene.setOnClickListener(v -> {
            try {
                ((SceneRecallListener) requireActivity()).recallScene(mScene, mTransitionSteps, mTransitionStepResolution, (int) binding.delaySlider.getValue());
                dismiss();
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

        binding.delaySlider.addOnChangeListener((slider, value, fromUser) ->
                binding.delayTime.setText(getString(R.string.transition_time_interval, String.valueOf((int) value * MeshParserUtils.GENERIC_ON_OFF_5_MS), "ms")));
        return binding.getRoot();
    }
}
