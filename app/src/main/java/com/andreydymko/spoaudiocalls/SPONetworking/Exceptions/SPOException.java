package com.andreydymko.spoaudiocalls.SPONetworking.Exceptions;

import java.io.IOException;

public class SPOException extends IOException {
    public SPOException() { super(); }
    public SPOException(String message) { super(message); }
    public SPOException(String message, Throwable cause) { super(message, cause); }
    public SPOException(Throwable cause) { super(cause); }
}
