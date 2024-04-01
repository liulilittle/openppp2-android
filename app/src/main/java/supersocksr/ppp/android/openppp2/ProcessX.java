package supersocksr.ppp.android.openppp2;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import androidx.annotation.Nullable;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidProcess;

import java.util.ArrayList;
import java.util.List;

public final class ProcessX {
    // Gets the current process ID.
    public static int my_pid() {
        try {
            return android.os.Process.myPid();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    // Determines whether the specified process is still running.
    public static boolean process_is_alive(java.lang.Process process) {
        if (process == null) {
            return false;
        }

        boolean isAlive = false;
        try {
            process.exitValue();
        } catch (Throwable ignored) {
            isAlive = true;
        }
        return isAlive;
    }

    // Gets all running processes that can be retrieved under the current user privileges.
    public static List<AndroidProcess> process_get_all_processes() {
        try {
            return AndroidProcesses.getRunningProcesses();
        } catch (Throwable ignored) {
            return new ArrayList<AndroidProcess>();
        }
    }

    // Through the process ID for process information.
    @Nullable
    public static AndroidProcess process_get_by_id(int pID) {
        for (AndroidProcess process : AndroidProcesses.getRunningProcesses()) {
            if (process.pid == pID) {
                return process;
            }
        }
        return null;
    }

    // Determine whether the Android process that the context points to is being debugged by adb.
    public static boolean is_debuggable(Context context) {
        if (context == null) {
            return false;
        }

        try {
            ApplicationInfo info = context.getApplicationInfo();
            if (info == null) {
                return false;
            }

            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
