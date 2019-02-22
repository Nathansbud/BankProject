package com.nathansbud;

import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class BankProject {
    private static Scanner sc = new Scanner(System.in);

    private static File folder = new File("users/");
    private static File[] files = folder.listFiles();

    static String[][] menuOptions = {
            {"Quit"},
            {"Login", "Create Account", "Forgot Password"},
            {"owo"}, //Login Options
            {"BIG MEME"},
            {"Forget Password"}, //Forget Password
            {"Deposit Funds", "Remove Funds", "Transfer Funds", "Settings", "Log Out"}
    };

    private static boolean isRunning = true;

    private static int menuState = 1;
    private static Stack<Integer> menuStack = new Stack<Integer>();
    private static User u = new User();

    private static final int USERNAME_LOC = 0;
    private static final int PWD_LOC = 1;
    private static final int UID_LOC = 2;
    private static final int EMAIL_LOC = 3;
    private static final int BALANCE_LOC = 4;
    private static final int HISTORY_LOC = 5;

    private static User loadUser(String name) {
        try {
            BufferedReader b = new BufferedReader(new FileReader(folder + "/" + name + ".txt"));

            return new User(b.readLine(), b.readLine()); //User, Password
            //return new User(b.readLine(), b.readLine(), b.readLine()); //User, Password, UID
            //return new User(b.readLine(), b.readLine(), b.readLine(), b.readLine()); //User, Password, UID, Email
        } catch(IOException e) {
            System.out.println("User does not exist!");
            return new User("NULL", "NULL");
        }
    }

    private static void createUser(User u) {
        createUser: {
            for (File f : files) {
                if (f.getName().substring(0, f.getName().lastIndexOf(".")).equals(u.getUsername())) {
                    System.out.println("Attempted to add invalid user!");
                    break createUser;
                }
            }

            try {
                PrintWriter nu = new PrintWriter(new BufferedWriter(new FileWriter(folder + "/" + u.getUsername() + ".txt")));
                nu.println(u.getUsername());
                nu.println(u.getPwd());
                nu.close();
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
            switch (menuState) { //needs to be while loops
                default:
                    try {
                        int n = Integer.parseInt(input);
                        if (n > 0 && n <= menuOptions[menuState].length) {
                            arguments[0] = input;
                            passed = true;
                        } else if(n == -99) {
                            arguments[0] = input;
                        } else {
                            System.out.println("Input must be between 1 " +  "and " + menuOptions[menuState].length);
                        }
                    } catch (NumberFormatException e) { //handle actual exceptions
                        System.out.println("idk yet");
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
            if(username.contains(" ") || (username.length() < 4) || (username.length() > 25) || (username.charAt(0) == '.')) {
                System.out.println("Username cannot contain spaces, start with a ., or be less than <4 and >25 characters");
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
            if(pass.length() < 4) {
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
                case 2:
                    boolean loginUserPassed = false;
                    boolean passwordPassed = false;

                    String login = "";
                    String pwd = "";

                    System.out.println("Enter your username: ");

                    while(!loginUserPassed) {
                        login = sc.nextLine();
                        for(File f : files) {
                            if(getFileUser(f).equals(login)) {
                                loginUserPassed = true;
                                break;
                            }
                        }

                        if(!loginUserPassed) {
                            System.out.println("This username does not exist! Would you like to create an account?");
                            menuPrint(new String[]{"Yes", "No"});
                        }
                    }

                    System.out.println("Enter your password: ");

                    String passMatch = "";
                    try {
                        BufferedReader b = new BufferedReader(new FileReader(folder + "/" + login + ".txt"));
                        for (int i = 0; i < PWD_LOC; i++) {
                            b.readLine();
                        } passMatch = b.readLine();
                        b.close();
                    } catch(IOException e) {
                        System.out.println("Username does not exist!");
                    }


                    while(!passwordPassed) {
                        pwd = sc.nextLine();
                        if(passMatch.equals(pwd)) {
                            System.out.println("Successful login!");
                            passwordPassed = true;
                        }

                        if(!passwordPassed) {
                            System.out.println("Username and password do not match!");
                        }
                    }

                    u = loadUser(login);
                    break;
                case 3: //should be condensed to a function
                    boolean userPassed = false;
                    boolean passPassed = false;

                    String username = "";
                    String password = "";
                    System.out.println("Enter in a username: ");


                    while(!userPassed) {
                        username = createUsername(sc.nextLine());
                        System.out.println("Re-enter your username: ");
                        if(sc.nextLine().equals(username)) {
                            userPassed = true;
                        } else {
                            System.out.println("Usernames do not match...enter in a username");
                        }
                    }

                    System.out.println("Enter in a password: ");
                    while(!passPassed) {
                        password = createPassword(sc.nextLine());
                        System.out.println("Re-enter your password: ");
                        if(sc.nextLine().equals(password)) {
                            passPassed = true;
                        } else {
                            System.out.println("Password do not match...enter in a password");
                        }
                    }

                    System.out.println("User created! Try logging in!");
                    createUser(new User(username, password));
                    menuState = 1;
                    input[0] = "-1";
                    break;
                case 5:
                    System.out.println("Welcome to " + u.getUsername());
                    menuPrint(menuState);
                    input = checkInput(sc.nextLine());
                    break;
            }



            switch(menuState) {
                default:
                    break;
                case 0:
                    break;
                case 1:
                    switch(input[0]) {
                        case "-1":
                            menuState = 1;
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
                    }
                    break;
                case 2:
                    menuState = 5;
                    break;
            }
        }
    }
}
