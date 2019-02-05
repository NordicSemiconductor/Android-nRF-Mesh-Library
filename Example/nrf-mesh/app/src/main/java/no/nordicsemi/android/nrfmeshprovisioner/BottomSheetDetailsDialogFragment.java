package no.nordicsemi.android.nrfmeshprovisioner;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import java.util.ArrayList;

import javax.inject.Inject;

import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.GroupModelAdapter;

public class BottomSheetDetailsDialogFragment extends BottomSheetDialogFragment implements GroupModelAdapter.OnItemClickListener {

    private static final String GROUP = "GROUP";
    private static final String ELEMENTS = "ELEMENTS";


    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private TextInputLayout mGroupNameInputLayout;
    private TextInputEditText mGroupNameTextInput;

    private Group mGroup;
    private ArrayList<Element> mElements = new ArrayList<>();
    private GroupModelAdapter mGroupModelsAdapter;

    public interface BottomSheetDetailsListener {
        void editModelItem(@NonNull final Element element, @NonNull final MeshModel model);

        void onGroupNameChanged(@NonNull final Group group);
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
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog);
        if (getArguments() != null) {
            mGroup = getArguments().getParcelable(GROUP);
            mElements = getArguments().getParcelableArrayList(ELEMENTS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false);

        mGroupNameInputLayout = rootView.findViewById(R.id.text_input_layout);
        mGroupNameTextInput = rootView.findViewById(R.id.text_input);
        mGroupNameTextInput.setText(mGroup.getName());

        final RecyclerView recyclerViewGroupItems = rootView.findViewById(R.id.group_items);
        recyclerViewGroupItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        mGroupModelsAdapter = new GroupModelAdapter(requireContext(), mGroup, mElements);
        mGroupModelsAdapter.setOnItemClickListener(this);
        recyclerViewGroupItems.setAdapter(mGroupModelsAdapter);

        final Button actionApply = rootView.findViewById(R.id.action_apply);
        actionApply.setOnClickListener(v -> {
            final String groupName = mGroupNameTextInput.getEditableText().toString();
            if (validateInput(groupName)) {
                hideKeyboard();
                mGroupNameTextInput.clearFocus();
                mGroup.setName(groupName);
                ((BottomSheetDetailsListener) requireActivity()).onGroupNameChanged(mGroup);
            }
        });

        return rootView;
    }

    @Override
    public void onModelItemClick(final Element element, final MeshModel model) {
        ((BottomSheetDetailsListener) requireActivity()).editModelItem(element, model);
    }

    public void updateAdapter(final Group group, final ArrayList<Element> elements) {
        if (mGroupModelsAdapter != null) {
            mGroup = group;
            mElements = elements;
            mGroupModelsAdapter.updateAdapter(group, elements);
            mGroupModelsAdapter.notifyDataSetChanged();
        }
    }

    private boolean validateInput(final String groupName) {
        return !TextUtils.isEmpty(groupName);
    }

    public void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mGroupNameTextInput.getWindowToken(), 0);
        }
    }
}
