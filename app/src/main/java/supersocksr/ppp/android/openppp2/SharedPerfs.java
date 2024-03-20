package supersocksr.ppp.android.openppp2;

import android.annotation.SuppressLint;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public final class SharedPerfs {
    public static final String PREFS = "supersocksr.ppp.android.shared_preferences";

    @SuppressLint("ApplySharedPref")
    public static boolean del(Context context, String key) {
        return set(context, key, null);
    }

    @SuppressLint("ApplySharedPref")
    public static boolean set(Context context, String key, String value) {
        if (context == null || TextUtils.isEmpty(key)) {
            return false;
        }

        try {
            SharedPreferences sharedPrefs = context.getSharedPreferences(SharedPerfs.PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            if (value == null) {
                editor.remove(key);
            } else {
                editor.putString(key, value);
            }

            editor.commit();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @SuppressLint("ApplySharedPref")
    public static boolean set(Context context, String key, int value) {
        if (context == null || TextUtils.isEmpty(key)) {
            return false;
        }

        try {
            SharedPreferences sharedPrefs = context.getSharedPreferences(SharedPerfs.PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt(key, value);
            editor.commit();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @SuppressLint("ApplySharedPref")
    public static boolean set(Context context, String key, long value) {
        if (context == null || TextUtils.isEmpty(key)) {
            return false;
        }

        try {
            SharedPreferences sharedPrefs = context.getSharedPreferences(SharedPerfs.PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putLong(key, value);
            editor.commit();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @SuppressLint("ApplySharedPref")
    public static boolean set(Context context, String key, boolean value) {
        if (context == null || TextUtils.isEmpty(key)) {
            return false;
        }

        try {
            SharedPreferences sharedPrefs = context.getSharedPreferences(SharedPerfs.PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean(key, value);
            editor.commit();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @SuppressLint("ApplySharedPref")
    public static boolean set(Context context, String key, float value) {
        if (context == null || TextUtils.isEmpty(key)) {
            return false;
        }

        try {
            SharedPreferences sharedPrefs = context.getSharedPreferences(SharedPerfs.PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putFloat(key, value);
            editor.commit();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @SuppressLint("ApplySharedPref")
    public static String get(Context context, String key) {
        return get(context, key, null);
    }

    @SuppressLint("ApplySharedPref")
    public static String get(Context context, String key, String defValue) {
        if (context == null || TextUtils.isEmpty(key)) {
            return null;
        }
        try {
            SharedPreferences sharedPrefs = context.getSharedPreferences(SharedPerfs.PREFS, Context.MODE_PRIVATE);
            return sharedPrefs.getString(key, defValue);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @SuppressLint("ApplySharedPref")
    public static int get(Context context, String key, int defValue) {
        if (context == null || TextUtils.isEmpty(key)) {
            return defValue;
        }
        try {
            SharedPreferences sharedPrefs = context.getSharedPreferences(SharedPerfs.PREFS, Context.MODE_PRIVATE);
            return sharedPrefs.getInt(key, defValue);
        } catch (Throwable ignored) {
            return defValue;
        }
    }

    @SuppressLint("ApplySharedPref")
    public static long get(Context context, String key, long defValue) {
        if (context == null || TextUtils.isEmpty(key)) {
            return defValue;
        }
        try {
            SharedPreferences sharedPrefs = context.getSharedPreferences(SharedPerfs.PREFS, Context.MODE_PRIVATE);
            return sharedPrefs.getLong(key, defValue);
        } catch (Throwable ignored) {
            return defValue;
        }
    }

    @SuppressLint("ApplySharedPref")
    public static boolean get(Context context, String key, boolean defValue) {
        if (context == null || TextUtils.isEmpty(key)) {
            return defValue;
        }
        try {
            SharedPreferences sharedPrefs = context.getSharedPreferences(SharedPerfs.PREFS, Context.MODE_PRIVATE);
            return sharedPrefs.getBoolean(key, defValue);
        } catch (Throwable ignored) {
            return defValue;
        }
    }

    @SuppressLint("ApplySharedPref")
    public static float get(Context context, String key, float defValue) {
        if (context == null || TextUtils.isEmpty(key)) {
            return defValue;
        }
        try {
            SharedPreferences sharedPrefs = context.getSharedPreferences(SharedPerfs.PREFS, Context.MODE_PRIVATE);
            return sharedPrefs.getFloat(key, defValue);
        } catch (Throwable ignored) {
            return defValue;
        }
    }

    public static boolean backup(Context context) {
        if (context == null) {
            return false;
        }
        try {
            // backup the changes
            BackupManager mBackupManager = new BackupManager(context);
            mBackupManager.dataChanged();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
