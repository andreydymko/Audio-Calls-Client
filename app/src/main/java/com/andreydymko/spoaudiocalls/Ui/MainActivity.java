package com.andreydymko.spoaudiocalls.Ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.andreydymko.spoaudiocalls.R;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerCallChangedCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerCallInvitationCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerCallsListCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Exceptions.SPONotConnectedException;
import com.andreydymko.spoaudiocalls.SPONetworking.SPOError;
import com.andreydymko.spoaudiocalls.SPONetworking.SPOServerConnector;
import com.andreydymko.spoaudiocalls.SPONetworking.Users.UserModel;
import com.andreydymko.spoaudiocalls.Ui.Compat.NotificationLocalCompat;
import com.andreydymko.spoaudiocalls.Ui.Compat.PreferencesCompat;
import com.andreydymko.spoaudiocalls.Ui.Compat.StandardIntents;
import com.andreydymko.spoaudiocalls.Ui.Compat.ToastUtils;
import com.andreydymko.spoaudiocalls.Ui.ExpandableCallsListView.ExpandableCallsListAdapter;
import com.andreydymko.spoaudiocalls.Ui.ExpandableCallsListView.OnUserClickJoinCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collection;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        SPOServerCallsListCallback,
        SPOServerCallChangedCallback,
        OnUserClickJoinCallback,
        SPOServerCallInvitationCallback {

    public final static String EXTRAS_USER_UUID = "SPO_AUDIO_CALLS_EXTRAS_USER_UUID";

    private Menu toolbarMenu;
    private SPOServerConnector serverConnector;
    private ExpandableListView expandableCallsListView;
    private ExpandableCallsListAdapter callsListAdapter;
    private FloatingActionButton fabCreateCall;
    private UUID userUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.mainActivityToolbar));
        setUserUUID();

        expandableCallsListView = findViewById(R.id.expandableCallsListView);
        callsListAdapter = new ExpandableCallsListAdapter(this);
        expandableCallsListView.setDividerHeight(1);
        expandableCallsListView.setAdapter(callsListAdapter);

        callsListAdapter.setOnJoinCallback(this);

        fabCreateCall = findViewById(R.id.fabCreateCall);
        fabCreateCall.setOnClickListener(this);

        serverConnector = SPOServerConnector.getInstance();
    }

    private void setUserUUID() {
        Intent intent = getIntent();
        if ((userUuid = (UUID) intent.getSerializableExtra(EXTRAS_USER_UUID)) != null) {
            try {
                getSupportActionBar().setTitle(userUuid.toString());
            } catch (NullPointerException ignore) {
                
            }
        }
    }

    @Override
    protected void onResume() {
        try {
            serverConnector.setOnServerMessagesListener(this);

            serverConnector.getCallsList();
        } catch (SPONotConnectedException e) {
            ToastUtils.toastNotConnected(this);
            e.printStackTrace();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        this.toolbarMenu = menu;
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.main_toolbar_logout) {
            logout();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.fabCreateCall) {
            StandardIntents.startCallCreationActivity(this, this.userUuid);
        }
    }

    private void logout() {
        try {
            serverConnector.disconnectFromServer();
        } catch (SPONotConnectedException e) {
            e.printStackTrace();
        }
        PreferencesCompat.deleteUserCredentials(this);
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public void onUserClickJoin(UUID callUuid) {
        try {
            serverConnector.enterCall(callUuid);
        } catch (SPONotConnectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCallInvitation(UUID callUuid, Collection<UUID> usersUuids) {
        NotificationLocalCompat.makeIncomingCallNotification(this, callUuid, usersUuids);
    }

    @Override
    public void onCallCreated(UUID callUuid, int UDPPort) {
        StandardIntents.startCallActivity(this, callUuid, UDPPort);
    }

    @Override
    public void onCallStarted(UUID callUuid) {
        callsListAdapter.addCall(callUuid);
        try {
            serverConnector.getUsersInsideCall(callUuid);
        } catch (SPONotConnectedException e) {
            ToastUtils.toastNotConnected(this);
            e.printStackTrace();
        }
    }

    @Override
    public void onCallEnded(UUID callUuid) {
        callsListAdapter.removeCall(callUuid);
    }

    @Override
    public void onUserEnteredCall(UUID callUuid, UUID userUuid) {
        callsListAdapter.addUser(callUuid, new UserModel(userUuid, true, false));
    }

    @Override
    public void onUserLeftCall(UUID callUuid, UUID userUuid) {
        callsListAdapter.removeUser(callUuid, new UserModel(userUuid, true, false));
    }

    @Override
    public void onUserConnectedToCall(UUID callUuid, UUID userUuid) {
        callsListAdapter.updateUser(callUuid, new UserModel(userUuid, true, true));
    }

    @Override
    public void onUserDisconnectedFromCall(UUID callUuid, UUID userUuid) {
        callsListAdapter.updateUser(callUuid, new UserModel(userUuid, true, false));
    }

    @Override
    public void onUsersInsideCallListReceived(UUID callUuid, Collection<UserModel> userModels) {
        callsListAdapter.addUsers(callUuid, userModels);
    }

    @Override
    public void onCallsListReceived(Collection<UUID> callsUuids) {
        callsListAdapter.clear();
        callsListAdapter.addCalls(callsUuids);
        for (UUID callUuid : callsUuids) {
            try {
                serverConnector.getUsersInsideCall(callUuid);
            } catch (SPONotConnectedException e) {
                ToastUtils.toastNotConnected(this);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onServerConnectionLost() {
        ToastUtils.toastConnectionLost(this);
    }

    @Override
    public void onError(SPOError error) {
        //ToastUtils.makeToast(this, error.getErrorText());
    }
}