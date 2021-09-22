package com.andreydymko.spoaudiocalls.Ui.Compat;

public class UserCredentials {
    private final String login;
    private final String password;

    public UserCredentials(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
