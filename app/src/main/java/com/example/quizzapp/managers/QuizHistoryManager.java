package com.example.quizzapp.managers;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.quizzapp.models.QuizResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuizHistoryManager {
    private static final String PREF_NAME = "quiz_history";
    private static final String KEY_QUIZ_RESULTS = "quiz_results";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public QuizHistoryManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Lưu kết quả quiz mới
     */
    public void saveQuizResult(QuizResult result) {
        List<QuizResult> results = getAllResults();

        // Thêm thời gian hoàn thành
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        result.setCompletionTime(sdf.format(new Date(result.getTimestamp())));

        results.add(0, result); // Thêm vào đầu danh sách (mới nhất trước)

        // Giới hạn số lượng kết quả lưu trữ (tùy chọn)
        if (results.size() > 50) {
            results = results.subList(0, 50);
        }

        saveResultsList(results);
    }

    /**
     * Lấy tất cả kết quả quiz
     */
    public List<QuizResult> getAllResults() {
        String json = sharedPreferences.getString(KEY_QUIZ_RESULTS, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<List<QuizResult>>(){}.getType();
        List<QuizResult> results = gson.fromJson(json, listType);
        return results != null ? results : new ArrayList<>();
    }

    /**
     * Xóa kết quả theo ID
     */
    public boolean deleteResult(String resultId) {
        List<QuizResult> results = getAllResults();
        boolean removed = results.removeIf(result -> result.getId().equals(resultId));

        if (removed) {
            saveResultsList(results);
        }

        return removed;
    }

    /**
     * Xóa tất cả lịch sử
     */
    public void clearAllHistory() {
        sharedPreferences.edit().remove(KEY_QUIZ_RESULTS).apply();
    }

    /**
     * Lấy kết quả theo Quiz ID
     */
    public List<QuizResult> getResultsByQuizId(String quizId) {
        List<QuizResult> allResults = getAllResults();
        List<QuizResult> filteredResults = new ArrayList<>();

        for (QuizResult result : allResults) {
            if (result.getQuizId().equals(quizId)) {
                filteredResults.add(result);
            }
        }

        return filteredResults;
    }

    /**
     * Lấy điểm cao nhất của một quiz
     */
    public int getBestScore(String quizId) {
        List<QuizResult> results = getResultsByQuizId(quizId);
        int bestScore = 0;

        for (QuizResult result : results) {
            if (result.getTotalScore() > bestScore) {
                bestScore = result.getTotalScore();
            }
        }

        return bestScore;
    }

    /**
     * Đếm số lần làm quiz
     */
    public int getQuizAttemptCount(String quizId) {
        return getResultsByQuizId(quizId).size();
    }

    /**
     * Lưu danh sách kết quả
     */
    private void saveResultsList(List<QuizResult> results) {
        String json = gson.toJson(results);
        sharedPreferences.edit().putString(KEY_QUIZ_RESULTS, json).apply();
    }

    /**
     * Lấy thống kê tổng quan
     */
    public QuizStats getOverallStats() {
        List<QuizResult> results = getAllResults();

        int totalQuizzes = results.size();
        int totalQuestions = 0;
        int totalCorrect = 0;
        int totalScore = 0;

        for (QuizResult result : results) {
            totalQuestions += result.getTotalQuestions();
            totalCorrect += result.getCorrectAnswers();
            totalScore += result.getTotalScore();
        }

        double averageAccuracy = totalQuestions > 0 ?
                (double) totalCorrect / totalQuestions * 100 : 0;

        return new QuizStats(totalQuizzes, totalQuestions, totalCorrect,
                totalScore, averageAccuracy);
    }

    /**
     * Class để lưu thống kê
     */
    public static class QuizStats {
        public final int totalQuizzes;
        public final int totalQuestions;
        public final int totalCorrect;
        public final int totalScore;
        public final double averageAccuracy;

        public QuizStats(int totalQuizzes, int totalQuestions, int totalCorrect,
                         int totalScore, double averageAccuracy) {
            this.totalQuizzes = totalQuizzes;
            this.totalQuestions = totalQuestions;
            this.totalCorrect = totalCorrect;
            this.totalScore = totalScore;
            this.averageAccuracy = averageAccuracy;
        }
    }
}