package com.example.quizzapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quizzapp.QuizDetailActivity;
import com.example.quizzapp.R;
import com.example.quizzapp.models.Quiz;

import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {
    private List<Quiz> quizList;
    private Context context;

    public QuizAdapter(List<Quiz> quizList, Context context) {
        this.quizList = quizList;
        this.context = context;
    }
    public void updateQuizList(List<Quiz> newQuizList) {
        this.quizList = newQuizList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = quizList.get(position);
        holder.quizTitleTextView.setText(quiz.getTitle());
        holder.quizDescriptionTextView.setText(quiz.getDescription());
        holder.questionCountTextView.setText(quiz.getQuestionCount() + " Questions");
        holder.categoryTextView.setText(quiz.getCategoryName());

        // Load image with Glide
        if (quiz.getImageUrl() != null && !quiz.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(quiz.getImageUrl())
                    .placeholder(R.drawable.quiz_placeholder)
                    .error(R.drawable.quiz_placeholder)
                    .into(holder.quizImageView);
        } else {
            holder.quizImageView.setImageResource(R.drawable.quiz_placeholder);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, QuizDetailActivity.class);
            intent.putExtra("QUIZ_ID", quiz.getId());
            intent.putExtra("QUIZ_TITLE", quiz.getTitle());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        ImageView quizImageView;
        TextView quizTitleTextView;
        TextView quizDescriptionTextView;
        TextView questionCountTextView;
        TextView categoryTextView;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            quizImageView = itemView.findViewById(R.id.quizImageView);
            quizTitleTextView = itemView.findViewById(R.id.quizTitleTextView);
            quizDescriptionTextView = itemView.findViewById(R.id.quizDescriptionTextView);
            questionCountTextView = itemView.findViewById(R.id.questionCountTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
        }
    }
}