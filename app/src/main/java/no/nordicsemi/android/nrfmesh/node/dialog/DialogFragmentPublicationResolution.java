package no.nordicsemi.android.nrfmesh.node.dialog;


import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;

public class DialogFragmentPublicationResolution extends DialogFragment {

    private static final String PUBLICATION_RESOLUTION = "PUBLICATION_RESOLUTION";
    private int mPublicationResolution;

    public static DialogFragmentPublicationResolution newInstance(final int resolution) {
        DialogFragmentPublicationResolution fragmentPublicationResolution = new DialogFragmentPublicationResolution();
        final Bundle args = new Bundle();
        args.putInt(PUBLICATION_RESOLUTION, resolution);
        fragmentPublicationResolution.setArguments(args);
        return fragmentPublicationResolution;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPublicationResolution = getArguments().getInt(PUBLICATION_RESOLUTION, 0);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_linear_scale)
                .setTitle(R.string.title_publication_resolution)
                .setSingleChoiceItems(R.array.arr_publication_resolution, mPublicationResolution, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    final int index = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    ((DialogFragmentPublicationResolutionListener) requireActivity()).setPublicationResolution(getResolution(index));})
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    final int index = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    ((DialogFragmentPublicationResolutionListener) requireActivity()).setPublicationResolution(getResolution(index));
                });

        return alertDialogBuilder.create();
    }

    private int getResolution(final int index) {
        switch (index) {
            default:
            case 0:
                return MeshParserUtils.RESOLUTION_100_MS;
            case 1:
                return MeshParserUtils.RESOLUTION_1_S;
            case 2:
                return MeshParserUtils.RESOLUTION_10_S;
            case 3:
                return MeshParserUtils.RESOLUTION_10_M;
        }

    }

    public interface DialogFragmentPublicationResolutionListener {

        void setPublicationResolution(final int resolution);

    }
}
