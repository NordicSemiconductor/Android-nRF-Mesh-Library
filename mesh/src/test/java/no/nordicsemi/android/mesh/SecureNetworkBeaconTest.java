package no.nordicsemi.android.mesh;

import org.junit.Test;

import java.util.Calendar;

import no.nordicsemi.android.mesh.utils.MeshParserUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Roshan Rajaratnam on 24/04/2020.
 */
public class SecureNetworkBeaconTest {


    @Test
    public void testOverwritingWithTheSameIvIndex() {
        final byte[] data = MeshParserUtils.toByteArray("0102EE6C0EFF5298ECFF000000025E5AA7B268B5E044");
        final SecureNetworkBeacon snb = new SecureNetworkBeacon(data);
        final IvIndex ivIndex = new IvIndex(2, true, Calendar.getInstance());

        final Calendar rightNow = Calendar.getInstance();
        rightNow.add(Calendar.HOUR, -1);
        final boolean result = snb.canOverwrite(ivIndex, rightNow, false,
                false, false);

        assertTrue("Cannot overrwrite iv index with a baecon with same index", result);
    }

    @Test
    public void testOverwritingWithNextIvIndex() {
        final byte[] data = MeshParserUtils.toByteArray("0102EE6C0EFF5298ECFF000000025E5AA7B268B5E044");
        final SecureNetworkBeacon snb = new SecureNetworkBeacon(data);
        final IvIndex ivIndex = new IvIndex(1, false, Calendar.getInstance());

        final Calendar ninetySixHoursAgo = Calendar.getInstance();
        final Calendar almostNinetySixHoursAgo = (Calendar) ninetySixHoursAgo.clone();
        final Calendar moreThanNinetySixHoursAgo = (Calendar) ninetySixHoursAgo.clone();

        ninetySixHoursAgo.add(Calendar.HOUR, -96);
        almostNinetySixHoursAgo.add(Calendar.HOUR, -96);
        almostNinetySixHoursAgo.add(Calendar.SECOND, 10);
        moreThanNinetySixHoursAgo.add(Calendar.HOUR, -96);
        moreThanNinetySixHoursAgo.add(Calendar.SECOND, -10);

        // Less than 96 hours - test should fail
        final boolean result0 = snb.canOverwrite(ivIndex, almostNinetySixHoursAgo,
                false, false, false);

        // When previous IV Index was updated using IV Recovery, 96h requirement
        // does not apply. Test should pass.
        final boolean result1 = snb.canOverwrite(ivIndex, almostNinetySixHoursAgo,
                true, false, false);

        // It's ok. 96 hours have passed.
        final boolean result2 = snb.canOverwrite(ivIndex, ninetySixHoursAgo,
                false, false, false);

        // Now even more time passed, so updating IV Index is ok.
        final boolean result3 = snb.canOverwrite(ivIndex, moreThanNinetySixHoursAgo,
                false, false, false);

        assertFalse(result0);
        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
    }

    @Test
    public void testOverwritingWithNextIvIndexInTestMode() {
        final byte[] data = MeshParserUtils.toByteArray("0102EE6C0EFF5298ECFF000000025E5AA7B268B5E044");
        final SecureNetworkBeacon snb = new SecureNetworkBeacon(data);
        final IvIndex ivIndex = new IvIndex(1, false, Calendar.getInstance());

        final Calendar ninetySixHoursAgo = Calendar.getInstance();
        final Calendar almostNinetySixHoursAgo = (Calendar) ninetySixHoursAgo.clone();
        final Calendar moreThanNinetySixHoursAgo = (Calendar) ninetySixHoursAgo.clone();

        ninetySixHoursAgo.add(Calendar.HOUR, -96);
        almostNinetySixHoursAgo.add(Calendar.HOUR, -96);
        almostNinetySixHoursAgo.add(Calendar.SECOND, 10);
        moreThanNinetySixHoursAgo.add(Calendar.HOUR, -96);
        moreThanNinetySixHoursAgo.add(Calendar.SECOND, -10);

        // In test mode the 96h requirement does not apply.
        final boolean result0 = snb.canOverwrite(ivIndex, almostNinetySixHoursAgo,
                false, true, false);

        final boolean result1 = snb.canOverwrite(ivIndex, ninetySixHoursAgo,
                false, true, false);

        final boolean result2 = snb.canOverwrite(ivIndex, moreThanNinetySixHoursAgo,
                false, true, false);

        assertTrue(result0);
        assertTrue(result1);
        assertTrue(result2);
    }

    @Test
    public void testOverwritingWithFarIvIndex() {
        final byte[] data = MeshParserUtils.toByteArray("0102EE6C0EFF5298ECFF000000025E5AA7B268B5E044");
        final SecureNetworkBeacon snb = new SecureNetworkBeacon(data);
        final IvIndex ivIndex = new IvIndex(0, false, Calendar.getInstance());
        final Calendar currentTime = Calendar.getInstance();

        final Calendar ninetySixHoursAgo = (Calendar) currentTime.clone();
        ninetySixHoursAgo.add(Calendar.HOUR, -96);
        final Calendar almostNinetySixHoursAgo = (Calendar) ninetySixHoursAgo.clone();
        almostNinetySixHoursAgo.add(Calendar.SECOND, 10);
        final Calendar moreThanNinetySixHoursAgo = (Calendar) ninetySixHoursAgo.clone();
        moreThanNinetySixHoursAgo.add(Calendar.SECOND, -10);

        final Calendar twoHundredEightyEightHoursAgo = (Calendar) currentTime.clone();
        twoHundredEightyEightHoursAgo.add(Calendar.HOUR, -288);
        final Calendar almostTwoHundredEightyEightHoursAgo = (Calendar) twoHundredEightyEightHoursAgo.clone();
        almostTwoHundredEightyEightHoursAgo.add(Calendar.SECOND, 10);
        final Calendar moreThanTwoHundredEightyEightHoursAgo = (Calendar) twoHundredEightyEightHoursAgo.clone();
        moreThanTwoHundredEightyEightHoursAgo.add(Calendar.SECOND, -10);

        // 3 * 96 = 288 hours are required to pass since last IV Index update
        // for the IV Index to change from 0 (normal operation) to 2 (update active).
        // The following tests check if SNB cannot be updated before that.
        final boolean result0 = snb.canOverwrite(ivIndex, almostNinetySixHoursAgo,
                false, false, false);

        final boolean result1 = snb.canOverwrite(ivIndex, ninetySixHoursAgo,
                false, false, false);

        final boolean result2 = snb.canOverwrite(ivIndex, moreThanNinetySixHoursAgo,
                false, false, false);

        final boolean result3 = snb.canOverwrite(ivIndex, almostTwoHundredEightyEightHoursAgo,
                false, false, false);

        // 3 * 96 = 288 hours have passed. IV Index can be updated
        // from 0 (normal operation) to 2 (update active) using IV Recovery
        // procedure.
        final boolean result4 = snb.canOverwrite(ivIndex, twoHundredEightyEightHoursAgo,
                false, false, false);

        // Even more time has passed.
        final boolean result5 = snb.canOverwrite(ivIndex, moreThanTwoHundredEightyEightHoursAgo,
                false, false, false);

        assertFalse(result0);
        assertFalse(result1);
        assertFalse(result2);
        assertFalse(result3);
        assertTrue(result4);
        assertTrue(result5);
    }

    @Test
    public void testOverwritingWithFarIvIndexInTestMode() {
        final byte[] data = MeshParserUtils.toByteArray("0102EE6C0EFF5298ECFF000000025E5AA7B268B5E044");
        final SecureNetworkBeacon snb = new SecureNetworkBeacon(data);
        final IvIndex ivIndex = new IvIndex(0, false, Calendar.getInstance());

        final Calendar ninetySixHoursAgo = Calendar.getInstance();
        final Calendar almostNinetySixHoursAgo = (Calendar) ninetySixHoursAgo.clone();
        final Calendar moreThanNinetySixHoursAgo = (Calendar) ninetySixHoursAgo.clone();

        ninetySixHoursAgo.add(Calendar.HOUR, -96);
        almostNinetySixHoursAgo.add(Calendar.HOUR, -96);
        almostNinetySixHoursAgo.add(Calendar.SECOND, 10);
        moreThanNinetySixHoursAgo.add(Calendar.HOUR, -96);
        moreThanNinetySixHoursAgo.add(Calendar.SECOND, -10);

        // Test mode only removes 96h requirements to transition to the next
        // IV Index. Here we are updating from 0 (normal operation) to
        // 2 (update active), which is 3 steps. Test mode cannot help.
        final boolean result0 = snb.canOverwrite(ivIndex, almostNinetySixHoursAgo,
                false, true, false);

        final boolean result1 = snb.canOverwrite(ivIndex, ninetySixHoursAgo,
                false, true, false);

        final boolean result2 = snb.canOverwrite(ivIndex, moreThanNinetySixHoursAgo,
                false, true, false);

        assertFalse(result0);
        assertFalse(result1);
        assertFalse(result2);
    }

    @Test
    public void testOverwritingWithVeryFarIvIndex() {
        // This Secure Network Beacon has IV Index 52 (update active)
        final byte[] data = MeshParserUtils.toByteArray("0102EE6C0EFF5298ECFF00000034A53312BF9198C86F");
        final SecureNetworkBeacon snb = new SecureNetworkBeacon(data);
        final IvIndex ivIndex = new IvIndex(9, false, Calendar.getInstance());

        // The IV Index changes from 9 to 52, that is by 43. Also, the update active
        // flag changes from false to true, which adds one more step.
        // At least 43 * 192h + additional 96h are required for the IV Index to be
        // assumed valid.

        final Calendar longTimeAgo = Calendar.getInstance();
        longTimeAgo.add(Calendar.HOUR, -(42 * 192 + 96));

        final Calendar notThatLongTimeAgo = (Calendar) longTimeAgo.clone();
        notThatLongTimeAgo.add(Calendar.SECOND, 10);

        final Calendar longLongTimeAgo = (Calendar) longTimeAgo.clone();
        longLongTimeAgo.add(Calendar.SECOND, -10);

        final boolean result0 = snb.canOverwrite(ivIndex, notThatLongTimeAgo,
                false, false, false);

        final boolean result1 = snb.canOverwrite(ivIndex, longTimeAgo,
                false, false, false);

        final boolean result2 = snb.canOverwrite(ivIndex, longLongTimeAgo,
                false, false, false);

        final boolean result3 = snb.canOverwrite(ivIndex, notThatLongTimeAgo,
                false, false, true);

        final boolean result4 = snb.canOverwrite(ivIndex, longTimeAgo,
                false, false, true);

        final boolean result5 = snb.canOverwrite(ivIndex, longLongTimeAgo,
                false, false, true);

        // This test fails for 2 reasons: not enough time and IV Index change
        // exceeds limit of 42.
        assertFalse(result0);
        // Those tests should fails, as IV Index changed by more than 42.
        assertFalse(result1);
        assertFalse(result2);
        // This test returns false, as the time difference is not long enough.
        assertFalse(result3);
        // Those tests pass, as more than 43 * 192h + 96h have passed, and
        // the IV Index + 42 limit was turned off.
        assertTrue(result4);
        assertTrue(result5);
    }
}