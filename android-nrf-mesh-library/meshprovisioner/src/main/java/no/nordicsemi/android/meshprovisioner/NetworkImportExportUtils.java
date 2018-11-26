package no.nordicsemi.android.meshprovisioner;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.ElementListDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.MeshModelListDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.NodeDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

/**
 * Utility class to handle network imports and exports
 */
class NetworkImportExportUtils {

    private static final String EXPORT_PATH = File.separator + "Nordic Semiconductor" + File.separator + "nRF Mesh" + File.separator;

    /**
     * Creates an AsyncTask to import the a m
     *
     * @param context   context
     * @param uri       file uri
     * @param callbacks internal callbacks to notify network import
     */
    static void importMeshNetwork(final Context context, final Uri uri, final LoadNetworkCallbacks callbacks) {
        new NetworkImportAsyncTask(context, uri, callbacks).execute();
    }

    /**
     * AsyncTask that reads and import a mesh network from the MeshCDB Json file
     */
    private static class NetworkImportAsyncTask extends AsyncTask<Void, Void, Void> {

        private static final String TAG = NetworkImportAsyncTask.class.getSimpleName();
        private WeakReference<Context> context;
        private final Uri uri;
        private final LoadNetworkCallbacks callbacks;
        private MeshNetwork network;
        private String error;

        /**
         * Creates an AsyncTask to import the a m
         *
         * @param context   context
         * @param uri       file uri
         * @param callbacks internal callbacks to notify network import
         */
        NetworkImportAsyncTask(final Context context, final Uri uri, final LoadNetworkCallbacks callbacks) {
            this.context = new WeakReference<>(context);
            this.uri = uri;
            this.callbacks = callbacks;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(final Void... voids) {
            importNetwork();
            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
            super.onPostExecute(aVoid);
            if (network != null) {
                callbacks.onNetworkImportedFromJson(network);
            } else {
                callbacks.onNetworkLoadFailed(error);
            }
        }

        /**
         * Imports the network from the MeshCDB json file
         */
        private void importNetwork() {
            try {

                Type netKeyList = new TypeToken<List<NetworkKey>>() {
                }.getType();
                Type appKeyList = new TypeToken<List<ApplicationKey>>() {
                }.getType();
                Type allocatedUnicastRange = new TypeToken<List<AllocatedUnicastRange>>() {
                }.getType();
                Type allocatedGroupRange = new TypeToken<List<AllocatedGroupRange>>() {
                }.getType();
                Type allocatedSceneRange = new TypeToken<List<AllocatedSceneRange>>() {
                }.getType();
                Type provisionerList = new TypeToken<List<Provisioner>>() {
                }.getType();
                Type nodeList = new TypeToken<List<ProvisionedMeshNode>>() {
                }.getType();
                Type meshModelList = new TypeToken<List<MeshModel>>() {
                }.getType();
                Type elementList = new TypeToken<List<Element>>() {
                }.getType();

                final GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(MeshNetwork.class, new MeshNetworkDeserializer());
                gsonBuilder.registerTypeAdapter(netKeyList, new NetKeyDeserializer());
                gsonBuilder.registerTypeAdapter(appKeyList, new AppKeyDeserializer());
                gsonBuilder.registerTypeAdapter(provisionerList, new ProvisionerDeserializer());
                gsonBuilder.registerTypeAdapter(allocatedUnicastRange, new AllocatedUnicastRangeDeserializer());
                gsonBuilder.registerTypeAdapter(allocatedGroupRange, new AllocatedGroupRangeDeserializer());
                gsonBuilder.registerTypeAdapter(allocatedSceneRange, new AllocatedSceneRangeDeserializer());
                gsonBuilder.registerTypeAdapter(nodeList, new NodeDeserializer());
                gsonBuilder.registerTypeAdapter(elementList, new ElementListDeserializer());
                gsonBuilder.registerTypeAdapter(meshModelList, new MeshModelListDeserializer());
                final Gson gson = gsonBuilder.create();

                final String json = readJsonStringFromUri();
                final MeshNetwork network = gson.fromJson(json, MeshNetwork.class);
                if (network != null) {
                    this.network = network;
                }
            } catch (final com.google.gson.JsonSyntaxException ex) {
                error = ex.getMessage();
                Log.e(TAG, " " + error);
            } catch (final IOException e) {
                error = e.getMessage();
                Log.e(TAG, " " + error);
            }
        }

        /**
         * Reads the complete json string from URI
         *
         * @return json string
         * @throws IOException in case of failure
         */
        private String readJsonStringFromUri() throws IOException {
            final StringBuilder stringBuilder = new StringBuilder();
            final InputStream inputStream = context.get().getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(
                        inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                inputStream.close();
            }
            return stringBuilder.toString();
        }
    }


    /**
     * AsyncTask that reads and import a mesh network from the MeshCDB Json file
     */
    private static class NetworkExportAsyncTask extends AsyncTask<Void, Void, Void> {

        private static final String TAG = NetworkImportAsyncTask.class.getSimpleName();
        private WeakReference<Context> context;
        private final Uri uri;
        private final LoadNetworkCallbacks callbacks;
        private MeshNetwork network;
        private String error;

        /**
         * Creates an AsyncTask to import the a m
         *
         * @param context   context
         * @param uri       file uri
         * @param callbacks internal callbacks to notify network import
         */
        NetworkExportAsyncTask(final Context context, final Uri uri, final LoadNetworkCallbacks callbacks) {
            this.context = new WeakReference<>(context);
            this.uri = uri;
            this.callbacks = callbacks;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(final Void... voids) {
            exportNetwork();
            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
            super.onPostExecute(aVoid);
            if (network != null) {
                callbacks.onNetworkImportedFromJson(network);
            } else {
                callbacks.onNetworkLoadFailed(error);
            }
        }

        /**
         * Imports the network from the MeshCDB json file
         */
        private void exportNetwork() {
            final String path = Environment.getExternalStorageDirectory() + EXPORT_PATH;

            BufferedWriter br = null;
            try {

                Type netKeyList = new TypeToken<List<NetworkKey>>() {
                }.getType();
                Type appKeyList = new TypeToken<List<ApplicationKey>>() {
                }.getType();
                Type nodeList = new TypeToken<List<ProvisionedMeshNode>>() {
                }.getType();
                Type meshModelList = new TypeToken<List<MeshModel>>() {
                }.getType();
                Type elementList = new TypeToken<List<Element>>() {
                }.getType();

                final GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(MeshNetwork.class, new MeshNetworkDeserializer());
                gsonBuilder.registerTypeAdapter(netKeyList, new NetKeyDeserializer());
                gsonBuilder.registerTypeAdapter(appKeyList, new AppKeyDeserializer());
                gsonBuilder.registerTypeAdapter(AllocatedGroupRange.class, new AllocatedGroupRangeDeserializer());
                gsonBuilder.registerTypeAdapter(AllocatedUnicastRange.class, new AllocatedUnicastRangeDeserializer());
                gsonBuilder.registerTypeAdapter(AllocatedSceneRange.class, new AllocatedSceneRangeDeserializer());
                gsonBuilder.registerTypeAdapter(nodeList, new NodeDeserializer());
                gsonBuilder.registerTypeAdapter(elementList, new ElementListDeserializer());
                gsonBuilder.registerTypeAdapter(meshModelList, new MeshModelListDeserializer());
                final Gson gson = gsonBuilder.create();

                final File directory = new File(path);
                if (!directory.exists()) {
                    if (!directory.mkdir()) {
                        return;
                    }
                }

                final File f = new File(path, "example_database.json");
                br = new BufferedWriter(new FileWriter(f));
                final String network = gson.toJson(MeshNetwork.class);
                final OutputStream outputStream = context.get().openFileOutput(f.getName(), Context.MODE_PRIVATE);
                outputStream.write(network.getBytes());
                outputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
