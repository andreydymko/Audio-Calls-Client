package com.andreydymko.spoaudiocalls.Ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.andreydymko.spoaudiocalls.R;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerCallChangedCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerCallInvitationCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Exceptions.SPOCouldNotFindServer;
import com.andreydymko.spoaudiocalls.SPONetworking.Exceptions.SPONotConnectedException;
import com.andreydymko.spoaudiocalls.SPONetworking.SPOError;
import com.andreydymko.spoaudiocalls.SPONetworking.SPOServerConnector;
import com.andreydymko.spoaudiocalls.SPONetworking.Sound.SoundReceivingThread;
import com.andreydymko.spoaudiocalls.SPONetworking.Sound.SoundSendingThread;
import com.andreydymko.spoaudiocalls.SPONetworking.Users.UserModel;
import com.andreydymko.spoaudiocalls.Ui.Compat.NotificationLocalCompat;
import com.andreydymko.spoaudiocalls.Ui.Compat.StandardIntents;
import com.andreydymko.spoaudiocalls.Ui.Compat.ToastUtils;
import com.andreydymko.spoaudiocalls.Ui.UsersListRecyclerView.UsersListAdapter;
import com.andreydymko.spoaudiocalls.Ui.UsersListRecyclerView.WrapContentLinearLayoutManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class CallActivity extends AppCompatActivity implements
        View.OnClickListener,
        SPOServerCallInvitationCallback,
        SPOServerCallChangedCallback {
    public static final String TAG = CallActivity.class.getSimpleName();

    public static final String EXTRAS_KEY_UDP_PORT = "SPO_AUDIO_CALLS_EXTRAS_UDP_PORT_CALL_ACTIVITY";
    public static final String EXTRAS_KEY_CALL_UUID = "SPO_AUDIO_CALLS_EXTRAS_CALL_UUID_CALL_ACTIVITY";
    public static final int BROADCAST_REQUEST_CODE_NOTIFICATION = 45647;
    public static final String INTENT_ACTION_ACCEPT_CALL = "SPO_AUDIO_CALLS_ACTION_ACCEPT_CALL";
    public static final String INTENT_ACTION_REJECT_CALL = "SPO_AUDIO_CALLS_ACTION_REJECT_CALL";

    private static final int PERMISSION_REQUEST_CODE = 123;

    private UUID callUuid;
    private int UDPPort;
    private SPOServerConnector serverConnector;
    private UsersListAdapter usersListAdapter;
    private DatagramSocket datagramSocket;
    private SoundReceivingThread soundReceivingThread;
    private SoundSendingThread soundSendingThread;

    private FloatingActionButton fabQuitCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_activivty);

        callUuid = (UUID) getIntent().getSerializableExtra(EXTRAS_KEY_CALL_UUID);
        UDPPort = getIntent().getIntExtra(EXTRAS_KEY_UDP_PORT, 0);
        if (UDPPort <= 0 || callUuid == null) {
            ToastUtils.toastConnectionLost(this);
            finish();
        }

        if (getPackageManager().checkPermission(Manifest.permission.RECORD_AUDIO, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.RECORD_AUDIO
                    },
                    PERMISSION_REQUEST_CODE);
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbarCallActivity));
        try {
            Objects.requireNonNull(getSupportActionBar()).setTitle(callUuid.toString());
        } catch (NullPointerException ignore) {}

        serverConnector = SPOServerConnector.getInstance();

        RecyclerView usersRecyclerView = findViewById(R.id.recyclerViewCallUsers);
        usersListAdapter = new UsersListAdapter();
        usersRecyclerView.setAdapter(usersListAdapter);
        usersRecyclerView.setHasFixedSize(true);

        WrapContentLinearLayoutManager layoutManager = new WrapContentLinearLayoutManager(this);
        usersRecyclerView.setLayoutManager(layoutManager);

        fabQuitCall = findViewById(R.id.fabQuitCall);
        fabQuitCall.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "on start");
        super.onStart();

        startCall();
    }

    private void startCall() {
        if (UDPPort == 0) {
            finish();
            return;
        }
        try {
            datagramSocket = new DatagramSocket(UDPPort);
        } catch (SocketException e) {
            e.printStackTrace();
            finish();
            return;
        }

        try {
            soundSendingThread = new SoundSendingThread(datagramSocket);
            soundReceivingThread = new SoundReceivingThread(datagramSocket);
        } catch (SPOCouldNotFindServer spoCouldNotFindServer) {
            ToastUtils.toastCantFindServer(this);
            spoCouldNotFindServer.printStackTrace();
            finish();
            return;
        }

        soundSendingThread.start();
        soundReceivingThread.start();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "on resume");
        try {
            serverConnector.setOnServerMessagesListener(this);

            serverConnector.getUsersInsideCall(this.callUuid);
        } catch (SPONotConnectedException e) {
            ToastUtils.toastNotConnected(this);
            e.printStackTrace();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "on pause");
        try {
            serverConnector.removeOnServerMessagesListener(this);
        } catch (SPONotConnectedException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "on destroy");
        finishCall();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fabQuitCall) {
            finish();
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
        // todo ignore?
    }

    @Override
    public void onCallEnded(UUID callUuid) {
        if (callUuid.equals(this.callUuid)) {
            finish();
        }
    }

    @Override
    public void onUserConnectedToCall(UUID callUuid, UUID userUuid) {
        Log.d(TAG, "user connected to call");
        if (callUuid.equals(this.callUuid)) {
            usersListAdapter.addUser(new UserModel(userUuid, true, true));
        }
    }

    @Override
    public void onUserDisconnectedFromCall(UUID callUuid, UUID userUuid) {
        Log.d(TAG, "user disconnected from call");
        if (callUuid.equals(this.callUuid)) {
            usersListAdapter.addUser(new UserModel(userUuid, true, false));
        }
    }

    @Override
    public void onUserEnteredCall(UUID callUuid, UUID userUuid) {
        Log.d(TAG, "User entered call");
        if (callUuid.equals(this.callUuid)) {
            usersListAdapter.addUser(new UserModel(userUuid, true, false));
        }
    }

    @Override
    public void onUserLeftCall(UUID callUuid, UUID userUuid) {
        Log.d(TAG, "user left call");
        if (callUuid.equals(this.callUuid))  {
            usersListAdapter.removeUser(new UserModel(userUuid, true, false));
        }
    }

    @Override
    public void onUsersInsideCallListReceived(UUID callUuid, Collection<UserModel> userModels) {
        Log.d(TAG, "users list received");
        if (callUuid.equals(this.callUuid)) {
            usersListAdapter.clear();
            for (UserModel userModel : userModels) {
                usersListAdapter.addUser(userModel);
            }
        }
    }

    @Override
    public void onServerConnectionLost() {
        ToastUtils.toastConnectionLost(this);
        finish();
    }

    @Override
    public void onError(SPOError error) {
        //ToastUtils.makeToast(this, error.getErrorText());
    }

    private void finishCall() {
        try {
            serverConnector.quitCall(this.callUuid);
        } catch (SPONotConnectedException e) {
            e.printStackTrace();
        }
        if (datagramSocket != null) {
            datagramSocket.close();
        }
        if (soundSendingThread != null) {
            soundSendingThread.interrupt();
        }
        if (soundReceivingThread != null) {
            soundReceivingThread.interrupt();
        }
    }
}