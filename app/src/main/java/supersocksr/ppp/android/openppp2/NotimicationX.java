package supersocksr.ppp.android.openppp2;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.net.VpnService;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.jetbrains.annotations.NotNull;

import supersocksr.ppp.android.MainActivity;
import supersocksr.ppp.android.R;

public final class NotimicationX {
    // Send a notification message to the notification bar on the screen.
    public static void notification_send_to_notice_bar(@NotNull VpnService service, int channel_id, String title, String sub_text, long when) {
        notification_send_to_notice_bar(service, channel_id, title, null, sub_text, when);
    }

    // Send a notification message to the notification bar on the screen.
    @SuppressLint({"UnspecifiedImmutableFlag", "ForegroundServiceType"})
    public static void notification_send_to_notice_bar(@NotNull VpnService service, int channel_id, String title, String text, String sub_text, long when) {
        try {
            String channelKey = service.getClass().getName();
            NotificationCompat.Builder builder;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                builder = new NotificationCompat.Builder(service, channelKey);
            } else {
                //noinspection deprecation
                builder = new NotificationCompat.Builder(service);
            }

            builder.setAutoCancel(false).setSmallIcon(R.drawable.ic_launcher).setTicker(service.getString(R.string.app_name)).setWhen(when).setPriority(Notification.PRIORITY_DEFAULT).setContentIntent(PackageX.package_get_pending_intent(service, MainActivity.class));
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                builder.setSubText(sub_text);
                builder.setContentTitle(title);
            } else {
                builder.setContentTitle(title + " • " + sub_text);
            }

            builder.setStyle(new NotificationCompat.BigTextStyle(builder).bigText(text));
            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_NO_CLEAR; // Notification.FLAG_FOREGROUND_SERVICE
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                NotificationManager notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = notificationManager.getNotificationChannel(channelKey);
                if (channel == null) {
                    channel = new NotificationChannel(channelKey, channelKey, NotificationManager.IMPORTANCE_DEFAULT);
                    channel.enableLights(true);
                    channel.setShowBadge(false);
                    channel.setLightColor(Color.GREEN);
                    notificationManager.createNotificationChannel(channel);
                }

                notificationManager.notify(channel_id, notification);
            } else {
                service.startForeground(channel_id, notification);
            }
        } catch (Throwable ignored) {
        }
    }

    // Clear the notification bar message sent by the app service to the top of the screen.
    public static void notification_clear_to_notice_bar(@NotNull VpnService service, int channel_id) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            try {
                NotificationManager notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(channel_id);
            } catch (Throwable ignored) {
            }
        } else {
            try {
                service.stopForeground(true);
            } catch (Throwable ignored) {
            }
        }
    }
}
