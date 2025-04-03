package com.technovix.quiznova.data.datasource.local

import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

interface QuizLocalDataSource {
    suspend fun insertQuestions(questions: List<QuestionEntity>)
    suspend fun getQuestionsByCategory(category: String, limit: Int): List<QuestionEntity>
    suspend fun deleteQuestionsByCategory(category: String)
    suspend fun getQuestionCountByCategory(category: String): Int

    suspend fun insertCategories(categories: List<CategoryEntity>)
    fun getAllCategories(): Flow<List<CategoryEntity>> // Flow dönebilir
    suspend fun getCategoryCount(): Int
}