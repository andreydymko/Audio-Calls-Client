package com.andreydymko.spoaudiocalls.Ui.Compat;

import androidx.annotation.DrawableRes;

import com.andreydymko.spoaudiocalls.R;

public class DrawableCompat {
    public static @DrawableRes
    int getStatusDrawable(boolean isOnline, boolean isConnected) {
        if (isConnected) {
            return R.drawable.ic_call_green;
        }
        return isOnline ? R.drawable.ic_green_circle : R.drawable.ic_gray_circle;
    }
}
