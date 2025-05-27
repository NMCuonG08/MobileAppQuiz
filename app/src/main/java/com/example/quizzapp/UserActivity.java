package com.example.quizzapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizzapp.api.NetworkUtils;
import com.example.quizzapp.managers.SessionManager;

public class UserActivity extends AppCompatActivity {
    private TextView tvUserInfo;
    private Button btnLogout;
    private SessionManager sessionManager;
    private NetworkUtils networkUtils;
    private ImageView btn_main;
    private ImageView btn_chat;

    private ImageView btn_history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        sessionManager = new SessionManager(this);
        networkUtils = new NetworkUtils(this);


        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadUserInfo();
    }

    private void initViews() {
        tvUserInfo = findViewById(R.id.tv_user_info);
        btnLogout = findViewById(R.id.btn_logout);
        btn_main  =findViewById(R.id.btn_home);
        btn_main.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btn_chat = findViewById(R.id.btn_chat);
        btn_chat.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, ChatbotActivity.class);
            startActivity(intent);
        });
        btn_history = findViewById(R.id.history);
        btn_history.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadUserInfo() {
        String userInfo = sessionManager.getUserInfo();
        if (userInfo != null && !userInfo.isEmpty()) {
            tvUserInfo.setText("Thông tin người dùng:\n" + userInfo);
        } else {
            tvUserInfo.setText("Chào mừng bạn đã đăng nhập!");
        }
    }

    private void logout() {
        btnLogout.setEnabled(false);
        btnLogout.setText("Đang đăng xuất...");

        // Gọi API logout nếu server có endpoint logout
        networkUtils.logout(new NetworkUtils.LogoutCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    sessionManager.clearSession();
                    Toast.makeText(UserActivity.this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Vẫn logout local nếu server lỗi
                    sessionManager.clearSession();
                    Toast.makeText(UserActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Không cho phép back về màn hình trước
        // Hoặc có thể hiển thị dialog xác nhận thoát app
        super.onBackPressed();
        moveTaskToBack(true);
    }
}