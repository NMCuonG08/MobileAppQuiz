package com.example.quizzapp.api;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class GeminiApi {

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=AIzaSyDlm3D6a-4XZ2UWPU1NPnCCxo4WiybNyXU";

    public interface GeminiCallback {
        void onResponse(String reply);
        void onError(String error);
    }

    public static void sendMessage(String userMessage, GeminiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject part = new JSONObject().put("text", userMessage);
            JSONArray parts = new JSONArray().put(part);
            JSONObject content = new JSONObject().put("parts", parts);
            JSONArray contents = new JSONArray().put(content);
            JSONObject body = new JSONObject().put("contents", contents);

            RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError("Error Code: " + response.code());
                        return;
                    }

                    String responseBody = response.body().string();

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        JSONArray candidates = json.getJSONArray("candidates");
                        JSONObject candidate = candidates.getJSONObject(0);
                        JSONObject content = candidate.getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        String reply = parts.getJSONObject(0).getString("text");

                        callback.onResponse(reply);
                    } catch (Exception e) {
                        callback.onError("Parsing error: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("JSON error: " + e.getMessage());
        }
    }
}
