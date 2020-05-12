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

package no.nordicsemi.android.nrfmesh.widgets;

import android.graphics.Canvas;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.adapter.GroupAdapter;
import no.nordicsemi.android.nrfmesh.node.adapter.NodeAdapter;

/**
 * This callback works with {@link RemovableViewHolder}. Only view holders that inherit from this class may be removed.
 */
public class RemovableItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mAdapter;
    private float mPreviousDx;
    private boolean swipeBack;

    public RemovableItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof RemovableViewHolder) {
            final RemovableViewHolder vHolder = ((RemovableViewHolder) viewHolder);
            if (!(vHolder).isRemovable())
                return 0;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(0, swipeFlags);
        } else
            return 0;
    }

    @Override
    public float getSwipeThreshold(@NonNull final RecyclerView.ViewHolder viewHolder) {
        return super.getSwipeThreshold(viewHolder);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        // do nothing, moving not allowed
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss((RemovableViewHolder) viewHolder);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        getDefaultUIUtil().clearView(((RemovableViewHolder) viewHolder).getSwipeableView());
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            getDefaultUIUtil().onSelected(((RemovableViewHolder) viewHolder).getSwipeableView());
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (recyclerView.getId() == R.id.recycler_view_groups) {
            final GroupAdapter adapter = (GroupAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                final int count = adapter.getModelCount(viewHolder.getAdapterPosition());
                if (count > 0) {
                    if (dX > -1100.0f && dX < 1100.0f && dX != 0) {
                        getDefaultUIUtil().onDraw(c, recyclerView, ((RemovableViewHolder) viewHolder).getSwipeableView(), dX, dY, actionState, isCurrentlyActive);
                        swipeBack = true;
                    } else {
                        if (dX == 0 && !swipeBack) {
                            mAdapter.onItemDismissFailed((RemovableViewHolder) viewHolder);
                        }
                    }
                } else {
                    getDefaultUIUtil().onDraw(c, recyclerView, ((RemovableViewHolder) viewHolder).getSwipeableView(), dX, dY, actionState, isCurrentlyActive);
                }
            }
        } else if (recyclerView.getId() == R.id.recycler_view_provisioned_nodes) {
            final NodeAdapter adapter = (NodeAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                if (dX > -800.0f && dX < 800.0f && dX != 0) {
                    getDefaultUIUtil().onDraw(c, recyclerView, ((RemovableViewHolder) viewHolder).getSwipeableView(), dX, dY, actionState, isCurrentlyActive);
                    //swipeBack = true;
                } else {
                    if (dX == 0 && !swipeBack) {
                        mAdapter.onItemDismissFailed((RemovableViewHolder) viewHolder);
                    }
                }
            }
        } else {
            getDefaultUIUtil().onDraw(c, recyclerView, ((RemovableViewHolder) viewHolder).getSwipeableView(), dX, dY, actionState, isCurrentlyActive);
        }

        final RemovableViewHolder vHolder = ((RemovableViewHolder) viewHolder);
        final ImageView view = vHolder.getDeleteView();
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(view.getLayoutParams());
        final int margin = (int) recyclerView.getContext().getResources().getDimension(R.dimen.activity_horizontal_margin);
        if (mPreviousDx <= 0 && dX > 0) {
            // swiping from left to right
            if (layoutParams.gravity != (Gravity.CENTER_VERTICAL | Gravity.START)) {
                layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                layoutParams.setMarginStart(margin);
                layoutParams.setMarginEnd(margin);
                view.setLayoutParams(layoutParams);
            }
        } else if (mPreviousDx >= 0 && dX < 0) {
            // swiping from right to left
            if (layoutParams.gravity != (Gravity.CENTER_VERTICAL | Gravity.START)) {
                layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                layoutParams.setMarginStart(margin);
                layoutParams.setMarginEnd(margin);
                view.setLayoutParams(layoutParams);
            }
        }
        mPreviousDx = dX;
    }

    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        getDefaultUIUtil().onDrawOver(c, recyclerView, ((RemovableViewHolder) viewHolder).getSwipeableView(), dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public int convertToAbsoluteDirection(final int flags, final int layoutDirection) {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        } else {
            return super.convertToAbsoluteDirection(flags, layoutDirection);
        }
    }
}