package com.example.mygoappapis.controller;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Random;

public class CommonMethods {


    public static String generateSessionKey(int length)
    {
        String alphabet =
                new String("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"); //9
        int n = alphabet.length(); //10

        String result = new String();
        Random r = new Random(); //11

        for (int i=0; i<length; i++) //12
            result = result + alphabet.charAt(r.nextInt(n)); //13

        return result;
    }

    public static String verifCode(int length)
    {
        String alphabet =
                new String("0123456789"); //9
        int n = alphabet.length(); //10

        String result = new String();
        Random r = new Random(); //11

        for (int i=0; i<length; i++) //12
            result = result + alphabet.charAt(r.nextInt(n)); //13

        return result;
    }

    public static int otpGenerate()
    {
        Random rnd = new Random();
        int n = 100000 + rnd.nextInt(900000);
        return n;
    }

    public static void sendMail(String toMail, String subject, String template)
    {
        String host = "smtp.gmail.com";//change accordingly
        //String to = "saramani95@gmail.com";//change accordingly
        final String user = "mygo2040@gmail.com";//change accordingly
        final String password = "mygomygo1983";//change accordingly
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user, "mygo"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toMail));
            message.setSubject(subject);
            message.setContent(template, "text/html");
            Transport.send(message);

        } catch (MessagingException ex) {
            ex.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String addressToLatLong(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            System.out.println(response.toString());
            return String.valueOf(response);
        } else {
            return "failure";
        }


    }

    public static String fetchLatLongFromJson(String jsonobj)
    {
        JSONObject jsonObj = new JSONObject(jsonobj);
        JSONArray jsonArrayresults = jsonObj.getJSONArray("results");
        JSONObject firstJsonIndex = jsonArrayresults.getJSONObject(0).getJSONObject("geometry");
        JSONObject latlongJsonFormat = firstJsonIndex.getJSONObject("location");
        Double lat1 =  latlongJsonFormat.getDouble("lat");
        Double long1 = latlongJsonFormat.getDouble("lng");

        return String.valueOf(lat1)+","+String.valueOf(long1);

    }



}
