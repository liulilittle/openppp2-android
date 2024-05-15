package supersocksr.ppp.android.openppp2;

import android.text.TextUtils;

import androidx.annotation.Nullable;

public final class SystemX {
    public static final int SIGHUP = 1;
    public static final int SIGINT = 2;
    public static final int SIGKILL = 9;
    public static final int SIGSEGV = 11;
    public static final int SIGPIPE = 13;

    // Gets the number of cores for the current cpu.
    public static int cpu_processor_count() {
        try {
            Runtime r = Runtime.getRuntime();
            int c = r.availableProcessors();
            if (c < 1) {
                c = 1;
            }
            return c;
        } catch (Throwable ignored) {
            return 1;
        }
    }

    // Modify file permissions to read and write.
    public static boolean chmod(String path) {
        return chmod("a+rw", path) | chmod("a+rwx", path);
    }

    // Modify the file permissions.
    public static boolean chmod(String permissions, String path) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        if (path == null || path.isEmpty()) {
            return false;
        }

        String argument = permissions + " " + path;
        return system("/system/bin/chmod " + argument);
    }

    // The execution of system commands is equivalent to the function of the C library system function.
    public static boolean system(String cmd) {
        return system(cmd, -1);
    }

    // The execution of system commands is equivalent to the function of the C library system function.
    public static boolean system(String cmd, int timeout) {
        Process process = exec(cmd);
        if (process == null) {
            return false;
        }

        int err = 0;
        try {
            if (timeout < 0) {
                err = process.waitFor();
            } else if (timeout > 0) {
                process.wait(timeout);
                err = process.exitValue();
            }
            return err == 0;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Execute the cmd command, run a system program or custom program, and pass the command line to the destination program.
    @Nullable
    public static Process exec(String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            return null;
        }

        try {
            return Runtime.getRuntime().exec(cmd);
        } catch (Throwable ignored) {
            return null;
        }
    }


    // Sends a signal to the process with the specified pid.
    public static boolean signal(int pID, int signal) {
        try {
            android.os.Process.sendSignal(pID, signal);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Forcibly exit the current process.
    public static boolean exit() {
        kill(ProcessX.my_pid());
        try {
            Runtime runtime = Runtime.getRuntime();
            if (runtime != null) {
                runtime.exit(0);
                return true;
            }
        } catch (Throwable ignored) {
        }

        try {
            SystemX.exit(0);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Forcibly exit the current process.
    public static void exit(int status) {
        try {
            Runtime.getRuntime().exit(status);
        } catch (Throwable ignored1) {
            try {
                java.lang.System.exit(status);
            } catch (Throwable ignored2) {
                int pid = ProcessX.my_pid();
                kill(pid);
            }
        }
    }

    // Sends a KILL signal to the process with the specified pid and forces the target process to be killed.
    public static boolean kill(int pID) {
        signal(pID, SIGINT);
        signal(pID, SIGKILL);

        try {
            android.os.Process.killProcess(pID);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
