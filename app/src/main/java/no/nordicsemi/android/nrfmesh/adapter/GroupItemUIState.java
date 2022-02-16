package no.nordicsemi.android.nrfmesh.adapter;

/**
 *
 */
public class GroupItemUIState {
    private final String name;
    private final int address;
    private final int subscribedModels;

    public GroupItemUIState(final String name, final int address, final int subscribedModels){
        this.name = name;
        this.address = address;
        this.subscribedModels = subscribedModels;
    }

    public String getName() {
        return name;
    }

    public int getAddress() {
        return address;
    }

    public int getSubscribedModels() {
        return subscribedModels;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GroupItemUIState that = (GroupItemUIState) o;

        if (address != that.address) return false;
        if (subscribedModels != that.subscribedModels) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + address;
        result = 31 * result + subscribedModels;
        return result;
    }
}
