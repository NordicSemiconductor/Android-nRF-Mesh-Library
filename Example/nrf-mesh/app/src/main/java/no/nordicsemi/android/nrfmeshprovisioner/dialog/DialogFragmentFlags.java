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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.nrfmeshprovisioner.R;


public class DialogFragmentFlags extends DialogFragment {

    private static final String KEY_REFRESH_FLAG = "KEY_REFRESH_FLAG";
    private static final String IV_UPDATE_FLAG = "IV_UPDATE_FLAG";
    //UI Bindings
    @BindView(R.id.radio_group_refresh_flag)
    RadioGroup radioGroupKeyRefreshFlag;
    @BindView(R.id.radio_group_iv_update_flag)
    RadioGroup radioGroupIVUpdateFlag;
    @BindView(R.id.radio_key_refresh_phase_0)
    RadioButton radioKeyRefreshPhase0;
    @BindView(R.id.radio_key_refresh_phase_2)
    RadioButton radioKeyRefreshPhase2;
    @BindView(R.id.radio_normal_operation)
    RadioButton radioNormalOperation;
    @BindView(R.id.radio_iv_update_active)
    RadioButton radioIvUpdate;

    private int mKeyRefreshFlag;
    private int mIvUpdateFlag;

    public static DialogFragmentFlags newInstance(final int keyRefreshFlag, final int ivUpdateFlag) {
        DialogFragmentFlags fragmentNetworkKey = new DialogFragmentFlags();
        final Bundle args = new Bundle();
        args.putInt(KEY_REFRESH_FLAG, keyRefreshFlag);
        args.putInt(IV_UPDATE_FLAG, ivUpdateFlag);
        fragmentNetworkKey.setArguments(args);
        return fragmentNetworkKey;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mKeyRefreshFlag = getArguments().getInt(KEY_REFRESH_FLAG);
            mIvUpdateFlag = getArguments().getInt(IV_UPDATE_FLAG);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_flags_input, null);

        //Bind ui
        ButterKnife.bind(this, rootView);

        if (mKeyRefreshFlag == 0)
            radioKeyRefreshPhase0.setChecked(true);
        else
            radioKeyRefreshPhase2.setChecked(true);

        if (mIvUpdateFlag == 0)
            radioNormalOperation.setChecked(true);
        else
            radioIvUpdate.setChecked(true);


        radioGroupKeyRefreshFlag.setEnabled(false);
        radioGroupIVUpdateFlag.setEnabled(false);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext()).setView(rootView)
                .setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null);

        alertDialogBuilder.setIcon(R.drawable.ic_flag);
        alertDialogBuilder.setTitle(R.string.title_flags);
        alertDialogBuilder.setMessage(R.string.dialog_summary_flags);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (validateInput()) {
                final int keyRefreshFlag = parseKeyRefreshFlag();
                final int ivUpdateFlag = parseIvUpdateFlag();
                if (getParentFragment() == null) {
                    ((DialogFragmentFlagsListener) getActivity()).onFlagsSelected(keyRefreshFlag, ivUpdateFlag);
                } else {
                    ((DialogFragmentFlagsListener) getParentFragment()).onFlagsSelected(keyRefreshFlag, ivUpdateFlag);
                }
                dismiss();
            }
        });

        return alertDialog;
    }

    private boolean validateInput() {

        int refreshFlagId = radioGroupKeyRefreshFlag.getCheckedRadioButtonId();
        switch (refreshFlagId) {
            case R.id.radio_key_refresh_phase_0:
            case R.id.radio_key_refresh_phase_2:
                break;
            default:
                return false;
        }

        int updateFlagId = radioGroupIVUpdateFlag.getCheckedRadioButtonId();
        switch (updateFlagId) {
            case R.id.radio_normal_operation:
            case R.id.radio_iv_update_active:
                break;
            default:
                return false;
        }
        return true;
    }

    private int parseKeyRefreshFlag() {
        int refreshFlagId = radioGroupKeyRefreshFlag.getCheckedRadioButtonId();
        switch (refreshFlagId) {
            default:
            case R.id.radio_key_refresh_phase_0:
                return 0;
            case R.id.radio_key_refresh_phase_2:
                return 1;
        }
    }

    private int parseIvUpdateFlag() {
        int refreshFlagId = radioGroupIVUpdateFlag.getCheckedRadioButtonId();
        switch (refreshFlagId) {
            default:
            case R.id.radio_normal_operation:
                return 0;
            case R.id.radio_iv_update_active:
                return 1;
        }
    }

    public interface DialogFragmentFlagsListener {

        void onFlagsSelected(final int keyRefreshFlag, final int ivUpdateFlag);

    }
}
