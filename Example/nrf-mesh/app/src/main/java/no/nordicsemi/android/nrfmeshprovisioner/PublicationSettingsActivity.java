package no.nordicsemi.android.nrfmeshprovisioner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.meshprovisioner.configuration.MeshModel;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentPubRetransmitIntervalSteps;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentPublicationResolution;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentPublicationSteps;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentPublishAddress;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentPublishTtl;
import no.nordicsemi.android.nrfmeshprovisioner.dialog.DialogFragmentRetransmitCount;

import static no.nordicsemi.android.nrfmeshprovisioner.utils.Utils.EXTRA_DEVICE;

public class PublicationSettingsActivity extends AppCompatActivity implements DialogFragmentPublishAddress.DialogFragmentPublishAddressListener,
        DialogFragmentPublicationSteps.DialogFragmentPublicationStepsListener,
        DialogFragmentPublicationResolution.DialogFragmentPublicationResolutionListener,
        DialogFragmentRetransmitCount.DialogFragmentRetransmitCountListener,
        DialogFragmentPubRetransmitIntervalSteps.DialogFragmentIntervalStepsListener,
        DialogFragmentPublishTtl.DialogFragmentPublishTtlListener {

    public static final int SET_PUBLICATION_SETTINGS = 2021;
    public static final String RESULT_PUBLISH_ADDRESS                       = "RESULT_PUBLISH_ADDRESS";
    public static final String RESULT_APP_KEY_INDEX                         = "RESULT_APP_KEY_INDEX";
    public static final String RESULT_CREDENTIAL_FLAG                       = "RESULT_CREDENTIAL_FLAG";
    public static final String RESULT_PUBLISH_TTL                           = "RESULT_PUBLISH_TTL";
    public static final String RESULT_PUBLICATION_STEPS                     = "RESULT_PUBLICATION_STEPS";
    public static final String RESULT_PUBLICATION_RESOLUTION                = "RESULT_PUBLICATION_RESOLUTION";
    public static final String RESULT_PUBLISH_RETRANSMIT_COUNT              = "RESULT_PUBLISH_RETRANSMIT_COUNT";
    public static final String RESULT_PUBLISH_RETRANSMIT_INTERVAL_STEPS     = "RESULT_PUBLISH_RETRANSMIT_INTERVAL_STEPS";

    private static final int DEFAULT_PUB_RETRANSMIT_COUNT = 1;
    private static final int DEFAULT_PUB_RETRANSTMI_INTERVAL_STEPS = 1;
    private static final int DEFAULT_PUBLICATION_STEPS = 0;

    private MeshModel mMeshModel;
    private byte[] mPublishAddress;
    private int mAppKeyIndex;
    private boolean mCredentialFlag;
    private int mPublishTtl = MeshParserUtils.DEFAULT_TTL;
    private int mPublicationSteps;
    private int mPublicationResolution;
    private int mPublishRetransmitCount;
    private int mPublishRetransmitIntervalSteps;

    @BindView(R.id.publish_address)
    TextView mPublishAddressView;
    @BindView(R.id.retransmit_count)
    TextView mRetransmitCountView;
    @BindView(R.id.interval_steps)
    TextView mIntervalStepsView;
    @BindView(R.id.publication_steps)
    TextView mPublicationStepsView;
    @BindView(R.id.publication_resolution)
    TextView mPublicationResolutionView;
    @BindView(R.id.app_key_index)
    TextView mAppKeyIndexView;
    @BindView(R.id.publication_ttl)
    TextView mPublishTtlView;
    @BindView(R.id.friendship_credential_flag)
    Switch mActionFriendshipCredentialSwitch;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publication_settings);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        final MeshModel meshModel = mMeshModel = intent.getParcelableExtra(EXTRA_DEVICE);
        if(meshModel == null)
            finish();

        //Setup views
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_publication_settings);

        final View actionPublishAddress = findViewById(R.id.container_publish_address);
        final View actionRetransmitCount = findViewById(R.id.container_retransmission_count);
        final View actionIntervalSteps = findViewById(R.id.container_interval_steps);
        final View actionPublicationSteps= findViewById(R.id.container_publish_steps);
        final View actionResolution = findViewById(R.id.container_resolution);
        final View actionKeyIndex = findViewById(R.id.container_app_key_index);
        final View actionPublishTtl = findViewById(R.id.container_publication_ttl);

        actionPublishAddress.setOnClickListener(v -> {
            final byte[] publishAddress = mMeshModel.getPublishAddress();
            final DialogFragmentPublishAddress fragmentPublishAddress = DialogFragmentPublishAddress.newInstance(publishAddress);
            fragmentPublishAddress.show(getSupportFragmentManager(), null);
        });

        actionRetransmitCount.setOnClickListener(v -> {
            final DialogFragmentRetransmitCount fragmentRetransmitCount = DialogFragmentRetransmitCount.newInstance(DEFAULT_PUB_RETRANSMIT_COUNT);
            fragmentRetransmitCount.show(getSupportFragmentManager(), null);
        });

        actionIntervalSteps.setOnClickListener(v -> {
            final DialogFragmentPubRetransmitIntervalSteps fragmentIntervalSteps = DialogFragmentPubRetransmitIntervalSteps.newInstance(DEFAULT_PUB_RETRANSTMI_INTERVAL_STEPS);
            fragmentIntervalSteps.show(getSupportFragmentManager(), null);
        });

        actionPublicationSteps.setOnClickListener(v -> {
            final DialogFragmentPublicationSteps fragmentPublicationSteps = DialogFragmentPublicationSteps.newInstance(DEFAULT_PUBLICATION_STEPS);
            fragmentPublicationSteps.show(getSupportFragmentManager(), null);
        });

        actionResolution.setOnClickListener(v -> {
            final DialogFragmentPublicationResolution fragmentPublicationResolution = DialogFragmentPublicationResolution.newInstance(MeshParserUtils.RESOLUTION_100_MS);
            fragmentPublicationResolution.show(getSupportFragmentManager(), null);
        });

        actionKeyIndex.setOnClickListener(v -> {
            final Intent bindAppKeysIntent = new Intent(this, BindAppKeysActivity.class);
            bindAppKeysIntent.putExtra(ManageAppKeysActivity.APP_KEYS, (Serializable) meshModel.getBoundAppkeys());
            startActivityForResult(bindAppKeysIntent, ManageAppKeysActivity.SELECT_APP_KEY);
        });

        actionPublishTtl.setOnClickListener(v -> {
            final DialogFragmentPublishTtl fragmentPublishTtl = DialogFragmentPublishTtl.newInstance(MeshParserUtils.DEFAULT_TTL);
            fragmentPublishTtl.show(getSupportFragmentManager(), null);
        });

        updateUi(meshModel);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.publication_apply, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_apply:
                setReturnIntent();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ManageAppKeysActivity.SELECT_APP_KEY){
            if(resultCode == RESULT_OK){
                final String appKey = data.getStringExtra(ManageAppKeysActivity.RESULT_APP_KEY);
                final int appKeyIndex = data.getIntExtra(ManageAppKeysActivity.RESULT_APP_KEY_INDEX, -1);
                if(appKey != null){
                    mAppKeyIndexView.setText(getString(R.string.app_key_index, appKeyIndex));
                }
            }
        }
    }

    @Override
    public void setPublishAddress(final byte[] publishAddress) {
        if(publishAddress!= null){
            mPublishAddress = publishAddress;
            mPublishAddressView.setText(MeshParserUtils.bytesToHex(publishAddress, true));
        }
    }

    @Override
    public void setPublicationResolution(final int resolution) {
        mPublicationResolution = resolution;
        mPublicationResolutionView.setText(getResolutionSummary(resolution));
    }

    @Override
    public void setPublicationSteps(final int publicationSteps) {
        mPublicationSteps = publicationSteps;
        mPublicationStepsView.setText(getString(R.string.publication_steps, publicationSteps));
    }

    @Override
    public void setRetransmitCount(final int retransmitCount) {
        mPublishRetransmitCount = retransmitCount;
        mRetransmitCountView.setText(getString(R.string.retransmit_count, retransmitCount));
    }

    @Override
    public void setRetransmitIntervalSteps(final int intervalSteps) {
        mPublishRetransmitIntervalSteps = intervalSteps;
        mIntervalStepsView.setText(getString(R.string.retransmit_interval_steps, intervalSteps));
    }

    @Override
    public void setPublishTtl(final int ttl) {
        mPublishTtl = ttl;
        mPublishTtlView.setText(String.valueOf(ttl));
    }

    private void updateUi(final MeshModel model){
        mPublishAddress = model.getPublishAddress();
        if(mPublishAddress != null) {
            mPublishAddressView.setText(MeshParserUtils.bytesToHex(mPublishAddress, true));
        }

        if(!model.getBoundAppKeyIndexes().isEmpty() && mMeshModel.getPublishAppKeyIndexInt() != null){
            mAppKeyIndex = mMeshModel.getPublishAppKeyIndexInt();
            mAppKeyIndexView.setText(getString(R.string.app_key_index, mAppKeyIndex));
        }

        mActionFriendshipCredentialSwitch.setChecked(mMeshModel.getCredentialFlag() == 1);
        mPublishTtlView.setText(String.valueOf(mMeshModel.getPublishTtl()));
        mPublicationStepsView.setText(getString(R.string.publication_steps, mMeshModel.getPublicationSteps()));
        mPublicationResolutionView.setText(getResolutionSummary(mMeshModel.getPublicationResolution()));
        mRetransmitCountView.setText(getString(R.string.retransmit_count, mMeshModel.getPublishRetransmitCount()));
        mIntervalStepsView.setText(getString(R.string.retransmit_interval_steps, mMeshModel.getPublishRetransmitIntervalSteps()));

    }

    private String getResolutionSummary(final int resolution) {
        switch (resolution) {
            default:
            case MeshParserUtils.RESOLUTION_100_MS:
                return getString(R.string.resolution_summary_100_ms);
            case MeshParserUtils.RESOLUTION_1_S:
                return getString(R.string.resolution_summary_1_s);
            case MeshParserUtils.RESOLUTION_10_S:
                return getString(R.string.resolution_summary_10_s);
            case MeshParserUtils.RESOLUTION_10_M:
                return getString(R.string.resolution_summary_100_m);
        }

    }

    private void setReturnIntent(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra(RESULT_PUBLISH_ADDRESS, mPublishAddress);
        returnIntent.putExtra(RESULT_APP_KEY_INDEX, mAppKeyIndex);
        returnIntent.putExtra(RESULT_CREDENTIAL_FLAG, mActionFriendshipCredentialSwitch.isChecked());
        returnIntent.putExtra(RESULT_PUBLISH_TTL, mPublishTtl);
        returnIntent.putExtra(RESULT_PUBLICATION_STEPS, mPublicationSteps);
        returnIntent.putExtra(RESULT_PUBLICATION_RESOLUTION, mPublicationResolution);
        returnIntent.putExtra(RESULT_PUBLISH_RETRANSMIT_COUNT, mPublishRetransmitCount);
        returnIntent.putExtra(RESULT_PUBLISH_RETRANSMIT_INTERVAL_STEPS, mPublishRetransmitIntervalSteps);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
