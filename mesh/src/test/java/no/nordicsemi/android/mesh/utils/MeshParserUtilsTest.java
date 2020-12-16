package no.nordicsemi.android.mesh.utils;

import junit.framework.TestCase;

import static no.nordicsemi.android.mesh.utils.MeshParserUtils.createVendorOpCode;
import static no.nordicsemi.android.mesh.utils.MeshParserUtils.getOpCode;
import static org.junit.Assert.assertArrayEquals;

public class MeshParserUtilsTest extends TestCase {
    public void testCreateVendorOpCode() {
        byte[] payload = createVendorOpCode(0x01, 0x05F1);
        byte[] expected = MeshParserUtils.toByteArray("C1F105");

        assertArrayEquals(expected, payload);
    }

    public void testGetOpCode_vendorOpCode() {
        byte[] payload = MeshParserUtils.toByteArray("C1F105");;

        int calculatedOpCode = getOpCode(payload, 3);

        assertEquals(0xC1F105, calculatedOpCode);
    }
}