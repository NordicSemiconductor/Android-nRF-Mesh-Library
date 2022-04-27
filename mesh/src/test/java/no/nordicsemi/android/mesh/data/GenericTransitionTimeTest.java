package no.nordicsemi.android.mesh.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class GenericTransitionTimeTest {

    @Test
    public void testResolutionSecondsGivesExpectedValueWithinRange() {
        GenericTransitionTime timeStart = new GenericTransitionTime(
                GenericTransitionTime.TransitionResolution.SECOND,
                GenericTransitionTime.TransitionStep.Specific(0)
        );

        assertEquals(64, timeStart.getValue());

        GenericTransitionTime timeHigh = new GenericTransitionTime(
                GenericTransitionTime.TransitionResolution.SECOND,
                GenericTransitionTime.TransitionStep.Specific(62)
        );

        assertEquals(126, timeHigh.getValue());
    }

    @Test
    public void testResolutionHundredMillisecondsGivesExpectedValueWithinRange() {
        GenericTransitionTime timeStart = new GenericTransitionTime(
                GenericTransitionTime.TransitionResolution.HUNDRED_MILLISECONDS,
                GenericTransitionTime.TransitionStep.Specific(0)
        );

        assertEquals(0, timeStart.getValue());

        GenericTransitionTime timeHigh = new GenericTransitionTime(
                GenericTransitionTime.TransitionResolution.HUNDRED_MILLISECONDS,
                GenericTransitionTime.TransitionStep.Specific(62)
        );

        assertEquals(62, timeHigh.getValue());
    }

    @Test
    public void testResolutionTenSecondsGivesExpectedValueWithinRange() {
        GenericTransitionTime timeStart = new GenericTransitionTime(
                GenericTransitionTime.TransitionResolution.TEN_SECONDS,
                GenericTransitionTime.TransitionStep.Specific(0)
        );

        assertEquals(128, timeStart.getValue());

        GenericTransitionTime timeHigh = new GenericTransitionTime(
                GenericTransitionTime.TransitionResolution.TEN_SECONDS,
                GenericTransitionTime.TransitionStep.Specific(62)
        );

        assertEquals(190, timeHigh.getValue());
    }

    @Test
    public void testResolutionTenMinutesGivesExpectedValueWithinRange() {
        GenericTransitionTime timeStart = new GenericTransitionTime(
                GenericTransitionTime.TransitionResolution.TEN_MINUTES,
                GenericTransitionTime.TransitionStep.Specific(0)
        );

        assertEquals(192, timeStart.getValue());

        GenericTransitionTime timeHigh = new GenericTransitionTime(
                GenericTransitionTime.TransitionResolution.TEN_MINUTES,
                GenericTransitionTime.TransitionStep.Specific(62)
        );

        assertEquals(254, timeHigh.getValue());
    }

    @Test
    public void testGivenValueGivesExpectedTransitionTimeSecond() {
        GenericTransitionTime time = new GenericTransitionTime(72);

        assertEquals(GenericTransitionTime.TransitionResolution.SECOND, time.resolution);
        assertEquals(GenericTransitionTime.TransitionStep.Specific(8), time.transitionStep);
    }

    @Test
    public void testGivenValueGivesExpectedTransitionTimeHundredMilliseconds() {
        GenericTransitionTime time = new GenericTransitionTime(8);

        assertEquals(GenericTransitionTime.TransitionResolution.HUNDRED_MILLISECONDS, time.resolution);
        assertEquals(GenericTransitionTime.TransitionStep.Specific(8), time.transitionStep);
    }

    @Test
    public void testGivenValueGivesExpectedTransitionTimeHundredTenSeconds() {
        GenericTransitionTime time = new GenericTransitionTime(136);

        assertEquals(GenericTransitionTime.TransitionResolution.TEN_SECONDS, time.resolution);
        assertEquals(GenericTransitionTime.TransitionStep.Specific(8), time.transitionStep);
    }

    @Test
    public void testGivenValueGivesExpectedTransitionTimeHundredTenMinutes() {
        GenericTransitionTime time = new GenericTransitionTime(200);

        assertEquals(GenericTransitionTime.TransitionResolution.TEN_MINUTES, time.resolution);
        assertEquals(GenericTransitionTime.TransitionStep.Specific(8), time.transitionStep);
    }
}