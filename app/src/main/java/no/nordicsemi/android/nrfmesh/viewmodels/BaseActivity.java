package no.nordicsemi.android.nrfmesh.viewmodels;

import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentTransactionStatus;
import no.nordicsemi.android.nrfmesh.utils.Utils;


public abstract class BaseActivity extends AppCompatActivity {

    protected BaseViewModel mViewModel;
    protected Handler mHandler;
    protected boolean mIsConnected;

    protected abstract void updateClickableViews();

    protected abstract void showProgressBar();

    protected abstract void hideProgressBar();

    protected abstract void enableClickableViews();

    protected abstract void disableClickableViews();

    protected final boolean checkConnectivity(final CoordinatorLayout container) {
        if (!mIsConnected) {
            mViewModel.displayDisconnectedSnackBar(this, container);
            return false;
        }
        return true;
    }

    /**
     * Update the mesh message
     *
     * @param meshMessage {@link MeshMessage} mesh message status
     */
    protected abstract void updateMeshMessage(final MeshMessage meshMessage);

    protected final Runnable mRunnableOperationTimeout = () -> {
        hideProgressBar();
        mViewModel.getMessageQueue().clear();
        if (mViewModel.isActivityVisible()) {
            DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.
                    newInstance(getString(R.string.title_transaction_failed), getString(R.string.operation_timed_out));
            fragmentMessage.show(getSupportFragmentManager(), null);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (mIsConnected) {
            getMenuInflater().inflate(R.menu.disconnect, menu);
        } else {
            getMenuInflater().inflate(R.menu.connect, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_connect) {
            mViewModel.navigateToScannerActivity(this, false, Utils.CONNECT_TO_NETWORK, false);
            return true;
        } else if (id == R.id.action_disconnect) {
            mViewModel.disconnect();
            return true;
        }
        return false;
    }

    protected final void initialize() {
        mHandler = new Handler(Looper.getMainLooper());
        mViewModel.isConnectedToProxy().observe(this, isConnected -> {
            if (isConnected != null) {
                mIsConnected = isConnected;
                hideProgressBar();
                updateClickableViews();
            }
            invalidateOptionsMenu();
        });

        mViewModel.getMeshMessage().observe(this, this::updateMeshMessage);

        final Boolean isConnectedToNetwork = mViewModel.isConnectedToProxy().getValue();
        if (isConnectedToNetwork != null) {
            mIsConnected = isConnectedToNetwork;
        }

        mViewModel.getTransactionStatus().observe(this, transactionStatus -> {
            if (transactionStatus != null) {
                hideProgressBar();
                final String message;
                if (transactionStatus.isIncompleteTimerExpired()) {
                    message = getString(R.string.segments_not_received_timed_out);
                } else {
                    message = getString(R.string.operation_timed_out);
                }
                DialogFragmentTransactionStatus fragmentMessage = DialogFragmentTransactionStatus.newInstance(getString(R.string.title_transaction_failed), message);
                fragmentMessage.show(getSupportFragmentManager(), null);
            }
        });

        invalidateOptionsMenu();
    }
}
