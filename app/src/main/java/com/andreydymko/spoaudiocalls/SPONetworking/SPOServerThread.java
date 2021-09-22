package com.andreydymko.spoaudiocalls.SPONetworking;

import android.os.Handler;
import android.os.Looper;

import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerCallChangedCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerCallInvitationCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerErrorsCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerLoginCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerCallsListCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerUserListCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Exceptions.SPOCouldNotFindServer;
import com.andreydymko.spoaudiocalls.SPONetworking.Exceptions.SPONotConnectedException;
import com.andreydymko.spoaudiocalls.SPONetworking.Users.UserModel;
import com.andreydymko.spoaudiocalls.Utils.NetUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class SPOServerThread extends Thread {

    private final static int SERVER_TCP_PORT = 8356;
    private final static int MAX_TIME_TO_ANSWER = 200;
    private final static int MAX_FAILS_NUM = 5;
    private final static int TIME_TO_RECONNECT = 5000;

    private final InetAddress serverInetAddress;
    private final Handler mainHandler;

    private Socket mainServerSocket;
    private InputStream socketInputStream;
    private OutputStream socketOutputStream;

    private final BlockingQueue<byte[]> msgsToSend;

    private final List<SPOServerErrorsCallback> errorsCallbackList;
    private final List<SPOServerLoginCallback> loginCallbackList;
    private final List<SPOServerCallChangedCallback> callChangedCallbackList;
    private final List<SPOServerCallsListCallback> callsListCallbackList;
    private final List<SPOServerUserListCallback> usersListCallbackList;
    private final List<SPOServerCallInvitationCallback> callInvitationCallbackList;

    private int failsNum = 0;

    public SPOServerThread() throws SPOCouldNotFindServer {
        mainHandler = new Handler(Looper.getMainLooper());
        try {
            serverInetAddress = InetAddress.getByName(SPOServerConnector.SERVER_ADDRESS);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new SPOCouldNotFindServer(e);
        }
        msgsToSend = new LinkedBlockingQueue<>();

        errorsCallbackList = new ArrayList<>();
        callsListCallbackList = new ArrayList<>();
        loginCallbackList = new ArrayList<>();
        callChangedCallbackList = new ArrayList<>();
        usersListCallbackList = new ArrayList<>();
        callInvitationCallbackList = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            mainServerSocket = new Socket(serverInetAddress, SERVER_TCP_PORT);
            mainServerSocket.setSoTimeout(MAX_TIME_TO_ANSWER);
            socketInputStream = mainServerSocket.getInputStream();
            socketOutputStream = mainServerSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!Thread.currentThread().isInterrupted()) {
            if (failsNum > MAX_FAILS_NUM) {
                serverConnectionLost();
                break;
            }
            try {
                drainMsgsQueue();
                resolveRequest();
                failsNum = 0;
            } catch (SocketTimeoutException e) {
                // todo e.printStackTrace();
                continue;
            } catch (BufferUnderflowException | IOException e) {
                failsNum++;
                e.printStackTrace();
            }
        }

        finishProcedures();
    }

    public void setOnSPOServerChangeListener(SPOServerCallback callback) {
        if (callback instanceof SPOServerErrorsCallback) {
            errorsCallbackList.add((SPOServerErrorsCallback) callback);
        }
        if (callback instanceof SPOServerCallsListCallback) {
            callsListCallbackList.add((SPOServerCallsListCallback) callback);
        }
        if (callback instanceof SPOServerCallChangedCallback) {
            callChangedCallbackList.add((SPOServerCallChangedCallback) callback);
        }
        if (callback instanceof SPOServerLoginCallback) {
            loginCallbackList.add((SPOServerLoginCallback) callback);
        }
        if (callback instanceof SPOServerUserListCallback) {
            usersListCallbackList.add((SPOServerUserListCallback) callback);
        }
        if (callback instanceof SPOServerCallInvitationCallback) {
            callInvitationCallbackList.add((SPOServerCallInvitationCallback) callback);
        }
    }

    public void removeOnSPOServerChangeListener(SPOServerCallback callback) {
        if (callback instanceof SPOServerErrorsCallback) {
            errorsCallbackList.remove(callback);
        }
        if (callback instanceof SPOServerCallsListCallback) {
            callsListCallbackList.remove(callback);
        }
        if (callback instanceof SPOServerCallChangedCallback) {
            callChangedCallbackList.remove(callback);
        }
        if (callback instanceof SPOServerLoginCallback) {
            loginCallbackList.remove(callback);
        }
        if (callback instanceof SPOServerUserListCallback) {
            usersListCallbackList.remove(callback);
        }
        if (callback instanceof SPOServerCallInvitationCallback) {
            callInvitationCallbackList.remove(callback);
        }
    }

    private synchronized void resolveRequest() throws IOException {
        checkSocket();
        byte requestCode = readByte();
        switch (requestCode) {
            case IncomingMessageType.ANSWER_ERROR:
                errorOccurred(readByte());
                break;
            case IncomingMessageType.ANSWER_LOGIN_SUCCESS:
                loginSuccess();
                break;
            case IncomingMessageType.REQUEST_CALL_INVITATION:
                toCallInvited();
                break;
            case IncomingMessageType.ANSWER_CALL_CREATED:
                callCreated();
                break;
            case IncomingMessageType.ANSWER_NEW_CALL_STARTED:
                callStarted();
                break;
            case IncomingMessageType.ANSWER_CALL_ENDED:
                callEnded();
                break;
            case IncomingMessageType.ANSWER_UPDATE_USERS_LIST:
                usersListReceived();
                break;
            case IncomingMessageType.ANSWER_NEW_USER_CONNECTED_TO_CALL:
                userConnectedToCall();
                break;
            case IncomingMessageType.ANSWER_NEW_USER_DISCONNECTED_FROM_CALL:
                userDisconnectedFromCall();
                break;
            case IncomingMessageType.ANSWER_NEW_USER_ENTERED_CALL:
                userEnteredCall();
                break;
            case IncomingMessageType.ANSWER_NEW_USER_LEFT_CALL:
                userLeftCall();
                break;
            case IncomingMessageType.ANSWER_UPDATE_USER_CALLS_LIST:
                callsListReceived();
                break;
            case IncomingMessageType.ANSWER_UPDATE_CALL_USERS_LIST:
                usersInsideCallReceived();
                break;
            case IncomingMessageType.REQUEST_PING:
            default:
                sendPing();
                break;
        }
    }

    private void serverConnectionLost() {
        serverConnectionLostCallback();
    }

    private void serverConnectionLostCallback() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerErrorsCallback spoServerErrorsCallback : errorsCallbackList) {
                    spoServerErrorsCallback.onServerConnectionLost();
                }
            }
        });
    }

    private void errorOccurred(byte requestCode) {
        errorCallback(requestCode);
    }

    private void errorCallback(final byte requestCode) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerErrorsCallback errorsCallback : errorsCallbackList) {
                    errorsCallback.onError(new SPOError(ErrorResolver.getErrorText(requestCode)));
                }
            }
        });
    }

    private void loginSuccess() throws IOException {
        loginCallback(readUUID());
    }

    private void loginCallback(final UUID uuid) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerLoginCallback loginCallback : loginCallbackList) {
                    loginCallback.onLoginSuccess(uuid);
                }
            }
        });
    }

    private void sendPing() throws IOException {
        sendPreDeterminedAnswer(OutgoingMessageType.ANSWER_PING);
    }

    private void toCallInvited() throws IOException {
        UUID callUuid = readUUID();
        int numOfUsers = readInt();
        Collection<UUID> usersUuids = readUuids(numOfUsers);
        callInvitationCallback(callUuid, usersUuids);
    }

    private void callInvitationCallback(final UUID callUuid, final Collection<UUID> usersUuids) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerCallInvitationCallback callInvitationCallback : callInvitationCallbackList) {
                    callInvitationCallback.onCallInvitation(callUuid, usersUuids);
                }
            }
        });
    }

    private void callCreated() throws IOException {
        UUID callUuid = readUUID();
        int port = readInt();

        callCreationCallback(callUuid, port);
    }

    private void callCreationCallback(final UUID callUuid, final int port) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerCallInvitationCallback invitationCallback : callInvitationCallbackList) {
                    invitationCallback.onCallCreated(callUuid, port);
                }
            }
        });
    }

    private void callStarted() throws IOException {
        UUID callUuid = readUUID();
        callStartCallback(callUuid);
    }

    private void callStartCallback(final UUID callUuid) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerCallChangedCallback callChangedCallback : callChangedCallbackList) {
                    callChangedCallback.onCallStarted(callUuid);
                }
            }
        });
    }

    private void callEnded() throws IOException {
        UUID callUuid = readUUID();
        callEndedCallback(callUuid);
    }

    private void callEndedCallback(final UUID callUuid) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerCallChangedCallback callChangedCallback : callChangedCallbackList) {
                    callChangedCallback.onCallEnded(callUuid);
                }
            }
        });
    }

    private void usersListReceived() throws IOException {
        int numOfUsers = readInt();
        Collection<UserModel> userModels = readOnlineUsers(numOfUsers);
        usersListReceivedCallback(userModels);
    }

    private void usersListReceivedCallback(final Collection<UserModel> userModels) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerUserListCallback usersListCallback : usersListCallbackList) {
                    usersListCallback.onUsersListReceived(userModels);
                }
            }
        });
    }

    private void userConnectedToCall() throws IOException {
        UUID callUuid = readUUID();
        UUID userUuid = readUUID();
        userConnectedToCallCallback(callUuid, userUuid);
    }

    private void userConnectedToCallCallback(final UUID callUuid, final UUID userUuid) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerCallChangedCallback callChangedCallback : callChangedCallbackList) {
                    callChangedCallback.onUserConnectedToCall(callUuid, userUuid);
                }
            }
        });
    }

    private void userDisconnectedFromCall() throws IOException {
        UUID callUuid = readUUID();
        UUID userUuid = readUUID();
        userDisconnectedFromCallCallback(callUuid, userUuid);
    }

    private void userDisconnectedFromCallCallback(final UUID callUuid, final UUID userUuid) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerCallChangedCallback callChangedCallback : callChangedCallbackList) {
                    callChangedCallback.onUserDisconnectedFromCall(callUuid, userUuid);
                }
            }
        });
    }

    private void userEnteredCall() throws IOException {
        UUID callUuid = readUUID();
        UUID userUuid = readUUID();
        userEnteredCallCallback(callUuid, userUuid);
    }

    private void userEnteredCallCallback(final UUID callUuid, final UUID userUuid) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerCallChangedCallback callChangedCallback : callChangedCallbackList) {
                    callChangedCallback.onUserEnteredCall(callUuid, userUuid);
                }
            }
        });
    }

    private void userLeftCall() throws IOException {
        UUID callUuid = readUUID();
        UUID userUuid = readUUID();
        userLeftCallCallback(callUuid, userUuid);
    }

    private void userLeftCallCallback(final UUID callUuid, final UUID userUuid) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerCallChangedCallback callChangedCallback : callChangedCallbackList) {
                    callChangedCallback.onUserLeftCall(callUuid, userUuid);
                }
            }
        });
    }

    private void callsListReceived() throws IOException {
        int numOfCalls = readInt();
        Collection<UUID> calls = readUuids(numOfCalls);
        callsListReceivedCallback(calls);
    }

    private void callsListReceivedCallback(final Collection<UUID> calls) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerCallsListCallback usersChangedCallback : callsListCallbackList) {
                    usersChangedCallback.onCallsListReceived(calls);
                }
            }
        });
    }

    private void usersInsideCallReceived() throws IOException {
        UUID callUuid = readUUID();
        int userCount = readInt();
        Collection<UserModel> users = readConnectedToCallUsers(userCount);
        usersInsideCallReceivedCallback(callUuid, users);
    }

    private void usersInsideCallReceivedCallback(final UUID callUuid, final Collection<UserModel> users) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SPOServerCallChangedCallback callChangedCallback : callChangedCallbackList) {
                    callChangedCallback.onUsersInsideCallListReceived(callUuid, users);
                }
            }
        });
    }

    private Collection<UserModel> readOnlineUsers(int usersCount) throws IOException {
        Collection<UserModel> userModels = new ArrayList<>(usersCount);
        UUID userUuid;
        boolean isOnline;
        for (int i = 0; i < usersCount; i++) {
            userUuid = readUUID();
            isOnline = readBoolean();
            userModels.add(new UserModel(userUuid, isOnline));
        }
        return userModels;
    }

    private Collection<UserModel> readConnectedToCallUsers(int usersCount) throws IOException {
        Collection<UserModel> connectedUsers = new ArrayList<>(usersCount);
        UUID userUuid;
        boolean isOnline;
        boolean isConnected;
        for (int i = 0; i < usersCount; i++) {
            userUuid = readUUID();
            isOnline = readBoolean();
            isConnected = readBoolean();
            connectedUsers.add(new UserModel(userUuid, isOnline, isConnected));
        }
        return connectedUsers;
    }

    private Collection<UUID> readUuids(int uuidsNum) throws IOException {
        Collection<UUID> uuids = new ArrayList<>(uuidsNum);
        for (int i = 0; i < uuidsNum; i++) {
            uuids.add(readUUID());
        }
        return uuids;
    }

    private UUID readUUID() throws IOException {
        return NetUtils.readUUID(socketInputStream);
    }

    private int readInt() throws IOException {
        return NetUtils.readInt(socketInputStream);
    }

    private byte readByte() throws IOException {
        return NetUtils.readByte(socketInputStream);
    }

    private String readString() throws IOException {
        return NetUtils.readString(socketInputStream, NetUtils.readInt(socketInputStream), SPOServerConnector.STANDARD_CHARSET);
    }

    private boolean readBoolean() throws IOException {
        return NetUtils.readBoolean(socketInputStream);
    }

    public void sendPreDeterminedAnswer(byte answerCode, final byte[]... additionalData) throws IOException {
        checkSocket();
        socketOutputStream.write(answerCode);
        for (byte[] additionalDatum : additionalData) {
            socketOutputStream.write(additionalDatum);
        }
    }

    private void checkSocket() throws SPONotConnectedException {
        if (mainServerSocket == null) {
            throw new SPONotConnectedException("Connect to the server first.");
        }
    }

    public boolean addMsgToQueue(byte[] msg) throws SPONotConnectedException {
        checkSocket();
        try {
            msgsToSend.offer(msg, 500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void drainMsgsQueue() throws IOException {
        if (msgsToSend.isEmpty()) {
            return;
        }
        byte[] bytes;
        try {
            while ((bytes = msgsToSend.poll(500, TimeUnit.MILLISECONDS)) != null) {
                socketOutputStream.write(bytes);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void finishProcedures() {
        try {
            mainServerSocket.close();
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }
}
