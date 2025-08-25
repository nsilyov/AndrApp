package com.example.andrapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.andrapp.bluetooth.presentation.BluetoothActivity;
import com.example.andrapp.legacy.UserSessionManager;
import com.example.andrapp.maps.MapsActivity;
import com.example.andrapp.legacy.User;
import com.example.andrapp.webview.WebViewActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String URL_TO_FETCH = "https://jsonplaceholder.typicode.com/todos/1";

    private TextView tvNetworkResult;
    private Button btnFetchData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOpenMaps = findViewById(R.id.btn_open_maps);
        Button btnOpenWebView = findViewById(R.id.btn_open_webview);
        Button btnOpenBluetooth = findViewById(R.id.btn_open_bluetooth);
        tvNetworkResult = findViewById(R.id.tv_network_result);
        btnFetchData = findViewById(R.id.btn_fetch_data);

        btnOpenMaps.setOnClickListener(v  -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);

            User currentUser = UserSessionManager.getInstance(this).getLoggedInUser();
            if (currentUser != null) {
                intent.putExtra("USER_EXTRA", currentUser);
            }

            startActivity(intent);
        });

        btnOpenWebView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            startActivity(intent);
        });

        btnOpenBluetooth.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
            startActivity(intent);
        });

        btnFetchData.setOnClickListener(v -> {
            tvNetworkResult.setText(R.string.fetching_network_data);
            new FetchDataTask().execute(URL_TO_FETCH);
            btnFetchData.setEnabled(false);
        });


        manageUserSession();
    }

    private void manageUserSession() {
        UserSessionManager sessionManager = UserSessionManager.getInstance(this);

        if (sessionManager.getLoggedInUser() == null) {
            Log.d(TAG, "No user session found. Creating a new one.");
            User mockUser = new User("user123", "LegacyUser");
            sessionManager.saveUserSession(mockUser);
            Toast.makeText(this, getString(R.string.created_new_session, mockUser.getUsername()),  Toast.LENGTH_SHORT).show();
        } else {
            User currentUser = sessionManager.getLoggedInUser();
            Log.d(TAG, "Existing user session found for: " + currentUser.getUsername());
            Toast.makeText(this, getString(R.string.welcome_back, currentUser.getUsername()), Toast.LENGTH_SHORT).show();
        }
    }

    private class FetchDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            if (urls.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            } catch (Exception e) {
                Log.e(TAG, "Error fetching data", e);
                return getString(R.string.error_prefix, e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                tvNetworkResult.setText(getString(R.string.network_result_prefix, result));
                tvNetworkResult.setTextColor(getResources().getColor(android.R.color.black));
            } else {
                tvNetworkResult.setText(R.string.failed_to_fetch_data);
                tvNetworkResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }
    }
}