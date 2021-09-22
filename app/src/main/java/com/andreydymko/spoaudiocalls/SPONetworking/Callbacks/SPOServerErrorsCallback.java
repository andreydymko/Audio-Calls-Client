package com.andreydymko.spoaudiocalls.SPONetworking.Callbacks;

import com.andreydymko.spoaudiocalls.SPONetworking.SPOError;

public interface SPOServerErrorsCallback extends SPOServerCallback {
    void onServerConnectionLost();
    void onError(final SPOError error);
}
