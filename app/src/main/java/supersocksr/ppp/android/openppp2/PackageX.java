package supersocksr.ppp.android.openppp2;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.VpnService;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.jaredrummler.android.processes.BuildConfig;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import supersocksr.ppp.android.MainActivity;
import supersocksr.ppp.android.openppp2.i.IFunc_2;
import supersocksr.ppp.android.openppp2.i.PackageInformation;
import supersocksr.ppp.android.openppp2.i.PackageInformation2;

public final class PackageX {
    // Gets the app package name for the context.
    @Nullable
    public static String package_get_package_name(Context context) {
        if (context != null) {
            try {
                return context.getPackageName();
            } catch (Throwable ignored) {
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                //noinspection Since15
                return MainActivity.class.getPackageName();
            } catch (Throwable ignored) {
            }
        }

        try {
            Package p = MainActivity.class.getPackage();
            if (p != null) {
                return p.getName();
            }
        } catch (Throwable ignored) {
        }

        return BuildConfig.APPLICATION_ID;
    }

    // Gets the name of the app process in the specified context.
    @Nullable
    public static String package_get_app_process_name(@NotNull Context context) {
        try {
            // 当前应用pid
            int pid = android.os.Process.myPid();

            // 任务管理类
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (manager == null) {
                return null;
            }

            // 遍历所有应用
            List<ActivityManager.RunningAppProcessInfo> list = manager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo info : list) {
                if (info.pid == pid) { // 得到当前应用
                    return info.processName; // 返回包名
                }
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    // Gets the signature of the app from the specified context.
    @Nullable
    @SuppressLint("PackageManagerGetSignatures")
    public static String package_get_app_signature(@NotNull Context context, String packageName) {
        try {
            // 包管理操作管理类
            PackageManager pm = context.getPackageManager();
            if (pm == null) {
                return null;
            }

            PackageInfo pack_info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            if (pack_info == null) {
                return null;
            }

            // 获取当前应用签名
            Signature[] signatures = pack_info.signatures;
            if (signatures == null || signatures.length < 1) {
                return null;
            }

            return signatures[0].toCharsString();
        } catch (Throwable ignored) {
        }
        return packageName;
    }

    // Gets the app icon by specifying the context and the app package name.
    @Nullable
    public static Drawable package_get_app_icon(@NotNull Context context, String packageName) {
        try {
            // 包管理操作管理类
            PackageManager pm = context.getPackageManager();
            if (pm == null) {
                return null;
            }

            // 获取到应用信息
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return info.loadIcon(pm);
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Get the version information of the app by specifying the context and the app package name.
    @Nullable
    public static String package_get_app_version(@NotNull Context context, String packageName) {
        try {
            // 包管理操作管理类
            PackageManager pm = context.getPackageManager();
            if (pm == null) {
                return null;
            }

            PackageInfo pack_info = pm.getPackageInfo(packageName, 0);
            if (pack_info == null) {
                return null;
            }

            return pack_info.versionName;
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Gets the name of the app by specifying the context and the app package name.
    @Nullable
    public static String package_get_app_name(@NotNull Context context, String packageName) {
        try {
            // 包管理操作管理类
            PackageManager pm = context.getPackageManager();
            if (pm == null) {
                return null;
            }

            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return info.loadLabel(pm).toString();
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Gets the name of its currently active activity in the specified context.
    @Nullable
    public static String package_get_current_activity_name(@NotNull Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                return null;
            }

            //noinspection deprecation
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
            if (tasks == null || tasks.isEmpty()) {
                return null;
            }

            ActivityManager.RunningTaskInfo task = tasks.get(0);
            if (task == null) {
                return null;
            }

            ComponentName top_activity = task.topActivity;
            if (top_activity == null) {
                return null;
            }

            return top_activity.getClassName();
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Gets its permission list by specifying the context and package name.
    @Nullable
    public static String[] package_get_all_permissions(@NotNull Context context, String packageName) {
        try {
            // 包管理操作管理类
            PackageManager pm = context.getPackageManager();
            if (pm == null) {
                return null;
            }

            PackageInfo pack_info = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            if (pack_info == null) {
                return null;
            }

            // 获取到所有的权限
            return pack_info.requestedPermissions;
        } catch (Throwable ignored) {
            return null;
        }
    }

    // PendingIntent Obtains the PendingIntent from context and type information, which is called when the notification bar is displayed.
    @Nullable
    public static PendingIntent package_get_pending_intent(Context context, Class<?> clazz) {
        if (context == null || clazz == null) {
            return null;
        }

        PendingIntent intent = package_get_pending_intent_by_context(context, clazz);
        if (intent == null) {
            if (context instanceof Application) {
                return null;
            } else {
                context = ApplicationX.application_get_application(context);
            }
        }
        return package_get_pending_intent_by_context(context, clazz);
    }

    // PendingIntent Obtains the PendingIntent from context and type information, which is called when the notification bar is displayed.
    public static PendingIntent package_get_pending_intent_by_context(Context context, Class<?> clazz) {
        PendingIntent intent = package_get_pending_intent_by_context(context, clazz, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (intent == null) {
            intent = package_get_pending_intent_by_context(context, clazz, 0);
        }
        return intent;
    }

    // PendingIntent Obtains the PendingIntent from context and type information, which is called when the notification bar is displayed.
    @SuppressLint("UnspecifiedImmutableFlag")
    public static PendingIntent package_get_pending_intent_by_context(Context context, Class<?> clazz, int flags) {
        if (context == null) {
            return null;
        }

        Intent intent = new Intent(context, clazz);
        if (flags != 0) {
            intent.setFlags(flags);
        }

        try {
            PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (pi != null) {
                return pi;
            }
        } catch (Throwable ignored) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE);
                if (pi != null) {
                    return pi;
                }
            } catch (Throwable ignored) {
            }
        }

        try {
            PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
            if (pi != null) {
                return pi;
            }
        } catch (Throwable ignored) {
        }

        Intent[] intents = new Intent[]{intent};
        try {
            PendingIntent pi = PendingIntent.getActivities(context, 0, intents, PendingIntent.FLAG_UPDATE_CURRENT);
            if (pi != null) {
                return pi;
            }
        } catch (Throwable ignored) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                PendingIntent pi = PendingIntent.getActivities(context, 0, intents, PendingIntent.FLAG_MUTABLE);
                if (pi != null) {
                    return pi;
                }
            } catch (Throwable ignored) {
            }
        }

        try {
            PendingIntent pi = PendingIntent.getActivities(context, 0, intents, 0);
            if (pi != null) {
                return pi;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    // Get the information of all the apps installed in the current system and related to network permissions.
    @NotNull
    @SuppressLint("QueryPermissionsNeeded")
    private static <T> List<T> package_get_all_network_application_impl(@NotNull Context context, IFunc_2<PackageManager, ApplicationInfo, T> cb) {
        List<T> infoList = new ArrayList<T>();
        try {
            PackageManager pm = context.getPackageManager();
            if (pm == null) {
                return infoList;
            }

            Set<String> packages = new HashSet<String>();
            for (ResolveInfo resolveInfo : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
                if (resolveInfo == null) {
                    continue;
                }

                ActivityInfo ai = resolveInfo.activityInfo;
                if (ai == null) {
                    continue;
                }

                packages.add(ai.packageName);
            }

            String selfPackageName = package_get_package_name(context);
            for (PackageInfo packageInfo : pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
                if (packageInfo == null) {
                    continue;
                } else if (!packages.contains(packageInfo.packageName)) {
                    continue;
                } else if (selfPackageName != null && selfPackageName.equals(packageInfo.packageName)) {
                    continue;
                } else {
                    String[] requested_permissions = packageInfo.requestedPermissions;
                    if (requested_permissions == null || requested_permissions.length < 1) {
                        continue;
                    }
                }

                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                if (applicationInfo != null) {
                    for (String permission : packageInfo.requestedPermissions) {
                        if (TextUtils.equals(permission, android.Manifest.permission.INTERNET)) {
                            T pi = cb.handle(pm, applicationInfo);
                            if (pi != null) {
                                infoList.add(pi);
                            }
                            break;
                        }
                    }
                }
            }
            return infoList;
        } catch (Throwable ignored) {
            return infoList;
        }
    }

    // Get the information of all the apps installed in the current system and related to network permissions.
    @NotNull
    public static List<PackageInformation2> package_get_all_network_application_2(@NotNull Context context) {
        return package_get_all_network_application_impl(context, (pm, applicationInfo) -> {
            try {
                PackageInformation2 pi = new PackageInformation2();
                pi.applicationName = applicationInfo.loadLabel(pm).toString();
                pi.packageName = applicationInfo.packageName;
                pi.iconImageUrl = "data:image/png;base64,";

                String image = DrawingX.bitmap_to_base64(applicationInfo.loadIcon(pm), Bitmap.CompressFormat.PNG);
                if (TextUtils.isEmpty(image)) {
                    return null;
                } else {
                    pi.iconImageUrl += image;
                }

                return pi;
            } catch (Throwable ignored) {
                return null;
            }
        });
    }

    // Get the information of all the apps installed in the current system and related to network permissions.
    @NotNull
    public static List<PackageInformation> package_get_all_network_application(@NotNull Context context) {
        return package_get_all_network_application_impl(context, (pm, applicationInfo) -> {
            try {
                PackageInformation pi = new PackageInformation();
                pi.applicationName = applicationInfo.loadLabel(pm).toString();
                pi.packageName = applicationInfo.packageName;
                pi.applicationIcon = applicationInfo.loadIcon(pm);
                return pi;
            } catch (Throwable ignored) {
                return null;
            }
        });
    }

    // Set those web apps to go with or without the VPN network.
    // When calling the trim function in the sendfd/recvfd mode.
    public static boolean package_allowed_application_packages(VpnService service, VpnService.Builder builder) {
        // The current VPN application's own registration cannot be banned, in order to show the importance of reminding it,
        // We clearly need to allow the current VPN application's package name to take the VPN route.
        String my_package_name = PackageX.package_get_package_name(service);
        if (X.is_empty(my_package_name)) {
            return false;
        }

        try {
            builder.addAllowedApplication(my_package_name);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Set those web apps to go with or without the VPN network.
    public static boolean package_allowed_application_packages(VpnService service, VpnService.Builder builder, Set<String> packages, boolean allowed) {
        if (X.is_empty(packages)) {
            return false;
        }

        // Put the following specified network APP package name corresponding to the program,
        // Set the VPN global routing rules, those apps need to go to VPN, those don't go to VPN.
        String my_package_name = PackageX.package_get_package_name(service);
        for (String package_name : packages) {
            if (X.is_empty(package_name)) {
                continue;
            }

            if (my_package_name != null) {
                if (package_name.equals(my_package_name)) {
                    continue;
                }
            }

            try {
                if (allowed) { // BYPASS
                    builder.addAllowedApplication(package_name);
                } else {       // PROXIFIER
                    builder.addDisallowedApplication(package_name);
                }
            } catch (Throwable ignored) {
            }
        }

        return true;
    }
}
