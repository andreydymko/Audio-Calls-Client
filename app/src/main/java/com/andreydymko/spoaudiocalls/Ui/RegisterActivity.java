package com.andreydymko.spoaudiocalls.Ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.andreydymko.spoaudiocalls.R;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerLoginCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Exceptions.SPOCouldNotFindServer;
import com.andreydymko.spoaudiocalls.SPONetworking.Exceptions.SPONotConnectedException;
import com.andreydymko.spoaudiocalls.SPONetworking.SPOError;
import com.andreydymko.spoaudiocalls.SPONetworking.SPOServerConnector;
import com.andreydymko.spoaudiocalls.Ui.Compat.LoginPassChecker;
import com.andreydymko.spoaudiocalls.Ui.Compat.PreferencesCompat;
import com.andreydymko.spoaudiocalls.Ui.Compat.UserCredentials;
import com.andreydymko.spoaudiocalls.Ui.Compat.ToastUtils;

import java.util.UUID;

public class RegisterActivity extends AppCompatActivity implements SPOServerLoginCallback, View.OnClickListener {



    private Button buttonRegister;
    private EditText editTextLogin, editTextPass1, editTextPass2;
    private SPOServerConnector serverConnector;

    private String lastLogin, lastPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        buttonRegister = findViewById(R.id.buttonRegister);
        editTextLogin = findViewById(R.id.editTextRegisterLogin);
        editTextPass1 = findViewById(R.id.editTextRegisterPassword1);
        editTextPass2 = findViewById(R.id.editTextRegisterPassword2);

        buttonRegister.setOnClickListener(this);
        serverConnector = SPOServerConnector.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            serverConnector.setOnServerMessagesListener(this);
        } catch (SPONotConnectedException e) {
            ToastUtils.toastNotConnected(this);
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            serverConnector.removeOnServerMessagesListener(this);
        } catch (SPONotConnectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonRegister) {
            if (!LoginPassChecker.checkLoginPasses(this, editTextLogin, editTextPass1, editTextPass2)) {
                return;
            }
            buttonRegister.setEnabled(false);
            registerUser(editTextLogin.getText().toString(),
                    editTextPass1.getText().toString());
        }
    }

    @Override
    public void onBackPressed() {
        startLoginActivity();
    }

    private void registerUser(String login, String pass) {
        this.lastLogin = login;
        this.lastPass = pass;

        try {
            serverConnector.registerNewUser(login, pass);
        } catch (SPONotConnectedException e) {
            ToastUtils.toastNotConnected(this);
            e.printStackTrace();
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onLoginSuccess(UUID userUuid) {
        buttonRegister.setEnabled(true);

        if (lastLogin != null || lastPass != null) {
            PreferencesCompat.saveUserCredentials(this, new UserCredentials(lastLogin, lastPass));
        }
        startLoginActivity();
    }

    @Override
    public void onServerConnectionLost() {
        buttonRegister.setEnabled(true);
        ToastUtils.toastConnectionLost(this);
    }

    @Override
    public void onError(SPOError error) {
        buttonRegister.setEnabled(true);
        ToastUtils.makeToast(this, error.getErrorText());
    }
}