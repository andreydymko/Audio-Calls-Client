package com.andreydymko.spoaudiocalls.Ui.Compat;

import android.content.Context;
import android.widget.Toast;

import com.andreydymko.spoaudiocalls.R;

public class ToastUtils {
    public static void makeToast(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_LONG).show();
    }

    public static void toastConnectionLost(Context context) {
        Toast.makeText(context, R.string.lost_connection_to_the_server, Toast.LENGTH_LONG).show();
    }

    public static void toastCantFindServer(Context context) {
        Toast.makeText(context, R.string.could_not_find_server, Toast.LENGTH_LONG).show();
    }

    public static void toastNotConnected(Context context) {
        Toast.makeText(context, R.string.not_connected_to_server, Toast.LENGTH_LONG).show();
    }

    public static void toastNotLoggedIn(Context context) {
        Toast.makeText(context, R.string.you_are_not_logged_in, Toast.LENGTH_LONG).show();
    }
}
