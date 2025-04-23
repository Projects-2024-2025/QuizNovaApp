package com.technovix.quiznova.data.datasource.remote

import com.technovix.quiznova.data.remote.dto.CategoriesResponse
import com.technovix.quiznova.data.remote.dto.OpenTriviaResponse
import com.technovix.quiznova.util.Resource

interface QuizRemoteDataSource {
    suspend fun getQuestions(amount: Int, categoryId: Int?, difficulty: String?, type: String): Resource<OpenTriviaResponse>
    suspend fun getCategories(): Resource<CategoriesResponse>
}