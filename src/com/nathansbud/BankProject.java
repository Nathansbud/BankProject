package com.nathansbud;

import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class BankProject {
    private static Scanner sc = new Scanner(System.in);
    private static File file = new File("data/users.txt");

    static String[][] menuOptions = {
            {"Quit"},
            {"Login", "Create Account", "Forgot Password"},
            {"owo"}, //Login Options
            {"BIG MEME"}

    };

    private static boolean isRunning = true;

    private static int menuState = 1;
    private Stack<Integer> menu = new Stack<Integer>();

    private static ArrayList<User> users = new ArrayList<User>();


    private static void populateUsers() {
        try {
            BufferedReader info = new BufferedReader(new FileReader(file));


            String st;

            String id = "";
            String username = "";
            String password;

            int index = 0;
            while ((st = info.readLine()) != null) {
                switch (index) {
                    case 1:
                        id = st;
                        break;
                    case 2:
                        username = st;
                        break;
                    case 3:
                        password = st;
                        users.add(new User(username, password, id));
                    default:
                        index = 0;
                        break;
                }

                index++;
            }
        } catch(IOException e) {
            System.out.println("PopulateUser IOExcept");
        }
    }
    private static void createUser(User u) {
        createUser:
        {
            for (User a : users) {
                if (a.getUsername().equals(u.getUsername())) {
                    System.out.println("Attempted to add invalid user!");
                    break createUser;
                }
            }

            try {
                users.add(u);
                PrintWriter au = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
                au.println("#");
                au.println(u.getUsername());
                au.println(u.getPwd());
                au.close();
            } catch (IOException e) {
                System.out.println("CreateUser IOExcept");
            }
        }
    }

    private static void menuPrint(int menu) {
        for (int i = 0; i < menuOptions[menu].length; i++) {
            System.out.println((i+1) + ". " + menuOptions[menu][i]);
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
            if(username.contains("#") || username.contains(" ") || (username.length() < 4) || (username.length() > 25)) {
                System.out.println("Username cannot contain spaces, #, or be less than <4 and >25 characters");
                username = sc.nextLine();
            } else {
                passed = true;
                for (User u: users){
                    if(u.getUsername().equals(username)) {
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


    public static void main(String[] args) {
        populateUsers();
        createUser(new User("Nathansbud", "Squaduporbodup", "0000000"));


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
                    menuPrint(menuState);
                    System.out.println();
                    input = checkInput(sc.nextLine());
                    break;
                case 3:
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
                    break;


            }
        }
    }
}
