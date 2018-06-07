package no.nordicsemi.android.meshprovisioner.configuration;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class EncryptionTests {

    @Test
    public void k2_master_isCorrect() throws Exception {
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
    public void k2_friendship_isCorrect() throws Exception {
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
    public void k3_isCorrect() throws Exception {
        final byte[] n = MeshParserUtils.toByteArray("f7a2a44f8e8a8029064f173ddc1e2b00");
        final byte[] networkId = MeshParserUtils.toByteArray("ff046958233db014");
        assertArrayEquals(networkId, SecureUtils.calculateK3(n));
    }

    @Test
    public void k4_isCorrect() throws Exception {
        final byte[] n = MeshParserUtils.toByteArray("3216d1509884b533248541792b877f98");
        final int aid = 0x38;
        assertEquals(aid, SecureUtils.calculateK4(n));
    }

    @Test
    public void application_key_id_isCorrect() throws Exception {
        //8.2.1
        final String expectedPayload = "56341263964771734fbd76e3b40519d1d94a48".toUpperCase();
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
        final String expectedIdentityKey = "84396c435ac48560b5965385253e210c".toUpperCase();
        final byte[] networkKey = MeshParserUtils.toByteArray("7dd7364cd842ad18c17c2b820c84c3d6".toUpperCase());

        assertEquals(expectedIdentityKey, MeshParserUtils.bytesToHex(SecureUtils.calculateIdentityKey(networkKey), false));
    }

    @Test
    public void parse_node_id_isCorrect() {
        //8.2.5
        final byte[] advertisingData = MeshParserUtils.toByteArray("141628180100861765aefcc57b34ae608fbbc1f2c6".toUpperCase());
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
        final byte[] networkKey = MeshParserUtils.toByteArray("7dd7364cd842ad18c17c2b820c84c3d6".toUpperCase());
        final String networkIDText = MeshParserUtils.bytesToHex(SecureUtils.calculateK3(networkKey), false).toLowerCase();
        final String expectedNetworkId = "3ecaff672f673370";
        assertEquals(expectedNetworkId, networkIDText);
    }
}