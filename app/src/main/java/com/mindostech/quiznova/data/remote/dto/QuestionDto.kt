package com.mindostech.quiznova.data.remote.dto

import com.google.gson.annotations.SerializedName


data class OpenTriviaResponse(
    @SerializedName("response_code") val responseCode: Int,
    val results: List<QuestionDto>
)

data class QuestionDto(
    val type: String,
    val difficulty: String,
    val category: String,
    val question: String,
    @SerializedName("correct_answer") val correctAnswer: String,
    @SerializedName("incorrect_answers") val incorrectAnswers: List<String>
)