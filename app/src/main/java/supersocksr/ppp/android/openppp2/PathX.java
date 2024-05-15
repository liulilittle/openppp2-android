package supersocksr.ppp.android.openppp2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class PathX {
    // Overwrite the path string.
    public static String path_rewrite(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        try {
            path = path.trim();
            if (path.isEmpty()) {
                return path;
            }

            while (path.indexOf('\\') > -1) {
                path = path.replace("\\", "/");
            }

            path = path.replaceAll("//", "/");
            return path;
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Gets the directory where the app installed the native so dynamic inventory.
    @Nullable
    public static String path_get_native_library_dir(@NotNull Context context) {
        try {
            ApplicationInfo ai = context.getApplicationInfo();
            if (ai == null) {
                return null;
            }

            return ai.nativeLibraryDir;
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Obtain the directory of the files in the app that will not be backed up by the system.
    @Nullable
    public static String path_get_no_backup_files_dir(@NotNull Context context) {
        try {
            File f = context.getNoBackupFilesDir();
            if (f == null) {
                return null;
            }

            return f.getPath();
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Gets the cache directory that the app can access.
    @Nullable
    public static String path_get_cache_dir(@NotNull Context context) {
        return path_get_cache_dir(context, false);
    }

    // Use context to get the cache file directory available to the app.
    @SuppressLint("SdCardPath")
    @Nullable
    public static String path_get_cache_dir(@NotNull Context context, boolean relink) {
        if (relink) {
            String package_name = PackageX.package_get_package_name(context);
            if (package_name == null) {
                return null;
            }

            String cacheDir = "/data/data/";
            cacheDir += package_name;
            cacheDir += "/cache";
            return cacheDir;
        } else {
            File cacheDir = context.getCacheDir();
            if (cacheDir == null) {
                return null;
            } else {
                return cacheDir.getPath();
            }
        }
    }

    // Gets the directory where the app can store custom files.
    @Nullable
    public static String path_get_files_dir(@NotNull Context context) {
        try {
            File f = context.getFilesDir();
            if (f == null) {
                return null;
            }

            return f.getPath();
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Gets the directory that the app can use for additional cache storage.
    @Nullable
    public static String path_get_external_cache_dir(@NotNull Context context) {
        try {
            File f = context.getExternalCacheDir();
            if (f == null) {
                return null;
            }

            return f.getPath();
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Gets a directory that the app can use to store additional files.
    @Nullable
    public static String path_get_external_files_dir(@NotNull Context context, String subdir) {
        try {
            File f = context.getExternalFilesDir(subdir);
            if (f == null) {
                return null;
            }

            return f.getPath();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
