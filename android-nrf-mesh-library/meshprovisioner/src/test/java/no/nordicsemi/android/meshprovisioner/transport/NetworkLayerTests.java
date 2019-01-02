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

package no.nordicsemi.android.meshprovisioner.transport;

import android.content.Context;
import android.util.SparseArray;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;
import no.nordicsemi.android.meshprovisioner.utils.SecureUtils;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class NetworkLayerTests {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private Context context;

    @Test
    public void create_network_pdu_isCorrect() {
        //Message #16

        final Map<Integer, String> expectedNetworkPdu = new HashMap<>();
        expectedNetworkPdu.put(0, "0068e80e5da5af0e6b9be7f5a642f2f98680e61c3a8b47f228".toUpperCase());

        final byte[] netkey = MeshParserUtils.toByteArray("7dd7364cd842ad18c17c2b820c84c3d6");

        final int ctl = 0x00;
        final int ttl = 0x0b;
        final byte[] sequenceNumber = MeshParserUtils.toByteArray("000006");
        final byte[] src = MeshParserUtils.toByteArray("1201");
        final byte[] dst = MeshParserUtils.toByteArray("0003");

        final byte[] lowerTransportPdu = MeshParserUtils.toByteArray("0089511bf1d1a81c11dcef".toUpperCase());
        final byte[] ivIndex = MeshParserUtils.toByteArray("12345678");

        final SecureUtils.K2Output k2Output = SecureUtils.calculateK2(netkey, SecureUtils.K2_MASTER_INPUT);

        final ProvisionedMeshNode meshNode = new ProvisionedMeshNode();
        meshNode.setK2Output(k2Output);

        final MeshTransport meshLayerTestBase = new MeshTransport(context, meshNode);
        final AccessMessage accessMessage = new AccessMessage();
        accessMessage.setTtl(ttl);
        accessMessage.setSrc(src);
        accessMessage.setDst(dst);
        accessMessage.setSequenceNumber(sequenceNumber);
        accessMessage.setIvIndex(ivIndex);
        final SparseArray<byte[]> lowerTransportAccessPdu = new SparseArray<>();
        lowerTransportAccessPdu.put(0, lowerTransportPdu);
        accessMessage.setLowerTransportAccessPdu(lowerTransportAccessPdu);

        final Message message = meshLayerTestBase.createNetworkLayerPDU(accessMessage);

        final SparseArray<byte[]> actualNetworkTransportPdu = message.getNetworkPdu();

        Assert.assertFalse("Segment count does not match", expectedNetworkPdu.size() != actualNetworkTransportPdu.size());

        if (expectedNetworkPdu.size() == actualNetworkTransportPdu.size()) {
            final int size = actualNetworkTransportPdu.size();
            for (int i = 0; i < size; i++) {
                final byte[] actualTransportPDU = actualNetworkTransportPdu.get(i);
                assertEquals(expectedNetworkPdu.get(i), MeshParserUtils.bytesToHex(actualTransportPDU, false));
            }
        }
    }

    @Test
    public void create_network_pdu_segmented_isCorrect() {
        //Message #6

        final Map<Integer, String> expectedNetworkPdu = new HashMap<>();
        expectedNetworkPdu.put(0, "0068cab5c5348a230afba8c63d4e686364979deaf4fd40961145939cda0e".toUpperCase());
        expectedNetworkPdu.put(1, "00681615b5dd4a846cae0c032bf0746f44f1b8cc8ce5edc57e55beed49c0".toUpperCase());

        final byte[] netkey = MeshParserUtils.toByteArray("7dd7364cd842ad18c17c2b820c84c3d6");

        final int ctl = 0x00;
        final int ttl = 0x04;
        final byte[] sequenceNumber = MeshParserUtils.toByteArray("3129ab");
        final byte[] src = MeshParserUtils.toByteArray("0003");
        final byte[] dst = MeshParserUtils.toByteArray("1201");

        final byte[] lowerTransportPdu0 = MeshParserUtils.toByteArray("8026ac01ee9dddfd2169326d23f3afdf".toUpperCase());
        final byte[] lowerTransportPdu1 = MeshParserUtils.toByteArray("8026ac21cfdc18c52fdef772e0e17308".toUpperCase());
        final byte[] ivIndex = MeshParserUtils.toByteArray("12345678");

        final SecureUtils.K2Output k2Output = SecureUtils.calculateK2(netkey, SecureUtils.K2_MASTER_INPUT);
        final ProvisionedMeshNode meshNode = new ProvisionedMeshNode();
        meshNode.setK2Output(k2Output);

        final MeshTransport meshLayerTestBase = new MeshTransport(context, meshNode);
        final AccessMessage accessMessage = new AccessMessage();
        accessMessage.setTtl(ttl);
        accessMessage.setSrc(src);
        accessMessage.setDst(dst);
        accessMessage.setSequenceNumber(sequenceNumber);
        accessMessage.setIvIndex(ivIndex);
        final SparseArray<byte[]> lowerTransportAccessPdu = new SparseArray<>();
        lowerTransportAccessPdu.put(0, lowerTransportPdu0);
        lowerTransportAccessPdu.put(1, lowerTransportPdu1);
        accessMessage.setLowerTransportAccessPdu(lowerTransportAccessPdu);

        final Message message = meshLayerTestBase.createNetworkLayerPDU(accessMessage);

        final SparseArray<byte[]> actualNetworkTransportPdu = message.getNetworkPdu();

        Assert.assertFalse("Segment count does not match", expectedNetworkPdu.size() != actualNetworkTransportPdu.size());

        if (expectedNetworkPdu.size() == actualNetworkTransportPdu.size()) {
            final int size = actualNetworkTransportPdu.size();
            for (int i = 0; i < size; i++) {
                final byte[] actualTransportPDU = actualNetworkTransportPdu.get(i);
                assertEquals(expectedNetworkPdu.get(i), MeshParserUtils.bytesToHex(actualTransportPDU, false));
            }
        }
    }

    @Test
    public void parseAccessMessage() {
        //Message #16
        final String expectedAccessPayload = "800300563412";
        final byte[] netkey = MeshParserUtils.toByteArray("7dd7364cd842ad18c17c2b820c84c3d6");

        final SecureUtils.K2Output k2Output = SecureUtils.calculateK2(netkey, SecureUtils.K2_MASTER_INPUT);
        final ProvisionedMeshNode meshNode = new ProvisionedMeshNode();
        meshNode.setK2Output(k2Output);
        meshNode.setIvIndex(MeshParserUtils.toByteArray("12345678"));
        meshNode.setDeviceKey(MeshParserUtils.toByteArray("9d6dd0e96eb25dc19a40ed9914f8f03f"));
        final byte[] pdu = MeshParserUtils.toByteArray("0068e80e5da5af0e6b9be7f5a642f2f98680e61c3a8b47f228");

        final MeshTransport meshLayerTestBase = new MeshTransport(context, meshNode);
        Message message = meshLayerTestBase.parsePdu(pdu);

        final String actualAccessPayload = MeshParserUtils.bytesToHex(((AccessMessage) message).getAccessPdu(), false);
        assertEquals(expectedAccessPayload, actualAccessPayload);

    }

    @Test
    public void parseSegmentedAccessMessage() {
        //Message #16
        final String expectedAccessPayload = "0056341263964771734fbd76e3b40519d1d94a48".toUpperCase();
        final byte[] netkey = MeshParserUtils.toByteArray("7dd7364cd842ad18c17c2b820c84c3d6");

        final SecureUtils.K2Output k2Output = SecureUtils.calculateK2(netkey, SecureUtils.K2_MASTER_INPUT);
        final ProvisionedMeshNode meshNode = new ProvisionedMeshNode();
        meshNode.setK2Output(k2Output);
        meshNode.setIvIndex(MeshParserUtils.toByteArray("12345678"));
        meshNode.setDeviceKey(MeshParserUtils.toByteArray("9d6dd0e96eb25dc19a40ed9914f8f03f"));
        //final byte [] pdu = MeshParserUtils.toByteArray("0068cab5c5348a230afba8c63d4e686364979deaf4fd40961145939cda0e");

        final ArrayList<byte[]> segmentedPdu = new ArrayList<>();
        segmentedPdu.add(MeshParserUtils.toByteArray("0068cab5c5348a230afba8c63d4e686364979deaf4fd40961145939cda0e"));
        segmentedPdu.add(MeshParserUtils.toByteArray("00681615b5dd4a846cae0c032bf0746f44f1b8cc8ce5edc57e55beed49c0"));
        final MeshTransport meshLayerTestBase = new MeshTransport(context, meshNode);

        for (byte[] pdu : segmentedPdu) {
            Message message = meshLayerTestBase.parsePdu(pdu);
            if (message != null) {
                final String actualAccessPayload = MeshParserUtils.bytesToHex(((AccessMessage) message).getAccessPdu(), false);
                assertEquals(expectedAccessPayload, actualAccessPayload);
            }
        }
    }



    @Test
    public void create_proxy_pdu_isCorrect() {
        //Message #6

        final Map<Integer, String> expectedProxyConfigurationPdu = new HashMap<>();
        expectedProxyConfigurationPdu.put(0, "0210386bd60efbbb8b8c28512e792d3711f4b526".toUpperCase());

        final byte[] netkey = MeshParserUtils.toByteArray("d1aafb2a1a3c281cbdb0e960edfad852");

        final int ctl = 0x00;
        final int ttl = 0x04;
        final byte[] sequenceNumber = MeshParserUtils.toByteArray("000001");
        final byte[] src = MeshParserUtils.toByteArray("0001");
        final byte[] dst = MeshParserUtils.toByteArray("0000");

        final byte[] lowerTransportPdu0 = MeshParserUtils.toByteArray("0000".toUpperCase());
        final byte[] ivIndex = MeshParserUtils.toByteArray("12345678");

        final SecureUtils.K2Output k2Output = SecureUtils.calculateK2(netkey, SecureUtils.K2_MASTER_INPUT);
        final ProvisionedMeshNode meshNode = new ProvisionedMeshNode();
        meshNode.setK2Output(k2Output);

        final MeshTransport meshLayerTestBase = new MeshTransport(context, meshNode);
        final AccessMessage accessMessage = new AccessMessage();
        accessMessage.setTtl(ttl);
        accessMessage.setSrc(src);
        accessMessage.setDst(dst);
        accessMessage.setSequenceNumber(sequenceNumber);
        accessMessage.setIvIndex(ivIndex);
        final SparseArray<byte[]> lowerTransportAccessPdu = new SparseArray<>();
        lowerTransportAccessPdu.put(0, lowerTransportPdu0);
        accessMessage.setLowerTransportAccessPdu(lowerTransportAccessPdu);

        final Message message = meshLayerTestBase.createNetworkLayerPDU(accessMessage);

        final SparseArray<byte[]> actualNetworkTransportPdu = message.getNetworkPdu();

        Assert.assertFalse("Segment count does not match", expectedProxyConfigurationPdu.size() != actualNetworkTransportPdu.size());

        if (expectedProxyConfigurationPdu.size() == actualNetworkTransportPdu.size()) {
            final int size = actualNetworkTransportPdu.size();
            for (int i = 0; i < size; i++) {
                final byte[] actualTransportPDU = actualNetworkTransportPdu.get(i);
                assertEquals(expectedProxyConfigurationPdu.get(i), MeshParserUtils.bytesToHex(actualTransportPDU, false));
            }
        }
    }
}