package com.example.serveTest;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyActivity extends Activity {

  private final String USER_AGENT = "Mozilla/5.0";
  //Messaging API Link -- currently set within local network
  private final String BASE_URL = "http://192.168.1.147:8080/";
  private EditText input_name, input_field;
  private TextView message_field;
  private String name;

  //Google Cloud Messaging variables
  private GoogleCloudMessaging gcm;
  private String regid;
  private String PROJECT_NUMBER = "618780476868";
  private Context context;

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

    context  = getApplicationContext();
    //Start listening for GCM events
    context.registerReceiver(mMessageReceiver, new IntentFilter("com.google.android.c2dm.intent.RECEIVE"));

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
    //GCM register
    getRegId();

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
              sendPost(false, input_field.getText().toString());
              input_field.getText().clear();
            } catch (Exception e) {
              Log.e("Failed POST request: ", e.getMessage());
            }

            // TODO Refactor this bit so that it displays new messages correctly.
            // TODO Use Google Cloud Messaging to remove need to constantly check for messages.
            try {
              /*String response = sendGet();
              JSONArray jsonArray = new JSONArray(response);

              for (int i = 0; i < jsonArray.length(); i++) {
                String name = jsonArray.getJSONObject(i).getString("name");
                String message = jsonArray.getJSONObject(i).getString("message");

                message_field.append("\n" + name + " - " + message);
              }*/
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

  public void getRegId(){
    new AsyncTask<Void, Void, String>() {
      @Override
      protected String doInBackground(Void... params) {
        try {
          if (gcm == null) {
            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
          }
          regid = gcm.register(PROJECT_NUMBER);
          Log.i("GCM",  regid);

        } catch (IOException ex) {
          Log.e("GCM", ex.getMessage());

        }
        return regid;
      }

      @Override
      protected void onPostExecute(String msg) {
        try {
          sendPost(true, msg);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }.execute(null, null, null);
  }

  // TODO Refactor GET and SET to remove duplicate code.
  // HTTP GET request
  private String sendGet() throws Exception {
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

  // HTTP POST request
  private void sendPost(boolean init, String msg) throws Exception {
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

  //Unregister receiver once done with app
  @Override
  protected void onDestroy() {
    super.onDestroy();
    context.unregisterReceiver(mMessageReceiver);
  }


  //This is the handler that will manager to process the broadcast intent
  private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {

      // Extract data included in the Intent
      String message = intent.getStringExtra("message");

      appendToMessages(message);
    }
  };

  public void appendToMessages(String message) {
    message_field.append("\n" + name + " - " + message);
  }
}