package com.example.quizzapp;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quizzapp.adapters.ChatAdapter;
import com.example.quizzapp.adapters.QuizAdapter;
import com.example.quizzapp.api.ApiClient;
import com.example.quizzapp.api.ApiInterface;
import com.example.quizzapp.models.Message;
import com.example.quizzapp.models.Quiz;
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
    private EditText searchEditText;
    private List<Quiz> allQuizzes = new ArrayList<>();
    private List<Quiz> filteredQuizzes = new ArrayList<>();
    private ImageView btn_user;
    private ImageView btn_chat;

    private ImageView btn_history;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerViewQuizzes);
        searchEditText = findViewById(R.id.searchEditText);

        // Changed to GridLayoutManager with 2 columns
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        quizAdapter = new QuizAdapter(filteredQuizzes, this);
        recyclerView.setAdapter(quizAdapter);

        // Set up search functionality
        setupSearch();

        fetchQuizzes();

        btn_user  =findViewById(R.id.btn_user);
        btn_user.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(intent);
        });

        btn_chat = findViewById(R.id.btn_chat);
        btn_chat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
            startActivity(intent);
        });
        btn_history = findViewById(R.id.history);
        btn_history.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

    }

    private void setupSearch() {
        // Handle IME search button
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filterQuizzes(searchEditText.getText().toString());
                return true;
            }
            return false;
        });

        // Handle text changes for real-time search
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter as user types
                filterQuizzes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void filterQuizzes(String query) {
        List<Quiz> filteredResults = new ArrayList<>();

        if (query.isEmpty()) {
            // If search is empty, show all quizzes
            filteredResults.addAll(allQuizzes);
        } else {
            // Filter quizzes by title or description containing the query
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Quiz quiz : allQuizzes) {
                if ((quiz.getTitle() != null && quiz.getTitle().toLowerCase().contains(lowerCaseQuery)) ||
                        (quiz.getDescription() != null && quiz.getDescription().toLowerCase().contains(lowerCaseQuery))) {
                    filteredResults.add(quiz);
                }
            }
        }

        // Use the updateQuizList method instead of modifying the adapter's list directly
        quizAdapter.updateQuizList(filteredResults);
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

                    // Store all quizzes for filtering
                    allQuizzes.clear();
                    allQuizzes.addAll(response.body());

                    // Initialize with all quizzes
                    filteredQuizzes.clear();
                    filteredQuizzes.addAll(allQuizzes);

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