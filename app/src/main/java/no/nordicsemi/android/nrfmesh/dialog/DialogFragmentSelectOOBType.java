/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrfmesh.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import no.nordicsemi.android.mesh.provisionerstates.ProvisioningCapabilities;
import no.nordicsemi.android.mesh.utils.AuthenticationOOBMethods;
import no.nordicsemi.android.mesh.utils.InputOOBAction;
import no.nordicsemi.android.mesh.utils.OutputOOBAction;
import no.nordicsemi.android.mesh.utils.StaticOOBType;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.adapter.AuthenticationOOBMethodsAdapter;
import no.nordicsemi.android.nrfmesh.databinding.DialogFragmentSelectOobTypeBinding;

public class DialogFragmentSelectOOBType extends DialogFragment {

    private static final String CAPABILITIES = "CAPABILITIES";
    private DialogFragmentSelectOobTypeBinding binding;

    private ProvisioningCapabilities capabilities;
    private AuthenticationOOBMethodsAdapter authenticationOobMethodsAdapter;

    public interface DialogFragmentSelectOOBTypeListener {

        void onNoOOBSelected();

        void onStaticOOBSelected(final StaticOOBType staticOOBType);

        void onOutputOOBActionSelected(final OutputOOBAction outputOOBType);

        void onInputOOBActionSelected(final InputOOBAction inputOOBType);
    }

    public static DialogFragmentSelectOOBType newInstance(final ProvisioningCapabilities capabilities) {
        final DialogFragmentSelectOOBType fragment = new DialogFragmentSelectOOBType();
        final Bundle args = new Bundle();
        args.putParcelable(CAPABILITIES, capabilities);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            capabilities = getArguments().getParcelable(CAPABILITIES);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        binding = DialogFragmentSelectOobTypeBinding.inflate(getLayoutInflater());

        final List<AuthenticationOOBMethods> availableOOBTypes = capabilities.getAvailableOOBTypes();
        authenticationOobMethodsAdapter = new AuthenticationOOBMethodsAdapter(requireContext(), availableOOBTypes);
        binding.oobTypes.setAdapter(authenticationOobMethodsAdapter);

        binding.oobTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                updateOOBUI(position);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext()).
                setView(binding.getRoot()).
                setIcon(R.drawable.ic_oob_lock_outline).
                setTitle(R.string.title_select_oob).
                setPositiveButton(R.string.ok, (dialog, which) -> {
                    final AuthenticationOOBMethods type = (AuthenticationOOBMethods) binding.oobTypes.getSelectedItem();
                    switch (type) {
                        case NO_OOB_AUTHENTICATION:
                            ((DialogFragmentSelectOOBTypeListener) requireContext()).onNoOOBSelected();
                            break;
                        case STATIC_OOB_AUTHENTICATION:
                            ((DialogFragmentSelectOOBTypeListener) requireContext()).onStaticOOBSelected(StaticOOBType.STATIC_OOB_AVAILABLE);
                            break;
                        case OUTPUT_OOB_AUTHENTICATION:
                            ((DialogFragmentSelectOOBTypeListener) requireContext()).onOutputOOBActionSelected(getSelectedOutputOOBType());
                            break;
                        case INPUT_OOB_AUTHENTICATION:
                            ((DialogFragmentSelectOOBTypeListener) requireContext()).onInputOOBActionSelected(getSelectedInputOOBType());
                            break;
                    }
                }).
                setNegativeButton(R.string.cancel, null);


        return alertDialogBuilder.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateOOBUI(final int position) {
        final AuthenticationOOBMethods oobType = authenticationOobMethodsAdapter.getItem(position);
        switch (oobType) {
            case NO_OOB_AUTHENTICATION:
                binding.outputOobContainer.setVisibility(View.GONE);
                binding.inputOobContainer.setVisibility(View.GONE);
                break;
            case OUTPUT_OOB_AUTHENTICATION:
                final List<OutputOOBAction> outputOOBActions = capabilities.getSupportedOutputOOBActions();
                binding.outputOobContainer.setVisibility(View.VISIBLE);
                binding.inputOobContainer.setVisibility(View.GONE);
                for (OutputOOBAction outputOOBAction : outputOOBActions) {
                    if (outputOOBAction.getOutputOOBAction() == OutputOOBAction.BLINK.getOutputOOBAction()) {
                        binding.radioBlink.setEnabled(true);
                        binding.radioBlink.setChecked(outputOOBActions.size() == 1);
                    } else if (outputOOBAction.getOutputOOBAction() == OutputOOBAction.BEEP.getOutputOOBAction()) {
                        binding.radioBeep.setEnabled(true);
                        binding.radioBeep.setChecked(outputOOBActions.size() == 1);
                    } else if (outputOOBAction.getOutputOOBAction() == OutputOOBAction.VIBRATE.getOutputOOBAction()) {
                        binding.radioVibrate.setEnabled(true);
                        binding.radioVibrate.setChecked(outputOOBActions.size() == 1);
                    } else if (outputOOBAction.getOutputOOBAction() == OutputOOBAction.OUTPUT_NUMERIC.getOutputOOBAction()) {
                        binding.radioOutputNumeric.setEnabled(true);
                        binding.radioOutputNumeric.setChecked(outputOOBActions.size() == 1);
                    } else if (outputOOBAction.getOutputOOBAction() == OutputOOBAction.OUTPUT_ALPHA_NUMERIC.getOutputOOBAction()) {
                        binding.radioOutputAlphaNumeric.setEnabled(true);
                        binding.radioOutputAlphaNumeric.setChecked(outputOOBActions.size() == 1);
                    }
                }
                break;
            case INPUT_OOB_AUTHENTICATION:
                binding.outputOobContainer.setVisibility(View.GONE);
                binding.inputOobContainer.setVisibility(View.VISIBLE);
                final List<InputOOBAction> inputOOBActions = capabilities.getSupportedInputOOBActions();
                for (InputOOBAction inputOOBAction : inputOOBActions) {
                    if (inputOOBAction.getInputOOBAction() == InputOOBAction.PUSH.getInputOOBAction()) {
                        binding.radioPush.setEnabled(true);
                        binding.radioPush.setChecked(inputOOBActions.size() == 1);
                    } else if (inputOOBAction.getInputOOBAction() == InputOOBAction.TWIST.getInputOOBAction()) {
                        binding.radioTwist.setEnabled(true);
                        binding.radioTwist.setChecked(inputOOBActions.size() == 1);
                    } else if (inputOOBAction.getInputOOBAction() == InputOOBAction.INPUT_NUMERIC.getInputOOBAction()) {
                        binding.radioInputNumeric.setEnabled(true);
                        binding.radioInputNumeric.setChecked(inputOOBActions.size() == 1);
                    } else if (inputOOBAction.getInputOOBAction() == InputOOBAction.INPUT_ALPHA_NUMERIC.getInputOOBAction()) {
                        binding.radioInputAlphaNumeric.setEnabled(true);
                        binding.radioInputAlphaNumeric.setChecked(inputOOBActions.size() == 1);
                    }
                }
                break;
        }
    }

    private OutputOOBAction getSelectedOutputOOBType() {
        final int id = binding.radioGroupOutputOob.getCheckedRadioButtonId();
        if (id == R.id.radio_blink) {
            return OutputOOBAction.BLINK;
        } else if (id == R.id.radio_beep) {
            return OutputOOBAction.BEEP;
        } else if (id == R.id.radio_vibrate) {
            return OutputOOBAction.VIBRATE;
        } else if (id == R.id.radio_output_numeric) {
            return OutputOOBAction.OUTPUT_NUMERIC;
        } else if (id == R.id.radio_output_alpha_numeric) {
            return OutputOOBAction.OUTPUT_ALPHA_NUMERIC;
        }
        return null;
    }

    private InputOOBAction getSelectedInputOOBType() {
        final int id = binding.radioGroupInputOob.getCheckedRadioButtonId();
        if (id == R.id.radio_push) {
            return InputOOBAction.PUSH;
        } else if (id == R.id.radio_twist) {
            return InputOOBAction.TWIST;
        } else if (id == R.id.radio_input_numeric) {
            return InputOOBAction.INPUT_NUMERIC;
        } else if (id == R.id.radio_input_alpha_numeric) {
            return InputOOBAction.INPUT_ALPHA_NUMERIC;
        }
        return null;
    }
}
