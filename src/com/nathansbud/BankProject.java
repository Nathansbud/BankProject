package com.nathansbud;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static com.nathansbud.BConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map;


/*--------------------------------------------*\
Todo:
    - Email-related stuff
    - Some form of interest accruing?

\*--------------------------------------------*/

public class BankProject {
    private static Scanner sc = new Scanner(System.in);

    private static File folder = new File("data");
    private static File[] files = folder.listFiles();

    private static User u = new User();
    private static Emailer emailer = new Emailer("creds"+File.separator+"email.json");
    private static JSONParser json = new JSONParser();

    private static boolean running = true;
    private static boolean debug = true;


    private enum Screen {
        //STATE SCREENS

        QUIT(0),
        START(1),
        LOGIN(2),
        CREATE(3),
        FORGOT_PASSWORD(4),

        HOMEPAGE(5),
        DEPOSIT(6),
        WITHDRAW(7),
        TRANSFER(8),
        HISTORY(9),
        MESSAGES(10),
        INBOX(11),
        OUTBOX(12),
        SETTINGS(15),

        TERMINATION(50),
        MASS_TERMINATION(999),
        //NON-STATE SCREENS

        CHOICE(-10), //Used for Yes/No options in map
        ;

        //Methods

        private final int code;

        Screen(int _code) {
            code = _code;
        }

        public final int getCode() {
            return code;
        }
    }

    private static Screen menuState = Screen.START;
    private static Map<Screen, String[]> menuLookup;

    /**
     * Debug method used to load in a specific user and state
     * @param user Username of user to be loaded in
     * @param state Screen state to set program to
     */
    public static void debugSetup(String user, Screen state) {
        if(debug) {
            u = loadUser(user);
            menuState = state;
        }
    }

    /**
     * Method to establish all menu options for necessary screens
     * @param userType Type of user to load menu options based off of
     */
    private static void populateMap(User.UserType userType) {
        menuLookup = new HashMap<Screen, String[]>();
        menuLookup.put(Screen.CHOICE, new String[]{"Yes", "No"});
        menuLookup.put(Screen.START, new String[]{"Login", "Create Account", "Forgot Password", "Quit"});
        menuLookup.put(Screen.CREATE, new String[]{});
        menuLookup.put(Screen.HOMEPAGE, new String[]{"Deposit Funds", "Withdraw Funds", "Transfer Funds", "Show History", "Messages", "Settings", "Log Out"}); //6, 7, 8, 9, 10, 11, 12
        menuLookup.put(Screen.MESSAGES, new String[]{"Read Messages", "Send Message"});
        if(userType != User.UserType.ADMIN) {
            menuLookup.put(Screen.SETTINGS, new String[]{"Change Password", "Terminate Account"});
        } else {
            menuLookup.put(Screen.SETTINGS, new String[]{"Change Password", "Terminate Account", "Terminate All"});
        }
    }

    /**
     * Method to load a user from their user.json file, given username as input.
     *
     * Parses the returned JSONObject to set the fields of the user to load, and loads in all funds from the user's transaction log if they exist
     * @param name Username of user to load in
     * @return User that was loaded in
     */
    private static User loadUser(String name) {
        JSONParser json = new JSONParser();
        try {
            BufferedReader b = new BufferedReader(new FileReader(folder + File.separator + name + File.separator + "user.json"));
            JSONObject userJson = (JSONObject)json.parse(b);
            b.close();

            User loadedUser = new User();

            loadedUser.setUsername((String)userJson.get("username"));
            loadedUser.setPwd((String)userJson.get("password"));
            loadedUser.setUID((String)userJson.get("uid"));
            loadedUser.setEmail((String)userJson.get("email"));
            loadedUser.setCreated((String)userJson.get("user_created"));
            loadedUser.setUserFilepath(folder + File.separator + name);

            switch((String)userJson.get("user_type")) {
                case "premium":
                    loadedUser.setUserType(User.UserType.PREMIUM);
                    break;
                case "admin":
                    loadedUser.setUserType(User.UserType.ADMIN);
                    break;
                default:
                    loadedUser.setUserType(User.UserType.NORMAL);
                    break;
            }

            b = new BufferedReader(new FileReader(new File(loadedUser.getUserFilepath() + File.separator + "transactions.txt")));
            loadedUser.setFunds(Double.parseDouble(b.readLine()));
            b.close();

            return loadedUser;
        } catch(IOException | ParseException e) {
            System.out.println("Failed to load user file!");
            return null;
        }
    }

    /**
     * Method to get all active UIDs
     * @return ArrayList of all current uids
     */
    private static ArrayList<String> getUIDs() {
        ArrayList<String> uids = new ArrayList<String>();

        try {
            BufferedReader b = new BufferedReader(new FileReader(new File(folder + File.separator + "uids.txt")));
            String uidRead;

            while ((uidRead = b.readLine()) != null) {
                uids.add(uidRead);
            }
            b.close();
        } catch(IOException e) {
            System.out.println(":(((");
        }
        return uids;
    }

    /**
     * Checks to make sure that money amounts are valid before doing operations with them.
     * @param deposit Money string being validated
     * @return Validated money string as a double
     */
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


    /**
     * Creates a new user by making a new user directory, messages directory, user json file, and transaction log.
     *
     * Reads in to make sure that the user being added is valid, then generates a validated UID and writes it out to UID file.
     * A user JSONObject is created, all necessary tags are set based on passed-in user ({@param cu}), and ({@link com.nathansbud.BankProject#files} is updated with new directory.
     * @param cu User to create
     */
    @SuppressWarnings("unchecked") private static void createUser(User cu) {
        JSONObject userJson = new JSONObject();

        for(File f : files) {
            if(!(f.getName().equals(".DS_Store") || f.getName().equals(".gitkeep"))) {
                if(f.getName().equals(cu.getUsername())) {
                    System.out.println("Tried to add invalid user!");
                    return;
                }
            }
        }

        try {
            ArrayList<String> uids = getUIDs();

            while(uids.contains(cu.getUID())) {
                cu.setUID(User.generateUID());
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(folder + File.separator + "uids.txt", true));
            bw.write(cu.getUID()+"\n");
            bw.flush();
            bw.close();
        } catch(IOException e) {
            System.out.println("Failed to find UID file!");
            return;
        }

        userJson.put("username", cu.getUsername());
        userJson.put("password", cu.getPwd());
        userJson.put("email", cu.getEmail());
        userJson.put("uid", cu.getUID());
        userJson.put("user_created", cu.getCreated());
        userJson.put("user_type", cu.getUserType().name().toLowerCase());


        File userDir = new File(folder + File.separator + cu.getUsername());
        userDir.mkdir();
        File messagesDir = new File(userDir + File.separator + "messages");
        messagesDir.mkdir();


        try{
            FileWriter w = new FileWriter(userDir + File.separator + "user.json");
            w.write(userJson.toJSONString());
            w.flush();
            w.close();

            w = new FileWriter(userDir + File.separator + "transactions.txt");
            w.write(cu.getFunds()+"\n");
            w.flush();
            w.close();
        } catch(IOException e) {
            System.out.println("BIG SAD, failed to write out user json file");
            e.printStackTrace();
        }

        files = folder.listFiles();
    }

    /**
     * Removes the user directory associated with {@param cu}, as well as associated UID.
     * Updates {@link com.nathansbud.BankProject#files} after finishing, to reflect removed user
     * @param cu User to remove
     */
    private static void removeUser(User cu) {
        File f = new File(cu.getUserFilepath());
        removeUID(cu.getUID());
        removeDirectory(f);
        files = folder.listFiles();
    }

    /**
     * Reads in user directory, and removes user UID and recursively deletes all files in the directory.
     * Updates {@link com.nathansbud.BankProject#files} after finishing, to reflect removed user
     * @param f User directory to remove
     */
    private static void removeUser(File f) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(f + File.separator + "user.json"));
            removeUID((String)((JSONObject)json.parse(br)).get("uid"));
            br.close();
            removeDirectory(f);
        } catch(ParseException | IOException e) {
            System.out.println(":(");
        }
        files = folder.listFiles();
    }

    /**
     * Method to remove UID from uids file. Reads in UIDs from {@link BankProject#getUIDs()}, and deletes specified
     * @param uid User ID to remove
     */
    private static void removeUID(String uid) {
        ArrayList<String> uids = getUIDs();
        uids.remove(uid);
        try {
            BufferedWriter f = new BufferedWriter(new FileWriter(new File(folder + File.separator + "uids.txt")));
            for(String s : uids) {
                f.write(s + "\n");
            }
            f.flush();
            f.close();
        } catch(IOException e) {
            System.out.println(":(((");
        }

    }

    /**
     * Method to recursively remove all files in a directory
     * @param dir Directory to remove
     */
    private static void removeDirectory(File dir) {
        if(dir.isDirectory()) {
            for(File f : dir.listFiles()) {
                removeDirectory(f);
            }
        }
        dir.delete();
    }

    /**
     * Method to print out the menu options for a given screen using {@link com.nathansbud.BankProject#menuLookup}.
     * No validation is done to ensure that current screen state has available options
     * @param menu Desired screen state
     */
    private static void menuPrint(Screen menu) {
        for (int i = 0; i < menuLookup.get(menu).length; i++) {
            System.out.println((i+1) + ". " + menuLookup.get(menu)[i]);

        }
    }

    /**
     * Method to validate user input against the option set provided from {@link com.nathansbud.BankProject#menuLookup}
     * @param state Screen state used as argument for {@link com.nathansbud.BankProject#menuLookup}
     * @param input User input string
     * @return The validated user input
     */
    private static String[] checkInput(Screen state, String input) {
        String arguments[] = new String[3];
        boolean passed = false;

        while(!passed) {
            try {
                int n = Integer.parseInt(input);
                if(n > 0 && n <= menuLookup.get(state).length) {
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
        }
        return arguments;
    }


    /**
     * Overloaded {@link com.nathansbud.BankProject#checkInput(Screen, String)} to validate screen input assuming that options are from current screen
     * @param input User input for option choice
     * @return The validated user input
     */
    private static String[] checkInput(String input) {
        return checkInput(menuState, input);
    }

    /**
     * Username validator before setting it
     * @param username Username to validate
     * @return The created username, adhering to all email rules
     */
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
                        System.out.println("This username is taken!"); //Todo: Change this to be a "do you want to log in?"
                        passed = false;
                        username = sc.nextLine();
                        break;
                    }
                }
            }
        }
        return username;
    }

    /**
     * Create email checks to see if an email exists or is invalid before creating it
     * @param email Email to validate
     * @return The created email, adhering to all email rules
     */
    private static String createEmail(String email) {
        ArrayList<String> emails = Emailer.getAllEmails();

        while(!Emailer.isValid(email) || emails.contains(email)) {
            if(!Emailer.isValid(email)) System.out.println("Email is not valid!");
            else System.out.println("Email already exists!");

            email = sc.nextLine();
        }

        return email;
    }

    /**
     * Create password is used on account creation go validate a password before creating it
     * @param pass Password to validate
     * @return The created user password, adhering to all password limits
     */
    private static String createPassword(String pass) {
        boolean passed = false;
        while(!passed) {
            if(pass.length() < PASSWORD_MINIMUM) {
                System.out.println("Password must be at least " + PASSWORD_MINIMUM + " characters!");
                pass = sc.nextLine();
            } else {
                passed = true;
            }
        }
        return pass;
    }


    /**
     * Getter for user files
     * @return All created account directories
     */
    public static File[] getFiles() {
        return files;
    }

    /**
     * Get file user returns the username of an associated directory
     * @param f User directory, as given inside the data folder
     * @return The associated file user for a directory
     */
    public static String getFileUser(File f) {
        return f.getName();
    }

    /**
     * The main function is used for the bank loop (user account and actions)
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Welcome to Nathansbank!");
        System.out.println("What would you like to do today?");
        String[] input = new String[1];
        populateMap(User.UserType.NORMAL);

        while(running) {
            System.out.println("Menu State: " + menuState.getCode() + " ("  + menuState + ")");
            input[0] = "0";

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
                //Todo: Make separate function
                case LOGIN: {
                    boolean loginUserPassed = false;
                    boolean passwordPassed = false;
                    boolean shouldContinue = true;

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
                            menuPrint(Screen.CHOICE);
                            input = checkInput(Screen.CHOICE, sc.nextLine());
                            switch(input[0]) {
                                case "1":
                                    shouldContinue = false;
                                    loginUserPassed = true;
                                    menuState = Screen.CREATE; //State Change: Login -> Create
                                    break;
                                case "2":
                                    System.out.println("Re-enter your username: ");
                                    break;
                            }
                        }
                    }


                   if(shouldContinue) {
                        System.out.println("Enter your password: ");

                        String passMatch = "";


                        try {
                            BufferedReader b = new BufferedReader(new FileReader(folder + File.separator + login + File.separator + "user.json"));
                            JSONObject userJson = (JSONObject)json.parse(b);
                            b.close();

                            passMatch = (String)userJson.get("password");
                        } catch (IOException | ParseException e) {
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
                            if (u != null) {
                                populateMap(u.getUserType());
                            } else {
                                System.out.println("User load error! Returning to login screen...");
                                menuState = Screen.START; //State Change: Login -> Start
                            }
                        }
                    }
                    break;
                }

                //Todo: Make separate function
                case CREATE: {
                    boolean passPassed = false;

                    System.out.println("Enter in a username: ");
                    String username = createUsername(sc.nextLine());

                    System.out.println("Enter in an email: ");
                    String email = createEmail(sc.nextLine());

                    String password = "";

                    System.out.println("Enter in a password: ");
                    while (!passPassed) {
                        password = createPassword(sc.nextLine());
                        System.out.println("Re-enter your password: ");
                        if (sc.nextLine().equals(password)) {
                            passPassed = true;
                        } else {
                            System.out.println("Passwords do not match...enter in a new password!");
                        }
                    }

                    System.out.println("User created! Try logging in!");

                    User cu = new User();

                    cu.setUsername(username);
                    cu.setPwd(password);
                    cu.setEmail(email);

                    cu.setFunds(0);
                    cu.setUserType(User.UserType.NORMAL);
                    cu.setCreated(String.valueOf(System.currentTimeMillis() / 1000L));

                    createUser(cu);
                    menuState = Screen.START; //State Change: Create -> Start
                    break;
                }
                case FORGOT_PASSWORD:
                    System.out.println("If an email is found matching a registered user, password reset instructions will be sent! Please input your email: ");
                    emailer.sendResetEmail(sc.nextLine());
                    menuState = Screen.START; //State Change: Password Reset -> Start
                    break;
                case HOMEPAGE:
                    System.out.println("Welcome back, " + u.getUsername() + " [User Type: " + u.getUserType().name() + "]");
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
//                          Transaction t = new Transaction("#", u.getUsername(), amount, String.valueOf(System.currentTimeMillis() / 1000L));
                            u.depositFunds(amount);
                        } else {
//                          Transaction t = new Transaction(u.getUsername(), "#", amount, String.valueOf(System.currentTimeMillis() / 1000L));
                            u.withdrawFunds(amount);
                        }
                        System.out.println("$" + String.format("%.2f", amount) + " has been"+((menuState == Screen.DEPOSIT)? (" added to ") : (" removed from ")) + "your account! Your total is now $" + String.format("%.2f", u.getFunds()));
                    }
                    menuState = Screen.HOMEPAGE; //State Change: Withdraw/Deposit -> Start
                    break;
                case TRANSFER:
                    System.out.println("Who would you like to transfer funds to?");
                    String transferUser = sc.nextLine();
                    System.out.println("How much would you like to transfer?");
                    double transferAmount = moneyCheck(sc.nextLine());
                    u.transferFunds(transferAmount, transferUser);
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

                    break;
                case MESSAGES: //Messages
                    menuPrint(menuState);
                    input = checkInput(sc.nextLine());
                    break;
                case INBOX:
                    System.out.println("Messages: ");
                    File[] fol = new File(u.getUserFilepath() + File.separator + "messages").listFiles();
                    if(fol != null) {
                        if(fol.length == 0 || (fol.length == 1 && fol[0].getName().equals(".DS_Store"))) {
                            System.out.println("You have no messages!");
                            System.out.println("Would you like to send a message?");
                            menuPrint(Screen.CHOICE);
                            input = checkInput(Screen.CHOICE, sc.nextLine());
                            switch(input[0]) { //State Change: Inbox -> Outbox/Homepage
                                case "1":
                                    menuState = Screen.OUTBOX;
                                    break;
                                case "2:":
                                    menuState = Screen.HOMEPAGE;
                                    break;
                            }
                        } else {
                            for(int i = 0, j = 0; i < fol.length; i++, j++) { //J variable due to DS_Store; not sure how to handle replying?
                                if(!fol[i].getName().equals(".DS_Store")) {
                                    System.out.println((j+1)+". " + fol[i].getName().substring(0, fol[i].getName().lastIndexOf("-")));
                                } else {
                                    j--;
                                }
                            }
                        }
                    } else {
                        System.out.println("NullFolder (?)");
                    }
                    break;
                case OUTBOX:
                    System.out.println("Message Recipient: ");
                    String recipient = sc.nextLine(); //Todo: User vetting; pipe this to a verifyUser function
                    System.out.println("Enter Subject: ");
                    String subject = sc.nextLine();
                    System.out.println("Enter Body: ");
                    String body = sc.nextLine();
                    User.sendMessage(subject, u.getUsername(), recipient, body);
                    menuState = Screen.HOMEPAGE; //State Change: Outbox -> Homepage
                    break;
                case SETTINGS:
                    menuPrint(menuState);
                    input = checkInput(sc.nextLine());
                    break;
                case TERMINATION:
                    System.out.println("Are you sure you want to terminate your account?");
                    menuPrint(Screen.CHOICE);
                    input = checkInput(Screen.CHOICE, sc.nextLine());
                    switch(input[0]) {
                        case "1":
                            removeUser(u);
                            u = null;
                            menuState = Screen.START; //State Change: Account Termination -> Start
                            break;
                        case "2":
                            menuState = Screen.HOMEPAGE; //State Change: Account Termination -> Homepage
                            break;
                    }
                    input[0] = "0";
                    break;
                case MASS_TERMINATION:
                    System.out.println("Are you sure you want to mass terminate (remove all other accounts)?");
                    menuPrint(Screen.CHOICE);
                    input = checkInput(Screen.CHOICE, sc.nextLine());
                    switch(input[0]) {
                        case "1":
                            for(File f : files) {
                                if(f.isDirectory() && !f.getName().equals(u.getUsername())) {
                                    System.out.println(f);
                                    removeUser(f);
                                }
                            }
                            try {
                                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(folder + File.separator + "uids.txt")));
                                bw.write(u.getUID() + "\n");
                                bw.flush();
                                bw.close();
                            } catch(IOException e) {
                                System.out.println("Failed to write out UIDs!");
                            }
                            break;
                        case "2":
                            menuState = Screen.HOMEPAGE; //State Change: Mass Termination -> Homepage
                            break;
                    }
                    input[0] = "0";
                    break;

            }


            switch(menuState) { //Todo: Merge this with ^
                default:
                    menuState = Screen.HOMEPAGE; //State Change: Current Screen -> Homepage
                    break;
                case QUIT:
                    break;
                case START:
                    switch(input[0]) {
                        case "0":
                            break;
                        case "1": //login
                            menuState = Screen.LOGIN; //State Change: Start -> Login
                            break;
                        case "2": //create account
                            menuState = Screen.CREATE; //State Change: Start -> Create
                            break;
                        case "3": //forgot password
                            menuState = Screen.FORGOT_PASSWORD; //State Change: Start -> Forgot Password
                            break;
                        case "4":
                            menuState = Screen.QUIT; //State Change: Start -> Quit
                            break;
                    }
                    break;
                case LOGIN:
                    menuState = Screen.HOMEPAGE; //State Change: Start -> Homepage
                    break;
                case FORGOT_PASSWORD:
                    menuState = Screen.START; //State Change: Forgot Password -> Start
                    break;
                case HOMEPAGE:
                    switch(input[0]) {
                        case "0": //Nothing
                            break;
                        case "1": //Deposit
                            menuState = Screen.DEPOSIT; //State Change: Homepage -> Deposit
                            break;
                        case "2": //Withdraw
                            menuState = Screen.WITHDRAW; //State Change: Homepage -> Withdraw
                            break;
                        case "3": //Transfer
                            menuState = Screen.TRANSFER; //State Change: Homepage -> Transfer
                            break;
                        case "4": //History
                            menuState = Screen.HISTORY; //State Change: Homepage -> History
                            break;
                        case "5": //Inbox
                            menuState = Screen.MESSAGES; //State Change: Homepage -> Messages
                            break;
                        case "6": //Settings
                            menuState = Screen.SETTINGS; //State Change: Homepage -> Settings
                            break;
                        case "7": //Log-Out
                            System.out.println("Logging out!");
                            menuState = Screen.START; //State Change: Homepage -> Start
                            break;
                    }
                    break;
                case TRANSFER:
                    menuState = Screen.HOMEPAGE; //State Change: Transfer -> Homepage
                    break;
                case HISTORY:
                    menuState = Screen.HOMEPAGE; //State Change: Transfer -> Homepage
                    break;
                case CREATE:
                    break;
                case OUTBOX:
                    break;
                case SETTINGS:
                    switch(u.getUserType()) {
                        case NORMAL:
                        case PREMIUM:
                            switch (input[0]) {
                                case "1": //reset pwd

                                    break;
                                case "2":
                                    menuState = Screen.TERMINATION; //State Change: Settings -> Account Termination
                                    break;
                            }
                            break;
                        case ADMIN:
                            switch (input[0]) {
                                case "1": //reset pwd
                                    break;
                                case "2":
                                    menuState = Screen.TERMINATION; //State Change: Settings -> Account Termination
                                    break;
                                case "3":
                                    menuState = Screen.MASS_TERMINATION; //State Change: Settings -> Mass Termination (Admin)
                                    break;
                            }
                            break;
                    }
                    break;
                case MESSAGES:
                    switch(input[0]) {
                        case "1":
                            menuState = Screen.INBOX; //State Change: Messages -> Inbox
                            break;
                        case "2":
                            menuState = Screen.OUTBOX; //State Change: Messages -> Outbox
                            break;
                    }
                    break;
            }
        }
    }
}
