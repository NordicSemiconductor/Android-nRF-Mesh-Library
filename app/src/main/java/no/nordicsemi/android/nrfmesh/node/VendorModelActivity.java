package no.nordicsemi.android.nrfmesh.node;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.models.VendorModel;
import no.nordicsemi.android.mesh.transport.ConfigVendorModelAppList;
import no.nordicsemi.android.mesh.transport.ConfigVendorModelSubscriptionList;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.VendorModelMessageAcked;
import no.nordicsemi.android.mesh.transport.VendorModelMessageStatus;
import no.nordicsemi.android.mesh.transport.VendorModelMessageUnacked;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutVendorModelControlsBinding;
import no.nordicsemi.android.nrfmesh.utils.HexKeyListener;
import no.nordicsemi.android.nrfmesh.utils.Utils;


@AndroidEntryPoint
public class VendorModelActivity extends ModelConfigurationActivity {

    private LayoutVendorModelControlsBinding layoutVendorModelControlsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        if (model instanceof VendorModel) {
            layoutVendorModelControlsBinding =
                    LayoutVendorModelControlsBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);
            final KeyListener hexKeyListener = new HexKeyListener();
            layoutVendorModelControlsBinding.opCode.setKeyListener(hexKeyListener);
            layoutVendorModelControlsBinding.opCode.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

                }

                @Override
                public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                    layoutVendorModelControlsBinding.opCodeLayout.setError(null);
                }

                @Override
                public void afterTextChanged(final Editable s) {

                }
            });

            layoutVendorModelControlsBinding.parameters.setKeyListener(hexKeyListener);
            layoutVendorModelControlsBinding.parameters.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

                }

                @Override
                public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                    layoutVendorModelControlsBinding.parametersLayout.setError(null);
                }

                @Override
                public void afterTextChanged(final Editable s) {

                }
            });

            layoutVendorModelControlsBinding.actionSend.setOnClickListener(v -> {
                layoutVendorModelControlsBinding.receivedMessageContainer.setVisibility(View.GONE);
                layoutVendorModelControlsBinding.receivedMessage.setText("");
                final String opCode = layoutVendorModelControlsBinding.opCode.getEditableText().toString().trim();
                final String parameters = layoutVendorModelControlsBinding.parameters.getEditableText().toString().trim();

                if (!validateOpcode(opCode, layoutVendorModelControlsBinding.opCodeLayout))
                    return;

                if (!validateParameters(parameters, layoutVendorModelControlsBinding.parametersLayout))
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

                sendVendorModelMessage(Integer.parseInt(opCode, 16), params, layoutVendorModelControlsBinding.chkAcknowledged.isChecked());
            });

            mViewModel.getSelectedModel().observe(this, meshModel -> {
                if (meshModel != null) {
                    updateAppStatusUi(meshModel);
                    updatePublicationUi(meshModel);
                    updateSubscriptionUi(meshModel);
                }
            });
        }
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        if (meshMessage instanceof VendorModelMessageStatus) {
            final VendorModelMessageStatus status = (VendorModelMessageStatus) meshMessage;
            layoutVendorModelControlsBinding.receivedMessageContainer.setVisibility(View.VISIBLE);
            layoutVendorModelControlsBinding.receivedMessage.setText(MeshParserUtils.bytesToHex(status.getAccessPayload(), false));
        } else if (meshMessage instanceof ConfigVendorModelAppList) {
            final ConfigVendorModelAppList status = (ConfigVendorModelAppList) meshMessage;
            mViewModel.removeMessage();
            if (status.isSuccessful()) {
                if (handleStatuses()) return;
            } else {
                displayStatusDialogFragment(getString(R.string.title_vendor_model_app_list), status.getStatusCodeName());
            }
        } else if (meshMessage instanceof ConfigVendorModelSubscriptionList) {
            final ConfigVendorModelSubscriptionList status = (ConfigVendorModelSubscriptionList) meshMessage;
            mViewModel.removeMessage();
            if (status.isSuccessful()) {
                if (handleStatuses()) return;
            } else {
                displayStatusDialogFragment(getString(R.string.title_vendor_model_subscription_list), status.getStatusCodeName());
            }
        }
        hideProgressBar();
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
                if (acknowledged) {
                    sendMessage(element.getElementAddress(),
                            new VendorModelMessageAcked(appKey, model.getModelId(), model.getCompanyIdentifier(), opcode, parameters));
                } else {
                    sendMessage(element.getElementAddress(),
                            new VendorModelMessageUnacked(appKey, model.getModelId(), model.getCompanyIdentifier(), opcode, parameters));
                }
            }
        }
    }
}
