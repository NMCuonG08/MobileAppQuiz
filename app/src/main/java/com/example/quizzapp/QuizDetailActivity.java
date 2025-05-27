package com.example.quizzapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.quizzapp.api.ApiClient;
import com.example.quizzapp.api.ApiInterface;
import com.example.quizzapp.models.QuizDetail;
import com.example.quizzapp.wrapper.QuizDetailResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizDetailActivity extends AppCompatActivity {
    private static final String TAG = "QuizDetailActivity";
    private ImageView quizImageView;
    private TextView titleTextView, descriptionTextView, questionCountTextView;
    private TextView categoryTextView, authorTextView, ratingTextView;
    private ProgressBar progressBar;

    private Button btn_start;


    private String quizId;
    private String quizTitle;

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
        quizTitle = getIntent().getStringExtra("QUIZ_TITLE");
        if (quizId == null || quizId.isEmpty()) {
            Toast.makeText(this, "Invalid quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch quiz details
        fetchQuizDetail();

        btn_start = findViewById(R.id.startQuizButton);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để mở QuizPlayActivity
                Intent intent = new Intent(QuizDetailActivity.this, QuizPlayActivity.class);
                intent.putExtra("QUIZ_ID", quizId);
                intent.putExtra("QUIZ_TITLE", quizTitle);
                startActivity(intent);
            }
        });

    }

    private void fetchQuizDetail() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Fetching quiz details for ID: " + quizId);

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<QuizDetailResponse> call = apiInterface.getQuizDetailById(quizId);

        call.enqueue(new Callback<QuizDetailResponse>() {
            @Override
            public void onResponse(Call<QuizDetailResponse> call, Response<QuizDetailResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    QuizDetailResponse quizResponse = response.body();
                    displayQuizDetail(quizResponse);
                }else {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    showError("Lỗi khi lấy chi tiết quiz");
                }
            }

            @Override
            public void onFailure(Call<QuizDetailResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Network error", t);
                showError("Lỗi mạng: " + t.getMessage());
            }
        });
    }


    private void displayQuizDetail(QuizDetailResponse quizResponse) {
        QuizDetail quizDetail = quizResponse.getQuizze();

        titleTextView.setText(quizDetail.getTitle());
        descriptionTextView.setText(quizDetail.getDescription());
        categoryTextView.setText(quizResponse.getCategoryName());
        authorTextView.setText("By " + quizResponse.getAuthor());
        ratingTextView.setText(quizDetail.getAverageRating() + " ★");

        // Sử dụng questionNumber từ QuizDetailResponse
        int questionCount = quizResponse.getQuestionNumber();
        questionCountTextView.setText(questionCount > 0 ? questionCount + " Questions" : "Unknown");

        // Load image với Glide
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