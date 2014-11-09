package eu.imagine.framework;

/**
 * Helper class for generating possible markers and for decoding detected
 * markers.
 */
public class MarkerPatternHelper {

    private static Messenger log = Messenger.getInstance();
    private static String TAG = "MarkerPatternHelper";
    public static boolean hammingDeforce = false;

    /**
     * Function to create the marker for a given ID.
     *
     * @param ID The ID to encode into the marker.
     * @return The complete pattern that resembles the marker,
     *         including coded ID, coded direction, and borders.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean[][] createMarker(int ID) {
        // Check that value is within allowable range:
        if (ID > 255) {
            log.debug(TAG, "ID too large, exceeds maximum!");
            return null;
        }
        // Initiate empty pattern – border and 3 inner id field corners are
        // left at black.
        boolean[][] pattern = new boolean[6][6];
        // Set id field corner positions to white to indicate orientation
        pattern[1][1] = true;
        pattern[4][1] = true;
        pattern[4][4] = true;
        // Create bool representation of ID:
        boolean[] encoded = bool(ID);
        // Hamming encode it:
        boolean[] code = hammingCreation(encoded);
        // Write to pattern:
        pattern[1][2] = code[0];
        pattern[1][3] = code[1];
        pattern[2][1] = code[2];
        pattern[2][2] = code[3];
        pattern[2][3] = code[4];
        pattern[2][4] = code[5];
        pattern[3][1] = code[6];
        pattern[3][2] = code[7];
        pattern[3][3] = code[8];
        pattern[3][4] = code[9];
        pattern[4][2] = code[10];
        pattern[4][3] = code[11];

        return pattern;
    }

    /**
     * Given a pattern that represents a correctly rotated marker ID field
     * (meanind the grid WITHOUT the border); returns the hamming code
     * corrected ID of it.
     *
     * @param pattern The pattern to check. Size must be 4x4!
     * @return Returns the corrected ID.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected static int getID(final boolean[][] pattern) {
        boolean[] code = new boolean[12];
        // Extract code:
        code[0] = pattern[0][1];
        code[1] = pattern[0][2];
        code[2] = pattern[1][0];
        code[3] = pattern[1][1];
        code[4] = pattern[1][2];
        code[5] = pattern[1][3];
        code[6] = pattern[2][0];
        code[7] = pattern[2][1];
        code[8] = pattern[2][2];
        code[9] = pattern[2][3];
        code[10] = pattern[3][1];
        code[11] = pattern[3][2];

        // Return (hopefully) corrected code:
        return hammingCorrection(code);
    }


    /**
     * Function that takes a 8-long boolean array and converts it to its
     * 12-long encoded equivalent.
     *
     * @param encoded The boolean array to encode. Must be 8 long!
     * @return The encoded array.
     */
    @SuppressWarnings("UnusedDeclaration")
    private static boolean[] hammingCreation(boolean[] encoded) {
        boolean[] code = new boolean[12];
        // place word in code
        code[2] = encoded[0];
        code[4] = encoded[1];
        code[5] = encoded[2];
        code[6] = encoded[3];
        code[8] = encoded[4];
        code[9] = encoded[5];
        code[10] = encoded[6];
        code[11] = encoded[7];
        // set parity
        code[0] = code[2] ^ code[4] ^ code[6] ^ code[8] ^ code[10];
        code[1] = code[2] ^ code[5] ^ code[6] ^ code[9] ^ code[10];
        code[3] = code[4] ^ code[5] ^ code[6] ^ code[11];
        code[7] = code[8] ^ code[9] ^ code[10] ^ code[11];

        return code;
    }

    /**
     * Method for testing and correcting a hamming encoded boolean
     * representative of a number.
     *
     * @param code The 12 long bool array containing the full code.
     * @return The 8 long bool array containing the corrected decoded booleans.
     */
    @SuppressWarnings("UnusedDeclaration")
    private static int hammingCorrection(boolean[] code) {
        boolean[] decoded = new boolean[8];
        // Check parity:
        int errorZero = -1;
        int errorOne = -1;
        int errorThree = -1;
        int errorSeven = -1;
        if (code[0] != code[2] ^ code[4] ^ code[6] ^ code[8] ^ code[10]) {
            errorZero = 0;
        }
        if (code[1] != code[2] ^ code[5] ^ code[6] ^ code[9] ^ code[10]) {
            errorOne = 1;
        }
        if (code[3] != code[4] ^ code[5] ^ code[6] ^ code[11]) {
            errorThree = 3;
        }
        if (code[7] != code[8] ^ code[9] ^ code[10] ^ code[11]) {
            errorSeven = 7;
        }
        // decode
        decoded[0] = code[2];
        decoded[1] = code[4];
        decoded[2] = code[5];
        decoded[3] = code[6];
        decoded[4] = code[8];
        decoded[5] = code[9];
        decoded[6] = code[10];
        decoded[7] = code[11];
        // If no error:
        if (errorZero < 0 && errorOne < 0 && errorThree < 0 && errorSeven < 0)
            return deBool(decoded);
        // ---- ERROR ----
        log.debug(TAG, "Error in ID, trying correction...");
        // Check for parity bit error (--> only one error)
        if (errorZero == 0 && errorOne < 0 && errorThree < 0 && errorSeven < 0)
            return deBool(decoded);
        if (errorZero < 0 && errorOne == 1 && errorThree < 0 && errorSeven < 0)
            return deBool(decoded);
        if (errorZero < 0 && errorOne < 0 && errorThree == 3 && errorSeven < 0)
            return deBool(decoded);
        if (errorZero < 0 && errorOne < 0 && errorThree < 0 && errorSeven == 7)
            return deBool(decoded);
        // Data error
        log.debug(TAG, "WARNING: correction uncertain!");
        // Uncertain because when 2 or more bits are wrong we can't detect
        // that, thus giving a wrong id back as of here. Or the correction
        // was successful... :P
        int index = (errorZero < 0 ? 0 : 1) + (errorOne < 0 ? 0 : 2) + (errorThree <
                0 ? 0 : 4) + (errorSeven < 0 ? 0 : 8);
        index--;
        // This can happen when more than one error happened:
        if (index >= code.length) {
            log.debug(TAG, "WARNING: Hamming failed to correct!");
            if (!hammingDeforce)
                return -1;
        } else
            code[index] = !code[index];
        return deBool(decoded);
    }

    /**
     * Helpful method for converting boolean arrays back to integers.
     *
     * @param bool The array to convert. Length must be 8.
     * @return The number represented by it.
     */
    @SuppressWarnings("UnusedDeclaration")
    private static int deBool(boolean[] bool) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            int fast = (int) Math.pow(2, (7 - i));
            result += bool[i] ? fast : 0;
        }
        return result;
    }

    /**
     * Helpful method for converting integers to boolean arrays.
     *
     * @param number The number to convert.
     * @return The boolean array representing the number.
     */
    @SuppressWarnings("UnusedDeclaration")
    private static boolean[] bool(int number) {
        boolean[] result = new boolean[8];
        for (int i = 0; i < 8; i++) {
            int fast = (int) Math.pow(2, (7 - i));
            if (number / fast > 0) {
                result[i] = true;
                number -= fast;
            }
        }
        return result;
    }
}
