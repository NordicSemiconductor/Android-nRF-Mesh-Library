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

package no.nordicsemi.android.nrfmesh.node;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.Features;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.CompanyIdentifiers;
import no.nordicsemi.android.mesh.utils.CompositionDataParser;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.ActivityNodeDetailsBinding;
import no.nordicsemi.android.nrfmesh.viewmodels.NodeDetailsViewModel;

@AndroidEntryPoint
public class NodeDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityNodeDetailsBinding binding = ActivityNodeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        final NodeDetailsViewModel viewModel = new ViewModelProvider(this).get(NodeDetailsViewModel.class);

        if (viewModel.getSelectedMeshNode().getValue() == null) {
            finish();
        }

        final ProvisionedMeshNode node = viewModel.getSelectedMeshNode().getValue();
        final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(node.getNodeName());

        binding.containerTimestamp.getRoot().setClickable(false);
        final TextView timestamp = binding.containerTimestamp.text;
        final String format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(node.getTimeStamp());
        timestamp.setText(format);

        binding.containerUnicastAddress.getRoot().setClickable(false);
        binding.containerUnicastAddress.text.setText(MeshParserUtils.bytesToHex(MeshAddress.addressIntToBytes(node.getUnicastAddress()), false));

        binding.containerDeviceKey.getRoot().setClickable(false);
        binding.containerDeviceKey.text.setText(MeshParserUtils.bytesToHex(node.getDeviceKey(), false));

        binding.copy.setOnClickListener(v -> {
            if (clipboard != null) {
                final ClipData clipDeviceKey = ClipData.newPlainText("Device Key", MeshParserUtils.bytesToHex(node.getDeviceKey(), false));
                clipboard.setPrimaryClip(clipDeviceKey);
                Toast.makeText(NodeDetailsActivity.this, R.string.device_key_clipboard_copied, Toast.LENGTH_SHORT).show();
            }
        });

        binding.containerCompanyIdentifier.getRoot().setClickable(false);
        if (node.getCompanyIdentifier() != null) {
            binding.containerCompanyIdentifier.text.setText(CompanyIdentifiers.getCompanyName(node.getCompanyIdentifier().shortValue()));
        } else {
            binding.containerCompanyIdentifier.text.setText(R.string.unknown);
        }

        binding.containerProductIdentifier.getRoot().setClickable(false);
        if (node.getProductIdentifier() != null) {
            binding.containerProductIdentifier.text.setText(CompositionDataParser.formatProductIdentifier(node.getProductIdentifier().shortValue(), false));
        } else {
            binding.containerProductIdentifier.text.setText(R.string.unavailable);
        }

        binding.containerProductVersion.getRoot().setClickable(false);
        if (node.getVersionIdentifier() != null) {
            binding.containerProductVersion.text.setText(CompositionDataParser.formatVersionIdentifier(node.getVersionIdentifier().shortValue(), false));
        } else {
            binding.containerProductVersion.text.setText(R.string.unavailable);
        }

        binding.containerCrpl.getRoot().setClickable(false);
        if (node.getCrpl() != null) {
            binding.containerCrpl.text.setText(CompositionDataParser.formatReplayProtectionCount(node.getCrpl().shortValue(), false));
        } else {
            binding.containerCrpl.text.setText(R.string.unavailable);
        }

        binding.containerFeatures.getRoot().setClickable(false);
        if (node.getNodeFeatures() != null) {
            binding.containerFeatures.text.setText(parseFeatures(node.getNodeFeatures()));
        } else {
            binding.containerFeatures.text.setText(R.string.unavailable);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    /**
     * Returns a String representation of the features
     */
    private String parseFeatures(final Features features) {
        return "Relay feature: " +
                (features.isRelayFeatureSupported() ? isEnabled(features.getRelay()) : "Unsupported") +
                "\nProxy feature: " +
                (features.isProxyFeatureSupported() ? isEnabled(features.getProxy()) : "Unsupported") +
                "\nFriend feature: " +
                (features.isFriendFeatureSupported() ? isEnabled(features.getFriend()) : "Unsupported") +
                "\nLow power feature: " +
                (features.isLowPowerFeatureSupported() ? isEnabled(features.getLowPower()) : "Unsupported");
    }

    public String isEnabled(final int feature) {
        return feature == Features.ENABLED ? "Enabled" : "Disabled";
    }
}
