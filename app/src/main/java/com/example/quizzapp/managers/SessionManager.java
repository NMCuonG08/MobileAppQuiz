package com.example.quizzapp.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_USER_INFO = "user_info";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String sessionId, String userInfo) {
        editor.putString(KEY_SESSION_ID, sessionId);
        editor.putString(KEY_USER_INFO, userInfo);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getSessionId() {
        return prefs.getString(KEY_SESSION_ID, null);
    }

    public String getUserInfo() {
        return prefs.getString(KEY_USER_INFO, null);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}