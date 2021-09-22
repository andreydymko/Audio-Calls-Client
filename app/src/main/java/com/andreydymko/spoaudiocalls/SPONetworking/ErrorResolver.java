package com.andreydymko.spoaudiocalls.SPONetworking;

class ErrorResolver {
    public static String getErrorText(byte requestCode) {
        switch (requestCode) {
            case OutgoingMessageType.REQUEST_REGISTER:
                return "Could not register new user";
            case OutgoingMessageType.ANSWER_ACCEPT_INCOMING_CALL:
                return "Could not accept incoming call";
            case OutgoingMessageType.ANSWER_PING:
                return "Pinging error";
            case OutgoingMessageType.ANSWER_REJECT_INCOMING_CALL:
                return "Could not reject incoming call";
            case OutgoingMessageType.REQUEST_CREATE_CALL:
                return "Could not create call";
            case OutgoingMessageType.REQUEST_ENTER_CALL:
                return "Could not enter the call";
            case OutgoingMessageType.REQUEST_GET_CALL_USERS_LIST:
                return "Could not get users inside call";
            case OutgoingMessageType.REQUEST_GET_USER_CALLS_LIST:
                return "Could not get list of calls";
            case OutgoingMessageType.REQUEST_GET_USERS_LIST:
                return "Could Not Get List Of Users";
            case OutgoingMessageType.REQUEST_INVITE_USERS:
                return "Could Not Invite Users";
            case OutgoingMessageType.REQUEST_LOGIN:
                return "Login Error";
            case OutgoingMessageType.REQUEST_LOGOUT:
                return "Could Not Logout";
            case OutgoingMessageType.REQUEST_QUIT_CALL:
                return "Could Not Quit Call";
            default:
                return "Unknown Error";
        }
    }
}
