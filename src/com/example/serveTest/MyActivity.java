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

import org.json.JSONArray;

import java.io.IOException;

public class MyActivity extends Activity {
  private TextView message_field;
  private String name;

  //Google Cloud Messaging variables
  private GoogleCloudMessaging gcm;
  private String regid;
  private String PROJECT_NUMBER = "618780476868";
  private Context context;

  private Thread initMessage = new Thread(new Runnable() {
    public void run() {
      loadAllMessages();
    }
  });

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



    context = getApplicationContext();
    //Start listening for GCM events
    context.registerReceiver(mMessageReceiver, new IntentFilter("com.google.android.c2dm.intent"
                                                                 + ".RECEIVE"));

    EditText input_name = (EditText)findViewById(R.id.name);
    Button loginButton = (Button) findViewById(R.id.login);

    loginButton.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View view) {
            name = input_name.getText().toString();
            if(name.isEmpty()) {
              return;
            }

            hideVirtualKeyboard();
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
    message_field = (TextView)findViewById(R.id.messages);
    initMessage.start();

    //GCM register
    if(regid == null){
      getRegId();
    }


    EditText input_field = (EditText)findViewById(R.id.sendText);
    Button sendButton = (Button)findViewById(R.id.button);


    try {
      initMessage.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    sendButton.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View view) {
            hideVirtualKeyboard();

            if(input_field.getText().toString().isEmpty()) {
              return;
            }

            try {
              DAO.sendPost(false, name, input_field.getText().toString());
              input_field.getText().clear();
            } catch (Exception e) {
              Log.e("Failed POST request: ", e.getMessage());
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

  // Writes all messages currently stored in the server to the message_field text area
  private void loadAllMessages() {
    try {
      String response = DAO.sendGet();
      if (response == null) {
        return;
      }

      JSONArray jsonArray = new JSONArray(response);

      for (int i = 0; i < jsonArray.length(); i++) {
        String name = jsonArray.getJSONObject(i).getString("name");
        String message = jsonArray.getJSONObject(i).getString("message");

        appendToMessages(name, message);
      }
    } catch (Exception e) {
      Log.e("Failed GET request:", e.getMessage());
    }
  }

  // Registers the client with the server.
  private void getRegId(){
    new AsyncTask<Void, Void, String>() {
      @Override
      protected String doInBackground(Void... params) {
        try {
          if (gcm == null) {
            gcm = GoogleCloudMessaging.getInstance(context);
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
          DAO.sendPost(true, name, msg);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }.execute(null, null, null);
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
      appendToMessages(intent.getStringExtra("name"), intent.getStringExtra("message"));
    }
  };

  // Appends the given name and message to message wall
  public void appendToMessages(String name, String message) {
    message_field.append("\n" + name + " - " + message);
  }

  // Removes virtual keyboard from screen.
  private void hideVirtualKeyboard() {
    try {
      InputMethodManager inputManager = (InputMethodManager)
          getSystemService(Context.INPUT_METHOD_SERVICE);
      inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                           InputMethodManager.HIDE_NOT_ALWAYS);
    }
    catch (NullPointerException e) {
      Log.e("Error Hiding Keyboard: ", e.getMessage());
    }
  }
}