package no.nordicsemi.android.mesh.transport.schedulerEntry;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import no.nordicsemi.android.mesh.data.GenericTransitionTime;
import no.nordicsemi.android.mesh.data.ScheduleEntry;
import no.nordicsemi.android.mesh.utils.BitReader;
import no.nordicsemi.android.mesh.utils.BitWriter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class EntrySpecTest {

    @Test
    public void assemble_message_parameters(){
        // Field 0 (Year)           : 7 Bits   - 0x14       | 0b0010100
        // Field 1 (Month)          : 12 Bits  - 0x0A02     | 0b101000000010
        // Field 2 (Day)            : 5 Bits   - 0x8        | 0b01000
        // Field 3 (Hour)           : 5 Bits   - 0xC        | 0b01100
        // Field 4 (Minute)         : 6 Bits   - 0x1E       | 0b011110
        // Field 5 (Second)         : 6 Bits   - 0x2D       | 0b101101
        // Field 6 (DayOfWeek)      : 7 Bits   - 0x60       | 0b1100000
        // Field 7 (Action)         : 4 Bits   - 0x1        | 0b0001
        // Field 8 (TransitionTime) : 8 Bits   - 0x48       | 0b01001000
        // Field 9 (Scene)          : 16 Bits  - 0x3333     | 0b0011001100110011
        ScheduleEntry entry = new ScheduleEntry()
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
       BitWriter bitWriter = new BitWriter();
        entry.assembleMessageParameters(bitWriter);

        byte[] assembleMessageParameters = bitWriter.toByteArray();
        byte[] testData = new byte[]{(byte)0x33,(byte) 0x33,(byte) 0x48,(byte) 0x1C,(byte) 0x16, (byte) 0xBC, (byte) 0xC4, (byte)0x50,(byte) 0x11,(byte) 0x40};
        assertArrayEquals(testData, assembleMessageParameters);
    }

    @Test
    public void parse_status_parameters(){
        // Field 0 (Year)           : 7 Bits   - 0x14       | 0b0010100
        // Field 1 (Month)          : 12 Bits  - 0x0A02     | 0b101000000010
        // Field 2 (Day)            : 5 Bits   - 0x8        | 0b01000
        // Field 3 (Hour)           : 5 Bits   - 0xC        | 0b01100
        // Field 4 (Minute)         : 6 Bits   - 0x1E       | 0b011110
        // Field 5 (Second)         : 6 Bits   - 0x2D       | 0b101101
        // Field 6 (DayOfWeek)      : 7 Bits   - 0x60       | 0b1100000
        // Field 7 (Action)         : 4 Bits   - 0x1        | 0b0001
        // Field 8 (TransitionTime) : 8 Bits   - 0x48       | 0b01001000
        // Field 9 (Scene)          : 16 Bits  - 0x3333     | 0b0011001100110011
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

        byte[] testData = new byte[]{(byte)0x33,(byte) 0x33,(byte) 0x48,(byte) 0x1C,(byte) 0x16, (byte) 0xBC, (byte) 0xC4, (byte)0x50,(byte) 0x11,(byte) 0x40};
        ScheduleEntry entry = new ScheduleEntry(new BitReader(testData));
        assertEquals(expectedEntry, entry);
    }

}
