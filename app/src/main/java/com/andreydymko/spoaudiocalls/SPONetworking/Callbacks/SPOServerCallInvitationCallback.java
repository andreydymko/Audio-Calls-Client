package com.andreydymko.spoaudiocalls.SPONetworking.Callbacks;

import java.util.Collection;
import java.util.UUID;

public interface SPOServerCallInvitationCallback extends SPOServerErrorsCallback {
    void onCallInvitation(UUID callUuid, Collection<UUID> usersUuids);
    void onCallCreated(UUID callUuid, int UDPPort);
}
