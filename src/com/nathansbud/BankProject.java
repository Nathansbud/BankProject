package com.nathansbud;

import static com.nathansbud.Constants.*;

import java.io.*;
import java.util.Scanner;
//import java.util.Stack;

public class BankProject { //Todo: FIX NEGATIVES!!!!
    private static Scanner sc = new Scanner(System.in);

    private static File folder = new File("users/");
    private static File[] files = folder.listFiles();

    static String[][] menuOptions = {
            {"Quit"},
            {"Login", "Create Account", "Forgot Password", "Quit"},
            {"Yes", "No"}, //Login Options
            {"BIG MEME"},
            {"Forget Password"}, //Forget Password
            {"Deposit Funds", "Withdraw Funds", "Transfer Funds", "Show History", "Settings", "Log Out"} //6, 7, 8, 9, 10
    };

    private static boolean isRunning = true;

    private static int menuState = 1;
    private static User u = new User();

    private static User loadUser(String name) {
        try {
            BufferedReader b = new BufferedReader(new FileReader(folder + "/" + name + ".txt"));
            User temp = new User(b.readLine(), b.readLine(), b.readLine(), Double.parseDouble(b.readLine()), b.readLine()); //User, Password, UID
            temp.setUserFilepath(folder + "/" + name + ".txt");
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
                    if (f.getName().substring(0, f.getName().lastIndexOf(".")).equals(cu.getUsername())) {
                        System.out.println("Attempted to add invalid user!");
                        break createUser;
                    }
                    try {
                        BufferedReader b = new BufferedReader(new FileReader(f));
                        for (int i = 0; i < UID_LOC; i++) {
                            b.readLine();
                        }
                        uids[t] = b.readLine();

                        if (uids[t].equals(cu.getUID())) {
                            uidPassed = false;
                        }
                        t++;
                    } catch (IOException e) {
                        System.out.println("uhhh"); //should never happen?
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
                PrintWriter nu = new PrintWriter(new BufferedWriter(new FileWriter(folder + "/" + cu.getUsername() + ".txt")));
                cu.setUserFilepath(folder + "/" + cu.getUsername() + ".txt");
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

    private static void menuPrint(int menu) {
        System.out.println("Menu State: " + menuState);
        for (int i = 0; i < menuOptions[menu].length; i++) {
            System.out.println((i+1) + ". " + menuOptions[menu][i]);
        }
    }

    private static void menuPrint(String[] menu) {
        for (int i = 0; i < menu.length; i++) {
            System.out.println((i+1)+ ". " + menu[i]);
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
                        if(n > 0 && n <= menuOptions[menuState].length) {
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
                System.out.println("Username cannot contain spaces or colons, start with a ., be less than " + USERNAME_MINIMUM + " and greater than " + USERNAME_MAXIMUM);
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
        return f.getName().substring(0, f.getName().lastIndexOf("."));
    }


    public static void main(String[] args) {
        System.out.println("Welcome to Nathansbank!");
        System.out.println("What would you like to do today?");
        String input[] = new String[1];

        while(isRunning) {
            switch(menuState) {
                default:
                    menuPrint(menuState);
                    input = checkInput(sc.nextLine());
                    break;
                case 0:
                    System.exit(0);
                    break;
                case 2: {
                    input[0] = "2";
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
                        menuState = 3;
                        input[0] = "0";
                    } else {
                        System.out.println("Enter your password: ");

                        String passMatch = "";
                        try {
                            BufferedReader b = new BufferedReader(new FileReader(folder + "/" + login + ".txt"));
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
                case 3: {  //should be condensed to a function
                    boolean userPassed = false;
                    boolean passPassed = false;

                    String username = "";
                    String password = "";
                    System.out.println("Enter in a username: ");


                    while (!userPassed) {
                        username = createUsername(sc.nextLine());
                        System.out.println("Re-enter your username: ");
                        if (sc.nextLine().equals(username)) {
                            userPassed = true;
                        } else {
                            System.out.println("Usernames do not match...enter in a username");
                        }
                    }

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
                    menuState = 1;
                    input[0] = "0";
                    break;
                }
                case 4:
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
                        //Todo: Implement email functionality, sendEmail call
                    } else {
                        System.out.println("This user does not exist!");
                    }
                    break;
                case 5:
                    System.out.println("Welcome back, " + u.getUsername());
                    System.out.println("Current Balance: $" + String.format("%.2f", u.getFunds()));
                    menuPrint(menuState);
                    input = checkInput(sc.nextLine());
                    break;
                case 6: //Deposit
                case 7:
                    System.out.println("How much would you like to " + ((menuState == 6) ? ("deposit?") : ("withdraw?")));
                    double amount;
                    amount = moneyCheck(sc.nextLine());
                    if(amount == -1) {
                        System.out.println("Returning to user page...");
                    } else {
                        if(menuState == 6) {
                            u.depositFunds(amount);
                        } else u.withdrawFunds(amount);
                        System.out.println("$" + amount + " has been"+((menuState == 6)? (" added to ") : (" removed from ")) + "your account! Your total is now $" + u.getFunds());
                    }
                    menuState = 5;
                    input[0] = "0";
                    break;
                case 8:
                    System.out.println("Who would you like to transfer funds to?");
                    String transferUser = sc.nextLine();
                    System.out.println("How much would you like to transfer?");
                    double transferAmount = moneyCheck(sc.nextLine());
                    u.transferFunds(transferAmount, transferUser);
                    sc.nextLine();
                    break;
                case 9: //Todo: Clean this up for settings!
                    System.out.println("Account History");
                    String[] s = u.getHistory();
                    double ct = 0;

                    for(int i = 0; i < s.length; i++) {
                        System.out.print((i + 1) + ": ");


                        boolean transferred = false;
                        boolean received = false;
                        boolean monetary = false;
                        double change = 0;

                        if(s[i].charAt(0) != 'S') {
                            change = Double.parseDouble(s[i].substring(2, (s[i].lastIndexOf(":") > 2) ? (s[i].lastIndexOf(":")) : (s[i].length())));
                            monetary = true;
                        }
                        /*
                        ACTION CODES:
                            - Deposited — D:{Amount}
                            - Withdrew — W:{Amount}
                            — Transferred — T:{Amount}:{Recipient}
                            - Received — R:{Amount}:{Sender}
                            - Settings — S:{String}
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
                            case 'S':
                                System.out.print("Changed Setting: ");
                                break;
                        }
                        if(monetary) {
                            System.out.print(String.format("%.2f", change));
                            if (transferred) {
                                System.out.print(" to " + s[i].substring(s[i].lastIndexOf(":") + 1));
                            } else if (received) {
                                System.out.print(" from " + s[i].substring(s[i].lastIndexOf(":") + 1));
                            }
                            System.out.println(" - Balance: " + String.format("%.2f", ct));
                        } else {
                            System.out.println(s[i].substring(2));
                        }
                    }
                    menuState = 5;
                    input[0] = "0";
                    break;
            }



            switch(menuState) {
                default:
                    break;
                case 0:
                    break;
                case 1:
                    switch(input[0]) {
                        case "0":
                            break;
                        case "1": //login
                            menuState = 2;
                            break;
                        case "2": //create account
                            menuState = 3;
                            break;
                        case "3": //forgot password
                            menuState = 4;
                            break;
                        case "4":
                            menuState = 0;
                            break;
                    }
                    break;
                case 2:
                    menuState = 5;
                    break;
                case 4:
                    menuState = 1;
                    break;
                case 5:
                    switch(input[0]) {
                        case "0":
                            break;
                        case "1":
                            menuState = 6;
                            break;
                        case "2":
                            menuState = 7;
                            break;
                        case "3":
                            menuState = 8;
                            break;
                        case "4":
                            menuState = 9;
                            break;
                        case "5":
                            menuState = 10;
                            break;
                        case "6":
                            System.out.println("Logging out!");
                            menuState = 1;
                            break;
                    }
                    break;
                case 6:
                    break;
                case 9:
                    break;
            }
        }
    }
}
