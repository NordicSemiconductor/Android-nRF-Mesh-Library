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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.mesh.Scene;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.scenes.SceneCallbacks;
import no.nordicsemi.android.nrfmesh.utils.HexKeyListener;
import no.nordicsemi.android.nrfmesh.utils.Utils;

import static no.nordicsemi.android.mesh.Scene.isValidScene;


public abstract class DialogFragmentScene extends DialogFragment {

    protected static final String SCENE = "SCENE";

    //UI Bindings
    @BindView(R.id.scene_name_layout)
    TextInputLayout sceneNameInputLayout;
    @BindView(R.id.name_input)
    TextInputEditText sceneNameInput;
    @BindView(R.id.scene_number_layout)
    TextInputLayout sceneNumberInputLayout;
    @BindView(R.id.number_input)
    TextInputEditText numberInput;
    protected Scene mScene;
    protected AlertDialog.Builder alertDialogBuilder;


    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mScene = getArguments().getParcelable(SCENE);
            Log.v("TAG", mScene.toString());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fragment_create_scene, null);

        //Bind ui
        ButterKnife.bind(this, rootView);
        final KeyListener hexKeyListener = new HexKeyListener();
        if (savedInstanceState != null) {
            mScene = savedInstanceState.getParcelable(SCENE);
        }

        if (mScene != null) {
            sceneNameInput.setText(mScene.getName());
            numberInput.setText(MeshAddress.formatAddress(mScene.getNumber(), false));
        }
        updateScene();

        numberInput.setKeyListener(hexKeyListener);
        numberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                mScene = null;
                if (TextUtils.isEmpty(s.toString())) {
                    sceneNumberInputLayout.setError(getString(R.string.error_empty_group_address));
                } else {
                    sceneNumberInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        alertDialogBuilder.setView(rootView);
        return alertDialogBuilder.show();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SCENE, mScene);
    }

    protected void updateScene() {
        if (mScene == null) {
            mScene = ((SceneCallbacks) requireParentFragment()).createScene();
        }
        sceneNameInput.setText(mScene.getName());
        numberInput.setText(Scene.formatSceneNumber(mScene.getNumber(), false));
    }

    protected final boolean validateInput(@NonNull final String name, @NonNull final String input) {
        try {
            if (TextUtils.isEmpty(name)) {
                sceneNameInputLayout.setError(getString(R.string.error_empty_group_name));
                return false;
            }
            if (input.length() % 4 != 0 || !input.matches(Utils.HEX_PATTERN)) {
                sceneNumberInputLayout.setError(getString(R.string.invalid_scene_number));
                return false;
            }

            if (!isValidScene(Integer.parseInt(input, 16))) {
                sceneNumberInputLayout.setError(getString(R.string.invalid_scene_number));
                return false;
            }
        } catch (IllegalArgumentException ex) {
            sceneNumberInputLayout.setError(ex.getMessage());
            return false;
        }

        return true;
    }
}
