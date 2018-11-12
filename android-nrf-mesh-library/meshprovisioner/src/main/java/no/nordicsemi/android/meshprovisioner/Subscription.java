package no.nordicsemi.android.meshprovisioner;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;

import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;

/**
 * Defines a group in a mesh network
 */
@SuppressWarnings("unused")
@Entity(tableName = "subscription",
        foreignKeys = {
                @ForeignKey(
                        entity = Element.class,
                        parentColumns = "address",
                        childColumns = "parent_address"),
                @ForeignKey(
                        entity = MeshModel.class,
                        parentColumns = "model_id",
                        childColumns = "model_id")})

public class Subscription {

    @ColumnInfo(name = "model_id")
    @Expose
    private int modelId;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "address")
    @Expose
    byte[] address;

    @ColumnInfo(name = "parent_address")
    @Expose
    private byte[] parentAddress;

    Subscription() {

    }

    /**
     * Constructs a mesh group
     *
     * @param address       address of the group
     * @param parentAddress address of the element this subscription belongs to
     */
    public Subscription(final byte[] address, final byte[] parentAddress) {
        this.address = address;
        this.parentAddress = parentAddress;
    }

    /**
     * Returns the group address
     *
     * @return 2 byte group address
     */
    public byte[] getAddress() {
        return address;
    }

    /**
     * Sets a subscription address
     *
     * @param address subscription address
     */
    void setAddress(final byte[] address) {
        this.address = address;
    }

}
