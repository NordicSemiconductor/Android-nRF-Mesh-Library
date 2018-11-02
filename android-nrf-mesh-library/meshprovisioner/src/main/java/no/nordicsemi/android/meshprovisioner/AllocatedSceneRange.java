package no.nordicsemi.android.meshprovisioner;

import com.google.gson.annotations.Expose;

/**
 * Class definition for allocating group range for provisioners.
 */
public class AllocatedSceneRange {

    @Expose
    private int firstScene;
    @Expose
    private int lastScene;

    /**
     * Constructs {@link AllocatedSceneRange} for provisioner
     *
     * @param firstScene high address of group range
     * @param lastScene low address of group range
     */
    public AllocatedSceneRange(final int firstScene, final int lastScene) {
        this.firstScene = firstScene;
        this.lastScene = lastScene;
    }

    /**
     * Returns the low address of the allocated group address
     *
     * @return low address
     */
    public int getLastScene() {
        return lastScene;
    }

    /**
     * Sets the low address of the allocated group address
     *
     * @param lastScene of the group range
     */
    public void setLastScene(final int lastScene) {
        this.lastScene = lastScene;
    }

    /**
     * Returns the high address of the allocated group range
     *
     * @return firstScene of the group range
     */
    public int getFirstScene() {
        return firstScene;
    }

    /**
     * Sets the high address of the group address
     *
     * @param firstScene of the group range
     */
    public void setFirstScene(final int firstScene) {
        this.firstScene = firstScene;
    }
}
