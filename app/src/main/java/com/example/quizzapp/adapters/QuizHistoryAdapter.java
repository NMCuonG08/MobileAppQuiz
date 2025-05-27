package com.example.quizzapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quizzapp.R;
import com.example.quizzapp.models.QuizResult;
import java.util.List;
import java.util.Locale;

public class QuizHistoryAdapter extends RecyclerView.Adapter<QuizHistoryAdapter.HistoryViewHolder> {

    private List<QuizResult> historyList;
    private OnHistoryItemClickListener listener;

    public interface OnHistoryItemClickListener {
        void onDeleteClick(QuizResult result, int position);
        void onItemClick(QuizResult result);
    }

    public QuizHistoryAdapter(List<QuizResult> historyList, OnHistoryItemClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        QuizResult result = historyList.get(position);
        holder.bind(result, position);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void updateData(List<QuizResult> newHistoryList) {
        this.historyList = newHistoryList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < historyList.size()) {
            historyList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, historyList.size());
        }
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvQuizTitle;
        private TextView tvCompletionTime;
        private TextView tvScore;
        private TextView tvPoints;
        private TextView tvAccuracy;
        private ImageButton btnDelete;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvQuizTitle = itemView.findViewById(R.id.tvQuizTitle);
            tvCompletionTime = itemView.findViewById(R.id.tvCompletionTime);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvAccuracy = itemView.findViewById(R.id.tvAccuracy);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(QuizResult result, int position) {
            // Set quiz title
            String title = result.getQuizTitle();
            if (title == null || title.trim().isEmpty()) {
                title = "Quiz #" + result.getQuizId();
            }
            tvQuizTitle.setText(title);

            // Set completion time
            String completionTime = result.getCompletionTime();
            if (completionTime == null || completionTime.trim().isEmpty()) {
                completionTime = "Unknown time";
            }
            tvCompletionTime.setText("Completed on " + completionTime);

            // Set score
            tvScore.setText(result.getScoreText());

            // Set points
            tvPoints.setText(result.getTotalScore() + " pts");

            // Set accuracy
            tvAccuracy.setText(String.format(Locale.getDefault(), "%.0f%%",
                    result.getAccuracyPercentage()));

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(result);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(result, position);
                }
            });

            // Add subtle animation
            itemView.setAlpha(0f);
            itemView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(position * 50L)
                    .start();
        }
    }
}