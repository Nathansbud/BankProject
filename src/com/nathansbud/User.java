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


    /**
     * Empty user constructor used when populating user fields from file, or creating test users
     */
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

    /**
     * Get history creates a log of all user transactions (including login/log-outs) to iterate over.
     * @return All entries in the user transactions file
     */
    public String[] getHistory(String user) {
        ArrayList<String> history = new ArrayList<>();

        try {
            BufferedReader b = new BufferedReader(new FileReader("data" + File.separator + user + File.separator + "transactions.txt"));
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

    public String[] getHistory() {
        return getHistory(username);
    }

    /**
     * @return Username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param _username Username to set
     */
    public void setUsername(String _username) {
        username = _username;
    }

    /**
     * @return User password
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * @param _pwd Password to set
     */
    public void setPwd(String _pwd) {
        pwd = _pwd;
    }

    /**
     * Generate UID generates a 10-digit UID by generating a random non-zero start integer, then populating the rest of the digits
     * @return Generated UID
     */
    public static String generateUID() { //Todo: This doesn't actually check for collisions, also is a stupid way to do this LUL
        String s = Integer.toString((int)(Math.random()*9+1));

        for(int i = 1; i < UID_LENGTH; i++) {
            s += (int)(Math.random()*10);
        }

        return s;
    }

    /**
     * @return User ID
     */
    public String getUID() {
        return uid;
    }
    /**
     * @param _uid User ID to set
     */
    public void setUID(String _uid) {
        uid = _uid;
    }

    /**
     * @return User type of current user
     */
    public UserType getUserType() {
        return userType;
    }
    /**
     * @param _userType User type to set
     */
    public void setUserType(UserType _userType) {
        userType = _userType;
    }

    /**
     * @param ut String value of user type to set
     */
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

    /**
     * @return User premium status
     */
    public boolean isPremium() {
        return isAdmin() || userType == UserType.PREMIUM;
    }

    /**
     * @return User admin status
     */
    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    /**
     * @return User creation timestamp
     */
    public String getCreated() {
        return created;
    }

    /**
     * @param _created Set timestamp of user creation
     */
    public void setCreated(String _created) {
        created = _created;
    }

    /**
     * Function is called when user logs in or out, as a means to log the operations in the transaction log.
     * If a login occurs, interest is also granted via {@link User#depositInterest()}.
     * @param login Boolean to log whether operation is a login or logout operation
     */

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

    /**
     * Rewrite funds is used to update user transaction files on any monetary operation, adding entries to the log.
     *
     * Function checks transaction type to determine necessary action string to input, then rewrites all lines as necessary.
     * If a transfer operation occurs, trivial recursion is used to call the function again with the correct transfer user.
     * @param amount Amount of funds to add/subtract
     * @param type Transaction type (Deposit, Withdraw, Interest, Transfer, Receive)
     * @param user Username of currently affected user, for user file (relevant in transfer operations)
     */
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
            //Start Author: Prithvi
            case INTEREST:
                actionString = "I:" + amount;
                break;
            //End Author: Prithvi
            default:
                actionString = "ERROR";
                break;
        }

        actionString += ":" + (System.currentTimeMillis() / 1000L);
        String transactionPath = "data" + File.separator + user + File.separator + "transactions.txt";

        String[] history = (type != TransactionType.RECEIVE) ? (getHistory()) : (getHistory(user));
        double ft = funds;
        try {
            if(type == TransactionType.RECEIVE) {
                ft = getFunds(user) + amount;
            }
            PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(transactionPath)));
            w.println(ft);
            for(String s : history) {
                w.println(s);
            } w.println(actionString);
            w.close();
        } catch(IOException e) {
            System.out.println("Funds rewriting failed with action string " + actionString);
        }
        if(type == TransactionType.TRANSFER) {
            rewriteFunds(amount, TransactionType.RECEIVE, send);
        }
    }

    /**
     * Used to update user funds with new deposit amount, and rewrite transaction file
     * @param deposit Amount to add to account
     */
    public void depositFunds(double deposit) {
        funds += deposit;
        rewriteFunds(deposit, TransactionType.DEPOSIT, username);
    }

    //Start Author: Shaunak & Prithvi
    /**
     * Function called on login, used to update user account balance on login with interest rate.
     *
     * Checks history by indexing backwards through ArrayList from {@link User#getHistory()} to find last log-in and logout.
     * Uses compound interest formula (P(e)^rt) by finding the time difference between the two, and using user's interest rate (6% for normal user, 12% for premium), then updates funds
     */
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

            double newAmount = funds * Math.pow(Math.E, ts / 31557600D * ((userType == UserType.NORMAL) ? (NORMAL_INTEREST_RATE) : (PREMIUM_INTEREST_RATE)));
            newAmount = newAmount*100/100.0 - funds;
            funds += newAmount;
            rewriteFunds(newAmount, TransactionType.INTEREST, username);
        }
    }
    //End Author: Shaunak & Prithvi
    /**
     * Withdraws an amount of funds from the user's current amount balance, {@link com.nathansbud.User#funds}
     * @param withdraw Amount of funds to withdraw
     * @return Withdrawn amount
     */
    public double withdrawFunds(double withdraw) {
        funds -= withdraw;
        rewriteFunds(withdraw, TransactionType.WITHDRAWAL, username);

        return withdraw;
    }

    /**
     * Transfer funds is used to send funds between two users. Checks to make sure recipient exists, otherwise will exit the transaction!
     *
     * @param transfer Amount to funds to be transferred; can be greater than current account balance
     * @param user Username of user to transfer funds
     */
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

    /**
     * @return User account balance
     */
    public double getFunds() {
        return funds;
    }

    /**
     * Static version of getFunds, to be used primarily in transfer situations
     *
     * @param user Username of user to get funds from
     * @return The funds of specified user
     */
    public static double getFunds(String user) {
        if(exists(user)) {
            try {
                BufferedReader b = new BufferedReader(new FileReader(new File("data" + File.separator + user + File.separator + "transactions.txt")));
                String line;
                int indexer = 0;

                while((line = b.readLine()) != null) {
                    if(indexer == BALANCE_LOC) {
                        double ret = Double.parseDouble(line);
                        b.close();
                        return ret;
                    }
                    indexer++;
                }
            } catch(IOException e) {
                System.out.println("Failed to read funds from specified user file! Returning 0...");
            }
            return 0;
        } else {
            System.out.println("Specified user does not exist! Returning 0...");
            return 0;
        }
    }

    /**
     * @param user User to check existence of
     * @return Whether or not a directory for user exists
     */
    public static boolean exists(String user) {
        return new File("data" + File.separator + user).exists();
    }
    /**
     * @param _funds Funds to set
     */
    public void setFunds(double _funds) {
        funds = _funds;
    }

    /**
     * @return User filepath
     */
    public String getUserFilepath() {
        return userFilepath;
    }

    /**
     * @param _userFilepath Filepath of to set
     */
    public void setUserFilepath(String _userFilepath) {
        userFilepath = _userFilepath;
    }

    /**
     * @return User email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param _email Email to set
     */
    public void setEmail(String _email) {
        email = _email;
    }

    /**
     * Method used to send messages between users
     * @param subject Subject of the message sent
     * @param sender Username of the sending user; by default is set to current logged-in user, but can be used for system messages
     * @param recipient Username of recipient
     * @param body Message content
     */
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
