package com.arewascope.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "arewa_scope_news";
    private static final String CHANNEL_NAME = "Arewa Scope News";
    private static final String CHANNEL_DESCRIPTION = "Breaking news and updates from Arewa Scope";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        android.util.Log.d("ArewaScopeFCM", "New token generated");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        createNotificationChannel(this);

        Map<String, String> data = remoteMessage.getData();
        String title = "Arewa Scope";
        String body = "New update available";
        String url = MainActivity.SITE_URL;

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        if (data != null) {
            if (data.containsKey("title") && data.get("title") != null && !data.get("title").trim().isEmpty()) {
                title = data.get("title");
            }
            if (data.containsKey("body") && data.get("body") != null && !data.get("body").trim().isEmpty()) {
                body = data.get("body");
            }
            if (data.containsKey("url") && data.get("url") != null && !data.get("url").trim().isEmpty()) {
                url = data.get("url");
            } else if (data.containsKey("link") && data.get("link") != null && !data.get("link").trim().isEmpty()) {
                url = data.get("link");
            }
        }

        showNotification(title, body, url);
    }

    private void showNotification(String title, String body, String url) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.NOTIFICATION_URL_EXTRA, url);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_arewa)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.parseColor("#10B981"));
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
