package com.mindostech.quiznova.data.datasource.remote

import com.mindostech.quiznova.data.remote.dto.CategoriesResponse
import com.mindostech.quiznova.data.remote.dto.OpenTriviaResponse
import com.mindostech.quiznova.util.Resource

interface QuizRemoteDataSource {
    suspend fun getQuestions(amount: Int, categoryId: Int?, difficulty: String?, type: String): Resource<OpenTriviaResponse>
    suspend fun getCategories(): Resource<CategoriesResponse>
}