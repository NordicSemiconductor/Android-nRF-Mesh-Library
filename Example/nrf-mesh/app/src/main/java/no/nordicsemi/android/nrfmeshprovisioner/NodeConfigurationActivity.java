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
import android.support.v7.widget.DefaultItemAnimator;
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
import no.nordicsemi.android.nrfmeshprovisioner.adapter.AddedAppKeyAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ElementAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.di.Injectable;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentAppKeyAddStatus;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentResetNode;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.nrfmeshprovisioner.viewmodels.NodeConfigurationViewModel;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmeshprovisioner.widgets.RemovableViewHolder;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DATA_MODEL_NAME;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DEVICE;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_ELEMENT_ADDRESS;
import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_MODEL_ID;

public class NodeConfigurationActivity extends AppCompatActivity implements Injectable,
        ElementAdapter.OnItemClickListener, DialogFragmentAppKeyAddStatus.DialogFragmentAppKeyAddStatusListener, DialogFragmentResetNode.DialogFragmentNodeResetListener,
        AddedAppKeyAdapter.OnItemClickListener, ItemTouchHelperAdapter {

    private final static String TAG = NodeConfigurationActivity.class.getSimpleName();
    private static final String DIALOG_FRAGMENT_APP_KEY_STATUS = "DIALOG_FRAGMENT_APP_KEY_STATUS";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @BindView(R.id.recycler_view_elements)
    RecyclerView mRecyclerViewElements;
    @BindView(R.id.composition_data_card)
    CardView mCompostionDataCard;
    private NodeConfigurationViewModel mViewModel;
    private AddedAppKeyAdapter mAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesh_node_configuration);
        ButterKnife.bind(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(NodeConfigurationViewModel.class);

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
        final Button actionResetNode = findViewById(R.id.action_reset_node);
        final TextView noElementsFound = findViewById(R.id.no_elements);
        final TextView noAppKeysFound = findViewById(R.id.no_app_keys);
        final View compositionActionContainer = findViewById(R.id.composition_action_container);
        mRecyclerViewElements.setLayoutManager(new LinearLayoutManager(this));
        final ElementAdapter adapter = new ElementAdapter(this, mViewModel.getExtendedMeshNode());
        adapter.setHasStableIds(true);
        adapter.setOnItemClickListener(this);
        mRecyclerViewElements.setAdapter(adapter);


        final RecyclerView recyclerViewAppKeys = findViewById(R.id.recycler_view_app_keys);
        recyclerViewAppKeys.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAppKeys.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new AddedAppKeyAdapter(this, mViewModel.getExtendedMeshNode());
        recyclerViewAppKeys.setAdapter(mAdapter);

        mViewModel.getExtendedMeshNode().observe(this, extendedMeshNode -> {
            if(extendedMeshNode.getMeshNode() == null) {
                finish();
                return;
            }

            if (extendedMeshNode.hasElements()) {
                compositionActionContainer.setVisibility(View.GONE);
                noElementsFound.setVisibility(View.INVISIBLE);
                mRecyclerViewElements.setVisibility(View.VISIBLE);
            } else {
                noElementsFound.setVisibility(View.VISIBLE);
                compositionActionContainer.setVisibility(View.VISIBLE);
                mRecyclerViewElements.setVisibility(View.INVISIBLE);
            }

            if (extendedMeshNode.hasAddedAppKeys()) {
                final Map<Integer, String> appKeys = extendedMeshNode.getMeshNode().getAddedAppKeys();
                if (!appKeys.isEmpty()) {
                    noAppKeysFound.setVisibility(View.GONE);
                    recyclerViewAppKeys.setVisibility(View.VISIBLE);
                } else {
                    noAppKeysFound.setVisibility(View.VISIBLE);
                    recyclerViewAppKeys.setVisibility(View.GONE);
                }
            }
        });

        getCompostionData.setOnClickListener(v -> mViewModel.sendGetCompositionData());

        actionAddAppkey.setOnClickListener(v -> {
            final Map<Integer, String> appKeys = mViewModel.getProvisioningData().getAppKeys();
            final Intent addAppKeys = new Intent(NodeConfigurationActivity.this, ManageNodeAppKeysActivity.class);
            addAppKeys.putExtra(ManageAppKeysActivity.APP_KEYS, new ArrayList<>(appKeys.values()));
            startActivityForResult(addAppKeys, ManageAppKeysActivity.SELECT_APP_KEY);
        });

        actionResetNode.setOnClickListener(v -> {
            final DialogFragmentResetNode resetNodeFragment = DialogFragmentResetNode.
                    newInstance(getString(R.string.title_reset_node), getString(R.string.reset_node_rationale_summary));
            resetNodeFragment.show(getSupportFragmentManager(), null);
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
    public void onElementItemClick(final ProvisionedMeshNode meshNode, final Element element, final MeshModel model) {
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

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {

    }

    @Override
    public void onItemClick(final String appKey) {

    }

    @Override
    public void onNodeReset() {
        final ProvisionedMeshNode provisionedMeshNode = mViewModel.getExtendedMeshNode().getMeshNode();
        mViewModel.resetNode(provisionedMeshNode);
    }
}
