package com.andreydymko.spoaudiocalls.SPONetworking;

public class SPOError {
    private final String errorText;

    public SPOError(String errorText) {
        this.errorText = errorText;
    }

    public String getErrorText() {
        return errorText;
    }
}
