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

package no.nordicsemi.android.nrfmeshprovisioner;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.DevicesAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ScannerLiveData;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SharedViewModel;

public class ScannerFragment extends Fragment implements Injectable, DevicesAdapter.OnItemClickListener {
    private static final String TAG = ScannerFragment.class.getSimpleName();
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 1022; // random number

    SharedViewModel mSharedViewModel;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.no_devices)
    View mEmptyView;
    @BindView(R.id.no_location_permission)
    View mNoLocationPermissionView;
    @BindView(R.id.action_grant_location_permission)
    Button mGrantPermissionButton;
    @BindView(R.id.action_permission_settings)
    Button mPermissionSettingsButton;
    @BindView(R.id.no_location)
    View mNoLocationView;
    @BindView(R.id.bluetooth_off)
    View mNoBluetoothView;

    private ScannerFragmentListener mScannerFragmentListener;

    interface ScannerFragmentListener {
        void showProgressBar();
        void hideProgressBar();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_scanner, null);
        ButterKnife.bind(this, rootView);

        mSharedViewModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(SharedViewModel.class);

        // Configure the recycler view
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_ble_devices);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        final DevicesAdapter adapter = new DevicesAdapter(this, mSharedViewModel.getScannerRepository().getScannerState());
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        return rootView;

    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        mScannerFragmentListener = (ScannerFragmentListener) context;
    }

    @Override
    public void onHiddenChanged(final boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            stopScan();
        } else {
            startScanning();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(isVisible()){
            startScanning();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopScan();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(final ExtendedBluetoothDevice device) {
        mSharedViewModel.disconnect();
        final Intent meshProvisionerIntent = new Intent(getActivity(), MeshProvisionerActivity.class);
        meshProvisionerIntent.putExtra(Utils.EXTRA_DEVICE, device);
        getActivity().startActivityForResult(meshProvisionerIntent, Utils.PROVISIONING_SUCCESS);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION:
                mSharedViewModel.getScannerRepository().getScannerState().refresh();
                break;
        }
    }

    @OnClick(R.id.action_enable_location)
    public void onEnableLocationClicked() {
        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    @OnClick(R.id.action_enable_bluetooth)
    public void onEnableBluetoothClicked() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableIntent);
    }

    @OnClick(R.id.action_grant_location_permission)
    public void onGrantLocationPermissionClicked() {
        Utils.markLocationPermissionRequested(getContext());
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
    }

    @OnClick(R.id.action_permission_settings)
    public void onPermissionSettingsClicked() {
        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
        startActivity(intent);
    }

    private void startScanning(){
        mSharedViewModel.getScannerRepository().getScannerState().startScanning();
        // Create view model containing utility methods for scanning
        mSharedViewModel.getScannerRepository().getScannerState().observe(this, this::startScan);
    }

    /**
     * Start scanning for Bluetooth devices or displays a message based on the scanner state.
     */
    private void startScan(final ScannerLiveData state) {
        // First, check the Location permission. This is required on Marshmallow onwards in order to scan for Bluetooth LE devices.
        if (Utils.isLocationPermissionsGranted(getContext())) {
            mNoLocationPermissionView.setVisibility(View.GONE);

            // Bluetooth must be enabled
            if (state.isBluetoothEnabled()) {
                mNoBluetoothView.setVisibility(View.GONE);
                // We are now OK to start scanning
                if(state.isScanRequested())
                    if(!state.isScanning()) {
                        Log.v(TAG, "scan started");
                        mSharedViewModel.getScannerRepository().startScan(BleMeshManager.MESH_PROVISIONING_UUID);
                    }

                if(isVisible())
                    mScannerFragmentListener.showProgressBar();

                if (state.isEmpty()) {
                    mEmptyView.setVisibility(View.VISIBLE);

                    if (!Utils.isLocationRequired(getContext()) || Utils.isLocationEnabled(getContext())) {
                        mNoLocationView.setVisibility(View.INVISIBLE);
                    } else {
                        mNoLocationView.setVisibility(View.VISIBLE);
                    }
                } else {
                    mEmptyView.setVisibility(View.GONE);
                }
            } else {
                mNoBluetoothView.setVisibility(View.VISIBLE);
                mScannerFragmentListener.hideProgressBar();
                mEmptyView.setVisibility(View.GONE);
            }
        } else {
            mNoLocationPermissionView.setVisibility(View.VISIBLE);
            mNoBluetoothView.setVisibility(View.GONE);
            mScannerFragmentListener.hideProgressBar();
            mEmptyView.setVisibility(View.GONE);

            final boolean deniedForever = Utils.isLocationPermissionDeniedForever(getActivity());
            mGrantPermissionButton.setVisibility(deniedForever ? View.GONE : View.VISIBLE);
            mPermissionSettingsButton.setVisibility(deniedForever ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * stop scanning for bluetooth devices.
     */
    private void stopScan() {
        mSharedViewModel.getScannerRepository().getScannerState().stopScanning();
        mSharedViewModel.getScannerRepository().stopScan();
        mScannerFragmentListener.hideProgressBar();
        Log.v(TAG, "stopping scan");
    }
}
