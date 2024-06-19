package supersocksr.ppp.android.openppp2;

import android.text.Editable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextX {
    private static final String _keys = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // Specifies the hash algorithm to get the summary string of string s.
    public static String digest(String hashAlgorithm, String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }

        try {
            MessageDigest m = MessageDigest.getInstance(hashAlgorithm);
            m.update(s.getBytes(StandardCharsets.UTF_8));
            byte[] b = m.digest();
            if (b.length > 0) {
                StringBuilder r = new StringBuilder();
                for (byte value : b) {
                    r.append(Integer.toHexString((0x000000FF & value) | 0xFFFFFF00).substring(6));
                }
                return r.toString();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    // The summary of string s is obtained by sh1 algorithm.
    public static String sha1_digest(String s) {
        return digest("SHA", s);
    }

    // The summary of string s is obtained by md5 algorithm.
    public static String md5_digest(String s) {
        return digest("MD5", s);
    }

    // Converts a string to a boolean value.
    public static boolean string_to_boolean(String s) {
        if (TextUtils.isEmpty(s)) {
            return false;
        }

        if (s.equals("0") || s.equals("\"\"") || s.equals("''") || s.equals("null") || s.equals("undefined")) {
            return false;
        }

        Pattern regex = null;
        Matcher matcher = null;
        try {
            regex = Pattern.compile("^(false)$");
            matcher = regex.matcher(s);
            return !matcher.matches();
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Delete all whitespace characters from the trim string.
    public static String string_trim(String s) {
        if (s == null) {
            return null;
        }

        if (s.isEmpty()) {
            return "";
        }

        int len = s.length();
        int st = 0;
        while ((st < len) && (s.charAt(st) <= ' ' || s.charAt(st) == 65279)) {
            st++;
        }

        while ((st < len) && (s.charAt(len - 1) <= ' ' || s.charAt(st) == 65279)) {
            len--;
        }
        return ((st > 0) || (len < s.length())) ? s.substring(st, len) : s;
    }

    // String url encoding.
    public static String url_encode(String s) {
        return url_encode(s, null);
    }

    // String url encoding.
    public static String url_encode(String s, String charset) {
        if (s == null) {
            return "";
        }

        if (TextUtils.isEmpty(charset)) {
            charset = "UTF-8";
        }

        try {
            s = URLEncoder.encode(s, charset);
        } catch (Throwable ignored) {
            return "";
        }
        return s;
    }

    // String url decoding.
    public static String url_decode(String s) {
        return url_decode(s, null);
    }

    // String url decoding.
    public static String url_decode(String s, String charset) {
        if (s == null) {
            return "";
        }

        if (TextUtils.isEmpty(charset)) {
            charset = "UTF-8";
        }

        try {
            s = URLDecoder.decode(s, charset);
        } catch (Throwable ignored) {
            return "";
        }
        return s;
    }

    // String percent encoding.
    @NonNull
    public static String percent_encode(String s) {
        return percent_encode(s, null);
    }

    // https://en.wikipedia.org/wiki/Percent-encoding
    // String percent encoding.
    @NonNull
    public static String percent_encode(String s, String charset) {
        if (s == null) {
            return "";
        }

        if (TextUtils.isEmpty(charset)) {
            charset = "UTF-8";
        }

        byte[] bytes;
        try {
            bytes = s.getBytes(charset);
        } catch (Throwable ignored) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append("%").append(string_to_string(b, 16, 2, '0'));
        }
        return result.toString();
    }

    //  Convert floating-point numbers to strings.
    @NonNull
    public static String string_to_string(float v, int radix, int pading, char paddingChar) {
        return string_to_string(Float.floatToIntBits(v), radix, pading, paddingChar);
    }

    // Convert floating-point numbers to strings.
    @NonNull
    public static String string_to_string(double v, int radix, int pading, char paddingChar) {
        return string_to_string(Double.doubleToLongBits(v), radix, pading, paddingChar);
    }

    // Convert integers to strings.
    @NonNull
    public static String string_to_string(long v, int radix, int pading, char paddingChar) {
        if (radix < 2) {
            radix = 10;
        } else if (radix > _keys.length()) {
            radix = _keys.length();
        }

        int p = radix;
        StringBuilder r;
        if (v == 0) {
            r = new StringBuilder("0");
        } else {
            r = new StringBuilder();
            while (v > 0) {
                long n = v % p;
                r.insert(0, _keys.charAt((int) n));
                v /= p;
            }
        }

        if (pading > r.length()) {
            r = new StringBuilder(string_left_padding(r.toString(), paddingChar, pading));
        }
        return r.toString();
    }

    // Converts the string to float.
    public static float string_to_float(String v, int radix) {
        return Float.intBitsToFloat(string_to_int(v, radix));
    }

    // Converts the string to double.
    public static double string_to_double(String v, int radix) {
        return Double.longBitsToDouble(string_to_long(v, radix));
    }

    // Converts the string to byte.
    public static byte string_to_byte(String v, int radix) {
        return (byte) string_to_long(v, radix);
    }

    // Converts the string to short.
    public static short string_to_short(String v, int radix) {
        return (short) string_to_long(v, radix);
    }

    // Converts the string to int.
    public static int string_to_int(String v, int radix) {
        return (int) string_to_long(v, radix);
    }

    // Converts the string to long.
    public static long string_to_long(String v, int radix) {
        if (X.is_empty(v)) {
            return 0;
        }

        if (radix < 2) {
            radix = 10;
        } else if (radix > _keys.length()) {
            radix = _keys.length();
        }

        long r = 0;
        long p = radix;
        long b = 0;
        for (int l = v.length() - 1, i = l; i > -1; i--) {
            char ch = v.charAt(i);
            long k;
            if (ch >= '0' && ch <= '9') {
                k = ch - '0';
            } else if (ch >= 'A' && ch <= 'Z') {
                k = 10 + (long) (ch - 'A');
            } else if (ch >= 'a' && ch <= 'z') {
                k = 10 + (long) (ch - 'a');
            } else {
                return 0;
            }

            if (k >= p) {
                return 0;
            }

            if (i == l) {
                r += k;
                b = p;
            } else {
                r += k * b;
                b = b * p;
            }
        }
        return r;
    }

    /**
     * get left padding String with specification char for existed suffix
     * for example,if need left padding zero for some String,
     * like left padding 0 to 12,and need total length 10,
     * then the result will be 0000000012.
     *
     * @param suffix      the suffix for return String
     * @param fill        the fill char for String
     * @param totalLength the total length need for composite String
     * @return
     */
    @NotNull
    public static String string_left_padding(String suffix, char fill, int totalLength) {
        return string_full("", suffix, fill, totalLength);
    }

    /**
     * get right padding String with specification char for existed prefix
     * for example,if need left padding zero for some String,
     * like right padding 0 to A,and need total length 10,
     * then the result will be A000000000.
     *
     * @param prefix      the prefix for return String
     * @param fill        the fill char for String
     * @param totalLength the total length need for composite String
     * @return
     */
    @NotNull
    public static String string_right_padding(String prefix, char fill, int totalLength) {
        return string_full(prefix, "", fill, totalLength);
    }

    /**
     * get char fill composite String for existed prefix or suffix
     * for example,if need left padding zero for some String,
     * like padding 0 to 12,and need total length 10,
     * then the result will be 0000000012. what's more,
     * if add a prefix for this composite String,like A,
     * then then return String will be A000000012.
     *
     * @param prefix      the prefix for return String
     * @param suffix      the suffix for return String
     * @param fill        the fill char for String
     * @param totalLength the total length need for composite String
     * @return
     */
    @NotNull
    public static String string_full(@NotNull String prefix, @NotNull String suffix, char fill, int totalLength) {
        int c = 0;
        int needLength = totalLength - (prefix.length() + suffix.length());

        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        while (c < needLength) {
            sb.append(fill);
            c++;
        }
        sb.append(suffix);
        return sb.toString();
    }

    // Converts the editable object to a string.
    public static String to_string(Editable editable) {
        if (editable == null) {
            return null;
        }

        try {
            return editable.toString();
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Safely calls the trim function of the string.
    public static String trim(String s) {
        if (s == null) {
            return null;
        }

        try {
            return s.trim();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
