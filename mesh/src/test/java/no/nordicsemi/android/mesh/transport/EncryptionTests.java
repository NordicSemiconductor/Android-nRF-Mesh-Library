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

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class EncryptionTests {

    @Test
    public void k2_master_isCorrect() {
        final byte[] n = MeshParserUtils.toByteArray("f7a2a44f8e8a8029064f173ddc1e2b00");
        final SecureUtils.K2Output k2Output = SecureUtils.calculateK2(n, SecureUtils.K2_MASTER_INPUT);
        final int nid = 0x7f;
        assertEquals(nid, k2Output.getNid());

        final byte[] encryptionKey = MeshParserUtils.toByteArray("9f589181a0f50de73c8070c7a6d27f46");
        assertArrayEquals(encryptionKey, k2Output.getEncryptionKey());

        final byte[] privacyKey = MeshParserUtils.toByteArray("4c715bd4a64b938f99b453351653124f");
        assertArrayEquals(privacyKey, k2Output.getPrivacyKey());
    }

    @Test
    public void k2_friendship_isCorrect() {
        final byte[] n = MeshParserUtils.toByteArray("f7a2a44f8e8a8029064f173ddc1e2b00");
        final byte[] p = MeshParserUtils.toByteArray("010203040506070809");
        final SecureUtils.K2Output k2Output = SecureUtils.calculateK2(n, p);
        final int nid = 0x73;
        assertEquals(nid, k2Output.getNid());

        final byte[] encryptionKey = MeshParserUtils.toByteArray("11efec0642774992510fb5929646df49");
        assertArrayEquals(encryptionKey, k2Output.getEncryptionKey());

        final byte[] privacyKey = MeshParserUtils.toByteArray("d4d7cc0dfa772d836a8df9df5510d7a7");
        assertArrayEquals(privacyKey, k2Output.getPrivacyKey());
    }

    @Test
    public void k3_isCorrect() {
        final byte[] n = MeshParserUtils.toByteArray("f7a2a44f8e8a8029064f173ddc1e2b00");
        final byte[] networkId = MeshParserUtils.toByteArray("ff046958233db014");
        assertArrayEquals(networkId, SecureUtils.calculateK3(n));
    }

    @Test
    public void k4_isCorrect() {
        final byte[] n = MeshParserUtils.toByteArray("3216d1509884b533248541792b877f98");
        final int aid = 0x38;
        assertEquals(aid, SecureUtils.calculateK4(n));
    }

    @Test
    public void application_key_id_isCorrect() {
        //8.2.1
        final String expectedPayload = "56341263964771734fbd76e3b40519d1d94a48".toUpperCase(Locale.US);
        final byte[] appKey = MeshParserUtils.toByteArray("63964771734fbd76e3b40519d1d94a48");
        final int netKeyIndex = 0x123;
        final byte[] networkKeyIndex = {(byte) (netKeyIndex & 0xFF), ((netKeyIndex >> 8) & 0xFF)};

        final int appKeyIndex = 0x456;
        final byte[] applicationKeyIndex = {(byte) (appKeyIndex & 0xFF), ((appKeyIndex >> 8) & 0xFF)};
        final ByteBuffer buffer = ByteBuffer.allocate(19).order(ByteOrder.BIG_ENDIAN);
        buffer.put(applicationKeyIndex[0]);
        buffer.put((byte) (((networkKeyIndex[0] & 0x0F) << 4) | applicationKeyIndex[1]));
        buffer.put((byte) ((((networkKeyIndex[1] & 0x0F) << 4) | ((networkKeyIndex[0] & 0xF0) >> 4))));
        buffer.put(appKey);
        assertEquals(expectedPayload, MeshParserUtils.bytesToHex(buffer.array(), false));
    }

    @Test
    public void identity_key_isCorrect() {
        //8.2.5
        final String expectedIdentityKey = "84396c435ac48560b5965385253e210c".toUpperCase(Locale.US);
        final byte[] networkKey = MeshParserUtils.toByteArray("7dd7364cd842ad18c17c2b820c84c3d6".toUpperCase(Locale.US));

        assertEquals(expectedIdentityKey, MeshParserUtils.bytesToHex(SecureUtils.calculateIdentityKey(networkKey), false));
    }

    @Test
    public void beacon_key_isCorrect() {
        //8.2.6
        final String expectedBeaconKey = "5423d967da639a99cb02231a83f7d254".toUpperCase(Locale.US);
        final byte[] networkKey = MeshParserUtils.toByteArray("7dd7364cd842ad18c17c2b820c84c3d6".toUpperCase(Locale.US));

        assertEquals(expectedBeaconKey, MeshParserUtils.bytesToHex(SecureUtils.calculateBeaconKey(networkKey), false));
    }

    @Test
    public void secure_network_beacon_isCorrect() {
        //8.2.6
        final byte[] ivIndex = MeshParserUtils.toByteArray("12345678");
        final int flags = 0x00;
        final byte[] networkId = MeshParserUtils.toByteArray("3ecaff672f673370");
        final byte[] authenticationValue = MeshParserUtils.toByteArray("8ea261582f364f6f3c74ef80336ca17e");

        final String expectedSecureNetworkBeacon = "01003ecaff672f673370123456788ea261582f364f6f".toUpperCase(Locale.US);
        final byte[] networkKey = MeshParserUtils.toByteArray("7dd7364cd842ad18c17c2b820c84c3d6".toUpperCase(Locale.US));

        assertEquals(expectedSecureNetworkBeacon,
                MeshParserUtils.bytesToHex(SecureUtils.calculateSecureNetworkBeacon(networkKey, 1,
                        flags, networkId, MeshParserUtils.bytesToInt(ivIndex)), false));
    }

    @Test
    public void parse_node_id_isCorrect() {
        //8.2.5
        final byte[] advertisingData = MeshParserUtils.toByteArray("141628180100861765aefcc57b34ae608fbbc1f2c6".toUpperCase(Locale.US));
        final byte[] identityKey = MeshParserUtils.toByteArray("84396c435ac48560b5965385253e210c");
        final ByteBuffer expectedBufferHash = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        expectedBufferHash.put(advertisingData, 5, 8);
        final byte[] hash = expectedBufferHash.array();
        final String expectedHash = MeshParserUtils.bytesToHex(hash, false);
        final byte[] src = MeshParserUtils.toByteArray("1201");
        final byte[] random = MeshParserUtils.toByteArray("34ae608fbbc1f2c6");

        final byte[] actualHash = SecureUtils.calculateHash(identityKey, random, src);
        assertEquals(expectedHash, MeshParserUtils.bytesToHex(actualHash, false));
    }

    @Test
    public void networkId() {
        final byte[] networkKey = MeshParserUtils.toByteArray("7dd7364cd842ad18c17c2b820c84c3d6".toUpperCase(Locale.US));
        final String networkIDText = MeshParserUtils.bytesToHex(SecureUtils.calculateK3(networkKey), false).toLowerCase();
        final String expectedNetworkId = "3ecaff672f673370";
        assertEquals(expectedNetworkId, networkIDText);
    }

    @Test
    public void isLabelUuidCorrect(){
        //Message #22
        final UUID uuid = UUID.fromString("0073e7e4-d8b9-440f-af84-15df4c56c0e1");
        final List<UUID> uuids = new ArrayList<>();
        uuids.add(uuid);
        final int address = 0xB529;
        Assert.assertEquals(uuid, MeshAddress.getLabelUuid(uuids, address));
    }
}