package com.andreydymko.spoaudiocalls.SPONetworking.Callbacks;

import com.andreydymko.spoaudiocalls.SPONetworking.Users.UserModel;

import java.util.Collection;
import java.util.UUID;

public interface SPOServerCallChangedCallback extends SPOServerErrorsCallback {
    void onCallStarted(UUID callUuid);
    void onCallEnded(UUID callUuid);
    void onUserConnectedToCall(UUID callUuid, UUID userUuid);
    void onUserDisconnectedFromCall(UUID callUuid, UUID userUuid);
    void onUserEnteredCall(UUID callUuid, UUID userUuid);
    void onUserLeftCall(UUID callUuid, UUID userUuid);
    void onUsersInsideCallListReceived(UUID callUuid, Collection<UserModel> userModels);
}
