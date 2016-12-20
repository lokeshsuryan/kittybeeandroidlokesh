package com.kittyapplication.sync;

public interface ServerAuthenticatorInterface {
    public String userSignIn(final String user, final String pass,
                             String authType) throws Exception;

    public String userSignUp(final String name, final String email, final String pass, String authType) throws Exception;
}
