package com.nathansbud;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.Session;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;


public class Emailer {
    private String email;
    private String password;
    private String name;

    private String host = "smtp.office365.com";
    private int port = 587;


    public Emailer(String _email, String _name, String _password) {
        email = _email;
        name = _name;
        password = _password;
    }

    public Emailer(String jsonPath) {
        loadCredentials(jsonPath);
    }

    public InternetAddress[] makeAddresses(String[] emails) {
        ArrayList<InternetAddress> addresses = new ArrayList<InternetAddress>();
        for(int i = 0; i < emails.length; i++) {
            try {
                addresses.add(new InternetAddress(emails[i]));
            } catch(AddressException e) {
                System.out.println(emails[i] + " is not a valid email address! Skipping...");
            }
        }
        return addresses.toArray(new InternetAddress[0]);
    }


    public void loadCredentials(String jsonPath) {
        JSONParser json = new JSONParser();
        try {
            BufferedReader f = new BufferedReader(new FileReader(new File(jsonPath)));
            JSONObject creds = (JSONObject)json.parse(f);
            f.close();
            email = (String)creds.get("email");
            password = (String)creds.get("password");
        } catch(ParseException | IOException e) {
            System.out.println("Creation of emailer failed due to bad credentials!");
        }


    }

    public void sendEmail(String subject, String body, String... recipients) {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(email, name));
            msg.setRecipients(Message.RecipientType.TO, makeAddresses(recipients));
            msg.setSubject(subject);
            msg.setContent(body, "text/html");

            Transport.send(msg, email, password);
        } catch(UnsupportedEncodingException | MessagingException e) {
            System.out.println("Something went wrong ya dork");
            e.printStackTrace();
        }
    }

    public void sendResetEmail(String email) {
        if(exists(email)) {
            new Thread(()->{
                sendEmail("Password Reset", "Insert password reset instructions here!", email); //Todo: Make this multithreaded so that it's non-blocking!
            }).start(); //Email send thread
            System.out.println("Email sent!");
        } else {
            System.out.println("Email does not exist!");
        }
    }

    public static boolean isValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);

        if (email == null) return false;
        return pat.matcher(email).matches();
    } //Stolen from https://www.geeksforgeeks.org/check-email-address-valid-not-java/
    public static boolean exists(String email) {
        return getAllEmails().contains(email);
    }

    public static ArrayList<String> getAllEmails() {
        ArrayList<String> emails = new ArrayList<String>();
        JSONParser json = new JSONParser();

        for(File f : BankProject.getFiles()) {
            if (f.isDirectory()) {
                try {
                    BufferedReader b = new BufferedReader(new FileReader(new File(f + File.separator + "user.json")));
                    JSONObject userJson = (JSONObject)json.parse(b);
                    b.close();
                    if (userJson.containsKey("email")) {
                        emails.add((String) userJson.get("email"));
                    }
                } catch (ParseException | IOException e) {
                    System.out.println("Failed to read emails");
                }
            }
        }
        return emails;
    }
}
