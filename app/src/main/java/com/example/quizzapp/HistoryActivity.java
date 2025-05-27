package com.example.quizzapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzapp.adapters.QuizHistoryAdapter;
import com.example.quizzapp.managers.QuizHistoryManager;
import com.example.quizzapp.models.QuizResult;

import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity implements QuizHistoryAdapter.OnHistoryItemClickListener {

    private RecyclerView recyclerViewHistory;
    private LinearLayout emptyStateLayout;
    private ProgressBar loadingProgress;

    // Stats Views
    private TextView tvTotalQuizzes;
    private TextView tvTotalScore;
    private TextView tvAverageAccuracy;

    // Action Buttons
    private Button btnClearAll;
    private Button btnRefresh;

    // Data
    private QuizHistoryManager historyManager;
    private QuizHistoryAdapter adapter;
    private List<QuizResult> historyList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupButtonListeners();

        // Initialize history manager
        historyManager = new QuizHistoryManager(this);

        // Load data
        loadHistoryData();
    }

    private void initViews() {
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loadingProgress = findViewById(R.id.loadingProgress);

        // Stats views
        tvTotalQuizzes = findViewById(R.id.tvTotalQuizzes);
        tvTotalScore = findViewById(R.id.tvTotalScore);
        tvAverageAccuracy = findViewById(R.id.tvAverageAccuracy);

        // Action buttons
        btnClearAll = findViewById(R.id.btnClearAll);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setHasFixedSize(true);
    }

    private void setupButtonListeners() {
        btnClearAll.setOnClickListener(v -> showClearAllDialog());
        btnRefresh.setOnClickListener(v -> loadHistoryData());
    }

    private void loadHistoryData() {
        showLoading(true);

        // Simulate loading delay for better UX
        recyclerViewHistory.postDelayed(() -> {
            historyList = historyManager.getAllResults();

            if (adapter == null) {
                adapter = new QuizHistoryAdapter(historyList, this);
                recyclerViewHistory.setAdapter(adapter);
            } else {
                adapter.updateData(historyList);
            }

            updateStatsDisplay();
            showLoading(false);

            // Show empty state if no data
            if (historyList.isEmpty()) {
                showEmptyState(true);
            } else {
                showEmptyState(false);
            }
        }, 500);
    }

    private void updateStatsDisplay() {
        QuizHistoryManager.QuizStats stats = historyManager.getOverallStats();

        tvTotalQuizzes.setText(String.valueOf(stats.totalQuizzes));
        tvTotalScore.setText(String.valueOf(stats.totalScore));
        tvAverageAccuracy.setText(String.format(Locale.getDefault(), "%.0f%%", stats.averageAccuracy));
    }

    private void showLoading(boolean show) {
        loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewHistory.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewHistory.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showClearAllDialog() {
        if (historyList == null || historyList.isEmpty()) {
            Toast.makeText(this, "No history to clear", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Clear All History")
                .setMessage("Are you sure you want to delete all quiz history? This action cannot be undone.")
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton("Clear All", (dialog, which) -> {
                    historyManager.clearAllHistory();
                    loadHistoryData();
                    Toast.makeText(this, "All history cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDialog(QuizResult result, int position) {
        String quizTitle = result.getQuizTitle();
        if (quizTitle == null || quizTitle.trim().isEmpty()) {
            quizTitle = "Quiz #" + result.getQuizId();
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Quiz Result")
                .setMessage("Are you sure you want to delete the result for \"" + quizTitle + "\"?")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = historyManager.deleteResult(result.getId());
                    if (deleted) {
                        adapter.removeItem(position);
                        updateStatsDisplay();

                        // Show empty state if no more items
                        if (adapter.getItemCount() == 0) {
                            showEmptyState(true);
                        }

                        Toast.makeText(this, "Result deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete result", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Interface implementations
    @Override
    public void onDeleteClick(QuizResult result, int position) {
        showDeleteDialog(result, position);
    }

    @Override
    public void onItemClick(QuizResult result) {
        // Hiển thị chi tiết kết quả (có thể mở dialog hoặc activity khác)
        showResultDetails(result);
    }

    private void showResultDetails(QuizResult result) {
        String quizTitle = result.getQuizTitle();
        if (quizTitle == null || quizTitle.trim().isEmpty()) {
            quizTitle = "Quiz #" + result.getQuizId();
        }

        String message = String.format(Locale.getDefault(),
                "Quiz: %s\n\n" +
                        "Completed: %s\n" +
                        "Score: %s\n" +
                        "Points Earned: %d\n" +
                        "Accuracy: %.1f%%",
                quizTitle,
                result.getCompletionTime(),
                result.getScoreText(),
                result.getTotalScore(),
                result.getAccuracyPercentage());

        new AlertDialog.Builder(this)
                .setTitle("Quiz Result Details")
                .setMessage(message)
                .setIcon(R.drawable.ic_info)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadHistoryData();
    }
}