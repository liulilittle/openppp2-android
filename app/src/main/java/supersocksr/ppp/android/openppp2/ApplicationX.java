package supersocksr.ppp.android.openppp2;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

import supersocksr.ppp.android.R;

public final class ApplicationX {
    // Get a reference to application by using Context.
    public static Application application_get_application(@Nullable Context context) {
        if (context == null) {
            return null;
        }

        if (context instanceof Application) {
            return (Application) context;
        }

        context = context.getApplicationContext();
        if (context instanceof Application) {
            return (Application) context;
        }
        return null;
    }

    // Get a reference to the current application.
    @Nullable
    @SuppressLint("PrivateApi")
    public static Application application_get_application() {
        Application application = null;
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread");

            // 得到当前的ActivityThread对象
            Object localObject = currentActivityThreadMethod.invoke(null, (Object[]) null);
            if (localObject == null) {
                return null;
            }

            Method getApplicationMethod = activityThreadClass.getMethod("getApplication");
            application = (Application) getApplicationMethod.invoke(localObject, (Object[]) null);
        } catch (Throwable ignored) {
        }
        return application;
    }

    // Get application name by using Context.
    public static String application_get_app_name(@Nullable Context context) {
        if (context == null) {
            return null;
        } else {
            return X.call(() -> context.getString(R.string.app_name), null).item2;
        }
    }
}
