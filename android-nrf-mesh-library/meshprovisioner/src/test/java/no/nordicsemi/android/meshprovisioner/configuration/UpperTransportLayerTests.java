package no.nordicsemi.android.meshprovisioner.configuration;

import android.content.Context;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nordicsemi.android.meshprovisioner.messages.AccessMessage;
import no.nordicsemi.android.meshprovisioner.utils.MeshParserUtils;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UpperTransportLayerTests {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    Context context;

    @Test
    public void create_upper_transport_pdu_access_message_isCorrect() throws Exception {
        Mockito.mock(Log.class);
        final String expectedTransportPdu = "89511bf1d1a81c11dcef".toUpperCase();
        final byte[] deviceKey = MeshParserUtils.toByteArray("9d6dd0e96eb25dc19a40ed9914f8f03f");
        final byte[] src = MeshParserUtils.toByteArray("1201");
        final byte[] dst = MeshParserUtils.toByteArray("0003");
        final byte[] sequenceNumber = MeshParserUtils.toByteArray("000006");
        final byte[] ivIndex = MeshParserUtils.toByteArray("12345678");
        final int ctl = 0x00;
        final byte aszmic = 0;
        final int akf = 0;
        final byte[] accessPdu = MeshParserUtils.toByteArray("800300563412");

        final ProvisionedMeshNode meshNode = new ProvisionedMeshNode();
        final MeshTransport meshLayerTestBase = new MeshTransport(context, meshNode);
        final AccessMessage accessMessage = new AccessMessage();
        accessMessage.setCtl(ctl);
        accessMessage.setSrc(src);
        accessMessage.setDst(dst);
        accessMessage.setSequenceNumber(sequenceNumber);
        accessMessage.setIvIndex(ivIndex);
        accessMessage.setKey(deviceKey);
        accessMessage.setAkf(akf);
        accessMessage.setAszmic(aszmic);
        accessMessage.setAccessPdu(accessPdu);
        meshLayerTestBase.createUpperTransportPDU(accessMessage);
        assertEquals(expectedTransportPdu, MeshParserUtils.bytesToHex(accessMessage.getUpperTransportPdu(), false));
    }
}