package com.andreydymko.spoaudiocalls.SPONetworking.Callbacks;

import java.util.UUID;

public interface SPOServerLoginCallback extends SPOServerErrorsCallback {
    void onLoginSuccess(UUID userUuid);
}
