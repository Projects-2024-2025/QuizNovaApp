package com.mindostech.quiznova.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindostech.quiznova.data.local.entity.CategoryEntity
import com.mindostech.quiznova.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow // Flow ile değişiklikleri anlık takip edebiliriz

@Dao
interface QuizDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT * FROM questions WHERE category = :category ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsByCategory(category: String, limit: Int): List<QuestionEntity>

    @Query("DELETE FROM questions WHERE category = :category")
    suspend fun deleteQuestionsByCategory(category: String)

    @Query("SELECT COUNT(*) FROM questions WHERE category = :category")
    suspend fun getQuestionCountByCategory(category: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("SELECT MIN(fetchedTimestamp) FROM questions WHERE category = :category")
    suspend fun getOldestQuestionTimestamp(category: String): Long?
}