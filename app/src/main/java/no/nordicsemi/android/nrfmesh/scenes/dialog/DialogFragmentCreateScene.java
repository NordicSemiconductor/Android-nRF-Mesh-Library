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

package no.nordicsemi.android.nrfmesh.scenes.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import no.nordicsemi.android.mesh.Scene;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.scenes.SceneCallbacks;


public class DialogFragmentCreateScene extends DialogFragmentScene {

    public static DialogFragmentCreateScene newInstance(@NonNull final Scene scene) {
        final DialogFragmentCreateScene fragmentCreateScene = new DialogFragmentCreateScene();
        final Bundle args = new Bundle();
        args.putParcelable(SCENE, scene);
        fragmentCreateScene.setArguments(args);
        return fragmentCreateScene;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        alertDialogBuilder = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_scene)
                .setIcon(R.drawable.ic_edit_black)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        final AlertDialog alertDialog = (AlertDialog) super.onCreateDialog(savedInstanceState);
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            final String name = binding.nameInput.getEditableText().toString().trim();
            try {
                if (validateInput(name)) {
                    mScene.setName(name);
                    if (((SceneCallbacks) requireActivity()).onSceneAdded(mScene)) {
                        dismiss();
                    }
                }
            } catch (IllegalArgumentException ex) {
                binding.sceneNumberLayout.setError(ex.getMessage());
            }
        });
        return alertDialog;
    }
}
