package no.nordicsemi.android.meshprovisioner;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

class NetworkImportAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = NetworkImportAsyncTask.class.getSimpleName();
    private final String path;
    private final LoadNetworkCallbacks callbacks;
    private MeshNetwork network;

    NetworkImportAsyncTask(final String path, final LoadNetworkCallbacks callbacks){
        this.path = path;
        this.callbacks = callbacks;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(final Void... voids) {
        importNetwork(path);
        return null;
    }

    @Override
    protected void onPostExecute(final Void aVoid) {
        super.onPostExecute(aVoid);
        if(network != null) {
            callbacks.onNetworkLoadedFromJson(network);
        } else {
            callbacks.onNetworkLoadFailed();
        }
    }

    private void importNetwork(final String path) {
        BufferedReader br = null;
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

            final File f = new File(path, "example_database.json");
            br = new BufferedReader(new FileReader(f));
            final MeshNetwork network = gson.fromJson(br, MeshNetwork.class);
            if(network != null){
                this.network = network;
            }
        } catch (Exception e) {
            Log.e(TAG, " " + e.getMessage());
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
