package com.example.serveTest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Data Access Object in charge of sending and getting data from the server.
 */
public class DAO {
  private static final String USER_AGENT = "Mozilla/5.0";
  //Messaging API Link -- currently set within local network
  private static final String BASE_URL = "http://192.168.1.147:8080/";

  /**
   * Sends an HTTP GET request to get all messages in the server.
   *
   * @return JSON string containing all messages
   * @throws Exception
   */
  public static String sendGet() throws Exception {
    URL obj = new URL(BASE_URL + "posts");
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    // optional default is GET
    con.setRequestMethod("GET");

    //add request header
    con.setRequestProperty("User-Agent", USER_AGENT);

    int responseCode = con.getResponseCode();

    BufferedReader in = new BufferedReader(
        new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();

    return response.toString();
  }

  /**
   * Sends an HTTP POST request to save data to the server and initiate GCM response
   *
   * @param init true if this POST is for registering client with GCM
   * @param name name of current user
   * @param msg desired message to post
   * @throws Exception
   */
  public static void sendPost(boolean init, String name, String msg) throws Exception {
    URL obj = new URL(BASE_URL + (init ? "register" : "posts"));
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    //add request header
    con.setRequestMethod("POST");
    con.setRequestProperty("User-Agent", USER_AGENT);
    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

    String urlParameters = "{\"name\":\"" + name + "\",\"message\":\"" + msg + "\"}";

    // Send post request
    con.setDoOutput(true);
    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
    wr.writeBytes(urlParameters);
    wr.flush();
    wr.close();

    int responseCode = con.getResponseCode();

    BufferedReader in = new BufferedReader(
        new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();
  }
}
