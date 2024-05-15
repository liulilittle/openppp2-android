package supersocksr.ppp.android.openppp2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ActivityX {
    // To determine whether the specified activity is running repeatedly, call it at onCreate.
    public static boolean activity_is_reload_activity(Activity activity) {
        if (activity == null) {
            return true;
        }

        try {
            return !activity.isTaskRoot();
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Determines whether the specified activity is running repeatedly,
    // But must be started by tapping the app from the Android Home, and calls it when onCreate is created.
    public static boolean activity_is_reload_activity_by_launcher(Activity activity) {
        if (activity_is_reload_activity(activity)) {
            try {
                Intent intent = activity.getIntent();
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
                    return action != null && action.equals(Intent.ACTION_MAIN);
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    // Gets a list of all activity info instances specified above.
    @NonNull
    @Contract("null -> new")
    public static ActivityInfo[] activity_get_all_activity_info(@Nullable Context context) {
        if (context == null) {
            return new ActivityInfo[0];
        }

        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (packageInfo == null || packageInfo.activities == null) {
                return new ActivityInfo[0];
            } else {
                return packageInfo.activities;
            }
        } catch (Throwable ignored) {
            return new ActivityInfo[0];
        }
    }

    // Gets a list of all open activity instances specified above.
    @NonNull
    @SuppressWarnings("unchecked")
    @SuppressLint("DiscouragedPrivateApi")
    public static Activity[] activity_get_all_activity(@Nullable Application application) {
        if (application == null) {
            return new Activity[0];
        }

        List<Activity> list = new ArrayList<Activity>();
        try {
            Class<Application> applicationClass = Application.class;
            Field mLoadedApkField = applicationClass.getDeclaredField("mLoadedApk");

            mLoadedApkField.setAccessible(true);
            Object mLoadedApk = mLoadedApkField.get(application);
            if (mLoadedApk == null) {
                return new Activity[0];
            }

            Class<?> mLoadedApkClass = mLoadedApk.getClass();
            Field mActivityThreadField = mLoadedApkClass.getDeclaredField("mActivityThread");

            mActivityThreadField.setAccessible(true);
            Object mActivityThread = mActivityThreadField.get(mLoadedApk);
            if (mActivityThread == null) {
                return new Activity[0];
            }

            Class<?> mActivityThreadClass = mActivityThread.getClass();
            Field mActivitiesField = mActivityThreadClass.getDeclaredField("mActivities");

            mActivitiesField.setAccessible(true);
            Object mActivities = mActivitiesField.get(mActivityThread);

            // 注意这里一定写成Map，低版本这里用的是HashMap，高版本用的是ArrayMap
            if (mActivities instanceof Map) {
                Map<Object, Object> arrayMap = (Map<Object, Object>) mActivities;
                for (Map.Entry<Object, Object> entry : arrayMap.entrySet()) {
                    Object value = entry.getValue();
                    Class<?> activityClientRecordClass = value.getClass();

                    Field activityField = activityClientRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);

                    Object o = activityField.get(value);
                    list.add((Activity) o);
                }
            }
        } catch (Throwable ignored) {
            list = null;
        }

        if (list == null) {
            return new Activity[0];
        } else {
            return list.toArray(new Activity[0]);
        }
    }

    // Gets a list of all open activity instances specified above.
    @NonNull
    public static Activity[] activity_get_all_activity(@Nullable Context context) {
        return activity_get_all_activity(ApplicationX.application_get_application(context));
    }

    // Gets the activity currently displayed at the top of the window.
    @Nullable
    public static Activity activity_get_activity(@Nullable Application application) {
        if (application == null) {
            return null;
        }

        Activity[] activities = activity_get_all_activity(application);
        if (activities.length < 1) {
            return null;
        }

        String topActivity = activity_get_top_activity_name_and_process_name(application);
        if (!TextUtils.isEmpty(topActivity)) {
            for (Activity activity : activities) {
                if (activity == null) {
                    continue;
                }

                String className = activity.getLocalClassName();
                if (className.equals(topActivity)) {
                    return activity;
                }
            }
        }

        for (Activity activity : activities) {
            if (activity == null) {
                continue;
            } else {
                return activity;
            }
        }
        return null;
    }

    // Gets the activity currently displayed at the top of the window.
    @Nullable
    public static Activity activity_get_activity(@Nullable Context context) {
        Application application = ApplicationX.application_get_application(context);
        return activity_get_activity(application);
    }

    // Gets the activity currently displayed at the top of the window.
    private static String activity_get_top_activity_name_and_process_name(@NonNull Context context) {
        String topActivityName = null;
        ActivityManager activityManager = (ActivityManager) (context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> runningTaskInfoLists = activityManager.getRunningTasks(1);
        if (runningTaskInfoLists != null && runningTaskInfoLists.size() > 0) {
            ActivityManager.RunningTaskInfo runningTaskInfo = runningTaskInfoLists.get(0);
            ComponentName f = runningTaskInfo.topActivity;
            if (f != null) {
                String topActivityClassName = f.getClassName();
                String[] temp = topActivityClassName.split("\\.");
                if (!X.is_empty(temp)) {
                    topActivityName = temp[temp.length - 1];
                }
            }
        }
        return topActivityName;
    }
}
