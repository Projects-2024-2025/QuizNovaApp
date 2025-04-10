package com.technovix.quiznova.data.remote

import com.technovix.quiznova.data.remote.dto.OpenTriviaResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenTriviaApi {

    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") categoryId: Int? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("type") type: String = "multiple"
    ): Response<OpenTriviaResponse>
}