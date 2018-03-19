package com.missingcontroller.huelightsreminder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new CheckLights().execute();
    }

    class CheckLights extends AsyncTask<String, Void, Void> {

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

                Log.wtf(TAG, a.toString());

                JSONObject jsonObject = new JSONObject(a.toString());
                JSONArray jsonArray = jsonObject.getJSONArray("lights");

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(jsonArray.getJSONObject(0));

                Log.wtf(TAG, "First Object: " + json);

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
