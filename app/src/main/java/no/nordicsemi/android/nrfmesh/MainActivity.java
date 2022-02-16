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

package no.nordicsemi.android.nrfmesh;

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.nrfmesh.databinding.ActivityMainBinding;
import no.nordicsemi.android.nrfmesh.viewmodels.SharedViewModel;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements
        NavigationBarView.OnItemSelectedListener,
        NavigationBarView.OnItemReselectedListener {
    // This flag is false when the app is first started (cold start).
    // In this case, the animation will be fully shown (1 sec).
    // Subsequent launches will display it only briefly.
    // It is only used on API 31+
    private static boolean coldStart = true;

    private static final String CURRENT_FRAGMENT = "CURRENT_FRAGMENT";

    private SharedViewModel mViewModel;

    private NetworkFragment mNetworkFragment;
    private GroupsFragment mGroupsFragment;
    private ProxyFilterFragment mProxyFilterFragment;
    private Fragment mSettingsFragment;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        // Set the proper theme for the Activity. This could have been set in "v23/styles..xml"
        // as "postSplashScreenTheme", but as this app works on pre-API-23 devices, it needs to be
        // set for them as well, and that code would not apply in such case.
        // As "postSplashScreenTheme" is optional, and setting the theme can be done using
        // setTheme, this is preferred in our case, as this also work for older platforms.
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // Set up the splash screen.
        // The app is using SplashScreen compat library, which is supported on Android 5+, but the
        // icon is only supported on API 23+.
        //
        // See: https://android.googlesource.com/platform/frameworks/support/+/androidx-main/core/core-splashscreen/src/main/java/androidx/core/splashscreen/package-info.java
        //
        // On Android 12+ the splash screen will be animated, while on 6 - 11 will present a still
        // image. See more: https://developer.android.com/guide/topics/ui/splash-screen/
        //
        // As nRF Mesh supports Android 4.3+, on older platforms a 9-patch image is presented
        // without the use of SplashScreen compat library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

            // Animated Vector Drawable is only supported on API 31+.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (coldStart) {
                    coldStart = false;
                    // Keep the splash screen on-screen for longer periods.
                    // Handle the splash screen transition.
                    final long then = System.currentTimeMillis();
                    splashScreen.setKeepOnScreenCondition(() -> mViewModel.getNetworkLiveData().getMeshNetwork() == null);
                }
            }
        }

        final ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.app_name);

        mNetworkFragment = (NetworkFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_network);
        mGroupsFragment = (GroupsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_groups);
        mProxyFilterFragment = (ProxyFilterFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_proxy);
        mSettingsFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_settings);
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setOnItemReselectedListener(this);

        if (savedInstanceState == null) {
            onNavigationItemSelected(bottomNavigationView.getMenu().findItem(R.id.action_network));
        } else {
            bottomNavigationView.setSelectedItemId(savedInstanceState.getInt(CURRENT_FRAGMENT));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final Boolean isConnectedToNetwork = mViewModel.isConnectedToProxy().getValue();
        if (isConnectedToNetwork != null && isConnectedToNetwork) {
            getMenuInflater().inflate(R.menu.disconnect, menu);
        } else {
            getMenuInflater().inflate(R.menu.connect, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_connect) {
            mViewModel.navigateToScannerActivity(this, false);
            return true;
        } else if (id == R.id.action_disconnect) {
            mViewModel.disconnect();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (id == R.id.action_network) {
            ft.show(mNetworkFragment).hide(mGroupsFragment).hide(mProxyFilterFragment).hide(mSettingsFragment);
        } else if (id == R.id.action_groups) {
            ft.hide(mNetworkFragment).show(mGroupsFragment).hide(mProxyFilterFragment).hide(mSettingsFragment);
        } else if (id == R.id.action_proxy) {
            ft.hide(mNetworkFragment).hide(mGroupsFragment).show(mProxyFilterFragment).hide(mSettingsFragment);
        } else if (id == R.id.action_settings) {
            ft.hide(mNetworkFragment).hide(mGroupsFragment).hide(mProxyFilterFragment).show(mSettingsFragment);
        }
        ft.commit();
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {
    }
}
