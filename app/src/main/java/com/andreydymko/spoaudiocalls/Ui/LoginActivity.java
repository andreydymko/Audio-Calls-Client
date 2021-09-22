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
import com.andreydymko.spoaudiocalls.Ui.Compat.NotificationLocalCompat;
import com.andreydymko.spoaudiocalls.Ui.Compat.PreferencesCompat;
import com.andreydymko.spoaudiocalls.Ui.Compat.StandardIntents;
import com.andreydymko.spoaudiocalls.Ui.Compat.UserCredentials;
import com.andreydymko.spoaudiocalls.Ui.Compat.ToastUtils;

import java.util.UUID;

public class LoginActivity extends AppCompatActivity implements SPOServerLoginCallback, View.OnClickListener {

    private Button buttonLogin, buttonRegister;
    private EditText editTextLogin, editTextPass;
    private SPOServerConnector serverConnector;
    private String lastLogin, lastPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        NotificationLocalCompat.createNotificationChannel(this);

        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonLoginRegister);
        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPass = findViewById(R.id.editTextLoginPassword);
        serverConnector = SPOServerConnector.getInstance();

        buttonLogin.setOnClickListener(this);
        buttonRegister.setOnClickListener(this);

        try {
            serverConnector.connectToServer();
        } catch (SPOCouldNotFindServer spoCouldNotFindServer) {
            ToastUtils.toastCantFindServer(this);
            spoCouldNotFindServer.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        try {
            serverConnector.setOnServerMessagesListener(this);
        } catch (SPONotConnectedException e) {
            ToastUtils.toastNotConnected(this);
            e.printStackTrace();
        }

        UserCredentials credentials;
        if ((credentials = PreferencesCompat.getUserCredentials(this)) != null) {
            buttonLogin.setEnabled(false);
            loginUser(credentials);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            serverConnector.removeOnServerMessagesListener(this);
        } catch (SPONotConnectedException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.buttonLogin) {
            if (!LoginPassChecker.checkLoginPasses(this, editTextLogin, editTextPass, null)) {
                return;
            }
            buttonLogin.setEnabled(false);
            loginUser(editTextLogin.getText().toString(), editTextPass.getText().toString());
        } else if (id == R.id.buttonLoginRegister) {
            startActivity(new Intent(this, RegisterActivity.class));
        }
        buttonLogin.setEnabled(true);
    }

    private void loginUser(UserCredentials credentials) {
        loginUser(credentials.getLogin(), credentials.getPassword());
    }

    private void loginUser(String login, String pass) {
        this.lastLogin = login;
        this.lastPass = pass;
        try {
            serverConnector.loginToServer(login, pass);
        } catch (SPONotConnectedException e) {
            buttonLogin.setEnabled(true);
            //ToastUtils.toastNotConnected(this);
            e.printStackTrace();
        }
    }

    @Override
    public void onLoginSuccess(UUID userUuid) {
        buttonLogin.setEnabled(true);

        if (lastLogin != null && lastPass != null) {
            PreferencesCompat.saveUserCredentials(this, new UserCredentials(lastLogin, lastPass));
        }

        StandardIntents.startMainActivity(this, userUuid);
    }

    @Override
    public void onServerConnectionLost() {
        buttonLogin.setEnabled(true);
        ToastUtils.toastConnectionLost(this);
    }

    @Override
    public void onError(final SPOError error) {
        buttonLogin.setEnabled(true);
        ToastUtils.makeToast(this, error.getErrorText());
    }
}