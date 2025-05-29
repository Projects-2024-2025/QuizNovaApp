package com.mindostech.quiznova.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Otomatik artan ID
    val category: String,
    val type: String,
    val difficulty: String,
    val question: String,
    val correctAnswer: String,
    val incorrectAnswers: List<String>, // Room TypeConverter gerektirecek
    val allAnswers: List<String>, // Karıştırılmış şıklar, UI'da kolaylık sağlar
    val fetchedTimestamp: Long = System.currentTimeMillis() // Verinin ne zaman çekildiği (cache süresi için)
)