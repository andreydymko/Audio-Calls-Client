package com.andreydymko.spoaudiocalls.Ui.ExpandableCallsListView;

import com.andreydymko.spoaudiocalls.SPONetworking.Users.UserModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CallViewModel {
    private final List<UserModel> usersList;
    private final UUID callId;

    public CallViewModel(UUID callId) {
        this.usersList = new ArrayList<>();
        this.callId = callId;
    }

    public List<UserModel> getUsersList() {
        return usersList;
    }

    public UUID getCallId() {
        return callId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallViewModel that = (CallViewModel) o;

        return callId != null ? callId.equals(that.callId) : that.callId == null;
    }

    @Override
    public int hashCode() {
        return callId != null ? callId.hashCode() : 0;
    }
}
