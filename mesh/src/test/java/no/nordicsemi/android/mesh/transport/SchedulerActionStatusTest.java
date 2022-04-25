package no.nordicsemi.android.mesh.transport;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;

import no.nordicsemi.android.mesh.data.GenericTransitionTime;
import no.nordicsemi.android.mesh.data.ScheduleEntry;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

public class SchedulerActionStatusTest {

    @Test
    public void test_reading_data() {
        ScheduleEntry expectedEntry = new ScheduleEntry()
                .setYear(ScheduleEntry.Year.Any)
                .setMonth(ScheduleEntry.Month.Any(Arrays.asList(
                        ScheduleEntry.Month.JANUARY,
                        ScheduleEntry.Month.FEBRUARY,
                        ScheduleEntry.Month.MARCH,
                        ScheduleEntry.Month.APRIL,
                        ScheduleEntry.Month.MAY,
                        ScheduleEntry.Month.JUNE,
                        ScheduleEntry.Month.JULY,
                        ScheduleEntry.Month.AUGUST,
                        ScheduleEntry.Month.SEPTEMBER,
                        ScheduleEntry.Month.OCTOBER,
                        ScheduleEntry.Month.NOVEMBER,
                        ScheduleEntry.Month.DECEMBER)))
                .setDayOfWeek(ScheduleEntry.DayOfWeek.Any(Arrays.asList(ScheduleEntry.DayOfWeek.WEDNESDAY, ScheduleEntry.DayOfWeek.THURSDAY, ScheduleEntry.DayOfWeek.FRIDAY)))
                .setHour(ScheduleEntry.Hour.Value(11))
                .setMinute(ScheduleEntry.Minute.Value(30))
                .setSecond(ScheduleEntry.Second.Value(0))
                .setAction(ScheduleEntry.Action.TurnOff)
                .setGenericTransitionTime(new GenericTransitionTime(GenericTransitionTime.TransitionResolution.TEN_SECONDS, GenericTransitionTime.TransitionStep.Specific(17)));

        AccessMessage am = new AccessMessage();
        am.setParameters(MeshParserUtils.toByteArray("45FE7FB03C8003910000"));

        SchedulerActionStatus sut = new SchedulerActionStatus(am);

        assertEquals(5, sut.getIndex());
        assertEquals(expectedEntry, sut.getEntry());

    }
}
