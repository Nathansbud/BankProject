package com.nathansbud;

import java.io.*;
import java.nio.file.*;

import static com.nathansbud.Constants.*;

public class User {
    private String username;
    private String pwd;
    private String uid; //Int - 7

    private double funds;


    private File userFile;
    private String userFilepath;

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


    public void depositFunds(double deposit) {
        String actionString = "D:" + deposit;
        String tempPath = userFilepath.substring(0, userFilepath.lastIndexOf("."))+".tst";

        funds += deposit;


        try {
            BufferedReader b = new BufferedReader(new FileReader(userFilepath));
            PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(tempPath)));

            String line;
            int indexer = 0;
            while ((line = b.readLine()) != null) {
                if(indexer == BALANCE_LOC) {
                    w.println(funds);
                } else {
                    w.println(line);
                }
                
                indexer++;
            } w.println(actionString);

            b.close();
            w.close();
            BufferedReader temp = new BufferedReader(new FileReader(tempPath));
            BufferedWriter rewrite = new BufferedWriter(new FileWriter(userFilepath));
            while((line = temp.readLine()) != null) {
                rewrite.write(line + "\n");
            }

            temp.close();
            rewrite.close();

            Files.deleteIfExists(Paths.get(tempPath));
        } catch(IOException e) {
            System.out.println("crap");
        }
    }
    public double withdrawFunds(double deposit) {
        funds -= deposit;
        return deposit;
    }
    public double getFunds() {
        return funds;
    }
    public void setFunds(double _funds) {
        funds = _funds;
    }

    public String getUserFilepath() {
        return userFilepath;
    }
    public void setUserFilepath(String _userFilepath) {
        userFilepath = _userFilepath;
    }

    public File getUserFile() {
        return userFile;
    }
    public void setUserFile(File _userFile) {
        userFile = _userFile;
    }
}
