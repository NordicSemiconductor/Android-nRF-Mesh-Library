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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ElementAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ElementConfigurationViewModel;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DATA_MODEL_NAME;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DEVICE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_ELEMENT_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_MODEL_ID;

public class ElementConfigurationActivity extends AppCompatActivity implements Injectable,
        ElementAdapter.OnItemClickListener {

    private final static String TAG = ElementConfigurationActivity.class.getSimpleName();

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @BindView(R.id.recycler_view_elements)
    RecyclerView mRecyclerViewElements;
    @BindView(R.id.composition_data_card)
    CardView mCompostionDataCard;
    private ElementConfigurationViewModel mViewModel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesh_element_configuration);
        ButterKnife.bind(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ElementConfigurationViewModel.class);

        final Intent intent = getIntent();
        final ProvisionedMeshNode node = intent.getParcelableExtra(Utils.EXTRA_DEVICE);
        if(node == null)
            finish();
        mViewModel.setMeshNode(node);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_elements);

        // Set up views
        final Button getCompostionData = findViewById(R.id.action_get_compostion_data);

        mRecyclerViewElements.setLayoutManager(new LinearLayoutManager(this));
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerViewElements.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerViewElements.addItemDecoration(dividerItemDecoration);

        mViewModel.getExtendedMeshNode().observe(this, extendedMeshNode -> {
            if(extendedMeshNode.hasElements()){
                invalidateOptionsMenu();
                mCompostionDataCard.setVisibility(View.INVISIBLE);
                final ElementAdapter adapter = new ElementAdapter(this, mViewModel.getExtendedMeshNode());
                adapter.setOnItemClickListener(this);
                mRecyclerViewElements.setAdapter(adapter);
                mRecyclerViewElements.setVisibility(View.VISIBLE);
            } else {
                mCompostionDataCard.setVisibility(View.VISIBLE);
                mRecyclerViewElements.setVisibility(View.INVISIBLE);
            }
        });

        getCompostionData.setOnClickListener(v -> {
            mViewModel.sendGetCompositionData();
        });

        mViewModel.isConnected().observe(this, isConnected -> {
            if(isConnected != null && !isConnected)
                finish();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final ProvisionedMeshNode node = mViewModel.getExtendedMeshNode().getMeshNode();
        if(node != null && node.getAddedAppKeyIndexes().isEmpty()) {
            getMenuInflater().inflate(R.menu.app_key_add, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_app_key_add:
                final Map<Integer, String> appKeys = mViewModel.getProvisioningData().getAppKeys();
                final Intent manageAppKeys = new Intent(ElementConfigurationActivity.this, ManageAppKeysActivity.class);
                manageAppKeys.putExtra(ManageAppKeysActivity.APP_KEYS, new ArrayList<>(appKeys.values()));
                startActivityForResult(manageAppKeys, ManageAppKeysActivity.SELECT_APP_KEY);
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ManageAppKeysActivity.SELECT_APP_KEY){
            if(resultCode == RESULT_OK){
                final String appKey = data.getStringExtra(ManageAppKeysActivity.RESULT);
                if(appKey != null){
                    /*mViewModel.getProvisioningData().setSelectedAppKey(appKey);
                    mViewModel.getProvisioningData().setSelectedAppKeyIndex(appKeyIndex);*/

                    final int appKeyIndex = Utils.getKey(mViewModel.getProvisioningData().getAppKeys(), appKey);
                    mViewModel.setSelectedAppKey(appKeyIndex, appKey);
                    mViewModel.sendAppKeyAdd(appKeyIndex, appKey);
                }
            }
        }
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
    public void onItemClick(final ProvisionedMeshNode meshNode, final Element element, final MeshModel model) {
        mViewModel.getElementConfigurationRepository().setModel(meshNode, AddressUtils.getUnicastAddressInt(element.getElementAddress()), model.getModelId());
        final Intent intent = new Intent(this, ModelConfigurationActivity.class);
        intent.putExtra(EXTRA_DEVICE, meshNode);
        intent.putExtra(EXTRA_ELEMENT_ADDRESS, AddressUtils.getUnicastAddressInt(element.getElementAddress()));
        intent.putExtra(EXTRA_MODEL_ID, model.getModelId());
        intent.putExtra(EXTRA_DATA_MODEL_NAME, model.getModelName());
        startActivity(intent);
    }
}
