package supersocksr.ppp.android.openppp2;

import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteOrder;
import java.util.Collection;

import supersocksr.ppp.android.openppp2.i.Action;
import supersocksr.ppp.android.openppp2.i.IFunc;
import supersocksr.ppp.android.openppp2.i.Tuple;

public final class X {
    public static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN);

    // Gets the class type of the object.
    @Nullable
    public static <T> Class<T> class_of(Object o) {
        if (o == null) {
            return null;
        }

        try {
            return (Class<T>) o.getClass();
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Gets the class type of the object (generic template type location).
    @Nullable
    public static <T> Class<T> class_of(Object o, int index) {
        if (o == null || index < -1) {
            return null;
        }

        if (index == -1) {
            return class_of(o);
        }

        ParameterizedType t = (ParameterizedType) o.getClass().getGenericSuperclass();
        if (t == null) {
            return null;
        }

        try {
            Type[] s = t.getActualTypeArguments();
            if (index >= s.length) {
                return null;
            } else {
                return (Class<T>) s[index];
            }
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Safe call func has return function.
    @NonNull
    @Contract("null -> new")
    public static <TResult> Tuple<Boolean, TResult> call(IFunc<TResult> func, TResult def) {
        if (func == null) {
            return new Tuple<>(false, def);
        }

        try {
            TResult r = func.handle();
            return new Tuple<>(true, r);
        } catch (Throwable ignored) {
            return new Tuple<>(false, def);
        }
    }

    // Safe call action no return function.
    public static boolean call_void(Action action) {
        if (action == null) {
            return false;
        }

        try {
            action.handle();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Convert time of the specified time.
    public static long time_convert_time(long start_time, long refer_time) {
        long now = System.currentTimeMillis() / 1000;
        start_time = Math.max(0, start_time);
        refer_time = Math.max(0, refer_time);
        return Math.max(0, start_time + Math.max(1, (now - refer_time)));
    }

    // Close the file descriptor handle.
    public static boolean close(ParcelFileDescriptor fd) {
        if (fd == null) {
            return false;
        }

        try {
            fd.close();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }


    // To determine whether a string is null or empty.
    public static boolean is_empty(String s) {
        return TextUtils.isEmpty(s);
    }

    // To determine whether a collection is null or empty.
    public static <T> boolean is_empty(Collection<T> s) {
        return s == null || s.size() < 1;
    }

    // To determine whether a array is null or empty.
    public static <T> boolean is_empty(T[] s) {
        return s == null || s.length < 1;
    }

    // Reverse the order of the array.
    public static <T> T[] reserve(T[] list) {
        if (is_empty(list)) {
            return list;
        }

        int i = 0;
        int j = list.length - 1;
        for (; i < list.length; i++, j--) {
            T temp = list[i];
            list[i] = list[j];
            list[j] = temp;
        }
        return list;
    }

    // Reverse the order of the array.
    public static byte[] reserve(byte[] list) {
        if (list == null || list.length < 1) {
            return list;
        }

        int i = 0;
        int j = list.length - 1;
        for (; i < list.length; i++, j--) {
            byte temp = list[i];
            list[i] = list[j];
            list[j] = temp;
        }
        return list;
    }

    // Byte array to short.
    public static short bytes_to_short(byte[] bytes) {
        if (bytes == null) {
            return 0;
        }

        short num = 0;
        if (bytes.length > 0) num |= ((int) bytes[0] & 0xff);
        if (bytes.length > 1) num |= ((int) bytes[1] & 0xff) << 8;

        if (X.IS_LITTLE_ENDIAN) {
            return num;
        } else {
            return Short.reverseBytes(num);
        }
    }

    // Byte array to int.
    public static int bytes_to_int(byte[] bytes) {
        if (bytes == null) {
            return 0;
        }

        int num = 0;
        if (bytes.length > 0) num |= ((int) bytes[0] & 0xff);
        if (bytes.length > 1) num |= ((int) bytes[1] & 0xff) << 8;
        if (bytes.length > 2) num |= ((int) bytes[2] & 0xff) << 16;
        if (bytes.length > 3) num |= ((int) bytes[3] & 0xff) << 24;

        if (X.IS_LITTLE_ENDIAN) {
            return num;
        } else {
            return Integer.reverseBytes(num);
        }
    }

    // Byte array to long.
    public static long bytes_to_long(byte[] bytes) {
        if (bytes == null) {
            return 0;
        }

        long num = 0;
        if (bytes.length > 0) num |= ((long) bytes[0] & 0xffL);
        if (bytes.length > 1) num |= ((long) bytes[1] & 0xffL) << 8;
        if (bytes.length > 2) num |= ((long) bytes[2] & 0xffL) << 16;
        if (bytes.length > 3) num |= ((long) bytes[3] & 0xffL) << 24;
        if (bytes.length > 4) num |= ((long) bytes[4] & 0xffL) << 32;
        if (bytes.length > 5) num |= ((long) bytes[5] & 0xffL) << 40;
        if (bytes.length > 6) num |= ((long) bytes[6] & 0xffL) << 48;
        if (bytes.length > 7) num |= ((long) bytes[7] & 0xffL) << 56;

        if (X.IS_LITTLE_ENDIAN) {
            return num;
        } else {
            return Long.reverseBytes(num);
        }
    }

    // Short to byte array.
    @NonNull
    @Contract(pure = true)
    public static byte[] short_to_bytes(short n) {
        if (!X.IS_LITTLE_ENDIAN) {
            n = Short.reverseBytes(n);
        }

        byte[] b = new byte[2];
        b[1] = (byte) ((n >> 8) & 0xFF);
        b[0] = (byte) (n & 0xFF);
        return b;
    }

    // Int to byte array.
    @NonNull
    @Contract(pure = true)
    public static byte[] int_to_bytes(int n) {
        if (!X.IS_LITTLE_ENDIAN) {
            n = Integer.reverseBytes(n);
        }

        byte[] b = new byte[4];
        b[3] = (byte) ((n >> 24) & 0xFF);
        b[2] = (byte) ((n >> 16) & 0xFF);
        b[1] = (byte) ((n >> 8) & 0xFF);
        b[0] = (byte) (n & 0xFF);
        return b;
    }

    // Long to byte array.
    @NonNull
    @Contract(pure = true)
    public static byte[] long_to_bytes(long n) {
        if (!X.IS_LITTLE_ENDIAN) {
            n = Long.reverseBytes(n);
        }

        byte[] b = new byte[8];
        b[7] = (byte) ((n >> 56) & 0xFF);
        b[6] = (byte) ((n >> 48) & 0xFF);
        b[5] = (byte) ((n >> 40) & 0xFF);
        b[4] = (byte) ((n >> 32) & 0xFF);
        b[3] = (byte) ((n >> 24) & 0xFF);
        b[2] = (byte) ((n >> 16) & 0xFF);
        b[1] = (byte) ((n >> 8) & 0xFF);
        b[0] = (byte) (n & 0xFF);
        return b;
    }

    // Byte array to float.
    public static float bytes_to_float(byte[] b) {
        return Float.intBitsToFloat(bytes_to_int(b));
    }

    // Byte array to double.
    public static double bytes_to_double(byte[] b) {
        return Double.longBitsToDouble(bytes_to_long(b));
    }
}
