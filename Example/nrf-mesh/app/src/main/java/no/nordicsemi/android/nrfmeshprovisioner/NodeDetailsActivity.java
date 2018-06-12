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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;

import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.CompanyIdentifiers;
import no.nordicsemi.android.meshprovisioner.utils.CompositionDataParser;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ElementAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ElementAdapterDetails;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentGlobalTtl;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;

public class NodeDetailsActivity extends AppCompatActivity implements Injectable, ElementAdapterDetails.OnItemClickListener {

    private final static String TAG = NodeDetailsActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_details);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        final ProvisionedMeshNode node = intent.getParcelableExtra(Utils.EXTRA_DEVICE);
        if(node == null)
            finish();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(node.getNodeName());

        final View containerNodeName = findViewById(R.id.container_name);
        final TextView nodeName = containerNodeName.findViewById(R.id.text);
        nodeName.setText(node.getNodeName());

        final View containerProvisioningTimeStamp = findViewById(R.id.container_timestamp);
        final TextView timestamp = containerProvisioningTimeStamp.findViewById(R.id.text);
        final String format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(node.getTimeStamp());
        timestamp.setText(format);

        final View containerNodeIdentifier = findViewById(R.id.container_identifier);
        final TextView nodeIdentifier = containerNodeIdentifier.findViewById(R.id.text);
        nodeIdentifier.setText(node.getNodeIdentifier());

        final View containerUnicastAddress = findViewById(R.id.container_unicast_address);
        final TextView unicastAddress = containerUnicastAddress.findViewById(R.id.text);
        unicastAddress.setText(MeshParserUtils.bytesToHex(node.getUnicastAddress(), false));

        final View containerCompanyIdentifier = findViewById(R.id.container_company_identifier);
        final TextView companyIdentifier = containerCompanyIdentifier.findViewById(R.id.text);
        companyIdentifier.setText(CompanyIdentifiers.getCompanyName((short) node.getCompanyIdentifier()));

        final View containerProductIdentifier = findViewById(R.id.container_product_identifier);
        final TextView productIdentifier = containerProductIdentifier.findViewById(R.id.text);
        productIdentifier.setText(CompositionDataParser.formatProductIdentifier(node.getProductIdentifier(), false));

        final View containerProductVersion = findViewById(R.id.container_product_version);
        final TextView productVersion = containerProductVersion.findViewById(R.id.text);
        productVersion.setText(CompositionDataParser.formatVersionIdentifier(node.getVersionIdentifier(), false));

        node.getVersionIdentifier();
        final View containerCrpl = findViewById(R.id.container_crpl);
        final TextView crpl = containerCrpl.findViewById(R.id.text);
        crpl.setText(CompositionDataParser.formatReplayProtectionCount(node.getCrpl(), false));

        final View containerFeatures = findViewById(R.id.container_features);
        final TextView features = containerFeatures.findViewById(R.id.text);
        features.setText(CompositionDataParser.formatFeatures(node.getFeatures(), false));

        final TextView view =  findViewById(R.id.no_elements_view);
        mRecyclerView = findViewById(R.id.recycler_view_elements);
        if(node.getElements().isEmpty()){
            view.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            view.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            final ElementAdapterDetails adapter = new ElementAdapterDetails(this, node);
            adapter.setOnItemClickListener(this);
            mRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onItemClick(final int position) {
        mRecyclerView.scrollToPosition(position);
    }
}
