package com.example.serveTest;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Ryan on 7/10/2015.
 */
public class GcmMessageHandler extends IntentService {

  String name;
  String mes;

  private Handler handler;
  public GcmMessageHandler() {
    super("GcmMessageHandler");
  }

  @Override
  public void onCreate() {
    // TODO Auto-generated method stub
    super.onCreate();
    handler = new Handler();
  }
  @Override
  protected void onHandleIntent(Intent intent) {
    Bundle extras = intent.getExtras();

    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
    // The getMessageType() intent parameter must be the intent you received
    // in your BroadcastReceiver.
    String messageType = gcm.getMessageType(intent);

    name = extras.getString("name");
    mes = extras.getString("message");
    showToast();
    Log.i("GCM", "Received : (" +messageType+")  "+extras.getString("message"));

    intent.putExtra("name", name);
    intent.putExtra("message", mes);

    //send broadcast
    getApplicationContext().sendBroadcast(intent);

    GcmBroadcastReceiver.completeWakefulIntent(intent);

  }

  public void showToast(){
    handler.post(new Runnable() {
      public void run() {
        Toast.makeText(getApplicationContext(), "New message: " + mes, Toast.LENGTH_LONG).show();
      }
    });

  }
}