package com.nathansbud;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

import static com.nathansbud.Constants.*;

public class User {
    private String username;
    private String pwd;
    private String uid; //Int - 7
    private String email;

    private double funds;


    private File userFile;
    private String userFilepath;

    private static final String FAIL_CONDITION = "[\\n\\s]";

    public User() {}

    public User(String _username, String _pwd, double _funds, String _email) {
        username = _username;
        pwd = _pwd;
        uid = generateUID();
        email = _email;

        funds = _funds;
    }

    public User(String _username, String _pwd, String _uid, double _funds, String _email) {
        username = _username;
        pwd = _pwd;
        uid = _uid;
        funds = _funds;
        email = _email;
    }

    public String[] getHistory() {
        ArrayList<String> history = new ArrayList<>();

        try {
            BufferedReader b = new BufferedReader(new FileReader(userFilepath));
            for (int i = 0; i < HISTORY_LOC; i++) {
                b.readLine();
            }
            String line;
            while((line = b.readLine()) != null) {
                history.add(line);
            }
        } catch(IOException e) {
            System.out.println("GetHistory Fail");
        }

        String[] h = new String[history.size()];
        for (int i = 0; i < h.length; i++) {
            h[i] = history.get(i);
        }
        return h;
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


    public void rewriteFunds(double amount, int type) {
        String actionString;
        if(type == 0) {
            actionString = "D:" + amount;
        } else {
            actionString = "W:" + amount;
        }

        String tempPath = userFilepath.substring(0, userFilepath.lastIndexOf("."))+".tst";

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
            File re = new File(tempPath);
            File old = new File(userFilepath);

            re.renameTo(old); //Todo: Figure out how to handle this bool?
        } catch(IOException e) {
            System.out.println("crap");
        }
    }
    public void depositFunds(double deposit) {
        funds += deposit;
        rewriteFunds(deposit, 0);
    }
    public double withdrawFunds(double withdraw) {
        funds -= withdraw;
        rewriteFunds(withdraw, 1);

        return withdraw;
    }

    public double getFunds() {
        return funds;
    }
    public void setFunds(double _funds) {
        funds = _funds;
    } //Warning: Should only be used on account create!

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

    public String getEmail() {
        return email;
    }
    public void setEmail(String _email) {
        email = _email;
    }
}
