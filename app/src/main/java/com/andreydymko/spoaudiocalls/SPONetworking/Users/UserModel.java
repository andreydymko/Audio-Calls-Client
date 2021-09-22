package com.andreydymko.spoaudiocalls.SPONetworking.Users;

import java.util.UUID;

public class UserModel {
    protected UUID userUuid;
    protected boolean isOnline;
    protected boolean isConnected;

    public UserModel(UUID userUuid, boolean isOnline) {
        this.userUuid = userUuid;
        this.isOnline = isOnline;
        this.isConnected = false;
    }

    public UserModel(UUID userUuid, boolean isOnline, boolean isConnected) {
        this.userUuid = userUuid;
        this.isOnline = isOnline;
        this.isConnected = isConnected;
    }

    public UUID getUserUuid() {
        return userUuid;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public String toString() {
        return userUuid + "; ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserModel userModel = (UserModel) o;

        return userUuid != null ? userUuid.equals(userModel.userUuid) : userModel.userUuid == null;
    }

    @Override
    public int hashCode() {
        return userUuid != null ? userUuid.hashCode() : 0;
    }
}
