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

import dagger.Subcomponent;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.AddAppKeyViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.AddKeysViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.AddNetKeyViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.AddProvisionerViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.AppKeysViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.EditAppKeyViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.EditNetKeyViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.EditProvisionerViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.GroupControlsViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ModelConfigurationViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.NetKeysViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.NodeConfigurationViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.NodeDetailsViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ProvisionersViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ProvisioningViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.PublicationViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.RangesViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ReconnectViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ScannerViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SharedViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.SplashViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ViewModelFactory;

/**
 * A sub component to create ViewModels. It is called by the
 * {@link ViewModelFactory}. Using this component allows
 * ViewModels to define {@link javax.inject.Inject} constructors.
 */
@Subcomponent
public interface ViewModelSubComponent {
    @Subcomponent.Builder
    interface Builder {
        ViewModelSubComponent build();
    }

    SplashViewModel splashViewModel();

    SharedViewModel commonViewModel();

    ScannerViewModel scannerViewModel();

    GroupControlsViewModel groupControlsViewModel();

    ProvisionersViewModel provisionersViewModel();

    AddProvisionerViewModel addProvisionerViewModel();

    EditProvisionerViewModel editProvisionerViewModel();

    RangesViewModel rangesViewModel();

    NetKeysViewModel netKeysViewModel();

    AddNetKeyViewModel addNetKeyViewModel();

    EditNetKeyViewModel editNetKeyViewModel();

    AppKeysViewModel appKeysViewModel();

    AddAppKeyViewModel addAppKeyViewModel();

    EditAppKeyViewModel editAppKeyViewModel();

    ProvisioningViewModel meshProvisionerViewModel();

    NodeConfigurationViewModel nodeConfigurationViewModel();

    AddKeysViewModel addKeysViewModel();

    NodeDetailsViewModel nodeDetailsViewModel();

    ModelConfigurationViewModel modelConfigurationViewModel();

    PublicationViewModel publicationViewModel();

    ReconnectViewModel reconnectViewModule();
}
