package no.nordicsemi.android.nrfmeshprovisioner.node;

import android.os.Bundle;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.VendorModelMessageAcked;
import no.nordicsemi.android.meshprovisioner.transport.VendorModelMessageStatus;
import no.nordicsemi.android.meshprovisioner.transport.VendorModelMessageUnacked;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmeshprovisioner.utils.HexKeyListener;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;

public class VendorModelActivity extends BaseModelConfigurationActivity {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private View messageContainer;
    private TextView receivedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof VendorModel) {
            final ConstraintLayout container = findViewById(R.id.node_controls_container);
            final View nodeControlsContainer = LayoutInflater.from(this).inflate(R.layout.layout_vendor_model_controls, container);

            final CheckBox chkAcknowledged = nodeControlsContainer.findViewById(R.id.chk_acknowledged);
            final TextInputLayout opCodeLayout = nodeControlsContainer.findViewById(R.id.op_code_layout);
            final TextInputEditText opCodeEditText = nodeControlsContainer.findViewById(R.id.op_code);

            final KeyListener hexKeyListener = new HexKeyListener();

            final TextInputLayout parametersLayout = nodeControlsContainer.findViewById(R.id.parameters_layout);
            final TextInputEditText parametersEditText = nodeControlsContainer.findViewById(R.id.parameters);
            messageContainer = nodeControlsContainer.findViewById(R.id.received_message_container);
            receivedMessage = nodeControlsContainer.findViewById(R.id.received_message);
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
                receivedMessage.setText("");
                final String opCode = opCodeEditText.getEditableText().toString().trim();
                final String parameters = parametersEditText.getEditableText().toString().trim();

                if (!validateOpcode(opCode, opCodeLayout))
                    return;

                if (!validateParameters(parameters, parametersLayout))
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

                sendVendorModelMessage(Integer.parseInt(opCode, 16), params, chkAcknowledged.isChecked());
            });
        }
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        if (meshMessage instanceof VendorModelMessageStatus) {
            final VendorModelMessageStatus status = (VendorModelMessageStatus) meshMessage;
            messageContainer.setVisibility(View.VISIBLE);
            receivedMessage.setText(MeshParserUtils.bytesToHex(status.getAccessPayload(), false));
        }
    }

    /**
     * Validate opcode
     *
     * @param opCode       opcode
     * @param opCodeLayout op c0de view
     * @return true if success or false otherwise
     */
    private boolean validateOpcode(final String opCode, final TextInputLayout opCodeLayout) {
        try {
            if (TextUtils.isEmpty(opCode)) {
                opCodeLayout.setError(getString(R.string.error_empty_value));
                return false;
            }

            if (opCode.length() % 2 != 0 || !opCode.matches(Utils.HEX_PATTERN)) {
                opCodeLayout.setError(getString(R.string.invalid_hex_value));
                return false;
            }
            if (MeshParserUtils.isValidOpcode(Integer.valueOf(opCode, 16))) {
                return true;
            }
        } catch (NumberFormatException ex) {
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

    /**
     * Validate parameters
     *
     * @param parameters       parameters
     * @param parametersLayout parameter view
     * @return true if success or false otherwise
     */
    private boolean validateParameters(final String parameters, final TextInputLayout parametersLayout) {
        try {
            if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                return true;
            }

            if (parameters.length() % 2 != 0 || !parameters.matches(Utils.HEX_PATTERN)) {
                parametersLayout.setError(getString(R.string.invalid_hex_value));
                return false;
            }

            if (MeshParserUtils.isValidParameters(MeshParserUtils.toByteArray(parameters))) {
                return true;
            }
        } catch (NumberFormatException ex) {
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

    /**
     * Send vendor model acknowledged message
     *
     * @param opcode     opcode of the message
     * @param parameters parameters of the message
     */
    public void sendVendorModelMessage(final int opcode, final byte[] parameters, final boolean acknowledged) {
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            final VendorModel model = (VendorModel) mViewModel.getSelectedModel().getValue();
            if (model != null) {
                final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);
                final MeshMessage message;
                if (acknowledged) {
                    message = new VendorModelMessageAcked(appKey, model.getModelId(), model.getCompanyIdentifier(), opcode, parameters);
                    super.sendMessage(element.getElementAddress(), message);
                } else {
                    message = new VendorModelMessageUnacked(appKey, model.getModelId(), model.getCompanyIdentifier(), opcode, parameters);
                    sendMessage(element.getElementAddress(), message);
                }
            }
        }
    }

    @Override
    protected void sendMessage(final int address, @NonNull final MeshMessage meshMessage) {
        try {
            if (!checkConnectivity())
                return;
            mViewModel.getMeshManagerApi().createMeshPdu(address, meshMessage);
        } catch (IllegalArgumentException ex) {
            hideProgressBar();
            final DialogFragmentError message = DialogFragmentError.
                    newInstance(getString(R.string.title_error), ex.getMessage());
            message.show(getSupportFragmentManager(), null);
        }
    }
}
