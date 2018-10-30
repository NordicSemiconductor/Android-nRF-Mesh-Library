package no.nordicsemi.android.meshprovisioner.transport;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.json.JSONArray;

import java.lang.reflect.Type;

import no.nordicsemi.android.meshprovisioner.models.SigModelParser;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.PublicationSettings;

public class MeshModelDeserializer implements JsonDeserializer<MeshModel> {
    @Override
    public MeshModel deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if(json.getAsJsonObject().has("data")) {
            final JsonObject jsonObject = json.getAsJsonObject().getAsJsonObject("data");
            final int modelId = jsonObject.getAsJsonObject().get("mModelId").getAsInt();
            final MeshModel meshModel = SigModelParser.getSigModel(modelId);

            final JsonArray jsonArrayBoundKeyIndexes = jsonObject.getAsJsonObject().getAsJsonArray("mBoundAppKeyIndexes");
            final JsonObject jsonBoundKeys = jsonObject.getAsJsonObject().get("mBoundAppKeys").getAsJsonObject();
            for (int i = 0; i < jsonArrayBoundKeyIndexes.size(); i++) {
                final int index = jsonArrayBoundKeyIndexes.get(i).getAsInt();
                final String key = jsonBoundKeys.get(String.valueOf(index)).getAsString();
                final ApplicationKey applicationKey = new ApplicationKey(index, MeshParserUtils.toByteArray(key));
                meshModel.mBoundApplicationKeys.put(index, applicationKey);
                meshModel.mBoundAppKeyIndexes.add(index);
            }

            final JsonArray jsonSubscriptionAddresses = jsonObject.getAsJsonObject().get("mSubscriptionAddress").getAsJsonArray();

            for (int i = 0; i < jsonSubscriptionAddresses.size(); i++) {
                final JsonArray jsonArray = jsonSubscriptionAddresses.get(i).getAsJsonArray();
                final byte[] subscriptionAddress = new byte[2];
                for (int j = 0; j < jsonArray.size(); j++) {
                    subscriptionAddress[j] = jsonArray.get(j).getAsByte();
                }
                meshModel.addSubscriptionAddress(subscriptionAddress);
            }

            if (jsonObject.getAsJsonObject().has("mPublicationSettings")) {
                final JsonObject jsonPublicationSettings = jsonObject.getAsJsonObject().get("mPublicationSettings").getAsJsonObject();
                if (jsonPublicationSettings != null) {
                    final int appKeyIndex = jsonPublicationSettings.get("appKeyIndex").getAsInt();
                    final boolean credentialFlag = jsonPublicationSettings.get("credentialFlag").getAsBoolean();
                    final int publicationResolution = jsonPublicationSettings.get("publicationResolution").getAsInt();
                    final int publicationSteps = jsonPublicationSettings.get("publicationSteps").getAsInt();

                    final JsonArray jsonPublishAddress = jsonPublicationSettings.get("publishAddress").getAsJsonArray();
                    final byte[] publishAddress = new byte[2];
                    for (int i = 0; i < jsonPublishAddress.size(); i++) {
                        publishAddress[i] = jsonPublishAddress.get(i).getAsByte();
                    }

                    final int publishRetransmitCount = jsonPublicationSettings.get("publishRetransmitCount").getAsInt();
                    final int publishRetransmitIntervalSteps = jsonPublicationSettings.get("publishRetransmitIntervalSteps").getAsInt();
                    final int publishTtl = jsonPublicationSettings.get("publishRetransmitIntervalSteps").getAsByte();

                    PublicationSettings publicationSettings = new PublicationSettings(publishAddress,
                            appKeyIndex, credentialFlag, publishTtl, publicationSteps, publicationResolution, publishRetransmitCount, publishRetransmitIntervalSteps);
                    meshModel.mPublicationSettings = publicationSettings;
                }
            }
            return meshModel;
        } else {
            final JsonObject jsonObject = json.getAsJsonObject();//.get("data")
            return getMeshModel2(jsonObject);
        }


    }

    private MeshModel getMeshModel2(final JsonObject jsonObject){
        final int modelId = jsonObject.get("mModelId").getAsInt();
        final MeshModel meshModel =  SigModelParser.getSigModel(modelId);

        final JsonArray jsonArrayBoundKeyIndexes = jsonObject.getAsJsonArray("mBoundAppKeyIndexes");
        final JsonObject jsonBoundKeys = jsonObject.getAsJsonObject("mBoundApplicationKeys");
        for(int i = 0; i < jsonArrayBoundKeyIndexes.size(); i++) {
            final int index = jsonArrayBoundKeyIndexes.get(i).getAsInt();
            final String key = jsonBoundKeys.get(String.valueOf(index)).getAsString();
            final ApplicationKey applicationKey = new ApplicationKey(index, MeshParserUtils.toByteArray(key));
            meshModel.mBoundApplicationKeys.put(index, applicationKey);
            meshModel.mBoundAppKeyIndexes.add(index);
        }

        final JsonArray jsonSubscriptionAddresses = jsonObject.getAsJsonArray("mSubscriptionAddress");

        for(int i= 0; i < jsonSubscriptionAddresses.size(); i++) {
            final JsonArray jsonArray = jsonSubscriptionAddresses.get(i).getAsJsonArray();
            final byte[] subscriptionAddress = new byte[2];
            for(int j = 0; j < jsonArray.size(); j++) {
                subscriptionAddress[j] = jsonArray.get(j).getAsByte();
            }
            meshModel.addSubscriptionAddress(subscriptionAddress);
        }

        if(jsonObject.getAsJsonObject().has("mPublicationSettings")) {
            final JsonObject jsonPublicationSettings = jsonObject.get("mPublicationSettings").getAsJsonObject();
            if (jsonPublicationSettings != null) {
                final int appKeyIndex = jsonPublicationSettings.get("appKeyIndex").getAsInt();
                final boolean credentialFlag = jsonPublicationSettings.get("credentialFlag").getAsBoolean();
                final int publicationResolution = jsonPublicationSettings.get("publicationResolution").getAsInt();
                final int publicationSteps = jsonPublicationSettings.get("publicationSteps").getAsInt();

                final JsonArray jsonPublishAddress = jsonPublicationSettings.getAsJsonArray("publishAddress");
                final byte[] publishAddress = new byte[2];
                for (int i = 0; i < jsonPublishAddress.size(); i++) {
                    publishAddress[i] = jsonPublishAddress.get(i).getAsByte();
                }

                final int publishRetransmitCount = jsonPublicationSettings.get("publishRetransmitCount").getAsInt();
                final int publishRetransmitIntervalSteps = jsonPublicationSettings.get("publishRetransmitIntervalSteps").getAsInt();
                final int publishTtl = jsonPublicationSettings.get("publishRetransmitIntervalSteps").getAsByte();

                PublicationSettings publicationSettings = new PublicationSettings(publishAddress,
                        appKeyIndex, credentialFlag, publishTtl, publicationSteps, publicationResolution, publishRetransmitCount, publishRetransmitIntervalSteps);
                meshModel.mPublicationSettings = publicationSettings;
            }
        }

        return meshModel;
    }

}
