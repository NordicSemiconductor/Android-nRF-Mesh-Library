package no.nordicsemi.android.nrfmesh.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.MeshManagerApi;

public class NetworkExportUtils {

    public interface NetworkExportCallbacks {

        void onNetworkExported();

        void onNetworkExportFailed(final String error);
    }

    /**
     * Creates an AsyncTask to exoirt the
     *
     * @param meshManagerApi Mesh manager api
     * @param outputStream   OutputStream obtained from the content resolver
     * @param callbacks      {@link NetworkExportCallbacks}
     */
    public static void exportMeshNetwork(@NonNull final MeshManagerApi meshManagerApi,
                                         @NonNull final OutputStream outputStream,
                                         @NonNull NetworkExportCallbacks callbacks) {
        final NetworkExportAsyncTask task = new NetworkExportAsyncTask(meshManagerApi, outputStream, callbacks);
        task.execute();
    }

    /**
     * Creates an AsyncTask to import the a m
     *
     * @param meshManagerApi Mesh manager api
     * @param path           OutputStream obtained from the content resolver
     * @param callbacks      {@link NetworkExportCallbacks}
     */
    public static void exportMeshNetwork(@NonNull final MeshManagerApi meshManagerApi,
                                         @NonNull final String path,
                                         @NonNull final String fileName,
                                         @NonNull NetworkExportCallbacks callbacks) {
        final NetworkExportAsyncTask2 task = new NetworkExportAsyncTask2(meshManagerApi, path, fileName, callbacks);
        task.execute();
    }

    private static class NetworkExportAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private static final String TAG = NetworkExportUtils.NetworkExportAsyncTask.class.getSimpleName();
        private final MeshManagerApi meshManagerApi;
        private final OutputStream outputStream;
        private final NetworkExportCallbacks callbacks;
        private String error;

        /**
         * Creates an AsyncTask to import the a m
         *
         * @param meshManagerApi Mesh manager api
         */
        NetworkExportAsyncTask(@NonNull final MeshManagerApi meshManagerApi,
                               @NonNull final OutputStream outputStream,
                               @NonNull NetworkExportCallbacks callbacks) {
            this.meshManagerApi = meshManagerApi;
            this.outputStream = outputStream;
            this.callbacks = callbacks;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(final Void... voids) {
            final String network = meshManagerApi.exportMeshNetwork();
            if (network == null)
                return false;
            try {
                outputStream.write(network.getBytes());
                outputStream.close();
                return true;
            } catch (IOException e) {
                error = e.getMessage();
                Log.e(TAG, "Exception ex: " + error);
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean aVoid) {
            super.onPostExecute(aVoid);
            if (aVoid) {
                callbacks.onNetworkExported();
            } else {
                callbacks.onNetworkExportFailed(error);
            }
        }
    }

    private static class NetworkExportAsyncTask2 extends AsyncTask<Void, Void, Boolean> {

        private static final String TAG = NetworkExportUtils.NetworkExportAsyncTask.class.getSimpleName();
        private final MeshManagerApi meshManagerApi;
        private final String path;
        private final String fileName;
        private final NetworkExportCallbacks callbacks;
        private String error;

        /**
         * Creates an AsyncTask to import the a m
         *
         * @param meshManagerApi Mesh manager api
         */
        NetworkExportAsyncTask2(@NonNull final MeshManagerApi meshManagerApi,
                                @NonNull final String path,
                                @NonNull final String fileName,
                                @NonNull NetworkExportCallbacks callbacks) {
            this.meshManagerApi = meshManagerApi;
            this.path = path;
            this.fileName = fileName;
            this.callbacks = callbacks;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(final Void... voids) {
            final String network = meshManagerApi.exportMeshNetwork();
            if (network == null)
                return false;
            try {
                final File directory = new File(path);
                if (!directory.exists()) {
                    if (!directory.mkdirs()) {
                        error = "Unable to create file";
                        return false;
                    }
                }
                final File file = new File(path, fileName);
                final BufferedWriter br = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
                br.write(network);
                br.flush();
                br.close();
                return true;
            } catch (IOException e) {
                error = e.getMessage();
                Log.e(TAG, "Exception ex: " + error);
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean aVoid) {
            super.onPostExecute(aVoid);
            if (aVoid) {
                callbacks.onNetworkExported();
            } else {
                callbacks.onNetworkExportFailed(error);
            }
        }
    }
}
