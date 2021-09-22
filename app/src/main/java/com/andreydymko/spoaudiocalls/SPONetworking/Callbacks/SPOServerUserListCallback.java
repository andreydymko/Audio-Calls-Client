package com.andreydymko.spoaudiocalls.SPONetworking.Callbacks;

import com.andreydymko.spoaudiocalls.SPONetworking.Users.UserModel;

import java.util.Collection;

public interface SPOServerUserListCallback extends SPOServerErrorsCallback {
    void onUsersListReceived(Collection<UserModel> users);
}
