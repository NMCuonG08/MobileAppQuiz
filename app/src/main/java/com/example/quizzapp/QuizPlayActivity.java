package com.example.quizzapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.quizzapp.api.ApiClient;
import com.example.quizzapp.api.ApiInterface;
import com.example.quizzapp.managers.QuizHistoryManager;
import com.example.quizzapp.models.Option;
import com.example.quizzapp.models.Question;
import com.example.quizzapp.models.QuizResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizPlayActivity extends AppCompatActivity {
    private static final String TAG = "QuizPlayActivity";
    private QuizHistoryManager historyManager;
    private String quizTitle;
    // UI Components
    private TextView questionCounter, timeRemaining, questionPoints, questionText, questionDescription;
    private ImageView questionImage;
    private ProgressBar questionProgress, loadingProgress;
    private LinearLayout optionsContainer;
    private Button skipButton, nextButton;

    // Quiz Data
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private String quizId;

    // Game State
    private int totalScore = 0;
    private int correctAnswers = 0;
    private CountDownTimer timer;
    private Set<String> selectedAnswers = new HashSet<>();
    private boolean isAnswerSubmitted = false;

    // Question Types Constants
    private static final String TYPE_TRUE_FALSE = "TRUE_FALSE";
    private static final String TYPE_SINGLE_ANSWER = "SINGLE_ANSWER";
    private static final String TYPE_MULTIPLE_ANSWER = "MULTIPLE_ANSWER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_play);

        // Khởi tạo các view
        initViews();

        // Lấy quiz ID từ Intent
        quizId = getIntent().getStringExtra("QUIZ_ID");
        quizTitle = getIntent().getStringExtra("QUIZ_TITLE");
        if (quizId == null || quizId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        historyManager = new QuizHistoryManager(this);
        // Thiết lập sự kiện cho các nút
        setupButtonListeners();

        // Tải câu hỏi từ API
        fetchQuestions();
    }

    /**
     * Khởi tạo tất cả các view component
     */
    private void initViews() {
        questionCounter = findViewById(R.id.questionCounter);
        timeRemaining = findViewById(R.id.timeRemaining);
        questionPoints = findViewById(R.id.questionPoints);
        questionText = findViewById(R.id.questionText);
        questionDescription = findViewById(R.id.questionDescription);
        questionImage = findViewById(R.id.questionImage);
        questionProgress = findViewById(R.id.questionProgress);
        loadingProgress = findViewById(R.id.loadingProgress);
        optionsContainer = findViewById(R.id.optionsContainer);
        skipButton = findViewById(R.id.skipButton);
        nextButton = findViewById(R.id.nextButton);
    }

    /**
     * Thiết lập sự kiện click cho các nút
     */
    private void setupButtonListeners() {
        skipButton.setOnClickListener(v -> {
            // Dừng timer và chuyển câu tiếp theo
            if (timer != null) {
                timer.cancel();
            }
            moveToNextQuestion();
        });

        nextButton.setOnClickListener(v -> {
            if (!isAnswerSubmitted) {
                // Người dùng chưa submit đáp án, kiểm tra và chấm điểm
                submitAnswer();
            } else {
                // Đã submit rồi, chuyển câu tiếp theo
                moveToNextQuestion();
            }
        });
    }

    /**
     * Gọi API để lấy danh sách câu hỏi
     */
    private void fetchQuestions() {
        loadingProgress.setVisibility(View.VISIBLE);
        Log.d(TAG, "Đang tải câu hỏi cho quiz ID: " + quizId);

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Question>> call = apiInterface.getQuestionsByQuizId(quizId);

        call.enqueue(new Callback<List<Question>>() {
            @Override
            public void onResponse(Call<List<Question>> call, Response<List<Question>> response) {
                loadingProgress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    questions = response.body();
                    if (questions.isEmpty()) {
                        Toast.makeText(QuizPlayActivity.this, "Không có câu hỏi nào trong quiz này", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    Log.d(TAG, "Đã tải " + questions.size() + " câu hỏi");
                    // Bắt đầu quiz với câu hỏi đầu tiên
                    displayCurrentQuestion();
                } else {
                    Log.e(TAG, "Lỗi API: " + response.code() + " - " + response.message());
                    Toast.makeText(QuizPlayActivity.this, "Lỗi khi tải câu hỏi", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Question>> call, Throwable t) {
                loadingProgress.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi mạng khi tải câu hỏi", t);
                Toast.makeText(QuizPlayActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Hiển thị câu hỏi hiện tại
     */
    private void displayCurrentQuestion() {
        if (questions == null || currentQuestionIndex >= questions.size()) {
            // Hết câu hỏi, chuyển đến màn hình kết quả
            showQuizResult();
            return;
        }

        Question currentQuestion = questions.get(currentQuestionIndex);

        // Reset trạng thái
        isAnswerSubmitted = false;
        selectedAnswers.clear();
        nextButton.setText("Submit Answer");
        nextButton.setEnabled(false);

        // Cập nhật thông tin header
        questionCounter.setText("Question " + (currentQuestionIndex + 1) + " of " + questions.size());
        questionProgress.setMax(questions.size());
        questionProgress.setProgress(currentQuestionIndex + 1);

        // Hiển thị điểm số
        String pointText = currentQuestion.getPoint() == 1 ? "1 point" : currentQuestion.getPoint() + " points";
        questionPoints.setText(pointText);

        // Hiển thị nội dung câu hỏi
        questionText.setText(currentQuestion.getText());

        // Hiển thị mô tả nếu có
        if (currentQuestion.getDescription() != null && !currentQuestion.getDescription().trim().isEmpty()) {
            questionDescription.setText(currentQuestion.getDescription());
            questionDescription.setVisibility(View.VISIBLE);
        } else {
            questionDescription.setVisibility(View.GONE);
        }

        // Hiển thị hình ảnh nếu có
        if (currentQuestion.getMedia() != null && !currentQuestion.getMedia().isEmpty() &&
                "image".equals(currentQuestion.getMediaType())) {
            questionImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(currentQuestion.getMedia())
                    .placeholder(R.drawable.image_placeholder_background)
                    .error(R.drawable.image_placeholder_background)
                    .into(questionImage);
        } else {
            questionImage.setVisibility(View.GONE);
        }

        // Tạo các lựa chọn dựa trên loại câu hỏi
        createAnswerOptions(currentQuestion);

        // Bắt đầu đếm thời gian
        startTimer(currentQuestion.getTime());
    }

    /**
     * Tạo các lựa chọn trả lời dựa trên loại câu hỏi
     */
    private void createAnswerOptions(Question question) {
        optionsContainer.removeAllViews();

        String questionType = question.getType();
        List<Option> options = question.getOptions();

        if (options == null || options.isEmpty()) {
            return;
        }

        switch (questionType) {
            case TYPE_TRUE_FALSE:
                createTrueFalseOptions(options);
                break;
            case TYPE_SINGLE_ANSWER:
                createSingleChoiceOptions(options);
                break;
            case TYPE_MULTIPLE_ANSWER:
                createMultipleChoiceOptions(options);
                break;
            default:
                Log.w(TAG, "Loại câu hỏi không được hỗ trợ: " + questionType);
                createSingleChoiceOptions(options); // Fallback
                break;
        }
    }

    /**
     * Tạo giao diện cho câu hỏi Đúng/Sai
     */
    /**
     * Tạo giao diện cho câu hỏi Đúng/Sai
     */
    private void createTrueFalseOptions(List<Option> options) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);

            // Tạo CardView cho mỗi option
            CardView cardView = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 8, 0, 8);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(12);
            cardView.setCardElevation(2);
            cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));

            // Tạo TextView thay vì RadioButton
            TextView textView = new TextView(this);
            textView.setText(option.getOption());
            textView.setPadding(24, 16, 24, 16);
            textView.setTextSize(16);
            textView.setTextColor(getResources().getColor(android.R.color.black));
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0);
            textView.setCompoundDrawablePadding(16);

            // Click listener cho CardView
            cardView.setOnClickListener(v -> {
                if (isAnswerSubmitted) return; // Không cho phép click sau khi submit

                selectedAnswers.clear();
                selectedAnswers.add(option.getOption());
                nextButton.setEnabled(true);

                // Reset tất cả card về trạng thái không được chọn
                for (int j = 0; j < container.getChildCount(); j++) {
                    CardView otherCard = (CardView) container.getChildAt(j);
                    TextView otherText = (TextView) otherCard.getChildAt(0);
                    otherText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0);
                    updateCardBackground(otherCard, false);
                }

                // Chọn card hiện tại
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_checked, 0, 0, 0);
                updateCardBackground(cardView, true);
            });

            cardView.addView(textView);
            container.addView(cardView);
        }

        optionsContainer.addView(container);
    }

    /**
     * Tạo giao diện cho câu hỏi một lựa chọn
     */
    private void createSingleChoiceOptions(List<Option> options) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);

            // Tạo CardView cho mỗi option
            CardView cardView = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 8, 0, 8);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(12);
            cardView.setCardElevation(2);
            cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));

            // Tạo TextView thay vì RadioButton
            TextView textView = new TextView(this);
            textView.setText(option.getOption());
            textView.setPadding(24, 16, 24, 16);
            textView.setTextSize(16);
            textView.setTextColor(getResources().getColor(android.R.color.black));
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0);
            textView.setCompoundDrawablePadding(16);

            // Click listener cho CardView
            cardView.setOnClickListener(v -> {
                if (isAnswerSubmitted) return; // Không cho phép click sau khi submit

                selectedAnswers.clear();
                selectedAnswers.add(option.getOption());
                nextButton.setEnabled(true);

                // Reset tất cả card về trạng thái không được chọn
                for (int j = 0; j < container.getChildCount(); j++) {
                    CardView otherCard = (CardView) container.getChildAt(j);
                    TextView otherText = (TextView) otherCard.getChildAt(0);
                    otherText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0);
                    updateCardBackground(otherCard, false);
                }

                // Chọn card hiện tại
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_checked, 0, 0, 0);
                updateCardBackground(cardView, true);
            });

            cardView.addView(textView);
            container.addView(cardView);
        }

        optionsContainer.addView(container);
    }

    /**
     * Tạo giao diện cho câu hỏi nhiều lựa chọn
     */
    private void createMultipleChoiceOptions(List<Option> options) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        for (Option option : options) {
            // Tạo CardView cho mỗi option
            CardView cardView = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 8, 0, 8);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(12);
            cardView.setCardElevation(2);
            cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));

            // Tạo CheckBox
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(option.getOption());
            checkBox.setPadding(24, 16, 24, 16);
            checkBox.setTextSize(16);
            checkBox.setTextColor(getResources().getColor(android.R.color.black));

            // Thiết lập sự kiện click
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedAnswers.add(option.getOption());
                } else {
                    selectedAnswers.remove(option.getOption());
                }

                // Kích hoạt nút Next nếu có ít nhất một lựa chọn
                nextButton.setEnabled(!selectedAnswers.isEmpty());
                updateCardBackground(cardView, isChecked);
            });

            cardView.addView(checkBox);
            container.addView(cardView);
        }

        optionsContainer.addView(container);
    }

    /**
     * Cập nhật màu nền của card khi được chọn/bỏ chọn
     */
    private void updateCardBackground(CardView cardView, boolean isSelected) {
        if (isSelected) {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.option_selected, null));
        } else {
            cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));
        }
    }

    /**
     * Bắt đầu đếm thời gian cho câu hỏi
     */
    private void startTimer(int timeInSeconds) {
        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(timeInSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                timeRemaining.setText(secondsRemaining + "s");

                // Thay đổi màu khi sắp hết thời gian
                if (secondsRemaining <= 5) {
                    timeRemaining.setBackgroundResource(R.drawable.timer_background);
                }
            }

            @Override
            public void onFinish() {
                timeRemaining.setText("0s");
                // Tự động submit hoặc chuyển câu tiếp theo khi hết thời gian
                if (!isAnswerSubmitted) {
                    submitAnswer();
                } else {
                    moveToNextQuestion();
                }
            }
        };

        timer.start();
    }

    /**
     * Submit đáp án và hiển thị kết quả
     */
    private void submitAnswer() {
        if (questions == null || currentQuestionIndex >= questions.size()) {
            return;
        }

        Question currentQuestion = questions.get(currentQuestionIndex);
        boolean isCorrect = checkAnswer(currentQuestion);

        if (isCorrect) {
            correctAnswers++;
            totalScore += currentQuestion.getPoint();
            Toast.makeText(this, "Correct! +" + currentQuestion.getPoint() + " points", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show();
        }

        // Hiển thị đáp án đúng
        highlightCorrectAnswers(currentQuestion);

        isAnswerSubmitted = true;
        nextButton.setText("Next Question");
        nextButton.setEnabled(true);

        // Dừng timer
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * Kiểm tra đáp án có đúng không
     */
    private boolean checkAnswer(Question question) {
        List<Option> options = question.getOptions();
        Set<String> correctAnswers = new HashSet<>();

        // Lấy tất cả đáp án đúng
        for (Option option : options) {
            if (option.isCorrect()) {
                correctAnswers.add(option.getOption());
            }
        }

        // So sánh với đáp án người dùng chọn
        return selectedAnswers.equals(correctAnswers);
    }

    /**
     * Làm nổi bật đáp án đúng sau khi submit
     */
    private void highlightCorrectAnswers(Question question) {
        List<Option> options = question.getOptions();

        // Vô hiệu hóa tất cả các option
        disableAllOptions();

        // Làm nổi bật đáp án đúng và sai
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            View child = optionsContainer.getChildAt(i);

            if (child instanceof LinearLayout) {
                LinearLayout container = (LinearLayout) child;
                for (int j = 0; j < container.getChildCount(); j++) {
                    CardView cardView = (CardView) container.getChildAt(j);

                    // Kiểm tra loại view bên trong CardView
                    View innerView = cardView.getChildAt(0);
                    String optionText = "";

                    if (innerView instanceof TextView) {
                        // Cho TRUE_FALSE và SINGLE_ANSWER
                        TextView textView = (TextView) innerView;
                        optionText = textView.getText().toString();
                    } else if (innerView instanceof CheckBox) {
                        // Cho MULTIPLE_ANSWER
                        CheckBox checkBox = (CheckBox) innerView;
                        optionText = checkBox.getText().toString();
                    }

                    if (!optionText.isEmpty()) {
                        highlightOptionCard(cardView, optionText, options);
                    }
                }
            }
        }
    }

    /**
     * Vô hiệu hóa tất cả các option sau khi submit
     */
    private void disableAllOptions() {
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            View child = optionsContainer.getChildAt(i);

            if (child instanceof LinearLayout) {
                LinearLayout container = (LinearLayout) child;
                for (int j = 0; j < container.getChildCount(); j++) {
                    CardView cardView = (CardView) container.getChildAt(j);
                    View innerView = cardView.getChildAt(0);

                    if (innerView instanceof TextView) {
                        // Vô hiệu hóa click listener cho CardView
                        cardView.setOnClickListener(null);
                        cardView.setClickable(false);
                    } else if (innerView instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) innerView;
                        checkBox.setEnabled(false);
                        checkBox.setClickable(false);
                    }
                }
            }
        }
    }

    /**
     * Làm nổi bật card option dựa trên đúng/sai
     */
    private void highlightOptionCard(CardView cardView, String optionText, List<Option> options) {
        boolean isCorrect = false;
        boolean isSelected = selectedAnswers.contains(optionText);

        // Kiểm tra xem option này có phải đáp án đúng không
        for (Option option : options) {
            if (option.getOption().equals(optionText) && option.isCorrect()) {
                isCorrect = true;
                break;
            }
        }

        // Thiết lập màu nền
        if (isCorrect) {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.option_correct, null));
        } else if (isSelected) {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.option_incorrect, null));
        }
    }

    /**
     * Vô hiệu hóa tất cả các option sau khi submit
     */


    /**
     * Chuyển đến câu hỏi tiếp theo
     */
    private void moveToNextQuestion() {
        currentQuestionIndex++;

        if (currentQuestionIndex >= questions.size()) {
            // Hết câu hỏi, hiển thị kết quả
            showQuizResult();
        } else {
            // Hiển thị câu hỏi tiếp theo
            displayCurrentQuestion();
        }
    }

    /**
     * Hiển thị kết quả quiz cuối cùng
     */
    private void showQuizResult() {
        if (timer != null) {
            timer.cancel();
        }

        // Lưu kết quả vào lịch sử local
        saveQuizResultToHistory();

        // Tạo Intent để mở QuizResultActivity
        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("TOTAL_QUESTIONS", questions.size());
        intent.putExtra("CORRECT_ANSWERS", correctAnswers);
        intent.putExtra("TOTAL_SCORE", totalScore);
        intent.putExtra("QUIZ_ID", quizId);
        intent.putExtra("QUIZ_TITLE", quizTitle);

        startActivity(intent);
        finish(); // Đóng QuizPlayActivity
    }
    private void saveQuizResultToHistory() {
        try {
            // Tạo QuizResult object
            QuizResult result = new QuizResult(
                    quizId,
                    quizTitle != null ? quizTitle : "Unknown Quiz",
                    questions.size(),
                    correctAnswers,
                    totalScore
            );

            // Lưu vào history manager
            historyManager.saveQuizResult(result);

            Log.d(TAG, "Quiz result saved to history: " + result.getScoreText());
        } catch (Exception e) {
            Log.e(TAG, "Error saving quiz result to history", e);
            // Không hiển thị lỗi cho user vì đây không phải chức năng chính
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy timer khi Activity bị destroy
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        // Hiển thị dialog xác nhận trước khi thoát
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? Your progress will be lost.")
                .setPositiveButton("Exit", (dialog, which) -> {
                    if (timer != null) {
                        timer.cancel();
                    }
                    super.onBackPressed();
                })
                .setNegativeButton("Continue", null)
                .show();
    }
}