package com.example.quizzapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spanned;
import android.text.SpannedString;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox);

        context = this; // Gán context để dùng ở chỗ khác

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, this);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        requestQueue = Volley.newRequestQueue(this);

        sendButton.setOnClickListener(v -> {
            String userMessage = messageEditText.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                // Thêm message user kiểu SpannedString
                messageList.add(new Message(SpannedString.valueOf(userMessage), "user"));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                chatRecyclerView.scrollToPosition(messageList.size() - 1);

                messageEditText.setText("");
                fetchBotResponse(userMessage);
            }
        });

        btn_main = findViewById(R.id.btn_home);
        btn_main.setOnClickListener(v -> {
            Intent intent = new Intent(ChatbotActivity.this, MainActivity.class);
            startActivity(intent);
        });
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
                    .setMessage("Are you sure you want to start a new chat? This will clear all messages.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        messageList.clear();
                        chatAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Cancel", null)
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

                    messageList.add(new Message(markdownFormatted, "bot"));
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
                },
                error -> {
                    messageList.add(new Message(SpannedString.valueOf("Error: " + error.toString()), "bot"));
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    chatRecyclerView.scrollToPosition(messageList.size() - 1);
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
