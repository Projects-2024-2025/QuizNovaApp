package com.technovix.quiznova.data.repository

import android.R.attr.type
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.technovix.quiznova.data.datasource.local.QuizLocalDataSource
import com.technovix.quiznova.data.datasource.remote.QuizRemoteDataSource
import com.technovix.quiznova.data.local.entity.QuestionEntity
import com.technovix.quiznova.data.remote.dto.OpenTriviaResponse
import com.technovix.quiznova.data.remote.dto.QuestionDto
import com.technovix.quiznova.util.NetworkMonitor
import com.technovix.quiznova.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
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

    @MockK private lateinit var remoteDataSource: QuizRemoteDataSource
    @MockK private lateinit var localDataSource: QuizLocalDataSource
    @MockK private lateinit var networkMonitor: NetworkMonitor

    // Artık spyk değil, normal lateinit
    private lateinit var repository: QuizRepositoryImpl

    private val testCategoryName = "TestCategory"
    private val testCategoryId = 1
    private val testAmount = 5
    private val cacheExpiryMs = TimeUnit.HOURS.toMillis(1)

    @Before
    fun setUp() {
        // Normal başlatma, spyk yok
        repository = QuizRepositoryImpl(remoteDataSource, localDataSource, networkMonitor)
    }

    // --- getQuestions Testleri ---

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
        // DTO'yu basitçe mocklayabiliriz, içeriği önemli değil çünkü mapDtoToEntity'nin başarısız olmayacağını varsayıyoruz
        val fakeRemoteQuestionDto = mockk<QuestionDto> {
            every { question } returns "HTML Question & stuff" // Gerçekçi olmayan ama null olmayan değerler
            every { correctAnswer } returns "Correct Answer"
            every { incorrectAnswers } returns listOf("Wrong 1", "Wrong 2", "Wrong 3")
            every { type } returns "multiple"
            every { difficulty } returns "easy"
            every { category } returns "API Category"
        }
        val fakeRemoteResponse = OpenTriviaResponse(0, listOf(fakeRemoteQuestionDto))
        val fakeNewLocalQuestions = List(testAmount) { mockk<QuestionEntity>(relaxed = true) }

        // Local source davranışları
        coEvery { localDataSource.getQuestionsByCategory(testCategoryName, testAmount) } returns emptyList() andThen fakeNewLocalQuestions // Başta boş, sonra yeni
        coEvery { localDataSource.getOldestQuestionTimestamp(testCategoryName) } returns invalidTimestamp
        coEvery { localDataSource.deleteQuestionsByCategory(testCategoryName) } returns Unit // Silme başarılı
        coEvery { localDataSource.insertQuestions(any()) } returns Unit // Ekleme başarılı

        // Network ve Remote source davranışları
        every { networkMonitor.isOnline } returns flowOf(true)
        coEvery { remoteDataSource.getQuestions(testAmount, testCategoryId, null, "multiple") } returns Resource.Success(fakeRemoteResponse)

        // Act & Assert
        repository.getQuestions(testCategoryId, testCategoryName, testAmount).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)

            // Success ve YENİ yerel veri bekleniyor
            val successItem = awaitItem()
            assertThat(successItem).isInstanceOf(Resource.Success::class.java) // Burası artık geçmeli
            assertThat((successItem as Resource.Success).data).isEqualTo(fakeNewLocalQuestions)

            // Doğrulamalar
            coVerify(exactly = 1) { remoteDataSource.getQuestions(testAmount, testCategoryId, null, "multiple") }
            coVerify(exactly = 1) { localDataSource.deleteQuestionsByCategory(testCategoryName) }
            // insertQuestions çağrısını daha detaylı doğrulayabiliriz ama any() şimdilik yeterli
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

        // Local source davranışları
        coEvery { localDataSource.getQuestionsByCategory(testCategoryName, testAmount) } returns emptyList() andThen fakeNewLocalQuestions
        coEvery { localDataSource.getOldestQuestionTimestamp(testCategoryName) } returns null // Önbellek yok
        coEvery { localDataSource.deleteQuestionsByCategory(testCategoryName) } returns Unit
        coEvery { localDataSource.insertQuestions(any()) } returns Unit

        // Network ve Remote source davranışları
        every { networkMonitor.isOnline } returns flowOf(true)
        coEvery { remoteDataSource.getQuestions(testAmount, testCategoryId, null, "multiple") } returns Resource.Success(fakeRemoteResponse)

        // Act & Assert
        repository.getQuestions(testCategoryId, testCategoryName, testAmount).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)

            // Success ve YENİ yerel veri bekleniyor
            val successItem = awaitItem()
            assertThat(successItem).isInstanceOf(Resource.Success::class.java) // Burası artık geçmeli
            assertThat((successItem as Resource.Success).data).isEqualTo(fakeNewLocalQuestions)

            // Doğrulamalar
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

        // Local source davranışları (sürekli eskiyi dönecek)
        coEvery { localDataSource.getQuestionsByCategory(testCategoryName, testAmount) } returns fakeOldLocalQuestions
        coEvery { localDataSource.getOldestQuestionTimestamp(testCategoryName) } returns invalidTimestamp

        // Network ve Remote source davranışları
        every { networkMonitor.isOnline } returns flowOf(true)
        coEvery { remoteDataSource.getQuestions(any(), any(), any(), any()) } returns Resource.Error(apiErrorMessage)

        // Act & Assert
        repository.getQuestions(testCategoryId, testCategoryName, testAmount).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)

            // Error ve ESKİ yerel veri bekleniyor
            val errorItem = awaitItem()
            assertThat(errorItem).isInstanceOf(Resource.Error::class.java)
            val error = errorItem as Resource.Error
            assertThat(error.data).isEqualTo(fakeOldLocalQuestions)
            // Tam hata mesajını kontrol et
            val expectedMessage = "Veri güncellenemedi ($apiErrorMessage), eski veriler gösteriliyor."
            assertThat(error.message).isEqualTo(expectedMessage)

            // Doğrulamalar
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

        // Local source davranışları
        coEvery { localDataSource.getQuestionsByCategory(testCategoryName, testAmount) } returns fakeLocalQuestions
        coEvery { localDataSource.getOldestQuestionTimestamp(testCategoryName) } returns System.currentTimeMillis() // Geçerli timestamp

        // Network davranışları
        every { networkMonitor.isOnline } returns flowOf(false) // ÇEVRİMDIŞI

        // Act & Assert
        repository.getQuestions(testCategoryId, testCategoryName, testAmount).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)

            // Success ve yerel veri bekleniyor
            val successItem = awaitItem()
            assertThat(successItem).isInstanceOf(Resource.Success::class.java)
            assertThat((successItem as Resource.Success).data).isEqualTo(fakeLocalQuestions)

            // Doğrulamalar
            coVerify(exactly = 0) { remoteDataSource.getQuestions(any(), any(), any(), any()) }
            coVerify(exactly = 0) { localDataSource.deleteQuestionsByCategory(any()) }
            coVerify(exactly = 0) { localDataSource.insertQuestions(any()) }
            awaitComplete()
        }
    }

    @Test
    fun `getQuestions - çevrimdışı - önbellek yok - Error döner, API çağrılmaz`() = runTest {
        // Arrange
        // Local source davranışları
        coEvery { localDataSource.getQuestionsByCategory(testCategoryName, testAmount) } returns emptyList()
        coEvery { localDataSource.getOldestQuestionTimestamp(testCategoryName) } returns null

        // Network davranışları
        every { networkMonitor.isOnline } returns flowOf(false) // ÇEVRİMDIŞI

        // Act & Assert
        repository.getQuestions(testCategoryId, testCategoryName, testAmount).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)

            // Error ve doğru mesaj bekleniyor
            val errorItem = awaitItem()
            assertThat(errorItem).isInstanceOf(Resource.Error::class.java)
            val error = errorItem as Resource.Error
            assertThat(error.data).isNull()
            // Tam hata mesajını kontrol et
            assertThat(error.message).isEqualTo("Çevrimdışı moddasınız ve bu kategori için kayıtlı soru bulunamadı.")

            // Doğrulamalar
            coVerify(exactly = 0) { remoteDataSource.getQuestions(any(), any(), any(), any()) }
            awaitComplete()
        }
    }

    // --- getCategories Testleri ---
    // TODO: getCategories için de testler ekle (Benzer mantıkla: local var/yok, online/offline vb.)

    // --- mapDtoToEntity testi artık gerekli değil veya ViewModel'de test edilmeli ---
}