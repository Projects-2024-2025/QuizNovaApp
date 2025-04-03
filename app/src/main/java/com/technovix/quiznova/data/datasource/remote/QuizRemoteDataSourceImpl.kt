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
                // Burada response code kontrolü de yapılabilir veya Repository'ye bırakılabilir
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("API Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            // Network hatası veya başka bir sorun
            Resource.Error(e.localizedMessage ?: "Unknown Remote Error")
        }
    }
    // ... getCategories ...
}