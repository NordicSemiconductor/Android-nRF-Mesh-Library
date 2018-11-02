package no.nordicsemi.android.meshprovisioner.transport;

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

import no.nordicsemi.android.meshprovisioner.models.SigModelParser;
import no.nordicsemi.android.meshprovisioner.models.VendorModel;
import no.nordicsemi.android.meshprovisioner.utils.AddressUtils;
import no.nordicsemi.android.meshprovisioner.utils.PublicationSettings;

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
            final int modelId = Integer.parseInt(jsonObject.get("modelId").getAsString(), 16);

            final PublicationSettings publicationSettings = getPublicationSettings(jsonObject);
            final List<byte[]> subscriptionAddresses = getSubscriptionAddresses(jsonObject);

            final MeshModel meshModel = getMeshModel(modelId);
            if(meshModel != null) {
                meshModel.mPublicationSettings = publicationSettings;
                meshModel.mSubscriptionAddress.addAll(subscriptionAddresses);
                meshModels.add(meshModel);
            }
        }
        return meshModels;
    }

    @Override
    public JsonElement serialize(final List<MeshModel> src, final Type typeOfSrc, final JsonSerializationContext context) {
        return null;
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
        final int address = Integer.parseInt(publish.get("address").getAsString(), 16);
        final byte[] publishAddress = AddressUtils.getUnicastAddressBytes(address);

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
        publicationSettings.setPublishTtl(ttl);
        publicationSettings.setPublicationSteps(publicationSteps);
        publicationSettings.setPublicationResolution(publicationResolution);
        publicationSettings.setPublishRetransmitCount(publishRetransmitCount);
        publicationSettings.setPublishRetransmitIntervalSteps(publishRetransmitIntervalSteps);
        publicationSettings.setCredentialFlag(credentials);

        return publicationSettings;
    }

    /**
     * Returns subscription addresses from json
     *
     * @param jsonObject json
     * @return list of subscription addresses
     */
    private List<byte[]> getSubscriptionAddresses(final JsonObject jsonObject) {
        final List<byte[]> subscriptions = new ArrayList<>();
        if (!(jsonObject.has("subscribe")))
            return subscriptions;

        final JsonArray jsonArray = jsonObject.get("subscribe").getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            final int address = Integer.parseInt(jsonArray.get(i).getAsString(), 16);
            subscriptions.add(AddressUtils.getUnicastAddressBytes(address));
        }
        return subscriptions;
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
