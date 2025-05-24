package com.example.quizzapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.quizzapp.api.ApiClient;
import com.example.quizzapp.api.ApiInterface;
import com.example.quizzapp.models.QuizDetail;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizDetailActivity extends AppCompatActivity {
    private static final String TAG = "QuizDetailActivity";
    private ImageView quizImageView;
    private TextView titleTextView, descriptionTextView, questionCountTextView;
    private TextView categoryTextView, authorTextView, ratingTextView;
    private ProgressBar progressBar;
    private String quizId; // Thay đổi từ long thành String vì MongoDB sử dụng ObjectId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_detail);

        // Initialize views
        quizImageView = findViewById(R.id.quizDetailImageView);
        titleTextView = findViewById(R.id.quizDetailTitle);
        descriptionTextView = findViewById(R.id.quizDetailDescription);
        questionCountTextView = findViewById(R.id.questionCountText);
        categoryTextView = findViewById(R.id.categoryText);
        authorTextView = findViewById(R.id.authorText);
        ratingTextView = findViewById(R.id.ratingText);
        progressBar = findViewById(R.id.detailProgressBar);

        // Get quiz ID from intent
        quizId = getIntent().getStringExtra("QUIZ_ID");
        if (quizId == null || quizId.isEmpty()) {
            Toast.makeText(this, "Invalid quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch quiz details
        fetchQuizDetail();
    }

    private void fetchQuizDetail() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Fetching quiz details for ID: " + quizId);

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<QuizDetail> call = apiInterface.getQuizDetailById(quizId);

        call.enqueue(new Callback<QuizDetail>() {
            @Override
            public void onResponse(Call<QuizDetail> call, Response<QuizDetail> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Quiz details received successfully");
                    displayQuizDetail(response.body());
                } else {
                    Log.e(TAG, "Error code: " + response.code() + " message: " + response.message());
                    showError("Error fetching quiz details");
                }
            }

            @Override
            public void onFailure(Call<QuizDetail> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void displayQuizDetail(QuizDetail quizDetail) {
        titleTextView.setText(quizDetail.getTitle());
        descriptionTextView.setText(quizDetail.getDescription());
        categoryTextView.setText(quizDetail.getCategoryName());
        authorTextView.setText("By " + quizDetail.getCreatorName());
        ratingTextView.setText(quizDetail.getAverageRating() + " ★");

        // Hiển thị số lượng câu hỏi nếu có
        int questionCount = quizDetail.getQuestionCount();
        questionCountTextView.setText(questionCount > 0 ? questionCount + " Questions" : "Unknown");

        // Load image with Glide
        if (quizDetail.getImageUrl() != null && !quizDetail.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(quizDetail.getImageUrl())
                    .placeholder(R.drawable.quiz_placeholder)
                    .error(R.drawable.quiz_placeholder)
                    .into(quizImageView);
        } else {
            quizImageView.setImageResource(R.drawable.quiz_placeholder);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}