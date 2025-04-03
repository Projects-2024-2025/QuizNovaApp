package com.technovix.quiznova.data.repository

import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.data.local.entity.QuestionEntity
import com.technovix.quiznova.util.Resource
import kotlinx.coroutines.flow.Flow // Flow kullanacağız

// Senior Dev: Interface, implementasyon detaylarını gizler. Testlerde sahte (mock)
// implementasyonlar kullanmamızı sağlar.
interface QuizRepository {
    // Flow<Resource<T>>: Veri akışını ve durumunu (Loading, Success, Error) bir arada yönetir.
    fun getCategories(): Flow<Resource<List<CategoryEntity>>>
    fun getQuestions(categoryId: Int, categoryName: String, amount: Int): Flow<Resource<List<QuestionEntity>>>
}