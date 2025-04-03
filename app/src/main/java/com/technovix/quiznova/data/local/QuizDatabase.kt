package com.technovix.quiznova.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.data.local.entity.QuestionEntity

// Senior Dev: @Database annotation'ı ile veritabanımızı tanımlıyoruz.
// entities kısmında tablolarımızı, version'da veritabanı şema versiyonunu belirtiyoruz.
// Şemada değişiklik yaparsak versiyonu artırmalı ve migration sağlamalıyız.
// TypeConverters ile özel dönüştürücülerimizi kaydediyoruz.
@Database(
    entities = [QuestionEntity::class, CategoryEntity::class],
    version = 1, // Şema değişirse artırılmalı
    exportSchema = false // Şimdilik schema export etmeye gerek yok
)
@TypeConverters(Converters::class)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao // DAO'muza erişim için abstract fonksiyon
}