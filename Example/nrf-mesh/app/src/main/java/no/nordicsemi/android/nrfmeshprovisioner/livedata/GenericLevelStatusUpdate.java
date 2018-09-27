package no.nordicsemi.android.nrfmeshprovisioner.livedata;

public class GenericLevelStatusUpdate {

    private final int presentLevel;
    private Integer targetLevel;
    private int steps;
    private int resolution;

    public GenericLevelStatusUpdate(final int presentLevel, final Integer targetLevel, final int steps, final int resolution){
        this.presentLevel = presentLevel;
        this.targetLevel = targetLevel;
        this.steps = steps;
        this.resolution = resolution;
    }

    public int getPresentLevel() {
        return presentLevel;
    }

    public Integer getTargetLevel() {
        return targetLevel;
    }

    public Integer getSteps() {
        return steps;
    }

    public int getResolution() {
        return resolution;
    }
}
