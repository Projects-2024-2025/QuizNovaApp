package com.mindostech.quiznova.data.datasource.local

import com.mindostech.quiznova.data.local.entity.CategoryEntity
import com.mindostech.quiznova.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

interface QuizLocalDataSource {
    suspend fun insertQuestions(questions: List<QuestionEntity>)
    suspend fun getQuestionsByCategory(category: String, limit: Int): List<QuestionEntity>
    suspend fun deleteQuestionsByCategory(category: String)
    suspend fun getQuestionCountByCategory(category: String): Int

    suspend fun insertCategories(categories: List<CategoryEntity>)
    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun getCategoryCount(): Int

    suspend fun getOldestQuestionTimestamp(category: String): Long?
}