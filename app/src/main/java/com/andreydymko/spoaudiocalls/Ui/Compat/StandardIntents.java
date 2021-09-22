package com.andreydymko.spoaudiocalls.Ui.Compat;

import android.content.Context;
import android.content.Intent;

import com.andreydymko.spoaudiocalls.Ui.CallActivity;
import com.andreydymko.spoaudiocalls.Ui.CallCreationActivity;
import com.andreydymko.spoaudiocalls.Ui.MainActivity;

import java.util.UUID;

public class StandardIntents {
    public static void startCallActivity(Context context, UUID callUuid, int UDPPort) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(CallActivity.EXTRAS_KEY_UDP_PORT, UDPPort);
        intent.putExtra(CallActivity.EXTRAS_KEY_CALL_UUID, callUuid);

        context.startActivity(intent);
    }

    public static void startMainActivity(Context context, UUID userUuid) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MainActivity.EXTRAS_USER_UUID, userUuid);
        context.startActivity(intent);
    }

    public static void startCallCreationActivity(Context context, UUID userUuid) {
        Intent intent = new Intent(context, CallCreationActivity.class);
        intent.putExtra(MainActivity.EXTRAS_USER_UUID, userUuid);
        context.startActivity(intent);
    }
}
