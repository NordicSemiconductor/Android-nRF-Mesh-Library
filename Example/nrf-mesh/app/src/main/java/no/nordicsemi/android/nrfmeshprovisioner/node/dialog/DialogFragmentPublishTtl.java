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

package no.nordicsemi.android.nrfmeshprovisioner.node.dialog;

import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class DialogFragmentPublishTtl extends DialogFragment {

    private static final String PUBLISH_TTL = "PUBLISH_TTL";
    //UI Bindings
    @BindView(R.id.chk_default_ttl)
    CheckBox chkPublishTtl;
    @BindView(R.id.text_input_layout)
    TextInputLayout ttlInputLayout;
    @BindView(R.id.text_input)
    TextInputEditText ttlInput;

    private int mPulishTtl;

    public static DialogFragmentPublishTtl newInstance(final int ttl) {
        DialogFragmentPublishTtl fragmentNetworkKey = new DialogFragmentPublishTtl();
        final Bundle args = new Bundle();
        args.putInt(PUBLISH_TTL, ttl);
        fragmentNetworkKey.setArguments(args);
        return fragmentNetworkKey;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPulishTtl = getArguments().getInt(PUBLISH_TTL);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_publish_ttl_input, null);
        ButterKnife.bind(this, rootView);

        chkPublishTtl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ttlInputLayout.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            if(isChecked){
                ttlInputLayout.setError(null);
            }
        });

        ttlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    ttlInputLayout.setError(getString(R.string.error_empty_ttl));
                } else {
                    ttlInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext()).setView(rootView)
                .setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null);

        alertDialogBuilder.setIcon(R.drawable.ic_timer);
        alertDialogBuilder.setTitle(R.string.title_publish_ttl);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            if(chkPublishTtl.isChecked()) {
                ((DialogFragmentPublishTtlListener) requireActivity()).setPublishTtl(MeshParserUtils.USE_DEFAULT_TTL);
                dismiss();
            } else {
                final String publishTtl = ttlInput.getEditableText().toString().trim();
                if (validateInput(publishTtl)) {
                    ((DialogFragmentPublishTtlListener) requireActivity()).setPublishTtl(Integer.parseInt(publishTtl));
                    dismiss();
                }
            }
        });

        if(savedInstanceState == null) {

            //Update ui
            if(MeshParserUtils.isDefaultPublishTtl(mPulishTtl)){
                chkPublishTtl.setChecked(true);
            } else {
                final String ttl = String.valueOf(mPulishTtl);
                ttlInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                ttlInput.setText(ttl);
                ttlInput.setSelection(ttl.length());
            }
            ttlInputLayout.setHint(getString(R.string.hint_publish_ttl));
        }

        return alertDialog;
    }

    private boolean validateInput(final String input) {
        try {
            if(TextUtils.isEmpty(input)){
                ttlInputLayout.setError(getString(R.string.error_empty_ttl));
                return false;
            }
            if(!MeshParserUtils.isValidTtl(Integer.parseInt(input))) {
                ttlInputLayout.setError(getString(R.string.error_invalid_publish_ttl));
                return false;
            }
        } catch(NumberFormatException ex) {
            ttlInputLayout.setError(getString(R.string.error_invalid_publish_ttl));
            return false;
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public interface DialogFragmentPublishTtlListener {

        void setPublishTtl(final int ttl);

    }
}
