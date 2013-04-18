package com.jetdrone.vertx.yoke.middleware;

class Utils {

    private static final String base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    static byte[] zeroPad(int length, byte[] bytes) {
        byte[] padded = new byte[length]; // initialized to zero by JVM
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        return padded;
    }

    public static String base64(byte[] stringArray) {

        StringBuilder encoded = new StringBuilder();

        // determine how many padding bytes to add to the output
        int paddingCount = (3 - (stringArray.length % 3)) % 3;
        // add any necessary padding to the input
        stringArray = zeroPad(stringArray.length + paddingCount, stringArray);
        // process 3 bytes at a time, churning out 4 output bytes
        // worry about CRLF insertions later
        for (int i = 0; i < stringArray.length; i += 3) {
            int j = ((stringArray[i] & 0xff) << 16) +
                    ((stringArray[i + 1] & 0xff) << 8) +
                    (stringArray[i + 2] & 0xff);

            encoded.append(base64code.charAt((j >> 18) & 0x3f));
            encoded.append(base64code.charAt((j >> 12) & 0x3f));
            encoded.append(base64code.charAt((j >> 6) & 0x3f));
            encoded.append(base64code.charAt(j & 0x3f));
        }

        encoded.setLength(encoded.length() - paddingCount);
        return encoded.toString();
    }
}
