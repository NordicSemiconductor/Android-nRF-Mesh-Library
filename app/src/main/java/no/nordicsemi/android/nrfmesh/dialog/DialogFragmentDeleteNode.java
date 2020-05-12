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
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.utils.Utils;

public class DialogFragmentDeleteNode extends DialogFragmentMessage {

    private int position;

    public interface DialogFragmentDeleteNodeListener {
        void onNodeDeleteConfirmed(final int position);

        void onNodeDeleteCancelled(final int position);
    }

    public static DialogFragmentDeleteNode newInstance(final int position) {
        Bundle args = new Bundle();
        DialogFragmentDeleteNode fragment = new DialogFragmentDeleteNode();
        args.putInt(Utils.EXTRA_DATA, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(Utils.EXTRA_DATA, -1);
        }
        title = getString(R.string.title_delete_node);
        message = getString(R.string.delete_node_rationale);

        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(Utils.EXTRA_DATA, -1);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        alertDialogBuilder = new AlertDialog.Builder(requireActivity());
        alertDialogBuilder.setIcon(R.drawable.ic_delete);
        alertDialogBuilder.setNegativeButton(getString(R.string.no), (dialog, which) ->
                ((DialogFragmentDeleteNodeListener) requireParentFragment()).onNodeDeleteCancelled(position));
        alertDialogBuilder.setPositiveButton(getString(R.string.yes), (dialog, which) ->
                ((DialogFragmentDeleteNodeListener) requireParentFragment()).onNodeDeleteConfirmed(position));

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Utils.EXTRA_DATA, position);
    }
}
