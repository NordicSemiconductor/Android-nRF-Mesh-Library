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

package no.nordicsemi.android.nrfmesh.ble.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmesh.databinding.DeviceItemBinding;
import no.nordicsemi.android.nrfmesh.viewmodels.ScannerLiveData;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder> {
    private final List<ExtendedBluetoothDevice> mDevices;
    private OnItemClickListener mOnItemClickListener;

    public DevicesAdapter(@NonNull final LifecycleOwner owner, @NonNull final ScannerLiveData scannerLiveData) {
        mDevices = scannerLiveData.getDevices();
        scannerLiveData.observe(owner, devices -> {
            final Integer i = devices.getUpdatedDeviceIndex();
            if (i != null)
                notifyItemChanged(i);
            else
                notifyDataSetChanged();
        });
    }

    public void setOnItemClickListener(@NonNull final OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(DeviceItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final ExtendedBluetoothDevice device = mDevices.get(position);
        final String deviceName = device.getName();

        if (!TextUtils.isEmpty(deviceName))
            holder.deviceName.setText(deviceName);
        else
            holder.deviceName.setText(R.string.unknown_device);
        holder.deviceAddress.setText(device.getAddress());
        final int rssiPercent = (int) (100.0f * (127.0f + device.getRssi()) / (127.0f + 20.0f));
        holder.rssi.setImageLevel(rssiPercent);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(final ExtendedBluetoothDevice device);
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceAddress;
        TextView deviceName;
        ImageView rssi;

        private ViewHolder(final @NonNull DeviceItemBinding binding) {
            super(binding.getRoot());
            deviceAddress = binding.deviceAddress;
            deviceName = binding.deviceName;
            rssi = binding.rssi;

            binding.deviceContainer.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    if(getAdapterPosition() > -1 && mDevices.size() > 0) {
                        mOnItemClickListener.onItemClick(mDevices.get(getAdapterPosition()));
                    }
                }
            });
        }
    }
}
