package com.andreydymko.spoaudiocalls.Ui.Compat;

import android.content.Context;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.andreydymko.spoaudiocalls.R;

public class LoginPassChecker {
    private final static int MIN_LOGIN_LENGTH = 8;
    private final static int MIN_PASS_LENGTH = 8;

    public static boolean checkLoginPasses(Context context,
                                           EditText editTextLogin,
                                           EditText editTextPass1,
                                           @Nullable EditText editTextPass2) {
        String pass1trim = editTextPass1.getText().toString().trim(),
                loginTrim = editTextLogin.getText().toString().trim(),
                pass2trim = null;

        if (editTextPass2 != null) {
            pass2trim = editTextPass2.getText().toString().trim();
        }

        if (loginTrim.isEmpty()) {
            editTextLogin.setError(context.getString(R.string.this_field_cannot_be_blank));
            return false;
        }

        if (loginTrim.length() < MIN_LOGIN_LENGTH) {
            editTextLogin.setError(context.getString(R.string.login_should_be, MIN_LOGIN_LENGTH));
        }

        if (pass1trim.isEmpty()) {
            editTextPass1.setError(context.getString(R.string.this_field_cannot_be_blank));
            return false;
        }


        if (editTextPass2 != null && pass2trim.isEmpty()) {
            editTextPass2.setError(context.getString(R.string.this_field_cannot_be_blank));
            return false;
        }


        if (pass1trim.length() < MIN_PASS_LENGTH) {
            editTextPass1.setError(context.getString(R.string.password_should_be, MIN_PASS_LENGTH));
            return false;
        }

        if (editTextPass2 != null && !pass1trim.contentEquals(pass2trim)) {
            editTextPass1.setError(context.getString(R.string.passwords_should_match));
            editTextPass2.setError(context.getString(R.string.passwords_should_match));
            return false;
        }
        return true;
    }
}
