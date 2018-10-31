package no.nordicsemi.android.nrfmeshprovisioner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NetworkTransmitSettingsActivity extends AppCompatActivity {

    /*

     */

    public static final int SET_NETWORK_TRANSMIT_SETTINGS = 4099;

    public static final String TRANSMIT_COUNT = "TRANSMIT_COUNT";
    public static final String TRANSMIT_INTERVAL_STEPS = "TRANSMIT_INTERVAL_STEPS";

    private static final int MIN_TRANSMIT_COUNT = 0;
    private static final int MAX_TRANSMIT_COUNT = 0b111;

    private static final int MIN_TRANSMIT_INTERVAL_STEPS = 0;
    private static final int MAX_TRANSMIT_INTERVAL_STEPS = 0b11111;

    @BindView(R.id.dialog_network_transmit_count)
    TextView networkTransmitCountText;
    @BindView(R.id.dialog_network_transmit_count_seekbar)
    SeekBar networkTransmitCountBar;
    @BindView(R.id.dialog_network_transmit_interval_steps)
    TextView networkTransmitIntervalStepsText;
    @BindView(R.id.dialog_network_transmit_interval_steps_seekbar)
    SeekBar networkTransmitIntervalStepsBar;

    private int mTransmitCount;
    private int mTransmitIntervalSteps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_transmit_settings);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        mTransmitCount = intent.getIntExtra(TRANSMIT_COUNT, 0);
        mTransmitIntervalSteps = intent.getIntExtra(TRANSMIT_INTERVAL_STEPS, 0);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_network_transmit);

        networkTransmitCountBar.setProgress(mTransmitCount);
        networkTransmitCountBar.setMax(MAX_TRANSMIT_COUNT);
        networkTransmitCountBar.incrementProgressBy(1);

        networkTransmitCountText.setText(getTransmitCountString());

        networkTransmitIntervalStepsBar.setProgress(mTransmitIntervalSteps);
        networkTransmitIntervalStepsBar.setMax(MAX_TRANSMIT_INTERVAL_STEPS);
        networkTransmitCountBar.incrementProgressBy(1);

        networkTransmitIntervalStepsText.setText(getTransmitIntervalStepsString());

    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TRANSMIT_COUNT, mTransmitCount);
        outState.putInt(TRANSMIT_INTERVAL_STEPS, mTransmitIntervalSteps);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTransmitCount = savedInstanceState.getInt(TRANSMIT_COUNT);
        mTransmitIntervalSteps = savedInstanceState.getInt(TRANSMIT_INTERVAL_STEPS);
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


    private String getTransmitCountString() {
        return (mTransmitCount + 1) + " " + R.string.unit_network_transmit_count;
    }


    private String getTransmitIntervalStepsString() {
        return (mTransmitIntervalSteps + 1) + " " + R.string.unit_network_transmit_interval_steps;
    }


    private void setReturnIntent(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra(TRANSMIT_COUNT, mTransmitCount);
        returnIntent.putExtra(TRANSMIT_INTERVAL_STEPS, mTransmitIntervalSteps);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

}
