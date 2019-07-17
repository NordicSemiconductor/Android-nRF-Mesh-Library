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

package no.nordicsemi.android.nrfmeshprovisioner.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import no.nordicsemi.android.nrfmeshprovisioner.GroupControlsActivity;
import no.nordicsemi.android.nrfmeshprovisioner.MainActivity;
import no.nordicsemi.android.nrfmeshprovisioner.ProvisioningActivity;
import no.nordicsemi.android.nrfmeshprovisioner.SplashScreenActivity;
import no.nordicsemi.android.nrfmeshprovisioner.ble.ReconnectActivity;
import no.nordicsemi.android.nrfmeshprovisioner.ble.ScannerActivity;
import no.nordicsemi.android.nrfmeshprovisioner.keys.AddAppKeyActivity;
import no.nordicsemi.android.nrfmeshprovisioner.keys.AddAppKeysActivity;
import no.nordicsemi.android.nrfmeshprovisioner.keys.AddNetKeyActivity;
import no.nordicsemi.android.nrfmeshprovisioner.keys.AddNetKeysActivity;
import no.nordicsemi.android.nrfmeshprovisioner.keys.AppKeysActivity;
import no.nordicsemi.android.nrfmeshprovisioner.keys.EditAppKeyActivity;
import no.nordicsemi.android.nrfmeshprovisioner.keys.EditNetKeyActivity;
import no.nordicsemi.android.nrfmeshprovisioner.keys.NetKeysActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.ConfigurationClientActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.ConfigurationServerActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.GenericLevelServerActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.GenericOnOffServerActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.ModelConfigurationActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.NodeConfigurationActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.NodeDetailsActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.PublicationSettingsActivity;
import no.nordicsemi.android.nrfmeshprovisioner.node.VendorModelActivity;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.AddProvisionerActivity;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.EditProvisionerActivity;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.ProvisionersActivity;
import no.nordicsemi.android.nrfmeshprovisioner.provisioners.RangesActivity;

@Module
abstract class ActivitiesModule {

    @ContributesAndroidInjector()
    abstract SplashScreenActivity contributeSplashScreenActivity();

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector()
    abstract ProvisionersActivity contributeProvisionersActivity();

    @ContributesAndroidInjector()
    abstract AddProvisionerActivity contributeAddProvisionersActivity();

    @ContributesAndroidInjector()
    abstract EditProvisionerActivity contributeEditProvisionersActivity();

    @ContributesAndroidInjector()
    abstract RangesActivity contributeRangesActivity();

    @ContributesAndroidInjector()
    abstract NetKeysActivity contributeNetKeysActivity();

    @ContributesAndroidInjector()
    abstract AddNetKeyActivity contributeAddNetKeyActivity();

    @ContributesAndroidInjector()
    abstract EditNetKeyActivity contributeEditNetKeyActivity();

    @ContributesAndroidInjector()
    abstract AppKeysActivity contributeAppKeysActivity();

    @ContributesAndroidInjector()
    abstract AddAppKeyActivity contributeAddAppKeyActivity();

    @ContributesAndroidInjector()
    abstract EditAppKeyActivity contributeEditAppKeyActivity();

    @ContributesAndroidInjector()
    abstract ProvisioningActivity contributeMeshProvisionerActivity();

    @ContributesAndroidInjector()
    abstract NodeConfigurationActivity contributeElementConfigurationActivity();

    @ContributesAndroidInjector()
    abstract AddAppKeysActivity contributeAddAppKeysActivity();

    @ContributesAndroidInjector()
    abstract AddNetKeysActivity contributeAddNetKeysActivity();

    @ContributesAndroidInjector()
    abstract ScannerActivity contributeScannerActivity();

    @ContributesAndroidInjector()
    abstract ReconnectActivity contributeReconnectActivity();

    @ContributesAndroidInjector()
    abstract NodeDetailsActivity contributeNodeDetailsActivity();

    @ContributesAndroidInjector()
    abstract GroupControlsActivity contributeGroupControlsActivity();

    @ContributesAndroidInjector()
    abstract PublicationSettingsActivity contributePublicationSettingsActivity();

    @ContributesAndroidInjector()
    abstract ConfigurationServerActivity contributeConfigurationServerActivity();

    @ContributesAndroidInjector()
    abstract ConfigurationClientActivity contributeConfigurationClientActivity();

    @ContributesAndroidInjector()
    abstract GenericOnOffServerActivity contributeGenericOnOffServerActivity();

    @ContributesAndroidInjector()
    abstract GenericLevelServerActivity contributeGenericLevelServerActivity();

    @ContributesAndroidInjector()
    abstract VendorModelActivity contributeVendorModelActivity();

    @ContributesAndroidInjector()
    abstract ModelConfigurationActivity contributeModelConfigurationActivity();
}
