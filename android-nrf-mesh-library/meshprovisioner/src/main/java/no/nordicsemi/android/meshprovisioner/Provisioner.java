package no.nordicsemi.android.meshprovisioner;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.meshprovisioner.utils.MeshTypeConverters;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Class definition of a Provisioner of mesh network
 */
@SuppressWarnings({"unused"})
@Entity(tableName = "provisioner",
        foreignKeys = @ForeignKey(entity = MeshNetwork.class,
                parentColumns = "mesh_uuid",
                childColumns = "mesh_uuid",
                onUpdate = CASCADE, onDelete = CASCADE),
        indices = @Index("mesh_uuid"))
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class Provisioner implements Parcelable {

    @ColumnInfo(name = "mesh_uuid")
    @NonNull
    @Expose
    private String meshUuid;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "provisioner_uuid")
    @Expose
    private String provisionerUuid;

    @ColumnInfo(name = "name")
    @Expose
    private String provisionerName = "nRF Mesh Provisioner";

    @TypeConverters(MeshTypeConverters.class)
    @Expose
    protected List<AllocatedGroupRange> allocatedGroupRanges = new ArrayList<>();

    @TypeConverters(MeshTypeConverters.class)
    @Expose
    protected List<AllocatedUnicastRange> allocatedUnicastRanges = new ArrayList<>();

    @TypeConverters(MeshTypeConverters.class)
    @Expose
    protected List<AllocatedSceneRange> allocatedSceneRanges = new ArrayList<>();

    @ColumnInfo(name = "sequence_number")
    @Expose
    private int sequenceNumber;

    @ColumnInfo(name = "provisioner_address")
    @Expose
    private int provisionerAddress;

    @ColumnInfo(name = "global_ttl")
    @Expose
    private int globalTtl = 5;

    @ColumnInfo(name = "last_selected")
    @Expose
    private boolean lastSelected;

    @Ignore
    private final Comparator<AddressRange> addressRangeComparator = (addressRange1, addressRange2) ->
            Integer.compare(addressRange1.getLowAddress(), addressRange2.getLowAddress());

    @Ignore
    private final Comparator<AllocatedSceneRange> sceneRangeComparator = (sceneRange1, sceneRange2) ->
            Integer.compare(sceneRange1.getFirstScene(), sceneRange2.getFirstScene());

    /**
     * Constructs {@link Provisioner}
     */
    public Provisioner(@NonNull final String provisionerUuid,
                       @NonNull final List<AllocatedUnicastRange> allocatedUnicastRanges,
                       @NonNull final List<AllocatedGroupRange> allocatedGroupRanges,
                       @NonNull final List<AllocatedSceneRange> allocatedSceneRanges,
                       @NonNull final String meshUuid) {
        this.provisionerUuid = provisionerUuid;
        this.allocatedUnicastRanges = allocatedUnicastRanges;
        this.allocatedGroupRanges = allocatedGroupRanges;
        this.allocatedSceneRanges = allocatedSceneRanges;
        this.meshUuid = meshUuid;
    }

    protected Provisioner(Parcel in) {
        meshUuid = in.readString();
        provisionerUuid = in.readString();
        provisionerName = in.readString();
        in.readTypedList(allocatedUnicastRanges, AllocatedUnicastRange.CREATOR);
        in.readTypedList(allocatedGroupRanges, AllocatedGroupRange.CREATOR);
        in.readTypedList(allocatedSceneRanges, AllocatedSceneRange.CREATOR);
        sequenceNumber = in.readInt();
        provisionerAddress = in.readInt();
        globalTtl = in.readInt();
        lastSelected = in.readByte() != 0;
    }

    public static final Creator<Provisioner> CREATOR = new Creator<Provisioner>() {
        @Override
        public Provisioner createFromParcel(Parcel in) {
            return new Provisioner(in);
        }

        @Override
        public Provisioner[] newArray(int size) {
            return new Provisioner[size];
        }
    };

    /**
     * Returns the provisionerUuid of the Mesh network
     *
     * @return String provisionerUuid
     */
    @NonNull
    public String getMeshUuid() {
        return meshUuid;
    }

    /**
     * Sets the provisionerUuid of the mesh network to this application key
     *
     * @param uuid mesh network provisionerUuid
     */
    public void setMeshUuid(@NonNull final String uuid) {
        meshUuid = uuid;
    }

    /**
     * Returns the provisioner name
     *
     * @return name
     */
    public String getProvisionerName() {
        return provisionerName;
    }

    /**
     * Sets a friendly name to a provisioner
     *
     * @param provisionerName friendly name
     */
    public void setProvisionerName(@NonNull final String provisionerName) throws IllegalArgumentException {
        if (TextUtils.isEmpty(provisionerName))
            throw new IllegalArgumentException("Name cannot be empty");
        this.provisionerName = provisionerName;
    }

    /**
     * Returns the provisionerUuid
     *
     * @return UUID
     */
    @NonNull
    public String getProvisionerUuid() {
        return provisionerUuid;
    }

    public void setProvisionerUuid(@NonNull final String provisionerUuid) {
        this.provisionerUuid = provisionerUuid;
    }

    /**
     * Returns {@link AllocatedGroupRange} for this provisioner
     *
     * @return allocated range of group addresses
     */
    public List<AllocatedGroupRange> getAllocatedGroupRanges() {
        return Collections.unmodifiableList(allocatedGroupRanges);
    }

    /**
     * Sets {@link AllocatedGroupRange} for this provisioner
     *
     * @param allocatedGroupRanges allocated range of group addresses
     */
    public void setAllocatedGroupRanges(final List<AllocatedGroupRange> allocatedGroupRanges) {
        this.allocatedGroupRanges = allocatedGroupRanges;
    }

    /**
     * Returns {@link AllocatedUnicastRange} for this provisioner
     *
     * @return allocated range of unicast addresses
     */
    public List<AllocatedUnicastRange> getAllocatedUnicastRanges() {
        return Collections.unmodifiableList(allocatedUnicastRanges);
    }

    /**
     * Sets {@link AllocatedGroupRange} for this provisioner
     *
     * @param allocatedUnicastRanges allocated range of unicast addresses
     */
    public void setAllocatedUnicastRanges(final List<AllocatedUnicastRange> allocatedUnicastRanges) {
        this.allocatedUnicastRanges = allocatedUnicastRanges;
    }

    /**
     * Returns {@link AllocatedSceneRange} for this provisioner
     *
     * @return allocated range of unicast addresses
     */
    public List<AllocatedSceneRange> getAllocatedSceneRanges() {
        return Collections.unmodifiableList(allocatedSceneRanges);
    }

    /**
     * Sets {@link AllocatedSceneRange} for this provisioner
     *
     * @param allocatedSceneRanges allocated range of unicast addresses
     */
    public void setAllocatedSceneRanges(final List<AllocatedSceneRange> allocatedSceneRanges) {
        this.allocatedSceneRanges = allocatedSceneRanges;
    }


    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(final int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getProvisionerAddress() {
        return provisionerAddress;
    }

    /**
     * Set provisioner address
     *
     * @param address address of the provisioner
     */
    public void setProvisionerAddress(final int address) throws IllegalArgumentException {
        if (!MeshAddress.isValidUnicastAddress(address)) {
            throw new IllegalArgumentException("Unicast address must range between 0x0001 to 0x7FFF");
        }
        this.provisionerAddress = address;
    }

    public int getGlobalTtl() {
        return globalTtl;
    }

    public void setGlobalTtl(final int globalTtl) {
        this.globalTtl = globalTtl;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public boolean isLastSelected() {
        return lastSelected;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setLastSelected(final boolean lastSelected) {
        this.lastSelected = lastSelected;
    }

    public int incrementSequenceNumber() {
        sequenceNumber = sequenceNumber + 1;
        return sequenceNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(meshUuid);
        parcel.writeString(provisionerUuid);
        parcel.writeString(provisionerName);
        parcel.writeTypedList(allocatedUnicastRanges);
        parcel.writeTypedList(allocatedGroupRanges);
        parcel.writeTypedList(allocatedSceneRanges);
        parcel.writeInt(sequenceNumber);
        parcel.writeInt(provisionerAddress);
        parcel.writeInt(globalTtl);
        parcel.writeByte((byte) (lastSelected ? 1 : 0));
    }

    /**
     * Add a range to the provisioner
     *
     * @param allocatedRange {@link Range}
     */
    public boolean addRange(@NonNull final Range allocatedRange) {
        if (allocatedRange instanceof AllocatedUnicastRange) {
            allocatedUnicastRanges.add((AllocatedUnicastRange) allocatedRange);
            final ArrayList<AllocatedUnicastRange> ranges = new ArrayList<>(allocatedUnicastRanges);
            Collections.sort(ranges, addressRangeComparator);
            allocatedUnicastRanges.clear();
            allocatedUnicastRanges.addAll(Range.mergeUnicastRanges(ranges));
            return true;
        } else if (allocatedRange instanceof AllocatedGroupRange) {
            allocatedGroupRanges.add((AllocatedGroupRange) allocatedRange);
            final ArrayList<AllocatedGroupRange> ranges = new ArrayList<>(allocatedGroupRanges);
            Collections.sort(ranges, addressRangeComparator);
            allocatedGroupRanges.clear();
            allocatedGroupRanges.addAll(Range.mergeGroupRanges(ranges));
            return true;
        } else if (allocatedRange instanceof AllocatedSceneRange) {
            allocatedSceneRanges.add((AllocatedSceneRange) allocatedRange);
            final ArrayList<AllocatedSceneRange> ranges = new ArrayList<>(allocatedSceneRanges);
            Collections.sort(allocatedSceneRanges, sceneRangeComparator);
            allocatedSceneRanges.clear();
            allocatedSceneRanges.addAll(Range.mergeSceneRanges(ranges));
            return true;
        }
        return false;
    }

    /**
     * Checks if a given Unicast Address is within an allocated unicast address range
     *
     * @param address Unicast Address
     * @return true if it is within a range or false otherwise
     * @throws IllegalArgumentException if address is invalid or out of range
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isAddressWithinAllocatedRange(final int address) throws IllegalArgumentException {
        if (!MeshAddress.isValidUnicastAddress(address)) {
            throw new IllegalArgumentException("Unicast address must range from 0x0001 - 0x7FFF");
        }

        for (AllocatedUnicastRange range : allocatedUnicastRanges) {
            if (address >= range.getLowAddress() && address <= range.getHighAddress())
                return true;
        }
        return false;
    }

    public boolean hasOverlappingUnicastRanges(@NonNull final List<AllocatedUnicastRange> otherRanges) {
        for (AllocatedUnicastRange range : allocatedUnicastRanges) {
            for (AllocatedUnicastRange other : otherRanges) {
                if (range.overlaps(other)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasOverlappingGroupRanges(@NonNull final List<AllocatedGroupRange> otherRanges) {
        for (AllocatedGroupRange range : allocatedGroupRanges) {
            for (AllocatedGroupRange other : otherRanges) {
                if (range.overlaps(other)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasOverlappingSceneRanges(@NonNull final List<AllocatedSceneRange> otherRanges) {
        for (AllocatedSceneRange range : allocatedSceneRanges) {
            for (AllocatedSceneRange other : otherRanges) {
                if (range.overlaps(other)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isNodeAddressInUse(@NonNull final List<ProvisionedMeshNode> nodes) {
        for (ProvisionedMeshNode node : nodes) {
            if (!node.getUuid().equalsIgnoreCase(provisionerUuid)) {
                if (node.getUnicastAddress() == provisionerAddress) {
                    return true;
                }
            }
        }
        return false;
    }
}
