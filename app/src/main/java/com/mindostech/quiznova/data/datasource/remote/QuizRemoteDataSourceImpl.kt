package com.mindostech.quiznova.data.datasource.remote

import android.content.Context
import com.mindostech.quiznova.R
import com.mindostech.quiznova.data.remote.OpenTriviaApi
import com.mindostech.quiznova.data.remote.dto.CategoriesResponse
import com.mindostech.quiznova.data.remote.dto.OpenTriviaResponse
import com.mindostech.quiznova.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import retrofit2.HttpException
import javax.inject.Inject

class QuizRemoteDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: OpenTriviaApi
) : QuizRemoteDataSource {
    override suspend fun getQuestions(amount: Int, categoryId: Int?, difficulty: String?, type: String): Resource<OpenTriviaResponse> {
        return try {
            val response = api.getQuestions(amount, categoryId, difficulty, type)
            if (response.isSuccessful && response.body() != null) {
                val triviaResponse = response.body()!!
                when (triviaResponse.responseCode) {
                    0 -> Resource.Success(triviaResponse)
                    1 -> Resource.Error(context.getString(R.string.api_error_no_results))
                    2 -> Resource.Error(context.getString(R.string.api_error_invalid_parameter))
                    3 -> Resource.Error(context.getString(R.string.api_error_token_not_found)) // Bu uygulamada token kullanmıyoruz ama API dokümanında var
                    4 -> Resource.Error(context.getString(R.string.api_error_token_empty))
                    else -> Resource.Error(context.getString(R.string.api_error_unknown_response_code, triviaResponse.responseCode))
                }
            } else {
                Resource.Error(context.getString(R.string.api_error_generic, response.code(), response.message()))
            }
        } catch (e: HttpException) {
            val errorMsg = when(e.code()) {
                404 -> context.getString(R.string.api_error_404)
                500 -> context.getString(R.string.api_error_500)
                else -> context.getString(R.string.api_error_unexpected_server_code, e.code())
            }
            Resource.Error(errorMsg)
        } catch (e: IOException) {
            Resource.Error(context.getString(R.string.network_error_connection))
        } catch (e: Exception) {
            println("QuizRemoteDataSource Hata: $e")
            Resource.Error(e.localizedMessage ?: context.getString(R.string.network_error_questions_generic))
        }
    }

    override suspend fun getCategories(): Resource<CategoriesResponse> {
        return try {
            val response = api.getCategories()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(context.getString(R.string.categories_server_response_failed, response.code()))
            }
        } catch (e: HttpException) {
            val errorMsg = when(e.code()) {
                404 -> context.getString(R.string.categories_resource_not_found_404)
                500 -> context.getString(R.string.categories_server_error_500)
                else -> context.getString(R.string.categories_unexpected_server_error_code, e.code())
            }
            Resource.Error(errorMsg)
        } catch (e: IOException) {
            Resource.Error(context.getString(R.string.categories_network_error_fetch))
        } catch (e: Exception) {
            println("QuizRemoteDataSource Kategori Hatası: $e")
            Resource.Error(e.localizedMessage ?: context.getString(R.string.network_error_categories_generic))
        }
    }
}