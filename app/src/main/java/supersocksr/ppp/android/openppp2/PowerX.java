package supersocksr.ppp.android.openppp2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class PowerX {
    // Open the weak lock that controls sleep state in the power plan.
    @SuppressLint({"InvalidWakeLockTag", "WakelockTimeout"})
    @Nullable
    public static PowerManager.WakeLock power_open_wake_lock(@NonNull Context context, String tag, long timeout) {
        if (timeout == 0 || TextUtils.isEmpty(tag)) {
            return null;
        }

        try {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager == null) {
                return null;
            }

            PowerManager.WakeLock wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, tag);
            if (wakelock == null) {
                return null;
            }

            if (timeout < 0) {
                wakelock.acquire();
            } else {
                wakelock.acquire(timeout);
            }
            return wakelock;
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Close the weak lock that controls sleep in the power plan.
    public static boolean power_close_wake_lock(PowerManager.WakeLock lock) {
        if (lock == null) {
            return false;
        }

        try {
            lock.release();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Check that the current screen is lit up.
    // return:
    // -1 err
    // 0  on
    // 1  off
    public static int power_screen_is_turn_on(@NonNull Context context) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm == null) {
                return -1;
            }

            //noinspection deprecation
            return pm.isScreenOn() ? 0 : 1;
        } catch (Throwable ignored) {
            return -1;
        }
    }
}
