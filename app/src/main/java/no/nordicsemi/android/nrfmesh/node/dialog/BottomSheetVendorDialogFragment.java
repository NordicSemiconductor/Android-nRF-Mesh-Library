package no.nordicsemi.android.nrfmesh.node.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutVendorModelBottomSheetBinding;
import no.nordicsemi.android.nrfmesh.utils.HexKeyListener;
import no.nordicsemi.android.nrfmesh.utils.Utils;

public class BottomSheetVendorDialogFragment extends BottomSheetDialogFragment {
    private static final String KEY_INDEX = "KEY_INDEX";
    private static final String MODEL_ID = "MODEL_ID";

    private LayoutVendorModelBottomSheetBinding binding;
    private int mModelId;
    private int mKeyIndex;

    public interface BottomSheetVendorModelControlsListener {
        void sendVendorModelMessage(final int modelId, final int keyIndex, final int opCode, final byte[] parameters, final boolean acknowledged);
    }

    public static BottomSheetVendorDialogFragment getInstance(final int modelId, final int appKeyIndex) {
        final BottomSheetVendorDialogFragment fragment = new BottomSheetVendorDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(MODEL_ID, modelId);
        args.putInt(KEY_INDEX, appKeyIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mModelId = getArguments().getInt(MODEL_ID);
            mKeyIndex = getArguments().getInt(KEY_INDEX);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        binding = LayoutVendorModelBottomSheetBinding.inflate(getLayoutInflater());
        final KeyListener hexKeyListener = new HexKeyListener();

        binding.opCode.setKeyListener(hexKeyListener);
        binding.opCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                binding.opCodeLayout.setError(null);
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        binding.parameters.setKeyListener(hexKeyListener);
        binding.parameters.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                binding.parametersLayout.setError(null);
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        binding.actionSend.setOnClickListener(v -> {
            binding.receivedMessageContainer.setVisibility(View.GONE);
            binding.receivedMessage.setText("");
            final String opCode = binding.opCode.getEditableText().toString().trim();
            final String parameters = binding.parameters.getEditableText().toString().trim();

            if (!validateOpcode(opCode, binding.opCodeLayout))
                return;

            if (!validateParameters(parameters, binding.parametersLayout))
                return;

            final byte[] params;
            if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                params = null;
            } else {
                params = MeshParserUtils.toByteArray(parameters);
            }

            ((BottomSheetVendorModelControlsListener) requireActivity())
                    .sendVendorModelMessage(mModelId, mKeyIndex, Integer.parseInt(opCode, 16), params, binding.chkAcknowledged.isChecked());
        });

        return binding.getRoot();
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

    public void setReceivedMessage(final byte[] accessPayload) {
        binding.receivedMessageContainer.setVisibility(View.VISIBLE);
        binding.receivedMessage.setText(MeshParserUtils.bytesToHex(accessPayload, false));
    }
}
