package nordpol;


import java.util.Arrays;
import java.util.Locale;
import java.io.IOException;
import nordpol.IsoCard;

/**
 * Utility functions APDU generation / parsing
 */
public class Apdu {

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    final private static String FIDESMO_AID_PREFIX = "A00000061700";
    final private static String SELECT_HEADER = "00A40400";
    public final static String OK_APDU = "9000";

    /**
     * Encodes a byte array into a hexadecimal string having two characters per byte
     * @param bytes the input byte[]
     * @return the resulting hex string
     */
    public static String encodeHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for ( int i = 0; i < bytes.length; i++ ) {
            sb.append(String.format("%02X", bytes[i] & 0xFF));
        }

        return sb.toString();
    }
    /**
     * Encodes a byte into a hexadecimal string
     * @param b the input byte
     * @return the resulting hex string
     */
    public static String encodeHex(byte b) {
        return encodeHex(new byte[] { b });
    }
    /**
     * Decodes a hexadecimal string into a byte array
     * @param hexString a hex string, two characters per byte
     * @return the decoded byte array
     */
    public static byte[] decodeHex(String hexString) {
        if ((hexString.length() & 0x01) != 0) {
            throw new IllegalArgumentException("Odd number of characters.");
        }
        char[] hexChars = hexString.toUpperCase(Locale.ROOT).toCharArray();
        byte[] result = new byte[hexChars.length / 2];
        for (int i = 0; i < hexChars.length; i += 2) {
            result[i / 2] = (byte) (Arrays.binarySearch(hexArray, hexChars[i]) * 16 +
                    Arrays.binarySearch(hexArray, hexChars[i + 1]));
        }
        return result;
    }

    /**
     * Builds the SELECT command APDU from a cardlet's app ID:
     * - builds the cardlet's AID appending the Fidesmo prefix and a suffix
     * - builds the command concatenating the header, AID lenght and AID
     * @param appId the application ID as a hex string, assigned by Fidesmo
     * @param suffix additional suffix as a hex string
     * @return SELECT command APDU
     */
    public static byte[] select(String appId, String suffix) {
        String cardletAid = FIDESMO_AID_PREFIX + appId + suffix;
        return select(cardletAid);
    }

    /**
     * Builds the SELECT command APDU from a cardlet's AID:
     * - builds a SELECT command concatenating the header, AID lenght and AID
     * @param aid the AID of the applet
     * @return SELECT command APDU
     */
    public static byte[] select(String aid) {
        byte aidLength = (byte)(aid.length()/2);
        String selectApdu = SELECT_HEADER + encodeHex(aidLength) + aid;
        return decodeHex(selectApdu);
    }

    /**
     * Extracts the status bytes of a response APDU - these are always the last two bytes
     * @param response A response APDU
     * @return the two status bytes
     */
    public static byte[] statusBytes(byte[] response) {
        return new byte[] {response[response.length -2], response[response.length-1]};
    }

    /**
     * Extracts the data payload of a response APDU: everything except the last two status bytes
     * @param response A response APDU
     * @return the data payload of the response
     */
    public static byte[] responseData(byte[] response) {
        return Arrays.copyOfRange(response, 0, response.length - 2);
    }

    /**
     * Matches the first APDUs last characters (of the length of the statusCode) against the statusCode
     * @param receivedApdu The response APDU as byte[] to be compared
     * @param statusCode The statusCode as byte[] to match against
     * @return true if the APDUs match, false if not
     */
    public static boolean hasStatus(byte[] receivedApdu, byte[] statusCode) throws IOException {
        byte[] receivedStatus = statusBytes(receivedApdu);
        return receivedStatus[0] == statusCode[0] && receivedStatus[1] == statusCode[1];
    }

    /**
     * Matches the first APDUs last characters (of the length of the statusCode) against the statusCode
     * @param receivedApdu The response APDU as byte[] to be compared
     * @param statusCode The statusCode as String to match against
     * @return true if the APDUs match, false if not
     */
    public static boolean hasStatus(byte[] receivedApdu, String statusCode) throws IOException {
        return hasStatus(receivedApdu, decodeHex(statusCode));
    }

    /**
     * Matches the first APDUs last characters (of the length of the statusCode) against the statusCode
     * @param receivedApdu The response APDU as String to be compared
     * @param statusCode The statusCode as String to match against
     * @return true if the APDUs match, false if not
     */
    public static boolean hasStatus(String receivedApdu, String statusCode) throws IOException {
        return hasStatus(decodeHex(receivedApdu), decodeHex(statusCode));
    }

    /**
     * Transceives the byte[] command to the Isocard. Automatically queries for
     * more response data if the status code indicates that more data is available.
     * @param command The byte[] APDU command to be sent to the card
     * @param isoCard The card to send the command to
     * @param getResponseApdu The APDU command to be sent to get more data
     * @return byte[] response from the card
     */
    public static byte[] transceiveAndGetResponse(byte[] command, IsoCard isoCard, String getResponseApdu) throws IOException {
        //Transfer first message
        byte[] resp = isoCard.transceive(command);
        byte[] buf = new byte[2048];
        int offset = 0;

        //Transfer the remains
        while (resp[resp.length - 2] == 0x61) {
            System.arraycopy(resp, 0, buf, offset, resp.length - 2);
            offset += resp.length - 2;
            resp = isoCard.transceive(decodeHex(getResponseApdu));
        }

        System.arraycopy(resp, 0, buf, offset, resp.length);
        byte[] properlySized = new byte[offset + resp.length];
        System.arraycopy(buf, 0, properlySized, 0, properlySized.length);

        return properlySized;
    }

    /**
     * Transceives the byte[] command to the IsoCard. Matches the response against the supplied expectedApduStatus
     * @param command The byte[] APDU command to be sent to the card
     * @param isoCard The card to send the command to
     * @param expectedApduStatus The expected apdu status that the response should contain.
     * @return byte[] response from the card if match with expectedApduStatus else it throws exception
     */
    public static byte[] transceiveAndRequireStatus(byte[] command, IsoCard isoCard, String expectedApduStatus) throws IOException {
        byte[] response = isoCard.transceive(command);
        if(hasStatus(response, expectedApduStatus)){
          return response;
        } else {
          throw new IOException("Require APDU status: " + expectedApduStatus + ", got " + response);
        }
    }

    /**
     * Transceives the byte[] command to the IsoCard. Matches the response against the supplied expectedApduStatus
     * @param command The byte[] APDU command to be sent to the card
     * @param isoCard The card to send the command to
     * @return byte[] response from the card if match with OK_APDU else it throws exception
     */
    public static byte[] transceiveAndRequireOk(byte[] command, IsoCard isoCard) throws IOException {
        return transceiveAndRequireStatus(command, isoCard, OK_APDU);
    }

    private static String shortenString(String string, int expectedSize){
         return string.substring(string.length() - expectedSize);
    }
}
