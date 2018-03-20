package com.missingcontroller.huelightsreminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "TEST";
    public String loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new CheckLights().execute();

        //SendNotification("Kitchen");


    }

    private void SendNotification(String loc) {
        Resources r = getResources();
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle("You left a light on.")
                .setContentText("You left the " + loc + " light on.")
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);

        new CheckLights().execute();
    }

    private void Timer(final String loc) {
        new CountDownTimer(300000, 1000) {

            public void onTick(long millisUntilFinished) {
                Log.wtf(TAG, "Time is " + millisUntilFinished);
            }

            public void onFinish() {
                SendNotification(loc);
            }
        }.start();
    }

    class CheckLights extends AsyncTask<String, Void, Void> {

        private Runnable RunTimer = new Runnable() {
            @Override
            public void run() {

                Timer(loc);
            }
        };

        protected Void doInBackground(String... urls) {

            try {
                // Defined URL  where to send data
                URL url = null;
                try {
                    url = new URL("http://192.168.1.154/api/ThbE9KYmC8DxS9C1AhdTiL6Fk3WSpTwODd0ATzOf");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                Log.wtf(TAG, "URL = " + url.toString());

                URLConnection kb = null;
                kb = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        kb.getInputStream(), "UTF-8"));

                String inputLine;
                StringBuilder a = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    a.append(inputLine);
                }
                in.close();

                //Log.wtf(TAG, a.toString());

                JSONObject jsonObject = new JSONObject(a.toString());
                JSONObject lights = jsonObject.getJSONObject("lights");

                for (int i = 0; i < lights.length(); i++) {
                    JSONObject light = lights.getJSONObject(String.valueOf(i + 1));
                    JSONObject state = light.getJSONObject("state");
                    Log.wtf(TAG, "Lights: (" + (i + 1) + ") " + state.get("on"));
                    if (!state.getBoolean("on")) {
                        switch (i + 1) {
                            case 1:
                            case 5:
                            case 2:
                                Log.wtf(TAG, "Bedroom Light " + (1 + i) + " is off");
                                break;
                            case 3:
                                Log.wtf(TAG, "Kitchen Light is off");
                                break;
                            case 4:
                                Log.wtf(TAG, "Living Room Light is off");
                                break;
                        }
                    } else {
                        switch (i + 1) {
                            case 1:
                            case 5:
                            case 2:
                                loc = "Bedroom";
                                runOnUiThread(RunTimer);
                                break;
                            case 3:
                                loc = "Kitchen";
                                runOnUiThread(RunTimer);
                                break;
                            case 4:
                                loc = "Living Room";
                                runOnUiThread(RunTimer);
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                Log.wtf(TAG, e.toString());
            }

            return null;
        }

        protected void onPostExecute(String feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }
}
