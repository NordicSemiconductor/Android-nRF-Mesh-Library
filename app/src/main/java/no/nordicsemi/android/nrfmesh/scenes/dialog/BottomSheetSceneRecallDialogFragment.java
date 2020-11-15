package no.nordicsemi.android.nrfmesh.scenes.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.Scene;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;

public class BottomSheetSceneRecallDialogFragment extends BottomSheetDialogFragment {
    private static final String SCENE = "SCENE";
    private int mTransitionStepResolution;
    private int mTransitionSteps;
    private Scene mScene;

    public interface SceneRecallListener {
        void recallScene(@NonNull final Scene scene);

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
        final View nodeControlsContainer = inflater.inflate(R.layout.layout_recall_scene_bottom_sheet, container, false);

        final SwitchMaterial transitionDelay = nodeControlsContainer.findViewById(R.id.switch_transition_delay);

        final TextView time = nodeControlsContainer.findViewById(R.id.transition_time);
        final Slider transitionTimeSlider = nodeControlsContainer.findViewById(R.id.transition_slider);
        transitionTimeSlider.setValueFrom(0);
        transitionTimeSlider.setValueTo(230);
        transitionTimeSlider.setStepSize(1);
        transitionTimeSlider.setValue(0);

        final Slider delaySlider = nodeControlsContainer.findViewById(R.id.delay_slider);
        delaySlider.setValueFrom(0);
        delaySlider.setValueTo(255);
        delaySlider.setValue(0);
        final TextView delayTime = nodeControlsContainer.findViewById(R.id.delay_time);

        final Button actionRecallScene = nodeControlsContainer.findViewById(R.id.action_recall);
        actionRecallScene.setOnClickListener(v -> {
            try {
                if (transitionDelay.isChecked()) {
                    ((SceneRecallListener) requireActivity()).recallScene(mScene, mTransitionSteps, mTransitionStepResolution, (int) delaySlider.getValue());
                } else {
                    ((SceneRecallListener) requireActivity()).recallScene(mScene);
                }
                dismiss();
            } catch (IllegalArgumentException ex) {
                Toast.makeText(requireContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

        delaySlider.addOnChangeListener((slider, value, fromUser) ->
                delayTime.setText(getString(R.string.transition_time_interval, String.valueOf((int) value * MeshParserUtils.GENERIC_ON_OFF_5_MS), "ms")));
        return nodeControlsContainer;
    }
}
