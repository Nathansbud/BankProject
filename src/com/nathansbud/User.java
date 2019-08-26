package com.nathansbud;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.ArrayList;

import static com.nathansbud.BConstants.*;

public class User {
    enum UserType {
        NORMAL(),
        PREMIUM(),
        ADMIN()
    }

    enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        INTEREST,
        TRANSFER,
        RECEIVE
    }

    private String username;
    private String pwd;
    private String uid; //Int - 7
    private String email;
    private String created;

    private UserType userType;

    private double funds;
    private String userFilepath;

    public User() {
        uid = generateUID();
    }

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
            BufferedReader b = new BufferedReader(new FileReader(userFilepath + File.separator + "transactions.txt"));
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

    public static String generateUID() { //Todo: This doesn't actually check for collisions
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

    public UserType getUserType() {
        return userType;
    }
    public void setUserType(UserType _userType) {
        userType = _userType;
    }

    public void setUserType(String ut) {
        switch(ut.toLowerCase()) {
            case "normal":
                userType = UserType.NORMAL;
                break;
            case "premium":
                userType = UserType.PREMIUM;
                break;
            case "admin":
                userType = UserType.ADMIN;
                break;
        }
    }

    public boolean isPremium() {
        return isAdmin() || userType == UserType.PREMIUM;
    }
    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    public String getCreated() {
        return created;
    }
    public void setCreated(String _created) {
        created = _created;
    }

    public void recordLogin(boolean login) {
        try {
            BufferedWriter b = new BufferedWriter(new FileWriter(new File( "data" + File.separator + username + File.separator + "transactions.txt"), true));
            b.write(((login)?("O:"):("C:"))+(System.currentTimeMillis()/1000L)+"\n");
            b.close();
        } catch(IOException e) {
            System.out.println("Big sad");
        }
        if(login) depositInterest();
    }
    public void rewriteFunds(double amount, TransactionType type, String user) {
        String actionString;
        String send = user;

        switch(type) {
            case DEPOSIT:
                actionString = "D:" + amount;
                break;
            case WITHDRAWAL:
                actionString = "W:" + amount;
                break;
            case TRANSFER:
                actionString = "T:" + amount + ":" + user;
                user = username;
                break;
            case RECEIVE:
                actionString = "R:" + amount + ":" + username;
                break;
            case INTEREST:
                actionString = "I:" + amount;
                break;
            default:
                actionString = "ERROR";
                break;
        }

        actionString += ":" + (System.currentTimeMillis() / 1000L);


        String selfPath = "data" + File.separator + user + File.separator + "transactions.txt";
        String tempPath = "data" + File.separator + user + File.separator + "transactions.tst";

        try {
            BufferedReader b = new BufferedReader(new FileReader(selfPath));
            PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(tempPath)));

            String line;
            int indexer = 0;
            while ((line = b.readLine()) != null) {
                if(indexer == BALANCE_LOC && type != TransactionType.RECEIVE) {
                    w.println(funds);
                } else if(type == TransactionType.RECEIVE && indexer == BALANCE_LOC) {
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

            if(type == TransactionType.TRANSFER) {
                rewriteFunds(amount, TransactionType.RECEIVE, send); //This is good recursion, yes? Maybe?
            }
        } catch(IOException e) {
            System.out.println("Fund Writing Fail");
        }
    }

    public void depositFunds(double deposit) {
        funds += deposit;
        rewriteFunds(deposit, TransactionType.DEPOSIT, username);
    }

    public void depositInterest() {
        String[] history = getHistory();
        long ts = 0;

        String lastLogout = "";
        String lastLogin = "";

        for (int i = history.length - 1; i >= 0; i--) {
            if (lastLogout.equals("") && history[i].startsWith("C")) lastLogout = history[i];
            if (lastLogin.equals("") && history[i].startsWith("O")) lastLogin = history[i];
            if (!lastLogout.equals("") && !lastLogin.equals("")) break;
        }

        if (!lastLogin.equals("") && !lastLogout.equals("")) {
            String[] s = lastLogin.split(":");
            Long lit = Long.parseLong(s[s.length - 1]);

            String[] a = lastLogout.split(":");
            Long lot = Long.parseLong(a[a.length - 1]);

            if (lit > lot) ts = lit - lot;
            else ts = System.currentTimeMillis() / 1000L - lot;

            double newAmount = funds * Math.pow(Math.E, ts / 31557600D * ((userType == UserType.NORMAL) ? (0.06) : (0.12)));
            newAmount = newAmount*100/100.0 - funds;
            funds += newAmount;
            rewriteFunds(newAmount, TransactionType.INTEREST, username);
        }
    }

    public double withdrawFunds(double withdraw) {
        funds -= withdraw;
        rewriteFunds(withdraw, TransactionType.WITHDRAWAL, username);

        return withdraw;
    }
    public void transferFunds(double transfer, String user) {
        File f = new File("data" + File.separator + user + File.separator + "transactions.txt");
        if(f.exists()) {
            funds -= transfer;
            rewriteFunds(transfer, TransactionType.TRANSFER, user);
            System.out.println("$" + String.format("%.2f", transfer) + " has been transferred to user " + user + "! Your total is now $" + String.format("%.2f", funds));
        } else {
            System.out.println("Transfer failed, user does not exist!");
        }
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

    public String getEmail() {
        return email;
    }
    public void setEmail(String _email) {
        email = _email;
    }

    //Static method because potential system messages; "BankProject" no-reply messages or something, idk...maybe shouldn't be static
    public static void sendMessage(String subject, String sender, String recipient, String body) {
        File toPath = new File("data" + File.separator + recipient);
        File fromPath = new File("data" + File.separator + sender);

        if(toPath.isDirectory() && fromPath.isDirectory()) {
            long unixTime = System.currentTimeMillis() / 1000L;

            File to = new File(toPath + File.separator +"messages" + File.separator + subject + "-" + unixTime + ".txt");
            File from = new File(fromPath + File.separator +"messages" + File.separator + subject + "-" + unixTime + ".txt");

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
