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

package no.nordicsemi.android.nrfmeshprovisioner.provisioners.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.AllocatedSceneRange;
import no.nordicsemi.android.meshprovisioner.Scene;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.nrfmeshprovisioner.R;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.RangeListener;
import no.nordicsemi.android.nrfmeshprovisioner.utils.HexKeyListener;

public class DialogFragmentSceneRange extends DialogFragment {

    private static final String RANGE = "RANGE";
    //UI Bindings
    @BindView(R.id.low_address_layout)
    TextInputLayout fistSceneInputLayout;
    @BindView(R.id.low_address_input)
    TextInputEditText firstSceneInput;
    @BindView(R.id.high_address_layout)
    TextInputLayout lastSceneInputLayout;
    @BindView(R.id.high_address_input)
    TextInputEditText lastSceneInput;

    private AllocatedSceneRange mRange;

    public static DialogFragmentSceneRange newInstance(@Nullable final AllocatedSceneRange range) {
        DialogFragmentSceneRange fragment = new DialogFragmentSceneRange();
        final Bundle args = new Bundle();
        args.putParcelable(RANGE, range);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRange = getArguments().getParcelable(RANGE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_range, null);

        //Bind ui
        ButterKnife.bind(this, rootView);
        final TextView summary = rootView.findViewById(R.id.summary);
        if (mRange != null) {
            final String lowAddress = MeshAddress.formatAddress(mRange.getFirstScene(), false);
            final String highAddress = MeshAddress.formatAddress(mRange.getLastScene(), false);
            firstSceneInput.setText(lowAddress);
            firstSceneInput.setSelection(lowAddress.length());
            lastSceneInput.setText(highAddress);
            lastSceneInput.setSelection(highAddress.length());
        }

        final KeyListener hexKeyListener = new HexKeyListener();
        fistSceneInputLayout.setHint(getString(R.string.first_scene));
        firstSceneInput.setKeyListener(hexKeyListener);
        firstSceneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    fistSceneInputLayout.setError(getString(R.string.error_empty_value));
                } else {
                    fistSceneInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        lastSceneInputLayout.setHint(getString(R.string.last_scene));
        lastSceneInput.setKeyListener(hexKeyListener);
        lastSceneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    lastSceneInputLayout.setError(getString(R.string.error_empty_value));
                } else {
                    lastSceneInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext())
                .setView(rootView)
                .setIcon(R.drawable.ic_arrow_collapse_black_alpha_24dp)
                .setTitle(R.string.title_range)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null);

        summary.setText(R.string.scene_range_summary);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String low = firstSceneInput.getEditableText().toString().trim();
            final String high = lastSceneInput.getEditableText().toString().trim();
            if (validateFirstScene(low) && validateLastScene(high)) {
                try {
                    AllocatedSceneRange range = mRange;
                    if (range == null) {
                        range = new AllocatedSceneRange(Integer.parseInt(low, 16), Integer.parseInt(high, 16));
                    } else {
                        range.setFirstScene(Integer.parseInt(low, 16));
                        range.setLastScene(Integer.parseInt(high, 16));
                    }
                    ((RangeListener) requireActivity()).addRange(range);
                    dismiss();
                } catch (IllegalArgumentException ex) {
                    fistSceneInputLayout.setError(ex.getMessage());
                }
            }
        });

        return alertDialog;
    }

    private boolean validateFirstScene(final String firstScene) {
        try {

            final int address = Integer.parseInt(firstScene, 16);
            if (!Scene.isValidScene(address)) {
                fistSceneInputLayout.setError("first scene value must range from 0x0001 - 0xFFFF");
                return false;
            }
        } catch (IllegalArgumentException ex) {
            fistSceneInputLayout.setError(ex.getMessage());
            return false;
        }
        return true;
    }

    private boolean validateLastScene(final String lastScene) {
        try {

            final int address = Integer.parseInt(lastScene, 16);
            if (!Scene.isValidScene(address)) {
                lastSceneInputLayout.setError("last scene value must range from 0x0001 - 0xFFFF");
                return false;
            }
        } catch (IllegalArgumentException ex) {
            lastSceneInputLayout.setError(ex.getMessage());
            return false;
        }
        return true;
    }
}
