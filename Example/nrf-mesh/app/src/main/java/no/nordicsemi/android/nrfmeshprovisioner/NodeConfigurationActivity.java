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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.configuration.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.Element;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ElementAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentAppKeyAddStatus;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentMessage;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.ElementConfigurationViewModel;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DATA_MODEL_NAME;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DEVICE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_ELEMENT_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_MODEL_ID;

public class NodeConfigurationActivity extends AppCompatActivity implements Injectable,
        ElementAdapter.OnItemClickListener, DialogFragmentAppKeyAddStatus.DialogFragmentAppKeyAddStatusListener {

    private final static String TAG = NodeConfigurationActivity.class.getSimpleName();
    private static final String DIALOG_FRAGMENT_APP_KEY_STATUS = "DIALOG_FRAGMENT_APP_KEY_STATUS";

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
        if(savedInstanceState == null) {
            if (node == null)
                finish();
            mViewModel.setMeshNode(node);
        }

        // Set up views
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_node_configuration);
        getSupportActionBar().setSubtitle(node.getNodeName());

        final Button getCompostionData = findViewById(R.id.action_get_compostion_data);
        final Button actionAddAppkey = findViewById(R.id.action_add_app_keys);
        final Button actionViewAdddedAppKeys = findViewById(R.id.action_view_app_keys);
        final TextView noElementsFound = findViewById(R.id.no_elements);
        final View compositionActionContainer = findViewById(R.id.composition_action_container);
        final TextView appKeyView = findViewById(R.id.app_key_index);
        mRecyclerViewElements.setLayoutManager(new LinearLayoutManager(this));
        final ElementAdapter adapter = new ElementAdapter(this, mViewModel.getExtendedMeshNode());
        adapter.setHasStableIds(true);
        adapter.setOnItemClickListener(this);
        mRecyclerViewElements.setAdapter(adapter);

        mViewModel.getExtendedMeshNode().observe(this, extendedMeshNode -> {
            if(extendedMeshNode.hasElements()){
                compositionActionContainer.setVisibility(View.GONE);
                noElementsFound.setVisibility(View.INVISIBLE);
                mRecyclerViewElements.setVisibility(View.VISIBLE);
            } else {
                noElementsFound.setVisibility(View.VISIBLE);
                compositionActionContainer.setVisibility(View.VISIBLE);
                mRecyclerViewElements.setVisibility(View.INVISIBLE);
            }

            if(extendedMeshNode.hasAddedAppKeys()){
                final Map<Integer, String> appKeys = extendedMeshNode.getMeshNode().getAddedAppKeys();
                if (!appKeys.isEmpty()) {
                    actionViewAdddedAppKeys.setVisibility(View.VISIBLE);
                    appKeyView.setText(getString(R.string.app_keys_added, appKeys.size()));
                } else {
                    actionViewAdddedAppKeys.setVisibility(View.INVISIBLE);
                    appKeyView.setText(getString(R.string.no_app_keys_added));
                }
            }
        });

        getCompostionData.setOnClickListener(v -> {
            mViewModel.sendGetCompositionData();
        });

        actionAddAppkey.setOnClickListener(v -> {
            final Map<Integer, String> appKeys = mViewModel.getProvisioningData().getAppKeys();
            final Intent addAppKeys = new Intent(NodeConfigurationActivity.this, ManageNodeAppKeysActivity.class);
            addAppKeys.putExtra(ManageAppKeysActivity.APP_KEYS, new ArrayList<>(appKeys.values()));
            startActivityForResult(addAppKeys, ManageAppKeysActivity.SELECT_APP_KEY);
        });

        actionViewAdddedAppKeys.setOnClickListener(v -> {
            final Intent viewAppKeysIntent = new Intent(NodeConfigurationActivity.this, ViewAddedAppKeysActivity.class);
            final ProvisionedMeshNode meshNode = mViewModel.getExtendedMeshNode().getMeshNode();
            viewAppKeysIntent.putExtra(ManageAppKeysActivity.APP_KEYS, new ArrayList<>(meshNode.getAddedAppKeys().values()));
            startActivity(viewAppKeysIntent);
        });

        mViewModel.getAppKeyAddStatus().observe(this, appKeyStatusLiveData -> {
            if (getSupportFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_APP_KEY_STATUS) == null) {
                if(!appKeyStatusLiveData.isSuccess()) {
                    final DialogFragmentAppKeyAddStatus fragmentAppKeyAddStatus = DialogFragmentAppKeyAddStatus.
                            newInstance(getString(R.string.title_appkey_status), ConfigAppKeyStatus.parseStatusMessage(this, appKeyStatusLiveData.getStatus()));
                    fragmentAppKeyAddStatus.show(getSupportFragmentManager(), DIALOG_FRAGMENT_APP_KEY_STATUS);
                }
            }
        });

        mViewModel.isConnected().observe(this, isConnected -> {
            if(isConnected != null && !isConnected)
                finish();
        });

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
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ManageAppKeysActivity.SELECT_APP_KEY){
            if(resultCode == RESULT_OK){
                final String appKey = data.getStringExtra(ManageAppKeysActivity.RESULT_APP_KEY);
                final int appKeyIndex = data.getIntExtra(ManageAppKeysActivity.RESULT_APP_KEY_INDEX, -1);
                if(appKey != null){
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

    @Override
    public void onAppKeyAddStatusReceived() {

    }
}
