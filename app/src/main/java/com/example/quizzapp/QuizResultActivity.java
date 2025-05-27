package com.example.quizzapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class QuizResultActivity extends AppCompatActivity {

    private TextView resultIcon;
    private TextView resultTitle;
    private TextView resultSubtitle;
    private TextView totalScoreText;
    private TextView percentageText;
    private TextView correctAnswersText;
    private TextView incorrectAnswersText;
    private TextView totalQuestionsText;
    private Button playAgainButton;
    private Button backToMenuButton;

    private int totalScore;
    private int correctAnswers;
    private int totalQuestions;
    private int percentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        initializeViews();
        getResultData();
        displayResults();
        setupButtonListeners();
    }

    private void initializeViews() {
        resultIcon = findViewById(R.id.resultIcon);
        resultTitle = findViewById(R.id.resultTitle);
        resultSubtitle = findViewById(R.id.resultSubtitle);
        totalScoreText = findViewById(R.id.totalScoreText);
        percentageText = findViewById(R.id.percentageText);
        correctAnswersText = findViewById(R.id.correctAnswersText);
        incorrectAnswersText = findViewById(R.id.incorrectAnswersText);
        totalQuestionsText = findViewById(R.id.totalQuestionsText);
        playAgainButton = findViewById(R.id.playAgainButton);
        backToMenuButton = findViewById(R.id.backToMenuButton);
    }

    private void getResultData() {
        Intent intent = getIntent();
        totalScore = intent.getIntExtra("TOTAL_SCORE", 0);
        correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", 0);
        totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0);

        if (totalQuestions > 0) {
            percentage = (correctAnswers * 100) / totalQuestions;
        } else {
            percentage = 0;
        }
    }

    private void displayResults() {
        // Set score and statistics
        totalScoreText.setText(String.valueOf(totalScore));
        percentageText.setText(percentage + "%");
        correctAnswersText.setText(String.valueOf(correctAnswers));
        incorrectAnswersText.setText(String.valueOf(totalQuestions - correctAnswers));
        totalQuestionsText.setText(String.valueOf(totalQuestions));

        // Update result message based on performance
        updateResultMessage();
    }

    private void updateResultMessage() {
        if (percentage >= 90) {
            resultIcon.setText("🏆");
            resultTitle.setText("Excellent!");
            resultSubtitle.setText("Outstanding performance! You're a quiz master!");
        } else if (percentage >= 75) {
            resultIcon.setText("🎉");
            resultTitle.setText("Great Job!");
            resultSubtitle.setText("Well done! You did really well!");
        } else if (percentage >= 60) {
            resultIcon.setText("👍");
            resultTitle.setText("Good Work!");
            resultSubtitle.setText("Nice effort! Keep practicing to improve!");
        } else if (percentage >= 40) {
            resultIcon.setText("📚");
            resultTitle.setText("Keep Trying!");
            resultSubtitle.setText("You're getting there! Study more and try again!");
        } else {
            resultIcon.setText("💪");
            resultTitle.setText("Don't Give Up!");
            resultSubtitle.setText("Practice makes perfect! Keep learning!");
        }
    }

    private void setupButtonListeners() {
        playAgainButton.setOnClickListener(v -> {
            // Return to quiz activity to play again
            Intent intent = new Intent(QuizResultActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        backToMenuButton.setOnClickListener(v -> {
            // Return to main menu
            Intent intent = new Intent(QuizResultActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Override back button to go to main menu instead of quiz
        Intent intent = new Intent(QuizResultActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}