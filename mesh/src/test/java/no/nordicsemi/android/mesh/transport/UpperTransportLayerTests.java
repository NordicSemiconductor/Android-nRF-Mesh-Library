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
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
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
public class UpperTransportLayerTests {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    Context context;

    @Test
    public void create_upper_transport_pdu_access_message_isCorrect() {
        Mockito.mock(Log.class);
        final String expectedTransportPdu = "89511bf1d1a81c11dcef".toUpperCase(Locale.US);
        final byte[] deviceKey = MeshParserUtils.toByteArray("9d6dd0e96eb25dc19a40ed9914f8f03f");
        final int src = 0x1201;
        final int dst = 0x0003;
        final byte[] sequenceNumber = MeshParserUtils.toByteArray("000006");
        final byte[] ivIndex = MeshParserUtils.toByteArray("12345678");
        final int ctl = 0x00;
        final byte aszmic = 0;
        final int akf = 0;
        final byte[] accessPdu = MeshParserUtils.toByteArray("800300563412");

        final ProvisionedMeshNode meshNode = new ProvisionedMeshNode();
        final MeshTransport meshLayerTestBase = new MeshTransport(context, meshNode);
        final AccessMessage accessMessage = new AccessMessage();
        accessMessage.setSrc(src);
        accessMessage.setDst(dst);
        accessMessage.setSequenceNumber(sequenceNumber);
        accessMessage.setIvIndex(ivIndex);
        accessMessage.setDeviceKey(deviceKey);
        accessMessage.setAkf(akf);
        accessMessage.setAszmic(aszmic);
        accessMessage.setAccessPdu(accessPdu);
        meshLayerTestBase.createUpperTransportPDU(accessMessage);
        assertEquals(expectedTransportPdu, MeshParserUtils.bytesToHex(accessMessage.getUpperTransportPdu(), false));
    }
}