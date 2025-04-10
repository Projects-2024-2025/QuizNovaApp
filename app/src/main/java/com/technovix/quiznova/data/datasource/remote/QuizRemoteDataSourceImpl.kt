package com.technovix.quiznova.data.datasource.remote

import com.technovix.quiznova.data.remote.OpenTriviaApi
import com.technovix.quiznova.data.remote.dto.OpenTriviaResponse
import com.technovix.quiznova.util.Resource
import javax.inject.Inject

class QuizRemoteDataSourceImpl @Inject constructor(
    private val api: OpenTriviaApi
) : QuizRemoteDataSource {
    override suspend fun getQuestions(amount: Int, categoryId: Int?, difficulty: String?, type: String): Resource<OpenTriviaResponse> {
        return try {
            val response = api.getQuestions(amount, categoryId, difficulty, type)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("API Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown Remote Error")
        }
    }
}