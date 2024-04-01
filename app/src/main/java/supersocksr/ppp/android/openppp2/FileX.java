package supersocksr.ppp.android.openppp2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import supersocksr.ppp.android.c.libopenppp2;

public final class FileX {
    // Check whether the file resource pointed by the file path exists.
    public static boolean file_exists(String path) {
        path = PathX.path_rewrite(path);
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        try {
            File f = new File(path);
            return f.exists();
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Writes the specified string content data to the specified file path.
    public static boolean file_write_all_text(Context context, String path, String content) {
        return file_write_all_text(context, path, content, null);
    }

    // Try to create a new blank file.
    public static boolean file_create_new(@Nullable String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        File file = new File(path);
        try {
            String dir = file.getParent();
            if (!TextUtils.isEmpty(dir)) {
                File f = new File(dir);
                if (!f.exists()) {
                    if (!f.mkdirs()) {
                        return false;
                    }
                }
            }

            if (file.exists()) {
                if (!file.delete()) {
                    return false;
                }
            }

            if (!file.exists()) {
                return file.createNewFile();
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Writes the specified string content and converts the data to the specified file path according to the charset.
    public static boolean file_write_all_text(Context context, String path, String content, Charset charset) {
        if (context == null) {
            return false;
        }

        path = PathX.path_rewrite(path);
        if (path == null || path.isEmpty()) {
            return false;
        }

        if (content == null || content.isEmpty()) {
            return file_delete(context, path);
        }

        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        if (!file_create_new(path)) {
            return false;
        }

        try {
            FileOutputStream fw = new FileOutputStream(new File(path));
            fw.write(content.getBytes(charset));
            fw.flush();
            fw.close();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Reads text content from the specified file path and converts it to the character set encoding.
    @Nullable
    public static String file_read_all_text(String path) {
        return file_read_all_text(path, StandardCharsets.UTF_8);
    }

    // Reads text content from the specified file path and converts it to the character set encoding.
    @Nullable
    public static String file_read_all_text(String path, Charset encoding) {
        path = PathX.path_rewrite(path);
        if (path == null || path.isEmpty()) {
            return null;
        }

        if (encoding == null) {
            encoding = StandardCharsets.UTF_8;
        }

        File f = new File(path);
        if (!f.exists()) {
            return null;
        }

        if (!f.canRead()) {
            return null;
        }

        FileInputStream fs;
        try {
            //noinspection resource
            fs = new FileInputStream(f);
        } catch (Throwable ignored) {
            return null;
        }

        long length;
        try {
            length = f.length();
        } catch (Throwable ignored) {
            return null;
        }

        if (length < 1) {
            return null;
        }

        byte[] bytes = new byte[(int) length];
        try {
            //noinspection ResultOfMethodCallIgnored
            fs.read(bytes);
        } catch (Throwable ignored) {
            return null;
        }

        try {
            return new String(bytes, encoding);
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Delete a file in the specified file path.
    public static boolean file_delete(Context context, String path) {
        path = PathX.path_rewrite(path);
        if (context == null) {
            return false;
        }

        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            File f = new File(path);
            if (f.exists()) {
                context.deleteFile(path);
            }
            return true;
        } catch (Throwable ignored) {
        }

        try {
            File f = new File(path);
            if (f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Get the type of the socket file descriptor.
    public static int file_get_socket_type(int fd) {
        return libopenppp2.c.socket_get_socket_type(fd);
    }

    // The file descriptor object is converted to fd int.
    @SuppressLint("DiscouragedPrivateApi")
    public static int file_descriptor_to_int(FileDescriptor fd) {
        if (fd == null) {
            return -1;
        }

        try {
            Method method = FileDescriptor.class.getDeclaredMethod("getInt$");
            Integer r = (Integer) method.invoke(fd);
            if (r != null) {
                return r;
            }
        } catch (Throwable ignored) {
        }

        try {
            Field field = FileDescriptor.class.getDeclaredField("descriptor");
            field.setAccessible(true);
            return field.getInt(fd);
        } catch (Throwable ignored) {
        }
        return -1;
    }

    // fd int converts to a file descriptor object.
    @SuppressLint("DiscouragedPrivateApi")
    @Nullable
    public static FileDescriptor int_to_file_descriptor(int fd) {
        FileDescriptor fileDescriptor = new FileDescriptor();
        try {
            Method method = FileDescriptor.class.getDeclaredMethod("setInt$", int.class);
            method.invoke(fileDescriptor, fd);
            return fileDescriptor;
        } catch (Throwable ignored) {
        }

        try {
            Field field = FileDescriptor.class.getDeclaredField("descriptor");
            field.setAccessible(true);
            field.setInt(fileDescriptor, fd);
            return fileDescriptor;
        } catch (Throwable ignored) {
        }
        return null;
    }

    // Gets all fd handles open by the current process.
    @SuppressLint("DiscouragedPrivateApi")
    @Nullable
    public static ArrayList<Integer> file_descriptor_get_all_handles() {
        ArrayList<String> files = file_get_all_files("/proc/" + ProcessX.my_pid() + "/fd", "*");
        if (files == null) {
            return null;
        }

        ArrayList<Integer> results = new ArrayList<Integer>();
        for (String file : files) {
            if (TextUtils.isEmpty(file)) {
                continue;
            }

            int left = file.lastIndexOf('/');
            if (left < 0) {
                continue;
            }

            String fd_str = file.substring(left + 1);
            if (TextUtils.isEmpty(fd_str)) {
                continue;
            }

            int fd = -1;
            try {
                fd = Integer.parseInt(fd_str);
            } catch (Throwable ignored) {
                continue;
            }

            results.add(fd);
        }
        return results;
    }

    // Find all files in the directory by matching wildcard characters.
    @Nullable
    public static ArrayList<String> file_get_all_files(String dir, String pattern) {
        try {
            dir = PathX.path_rewrite(dir);
            if (TextUtils.isEmpty(dir)) {
                return null;
            }

            if (TextUtils.isEmpty(pattern)) {
                pattern = "*";
            }

            File file = new File(dir);
            pattern = pattern.replace('.', '#');
            pattern = pattern.replaceAll("#", "\\\\.");
            pattern = pattern.replace('*', '#');
            pattern = pattern.replaceAll("#", ".*");
            pattern = pattern.replace('?', '#');
            pattern = pattern.replaceAll("#", ".?");
            pattern = "^" + pattern + "$";
            return file_find_all_file_pattern(file, Pattern.compile(pattern));
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Find all files in the directory by matching wildcard characters(internal implement).
    private static ArrayList<String> file_find_all_file_pattern(File file, Pattern p) {
        if (file == null) {
            return null;
        }

        if (file.isFile()) {
            Matcher fMatcher = p.matcher(file.getName());
            if (fMatcher.matches()) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(file.getPath());
                return list;
            }
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                ArrayList<String> list = new ArrayList<String>();
                for (File value : files) {
                    ArrayList<String> s = file_find_all_file_pattern(value, p);
                    if (s != null) {
                        list.addAll(s);
                    }
                }
                return list;
            }
        }
        return null;
    }
}
