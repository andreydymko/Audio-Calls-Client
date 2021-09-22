package com.andreydymko.spoaudiocalls.Ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.andreydymko.spoaudiocalls.R;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerCallInvitationCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerUserListCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Exceptions.SPONotConnectedException;
import com.andreydymko.spoaudiocalls.SPONetworking.SPOError;
import com.andreydymko.spoaudiocalls.SPONetworking.SPOServerConnector;
import com.andreydymko.spoaudiocalls.SPONetworking.Users.UserModel;
import com.andreydymko.spoaudiocalls.Ui.Compat.NotificationLocalCompat;
import com.andreydymko.spoaudiocalls.Ui.Compat.StandardIntents;
import com.andreydymko.spoaudiocalls.Ui.Compat.ToastUtils;
import com.andreydymko.spoaudiocalls.Ui.UsersListRecyclerView.UsersListAdapter;

import java.util.Collection;
import java.util.UUID;

public class CallCreationActivity extends AppCompatActivity implements
        UsersListAdapter.ItemClickListener,
        SPOServerCallInvitationCallback,
        SPOServerUserListCallback {

    private SPOServerConnector serverConnector;
    private UsersListAdapter usersListAdapter;
    private UUID userUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_creation);

        serverConnector = SPOServerConnector.getInstance();

        RecyclerView usersRecyclerView = findViewById(R.id.recyclerViewCreateCall);
        usersListAdapter = new UsersListAdapter(this);
        usersRecyclerView.setAdapter(usersListAdapter);
        usersRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        usersRecyclerView.setLayoutManager(layoutManager);

        userUuid = (UUID) getIntent().getSerializableExtra(MainActivity.EXTRAS_USER_UUID);
    }

    @Override
    protected void onResume() {
        try {
            serverConnector.setOnServerMessagesListener(this);

            serverConnector.getUsersList();
        } catch (SPONotConnectedException e) {
            ToastUtils.toastNotConnected(this);
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            serverConnector.setOnServerMessagesListener(this);
        } catch (SPONotConnectedException e) {
            ToastUtils.toastNotConnected(this);
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onItemClicked(UserModel userModel) {
        try {
            serverConnector.createCall(userModel.getUserUuid());
        } catch (SPONotConnectedException e) {
            ToastUtils.toastNotConnected(this);
            e.printStackTrace();
        }
    }

    @Override
    public void onUsersListReceived(Collection<UserModel> users) {
        if (userUuid != null) {
            users.remove(new UserModel(userUuid, false, false));
        }
        for (UserModel user : users) {
            usersListAdapter.addUser(user);
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
    public void onServerConnectionLost() {
        ToastUtils.toastConnectionLost(this);
    }

    @Override
    public void onError(SPOError error) {
        //ToastUtils.makeToast(this, error.getErrorText());
    }
}