package com.mindostech.quiznova.data.datasource.local

import com.google.common.truth.Truth.assertThat
import com.mindostech.quiznova.data.local.QuizDao
import com.mindostech.quiznova.data.local.entity.CategoryEntity
import com.mindostech.quiznova.data.local.entity.QuestionEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class QuizLocalDataSourceImplTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var dao: QuizDao

    private lateinit var localDataSource: QuizLocalDataSourceImpl

    @Before
    fun setUp() {
        localDataSource = QuizLocalDataSourceImpl(dao)
    }

    @Test
    fun `insertQuestions DAO'nun insertQuestions fonksiyonunu çağırır`() = runTest {
        // Arrange
        val questions = listOf(mockk<QuestionEntity>())

        coEvery { dao.insertQuestions(any()) } returns Unit

        // Act
        localDataSource.insertQuestions(questions)

        // Assert
        coVerify(exactly = 1) { dao.insertQuestions(questions) }
    }

    @Test
    fun `getQuestionsByCategory DAO'dan dönen listeyi döndürür`() = runTest {
        // Arrange
        val category = "History"
        val limit = 5
        val expectedQuestions = listOf(mockk<QuestionEntity>())
        coEvery { dao.getQuestionsByCategory(category, limit) } returns expectedQuestions

        // Act
        val result = localDataSource.getQuestionsByCategory(category, limit)

        // Assert
        assertThat(result).isEqualTo(expectedQuestions)
        coVerify(exactly = 1) { dao.getQuestionsByCategory(category, limit) }
    }

    @Test
    fun `deleteQuestionsByCategory DAO'nun deleteQuestionsByCategory fonksiyonunu çağırır`() = runTest {
        // Arrange
        val category = "Sports"
        coEvery { dao.deleteQuestionsByCategory(any()) } returns Unit

        // Act
        localDataSource.deleteQuestionsByCategory(category)

        // Assert
        coVerify(exactly = 1) { dao.deleteQuestionsByCategory(category) }
    }

    @Test
    fun `getQuestionCountByCategory DAO'dan dönen sayıyı döndürür`() = runTest {
        // Arrange
        val category = "Science"
        val expectedCount = 15
        coEvery { dao.getQuestionCountByCategory(category) } returns expectedCount

        // Act
        val result = localDataSource.getQuestionCountByCategory(category)

        // Assert
        assertThat(result).isEqualTo(expectedCount)
        coVerify(exactly = 1) { dao.getQuestionCountByCategory(category) }
    }

    @Test
    fun `insertCategories DAO'nun insertCategories fonksiyonunu çağırır`() = runTest {
        // Arrange
        val categories = listOf(mockk<CategoryEntity>())
        coEvery { dao.insertCategories(any()) } returns Unit

        // Act
        localDataSource.insertCategories(categories)

        // Assert
        coVerify(exactly = 1) { dao.insertCategories(categories) }
    }

    @Test
    fun `getAllCategories DAO'dan gelen Flow'u döndürür`() = runTest {
        // Arrange
        val expectedCategories = listOf(mockk<CategoryEntity>())
        coEvery { dao.getAllCategories() } returns flowOf(expectedCategories)

        // Act
        val resultFlow = localDataSource.getAllCategories()
        val result = resultFlow.first()

        // Assert
        assertThat(result).isEqualTo(expectedCategories)
    }

    @Test
    fun `getCategoryCount DAO'dan dönen sayıyı döndürür`() = runTest {
        // Arrange
        val expectedCount = 10
        coEvery { dao.getCategoryCount() } returns expectedCount

        // Act
        val result = localDataSource.getCategoryCount()

        // Assert
        assertThat(result).isEqualTo(expectedCount)
        coVerify(exactly = 1) { dao.getCategoryCount() }
    }

    @Test
    fun `getOldestQuestionTimestamp DAO'dan dönen zaman damgasını döndürür`() = runTest {
        // Arrange
        val category = "Geography"
        val expectedTimestamp = System.currentTimeMillis() - 10000
        coEvery { dao.getOldestQuestionTimestamp(category) } returns expectedTimestamp

        // Act
        val result = localDataSource.getOldestQuestionTimestamp(category)

        // Assert
        assertThat(result).isEqualTo(expectedTimestamp)
        coVerify(exactly = 1) { dao.getOldestQuestionTimestamp(category) }
    }

    @Test
    fun `getOldestQuestionTimestamp kategori boşsa null döndürür`() = runTest {
        // Arrange
        val category = "EmptyCategory"
        coEvery { dao.getOldestQuestionTimestamp(category) } returns null

        // Act
        val result = localDataSource.getOldestQuestionTimestamp(category)

        // Assert
        assertThat(result).isNull()
        coVerify(exactly = 1) { dao.getOldestQuestionTimestamp(category) }
    }
}