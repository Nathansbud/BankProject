package com.nathansbud;

import java.io.*;
import java.util.ArrayList;

import static com.nathansbud.Constants.*;

public class User {
    private String username;
    private String pwd;
    private String uid; //Int - 7
    private String email;

    private double funds;

    private String userFilepath;

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


    public void rewriteFunds(double amount, int type, String user) {
        String actionString;
        String send = user;

        if(type == 0) {
            actionString = "D:" + amount;
        } else if(type == 1) {
            actionString = "W:" + amount;
        } else if(type == 2) {
            actionString = "T:" + amount + ":" + user;
            user = username;
        } else if(type == 3) {
            actionString = "R:" + amount + ":" + username;
        }  else {
            actionString = "ERROR";
        }


        String selfPath = "users/" + user + "/user.txt";
        String tempPath = "users/" + user + "/user.tst";

        try {
            BufferedReader b = new BufferedReader(new FileReader(selfPath));
            PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(tempPath)));

            String line;
            int indexer = 0;
            while ((line = b.readLine()) != null) {
                if(indexer == BALANCE_LOC && type != 3) {
                    w.println(funds);
                } else if(type == 3 && indexer == BALANCE_LOC) {
                    w.println(Double.parseDouble(line)+amount);
                } else {
                    w.println(line);
                }

                indexer++;
            } w.println(actionString);

            b.close();
            w.close();
            File re = new File(tempPath);
            File old = new File(selfPath);

            re.renameTo(old); //Todo: Figure out how to handle this bool?

            if(type == 2) {
                rewriteFunds(amount, 3, send); //This is good recursion, yes? Maybe?
            }
        } catch(IOException e) {
            System.out.println("Fund Writing Fail");
        }
    }
    public void depositFunds(double deposit) {
        funds += deposit;
        rewriteFunds(deposit, 0, username);
    }
    public double withdrawFunds(double withdraw) {
        funds -= withdraw;
        rewriteFunds(withdraw, 1, username);

        return withdraw;
    }
    public void transferFunds(double transfer, String user) {
        File f = new File("users/" + user + "/user.txt");
        if(f.exists()) {
            funds -= transfer;
            rewriteFunds(transfer, 2, user);
        } else {
            System.out.println("Transfer failed, user does not exist!");
        }
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

    public String getEmail() {
        return email;
    }
    public void setEmail(String _email) {
        email = _email;
    }

    //Static method because potential system messages; "Nathansbank" no-reply messages or something, idk...maybe shouldn't be static
    public static void sendMessage(String subject, String sender, String recipient, String body) {
        File toPath = new File("users/" + recipient);
        File fromPath = new File("users/" + sender);

        if(toPath.isDirectory() && fromPath.isDirectory()) {
            long unixTime = System.currentTimeMillis() / 1000L;

            File to = new File(toPath + "/messages/" + subject + "-" + unixTime + ".txt");
            File from = new File(fromPath + "/messages/" + subject + "-" + unixTime + ".txt");

            try {
                if (to.isFile()) {
                    PrintWriter toMessage = new PrintWriter(new BufferedWriter(new FileWriter(to)));
                    toMessage.println("M:"+body + ":" + sender);
                    toMessage.close();

                    if(!sender.equals(recipient)) {
                        PrintWriter fromMessage = new PrintWriter(new BufferedWriter(new FileWriter(from)));
                        fromMessage.println("M:" + body + ":" + sender);
                        fromMessage.close();
                    }
                } else {
                    PrintWriter toMessage = new PrintWriter(new BufferedWriter(new FileWriter(to)));

                    toMessage.println("F:" + sender);
                    toMessage.println("T:" + recipient);
                    toMessage.println("M:"+body + ":" + sender);

                    toMessage.close();


                    if(!sender.equals(recipient)) {
                        PrintWriter fromMessage = new PrintWriter(new BufferedWriter(new FileWriter(from)));
                        fromMessage.println("F:" + sender);
                        fromMessage.println("T:" + recipient);
                        fromMessage.println("M:" + body + ":" + sender);

                        fromMessage.close();
                    }
                }
            } catch (IOException e) {
                System.out.println("SendMessage IOExcept");
            }
        } else if(!toPath.isDirectory()) {
            System.out.println("Recipient does not exist!");
        } else {
            System.out.println("Sender does not exist!");
        }
    }
}
