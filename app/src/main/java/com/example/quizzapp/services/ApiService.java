package com.example.quizzapp.services;

import okhttp3.*;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiService {
    private static final String BASE_URL = "http://your-server.com"; // Thay đổi URL của bạn
    private OkHttpClient client;
    private Gson gson;
    private CookieJar cookieJar;

    public ApiService() {
        // Tạo CookieJar để lưu session cookies
        cookieJar = new CookieJar() {
            private final java.util.HashMap<String, java.util.List<Cookie>> cookieStore = new java.util.HashMap<>();

            @Override
            public void saveFromResponse(HttpUrl url, java.util.List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
            }

            @Override
            public java.util.List<Cookie> loadForRequest(HttpUrl url) {
                java.util.List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new java.util.ArrayList<>();
            }
        };

        client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        gson = new Gson();
    }

    public interface LoginCallback {
        void onSuccess(LoginResponse response);
        void onError(String error);
    }

    public void login(String username, String password, LoginCallback callback) {
        // Tạo JSON body
        LoginRequest loginRequest = new LoginRequest(username, password);
        String jsonBody = gson.toJson(loginRequest);

        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "/login") // Thay đổi endpoint của bạn
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    LoginResponse loginResponse = gson.fromJson(responseBody, LoginResponse.class);
                    callback.onSuccess(loginResponse);
                } else {
                    callback.onError("Login failed: " + response.message());
                }
            }
        });
    }

    // Gọi API với session đã lưu
    public void makeAuthenticatedRequest(String endpoint, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
