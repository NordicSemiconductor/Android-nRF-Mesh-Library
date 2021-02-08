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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import no.nordicsemi.android.nrfmesh.R;

public class DialogFragmentPermissionRationale extends DialogFragmentMessage {
    private static final String PERMISSION_DENIED_FOREVER = "PERMISSION_DENIED_FOREVER";
    private boolean isDeniedForever;

    public interface StoragePermissionListener {
        void requestPermission();
    }

    public static DialogFragmentPermissionRationale newInstance(final boolean permissionDeniedForever, final String title, final String message) {
        final Bundle args = new Bundle();
        final DialogFragmentPermissionRationale fragment = new DialogFragmentPermissionRationale();
        args.putBoolean(PERMISSION_DENIED_FOREVER, permissionDeniedForever);
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            isDeniedForever = getArguments().getBoolean(PERMISSION_DENIED_FOREVER);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        alertDialogBuilder = new AlertDialog.Builder(requireActivity());
        alertDialogBuilder.setIcon(R.drawable.ic_info_outline);
        alertDialogBuilder.setPositiveButton(getString(R.string.ok), (dialog, which) -> ((StoragePermissionListener)requireActivity()).requestPermission());
        if(isDeniedForever){
            message = message + getString(R.string.permission_rationale_settings);
            alertDialogBuilder.setNeutralButton(getString(R.string.settings), (dialog, which) -> {
                final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
                startActivity(intent);
            });
        }
        return super.onCreateDialog(savedInstanceState);
    }
}
