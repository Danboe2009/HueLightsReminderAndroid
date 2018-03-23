package com.missingcontroller.huelightsreminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Danboe on 3/21/2018.
 */

public class ServiceClass extends Service {

    private static String TAG = "Service";
    public String loc;
    public int num;
    public CountDownTimer countDownTimer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void SendNotification(String loc) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Intent intent = new Intent(this, ServiceClass.class);
            intent.putExtra("NotiClick", true);
            intent.putExtra("Number", num);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Resources r = getResources();
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle("You left a light on.")
                    .setContentText("You left the " + loc + " light on.")
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .build();


            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(num, notification);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("NotiClick", true);
            intent.putExtra("Number", num);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Sets an ID for the notification, so it can be updated.
            int notifyID = 1;
            String CHANNEL_ID = "my_channel_01";// The id of the channel.
            CharSequence name = getString(R.string.app_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
// Create a notification and set the notification channel.
            Notification notification = new Notification.Builder(ServiceClass.this)
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle("You left a light on.")
                    .setContentText("You left the " + loc + " light on.")
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setChannelId(CHANNEL_ID)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(num, notification);
        }


        Log.wtf(TAG, "Notification Sent: " + num + " " + loc);
    }

    private void Timer() {
        countDownTimer.cancel();
        countDownTimer.start();
    }

    class CheckLights extends AsyncTask<String, Void, Void> {

        private Runnable RunNoti = new Runnable() {
            @Override
            public void run() {
                SendNotification(loc);
                Timer();
            }
        };

        private Runnable RunTimer = new Runnable() {
            @Override
            public void run() {
                Timer();
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
                        RunTimer.run();
                        //runOnUiThread(RunTimer);
                    } else if (state.getBoolean("on")) {
                        switch (i + 1) {
                            case 1:
                            case 5:
                            case 2:
                                loc = "Bedroom";
                                num = 1;
                                Log.wtf(TAG, "Bedroom Light " + (1 + i) + " is on");
                                RunNoti.run();
                                break;
                            case 3:
                                loc = "Kitchen";
                                num = 2;
                                Log.wtf(TAG, "Kitchen Light is on");
                                RunNoti.run();
                                break;
                            case 4:
                                loc = "Living Room";
                                num = 3;
                                Log.wtf(TAG, "Living Room Light is on");
                                RunNoti.run();
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
