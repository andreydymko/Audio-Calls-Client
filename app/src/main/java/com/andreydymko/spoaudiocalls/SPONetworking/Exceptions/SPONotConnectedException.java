package com.andreydymko.spoaudiocalls.SPONetworking.Exceptions;

import java.io.IOException;

public class SPONotConnectedException extends SPOException {
    public SPONotConnectedException() { super(); }
    public SPONotConnectedException(String message) { super(message); }
    public SPONotConnectedException(String message, Throwable cause) { super(message, cause); }
    public SPONotConnectedException(Throwable cause) { super(cause); }
}
