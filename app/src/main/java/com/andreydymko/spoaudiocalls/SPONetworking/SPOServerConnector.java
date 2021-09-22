package com.andreydymko.spoaudiocalls.SPONetworking;

import com.andreydymko.spoaudiocalls.SPONetworking.Callbacks.SPOServerCallback;
import com.andreydymko.spoaudiocalls.SPONetworking.Exceptions.SPOCouldNotFindServer;
import com.andreydymko.spoaudiocalls.SPONetworking.Exceptions.SPONotConnectedException;
import com.andreydymko.spoaudiocalls.Utils.ByteUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;

public class SPOServerConnector {
    public final static String SERVER_ADDRESS = "192.168.1.100";

    private SPOServerConnector(){}
    private static SPOServerConnector instance;

    public static SPOServerConnector getInstance() {
        if (instance == null) {
            synchronized (SPOServerConnector.class) {
                if (instance == null) {
                    instance = new SPOServerConnector();
                }
            }
        }
        return instance;
    }

    public final static Charset STANDARD_CHARSET = StandardCharsets.UTF_16LE;
    private SPOServerThread mainServerThread;

    public void connectToServer() throws SPOCouldNotFindServer {
        if (mainServerThread != null) {
            return;
        }
        this.mainServerThread = new SPOServerThread();
        this.mainServerThread.start();
    }

    public void disconnectFromServer() throws SPONotConnectedException {
        checkThread();
        logoutFromServer();
        mainServerThread.interrupt();
        mainServerThread = null;
    }

    public void setOnServerMessagesListener(SPOServerCallback callback) throws SPONotConnectedException {
        checkThread();
        mainServerThread.setOnSPOServerChangeListener(callback);
    }

    public void removeOnServerMessagesListener(SPOServerCallback callback) throws SPONotConnectedException {
        checkThread();
        mainServerThread.removeOnSPOServerChangeListener(callback);
    }

    public void registerNewUser(String login, String password) throws SPONotConnectedException {
        checkThread();
        byte[] loginBytes = login.getBytes(STANDARD_CHARSET);
        byte[] passBytes = password.getBytes(STANDARD_CHARSET);
        mainServerThread.addMsgToQueue(
                ByteUtils.concatAll(OutgoingMessageType.REQUEST_REGISTER,
                        ByteUtils.getIntBytes(loginBytes.length),
                        loginBytes,
                        ByteUtils.getIntBytes(passBytes.length),
                        passBytes));
    }

    public void loginToServer(String login, String password) throws SPONotConnectedException {
        checkThread();
        byte[] loginBytes = login.getBytes(STANDARD_CHARSET);
        byte[] passBytes = password.getBytes(STANDARD_CHARSET);
        mainServerThread.addMsgToQueue(
                ByteUtils.concatAll(OutgoingMessageType.REQUEST_LOGIN,
                        ByteUtils.getIntBytes(loginBytes.length),
                        loginBytes,
                        ByteUtils.getIntBytes(passBytes.length),
                        passBytes));
    }

    public void logoutFromServer() throws SPONotConnectedException {
        checkThread();
        mainServerThread.addMsgToQueue(
                ByteUtils.concatAll(OutgoingMessageType.REQUEST_LOGOUT));
    }

    public void createCall(Collection<UUID> usersUuids) throws SPONotConnectedException {
        checkThread();
        mainServerThread.addMsgToQueue(
                ByteUtils.concatAll(
                        OutgoingMessageType.REQUEST_CREATE_CALL,
                        ByteUtils.getIntBytes(usersUuids.size()),
                        ByteUtils.getUUIDsBytes(usersUuids)));
    }

    public void createCall(UUID userUuid) throws SPONotConnectedException {
        checkThread();
        mainServerThread.addMsgToQueue(
                ByteUtils.concatAll(OutgoingMessageType.REQUEST_CREATE_CALL,
                        ByteUtils.getIntBytes(1),
                        ByteUtils.getUUIDBytes(userUuid)));
    }

    public void enterCall(UUID callUuid) throws SPONotConnectedException {
        checkThread();
        mainServerThread.addMsgToQueue(
                ByteUtils.concatAll(OutgoingMessageType.REQUEST_ENTER_CALL,
                        ByteUtils.getUUIDBytes(callUuid)));
    }

    public void quitCall(UUID callUuid) throws SPONotConnectedException {
        checkThread();
        mainServerThread.addMsgToQueue(
                ByteUtils.concatAll(OutgoingMessageType.REQUEST_QUIT_CALL,
                        ByteUtils.getUUIDBytes(callUuid)));
    }

    public void acceptCall(UUID callUuid) throws SPONotConnectedException {
        checkThread();
        mainServerThread.addMsgToQueue(
                ByteUtils.concatAll(OutgoingMessageType.ANSWER_ACCEPT_INCOMING_CALL,
                        ByteUtils.getUUIDBytes(callUuid)));
    }

    public void rejectCall(UUID callUuid) throws SPONotConnectedException {
        checkThread();
        mainServerThread.addMsgToQueue(
                ByteUtils.concatAll(OutgoingMessageType.ANSWER_REJECT_INCOMING_CALL,
                        ByteUtils.getUUIDBytes(callUuid)));
    }

    public void inviteUsersToCall(UUID callUuid, Collection<UUID> usersUuids) throws SPONotConnectedException {
        checkThread();
        mainServerThread.addMsgToQueue(
                ByteUtils.concatAll(OutgoingMessageType.REQUEST_INVITE_USERS,
                        ByteUtils.getUUIDBytes(callUuid),
                        ByteUtils.getIntBytes(usersUuids.size()),
                        ByteUtils.getUUIDsBytes(usersUuids)));
    }

    public void getUsersList() throws SPONotConnectedException {
        checkThread();
        mainServerThread.addMsgToQueue(
                ByteUtils.concatAll(OutgoingMessageType.REQUEST_GET_USERS_LIST));
    }

    public void getCallsList() throws SPONotConnectedException {
        checkThread();
        mainServerThread.addMsgToQueue(
                ByteUtils.concatAll(OutgoingMessageType.REQUEST_GET_USER_CALLS_LIST));
    }

    public void getUsersInsideCall(UUID callUuid) throws SPONotConnectedException {
        checkThread();
        mainServerThread.addMsgToQueue(
                ByteUtils.concatAll(OutgoingMessageType.REQUEST_GET_CALL_USERS_LIST,
                        ByteUtils.getUUIDBytes(callUuid)));
    }

    private void checkThread() throws SPONotConnectedException {
        if (mainServerThread == null) {
            try {
                connectToServer();
            } catch (SPOCouldNotFindServer spoCouldNotFindServer) {
                spoCouldNotFindServer.printStackTrace();
            }
            throw new SPONotConnectedException("Connect to the server first");
        }
    }
}
