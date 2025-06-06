package com.example.quizzapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.quizzapp.adapters.ChatAdapter;
import com.example.quizzapp.api.ApiClient;
import com.example.quizzapp.models.Message;
import com.example.quizzapp.ChatHistoryManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;

public class ChatbotActivity extends AppCompatActivity {
    private Context context;
    private EditText messageEditText;
    private ImageButton sendButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private RequestQueue requestQueue;
    private ImageView btn_main;

    private ImageView btn_user;
    private ImageView btn_history;
    private String sessionId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox);

        context = this;

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        btn_main = findViewById(R.id.btn_home);

        sessionId = ChatHistoryManager.createSession(this);
        messageList = ChatHistoryManager.loadSession(this, sessionId);

        chatAdapter = new ChatAdapter(messageList, this);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        requestQueue = Volley.newRequestQueue(this);

        sendButton.setOnClickListener(v -> sendMessage());

        // ✅ Handle Enter key as send
        messageEditText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                sendMessage();
                return true;
            }
            return false;
        });

        btn_main.setOnClickListener(v -> {
            Intent intent = new Intent(ChatbotActivity.this, MainActivity.class);
            startActivity(intent);
        });
        btn_history = findViewById(R.id.history);
        btn_history.setOnClickListener(v -> {
            Intent intent = new Intent(ChatbotActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
        btn_user = findViewById(R.id.btn_user);
        btn_user.setOnClickListener(v -> {
            Intent intent = new Intent(ChatbotActivity.this, UserActivity.class);
            startActivity(intent);
        });

    }

    private void sendMessage() {
        String userMessage = messageEditText.getText().toString().trim();
        if (!userMessage.isEmpty()) {
            messageList.add(new Message(userMessage, "user"));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            chatRecyclerView.scrollToPosition(messageList.size() - 1);
            ChatHistoryManager.saveSession(context, sessionId, messageList);
            messageEditText.setText("");
            fetchBotResponse(userMessage);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_new_chat) {
            new AlertDialog.Builder(this)
                    .setTitle("Reset Chat")
                    .setMessage("Are you sure you want to start a new chat? This will save and clear current messages.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        ChatHistoryManager.saveSession(this, sessionId, messageList);
                        sessionId = ChatHistoryManager.createSession(this);
                        messageList.clear();
                        messageList.addAll(ChatHistoryManager.loadSession(this, sessionId));
                        chatAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;

        } else if (item.getItemId() == R.id.action_open_history) {
            List<String> sessions = ChatHistoryManager.listSessions(this);
            List<String> displayList = new ArrayList<>();

            for (String session : sessions) {
                List<Message> messages = ChatHistoryManager.loadSession(this, session);
                if (!messages.isEmpty()) {
                    displayList.add(messages.get(0).getContent());  // First message shown
                } else {
                    displayList.add("[Empty session]");
                }
            }

            String[] displayArray = displayList.toArray(new String[0]);

            new AlertDialog.Builder(this)
                    .setTitle("Choose Chat Session")
                    .setItems(displayArray, (dialog, which) -> {
                        String selectedSession = sessions.get(which);
                        new AlertDialog.Builder(this)
                                .setTitle("Session Options")
                                .setMessage("Do you want to open or delete this session?")
                                .setPositiveButton("Open", (d, w) -> {
                                    sessionId = selectedSession;
                                    messageList.clear();
                                    messageList.addAll(ChatHistoryManager.loadSession(this, sessionId));
                                    chatAdapter.notifyDataSetChanged();
                                })
                                .setNegativeButton("Delete", (d, w) -> {
                                    ChatHistoryManager.clearSession(this, selectedSession);
                                    if (selectedSession.equals(sessionId)) {
                                        sessionId = ChatHistoryManager.createSession(this);
                                        messageList.clear();
                                        messageList.addAll(ChatHistoryManager.loadSession(this, sessionId));
                                        chatAdapter.notifyDataSetChanged();
                                    }
                                })
                                .setNeutralButton("Cancel", null)
                                .show();
                    })
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchBotResponse(String userMessage) {
        String url = ApiClient.BASE_URL + "api/chatbot/chat";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("query", userMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    String botReply = response.replaceAll("^\"|\"$", "");

                    Markwon markwon = Markwon.create(context);
                    Spanned markdownFormatted = markwon.toMarkdown(botReply);

                    messageList.add(new Message(markdownFormatted.toString(), "bot"));

//                    messageList.add(new Message(botReply, "bot"));

                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                    ChatHistoryManager.saveSession(context, sessionId, messageList);
                },
                error -> {
                    messageList.add(new Message("Error: " + error.toString(), "bot"));
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                    ChatHistoryManager.saveSession(context, sessionId, messageList);
                }
        ) {
            @Override
            public byte[] getBody() {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("query", userMessage);
                    return jsonBody.toString().getBytes("utf-8");
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

// 👇 THÊM ĐOẠN NÀY ĐỂ TĂNG TIMEOUT
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000, // timeout 20 giây
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(stringRequest);

    }
}
