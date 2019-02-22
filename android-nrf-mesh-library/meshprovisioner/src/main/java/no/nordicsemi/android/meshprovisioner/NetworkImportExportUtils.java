package no.nordicsemi.android.meshprovisioner;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.List;

import no.nordicsemi.android.meshprovisioner.transport.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.InternalElementListDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.MeshModelListDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.NetworkKey;
import no.nordicsemi.android.meshprovisioner.transport.NodeDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

/**
 * Utility class to handle network imports and exports
 */
class NetworkImportExportUtils {

    /**
     * Creates an AsyncTask to import the a mesh network
     *
     * @param context   context
     * @param uri       file path
     * @param callbacks internal callbacks to notify network import
     */
    static void importMeshNetwork(final Context context, final Uri uri, final LoadNetworkCallbacks callbacks) {
        new NetworkImportAsyncTask(context, uri, callbacks).execute();
    }

    /**
     * Creates an AsyncTask to import the a mesh network
     *
     * @param context   context
     * @param networkJson       network json
     * @param callbacks internal callbacks to notify network import
     */
    static void importMeshNetworkFromJson(final Context context, final String networkJson, final LoadNetworkCallbacks callbacks) {
        new NetworkImportAsyncTask(context, networkJson, callbacks).execute();
    }

    /**
     * Creates an AsyncTask to export mesh network
     *
     * @param meshNetwork mesh network to be exported
     * @param path        path where the file should be exported
     * @param callbacks   internal callbacks to notify network export
     */
    static void exportMeshNetwork(final MeshNetwork meshNetwork, final String path, final LoadNetworkCallbacks callbacks) {
        new NetworkExportAsyncTask(meshNetwork, path, callbacks).execute();
    }

    /**
     * AsyncTask that reads and import a mesh network from the Mesh Provisioning/Configuration Database Json file
     */
    private static class NetworkImportAsyncTask extends AsyncTask<Void, Void, Void> {

        private static final String TAG = NetworkImportAsyncTask.class.getSimpleName();
        private WeakReference<Context> context;
        private final Uri uri;
        private final LoadNetworkCallbacks callbacks;
        private MeshNetwork network;
        private String error;
        private final String networkJson;

        /**
         * Creates an AsyncTask to import the a m
         *
         * @param context   context
         * @param uri       file path
         * @param callbacks internal callbacks to notify network import
         */
        NetworkImportAsyncTask(@NonNull final Context context, @NonNull final Uri uri, @NonNull final LoadNetworkCallbacks callbacks) {
            this.context = new WeakReference<>(context);
            this.uri = uri;
            this.networkJson = null;
            this.callbacks = callbacks;
        }

        /**
         * Creates an AsyncTask to import the a m
         *
         * @param context   context
         * @param networkJson  network json
         * @param callbacks internal callbacks to notify network import
         */
        NetworkImportAsyncTask(final Context context, final String networkJson, final LoadNetworkCallbacks callbacks) {
            this.context = new WeakReference<>(context);
            this.networkJson = networkJson;
            this.uri = null;
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
                callbacks.onNetworkImportFailed(error);
            }
        }

        /**
         * Imports the network from the Mesh Provisioning/Configuration Database json file
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
                gsonBuilder.registerTypeAdapter(elementList, new InternalElementListDeserializer());
                gsonBuilder.registerTypeAdapter(meshModelList, new MeshModelListDeserializer());
                final Gson gson = gsonBuilder.create();

                final String json = this.networkJson != null ? this.networkJson : readJsonStringFromUri();
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
     * AsyncTask that reads and import a mesh network from the Mesh Provisioning/Configuration Database Json file
     */
    private static class NetworkExportAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private static final String TAG = NetworkImportAsyncTask.class.getSimpleName();
        private final String path;
        private final LoadNetworkCallbacks callbacks;
        private MeshNetwork network;
        private String error;
        private String networkJson;

        /**
         * Creates an AsyncTask to import the a m
         *
         * @param network   mesh network to be exported
         * @param path      final string path
         * @param callbacks internal callbacks to notify network import
         */
        NetworkExportAsyncTask(final MeshNetwork network, @Nullable final String path, final LoadNetworkCallbacks callbacks) {
            this.network = network;
            this.path = path;
            this.callbacks = callbacks;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(final Void... voids) {
            return exportNetwork();
        }

        @Override
        protected void onPostExecute(final Boolean aVoid) {
            super.onPostExecute(aVoid);
            if (aVoid) {
                if (this.path == null) {
                    callbacks.onNetworkExportedJson(network, this.networkJson);
                } else {
                    callbacks.onNetworkExported(network);
                }
            } else {
                callbacks.onNetworkExportFailed(error);
            }
        }

        /**
         * Exports the network from the Mesh Provisioning/Configuration Database json file
         */
        private boolean exportNetwork() {
            BufferedWriter br = null;
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

                gsonBuilder.registerTypeAdapter(netKeyList, new NetKeyDeserializer());
                gsonBuilder.registerTypeAdapter(appKeyList, new AppKeyDeserializer());
                gsonBuilder.registerTypeAdapter(provisionerList, new ProvisionerDeserializer());
                gsonBuilder.registerTypeAdapter(allocatedUnicastRange, new AllocatedUnicastRangeDeserializer());
                gsonBuilder.registerTypeAdapter(allocatedGroupRange, new AllocatedGroupRangeDeserializer());
                gsonBuilder.registerTypeAdapter(allocatedSceneRange, new AllocatedSceneRangeDeserializer());
                gsonBuilder.registerTypeAdapter(nodeList, new NodeDeserializer());
                gsonBuilder.registerTypeAdapter(elementList, new InternalElementListDeserializer());
                gsonBuilder.registerTypeAdapter(meshModelList, new MeshModelListDeserializer());
                gsonBuilder.registerTypeAdapter(MeshNetwork.class, new MeshNetworkDeserializer());

                final Gson gson = gsonBuilder.create();

                if (this.path != null) {
                    gsonBuilder.setPrettyPrinting();
                    final String networkJson = gson.toJson(network);
                    final String fileName = network.getMeshUUID() + ".json";
                    final File f = new File(path, fileName);
                    br = new BufferedWriter(new FileWriter(f));
                    br.write(networkJson);
                    br.flush();
                    br.close();
                } else {
                    this.networkJson = gson.toJson(network);
                }
                return true;
            } catch (final com.google.gson.JsonSyntaxException ex) {
                error = ex.getMessage();
                Log.e(TAG, " " + error);
                return false;
            } catch (final IOException e) {
                error = e.getMessage();
                Log.e(TAG, " " + error);
                return false;
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
