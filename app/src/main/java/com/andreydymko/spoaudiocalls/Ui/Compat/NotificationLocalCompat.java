package com.andreydymko.spoaudiocalls.Ui.Compat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ReceiverCallNotAllowedException;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.andreydymko.spoaudiocalls.R;
import com.andreydymko.spoaudiocalls.SPONetworking.Users.UserModel;
import com.andreydymko.spoaudiocalls.Ui.CallActivity;
import com.andreydymko.spoaudiocalls.Ui.CallNotificationResultReceiver;

import java.util.Collection;
import java.util.UUID;

public class NotificationLocalCompat {
    public final static String NOTIFICATION_CHANNEL_ID = "SPO_AUDIO_CALLS_INCOMING_CALL_NOTIFICATION_CHANNEL_ID";
    public final static int INCOMING_CALL_NOTIFICATION_ID = 8765;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = context.getString(R.string.notif_channel_name);
            String description = context.getString(R.string.notif_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static void makeIncomingCallNotification(Context context,
                                                              UUID callUuid,
                                                              Collection<UUID> users) {

        StringBuilder stringBuilder = new StringBuilder(users.size() * (UUID.randomUUID().toString().length() + 2));
        for (UUID user : users) {
            stringBuilder.append(user.toString()).append("; ");
        }

        Intent intentAccept = new Intent(context, CallNotificationResultReceiver.class);
        intentAccept.setAction(CallActivity.INTENT_ACTION_ACCEPT_CALL);
        intentAccept.putExtra(CallActivity.EXTRAS_KEY_CALL_UUID, callUuid);

        Intent intentReject = new Intent(context, CallNotificationResultReceiver.class);
        intentReject.setAction(CallActivity.INTENT_ACTION_REJECT_CALL);
        intentReject.putExtra(CallActivity.EXTRAS_KEY_CALL_UUID, callUuid);

        PendingIntent pendingIntentAccept = PendingIntent.getBroadcast(
                context,
                CallActivity.BROADCAST_REQUEST_CODE_NOTIFICATION,
                intentAccept,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        PendingIntent pendingIntentReject = PendingIntent.getBroadcast(
                context,
                CallActivity.BROADCAST_REQUEST_CODE_NOTIFICATION,
                intentReject,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Action actionAccept = new NotificationCompat.Action(
                R.drawable.ic_call_green,
                context.getString(R.string.accept),
                pendingIntentAccept
        );

        NotificationCompat.Action actionReject = new NotificationCompat.Action(
                R.drawable.ic_call_red,
                context.getString(R.string.reject),
                pendingIntentReject
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(context.getString(R.string.incoming_call))
                .setContentText(stringBuilder.toString())
                .addAction(actionReject)
                .addAction(actionAccept)
                .setSmallIcon(R.drawable.ic_baseline_phone_callback_24)
                .setTimeoutAfter(30000)
                .setDefaults(Notification.DEFAULT_ALL)
                .setDeleteIntent(pendingIntentReject);

        NotificationManagerCompat.from(context).notify(INCOMING_CALL_NOTIFICATION_ID, builder.build());
    }
}
