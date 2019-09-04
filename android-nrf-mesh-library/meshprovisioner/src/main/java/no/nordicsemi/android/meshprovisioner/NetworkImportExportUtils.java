package no.nordicsemi.android.meshprovisioner;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.InternalElementListDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.MeshModelListDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.NodeDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

/**
 * Utility class to handle network imports and exports
 */
class NetworkImportExportUtils {

    private static final String TAG = NetworkImportExportUtils.class.getSimpleName();

    /**
     * Creates an AsyncTask to import the a mesh network
     *
     * @param context   context
     * @param uri       file path
     * @param callbacks internal callbacks to notify network import
     */
    static void importMeshNetwork(@NonNull final Context context,
                                  @NonNull final Uri uri,
                                  @NonNull final LoadNetworkCallbacks callbacks) throws JsonSyntaxException {
        new NetworkImportAsyncTask(context, uri, callbacks).execute();
    }

    /**
     * Creates an AsyncTask to import the a mesh network
     *
     * @param context     context
     * @param networkJson network json
     * @param callbacks   internal callbacks to notify network import
     */
    static void importMeshNetworkFromJson(@NonNull final Context context,
                                          @NonNull final String networkJson,
                                          @NonNull final LoadNetworkCallbacks callbacks) {
        new NetworkImportAsyncTask(context, networkJson, callbacks).execute();
    }

    /**
     * AsyncTask that reads and import a mesh network from the Mesh Provisioning/Configuration Database Json file
     */
    private static class NetworkImportAsyncTask extends AsyncTask<Void, Void, Void> {

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
         * @param context     context
         * @param networkJson network json
         * @param callbacks   internal callbacks to notify network import
         */
        NetworkImportAsyncTask(@NonNull final Context context,
                               @NonNull final String networkJson,
                               @NonNull final LoadNetworkCallbacks callbacks) {
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
            try {
                importNetwork();
            } catch (Exception ex) {
                error = ex.getMessage() + "\n\nP.S. If the json file was exported using an older version 2.0.0 (Mesh Library), " +
                        "please retry exporting the json file and importing again.";
            }
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
        private void importNetwork() throws JsonSyntaxException {
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
                gsonBuilder.registerTypeAdapter(allocatedUnicastRange, new AllocatedUnicastRangeDeserializer());
                gsonBuilder.registerTypeAdapter(allocatedGroupRange, new AllocatedGroupRangeDeserializer());
                gsonBuilder.registerTypeAdapter(allocatedSceneRange, new AllocatedSceneRangeDeserializer());
                gsonBuilder.registerTypeAdapter(nodeList, new NodeDeserializer());
                gsonBuilder.registerTypeAdapter(elementList, new InternalElementListDeserializer());
                gsonBuilder.registerTypeAdapter(meshModelList, new MeshModelListDeserializer());
                final Gson gson = gsonBuilder.serializeNulls().create();

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
     * Exports the mesh network to a Json file
     *
     * @param network mesh network to be exported
     */
    @Nullable
    static String export(@NonNull final MeshNetwork network) {
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
            Type nodeList = new TypeToken<List<ProvisionedMeshNode>>() {
            }.getType();
            Type meshModelList = new TypeToken<List<MeshModel>>() {
            }.getType();
            Type elementList = new TypeToken<List<Element>>() {
            }.getType();

            final GsonBuilder gsonBuilder = new GsonBuilder();

            gsonBuilder.registerTypeAdapter(netKeyList, new NetKeyDeserializer());
            gsonBuilder.registerTypeAdapter(appKeyList, new AppKeyDeserializer());
            gsonBuilder.registerTypeAdapter(allocatedUnicastRange, new AllocatedUnicastRangeDeserializer());
            gsonBuilder.registerTypeAdapter(allocatedGroupRange, new AllocatedGroupRangeDeserializer());
            gsonBuilder.registerTypeAdapter(allocatedSceneRange, new AllocatedSceneRangeDeserializer());
            gsonBuilder.registerTypeAdapter(nodeList, new NodeDeserializer());
            gsonBuilder.registerTypeAdapter(elementList, new InternalElementListDeserializer());
            gsonBuilder.registerTypeAdapter(meshModelList, new MeshModelListDeserializer());
            gsonBuilder.registerTypeAdapter(MeshNetwork.class, new MeshNetworkDeserializer());

            final Gson gson = gsonBuilder
                    .serializeNulls()
                    .setPrettyPrinting()
                    .create();
            return gson.toJson(network);
        } catch (final com.google.gson.JsonSyntaxException ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
            return null;
        } catch (final Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return null;
        }
    }

}
