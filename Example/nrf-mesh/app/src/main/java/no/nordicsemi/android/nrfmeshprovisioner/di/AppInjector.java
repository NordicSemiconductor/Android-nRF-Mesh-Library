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

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import dagger.android.AndroidInjection;
import dagger.android.support.AndroidSupportInjection;
import dagger.android.support.HasSupportFragmentInjector;

/**
 * Helper class to automatically inject fragments if they implement {@link Injectable}.
 */
class AppInjector {
	private AppInjector() {
	}

	static void init(final MeshApplication application) {
		DaggerMeshAppComponent.builder()
				.contextModule(new ContextModule(application))
				.build()
				.inject(application);

		application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
				handleActivity(activity);
			}

			@Override
			public void onActivityStarted(final Activity activity) {
				// empty
			}

			@Override
			public void onActivityResumed(final Activity activity) {
				// empty
			}

			@Override
			public void onActivityPaused(final Activity activity) {
				// empty
			}

			@Override
			public void onActivityStopped(final Activity activity) {
				// empty
			}

			@Override
			public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {
				// empty
			}

			@Override
			public void onActivityDestroyed(final Activity activity) {
				// empty
			}
		});
	}

	private static void handleActivity(final Activity activity) {
		if (activity instanceof Injectable || activity instanceof HasSupportFragmentInjector) {
			AndroidInjection.inject(activity);
		}
		if (activity instanceof AppCompatActivity) {
			((AppCompatActivity) activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(
					new FragmentManager.FragmentLifecycleCallbacks() {
						@Override
						public void onFragmentCreated(@NonNull final FragmentManager fm, @NonNull final Fragment f, final Bundle savedInstanceState) {
							if (f instanceof Injectable) {
								AndroidSupportInjection.inject(f);
							}
						}
					}, true);
		}
	}
}
