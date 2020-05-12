/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.mesh.transport;

import android.content.Context;
import android.util.SparseArray;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Locale;

import no.nordicsemi.android.mesh.utils.MeshParserUtils;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
//TODO revisit this
public class LowerTransportLayerTests {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    Context context;

    @Test
    public void create_unsegmented_access_message_isCorrect() {
        //Message #16
        final byte[] expectedLowerTransportPdu = MeshParserUtils.toByteArray("0089511bf1d1a81c11dcef".toUpperCase(Locale.US));
        final byte[] deviceKey = MeshParserUtils.toByteArray("9d6dd0e96eb25dc19a40ed9914f8f03f");
        final int src = 0x1201;
        final int dst = 0x0003;
        final byte[] sequenceNumber = MeshParserUtils.toByteArray("000006");
        final byte[] ivIndex = MeshParserUtils.toByteArray("12345678");
        final byte aszmic = 0;
        final int akf = 0;
        final byte[] upperTransportPdu = MeshParserUtils.toByteArray("89511bf1d1a81c11dcef".toUpperCase(Locale.US));

        final MeshTransport meshLayerTestBase = new MeshTransport(context);
        final AccessMessage accessMessage = new AccessMessage();
        accessMessage.setSrc(src);
        accessMessage.setDst(dst);
        accessMessage.setSequenceNumber(sequenceNumber);
        accessMessage.setIvIndex(ivIndex);
        accessMessage.setDeviceKey(deviceKey);
        accessMessage.setAkf(akf);
        accessMessage.setAszmic(aszmic);
        accessMessage.setUpperTransportPdu(upperTransportPdu);

        meshLayerTestBase.createLowerTransportAccessPDU(accessMessage);

        final SparseArray<byte[]> actualTransportAccessPdu = accessMessage.getLowerTransportAccessPdu();
        Assert.assertArrayEquals(expectedLowerTransportPdu, actualTransportAccessPdu.get(0));
    }

    @Test
    public void create_segmented_access_message_isCorrect() {
        //Message #6

        final SparseArray<String> expectedSegmentedTransportPDU = new SparseArray<>();
        expectedSegmentedTransportPDU.put(0, "8026ac01ee9dddfd2169326d23f3afdf".toUpperCase(Locale.US));
        expectedSegmentedTransportPDU.put(1, "8026ac21cfdc18c52fdef772e0e17308".toUpperCase(Locale.US));

        final byte[] deviceKey = MeshParserUtils.toByteArray("9d6dd0e96eb25dc19a40ed9914f8f03f");
        final int src = 0x0003;
        final int dst = 0x1201;
        final byte[] sequenceNumber = MeshParserUtils.toByteArray("3129ab");
        final byte[] ivIndex = MeshParserUtils.toByteArray("12345678");
        final byte aszmic = 0;
        final int akf = 0;
        final byte[] upperTransportPdu = MeshParserUtils.toByteArray("ee9dddfd2169326d23f3afdfcfdc18c52fdef772e0e17308".toUpperCase(Locale.US));

        final MeshTransport meshLayerTestBase = new MeshTransport(context);
        final AccessMessage accessMessage = new AccessMessage();
        accessMessage.setSrc(src);
        accessMessage.setDst(dst);
        accessMessage.setSequenceNumber(sequenceNumber);
        accessMessage.setIvIndex(ivIndex);
        accessMessage.setDeviceKey(deviceKey);
        accessMessage.setAkf(akf);
        accessMessage.setAszmic(aszmic);
        accessMessage.setUpperTransportPdu(upperTransportPdu);

        meshLayerTestBase.createLowerTransportAccessPDU(accessMessage);

        final SparseArray<byte[]> actualSegmentedTransportPdu = accessMessage.getLowerTransportAccessPdu();

        Assert.assertTrue("Segment count does not match", expectedSegmentedTransportPDU.size() != actualSegmentedTransportPdu.size());

        if (expectedSegmentedTransportPDU.size() == actualSegmentedTransportPdu.size()) {
            final int size = actualSegmentedTransportPdu.size();
            for (int i = 0; i < size; i++) {
                final byte[] actualTransportPDU = actualSegmentedTransportPdu.get(i);
                assertEquals(expectedSegmentedTransportPDU.get(i), MeshParserUtils.bytesToHex(actualTransportPDU, false));
            }
        }
    }

    @Test
    public void create_unsegmented_control_message_isCorrect() {
        //Message #1
        final byte[] expectedLowerTransportPdu = MeshParserUtils.toByteArray("034b50057e400000010000");
        final int src = 0x1201;
        final int dst = 0xfffd;
        final byte[] sequenceNumber = MeshParserUtils.toByteArray("000006");
        final byte[] ivIndex = MeshParserUtils.toByteArray("12345678");
        final int opCode = 0x03;
        final byte aszmic = 0;
        final int akf = 0;
        final byte[] upperTransportPdu = MeshParserUtils.toByteArray("4b50057e400000010000".toUpperCase(Locale.US));

        final MeshTransport meshLayerTestBase = new MeshTransport(context);
        final ControlMessage controlMessage = new ControlMessage();
        controlMessage.setSrc(src);
        controlMessage.setDst(dst);
        controlMessage.setSequenceNumber(sequenceNumber);
        controlMessage.setIvIndex(ivIndex);
        controlMessage.setAkf(akf);
        controlMessage.setAszmic(aszmic);
        controlMessage.setOpCode(opCode);
        controlMessage.setTransportControlPdu(upperTransportPdu);

        meshLayerTestBase.createLowerTransportControlPDU(controlMessage);
        final SparseArray<byte[]> actualTransportAccessPdu = controlMessage.getLowerTransportControlPdu();
        Assert.assertArrayEquals(expectedLowerTransportPdu, actualTransportAccessPdu.get(0));
    }
}