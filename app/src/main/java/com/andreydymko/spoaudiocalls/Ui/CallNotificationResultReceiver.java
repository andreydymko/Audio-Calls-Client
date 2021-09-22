package com.andreydymko.spoaudiocalls.Ui;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.andreydymko.spoaudiocalls.SPONetworking.Exceptions.SPONotConnectedException;
import com.andreydymko.spoaudiocalls.SPONetworking.SPOServerConnector;
import com.andreydymko.spoaudiocalls.Ui.Compat.NotificationLocalCompat;
import com.andreydymko.spoaudiocalls.Ui.Compat.StandardIntents;
import com.andreydymko.spoaudiocalls.Ui.Compat.ToastUtils;

import java.util.UUID;

import static android.content.Context.NOTIFICATION_SERVICE;

public class CallNotificationResultReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        switch (intent.getAction()) {
            case CallActivity.INTENT_ACTION_ACCEPT_CALL:
                UUID callUuid = (UUID) intent.getSerializableExtra(CallActivity.EXTRAS_KEY_CALL_UUID);
                try {
                    SPOServerConnector.getInstance().acceptCall(callUuid);
                } catch (SPONotConnectedException e) {
                    ToastUtils.toastNotConnected(context);
                    e.printStackTrace();
                }
                notificationManager.cancel(NotificationLocalCompat.INCOMING_CALL_NOTIFICATION_ID);
                break;
            case CallActivity.INTENT_ACTION_REJECT_CALL:
                try {
                    SPOServerConnector.getInstance().rejectCall((UUID) intent.getSerializableExtra(CallActivity.EXTRAS_KEY_CALL_UUID));
                } catch (SPONotConnectedException e) {
                    ToastUtils.toastNotConnected(context);
                    e.printStackTrace();
                }
                notificationManager.cancel(NotificationLocalCompat.INCOMING_CALL_NOTIFICATION_ID);
                break;
            default:
                break;
        }
    }
}
