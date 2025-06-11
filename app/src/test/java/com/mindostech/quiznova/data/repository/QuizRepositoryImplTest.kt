package com.mindostech.quiznova.data.repository

import android.content.Context
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mindostech.quiznova.data.datasource.local.QuizLocalDataSource
import com.mindostech.quiznova.data.datasource.remote.QuizRemoteDataSource
import com.mindostech.quiznova.data.local.entity.QuestionEntity
import com.mindostech.quiznova.data.remote.dto.OpenTriviaResponse
import com.mindostech.quiznova.data.remote.dto.QuestionDto
import com.mindostech.quiznova.util.NetworkMonitor
import com.mindostech.quiznova.util.Resource
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit


@ExperimentalCoroutinesApi
class QuizRepositoryImplTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    lateinit var mockContext: Context

    @RelaxedMockK
    lateinit var remoteDataSource: QuizRemoteDataSource

    @RelaxedMockK
    lateinit var localDataSource: QuizLocalDataSource

    @RelaxedMockK
    lateinit var networkMonitor: NetworkMonitor

    private lateinit var repository: QuizRepositoryImpl

    private val testCategoryName = "TestCategory"
    private val testCategoryId = 1
    private val testAmount = 5
    private val cacheExpiryMs = TimeUnit.HOURS.toMillis(1)

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        repository = QuizRepositoryImpl(
            context = mockContext,
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            networkMonitor = networkMonitor
        )
    }


    @Test
    fun `getQuestions - çevrimiçi, önbellek geçerli ve yeterli - yerel veri döner, API çağrılmaz`() = runTest {
        // Arrange
        val currentTime = System.currentTimeMillis()
        val validTimestamp = currentTime - (cacheExpiryMs / 2)
        val fakeLocalQuestions = List(testAmount) { mockk<QuestionEntity>(relaxed = true) }

        coEvery { localDataSource.getQuestionsByCategory(testCategoryName, testAmount) } returns fakeLocalQuestions
        coEvery { localDataSource.getOldestQuestionTimestamp(testCategoryName) } returns validTimestamp
        every { networkMonitor.isOnline } returns flowOf(true)

        // Act & Assert
        repository.getQuestions(testCategoryId, testCategoryName, testAmount).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val successItem = awaitItem()
            assertThat(successItem).isInstanceOf(Resource.Success::class.java)
            assertThat((successItem as Resource.Success).data).isEqualTo(fakeLocalQuestions)

            coVerify(exactly = 0) { remoteDataSource.getQuestions(any(), any(), any(), any()) }
            coVerify(exactly = 0) { localDataSource.deleteQuestionsByCategory(any()) }
            coVerify(exactly = 0) { localDataSource.insertQuestions(any()) }
            awaitComplete()
        }
    }

    @Test
    fun `getQuestions - çevrimiçi, önbellek geçersiz - API başarılı - veri kaydedilir ve döner`() = runTest {
        // Arrange
        val invalidTimestamp = System.currentTimeMillis() - (cacheExpiryMs * 2)
        val fakeRemoteQuestionDto = mockk<QuestionDto> {
            every { question } returns "HTML Question & stuff"
            every { correctAnswer } returns "Correct Answer"
            every { incorrectAnswers } returns listOf("Wrong 1", "Wrong 2", "Wrong 3")
            every { type } returns "multiple"
            every { difficulty } returns "easy"
            every { category } returns "API Category"
        }
        val fakeRemoteResponse = OpenTriviaResponse(0, listOf(fakeRemoteQuestionDto))
        val fakeNewLocalQuestions = List(testAmount) { mockk<QuestionEntity>(relaxed = true) }

        coEvery { localDataSource.getQuestionsByCategory(testCategoryName, testAmount) } returns emptyList() andThen fakeNewLocalQuestions
        coEvery { localDataSource.getOldestQuestionTimestamp(testCategoryName) } returns invalidTimestamp
        coEvery { localDataSource.deleteQuestionsByCategory(testCategoryName) } returns Unit
        coEvery { localDataSource.insertQuestions(any()) } returns Unit

        every { networkMonitor.isOnline } returns flowOf(true)
        coEvery { remoteDataSource.getQuestions(testAmount, testCategoryId, null, "multiple") } returns Resource.Success(fakeRemoteResponse)

        // Act & Assert
        repository.getQuestions(testCategoryId, testCategoryName, testAmount).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)

            val successItem = awaitItem()
            assertThat(successItem).isInstanceOf(Resource.Success::class.java)
            assertThat((successItem as Resource.Success).data).isEqualTo(fakeNewLocalQuestions)

            coVerify(exactly = 1) { remoteDataSource.getQuestions(testAmount, testCategoryId, null, "multiple") }
            coVerify(exactly = 1) { localDataSource.deleteQuestionsByCategory(testCategoryName) }
            coVerify(exactly = 1) { localDataSource.insertQuestions(any()) }
            awaitComplete()
        }
    }

    @Test
    fun `getQuestions - çevrimiçi, önbellek yok - API başarılı - veri kaydedilir ve döner`() = runTest {
        // Arrange
        val realRemoteQuestionDto = QuestionDto(
            type = "multiple",
            difficulty = "easy",
            category = "Test API Category",
            question = "This is a test question?",
            correctAnswer = "Correct",
            incorrectAnswers = listOf("Wrong1", "Wrong2", "Wrong3")
        )
        val fakeRemoteResponse = OpenTriviaResponse(0, listOf(realRemoteQuestionDto))
        val fakeNewLocalQuestions = List(testAmount) { mockk<QuestionEntity>(relaxed = true) }

        coEvery { localDataSource.getQuestionsByCategory(testCategoryName, testAmount) } returns emptyList() andThen fakeNewLocalQuestions
        coEvery { localDataSource.getOldestQuestionTimestamp(testCategoryName) } returns null
        coEvery { localDataSource.deleteQuestionsByCategory(testCategoryName) } returns Unit
        coEvery { localDataSource.insertQuestions(any()) } returns Unit

        every { networkMonitor.isOnline } returns flowOf(true)
        coEvery { remoteDataSource.getQuestions(testAmount, testCategoryId, null, "multiple") } returns Resource.Success(fakeRemoteResponse)

        // Act & Assert
        repository.getQuestions(testCategoryId, testCategoryName, testAmount).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)

            val successItem = awaitItem()
            assertThat(successItem).isInstanceOf(Resource.Success::class.java)
            assertThat((successItem as Resource.Success).data).isEqualTo(fakeNewLocalQuestions)

            coVerify(exactly = 1) { remoteDataSource.getQuestions(testAmount, testCategoryId, null, "multiple") }
            coVerify(exactly = 1) { localDataSource.deleteQuestionsByCategory(testCategoryName) }
            coVerify(exactly = 1) { localDataSource.insertQuestions(any()) }
            awaitComplete()
        }
    }

    @Test
    fun `getQuestions - çevrimiçi, önbellek geçersiz - API başarısız (eski veri var) - Error ve eski veri döner`() = runTest {
        // Arrange
        val invalidTimestamp = System.currentTimeMillis() - (cacheExpiryMs * 2)
        val fakeOldLocalQuestions = listOf(mockk<QuestionEntity>(relaxed = true))
        val apiErrorMessage = "API Hatası 500"

        coEvery { localDataSource.getQuestionsByCategory(testCategoryName, testAmount) } returns fakeOldLocalQuestions
        coEvery { localDataSource.getOldestQuestionTimestamp(testCategoryName) } returns invalidTimestamp

        every { networkMonitor.isOnline } returns flowOf(true)
        coEvery { remoteDataSource.getQuestions(any(), any(), any(), any()) } returns Resource.Error(apiErrorMessage)

        // Act & Assert
        repository.getQuestions(testCategoryId, testCategoryName, testAmount).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)

            val errorItem = awaitItem()
            assertThat(errorItem).isInstanceOf(Resource.Error::class.java)
            val error = errorItem as Resource.Error
            assertThat(error.data).isEqualTo(fakeOldLocalQuestions)
            val expectedMessage = "Veri güncellenemedi ($apiErrorMessage), eski veriler gösteriliyor."
            assertThat(error.message).isEqualTo(expectedMessage)

            coVerify(exactly = 1) { remoteDataSource.getQuestions(any(), any(), any(), any()) }
            coVerify(exactly = 0) { localDataSource.deleteQuestionsByCategory(any()) }
            coVerify(exactly = 0) { localDataSource.insertQuestions(any()) }
            awaitComplete()
        }
    }

    @Test
    fun `getQuestions - çevrimdışı - önbellek var - yerel veri döner, API çağrılmaz`() = runTest {
        // Arrange
        val fakeLocalQuestions = List(testAmount) { mockk<QuestionEntity>(relaxed = true) }

        coEvery { localDataSource.getQuestionsByCategory(testCategoryName, testAmount) } returns fakeLocalQuestions
        coEvery { localDataSource.getOldestQuestionTimestamp(testCategoryName) } returns System.currentTimeMillis()

        every { networkMonitor.isOnline } returns flowOf(false)

        // Act & Assert
        repository.getQuestions(testCategoryId, testCategoryName, testAmount).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)

            val successItem = awaitItem()
            assertThat(successItem).isInstanceOf(Resource.Success::class.java)
            assertThat((successItem as Resource.Success).data).isEqualTo(fakeLocalQuestions)

            coVerify(exactly = 0) { remoteDataSource.getQuestions(any(), any(), any(), any()) }
            coVerify(exactly = 0) { localDataSource.deleteQuestionsByCategory(any()) }
            coVerify(exactly = 0) { localDataSource.insertQuestions(any()) }
            awaitComplete()
        }
    }

    @Test
    fun `getQuestions - çevrimdışı - önbellek yok - Error döner, API çağrılmaz`() = runTest {
        // Arrange
        coEvery { localDataSource.getQuestionsByCategory(testCategoryName, testAmount) } returns emptyList()
        coEvery { localDataSource.getOldestQuestionTimestamp(testCategoryName) } returns null

        every { networkMonitor.isOnline } returns flowOf(false)

        // Act & Assert
        repository.getQuestions(testCategoryId, testCategoryName, testAmount).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)

            val errorItem = awaitItem()
            assertThat(errorItem).isInstanceOf(Resource.Error::class.java)
            val error = errorItem as Resource.Error
            assertThat(error.data).isNull()
            assertThat(error.message).isEqualTo("Çevrimdışı moddasınız ve bu kategori için kayıtlı soru bulunamadı.")

            coVerify(exactly = 0) { remoteDataSource.getQuestions(any(), any(), any(), any()) }
            awaitComplete()
        }
    }
}