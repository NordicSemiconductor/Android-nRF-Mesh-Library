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

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Configuration message tests
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ConfigMessageTests {

    @Test
    public void createConfigAppKeyAdd() {
        //Message 6
        final String expectedAccessPayload = "0023614563964771734fbd76e3b40519d1d94a48".toUpperCase(Locale.US);
        final NetworkKey netKey = new NetworkKey(MeshParserUtils.hexToInt("0123"), MeshParserUtils.toByteArray("7dd7364cd842ad18c17c2b820c84c3d6"));
        final ApplicationKey applicationKey = new ApplicationKey(MeshParserUtils.hexToInt("0456"), MeshParserUtils.toByteArray("63964771734fbd76e3b40519d1d94a48"));
        final ConfigAppKeyAdd configAppKeyAdd = new ConfigAppKeyAdd(netKey, applicationKey);
        final ByteBuffer buffer = ByteBuffer.allocate(configAppKeyAdd.getParameters().length + 1);
        buffer.put((byte) configAppKeyAdd.getOpCode());
        buffer.put(configAppKeyAdd.getParameters());
        final byte[] accessPayload = buffer.array();
        assertEquals(expectedAccessPayload, MeshParserUtils.bytesToHex(accessPayload, false));
    }

    @Test
    public void parseConfigAppKeyStatus() {
        //based on Message 6
        final int expectedNetKeyIndex = MeshParserUtils.hexToInt("0123");
        final int expectedAppKeyIndex = MeshParserUtils.hexToInt("0456");
        final AccessMessage message = new AccessMessage();
        message.setOpCode(MeshParserUtils.hexToInt("8003"));
        message.setParameters(MeshParserUtils.toByteArray("00236145".toUpperCase(Locale.US)));

        final ConfigAppKeyStatus configAppKeyStatus = new ConfigAppKeyStatus(message);
        final int actualNetKeyIndex = configAppKeyStatus.getNetKeyIndex();
        final int actualAppKeyIndex = configAppKeyStatus.getAppKeyIndex();
        assertEquals(expectedNetKeyIndex, actualNetKeyIndex);
        assertEquals(expectedAppKeyIndex, actualAppKeyIndex);
    }

    @Test
    public void createConfigNetKeyAdd() {
        //Message 6
        final String expectedAccessPayload = "804023017dd7364cd842ad18c17c2b820c84c3d6".toUpperCase(Locale.US);
        final NetworkKey netKey = new NetworkKey(MeshParserUtils.hexToInt("0123"), MeshParserUtils.toByteArray("7dd7364cd842ad18c17c2b820c84c3d6"));
        final ConfigNetKeyAdd configNetKeyAdd = new ConfigNetKeyAdd(netKey);
        final ByteBuffer buffer = ByteBuffer.allocate(configNetKeyAdd.getParameters().length + 2);
        buffer.putShort((short) configNetKeyAdd.getOpCode());
        buffer.put(configNetKeyAdd.getParameters());
        final byte[] accessPayload = buffer.array();
        assertEquals(expectedAccessPayload, MeshParserUtils.bytesToHex(accessPayload, false));
    }

    @Test
    public void parseConfigNetKeyStatus() {
        //based on Message 6
        final int expectedNetKeyIndex = MeshParserUtils.hexToInt("0123");
        final AccessMessage message = new AccessMessage();
        message.setOpCode(MeshParserUtils.hexToInt("8040"));
        message.setParameters(MeshParserUtils.toByteArray("002301".toUpperCase(Locale.US)));

        final ConfigNetKeyStatus configAppKeyStatus = new ConfigNetKeyStatus(message);
        final int actualNetKeyIndex = configAppKeyStatus.getNetKeyIndex();
        assertEquals(expectedNetKeyIndex, actualNetKeyIndex);
    }

    @Test
    public void parseConfigNetKeyList() {
        //based on Message 6
        final List<Integer> keyIndexes = new ArrayList<>();
        keyIndexes.add(MeshParserUtils.hexToInt("0123"));
        keyIndexes.add(MeshParserUtils.hexToInt("0456"));
        keyIndexes.add(MeshParserUtils.hexToInt("0789"));
        final AccessMessage message = new AccessMessage();
        message.setOpCode(MeshParserUtils.hexToInt("8040"));
        message.setParameters(MeshParserUtils.toByteArray("2361458907".toUpperCase(Locale.US)));

        final ConfigNetKeyList configNetKeyList = new ConfigNetKeyList(message);
        assertArrayEquals(keyIndexes.toArray(), configNetKeyList.getKeyIndexes().toArray());
    }
}