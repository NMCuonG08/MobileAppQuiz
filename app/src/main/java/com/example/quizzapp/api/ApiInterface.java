package com.example.quizzapp.api;

import com.example.quizzapp.models.Question;
import com.example.quizzapp.models.Quiz;
import com.example.quizzapp.models.QuizDetail;
import com.example.quizzapp.wrapper.QuizDetailResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {
    // Lấy tất cả quiz
    @GET("api/quizzes/getAll")
    Call<List<Quiz>> getQuizzes();

    // Lấy quizzes theo userId
    @GET("api/quizzes/getAllQuiz")
    Call<List<Quiz>> getQuizzesByUserId(@Query("userId") String userId);

    // Lấy quiz theo ID
    @GET("api/quizzes/getById")
    Call<Quiz> getQuizById(@Query("id") String id);

    // Lấy chi tiết quiz theo ID
    @GET("api/quizzes/getDetailById")
    Call<QuizDetailResponse> getQuizDetailById(@Query("id") String id);

    // Lấy quiz theo danh mục
    @GET("api/quizzes/getByCategory")
    Call<List<Quiz>> getQuizzesByCategory(@Query("category") String category);

    // Lấy quiz thách đấu
    @GET("api/quizzes/getchallenges")
    Call<List<Quiz>> getChallengesQuiz();

    // Đánh giá quiz
    @POST("api/quizzes/{quizId}/rate")
    Call<Object> rateQuiz(@Path("quizId") String quizId, @Body Map<String, Float> ratingData);

    @GET("api/questions/getQuestionsByQuizzId")
    Call<List<Question>> getQuestionsByQuizId(@Query("quizId") String quizId);


}