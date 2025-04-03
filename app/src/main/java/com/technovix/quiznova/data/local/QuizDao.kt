package com.technovix.quiznova.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow // Flow ile değişiklikleri anlık takip edebiliriz

@Dao
interface QuizDao {

    //  OnConflictStrategy.REPLACE, aynı ID'ye sahip bir soru gelirse eskisini yenisiyle değiştirir.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    // Senior Dev: Belirli bir kategorideki soruları getirir. Cache süresi kontrolü için timestamp'i de alabiliriz.
    @Query("SELECT * FROM questions WHERE category = :category ORDER BY RANDOM() LIMIT :limit") // Rastgele sırala
    suspend fun getQuestionsByCategory(category: String, limit: Int): List<QuestionEntity>

    // Senior Dev: Bir kategorideki tüm soruları siler (yeni veri gelince eskileri temizlemek için).
    @Query("DELETE FROM questions WHERE category = :category")
    suspend fun deleteQuestionsByCategory(category: String)

    @Query("SELECT COUNT(*) FROM questions WHERE category = :category")
    suspend fun getQuestionCountByCategory(category: String): Int

    // --- Categories ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    // Senior Dev: Flow<List<CategoryEntity>> kullanmak, kategoriler tablosunda bir değişiklik
    // olduğunda (yeni kategori eklenince vs.) UI'ın otomatik olarak güncellenmesini sağlar.
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}