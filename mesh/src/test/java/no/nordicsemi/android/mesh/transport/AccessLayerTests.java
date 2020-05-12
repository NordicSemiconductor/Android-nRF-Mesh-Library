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
public class AccessLayerTests {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    Context context;

    @Test
    public void create_access_message_isCorrect() {
        //Message 16
        final String expectedAccessMessage = "800300563412";

        final ProvisionedMeshNode meshNode = new ProvisionedMeshNode();
        final MeshTransport meshLayerTestBase = new MeshTransport(context, meshNode);
        final int opCode = 0x8003;
        final byte[] parameters = MeshParserUtils.toByteArray("00563412");
        final AccessMessage accessMessage = new AccessMessage();
        accessMessage.setOpCode(opCode);
        accessMessage.setParameters(parameters);
        meshLayerTestBase.createAccessMessage(accessMessage);
        final byte[] actualAccessMessage = accessMessage.getAccessPdu();
        assertEquals(expectedAccessMessage, MeshParserUtils.bytesToHex(actualAccessMessage, false));
    }

    @Test
    public void create_access_message_isCorrect1() {
        //Message 16
        final String expectedAccessMessage = "8008FF";
        final int opCode = 0x8008;
        final byte[] parameters = MeshParserUtils.toByteArray("FF");

        final AccessMessage accessMessage = new AccessMessage();
        accessMessage.setOpCode(opCode);
        accessMessage.setParameters(parameters);

        final ProvisionedMeshNode meshNode = new ProvisionedMeshNode();
        final MeshTransport meshLayerTestBase = new MeshTransport(context, meshNode);
        meshLayerTestBase.createAccessMessage(accessMessage);
        final byte[] actualAccessMessage = accessMessage.getAccessPdu();
        assertEquals(expectedAccessMessage, MeshParserUtils.bytesToHex(actualAccessMessage, false));
    }

    @Test
    public void create_custom_access_message_isCorrect() {
        //Message 21

        final String expectedAccessMessage = "d50a0048656c6c6f".toUpperCase(Locale.US);

        final int opCode = 0x15;
        final int companyIdentifier = 0x000A;
        final byte[] parameters = MeshParserUtils.toByteArray("48656c6c6f");

        final ProvisionedMeshNode meshNode = new ProvisionedMeshNode();

        final AccessMessage accessMessage = new AccessMessage();
        accessMessage.setOpCode(opCode);
        accessMessage.setParameters(parameters);
        accessMessage.setCompanyIdentifier(companyIdentifier);
        accessMessage.setParameters(parameters);
        final MeshTransport meshTransport = new MeshTransport(context, meshNode);
        meshTransport.createCustomAccessMessage(accessMessage);
        final byte[] actualAccessMessage = accessMessage.getAccessPdu();
        assertEquals(expectedAccessMessage, MeshParserUtils.bytesToHex(actualAccessMessage, false));
    }

    @Test
    public void create_custom_access_message_isCorrect1() {
        //Message 16
        final String expectedAccessMessage = "d50a0048656c6c6f".toUpperCase(Locale.US);

        final int opCode = 0x15;
        final int companyIdentifier = 0x000A;
        final byte[] parameters = MeshParserUtils.toByteArray("48656c6c6f");

        final AccessMessage accessMessage = new AccessMessage();
        accessMessage.setOpCode(opCode);
        accessMessage.setCompanyIdentifier(companyIdentifier);
        accessMessage.setParameters(parameters);

        final ProvisionedMeshNode meshNode = new ProvisionedMeshNode();
        final MeshTransport meshTransport = new MeshTransport(context, meshNode);
        meshTransport.createCustomAccessMessage(accessMessage);
        final byte[] actualAccessMessage = accessMessage.getAccessPdu();
        assertEquals(expectedAccessMessage, MeshParserUtils.bytesToHex(actualAccessMessage, false));
    }
}