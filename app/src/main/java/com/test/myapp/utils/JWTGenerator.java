package com.test.myapp.utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.test.myapp.JoinSessionActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class JWTGenerator extends AsyncTask<Void, Void, String> {
    private String SAMPLE_SESSION_NAME = "newsession";
    private String SAMPLE_SESSION_PWD = "zoom";
    private String SAMPLE_USER_IDENTITY = "zoom dev";
    private int SAMPLE_ROLE = 1;
    private String myURL = "https://ngrok.app";
    private ProgressDialog progressDialog;
    private JoinSessionActivity jsa;
    private String JSON_PAYLOAD = "{\"sessionName\":\"" + SAMPLE_SESSION_NAME +
            "\",\"role\":" + SAMPLE_ROLE +
            ",\"userIdentity\":\"" + SAMPLE_USER_IDENTITY +
            "\",\"geoRegions\":\"US,AU,CA,IN,CN,BR,MX,HK,SG,JP,DE,NL\"," +
            "\"cloudRecordingOption\":1,\"cloudRecordingElection\":0}";

    public JWTGenerator(JoinSessionActivity joinSessionActivity) {
        jsa = joinSessionActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(jsa,
                "Fetching Token...", "");
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL(myURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = JSON_PAYLOAD.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            connection.getOutputStream().close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } else {
                return "Error: " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
        } catch (JSONException e) {
            Log.d("sample", e.toString());
            throw new RuntimeException(e);
        }
        progressDialog.dismiss();

        // Retrieve the "signature" parameter
        String signature = jsonObject.optString("signature");
        jsa.onPostExecution(signature);
    }
}
