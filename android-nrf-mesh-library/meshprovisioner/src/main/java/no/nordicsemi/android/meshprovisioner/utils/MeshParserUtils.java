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
package no.nordicsemi.android.meshprovisioner.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.SparseArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;

import no.nordicsemi.android.meshprovisioner.R;

@SuppressWarnings("WeakerAccess")
public class MeshParserUtils {

    private static final String PATTERN_NETWORK_KEY = "[0-9a-fA-F]{32}";
    private static final int TAI_YEAR = 2000;
    private static final int TAI_MONTH = 1;
    private static final int TAI_DATE = 1;

    private static final int PROHIBITED_DEFAULT_TTL_STATE_MIN = 0x01;
    private static final int PROHIBITED_DEFAULT_TTL_STATE_MID = 0x80;
    private static final int PROHIBITED_DEFAULT_TTL_STATE_MAX = 0xFF;
    public static final int USE_DEFAULT_TTL = 0xFF;

    private static final int MIN_TTL = 0x00;
    private static final int MAX_TTL = 0x7F;
    private static final int PROHIBITED_PUBLISH_TTL_MIN = 0x80;
    private static final int PROHIBITED_PUBLISH_TTL_MAX = 0xFE;

    private static final int IV_ADDRESS_MIN = 0;
    private static final int IV_ADDRESS_MAX = 4096;
    private static final int UNICAST_ADDRESS_MIN = 0;
    private static final char[] HEX_ARRAY = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static final int RESOLUTION_100_MS = 0b00;
    public static final int RESOLUTION_1_S = 0b01;
    public static final int RESOLUTION_10_S = 0b10;
    public static final int RESOLUTION_10_M = 0b11;

    public static final byte[] DISABLED_PUBLICATION_ADDRESS = new byte[]{0x00, 0x00};
    public static final int GENERIC_ON_OFF_5_MS = 5;

    public static String bytesToHex(final byte[] bytes, final boolean add0x) {
        if (bytes == null)
            return "";
        return bytesToHex(bytes, 0, bytes.length, add0x);
    }

    public static String bytesToHex(final byte[] bytes, final int start, final int length, final boolean add0x) {
        if (bytes == null || bytes.length <= start || length <= 0)
            return "";

        final int maxLength = Math.min(length, bytes.length - start);
        final char[] hexChars = new char[maxLength * 2];
        for (int j = 0; j < maxLength; j++) {
            final int v = bytes[start + j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        if (!add0x)
            return new String(hexChars);
        return "0x" + new String(hexChars);
    }

    public static byte[] toByteArray(String hexString) {
        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    private static boolean isValidKeyIndex(final Integer value) {
        return value == null || value != (value & 0xFFF);
    }

    private static boolean isValidUnicastAddress(final Integer value) {
        return value != null && value == (value & 0x7FFF);
    }

    /**
     * Checks if the unicast address is valid
     *
     * @param address address to be validated
     * @return true if is valid and false otherwise
     */
    public static boolean isValidUnicastAddress(final byte[] address) {
        if (address == null || address.length != 2)
            return false;
        final int addressVal = ((address[0] & 0xFF) << 8) | address[1] & 0xFF;

        return addressVal > 0x0000 && addressVal <= 0x7FFF;
    }

    /**
     * Checks if the address is a valid unassigned address
     *
     * @param address address to be validated
     * @return true if is valid and false otherwise
     */
    public static boolean isValidUnassignedAddress(final byte[] address) {
        if (address == null|| address.length != 2)
            return false;
        final int addressVal = ((address[0] & 0xFF) << 8) | address[1] & 0xFF;

        return addressVal == 0x0000;
    }

    /**
     * Validates a given group address
     *
     * @param address group address
     * @return true if is valid and false otherwise
     */
    public static boolean isValidGroupAddress(@NonNull final byte[] address) {
        if (address != null && address.length == 2) {
            return (address[0] >= 0xC0 && address[0] <= 0xFF) && address[0] != 0xFF || !(address[1] >= 0x00 && address[1] <= 0xFB);
        }
        return false;
    }

    /**
     * Validates a given group address
     *
     * @param address group address
     * @return true if is valid and false otherwise
     */
    public static boolean isValidGroupAddress(final int address) {
        return (address >= 0xC000 && address <= 0xFEFF) || (address >= 0xFFFC && address <= 0xFFFF);
    }

    private static boolean isValidIvIndex(final Integer value) {
        return value != null && (value >= 0 && value <= Integer.MAX_VALUE);
    }

    public static byte parseUpdateFlags(final int keyRefreshFlag, final int ivUpdateFlag) {
        byte flags = 0;
        if (keyRefreshFlag == 1) {
            flags |= 0b01;
        } else {
            flags &= ~0b01;
        }

        if (ivUpdateFlag == 1) {
            flags |= 0b10;
        } else {
            flags &= ~0b01;
        }
        return flags;
    }

    public static int getBitValue(final int value, final int position) {
        return (value >> position) & 1;
    }

    public static byte[] addKeyIndexPadding(final Integer keyIndex) {
        return ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort((short) (keyIndex & 0x0FFF)).array();
    }

    public static int removeKeyIndexPadding(final byte[] keyIndex) {
        return keyIndex[0] & 0x0F | keyIndex[1];
    }

    /**
     * Validates the ttl input
     *
     * @param context  context
     * @param ttlInput ttl input
     * @return true if the global ttl is a valid value
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateTtlInput(final Context context, final Integer ttlInput) throws IllegalArgumentException {

        if (ttlInput == null) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_global_ttl));
        } else if (!isValidTtl(ttlInput)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_global_ttl));
        }

        return true;
    }

    /**
     * Validates the network key input
     *
     * @param context context
     * @param input   Network Key input
     * @return true if the Network Key is a valid value
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateNetworkKeyInput(final Context context, final String input) throws IllegalArgumentException {

        if (TextUtils.isEmpty(input)) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_network_key));
        } else if (!input.matches(PATTERN_NETWORK_KEY)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_network_key));
        }

        return true;
    }

    /**
     * Validates the Key Index input
     *
     * @param context context
     * @param input   Key Index input
     * @return true if the Key Index is a valid value
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateKeyIndexInput(final Context context, final String input) throws IllegalArgumentException {

        if (TextUtils.isEmpty(input)) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_key_index));
        }

        final Integer keyIndex;

        try {
            keyIndex = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_key_index));
        }
        if (isValidKeyIndex(keyIndex)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_key_index));
        }

        return true;
    }

    /**
     * Validates the Key Index input
     *
     * @param context  context
     * @param keyIndex Key Index input
     * @return true if the Key Index is a valid value
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateKeyIndexInput(final Context context, final Integer keyIndex) throws IllegalArgumentException {

        if (keyIndex == null) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_key_index));
        }

        if (isValidKeyIndex(keyIndex)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_key_index));
        }

        return true;
    }

    /**
     * Validates the IV Index input
     *
     * @param context context
     * @param input   IV Index input
     * @return true if the the value is valid
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateIvIndexInput(final Context context, final String input) throws IllegalArgumentException {

        if (TextUtils.isEmpty(input)) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_iv_index));
        }

        final Integer ivIndex;
        try {
            ivIndex = Integer.parseInt(input, 16);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_iv_index));
        }

        if (!isValidIvIndex(ivIndex)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_iv_index));
        }

        if (ivIndex < IV_ADDRESS_MIN && ivIndex > IV_ADDRESS_MAX) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_iv_index));
        }

        return true;
    }

    /**
     * Validates the IV Index input
     *
     * @param context context
     * @param ivIndex IV Index input
     * @return true if the the value is valid
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateIvIndexInput(final Context context, final Integer ivIndex) throws IllegalArgumentException {

        if (ivIndex == null) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_iv_index));
        }

        if (!isValidIvIndex(ivIndex)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_iv_index));
        }

        if (ivIndex < IV_ADDRESS_MIN && ivIndex > IV_ADDRESS_MAX) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_iv_index));
        }

        return true;
    }

    /**
     * Validates the Unicast Address input
     *
     * @param context context
     * @param input   Unicast Address input
     * @return true if the the value is valid
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateUnicastAddressInput(final Context context, final String input) throws IllegalArgumentException {

        if (TextUtils.isEmpty(input)) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_unicast_address));
        }

        final Integer unicastAddress;
        try {
            unicastAddress = Integer.parseInt(input, 16);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_unicast_address));
        }

        if (!isValidUnicastAddress(unicastAddress)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_unicast_address));
        }

        if (unicastAddress == UNICAST_ADDRESS_MIN) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_unicast_address));
        }

        return true;
    }

    /**
     * Validates the Unicast Address input
     *
     * @param context        context
     * @param unicastAddress Unicast Address input
     * @return true if the the value is valid
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateUnicastAddressInput(final Context context, final Integer unicastAddress) throws IllegalArgumentException {

        if (unicastAddress == null) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_unicast_address));
        }

        if (!isValidUnicastAddress(unicastAddress)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_unicast_address));
        }

        if (unicastAddress == UNICAST_ADDRESS_MIN) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_unicast_address));
        }

        return true;
    }

    /**
     * Validates the app key input
     *
     * @param context context
     * @param input   App key input
     * @return true if the Network Key is a valid value
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateAppKeyInput(final Context context, final String input) throws IllegalArgumentException {

        if (TextUtils.isEmpty(input)) {
            throw new IllegalArgumentException(context.getString(R.string.error_empty_app_key));
        } else if (!input.matches(PATTERN_NETWORK_KEY)) {
            throw new IllegalArgumentException(context.getString(R.string.error_invalid_app_key));
        }

        return true;
    }

    public static boolean isValidSequenceNumber(final Integer sequenceNumber) {

        boolean flag = sequenceNumber != null && sequenceNumber == (sequenceNumber & 0xFFFFFF);

        if (sequenceNumber == 0xFFFFFF) {
            flag = false;
        }
        return flag;
    }

    public static byte[] getSequenceNumberBytes(int sequenceNumber) {
        if (MeshParserUtils.isValidSequenceNumber(sequenceNumber)) {
            return new byte[]{(byte) ((sequenceNumber >> 16) & 0xFF), (byte) ((sequenceNumber >> 8) & 0xFF), (byte) (sequenceNumber & 0xFF)};
        }
        return null;
    }

    public static int getSequenceNumber(final byte[] sequenceNumber) {
        return (((sequenceNumber[0] & 0xFF) << 16) | ((sequenceNumber[1] & 0xFF) << 8) | (sequenceNumber[2] & 0xFF));
    }

    public static int getSequenceNumberFromPDU(final byte[] pdu) {
        return (((pdu[3] & 0xFF) << 16) | ((pdu[4] & 0xFF) << 8) | (pdu[5] & 0xFF)); // get sequence number array from pdu
    }

    public static int calculateSeqZero(final byte[] sequenceNumber) {
        return ((sequenceNumber[1] & 0x1F) << 8) | (sequenceNumber[2] & 0xFF); // 13 least significant bits
    }

    public static byte[] getSrcAddress(final byte[] pdu) {
        return ByteBuffer.allocate(2).put(pdu, 6, 2).array(); // get src address from pdu
    }

    public static byte[] getDstAddress(final byte[] pdu) {
        return ByteBuffer.allocate(2).put(pdu, 8, 2).array(); // get mDst address from pdu
    }

    private static int getSegmentedMessageLength(final SparseArray<byte[]> segmentedMessageMap) {
        int length = 0;
        for (int i = 0; i < segmentedMessageMap.size(); i++) {
            length += segmentedMessageMap.get(i).length;
        }
        return length;
    }

    public static byte[] concatenateSegmentedMessages(final SparseArray<byte[]> segmentedMessages) {
        final int length = getSegmentedMessageLength(segmentedMessages);
        final ByteBuffer completeBuffer = ByteBuffer.allocate(length);
        completeBuffer.order(ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < segmentedMessages.size(); i++) {
            completeBuffer.put(segmentedMessages.get(i));
        }
        return completeBuffer.array();
    }

    /**
     * Returns the opcode within the access payload
     *
     * @param accessPayload payload
     * @param opcodeCount   number of opcodes
     * @return array of opcodes
     */
    public static int getOpCode(final byte[] accessPayload, final int opcodeCount) {
        switch (opcodeCount) {
            case 1:
                return accessPayload[0];
            case 2:
                return MeshParserUtils.unsignedBytesToInt(accessPayload[1], accessPayload[0]);
            case 3:
                return ((byte) (accessPayload[0] & 0xFF) | (byte) ((accessPayload[1] << 8) & 0xFF) | (byte) ((accessPayload[2] << 16) & 0xFF));
        }
        return -1;
    }

    /**
     * Returns the length of the opcode.
     * If the MSB = 0 then the length is 1
     * If the MSB = 1 then the length is 2
     * If the MSB = 2 then the length is 3
     *
     * @param opCode operation code
     * @return length of opcodes
     */
    public static byte[] getOpCodes(final int opCode) {
        if ((opCode & 0xC00000) == 0xC00000) {
            return new byte[]{(byte) ((opCode >> 16) & 0xFF), (byte) ((opCode >> 8) & 0xFF), (byte) (opCode & 0xFF)};
        } else if ((opCode & 0xFF8000) == 0x8000) {
            return new byte[]{(byte) ((opCode >> 8) & 0xFF), (byte) (opCode & 0xFF)};
        } else {
            //return new byte[]{ (byte) ((opCode >> 8) & 0xFF), (byte) (opCode & 0xFF)};
            return new byte[]{(byte) opCode};
        }
    }

    /**
     * Returns the vendor opcode packed with company identifier
     * If the MSB = 0 then the length is 1
     * If the MSB = 1 then the length is 2
     * If the MSB = 2 then the length is 3
     *
     * @param opCode operation code
     * @return length of opcodes
     */
    public static byte[] createVendorOpCode(final int opCode, final int companyIdentifier) {
        if (companyIdentifier != 0xFFFF) {
            //TODO nRF Mesh SDK implementation contains a bug related to endianness of the company identifier
            //In order to get this working with the sdk you may have to switch the company identifier bytes here
            return new byte[]{(byte) (0xC0 | (opCode & 0x3F)), (byte) (companyIdentifier & 0xFF), (byte) ((companyIdentifier >> 8) & 0xFF)};
        }
        return null;
    }

    /**
     * Checks if the opcode is valid
     *
     * @param opCode opCode of mesh message
     * @return if the opcode is valid
     */
    public static boolean isValidOpcode(final int opCode) throws IllegalArgumentException {
        if (opCode != (opCode & 0xFFFFFF))
            throw new IllegalArgumentException("Invalid opcode, opcode must be 1-3 octets");

        return true;
    }

    /**
     * Checks if the parameters are within the valid range
     *
     * @param parameters opCode of mesh message
     * @return if the opcode is valid
     */
    public static boolean isValidParameters(final byte[] parameters) throws IllegalArgumentException {
        if (parameters != null && parameters.length > 379)
            throw new IllegalArgumentException("Invalid parameters, parameters must be 0-379 octets");

        return true;
    }

    /**
     * Checks if the ttl value is within the allowed range where the range is 0x00 - 0x7F
     *
     * @param ttl ttl
     * @return true if valid and false otherwise
     */
    public static boolean isValidTtl(final int ttl) {
        return (ttl >= MIN_TTL) && (ttl <= MAX_TTL);
    }

    /**
     * Checks if the default publish ttl is used for publication
     *
     * @param publishTtl publish ttl
     * @return true if valid and false otherwise
     */
    public static boolean isDefaultPublishTtl(final int publishTtl) {
        return publishTtl == USE_DEFAULT_TTL;
    }

    /**
     * Checks if the retransmit count is within the allowed range
     *
     * @param retrantmistCount publish ttl
     * @return true if valid and false otherwise
     */
    public static boolean validateRetransmitCount(final int retrantmistCount) {
        return retrantmistCount == (retrantmistCount & 0b111);
    }

    /**
     * Checks if the publish retransmit interval steps is within the allowed range
     *
     * @param intervalSteps publish ttl
     * @return true if valid and false otherwise
     */
    public static boolean validatePublishRetransmitIntervalSteps(final int intervalSteps) {
        return intervalSteps == (intervalSteps & 0b11111);
    }

    /**
     * Returns the remaining time as a string
     *
     * @param remainingTime remaining time that for the transition to finish
     * @return remaining time as string.
     */
    public static String getRemainingTime(final int remainingTime) {
        final int stepResolution = remainingTime >> 6;
        final int numberOfSteps = remainingTime & 0x3F;
        switch (stepResolution) {
            case RESOLUTION_100_MS:
                return (numberOfSteps * 100) + " milliseconds";
            case RESOLUTION_1_S:
                return numberOfSteps + " seconds";
            case RESOLUTION_10_S:
                return (numberOfSteps * 10) + " seconds";
            case RESOLUTION_10_M:
                return (numberOfSteps * 10) + " minutes";
            default:
                return "Unknown";
        }
    }

    /**
     * Returns the remaining time as a string
     *
     * @return remaining time as string.
     */
    public static String getRemainingTransitionTime(final int stepResolution, final int numberOfSteps) {
        switch (stepResolution) {
            case RESOLUTION_100_MS:
                return (numberOfSteps * 100) + " ms";
            case RESOLUTION_1_S:
                return numberOfSteps + " s";
            case RESOLUTION_10_S:
                return (numberOfSteps * 10) + " s";
            case RESOLUTION_10_M:
                return (numberOfSteps * 10) + " min.";
            default:
                return "Unknown";
        }
    }

    /**
     * Returns the remaining time in milliseconds
     *
     * @param resolution time resolution
     * @param steps      number of steps
     * @return time in milliseconds
     */
    public static int getRemainingTime(final int resolution, final int steps) {
        switch (resolution) {
            case RESOLUTION_100_MS:
                return (steps * 100);
            case RESOLUTION_1_S:
                return steps * 1000;
            case RESOLUTION_10_S:
                return (steps * 10) * 1000;
            case RESOLUTION_10_M:
                return (steps * 10) * 1000 * 60;
        }
        return 0;
    }

    public static int getValue(final byte[] bytes) {
        if (bytes == null || bytes.length != 2)
            return 0;
        return unsignedToSigned(unsignedBytesToInt(bytes[0], bytes[1]), 16);
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    public static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    public static int unsignedBytesToInt(byte b0, byte b1) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    public static int bytesToInt(byte[] b) {
        return b.length == 4 ? ByteBuffer.wrap(b).getInt() : ByteBuffer.wrap(b).getShort();
    }

    public static byte[] intToBytes(int i) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(i);
        return b.array();
    }

    /**
     * Convert an unsigned integer value to a two's-complement encoded signed value.
     */

    private static int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size - 1)) != 0) {
            unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        }
        return unsigned;
    }

    /**
     * Returns the international atomic time (TAI) in seconds
     * <p>
     * TAI seconds and is the number of seconds after 00:00:00 TAI on 2000-01-01
     * </p>
     *
     * @param currentTime current time in milliseconds
     */
    public static long getInternationalAtomicTime(final long currentTime) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(TAI_YEAR, TAI_MONTH, TAI_DATE, 0, 0, 0);
        final long millisSinceEpoch = calendar.getTimeInMillis();
        return (currentTime - millisSinceEpoch) / 1000;
    }
}