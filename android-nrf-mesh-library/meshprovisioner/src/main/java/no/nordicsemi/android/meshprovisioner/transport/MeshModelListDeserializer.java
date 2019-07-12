package no.nordicsemi.android.meshprovisioner.transport;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import no.nordicsemi.android.meshprovisioner.models.SigModelParser;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

/**
 * Class for deserializing a list of elements stored in the Mesh Configuration Database
 */
public final class MeshModelListDeserializer implements JsonSerializer<List<MeshModel>>, JsonDeserializer<List<MeshModel>> {

    @Override
    public List<MeshModel> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final List<MeshModel> meshModels = new ArrayList<>();
        final JsonArray jsonArray = json.getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            final JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            final int modelId = MeshParserUtils.hexToInt(jsonObject.get("modelId").getAsString());

            final MeshModel meshModel = getMeshModel(modelId);
            if (meshModel != null) {
                meshModel.mPublicationSettings = getPublicationSettings(jsonObject);
                setSubscriptionAddresses(meshModel, jsonObject);
                final List<Integer> boundKeyIndexes = getBoundAppKeyIndexes(jsonObject);
                meshModel.mBoundAppKeyIndexes.addAll(boundKeyIndexes);
                meshModels.add(meshModel);
            }
        }
        return meshModels;
    }

    @Override
    public JsonElement serialize(final List<MeshModel> models, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonArray jsonArray = new JsonArray();
        for (MeshModel model : models) {
            final JsonObject meshModelJson = new JsonObject();
            if (model instanceof VendorModel) {
                meshModelJson.addProperty("modelId", String.format(Locale.US, "%08X", model.getModelId()));
            } else {
                meshModelJson.addProperty("modelId", String.format(Locale.US, "%04X", model.getModelId()));
            }

            meshModelJson.add("bind", serializeBoundAppKeys(model.getBoundAppKeyIndexes()));
            meshModelJson.add("subscribe", serializeSubscriptionAddresses(model));

            if (model.getPublicationSettings() != null) {
                meshModelJson.add("publish", serializePublicationSettings(model.getPublicationSettings()));
            }

            jsonArray.add(meshModelJson);
        }
        return jsonArray;
    }

    /**
     * Get publication settings from json
     *
     * @param jsonObject json object
     * @return {@link PublicationSettings}
     */
    private PublicationSettings getPublicationSettings(final JsonObject jsonObject) {
        if (!jsonObject.has("publish"))
            return null;

        final JsonObject publish = jsonObject.get("publish").getAsJsonObject();

        final String hexAddress = publish.get("address").getAsString();
        final int publishAddress;
        UUID uuid = null;
        if (!TextUtils.isEmpty(hexAddress) && hexAddress.length() == 32) {
            uuid = UUID.fromString(MeshParserUtils.formatUuid(hexAddress));
            publishAddress = MeshAddress.generateVirtualAddress(uuid);
        } else {
            publishAddress = Integer.parseInt(hexAddress, 16);
        }

        final int index = publish.get("index").getAsInt();
        final int ttl = publish.get("ttl").getAsByte();

        //Unpack publish period
        final int period = publish.get("period").getAsInt();
        final int publicationSteps = period >> 6;
        final int publicationResolution = period & 0x03;

        final int publishRetransmitCount = publish.get("retransmit").getAsJsonObject().get("count").getAsInt();
        final int publishRetransmitIntervalSteps = publish.get("retransmit").getAsJsonObject().get("interval").getAsInt();

        final boolean credentials = publish.get("credentials").getAsInt() == 1;

        //Set the values
        final PublicationSettings publicationSettings = new PublicationSettings();
        publicationSettings.setPublishAddress(publishAddress);
        publicationSettings.setLabelUUID(uuid);
        publicationSettings.setPublishTtl(ttl);
        publicationSettings.setPublicationSteps(publicationSteps);
        publicationSettings.setPublicationResolution(publicationResolution);
        publicationSettings.setPublishRetransmitCount(publishRetransmitCount);
        publicationSettings.setPublishRetransmitIntervalSteps(publishRetransmitIntervalSteps);
        publicationSettings.setCredentialFlag(credentials);

        return publicationSettings;
    }

    /**
     * Sets subscription addresses to a mesh model from json
     *
     * @param meshModel  Mesh Model
     * @param jsonObject Json representation of the mesh model
     */
    private void setSubscriptionAddresses(final MeshModel meshModel, final JsonObject jsonObject) {
        final List<Integer> subscriptions = new ArrayList<>();
        if (!(jsonObject.has("subscribe"))) {
            return;
        }

        final JsonArray jsonArray = jsonObject.get("subscribe").getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            final String hexAddress = jsonArray.get(i).getAsString();
            final int address;
            if (hexAddress.length() == 32) {
                final UUID uuid = UUID.fromString(MeshParserUtils.formatUuid(hexAddress));
                address = MeshAddress.generateVirtualAddress(uuid);
                meshModel.labelUuids.add(uuid);
            } else {
                address = Integer.parseInt(hexAddress, 16);
            }
            subscriptions.add(address);
        }
        meshModel.subscriptionAddresses.addAll(subscriptions);
    }

    private List<Integer> getBoundAppKeyIndexes(final JsonObject jsonObject) {
        final List<Integer> boundKeyIndexes = new ArrayList<>();
        if (!(jsonObject.has("bind")))
            return boundKeyIndexes;

        final JsonArray jsonArray = jsonObject.get("bind").getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            final int index = jsonArray.get(i).getAsInt();
            boundKeyIndexes.add(index);
        }
        return boundKeyIndexes;
    }

    /**
     * Returns JsonElement containing the subscription addresses addresses from json
     *
     * @param model Mesh model
     */
    private JsonArray serializeSubscriptionAddresses(@NonNull final MeshModel model) {
        final JsonArray subscriptionsJson = new JsonArray();
        for (Integer address : model.getSubscribedAddresses()) {
            if (MeshAddress.isValidVirtualAddress(address)) {
                final UUID uuid = model.getLabelUUID(address);
                subscriptionsJson.add(MeshParserUtils.uuidToHex(uuid));
            } else {
                subscriptionsJson.add(MeshAddress.formatAddress(address, false));
            }
        }
        return subscriptionsJson;
    }

    /**
     * Returns JsonElement containing the subscription addresses addresses from json
     *
     * @param settings publication settings for this node
     */
    @SuppressWarnings("ConstantConditions")
    private JsonObject serializePublicationSettings(final PublicationSettings settings) {
        final JsonObject publicationJson = new JsonObject();
        if(MeshAddress.isValidVirtualAddress(settings.getPublishAddress())){
            publicationJson.addProperty("address", MeshParserUtils.uuidToHex(settings.getLabelUUID()));
        } else {
            publicationJson.addProperty("address", MeshAddress.formatAddress(settings.getPublishAddress(), false));
        }
        publicationJson.addProperty("index", settings.getAppKeyIndex());
        publicationJson.addProperty("ttl", settings.getPublishTtl());
        publicationJson.addProperty("period", settings.encodePublicationPeriod());

        final JsonObject retransmitJson = new JsonObject();
        retransmitJson.addProperty("count", settings.getPublishRetransmitCount());
        retransmitJson.addProperty("interval", settings.getPublishRetransmitIntervalSteps());
        publicationJson.add("retransmit", retransmitJson);
        publicationJson.addProperty("credentials", settings.getCredentialFlag() ? 1 : 0);
        return publicationJson;
    }

    /**
     * Returns JsonElement containing the subscription addresses addresses from json
     *
     * @param boundAppKeys List of bound app key indexes
     */
    private JsonArray serializeBoundAppKeys(final List<Integer> boundAppKeys) {
        final JsonArray boundAppKeyIndexes = new JsonArray();
        for (Integer index : boundAppKeys) {
            boundAppKeyIndexes.add(index);
        }
        return boundAppKeyIndexes;
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
