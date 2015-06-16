package com.example.serveTest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyActivity extends Activity {

  private final String USER_AGENT = "Mozilla/5.0";
  private EditText input_name, input_field;
  private TextView message_field;
  private String name;

  /**
   * Called when the activity is first created.
   *
   * Opens "login" screen which sets the users name and sends them into the chat-room.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //Remove the header bar
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setContentView(R.layout.login);

    input_name = (EditText)findViewById(R.id.name);

    Button loginButton = (Button) findViewById(R.id.login);
    loginButton.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View view) {
            name = input_name.getText().toString();
            if(name.isEmpty()) {
              return;
            }

            //Hide the keyboard
            try {
              InputMethodManager inputManager = (InputMethodManager)
                  getSystemService(Context.INPUT_METHOD_SERVICE);
              inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                                   InputMethodManager.HIDE_NOT_ALWAYS);
            }
            catch (NullPointerException e) {
              Log.e("Error Hiding Keyboard: ", e.getMessage());
            }
            startChatView();
          }
        }
    );

  }

  /**
   * Sends user into chat room and allows them to send / receive messages.
   */
  public void startChatView() {
    setContentView(R.layout.main);

    input_field = (EditText)findViewById(R.id.sendText);
    message_field = (TextView)findViewById(R.id.messages);



    Button sendButton = (Button)findViewById(R.id.button);
    sendButton.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View view) {
            if(input_field.getText().toString().isEmpty()) {
              return;
            }
            //Hide the keyboard
            try {
              InputMethodManager inputManager = (InputMethodManager)
                  getSystemService(Context.INPUT_METHOD_SERVICE);
              inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                                   InputMethodManager.HIDE_NOT_ALWAYS);
            }
            catch (NullPointerException e) {
              Log.e("Error Hiding Keyboard: ", e.getMessage());
            }

            try {
              sendPost(input_field.getText().toString());
              input_field.getText().clear();
            } catch (Exception e) {
              Log.e("Failed POST request: ", e.getMessage());
            }

            // TODO Refactor this bit so that it displays new messages correctly.
            // TODO Use Google Cloud Messaging to remove need to constantly check for messages.
            try {
              String response = sendGet();
              JSONArray jsonArray = new JSONArray(response);

              for (int i = 0; i < jsonArray.length(); i++) {
                String name = jsonArray.getJSONObject(i).getString("name");
                String message = jsonArray.getJSONObject(i).getString("message");

                message_field.append("\n" + name + " - " + message);
              }
            } catch (Exception e) {
              Log.e("Failed GET request:", e.getMessage());
            }

            final ScrollView scrollview = ((ScrollView) findViewById(R.id.scrollView));
            scrollview.postDelayed(new Runnable() {
              @Override
              public void run() {
                scrollview.setSmoothScrollingEnabled(true);
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
              }
            }, 100);
          }
        });


    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
  }

  // HTTP GET request
  private String sendGet() throws Exception {
    //Messaging API Link -- currently set within local network
    String url = "http://192.168.0.10:8080/posts";

    URL obj = new URL(url);
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

  // HTTP POST request
  private void sendPost(String msg) throws Exception {
    //Messaging API Link -- currently set within local network
    String url = "http://192.168.0.10:8080/posts";

    URL obj = new URL(url);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    //add reuqest header
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