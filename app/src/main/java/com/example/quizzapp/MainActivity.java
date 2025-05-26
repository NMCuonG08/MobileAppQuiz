package com.example.quizzapp;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.quizzapp.adapters.ChatAdapter;
import com.example.quizzapp.adapters.QuizAdapter;
import com.example.quizzapp.api.ApiClient;
import com.example.quizzapp.api.ApiInterface;
import com.example.quizzapp.models.Message;
import com.example.quizzapp.models.Quiz;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private QuizAdapter quizAdapter;
    private ProgressBar progressBar;
    private List<Quiz> quizList = new ArrayList<>();

    private BottomNavigationView bottomNavigationView;

    private ImageView btn_chat;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // <-- Layout phải được inflate trước

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerViewQuizzes);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        quizAdapter = new QuizAdapter(quizList, this);
        recyclerView.setAdapter(quizAdapter);

        fetchQuizzes();

       btn_chat = findViewById(R.id.btn_chat);
       btn_chat.setOnClickListener(v -> {
           Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
           startActivity(intent);
       });


    }

    private void fetchQuizzes() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Fetching quizzes...");

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        // Sử dụng API endpoint đã được điều chỉnh không cần userId
        Call<List<Quiz>> call = apiInterface.getQuizzes();

        call.enqueue(new Callback<List<Quiz>>() {
            @Override
            public void onResponse(Call<List<Quiz>> call, Response<List<Quiz>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Received " + response.body().size() + " quizzes");
                    quizList.clear();
                    quizList.addAll(response.body());
                    quizAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error code: " + response.code() + " message: " + response.message());
                    showError("Error fetching quizzes: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Quiz>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}