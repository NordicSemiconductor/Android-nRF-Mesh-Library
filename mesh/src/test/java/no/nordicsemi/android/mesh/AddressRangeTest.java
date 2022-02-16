package no.nordicsemi.android.mesh;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AddressRangeTest {

    @Test
    public void testIsInRange() {
        final AllocatedGroupRange range = new AllocatedGroupRange(0xC000, 0xD000);
        assertTrue(range.isInRange(0xC000));
        assertTrue(range.isInRange(0xD000));
        assertTrue(range.isInRange(0xCFFF));
        assertFalse(range.isInRange(0xFFFF));
    }

    @Test
    public void testIsInAnyRange() {
        final AllocatedGroupRange range = new AllocatedGroupRange(0xC000, 0xD000);
        final AllocatedGroupRange range2 = new AllocatedGroupRange(0xD100, 0xD300);
        final List<AllocatedGroupRange> ranges = Arrays.asList(range, range2);

        // Within first range
        assertTrue(AddressRange.isAddressInAnyRanges(ranges, 0xC000));
        assertTrue(AddressRange.isAddressInAnyRanges(ranges, 0xC800));
        assertTrue(AddressRange.isAddressInAnyRanges(ranges, 0xD000));

        // Within second range
        assertTrue(AddressRange.isAddressInAnyRanges(ranges, 0xD100));
        assertTrue(AddressRange.isAddressInAnyRanges(ranges, 0xD180));
        assertTrue(AddressRange.isAddressInAnyRanges(ranges, 0xD200));

        // Before, between and after range
        assertFalse(AddressRange.isAddressInAnyRanges(ranges, 0xA000));
        assertFalse(AddressRange.isAddressInAnyRanges(ranges, 0xD001));
        assertFalse(AddressRange.isAddressInAnyRanges(ranges, 0xFFFF));
    }
}
