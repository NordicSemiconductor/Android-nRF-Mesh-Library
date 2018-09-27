package no.nordicsemi.android.nrfmeshprovisioner;

import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.utils.HexKeyListener;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;

public class VendorModelActivity extends BaseModelConfigurationActivity {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Override
    protected final void addControlsUi(final MeshModel model) {
        if (model instanceof VendorModel) {
            final CardView cardView = findViewById(R.id.node_controls_card);
            final View nodeControlsContainer = LayoutInflater.from(this).inflate(R.layout.layout_vendor_model_controls, cardView);

            final CheckBox chkAcknowledged = nodeControlsContainer.findViewById(R.id.chk_acknowledged);
            final TextInputLayout opCodeLayout = nodeControlsContainer.findViewById(R.id.op_code_layout);
            final TextInputEditText opCodeEditText = nodeControlsContainer.findViewById(R.id.op_code);

            final KeyListener hexKeyListener = new HexKeyListener();

            final TextInputLayout parametersLayout = nodeControlsContainer.findViewById(R.id.parameters_layout);
            final TextInputEditText parametersEditText = nodeControlsContainer.findViewById(R.id.parameters);
            final View messageContainer = nodeControlsContainer.findViewById(R.id.received_message_container);
            final TextView receivedMessage = nodeControlsContainer.findViewById(R.id.received_message);
            final Button actionSend = nodeControlsContainer.findViewById(R.id.action_send);

            opCodeEditText.setKeyListener(hexKeyListener);
            opCodeEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

                }

                @Override
                public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                    opCodeLayout.setError(null);
                }

                @Override
                public void afterTextChanged(final Editable s) {

                }
            });

            parametersEditText.setKeyListener(hexKeyListener);
            parametersEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

                }

                @Override
                public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                    parametersLayout.setError(null);
                }

                @Override
                public void afterTextChanged(final Editable s) {

                }
            });

            actionSend.setOnClickListener(v -> {
                messageContainer.setVisibility(View.GONE);
                final String opCode = opCodeEditText.getText().toString().trim();
                final String parameters = parametersEditText.getText().toString().trim();
                /*
                try {
                    if (!validateOpcode(opCode, opCodeLayout)) {
                        return;
                    }
                } catch (Exception ex) {
                    opCodeLayout.setError(ex.getMessage());
                    return;
                }

                final byte[] params;
                if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                    params = null;
                } else {
                    try {
                        if (!validateOpcode(parameters)) {
                            return;
                        }
                    } catch (Exception ex) {
                        opCodeLayout.setError(ex.getMessage());
                        return;
                    }
                    params = MeshParserUtils.toByteArray(parameters);
                }*/

                if(!validateOpcode(opCode, opCodeLayout))
                    return;

                if(!validateParameters(parameters, parametersLayout))
                    return;

                if (model.getBoundAppKeyIndexes().isEmpty()) {
                    Toast.makeText(this, R.string.no_app_keys_bound, Toast.LENGTH_LONG).show();
                    return;
                }

                final byte[] params;
                if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                    params = null;
                } else {
                    params = MeshParserUtils.toByteArray(parameters);
                }

                final ProvisionedMeshNode node = (ProvisionedMeshNode) mViewModel.getExtendedMeshNode().getMeshNode();
                if (chkAcknowledged.isChecked()) {
                    mViewModel.sendVendorModelAcknowledgedMessage(node, model, model.getBoundAppKeyIndexes().get(0), Integer.parseInt(opCode, 16), params);
                } else {
                    mViewModel.sendVendorModelUnacknowledgedMessage(node, model, model.getBoundAppKeyIndexes().get(0), Integer.parseInt(opCode, 16), params);
                }
            });

            mViewModel.getVendorModelState().observe(this, bytes -> {
                messageContainer.setVisibility(View.VISIBLE);
                receivedMessage.setText(MeshParserUtils.bytesToHex(bytes, false));
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private boolean validateOpcode(final String opCode, final TextInputLayout opCodeLayout) {
        try {
            if(TextUtils.isEmpty(opCode)){
                opCodeLayout.setError(getString(R.string.error_empty_value));
                return false;
            }

            if(opCode.length() % 2 != 0 || !opCode.matches(Utils.HEX_PATTERN)) {
                opCodeLayout.setError(getString(R.string.invalid_hex_value));
                return false;
            }
            if(MeshParserUtils.isValidOpcode(Integer.valueOf(opCode, 16))) {
                return true;
            }
        }  catch (NumberFormatException ex) {
            opCodeLayout.setError(getString(R.string.invalid_value));
            return false;
        } catch (IllegalArgumentException ex) {
            opCodeLayout.setError(ex.getMessage());
            return false;
        } catch (Exception ex) {
            opCodeLayout.setError(ex.getMessage());
            return false;
        }
        return true;
    }

    private boolean validateParameters(final String parameters, final TextInputLayout parametersLayout) {
        try {
            if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                return true;
            }

            if(parameters.length() % 2 != 0 || !parameters.matches(Utils.HEX_PATTERN)) {
                parametersLayout.setError(getString(R.string.invalid_hex_value));
                return false;
            }

            if(MeshParserUtils.isValidParameters(MeshParserUtils.toByteArray(parameters))) {
                return true;
            }
        }  catch (NumberFormatException ex) {
            parametersLayout.setError(getString(R.string.invalid_value));
            return false;
        } catch (IllegalArgumentException ex) {
            parametersLayout.setError(ex.getMessage());
            return false;
        } catch (Exception ex) {
            parametersLayout.setError(ex.getMessage());
            return false;
        }
        return true;
    }
}
