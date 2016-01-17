package com.github.lawena.util;

/**
 * Extracts from com.samskivert.util.StringUtil
 */
public class StringUtils {

    protected static final String XLATE = "0123456789abcdef";

    /**
     * Parses an array of strings from a single string. The array should be represented as a bare
     * list of strings separated by commas, for example:
     * <p>
     * <pre>mary, had, a, little, lamb, and, an, escaped, comma,,</pre>
     *
     * If a comma is desired in one of the strings, it should be escaped by putting two commas in a
     * row. Any inability to parse the string array will result in the function returning null.
     */
    public static String[] parseStringArray(String source) {
        return parseStringArray(source, false);
    }

    /**
     * Like {@link #parseStringArray(String)} but can be instructed to invoke {@link String#intern}
     * on the strings being parsed into the array.
     */
    public static String[] parseStringArray(String source, boolean intern) {
        int tcount = 0, tpos = -1, tstart = 0;

        // empty strings result in zero length arrays
        if (source.length() == 0) {
            return new String[0];
        }

        // sort out escaped commas
        source = source.replace(",,", "%COMMA%");

        // count up the number of tokens
        while ((tpos = source.indexOf(",", tpos + 1)) != -1) {
            tcount++;
        }

        String[] tokens = new String[tcount + 1];
        tpos = -1;
        tcount = 0;

        // do the split
        while ((tpos = source.indexOf(",", tpos + 1)) != -1) {
            tokens[tcount] = source.substring(tstart, tpos);
            tokens[tcount] = tokens[tcount].trim().replace("%COMMA%", ",");
            if (intern) {
                tokens[tcount] = tokens[tcount].intern();
            }
            tstart = tpos + 1;
            tcount++;
        }

        // grab the last token
        tokens[tcount] = source.substring(tstart);
        tokens[tcount] = tokens[tcount].trim().replace("%COMMA%", ",");

        return tokens;
    }

    /**
     * Generates a string from the supplied bytes that is the HEX encoded representation of those
     * bytes.  Returns the empty string for a <code>null</code> or empty byte array.
     *
     * @param bytes the bytes for which we want a string representation.
     * @param count the number of bytes to stop at (which will be coerced into being <= the length
     *              of the array).
     */
    public static String hexlate(byte[] bytes, int count) {
        if (bytes == null) {
            return "";
        }

        count = Math.min(count, bytes.length);
        char[] chars = new char[count * 2];

        for (int i = 0; i < count; i++) {
            int val = bytes[i];
            if (val < 0) {
                val += 256;
            }
            chars[2 * i] = XLATE.charAt(val / 16);
            chars[2 * i + 1] = XLATE.charAt(val % 16);
        }

        return new String(chars);
    }

    /**
     * Generates a string from the supplied bytes that is the HEX encoded representation of those
     * bytes.
     */
    public static String hexlate(byte[] bytes) {
        return (bytes == null) ? "" : hexlate(bytes, bytes.length);
    }

    private StringUtils() {
    }
}
