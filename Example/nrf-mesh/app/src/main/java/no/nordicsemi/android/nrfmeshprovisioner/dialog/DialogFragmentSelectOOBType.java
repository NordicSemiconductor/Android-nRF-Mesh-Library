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

package no.nordicsemi.android.nrfmeshprovisioner.dialog;

import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningCapabilities;
import no.nordicsemi.android.meshprovisioner.utils.AuthenticationOOBMethods;
import no.nordicsemi.android.meshprovisioner.utils.InputOOBAction;
import no.nordicsemi.android.meshprovisioner.utils.OutputOOBAction;
import no.nordicsemi.android.meshprovisioner.utils.StaticOOBType;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.AuthenticationOOBMethodsAdapter;

public class DialogFragmentSelectOOBType extends DialogFragment {

    private static final String CAPABILITIES = "CAPABILITIES";
    //UI Bindings
    @BindView(R.id.oob_types)
    Spinner oobTypesSpinner;
    @BindView(R.id.output_oob_container)
    LinearLayout containerOutputOOB;
    @BindView(R.id.input_oob_container)
    LinearLayout containerInputOOB;
    @BindView(R.id.radio_group_output_oob)
    RadioGroup rgOutputOob;
    @BindView(R.id.radio_blink)
    RadioButton rbBlink;
    @BindView(R.id.radio_beep)
    RadioButton rbBeep;
    @BindView(R.id.radio_vibrate)
    RadioButton rbVibrate;
    @BindView(R.id.radio_output_numeric)
    RadioButton rbOutputNumeric;
    @BindView(R.id.radio_output_alpha_numeric)
    RadioButton rbOuputAlphaNumeric;
    @BindView(R.id.radio_group_input_oob)
    RadioGroup rgInputOob;
    @BindView(R.id.radio_push)
    RadioButton rbPush;
    @BindView(R.id.radio_twist)
    RadioButton rbTwist;
    @BindView(R.id.radio_input_numeric)
    RadioButton rbInputNumeric;
    @BindView(R.id.radio_input_alpha_numeric)
    RadioButton rbInputAlphaNumeric;

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
        @SuppressLint("InflateParams") final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_select_oob_type, null);

        //Bind ui
        ButterKnife.bind(this, rootView);
        final List<AuthenticationOOBMethods> availableOOBTypes = capabilities.getAvailableOOBTypes();
        authenticationOobMethodsAdapter = new AuthenticationOOBMethodsAdapter(requireContext(), availableOOBTypes);
        oobTypesSpinner.setAdapter(authenticationOobMethodsAdapter);

        oobTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                updateOOBUI(position);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {

            }
        });

        rgOutputOob.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_blink:
                    break;
                case R.id.radio_beep:
                    break;
                case R.id.radio_vibrate:
                    break;
                case R.id.radio_output_numeric:
                    break;
                case R.id.radio_output_alpha_numeric:
                    break;
            }
        });

        rgInputOob.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_push:
                    break;
                case R.id.radio_twist:
                    break;
                case R.id.radio_input_numeric:
                    break;
                case R.id.radio_input_alpha_numeric:
                    break;
            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext()).
                setView(rootView).
                setIcon(R.drawable.ic_oob_lock_outline).
                setTitle(R.string.title_select_oob).
                setPositiveButton(R.string.ok, (dialog, which) -> {
                    final AuthenticationOOBMethods type = (AuthenticationOOBMethods) oobTypesSpinner.getSelectedItem();
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

    private void updateOOBUI(final int position) {
        final AuthenticationOOBMethods oobType = authenticationOobMethodsAdapter.getItem(position);
        switch (oobType) {
            case NO_OOB_AUTHENTICATION:
                containerOutputOOB.setVisibility(View.GONE);
                containerInputOOB.setVisibility(View.GONE);
                break;
            case STATIC_OOB_AUTHENTICATION:
                containerOutputOOB.setVisibility(View.GONE);
                containerInputOOB.setVisibility(View.GONE);
                break;
            case OUTPUT_OOB_AUTHENTICATION:
                final List<OutputOOBAction> outputOOBActions = capabilities.getSupportedOutputOOBActions();
                containerOutputOOB.setVisibility(View.VISIBLE);
                containerInputOOB.setVisibility(View.GONE);
                for(OutputOOBAction outputOOBAction : outputOOBActions) {
                    if(outputOOBAction.getOutputOOBAction() == OutputOOBAction.BLINK.getOutputOOBAction()){
                        rbBlink.setEnabled(true);
                        rbBlink.setChecked(outputOOBActions.size() == 1);
                    } else if(outputOOBAction.getOutputOOBAction() == OutputOOBAction.BEEP.getOutputOOBAction()){
                        rbBeep.setEnabled(true);
                        rbBeep.setChecked(outputOOBActions.size() == 1);
                    } else if(outputOOBAction.getOutputOOBAction() == OutputOOBAction.VIBRATE.getOutputOOBAction()){
                        rbVibrate.setEnabled(true);
                        rbVibrate.setChecked(outputOOBActions.size() == 1);
                    } else if(outputOOBAction.getOutputOOBAction() == OutputOOBAction.OUTPUT_NUMERIC.getOutputOOBAction()){
                        rbOutputNumeric.setEnabled(true);
                        rbOutputNumeric.setChecked(outputOOBActions.size() == 1);
                    } else if(outputOOBAction.getOutputOOBAction() == OutputOOBAction.OUTPUT_ALPHA_NUMERIC.getOutputOOBAction()){
                        rbOuputAlphaNumeric.setEnabled(true);
                        rbOuputAlphaNumeric.setChecked(outputOOBActions.size() == 1);
                    }
                }
                break;
            case INPUT_OOB_AUTHENTICATION:
                containerOutputOOB.setVisibility(View.GONE);
                containerInputOOB.setVisibility(View.VISIBLE);
                final List<InputOOBAction> inputOOBActions = capabilities.getSupportedInputOOBActions();
                for(InputOOBAction inputOOBAction : inputOOBActions) {
                    if(inputOOBAction.getInputOOBAction() == InputOOBAction.PUSH.getInputOOBAction()){
                        rbPush.setEnabled(true);
                        rbPush.setChecked(inputOOBActions.size() == 1);
                    } else if(inputOOBAction.getInputOOBAction() == InputOOBAction.TWIST.getInputOOBAction()){
                        rbTwist.setEnabled(true);
                        rbTwist.setChecked(inputOOBActions.size() == 1);
                    } else if(inputOOBAction.getInputOOBAction() == InputOOBAction.INPUT_NUMERIC.getInputOOBAction()){
                        rbInputNumeric.setEnabled(true);
                        rbInputNumeric.setChecked(inputOOBActions.size() == 1);
                    } else if(inputOOBAction.getInputOOBAction() == InputOOBAction.INPUT_ALPHA_NUMERIC.getInputOOBAction()){
                        rbInputAlphaNumeric.setEnabled(true);
                        rbInputAlphaNumeric.setChecked(inputOOBActions.size() == 1);
                    }
                }
                break;
        }
    }

    private OutputOOBAction getSelectedOutputOOBType() {
        final int id = rgOutputOob.getCheckedRadioButtonId();
        switch (id) {
            case R.id.radio_blink:
                return OutputOOBAction.BLINK;
            case R.id.radio_beep:
                return OutputOOBAction.BEEP;
            case R.id.radio_vibrate:
                return OutputOOBAction.VIBRATE;
            case R.id.radio_output_numeric:
                return OutputOOBAction.OUTPUT_NUMERIC;
            case R.id.radio_output_alpha_numeric:
                return OutputOOBAction.OUTPUT_ALPHA_NUMERIC;
        }
        return null;
    }

    private InputOOBAction getSelectedInputOOBType() {
        final int id = rgInputOob.getCheckedRadioButtonId();
        switch (id) {
            case R.id.radio_push:
                return InputOOBAction.PUSH;
            case R.id.radio_twist:
                return InputOOBAction.TWIST;
            case R.id.radio_input_numeric:
                return InputOOBAction.INPUT_NUMERIC;
            case R.id.radio_input_alpha_numeric:
                return InputOOBAction.INPUT_ALPHA_NUMERIC;
        }
        return null;
    }
}
