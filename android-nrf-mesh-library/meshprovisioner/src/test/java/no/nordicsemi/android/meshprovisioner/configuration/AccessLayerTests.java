package no.nordicsemi.android.meshprovisioner.configuration;

import android.content.Context;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
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

        final String expectedAccessMessage = "d50a0048656c6c6f".toUpperCase();

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
        final String expectedAccessMessage = "d50a0048656c6c6f".toUpperCase();

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