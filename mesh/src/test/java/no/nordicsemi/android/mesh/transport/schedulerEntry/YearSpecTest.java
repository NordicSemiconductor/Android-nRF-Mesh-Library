package no.nordicsemi.android.mesh.transport.schedulerEntry;

import org.junit.Test;

import no.nordicsemi.android.mesh.data.ScheduleEntry;

import static org.junit.Assert.assertEquals;

public class YearSpecTest {

    @Test
    public void create_any_year_value(){
        ScheduleEntry.Year year = ScheduleEntry.Year.Any;

        assertEquals(100, year.getValue());
    }

    @Test
    public void create_specific_year_value(){
        ScheduleEntry.Year year = ScheduleEntry.Year.Specific(0x63);

        assertEquals(99, year.getValue());
    }

    @Test
    public void create_specific_year_value_max_value_is_99(){
        ScheduleEntry.Year year = ScheduleEntry.Year.Specific(0x68);

        assertEquals(99, year.getValue());
    }

}
