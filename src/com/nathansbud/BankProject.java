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
            {"Nice Big List"},

    };

    static boolean isRunning = true;

    static int menuState = 1;
    Stack<Integer> menu = new Stack<Integer>();

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
                if (a.getId().equals(u.getId()) || a.getUsername().equals(u.getUsername())) {
                    System.out.println("Attempted to add invalid user!");
                    break createUser;
                }
            }

            try {
                users.add(u);
                PrintWriter au = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
                au.println("#");
                au.println(u.getId());
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

        switch(menuState) { //needs to be while loops
            default:
                try {
                    int n = Integer.parseInt(input);
                    if(n > 0 && n <= menuOptions[menuState].length) {
                        arguments[0] = input;
                    }
                } catch(NumberFormatException e) { //handle actual exceptions
                    System.out.println("idk yet");
                }
                break;
        }

        return arguments;
    }



    public static void main(String[] args) {
        populateUsers();
        createUser(new User("Nathansbud", "Squaduporbodup", "0000000"));


        System.out.println("Welcome to Nathansbank!");
        System.out.println("What would you like to do today?");
        String input[];

        while(isRunning) {
            switch(menuState) {
                default:
                    menuPrint(menuState);
                    break;
            }

            input = checkInput(sc.nextLine());

            switch(menuState) {
                default:
                    break;
                case 0:
                    break;
                case 1:
                    switch(input[0]) {
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


            }
        }
    }
}
