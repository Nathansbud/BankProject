package com.nathansbud;

public class User {
    private String username;
    private String pwd;
    private String uid; //Int - 7

    private double funds;


    private static final int PASSWORD_MINIMUM = 3;
    private static final int PASSWORD_MAXIMUM = 50;

    private static final int USERNAME_MINIMUM = 3;
    private static final int USERNAME_MAXIMUM = 20;

    private static final int UID_LENGTH = 7;

    private static final String FAIL_CONDITION = "[\\n\\s]";

    public User() {}

    public User(String _username, String _pwd, double _funds) {
        username = _username;
        pwd = _pwd;
        uid = generateUID();

        funds = _funds;
    }

    public User(String _username, String _pwd, String _uid, double _funds) {
        username = _username;
        pwd = _pwd;
        uid = _uid;
        funds = _funds;
    }


    public String getUsername() {
        return username;
    }
    public void setUsername(String _username) {
        username = _username;
    }

    public String getPwd() {
        return pwd;
    }
    public void setPwd(String _pwd) {
        pwd = _pwd;
    }


    public static String generateUID() {
        String s = Integer.toString((int)(Math.random()*9+1));

        for(int i = 1; i < UID_LENGTH; i++) {
            s += (int)(Math.random()*10);
        }

        return s;
    }
    public String getUID() {
        return uid;
    }
    public void setUID(String _uid) {
        uid = _uid;
    }

    public double getFunds() {
        return funds;
    }
    public void setFunds(double _funds) {
        funds = _funds;
    }
}
