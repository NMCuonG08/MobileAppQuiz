package com.example.quizzapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.quizzapp.models.Message;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ChatHistoryManager {
    private static final String PREF_NAME       = "chat_history";
    private static final String KEY_SESSIONS    = "session_ids";
    private static final String KEY_PREFIX      = "session_";

    /** Create a new chat session and return its ID */
    public static String createSession(Context ctx) {
        String sessionId = UUID.randomUUID().toString();
        Set<String> sessions = loadSessionIds(ctx);
        sessions.add(sessionId);
        saveSessionIds(ctx, sessions);
        return sessionId;
    }

    /** Return a list of all saved session IDs */
    public static List<String> listSessions(Context ctx) {
        return new ArrayList<>(loadSessionIds(ctx));
    }

    /** Save messages for a specific session */
    public static void saveSession(Context ctx, String sessionId, List<Message> messages) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_PREFIX + sessionId, new Gson().toJson(messages))
                .apply();
    }

    /** Load messages for a specific session (or empty list) */
    public static List<Message> loadSession(Context ctx, String sessionId) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PREFIX + sessionId, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Message>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    /** Delete one session’s history and remove it from the session list */
    public static void clearSession(Context ctx, String sessionId) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_PREFIX + sessionId)
                .apply();
        Set<String> sessions = loadSessionIds(ctx);
        sessions.remove(sessionId);
        saveSessionIds(ctx, sessions);
    }

    /** Internal: load the set of known session IDs */
    private static Set<String> loadSessionIds(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(KEY_SESSIONS, new HashSet<>());
    }

    /** Internal: persist the set of known session IDs */
    private static void saveSessionIds(Context ctx, Set<String> sessions) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putStringSet(KEY_SESSIONS, sessions)
                .apply();
    }
}
