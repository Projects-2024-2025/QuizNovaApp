package com.technovix.quiznova.data.datasource.local

import com.technovix.quiznova.data.local.QuizDao
import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class QuizLocalDataSourceImpl @Inject constructor(
    private val dao: QuizDao
) : QuizLocalDataSource {
    override suspend fun insertQuestions(questions: List<QuestionEntity>) = dao.insertQuestions(questions)
    override suspend fun getQuestionsByCategory(category: String, limit: Int): List<QuestionEntity> = dao.getQuestionsByCategory(category, limit)
    override suspend fun deleteQuestionsByCategory(category: String) = dao.deleteQuestionsByCategory(category)
    override suspend fun getQuestionCountByCategory(category: String): Int = dao.getQuestionCountByCategory(category)
    override suspend fun insertCategories(categories: List<CategoryEntity>) = dao.insertCategories(categories)
    override fun getAllCategories(): Flow<List<CategoryEntity>> = dao.getAllCategories()
    override suspend fun getCategoryCount(): Int = dao.getCategoryCount()
}