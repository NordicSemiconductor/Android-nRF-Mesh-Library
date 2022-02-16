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

package no.nordicsemi.android.nrfmesh.scenes.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.elevation.ElevationOverlayProvider;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.RemovableRowItemBinding;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class StoredScenesAdapter extends RecyclerView.Adapter<StoredScenesAdapter.ViewHolder> {

    @SuppressWarnings("ComparatorCombinators")
    public static final Comparator<SceneUiState> sceneComparator = (scene1UiState1, sceneUiState2) -> Integer.compare(scene1UiState1.getNumber(), sceneUiState2.getNumber());
    private final AsyncListDiffer<SceneUiState> differ = new AsyncListDiffer<>(this, new SceneItemDiffCallback());
    private OnItemListener mOnItemListener;

    public void update(@NonNull final List<SceneUiState> scenes) {
        Collections.sort(scenes, sceneComparator);
        differ.submitList(scenes);
    }

    public void setOnItemClickListener(final OnItemListener listener) {
        mOnItemListener = listener;
    }

    @NonNull
    @Override
    public StoredScenesAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new StoredScenesAdapter.ViewHolder(RemovableRowItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final StoredScenesAdapter.ViewHolder holder, final int position) {
        final SceneUiState scene = differ.getCurrentList().get(position);
        holder.sceneName.setText(scene.getName());
        final String number = "0x" + String.format(Locale.US, "%04X", scene.getNumber());
        holder.sceneNumber.setText(number);
        holder.getSwipeableView().setTag(scene);
        if (scene.isCurrentScene()) {
            holder.image.setVisibility(VISIBLE);
        } else {
            holder.image.setVisibility(INVISIBLE);
        }
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    @FunctionalInterface
    public interface OnItemListener {
        void onItemClick(@NonNull final SceneUiState sceneNumber);
    }

    public final class ViewHolder extends RemovableViewHolder {
        TextView sceneName;
        TextView sceneNumber;
        ImageView image;

        private ViewHolder(final @NonNull RemovableRowItemBinding binding) {
            super(binding.getRoot());
            ((ImageView) binding.icon)
                    .setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_baseline_palette_24dp));
            sceneName = binding.title;
            sceneNumber = binding.subtitle;
            image = binding.image;
            final ElevationOverlayProvider provider = new ElevationOverlayProvider(itemView.getContext());
            final int color = provider.compositeOverlayIfNeeded(provider.getThemeSurfaceColor(), 3.5f);
            getSwipeableView().setBackgroundColor(color);
            binding.container.setOnClickListener(v -> {
                if (mOnItemListener != null)
                    mOnItemListener.onItemClick(differ.getCurrentList().get(getAbsoluteAdapterPosition()));
            });
        }
    }
}
