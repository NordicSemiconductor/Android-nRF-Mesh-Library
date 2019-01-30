package no.nordicsemi.android.nrfmeshprovisioner;

import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.GroupModelAdapter;

public class BottomSheetDetailsDialogFragment extends BottomSheetDialogFragment implements GroupModelAdapter.OnItemClickListener {

    private static final String GROUP = "GROUP";
    private static final String ELEMENTS = "ELEMENTS";
    private static final String MODELS = "MODELS";


    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private Group mGroup;
    private ArrayList<Element> mElements = new ArrayList<>();
    private GroupModelAdapter mGroupModelsAdapter;

    public void updateAdapter(final Group group, final ArrayList<Element> elements) {
        if(mGroupModelsAdapter != null) {
            mGroup = group;
            mElements = elements;
            mGroupModelsAdapter.updateAdapter(group, elements);
            mGroupModelsAdapter.notifyDataSetChanged();
        }
    }

    public interface BottomSheetDetailsListener {
        void editModelItem(@NonNull final Element element, @NonNull final MeshModel model);
    }

    public static BottomSheetDetailsDialogFragment getInstance(final Group group, final ArrayList<Element> elements) {
        final BottomSheetDetailsDialogFragment fragment = new BottomSheetDetailsDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(GROUP, group);
        args.putParcelableArrayList(ELEMENTS, elements);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mGroup = getArguments().getParcelable(GROUP);
            mElements = getArguments().getParcelableArrayList(ELEMENTS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false);

        final RecyclerView recyclerViewGroupItems = rootView.findViewById(R.id.group_items);
        recyclerViewGroupItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        mGroupModelsAdapter = new GroupModelAdapter(requireContext(), mGroup, mElements);
        mGroupModelsAdapter.setOnItemClickListener(this);
        recyclerViewGroupItems.setAdapter(mGroupModelsAdapter);
        return rootView;
    }

    @Override
    public void onModelItemClick(final Element element, final MeshModel model) {
        ((BottomSheetDetailsListener)requireActivity()).editModelItem(element, model);
    }
}
