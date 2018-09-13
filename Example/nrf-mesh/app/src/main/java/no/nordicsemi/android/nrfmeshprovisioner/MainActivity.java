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

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentResetNetwork;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SharedViewModel;

public class MainActivity extends AppCompatActivity implements Injectable, HasSupportFragmentInjector,  BottomNavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemReselectedListener,
        ScannerFragment.ScannerFragmentListener, FragmentManager.OnBackStackChangedListener,
        NetworkFragment.NetworkFragmentListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CURRENT_FRAGMENT = "CURRENT_FRAGMENT";

    @Inject
    DispatchingAndroidInjector<Fragment> mDispatchingAndroidInjector;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.state_scanning)
    View mScanningView;

    private SharedViewModel mViewModel;
    private BottomNavigationView mBottomNavigationView;

    private NetworkFragment mNetworkFragment;
    private ScannerFragment mScannerFragment;
    private Fragment mSettingsFragment;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(SharedViewModel.class);

        mNetworkFragment = (NetworkFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_network);
        mScannerFragment = (ScannerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_scanner);
        mSettingsFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_settings);
        mBottomNavigationView = findViewById(R.id.bottom_navigation_view);

        mBottomNavigationView.setOnNavigationItemSelectedListener(this);
        mBottomNavigationView.setOnNavigationItemReselectedListener(this);

        mViewModel.getProvisionedNodesLiveData().observe(this, provisionedNodesLiveData -> {
            invalidateOptionsMenu();
        });

        mViewModel.isConnected().observe(this, isConnected -> {
            if(isConnected != null) {
                invalidateOptionsMenu();
            }
        });

        if(savedInstanceState == null) {
            onNavigationItemSelected(mBottomNavigationView.getMenu().findItem(R.id.action_network));
        } else {
            mBottomNavigationView.setSelectedItemId(savedInstanceState.getInt(CURRENT_FRAGMENT));
        }
    }/*

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if(!mViewModel.getProvisionedNodesLiveData().getProvisionedNodes().isEmpty()){
            if(mNetworkFragment.isVisible()) {
                if (!mViewModel.isConenctedToMesh()) {
                    getMenuInflater().inflate(R.menu.connect, menu);
                } else {
                    getMenuInflater().inflate(R.menu.disconnect, menu);
                }
            } else if(mSettingsFragment.isVisible()){
                getMenuInflater().inflate(R.menu.reset_network, menu);
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_connect:
                final Intent scannerActivity = new Intent(this, ProvisionedNodesScannerActivity.class);
                scannerActivity.putExtra(ProvisionedNodesScannerActivity.NETWORK_ID, mViewModel.getNetworkId());
                startActivity(scannerActivity);
                return true;
            case R.id.action_disconnect:
                mViewModel.disconnect();
                return true;
            case R.id.action_reset_network:
                final DialogFragmentResetNetwork dialogFragmentResetNetwork = DialogFragmentResetNetwork.
                        newInstance(getString(R.string.title_reset_network), getString(R.string.message_reset_network));
                        dialogFragmentResetNetwork.show(getSupportFragmentManager(), null);
                return true;
        }
        return false;
    }*/

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Utils.PROVISIONING_SUCCESS){
            if(resultCode == RESULT_OK){
                final boolean result = data.getBooleanExtra("result", false);
                if(result){
                    mBottomNavigationView.setSelectedItemId(R.id.action_network);
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (id) {
            case R.id.action_network:
                ft.show(mNetworkFragment).hide(mScannerFragment).hide(mSettingsFragment);
                break;
            case R.id.action_scanner:
                ft.hide(mNetworkFragment).show(mScannerFragment).hide(mSettingsFragment);
                break;
            case R.id.action_settings:
                ft.hide(mNetworkFragment).hide(mScannerFragment).show(mSettingsFragment);
                break;
        }
        ft.commit();
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {
    }

    @Override
    public void showProgressBar() {
        mScanningView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        mScanningView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackStackChanged() {

    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return mDispatchingAndroidInjector;
    }

    @Override
    public void onProvisionedMeshNodeSelected() {

    }

    /*@Override
    public void onNetworkReset() {
        mViewModel.resetMeshNetwork();
    }*/
}
