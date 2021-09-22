package com.andreydymko.spoaudiocalls.SPONetworking.Callbacks;

import java.util.Collection;
import java.util.UUID;

public interface SPOServerCallsListCallback extends SPOServerErrorsCallback {
    void onCallsListReceived(Collection<UUID> callsUuids);
}
