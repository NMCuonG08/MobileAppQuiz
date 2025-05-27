package com.example.quizzapp.api;

import android.content.Context;
import android.util.Log;

import com.example.quizzapp.managers.SessionManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkUtils {
    private static final String TAG = "Network";
    private static final String BASE_URL = "http://10.0.2.2:5000/"; // Thay đổi URL server của bạn
    private static final String LOGIN_ENDPOINT = "api/users/login";
    private static final String LOGOUT_ENDPOINT = "/api/logout";

    private Context context;
    private SessionManager sessionManager;
    private ExecutorService executor;

    public NetworkUtils(Context context) {
        this.context = context;
        this.sessionManager = new SessionManager(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public interface LoginCallback {
        void onSuccess(String sessionId, String userInfo);
        void onError(String error);
    }

    public interface LogoutCallback {
        void onSuccess();
        void onError(String error);
    }

    public void login(String email, String password, LoginCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(BASE_URL + LOGIN_ENDPOINT);
                Log.d(TAG, "Login URL: " + url.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Tạo JSON data
                String jsonInputString = String.format("{\"email\":\"%s\",\"password\":\"%s\"}",
                        email, password);
                Log.d(TAG, "Request body: " + jsonInputString);

                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                    wr.writeBytes(jsonInputString);
                    wr.flush();
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Đọc response body
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    Log.d(TAG, "Success response: " + response.toString());

                    // Parse JWT token từ response JSON
                    String token = parseTokenFromResponse(response.toString());
                    String userInfo = parseUserInfoFromResponse(response.toString());

                    callback.onSuccess(token, userInfo);
                } else {
                    // ĐỌC CHI TIẾT LỖI TỪ SERVER
                    BufferedReader errorReader;
                    if (conn.getErrorStream() != null) {
                        errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    } else {
                        errorReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    }

                    String inputLine;
                    StringBuilder errorResponse = new StringBuilder();
                    while ((inputLine = errorReader.readLine()) != null) {
                        errorResponse.append(inputLine);
                    }
                    errorReader.close();

                    String errorMessage = errorResponse.toString();
                    Log.e(TAG, "Error response code: " + responseCode);
                    Log.e(TAG, "Error response body: " + errorMessage);
                    Log.e(TAG, "Request URL: " + url.toString());
                    Log.e(TAG, "Request body: " + jsonInputString);

                    callback.onError("HTTP " + responseCode + ": " + errorMessage);
                }

            } catch (Exception e) {
                Log.e(TAG, "Login exception: " + e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        });
    }

    public void logout(LogoutCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(BASE_URL + LOGOUT_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");

                // Gửi JWT token trong Authorization header
                String token = sessionManager.getSessionId(); // Lưu JWT token như sessionId
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    callback.onSuccess();
                } else {
                    callback.onError("HTTP " + responseCode);
                }

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    private String parseTokenFromResponse(String jsonResponse) {
        try {
            // Parse JSON đơn giản để lấy token
            int tokenStart = jsonResponse.indexOf("\"token\":\"") + 9;
            int tokenEnd = jsonResponse.indexOf("\"", tokenStart);
            return jsonResponse.substring(tokenStart, tokenEnd);
        } catch (Exception e) {
            return null;
        }
    }

    private String parseUserInfoFromResponse(String jsonResponse) {
        try {
            // Parse JSON để lấy thông tin user
            int userStart = jsonResponse.indexOf("\"user\":");
            int userEnd = jsonResponse.lastIndexOf("}");
            String userJson = jsonResponse.substring(userStart + 7, userEnd);

            // Tạo thông tin user đơn giản
            String name = extractJsonValue(userJson, "name");
            String email = extractJsonValue(userJson, "email");
            String role = extractJsonValue(userJson, "role");

            return String.format("Tên: %s\nEmail: %s\nVai trò: %s", name, email, role);
        } catch (Exception e) {
            return "Thông tin người dùng";
        }
    }

    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":\"";
            int start = json.indexOf(searchKey) + searchKey.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }

    // Method để gọi API với session
    public void makeAuthenticatedRequest(String endpoint, AuthenticatedCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(BASE_URL + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");

                // Gửi JWT token
                String token = sessionManager.getSessionId();
                if (token != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    callback.onSuccess(response.toString());
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    callback.onUnauthorized();
                } else {
                    callback.onError("HTTP " + responseCode);
                }

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public interface AuthenticatedCallback {
        void onSuccess(String response);
        void onError(String error);
        void onUnauthorized(); // Session hết hạn
    }
}