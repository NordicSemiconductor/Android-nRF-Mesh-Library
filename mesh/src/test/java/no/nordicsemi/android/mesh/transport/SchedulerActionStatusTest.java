package no.nordicsemi.android.mesh.transport;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Arrays;

import no.nordicsemi.android.mesh.data.GenericTransitionTime;
import no.nordicsemi.android.mesh.data.ScheduleEntry;

import static org.junit.Assert.assertEquals;

public class SchedulerActionStatusTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    byte[] schedulerData = new byte[]{(byte)0x33,(byte) 0x33,(byte) 0x48,(byte) 0x1C,(byte) 0x16, (byte) 0xBC, (byte) 0xC4, (byte)0x50,(byte) 0x11,(byte) 0x40};

    @Mock
    AccessMessage accessMessage;

    @Test
    public void test_reading_data() {
        ScheduleEntry expectedEntry = new ScheduleEntry()
                .setYear(ScheduleEntry.Year.Specific(20))
                .setMonth(ScheduleEntry.Month.Any(
                        Arrays.asList(ScheduleEntry.Month.FEBRUARY, ScheduleEntry.Month.OCTOBER, ScheduleEntry.Month.DECEMBER)))
                .setDay(ScheduleEntry.Day.Value(8))
                .setHour(ScheduleEntry.Hour.Value(12))
                .setMinute(ScheduleEntry.Minute.Value(30))
                .setSecond(ScheduleEntry.Second.Value(45))
                .setDayOfWeek(ScheduleEntry.DayOfWeek.Any(new ArrayList<>(
                        Arrays.asList(ScheduleEntry.DayOfWeek.SATURDAY, ScheduleEntry.DayOfWeek.SUNDAY))))
                .setAction(ScheduleEntry.Action.TurnOn)
                .setGenericTransitionTime(new GenericTransitionTime(GenericTransitionTime.TransitionResolution.SECOND, GenericTransitionTime.TransitionStep.Specific(8)))
                .setScene(ScheduleEntry.Scene.Address(0x3333));

        Mockito.when(accessMessage.getParameters()).thenReturn(schedulerData);

        SchedulerActionStatus sut = new SchedulerActionStatus(accessMessage);

        assertEquals(0, sut.getIndex());
        assertEquals(expectedEntry, sut.getEntry());

    }
}
