package supersocksr.ppp.android;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import supersocksr.ppp.android.openppp2.i.Action;

/**
 * The Application class that manages AppOpenManager.
 */
public class PppApplication extends Application {
    private static PppApplication application_ = null;
    private Handler handler_ = null;

    // Get a reference to the current VPN application.
    public static PppApplication get() {
        return PppApplication.application_;
    }

    // Get a reference to application by using Context.
    public static PppApplication get(@Nullable Context context) {
        if (context == null) {
            return null;
        }

        if (context instanceof PppApplication) {
            return (PppApplication) context;
        }

        context = context.getApplicationContext();
        if (context instanceof PppApplication) {
            return (PppApplication) context;
        }
        return null;
    }

    // Delay the processing or delay the processing loop all the time.
    @Contract(pure = true)
    private static boolean loop_delay(@NonNull PppApplication application, int milliseconds, boolean continuous_loop, Action action) {
        Handler handler = application.handler();
        if (handler == null) {
            return false;
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                action.handle();
                if (continuous_loop) {
                    loop_delay(application, milliseconds, continuous_loop, action);
                }
            }
        }, milliseconds);
        return true;
    }

    // Gets the handler of the application thread.
    public Handler handler() {
        return this.handler_;
    }

    // When the application is created.
    @Override
    public void onCreate() {
        super.onCreate();
        application_ = PppApplication.this;
        handler_ = new Handler();
        loop(this::update, 1000);
    }

    // The action is executed after a delay of X milliseconds.
    public boolean delay(Action action, int milliseconds) {
        if (action == null) {
            return false;
        } else {
            milliseconds = Math.max(0, milliseconds);
        }

        return loop_delay(PppApplication.this, milliseconds, false, action);
    }

    // The action is executed X milliseconds later, but continues to loop.
    public boolean loop(Action action, int milliseconds) {
        if (action == null) {
            return false;
        } else {
            milliseconds = Math.max(0, milliseconds);
        }

        return loop_delay(PppApplication.this, milliseconds, true, action);
    }

    // Per second to perform the update function.
    protected void update() {

    }
}