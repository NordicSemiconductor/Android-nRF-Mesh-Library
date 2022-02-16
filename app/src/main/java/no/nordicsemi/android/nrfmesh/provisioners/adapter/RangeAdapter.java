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

package no.nordicsemi.android.nrfmesh.provisioners.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.AddressRange;
import no.nordicsemi.android.mesh.AllocatedGroupRange;
import no.nordicsemi.android.mesh.AllocatedSceneRange;
import no.nordicsemi.android.mesh.AllocatedUnicastRange;
import no.nordicsemi.android.mesh.Provisioner;
import no.nordicsemi.android.mesh.Range;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.RangeItemBinding;
import no.nordicsemi.android.nrfmesh.widgets.RangeView;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

public class RangeAdapter<T extends Range> extends RecyclerView.Adapter<RangeAdapter<T>.ViewHolder> {

    private final AsyncListDiffer<T> differ = new AsyncListDiffer<>(this, new RangeDiffCallback<>());
    private final List<Provisioner> mProvisioners;
    private final String mUuid;
    private OnItemClickListener mOnItemClickListener;

    public RangeAdapter(@NonNull final String uuid, @NonNull final List<T> ranges, @NonNull final List<Provisioner> provisioners) {
        mUuid = uuid;
        mProvisioners = provisioners;
        differ.submitList(new ArrayList<>(ranges));
    }

    public void setOnItemClickListener(final RangeAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void updateData(@NonNull List<? extends Range> ranges) {
        final List<? extends Range> a = new ArrayList<>(ranges);
        //noinspection unchecked
        differ.submitList((List<T>) a);
    }

    private List<T> populateLists(@NonNull List<T> ranges) {
        final List<Range> r = new ArrayList<>();
        for (T range : ranges) {
            try {
                r.add(range.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        //noinspection unchecked
        return (List<T>) r;
    }

    @NonNull
    @Override
    public RangeAdapter<T>.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new RangeAdapter<T>.ViewHolder(RangeItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RangeAdapter.ViewHolder holder, final int position) {
        final Range range = differ.getCurrentList().get(position);
        final String low, high;
        if (range instanceof AddressRange) {
            low = MeshAddress.formatAddress(((AddressRange) range).getLowAddress(), true);
            high = MeshAddress.formatAddress(((AddressRange) range).getHighAddress(), true);
        } else {
            low = MeshAddress.formatAddress(((AllocatedSceneRange) range).getFirstScene(), true);
            high = MeshAddress.formatAddress(((AllocatedSceneRange) range).getLastScene(), true);
        }
        holder.rangeValue.setText(holder.itemView.getContext().getString(R.string.range_adapter_format, low, high));
        holder.rangeView.clearRanges();
        holder.rangeView.addRange(range);
        addOverlappingRanges(range, holder.rangeView);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public Range getItem(final int position) {
        return differ.getCurrentList().get(position);
    }

    public void addItem(final int position, @NonNull final Range range) {
        //mRanges.add(position, range);
        notifyItemInserted(position);
    }

    public void removeItem(final int position) {
        //mRanges.remove(position);
        notifyItemRemoved(position);
    }

    private void addOverlappingRanges(@NonNull final Range range, @NonNull final RangeView rangeView) {
        rangeView.clearOtherRanges();
        for (Provisioner p : mProvisioners) {
            if (!p.getProvisionerUuid().equalsIgnoreCase(mUuid)) {
                if (range instanceof AllocatedUnicastRange) {
                    for (AllocatedUnicastRange otherRange : p.getAllocatedUnicastRanges()) {
                        if (range.overlaps(otherRange)) {
                            rangeView.addOtherRange(otherRange);
                        }
                    }
                } else if (range instanceof AllocatedGroupRange) {
                    for (AllocatedGroupRange otherRange : p.getAllocatedGroupRanges()) {
                        if (range.overlaps(otherRange)) {
                            rangeView.addOtherRange(otherRange);
                        }
                    }
                } else {
                    for (AllocatedSceneRange otherRange : p.getAllocatedSceneRanges()) {
                        if (range.overlaps(otherRange)) {
                            rangeView.addOtherRange(otherRange);
                        }
                    }
                }
            }
        }
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(final int position, @NonNull final Range range);
    }

    final class ViewHolder extends RemovableViewHolder {
        TextView rangeValue;
        RangeView rangeView;

        private ViewHolder(final @NonNull RangeItemBinding binding) {
            super(binding.getRoot());
            rangeValue = binding.rangeText;
            rangeView = binding.range;
            binding.container.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(getAbsoluteAdapterPosition(), differ.getCurrentList().get(getAbsoluteAdapterPosition()));
                }
            });
        }
    }
}
