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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import no.nordicsemi.android.mesh.transport.ConfigProxySet;
import no.nordicsemi.android.nrfmesh.R;

public class DialogFragmentProxySet extends DialogFragmentMessage {

    private static final String SET_PROXY_STATE = "SET_PROXY_STATE";
    private boolean mState = true; //Let's set true by default so that we don't break anyone's node

    public interface DialogFragmentProxySetListener {
        void onProxySet(@ConfigProxySet.ProxyState final int state);
    }

    public static DialogFragmentProxySet newInstance(final String title, final String message, final boolean state) {
        final DialogFragmentProxySet fragment = new DialogFragmentProxySet();
        final Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        args.putBoolean(SET_PROXY_STATE, state);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mState = getArguments().getBoolean(SET_PROXY_STATE, true);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        alertDialogBuilder = new AlertDialog.Builder(requireActivity());
        alertDialogBuilder.setIcon(R.drawable.ic_reset);
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setNegativeButton(getString(R.string.no), null);
        alertDialogBuilder.setPositiveButton(getString(R.string.yes), (dialog, which) -> (
                (DialogFragmentProxySetListener) requireActivity()).
                onProxySet(mState ? ConfigProxySet.PROXY_FEATURE_ENABLED : ConfigProxySet.PROXY_FEATURE_DISABLED));

        return super.onCreateDialog(savedInstanceState);
    }
}
