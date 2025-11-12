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

package no.nordicsemi.android.mesh.provisionerstates;


import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.jce.spec.ECNamedCurveParameterSpec;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.ECPublicKeySpec;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.BigIntegers;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.KeyAgreement;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.InternalTransportCallbacks;
import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.mesh.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.mesh.logger.MeshLogger;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

public class ProvisioningPublicKeyState extends ProvisioningState {

    private static final int PROVISIONING_PUBLIC_KEY_XY_PDU_LENGTH = 69;
    private final String TAG = ProvisioningPublicKeyState.class.getSimpleName();
    private final MeshProvisioningStatusCallbacks provisioningStatusCallbacks;
    private final UnprovisionedMeshNode node;
    private final InternalTransportCallbacks internalTransportCallbacks;

    private PrivateKey mProvisionerPrivateKey;

    /**
     * Constructs the provisioning publick key state.
     *
     * @param node                        {@link UnprovisionedMeshNode} node.
     * @param internalTransportCallbacks  {@link InternalTransportCallbacks} callbacks.
     * @param provisioningStatusCallbacks {@link MeshProvisioningStatusCallbacks} callbacks.
     */
    public ProvisioningPublicKeyState(final UnprovisionedMeshNode node,
                                      final InternalTransportCallbacks internalTransportCallbacks,
                                      final MeshProvisioningStatusCallbacks provisioningStatusCallbacks) {
        super();
        this.node = node;
        this.provisioningStatusCallbacks = provisioningStatusCallbacks;
        this.internalTransportCallbacks = internalTransportCallbacks;
    }

    @Override
    public State getState() {
        return State.PROVISIONING_PUBLIC_KEY;
    }

    @Override
    public void executeSend() {
        generateKeyPairs();
        final byte[] pdu = generatePublicKeyXYPDU();
        provisioningStatusCallbacks.onProvisioningStateChanged(node, States.PROVISIONING_PUBLIC_KEY_SENT, pdu);
        internalTransportCallbacks.sendProvisioningPdu(node, pdu);
    }

    @Override
    public boolean parseData(@NonNull final byte[] data) {
        if (node.getProvisioneePublicKeyXY() == null) {
            provisioningStatusCallbacks.onProvisioningStateChanged(node, States.PROVISIONING_PUBLIC_KEY_RECEIVED, data);
        }
        generateSharedECDHSecret(data);
        // Errata E16350 added an extra validation whether the received Public Key
        // is different than Provisioner's one.
        return !Arrays.equals(node.provisionerPublicKeyXY, node.provisioneePublicKeyXY);
    }

    private void generateKeyPairs() {
        try {
            final ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH", "SC");
            keyPairGenerator.initialize(parameterSpec);
            final KeyPair keyPair = keyPairGenerator.generateKeyPair();
            final ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

            mProvisionerPrivateKey = (ECPrivateKey) keyPair.getPrivate();

            final ECPoint point = publicKey.getQ();

            final BigInteger x = point.getXCoord().toBigInteger();
            final BigInteger y = point.getYCoord().toBigInteger();
            final byte[] tempX = BigIntegers.asUnsignedByteArray(32, x);
            final byte[] tempY = BigIntegers.asUnsignedByteArray(32, y);

            MeshLogger.verbose(TAG, "X: length: " + tempX.length + " " + MeshParserUtils.bytesToHex(tempX, false));
            MeshLogger.verbose(TAG, "Y: length: " + tempY.length + " " + MeshParserUtils.bytesToHex(tempY, false));

            final byte[] tempXY = new byte[64];
            System.arraycopy(tempX, 0, tempXY, 0, tempX.length);
            System.arraycopy(tempY, 0, tempXY, tempY.length, tempY.length);

            node.setProvisionerPublicKeyXY(tempXY);

            MeshLogger.verbose(TAG, "XY: " + MeshParserUtils.bytesToHex(tempXY, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] generatePublicKeyXYPDU() {
        final byte[] tempXY = node.getProvisionerPublicKeyXY();
        ByteBuffer buffer = ByteBuffer.allocate(tempXY.length + 2);
        buffer.put(MeshManagerApi.PDU_TYPE_PROVISIONING);
        buffer.put(TYPE_PROVISIONING_PUBLIC_KEY);
        buffer.put(tempXY);
        return buffer.array();
    }

    private void generateSharedECDHSecret(final byte[] xy) {
        if(node.getProvisioneePublicKeyXY() == null) {
            node.setProvisioneePublicKeyXY(xy);
        } else {
            // Mark the node as secure if the provisionee public key is not null.
            // This would assume that the key was obtained via an OOB method and is provided by the
            // user before starting provisioning.
            node.markAsSecure();
        }

        final byte[] xComponent = new byte[32];
        System.arraycopy(xy, 0, xComponent, 0, xComponent.length);

        final byte[] yComponent = new byte[32];
        System.arraycopy(xy, 32, yComponent, 0, xComponent.length);

        MeshLogger.verbose(TAG, "Provisionee X: " + MeshParserUtils.bytesToHex(yComponent, false));
        MeshLogger.verbose(TAG, "Provisionee Y: " + MeshParserUtils.bytesToHex(xComponent, false));

        final BigInteger x = BigIntegers.fromUnsignedByteArray(xy, 0, 32);
        final BigInteger y = BigIntegers.fromUnsignedByteArray(xy, 32, 32);

        final ECParameterSpec ecParameters = ECNamedCurveTable.getParameterSpec("secp256r1");
        ECCurve curve = ecParameters.getCurve();
        ECPoint ecPoint = curve.validatePoint(x, y);

        ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, ecParameters);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("ECDH", "SC");
            ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(keySpec);

            KeyAgreement a = KeyAgreement.getInstance("ECDH", "SC");
            a.init(mProvisionerPrivateKey);
            a.doPhase(publicKey, true);

            final byte[] sharedECDHSecret = a.generateSecret();
            node.setSharedECDHSecret(sharedECDHSecret);
            MeshLogger.verbose(TAG, "ECDH Secret: " + MeshParserUtils.bytesToHex(sharedECDHSecret, false));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
