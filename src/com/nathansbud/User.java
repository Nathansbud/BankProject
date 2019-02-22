package com.nathansbud;

import java.util.Arrays;

public class User {
    private String username;
    private String pwd;
    private String id; //Int - 7

    private double funds;


    private static final int PASSWORD_MINIMUM = 3;
    private static final int PASSWORD_MAXIMUM = 50;

    private static final int USERNAME_MINIMUM = 3;
    private static final int USERNAME_MAXIMUM = 20;

    private static final String FAIL_CONDITION = "[\\n\\s]";

    public User() {}

    public User(String _username, String _pwd) {
        username = _username;
        pwd = _pwd;
    }

    public User(String _username, String _pwd, String _id) {
        username = _username;
        pwd = _pwd;
        id = _id;
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


    public static String generateId() {
        int[] t = new int[7];
        t[0] = (int)(Math.random()*9+1);
        for(int i = 1; i < t.length; i++) {
            t[i] = (int)(Math.random()*10);
        }

        return Arrays.toString(t).replaceAll("\\[", "").replaceAll("\\]","");
    }
    public String getId() {
        return id;
    }
    public void setId(String _id) {
        id = _id;
    }

    public double getFunds() {
        return funds;
    }
    public void setFunds(double _funds) {
        funds = _funds;
    }
}
