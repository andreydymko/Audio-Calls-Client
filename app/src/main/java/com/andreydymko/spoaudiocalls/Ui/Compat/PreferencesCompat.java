package com.andreydymko.spoaudiocalls.Ui.Compat;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesCompat {
    private final static String USER_LOGIN_PREF_KEY = "SPO_USER_CREDENTIALS_LOGIN_PREFERENCE_KEY";
    private final static String USER_PASS_PREF_KEY = "SPO_USER_CREDENTIALS_PASSWORD_PREFERENCE_KEY";

    public static void saveUserCredentials(Context context, UserCredentials userCredentials) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(USER_LOGIN_PREF_KEY, userCredentials.getLogin())
                .putString(USER_PASS_PREF_KEY, userCredentials.getPassword())
                .apply();
    }

    public static UserCredentials getUserCredentials(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!preferences.contains(USER_LOGIN_PREF_KEY) || !preferences.contains(USER_PASS_PREF_KEY)) {
            return null;
        }
        return new UserCredentials(preferences.getString(USER_LOGIN_PREF_KEY, ""),
                preferences.getString(USER_PASS_PREF_KEY, ""));
    }

    public static void deleteUserCredentials(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(USER_LOGIN_PREF_KEY)
                .remove(USER_PASS_PREF_KEY)
                .apply();
    }
}
