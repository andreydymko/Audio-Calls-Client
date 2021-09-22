package com.andreydymko.spoaudiocalls.SPONetworking.Exceptions;

public class SPOCouldNotFindServer extends SPOException {
    public SPOCouldNotFindServer() { super(); }
    public SPOCouldNotFindServer(String message) { super(message); }
    public SPOCouldNotFindServer(String message, Throwable cause) { super(message, cause); }
    public SPOCouldNotFindServer(Throwable cause) { super(cause); }
}
