package no.nordicsemi.android.nrfmeshprovisioner.livedata;

public class GenericOnOffStatusUpdate {

    private final boolean presentOnOff;
    private Boolean targetOnOff;
    private int steps;
    private int resolution;

    public GenericOnOffStatusUpdate(final boolean presentOnOff, final Boolean targetOnOff, final int steps, final int resolution){
        this.presentOnOff = presentOnOff;
        this.targetOnOff = targetOnOff;
        this.steps = steps;
        this.resolution = resolution;
    }

    public boolean isPresentOnOff() {
        return presentOnOff;
    }

    public Boolean getTargetOnOff() {
        return targetOnOff;
    }

    public Integer getSteps() {
        return steps;
    }

    public int getResolution() {
        return resolution;
    }
}
