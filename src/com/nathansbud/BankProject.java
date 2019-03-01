package com.nathansbud;

import static com.nathansbud.Constants.*;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.util.Map;

//TODO: CLEAN UP SCREEN REDIRECT; INPUT IS A MESS!!!!

public class BankProject { //Todo: FIX NEGATIVES!!!!
    private static Scanner sc = new Scanner(System.in);

    private static File folder = new File("users/");
    private static File[] files = folder.listFiles();

    private static User u = new User();

    private static boolean isRunning = true;

    private enum Screen {

        //SCREENS

        QUIT(0),
        START(1),
        LOGIN(2),
        CREATE(3),
        FORGOT(4),
        HOMEPAGE(5),
        DEPOSIT(6),
        WITHDRAW(7),
        TRANSFER(8),
        HISTORY(9),
        INBOX(10),
        SETTINGS(11);

        //Methods

        private final int code;
//        private final String[] opts;

        Screen(int _code) {
            code = _code;
        }
//
//        Screen(int _code, String[] _opts) {
//            code = _code;
//            opts = _opts;
//        }


        public final int getCode() {
            return code;
        }
    }


    private static Screen menuState = Screen.START;
    private static Map<Screen, String[]> menuLookup = new HashMap<>();

    private static void populateMap() {
        menuLookup.put(Screen.START, new String[]{"Login", "Create Account", "Forgot Password", "Quit"});
        menuLookup.put(Screen.LOGIN, new String[]{"Yes", "No"});
        menuLookup.put(Screen.CREATE, new String[]{});
        menuLookup.put(Screen.HOMEPAGE, new String[]{"Deposit Funds", "Withdraw Funds", "Transfer Funds", "Show History", "Inbox", "Settings", "Log Out"}); //6, 7, 8, 9, 10, 11, 12
        menuLookup.put(Screen.INBOX, new String[]{"Read Messages", "Send Message"});
    }

    private static User loadUser(String name) {
        try {
            BufferedReader b = new BufferedReader(new FileReader(folder + "/" + name + "/user.txt"));
            User temp = new User(b.readLine(), b.readLine(), b.readLine(), Double.parseDouble(b.readLine()), b.readLine()); //User, Password, UID
            temp.setUserFilepath(folder + "/" + name + "/user.txt");
            b.close();
            return temp;

        } catch(IOException e) {
            System.out.println("User does not exist!");
            return new User("NULL", "NULL", "NULL", 0, "NULL");
        }
    }

    private static double moneyCheck(String deposit) {
        boolean depositPassed = false;
        double amount = -1;
        while(!depositPassed) {
            try {
                amount = Double.parseDouble(deposit);
                if(amount > 0) {
                    depositPassed = true;
                } else {
                    System.out.println("Deposit amount must be >0");
                    deposit = sc.nextLine();
                }
            } catch(NumberFormatException e) {
                if(deposit.toLowerCase().equals("back")) {
                    depositPassed = true;
                } else {
                    System.out.println("Deposit amount must be a number!");
                    deposit = sc.nextLine();
                }
            }
        }
        return amount;
    }

    private static void createUser(User cu) {
        createUser: {
            String uids[] = new String[files.length];
            boolean uidPassed = true;
            double tempFunds = cu.getFunds();

            int t = 0; //User ID nonsense

            for (File f : files) {
                if(!f.getName().equals(".DS_Store")) {
                    if (f.getName().equals(cu.getUsername())) {
                        System.out.println("Attempted to add invalid user!");
                        break createUser;
                    }
                    try {
                        BufferedReader b = new BufferedReader(new FileReader(f + "/user.txt"));

                        for (int i = 0; i < UID_LOC; i++) {
                            b.readLine();
                        }
                        uids[t] = b.readLine();

                        if (uids[t].equals(cu.getUID())) {
                            uidPassed = false;
                        }
                        t++;
                    } catch (IOException e) {
                        System.out.println("CreateUser UID LoadException"); //should never happen?
                    }
                }
            }

            while(!uidPassed) { //Generate new UID if collision
                cu.setUID(User.generateUID());
                uidPassed = true;

                for(String c : uids) {
                    if(c.equals(cu.getUID())) {
                       uidPassed = false;
                    }
                }
            }

            try {
                File dir = new File(folder + "/" + cu.getUsername());
                boolean dirMade = dir.mkdir();
                File messages = new File(dir + "/messages");
                boolean messagesMade = messages.mkdir();

                PrintWriter nu = new PrintWriter(new BufferedWriter(new FileWriter(dir + "/user.txt")));
                cu.setUserFilepath(dir + "/user.txt");
                cu.setFunds(0);

                nu.println(cu.getUsername());
                nu.println(cu.getPwd());
                nu.println(cu.getUID());
                nu.println(cu.getFunds());
                nu.println(cu.getEmail());
                nu.println("#");
                nu.close();

                files = folder.listFiles();
                cu.depositFunds(tempFunds);
            } catch (IOException e) {
                System.out.println("CreateUser IOExcept");
            }
        }
    }

    private static void menuPrint(Screen menu) {
        System.out.println("Menu State: " + menuState.getCode() + " ("  + menuState + ")");
        for (int i = 0; i < menuLookup.get(menu).length; i++) {
            System.out.println((i+1) + ". " + menuLookup.get(menu)[i]);

        }
    }

    private static String[] checkInput(String input) {
        String arguments[] = new String[3];
        boolean passed = false;

        while(!passed) {
            switch (menuState) {
                default:
                    try {
                        int n = Integer.parseInt(input);
                        if(n > 0 && n <= menuLookup.get(menuState).length) {
                            arguments[0] = input;
                            passed = true;
                        } else {
                            System.out.println("Input must be one of the above numerical choices!");
                        }
                    } catch (NumberFormatException e) { //handle actual exceptions
                        System.out.println("Input must be one of the above numerical choices!");
                    }
                    if(!passed) {
                        input = sc.nextLine();
                    }
                    break;
            }
        }

        return arguments;
    }

    private static String createUsername(String username) {
        boolean passed = false;
        while(!passed) {
            if(username.contains(" ") || username.contains(":") || username.length() < USERNAME_MINIMUM || (username.length() > USERNAME_MAXIMUM) || (username.charAt(0) == '.')) {
                System.out.println("Username cannot contain spaces or colons, start with a ., and must be between " + USERNAME_MINIMUM + " and " + USERNAME_MAXIMUM + " characters");
                username = sc.nextLine();
            } else {
                passed = true;
                for (File f : files){
                    if(getFileUser(f).equals(username)) {
                        System.out.println("This username is taken!");
                        passed = false;
                        username = sc.nextLine();
                        break;
                    }
                }
            }
        }
        return username;
    }

    private static String createPassword(String pass) {
        boolean passed = false;
        while(!passed) {
            if(pass.length() < PASSWORD_MINIMUM) {
                System.out.println("Password must be greater than 4 characters");
                pass = sc.nextLine();
            } else {
                passed = true;
            }
        }
        return pass;
    }

    public static String getFileUser(File f) {
        return f.getName();
                //.substring(0, f.getName().lastIndexOf("."));
    }


    public static void main(String[] args) {
        System.out.println("Welcome to Nathansbank!");
        System.out.println("What would you like to do today?");
        String input[] = new String[1];
        populateMap();

        while(isRunning) {
            switch(menuState) {
                default:
                    System.out.println("Menu State: " + menuState);
                    sc.nextLine(); //Stall to avoid infinite loop, since this state should never be reached in practice
                    break;
                case QUIT:
                    System.exit(0);
                    break;
                case START:
                    menuPrint(menuState);
                    input = checkInput(sc.nextLine());
                    break;
                case LOGIN: {
                    input[0] = "2"; //not sure what's up with this but it breaks without it and too lazy to trace it rn
                    boolean loginUserPassed = false;
                    boolean passwordPassed = false;

                    String login = "";
                    String pwd = "";

                    System.out.println("Enter your username: ");

                    while (!loginUserPassed) {
                        login = sc.nextLine();
                        for (File f : files) {
                            if (getFileUser(f).equals(login)) {
                                loginUserPassed = true;
                                break;
                            }
                        }

                        if (!loginUserPassed) {
                            System.out.println("This username does not exist! Would you like to create an account?");
                            menuPrint(menuState);
                            input = checkInput(sc.nextLine());
                            if(input[0].equals("1")) {
                                loginUserPassed = true;
                            } else {
                                System.out.println("Enter your username: ");
                            }
                        }


                    }

                    if(input[0].equals("1")) {
                        menuState = Screen.CREATE;
                        input[0] = "0";
                    } else {
                        System.out.println("Enter your password: ");

                        String passMatch = "";
                        try {
                            BufferedReader b = new BufferedReader(new FileReader(folder + "/" + login + "/user.txt"));
                            for (int i = 0; i < PWD_LOC; i++) {
                                b.readLine();
                            }
                            passMatch = b.readLine();
                            b.close();
                        } catch (IOException e) {
                            System.out.println("Username does not exist!");
                        }

                        while (!passwordPassed) {
                            pwd = sc.nextLine();
                            if (passMatch.equals(pwd)) {
                                System.out.println("Successful login!");
                                passwordPassed = true;
                            }

                            if (!passwordPassed) {
                                System.out.println("Username and password do not match!");
                            }

                            u = loadUser(login);
                        }
                    }
                    break;
                }
                case CREATE: {  //should be condensed to a function
                    boolean passPassed = false;

                    System.out.println("Enter in a username: ");


                    String username = createUsername(sc.nextLine());
                    String password = "";

                    System.out.println("Enter in a password: ");
                    while (!passPassed) {
                        password = createPassword(sc.nextLine());
                        System.out.println("Re-enter your password: ");
                        if (sc.nextLine().equals(password)) {
                            passPassed = true;
                        } else {
                            System.out.println("Password do not match...enter in a password");
                        }
                    }

                    System.out.println("User created! Try logging in!");
                    createUser(new User(username, password, 100, "test@gmail.com"));
                    menuState = Screen.START;
                    input[0] = "0";
                    break;
                }
                case FORGOT:
                    System.out.println("Please input your username!");
                    String usr = sc.nextLine();
                    boolean userExists = false;
                    for (File f : files) {
                        if(getFileUser(f).equals(usr)) {
                            userExists = true;
                            break;
                        }
                    }
                    if(userExists) {
                        System.out.println("Please input your email address: ");
                    } else {
                        System.out.println("This user does not exist!");
                    }
                    break;
                case HOMEPAGE:
                    System.out.println("Welcome back, " + u.getUsername());
                    System.out.println("Current Balance: $" + String.format("%.2f", u.getFunds()));
                    menuPrint(menuState);
                    input = checkInput(sc.nextLine());
                    break;
                case DEPOSIT: //Deposit
                case WITHDRAW:
                    System.out.println("How much would you like to " + ((menuState == Screen.DEPOSIT) ? ("deposit?") : ("withdraw?")));
                    double amount;
                    amount = moneyCheck(sc.nextLine());
                    if(amount == -1) {
                        System.out.println("Returning to user page...");
                    } else {
                        if(menuState == Screen.DEPOSIT) {
                            u.depositFunds(amount);
                        } else u.withdrawFunds(amount);
                        System.out.println("$" + amount + " has been"+((menuState == Screen.DEPOSIT)? (" added to ") : (" removed from ")) + "your account! Your total is now $" + String.format("%.2f", u.getFunds()));
                    }
                    menuState = Screen.HOMEPAGE;
                    input[0] = "0";
                    break;
                case TRANSFER:
                    System.out.println("Who would you like to transfer funds to?");
                    String transferUser = sc.nextLine();
                    System.out.println("How much would you like to transfer?");
                    double transferAmount = moneyCheck(sc.nextLine());
                    u.transferFunds(transferAmount, transferUser);
                    System.out.println("$" + transferAmount + "has been transferred to user " + transferUser + "! Your total is now $" + String.format("%.2f", u.getFunds()));
                    menuState = Screen.HOMEPAGE;
                    input[0] = "0";
                    break;
                case HISTORY:
                    System.out.println("Account History");
                    String[] s = u.getHistory();
                    double ct = 0;

                    for(int i = 0; i < s.length; i++) {
                        System.out.print((i + 1) + ": ");

                        boolean transferred = false;
                        boolean received = false;
                        double change = Double.parseDouble(s[i].substring(2, (s[i].lastIndexOf(":") > 2) ? (s[i].lastIndexOf(":")) : (s[i].length())));

                        /*
                        ACTION CODES:
                            - Deposited — D:{Amount}
                            - Withdrew — W:{Amount}
                            — Transferred — T:{Amount}:{Recipient}
                            - Received — R:{Amount}:{Sender}
                        */

                        switch (s[i].charAt(0)) {
                            case 'D':
                                System.out.print("Deposited $");
                                ct += change;
                                break;
                            case 'W':
                                System.out.print("Withdrew $");
                                ct -= change;
                                break;
                            case 'T':
                                System.out.print("Transferred $");
                                ct -= change;
                                transferred = true;
                                break;
                            case 'R':
                                System.out.print("Received $");
                                ct += change;
                                received = true;
                                break;
                        }
                        System.out.print(String.format("%.2f", change));
                        if (transferred) {
                            System.out.print(" to " + s[i].substring(s[i].lastIndexOf(":") + 1));
                        } else if (received) {
                            System.out.print(" from " + s[i].substring(s[i].lastIndexOf(":") + 1));
                        }
                        System.out.println(" - Balance: " + String.format("%.2f", ct));
                    }
                    menuState = Screen.HOMEPAGE;
                    input[0] = "0";
                    break;
                case INBOX: //Inbox
                    menuPrint(menuState);
                    break;
            }


            switch(menuState) { //Todo: Merge this with ^
                default:
                    break;
                case QUIT:
                    break;
                case START:
                    switch(input[0]) {
                        case "0":
                            break;
                        case "1": //login
                            menuState = Screen.LOGIN;
                            break;
                        case "2": //create account
                            menuState = Screen.CREATE;
                            break;
                        case "3": //forgot password
                            menuState = Screen.FORGOT;
                            break;
                        case "4":
                            menuState = Screen.QUIT;
                            break;
                    }
                    break;
                case LOGIN:
                    menuState = Screen.HOMEPAGE;
                    break;
                case FORGOT:
                    menuState = Screen.START;
                    break;
                case HOMEPAGE:
                    switch(input[0]) {
                        case "0": //Nothing
                            break;
                        case "1": //Deposit
                            menuState = Screen.DEPOSIT;
                            break;
                        case "2": //Withdraw
                            menuState = Screen.WITHDRAW;
                            break;
                        case "3": //Transfer
                            menuState = Screen.TRANSFER;
                            break;
                        case "4": //History
                            menuState = Screen.HISTORY;
                            break;
                        case "5": //Inbox
                            menuState = Screen.INBOX;
                            break;
                        case "6": //Settings
                            menuState = Screen.SETTINGS;
                            break;
                        case "7": //Log-Out
                            System.out.println("Logging out!");
                            menuState = Screen.START;
                            break;
                    }
                    break;
                case HISTORY:
                    break;
                case INBOX:
                    break;
            }
        }
    }
}
