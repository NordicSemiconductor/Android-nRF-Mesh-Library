package no.nordicsemi.android.meshprovisioner.transport;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import no.nordicsemi.android.meshprovisioner.models.SigModelParser;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.PublicationSettings;

/**
 * Do not touch this class, implemented for mesh model deserialization
 */
public final class InternalMeshModelDeserializer implements JsonDeserializer<MeshModel> {
    @Override
    public MeshModel deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject;
        if (json.getAsJsonObject().has("data")) {
            jsonObject = json.getAsJsonObject().getAsJsonObject("data");
            return parsePreMigrationMeshModel(jsonObject);
        } else {
            jsonObject = json.getAsJsonObject();
            return parseMigratedMeshModel(jsonObject);
        }
    }

    /**
     * Parses mesh model data prior to migration
     *
     * @param jsonObject jsonObject
     */
    private MeshModel parsePreMigrationMeshModel(final JsonObject jsonObject) {
        final int modelId = jsonObject.getAsJsonObject().get("mModelId").getAsInt();
        final MeshModel meshModel = getMeshModel(modelId);

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
                final byte[] publishAddress = new byte[jsonPublishAddress.size()];
                for (int i = 0; i < jsonPublishAddress.size(); i++) {
                    publishAddress[i] = jsonPublishAddress.get(i).getAsByte();
                }

                final int publishRetransmitCount = jsonPublicationSettings.get("publishRetransmitCount").getAsInt();
                final int publishRetransmitIntervalSteps = jsonPublicationSettings.get("publishRetransmitIntervalSteps").getAsInt();
                final int publishTtl = jsonPublicationSettings.get("publishRetransmitIntervalSteps").getAsByte();

                meshModel.mPublicationSettings = new PublicationSettings(publishAddress,
                        appKeyIndex, credentialFlag, publishTtl, publicationSteps, publicationResolution, publishRetransmitCount, publishRetransmitIntervalSteps);
            }
        } else {
            final byte[] publishKeyAppIndex = new byte[2];
            if (jsonObject.getAsJsonObject().has("publishAppKeyIndex")) {
                final JsonArray jsonPublishKeyIndex = jsonObject.getAsJsonObject().get("publishAppKeyIndex").getAsJsonArray();
                for (int i = 0; i < jsonPublishKeyIndex.size(); i++) {
                    publishKeyAppIndex[i] = jsonPublishKeyIndex.get(i).getAsByte();
                }
            }

            final byte[] publishAddress = new byte[2];
            if (jsonObject.getAsJsonObject().has("publishAddress")) {
                final JsonArray jsonPublishAddress = jsonObject.getAsJsonObject().get("publishAddress").getAsJsonArray();
                for (int i = 0; i < jsonPublishAddress.size(); i++) {
                    publishAddress[i] = jsonPublishAddress.get(i).getAsByte();
                }
            }
            final int publicationResolution = jsonObject.getAsJsonObject().get("publicationResolution").getAsInt();
            final int publicationSteps = jsonObject.getAsJsonObject().get("publicationSteps").getAsInt();
            final int publishPeriod = jsonObject.getAsJsonObject().get("publishPeriod").getAsInt();
            final int publishRetransmitCount = jsonObject.getAsJsonObject().get("publishRetransmitCount").getAsInt();
            final int publishRetransmitIntervalSteps = jsonObject.getAsJsonObject().get("publishRetransmitIntervalSteps").getAsInt();
            final int publishTtl = jsonObject.getAsJsonObject().get("publishTtl").getAsInt();
            meshModel.mPublicationSettings = new PublicationSettings(publishAddress,
                    MeshParserUtils.removeKeyIndexPadding(publishKeyAppIndex), false, publishTtl, publicationSteps, publicationResolution, publishRetransmitCount, publishRetransmitIntervalSteps);
        }
        return meshModel;
    }

    /**
     * Parses migrated mesh model data
     *
     * @param jsonObject jsonObject
     */
    private MeshModel parseMigratedMeshModel(final JsonObject jsonObject) {
        final int modelId = jsonObject.get("mModelId").getAsInt();
        final MeshModel meshModel = getMeshModel(modelId);

        final JsonArray jsonArrayBoundKeyIndexes = jsonObject.getAsJsonArray("mBoundAppKeyIndexes");
        final JsonObject jsonBoundKeys = jsonObject.getAsJsonObject("mBoundApplicationKeys");
        for (int i = 0; i < jsonArrayBoundKeyIndexes.size(); i++) {
            final int index = jsonArrayBoundKeyIndexes.get(i).getAsInt();
            final byte[] key = getKey(jsonBoundKeys.get(String.valueOf(index)).getAsJsonObject().getAsJsonArray("key"));
            final ApplicationKey applicationKey = new ApplicationKey(index, key);
            meshModel.mBoundApplicationKeys.put(index, applicationKey);
            meshModel.mBoundAppKeyIndexes.add(index);
        }

        final JsonArray jsonSubscriptionAddresses = jsonObject.getAsJsonArray("mSubscriptionAddress");

        for (int i = 0; i < jsonSubscriptionAddresses.size(); i++) {
            final JsonArray jsonArray = jsonSubscriptionAddresses.get(i).getAsJsonArray();
            final byte[] subscriptionAddress = new byte[2];
            for (int j = 0; j < jsonArray.size(); j++) {
                subscriptionAddress[j] = jsonArray.get(j).getAsByte();
            }
            meshModel.addSubscriptionAddress(subscriptionAddress);
        }

        if (jsonObject.getAsJsonObject().has("mPublicationSettings")) {
            final JsonObject jsonPublicationSettings = jsonObject.get("mPublicationSettings").getAsJsonObject();
            if (jsonPublicationSettings != null) {
                final int appKeyIndex = jsonPublicationSettings.get("appKeyIndex").getAsInt();
                final boolean credentialFlag = jsonPublicationSettings.get("credentialFlag").getAsBoolean();
                final int publicationResolution = jsonPublicationSettings.get("publicationResolution").getAsInt();
                final int publicationSteps = jsonPublicationSettings.get("publicationSteps").getAsInt();

                final JsonArray jsonPublishAddress = jsonPublicationSettings.getAsJsonArray("publishAddress");
                final byte[] publishAddress = new byte[jsonPublishAddress.size()];
                for (int i = 0; i < jsonPublishAddress.size(); i++) {
                    publishAddress[i] = jsonPublishAddress.get(i).getAsByte();
                }

                final int publishRetransmitCount = jsonPublicationSettings.get("publishRetransmitCount").getAsInt();
                final int publishRetransmitIntervalSteps = jsonPublicationSettings.get("publishRetransmitIntervalSteps").getAsInt();
                final int publishTtl = jsonPublicationSettings.get("publishRetransmitIntervalSteps").getAsByte();

                meshModel.mPublicationSettings = new PublicationSettings(publishAddress,
                        appKeyIndex, credentialFlag, publishTtl, publicationSteps, publicationResolution, publishRetransmitCount, publishRetransmitIntervalSteps);
            }
        }

        return meshModel;
    }

    private byte[] getKey(final JsonArray array) {
        final byte[] key = new byte[array.size()];
        for (int i = 0; i < array.size(); i++) {
            key[i] = array.get(i).getAsByte();
        }
        return key;
    }

    /**
     * Returns a {@link MeshModel}
     *
     * @param modelId model Id
     * @return {@link MeshModel}
     */
    private MeshModel getMeshModel(final int modelId) {
        if (modelId < Short.MIN_VALUE || modelId > Short.MAX_VALUE) {
            return new VendorModel(modelId);
        } else {
            return SigModelParser.getSigModel(modelId);
        }
    }

}
