package com.technovix.quiznova.data.repository

import android.text.Html
import com.technovix.quiznova.data.datasource.local.QuizLocalDataSource
import com.technovix.quiznova.data.datasource.remote.QuizRemoteDataSource
import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.data.local.entity.QuestionEntity
import com.technovix.quiznova.data.remote.dto.QuestionDto
import com.technovix.quiznova.util.NetworkMonitor
import com.technovix.quiznova.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepositoryImpl @Inject constructor(

    private val remoteDataSource: QuizRemoteDataSource,
    private val localDataSource: QuizLocalDataSource,
    private val networkMonitor: NetworkMonitor
) : QuizRepository {

    private val hardcodedCategories = listOf(
        CategoryEntity(id = 9, name = "General Knowledge"),
        CategoryEntity(id = 10, name = "Entertainment: Books"),
        CategoryEntity(id = 11, name = "Entertainment: Film"),
        CategoryEntity(id = 12, name = "Entertainment: Music"),
        CategoryEntity(id = 17, name = "Science & Nature"),
        CategoryEntity(id = 18, name = "Science: Computers"),
        CategoryEntity(id = 21, name = "Sports"),
        CategoryEntity(id = 22, name = "Geography"),
        CategoryEntity(id = 23, name = "History"),
        CategoryEntity(id = 27, name = "Animals")
    )

    override fun getCategories(): Flow<Resource<List<CategoryEntity>>> = flow {
        emit(Resource.Loading())

        localDataSource.getAllCategories().collect { categories ->
            emit(Resource.Success(categories))
            if (categories.isEmpty() && localDataSource.getCategoryCount() == 0) {
                try {
                    localDataSource.insertCategories(hardcodedCategories)
                } catch (e: Exception) {
                    println("Kategori ekleme hatası: ${e.localizedMessage}")
                }
            }
        }
    }.catch { e ->
        emit(Resource.Error("Kategoriler yüklenirken bilinmeyen bir hata oluştu: ${e.localizedMessage}"))
    }.flowOn(Dispatchers.IO)

    override fun getQuestions(categoryId: Int, categoryName: String, amount: Int): Flow<Resource<List<QuestionEntity>>> = flow {
        emit(Resource.Loading())

        val isOnline = networkMonitor.isOnline.first()

        if (isOnline) {
            when (val remoteResult = remoteDataSource.getQuestions(amount, categoryId, null, "multiple")) {
                is Resource.Success -> {
                    try {
                        localDataSource.deleteQuestionsByCategory(categoryName)
                        val questionEntities = mapDtoToEntity(remoteResult.data!!.results, categoryName)
                        localDataSource.insertQuestions(questionEntities)
                        emit(Resource.Success(localDataSource.getQuestionsByCategory(categoryName, amount)))
                    } catch(e: Exception) {
                        emit(Resource.Error("Sorular işlenirken hata oluştu: ${e.localizedMessage}"))
                    }
                }
                is Resource.Error -> {
                    emit(handleDataSourceError(remoteResult.message, categoryName, amount))
                }
                is Resource.Loading -> { /* Bu durum DataSource'dan gelmez */ }
            }
        }
        else {
            val localQuestions = localDataSource.getQuestionsByCategory(categoryName, amount)
            if (localQuestions.isNotEmpty()) {
                emit(Resource.Success(localQuestions))
            }
            else {
                emit(Resource.Error("Çevrimdışı moddasınız ve bu kategori için kayıtlı soru bulunamadı."))
            }
        }
    }.catch { e ->
        emit(Resource.Error("Sorular yüklenirken bilinmeyen bir hata oluştu: ${e.localizedMessage}"))
    }.flowOn(Dispatchers.IO)

    private suspend fun handleDataSourceError(errorMessage: String?, categoryName: String, amount: Int): Resource<List<QuestionEntity>> {
        val localQuestions = localDataSource.getQuestionsByCategory(categoryName, amount)
        return if (localQuestions.isNotEmpty()) {
            Resource.Error("Veri güncellenemedi (${errorMessage ?: "Bilinmeyen Hata"}), eski veriler gösteriliyor.", localQuestions)
        } else {
            Resource.Error("Sorular yüklenemedi: ${errorMessage ?: "Bilinmeyen Hata"}")
        }
    }

    private fun mapDtoToEntity(dtos: List<QuestionDto>, categoryName: String): List<QuestionEntity> {
        return dtos.map { dto ->
            val decodedQuestion = Html.fromHtml(dto.question, Html.FROM_HTML_MODE_LEGACY).toString()
            val decodedCorrectAnswer = Html.fromHtml(dto.correctAnswer, Html.FROM_HTML_MODE_LEGACY).toString()
            val decodedIncorrectAnswers = dto.incorrectAnswers.map { Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY).toString() }
            val allAnswers = (decodedIncorrectAnswers + decodedCorrectAnswer).shuffled()
            QuestionEntity(
                category = categoryName, type = dto.type, difficulty = dto.difficulty, question = decodedQuestion,
                correctAnswer = decodedCorrectAnswer, incorrectAnswers = decodedIncorrectAnswers, allAnswers = allAnswers
            )
        }
    }
}