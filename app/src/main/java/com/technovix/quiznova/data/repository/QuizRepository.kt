package com.technovix.quiznova.data.repository

import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.data.local.entity.QuestionEntity
import com.technovix.quiznova.util.Resource
import kotlinx.coroutines.flow.Flow // Flow kullanacağız

interface QuizRepository {
    fun getCategories(): Flow<Resource<List<CategoryEntity>>>
    fun getQuestions(categoryId: Int, categoryName: String, amount: Int): Flow<Resource<List<QuestionEntity>>>
}