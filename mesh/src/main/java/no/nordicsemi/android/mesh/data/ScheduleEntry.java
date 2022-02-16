package no.nordicsemi.android.mesh.data;

import java.security.InvalidParameterException;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import no.nordicsemi.android.mesh.utils.BitReader;
import no.nordicsemi.android.mesh.utils.BitWriter;

import static no.nordicsemi.android.mesh.data.GenericTransitionTime.TRANSITION_TIME_BITS_LENGTH;

/**
 * Represents an entry inside the schedule register.
 * <p>
 * The Year, Month, Day, Hour, Minute, and Second fields represent local time (i.e., after the TAI-UTC Delta and Time Zone Offset have been applied).
 * The fields have the meaning defined in ISO 8601 [15] (which replicates the "Gregorian" calendar in common use). Some of these values can either represent an exact value or a range of values when the scheduled action is performed.
 */
public class ScheduleEntry {

    public static final int SCHEDULER_ENTRY_PARAMS_BITS_LENGTH = 76;

    public Year year = Year.Any;
    public Month month = Month.Any(Collections.emptyList());
    public Day day = Day.Any;
    public Hour hour = Hour.Value(12);
    public Minute minute = Minute.Value(0);
    public Second second = Second.Value(0);
    public DayOfWeek dayOfWeek = DayOfWeek.Any(Collections.emptyList());
    public Action action = Action.NoAction;
    public Scene scene = Scene.NoScene;
    public GenericTransitionTime transitionTime = new GenericTransitionTime(GenericTransitionTime.TransitionResolution.SECOND, GenericTransitionTime.TransitionStep.Immediate);

    //region Setter
    public ScheduleEntry setYear(Year year) {
        this.year = year;
        return this;
    }

    public ScheduleEntry setMonth(Month month) {
        this.month = month;
        return this;
    }

    public ScheduleEntry setDay(Day day) {
        this.day = day;
        return this;
    }

    public ScheduleEntry setHour(Hour hour) {
        this.hour = hour;
        return this;
    }

    public ScheduleEntry setMinute(Minute minute) {
        this.minute = minute;
        return this;
    }

    public ScheduleEntry setSecond(Second second) {
        this.second = second;
        return this;
    }

    public ScheduleEntry setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        return this;
    }

    public ScheduleEntry setAction(Action action) {
        this.action = action;
        return this;
    }

    public ScheduleEntry setScene(Scene scene) {
        this.scene = scene;
        return this;
    }

    public ScheduleEntry setGenericTransitionTime(GenericTransitionTime genericTransitionTime) {
        this.transitionTime = genericTransitionTime;
        return this;
    }
    //endregion

    //region Entry types
    private abstract static class EntryType {
        protected int value;

        public int getValue() {
            return value;
        }

        protected EntryType(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntryType entry = (EntryType) o;
            return value == entry.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    /**
     * Scheduled year for the action (see Table 5.5 inside the specification)
     */
    public static final class Year extends EntryType {

        private Year(int value) {
            super(value);
        }

        public static Year Any = new Year(0x64);

        public static Year Specific(int lastTwoDigest) {
            return new Year(Math.min(lastTwoDigest, 99));
        }

        @Override
        public String toString() {
            return "Year{" +
                    "value=" + value +
                    '}';
        }
    }

    /**
     * Scheduled month for the action (see Table 5.6 inside the specification)
     */
    public static final class Month extends EntryType {

        private Month(int value) {
            super(value);
        }

        public static Month Any(List<Month> values) {
            BitSet bitSet = new BitSet(12);
            for (Month month : values) {
                BitSet monthBit = new BitSet();
                monthBit.set(month.value);
                bitSet.or(monthBit);
            }
            return new Month(calculateBitValue(bitSet));
        }

        public static Month JANUARY = new Month(1);
        public static Month FEBRUARY = new Month(1 << 1);
        public static Month MARCH = new Month(1 << 2);
        public static Month APRIL = new Month(1 << 3);
        public static Month MAY = new Month(1 << 4);
        public static Month JUNE = new Month(1 << 5);
        public static Month JULY = new Month(1 << 6);
        public static Month AUGUST = new Month(1 << 7);
        public static Month SEPTEMBER = new Month(1 << 8);
        public static Month OCTOBER = new Month(1 << 9);
        public static Month NOVEMBER = new Month(1 << 10);
        public static Month DECEMBER = new Month(1 << 11);

        @Override
        public String toString() {
            return "Month{" +
                    "value=" + value +
                    '}';
        }
    }

    /**
     * Scheduled day of the month for the action (see Table 5.7 inside the specification)
     */
    public static final class Day extends EntryType {

        private Day(int value) {
            super(value);
        }

        public static Day Any = new Day(0x00);

        public static Day Value(int value) {
            return new Day(Math.min(value, 31));
        }

        @Override
        public String toString() {
            return "Day{" +
                    "value=" + value +
                    '}';
        }
    }

    /**
     * Scheduled hour for the action (see Table 5.8 inside the specification)
     */
    public static final class Hour extends EntryType {
        private Hour(int value) {
            super(value);
        }

        public static Hour Value(int value) {
            return new Hour(Math.min(value, 23));
        }

        public static Hour Any = new Hour(0x18);
        public static Hour Random = new Hour(0x19);

        @Override
        public String toString() {
            return "Hour{" +
                    "value=" + value +
                    '}';
        }
    }

    /**
     * Scheduled minute for the action (see Table 5.9 inside the specification)
     */
    public static final class Minute extends EntryType {

        private Minute(int value) {
            super(value);
        }

        public static Minute Value(int value) {
            return new Minute(Math.min(value, 59));
        }

        public static Minute Any = new Minute(0x3C);
        public static Minute Every15 = new Minute(0x3D);
        public static Minute Every20 = new Minute(0x3E);
        public static Minute Random = new Minute(0x3F);

        @Override
        public String toString() {
            return "Minute{" +
                    "value=" + value +
                    '}';
        }
    }

    /**
     * Scheduled second for the action (see Table 5.10 inside the specification)
     */
    public static final class Second extends EntryType {

        private Second(int value) {
            super(value);
        }

        public static Second Value(int value) {
            return new Second(Math.min(value, 59));
        }

        public static Second Any = new Second(0x3C);
        public static Second Every15 = new Second(0x3D);
        public static Second Every20 = new Second(0x3E);
        public static Second Random = new Second(0x3F);

        @Override
        public String toString() {
            return "Second{" +
                    "value=" + value +
                    '}';
        }
    }

    /**
     * Schedule days of the week for the action (see Table 5.11 inside the specification)
     */
    public static final class DayOfWeek extends EntryType {

        private DayOfWeek(int value) {
            super(value);
        }

        public static DayOfWeek Any(List<DayOfWeek> values) {
            BitSet bitSet = new BitSet(12);
            for (DayOfWeek dayOfWeek : values) {
                BitSet dayOfWeekBit = new BitSet();
                dayOfWeekBit.set(dayOfWeek.value);
                bitSet.or(dayOfWeekBit);
            }
            return new DayOfWeek(calculateBitValue(bitSet));
        }

        public static DayOfWeek MONDAY = new DayOfWeek(1);
        public static DayOfWeek TUESDAY = new DayOfWeek(1 << 1);
        public static DayOfWeek WEDNESDAY = new DayOfWeek(1 << 2);
        public static DayOfWeek THURSDAY = new DayOfWeek(1 << 3);
        public static DayOfWeek FRIDAY = new DayOfWeek(1 << 4);
        public static DayOfWeek SATURDAY = new DayOfWeek(1 << 5);
        public static DayOfWeek SUNDAY = new DayOfWeek(1 << 6);

        @Override
        public String toString() {
            return "DayOfWeek{" +
                    "value=" + value +
                    '}';
        }
    }

    /**
     * Action to be performed at the scheduled time (see Table 5.12 inside the specification)
     */
    public static final class Action extends EntryType {

        private Action(int value) {
            super(value);
        }

        public static Action TurnOff = new Action(0x0);
        public static Action TurnOn = new Action(0x1);
        public static Action SceneRecall = new Action(0x2);
        public static Action NoAction = new Action(0xF);

        @Override
        public String toString() {
            return "Action{" +
                    "value=" + value +
                    '}';
        }
    }

    /**
     * Scene number to be used for some actions (see Table 5.13 inside the specification)
     */
    public static final class Scene extends EntryType {

        private Scene(int value) {
            super(value);
        }

        public static Scene NoScene = new Scene(0);

        public static Scene Address(int address) {
            return new Scene(address);
        }

        @Override
        public String toString() {
            return "Scene{" +
                    "value=" + value +
                    '}';
        }
    }

    //endregion

    //region Read & Write data
    public void assembleMessageParameters(BitWriter bitWriter) {
        bitWriter.write(scene.value, 16);
        bitWriter.write(transitionTime.getValue(), 8);
        bitWriter.write(action.value, 4);
        bitWriter.write(dayOfWeek.value, 7);
        bitWriter.write(second.value, 6);
        bitWriter.write(minute.value, 6);
        bitWriter.write(hour.value, 5);
        bitWriter.write(day.value, 5);
        bitWriter.write(month.value, 12);
        bitWriter.write(year.value, 7);
    }

    public ScheduleEntry(BitReader bitReader) {
        if (bitReader.bitsLeft() <= SCHEDULER_ENTRY_PARAMS_BITS_LENGTH) {
            throw new InvalidParameterException("Bitreader has not enough bits");
        }
        scene = new Scene(bitReader.getBits(16));
        transitionTime = new GenericTransitionTime(bitReader.getBits(TRANSITION_TIME_BITS_LENGTH));
        action = new Action(bitReader.getBits(4));
        dayOfWeek = new DayOfWeek(bitReader.getBits(7));
        second = new Second(bitReader.getBits(6));
        minute = new Minute(bitReader.getBits(6));
        hour = new Hour(bitReader.getBits(5));
        day = new Day(bitReader.getBits(5));
        month = new Month(bitReader.getBits(12));
        year = new Year(bitReader.getBits(7));
    }

    //endregion

    public ScheduleEntry() {
    }

    /**
     * Iterates through the bitset and adds every Bit, which is set to true, to a value.
     *
     * @param bitSet The bitset.
     */
    public static int calculateBitValue(BitSet bitSet) {
        int value = 0;
        for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
            value += i;
        }
        return value;
    }

    //region Equals & Hash
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleEntry entry = (ScheduleEntry) o;
        return year.equals(entry.year) &&
                month.equals(entry.month) &&
                day.equals(entry.day) &&
                hour.equals(entry.hour) &&
                minute.equals(entry.minute) &&
                second.equals(entry.second) &&
                dayOfWeek.equals(entry.dayOfWeek) &&
                action.equals(entry.action) &&
                scene.equals(entry.scene) &&
                transitionTime.equals(entry.transitionTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, day, hour, minute, second, dayOfWeek, action, scene, transitionTime);
    }

    @Override
    public String toString() {
        return "ScheduleEntry{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                ", dayOfWeek=" + dayOfWeek +
                ", action=" + action +
                ", scene=" + scene +
                ", transitionTime=" + transitionTime +
                '}';
    }

    //endregion
}
