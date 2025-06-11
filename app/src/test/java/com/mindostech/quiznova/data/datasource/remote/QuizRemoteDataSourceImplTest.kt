package com.mindostech.quiznova.data.datasource.remote

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.mindostech.quiznova.data.remote.OpenTriviaApi
import com.mindostech.quiznova.data.remote.dto.OpenTriviaResponse
import com.mindostech.quiznova.data.remote.dto.QuestionDto
import com.mindostech.quiznova.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class QuizRemoteDataSourceImplTest {
    @MockK
    private lateinit var api: OpenTriviaApi

    @RelaxedMockK
    lateinit var mockContext: Context

    private lateinit var dataSource: QuizRemoteDataSourceImpl

    @Before
    fun setUp() {
        io.mockk.MockKAnnotations.init(this, relaxUnitFun = true)
        dataSource = QuizRemoteDataSourceImpl(mockContext, api)
    }

    @Test
    fun `getQuestions API başarılı olduğunda Success döner`() = runTest {
        // Arrange
        val mockQuestionDto = mockk<QuestionDto>()
        val mockResponseDto = OpenTriviaResponse(responseCode = 0, results = listOf(mockQuestionDto))
        val successResponse: Response<OpenTriviaResponse> = Response.success(mockResponseDto)
        coEvery { api.getQuestions(any(), any(), any(), any()) } returns successResponse

        // Act
        var result = dataSource.getQuestions(10, null, null, "multiple")

        // Assert
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat((result as Resource.Success).data).isEqualTo(mockResponseDto)
        coVerify { api.getQuestions(10, null, null, "multiple") }
    }

    @Test
    fun `getQuestions API response_code 1 döndürdüğünde Error döner`() = runTest {
        // Arrange
        val mockResponseDto = OpenTriviaResponse(responseCode = 1, results = emptyList())
        val successResponse: Response<OpenTriviaResponse> = Response.success(mockResponseDto)
        coEvery { api.getQuestions(any(), any(), any(), any()) } returns successResponse

        // Act
        val result = dataSource.getQuestions(10, null, null, "multiple")

        // Assert
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).contains("Yeterli soru bulunamadı")
    }

    @Test
    fun `getQuestions API başarısız yanıt döndürdüğünde Error döner`() = runTest {
        // Arrange
        val errorResponseBody = """{"error": "Not Found"}""".toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse: Response<OpenTriviaResponse> = Response.error(404, errorResponseBody)
        coEvery { api.getQuestions(any(), any(), any(), any()) } returns errorResponse

        // Act
        val result = dataSource.getQuestions(10, null, null, "multiple")

        // Assert
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).startsWith("API Hatası: 404")
    }

    @Test
    fun `getQuestions HttpException fırlattığında Error döner`() = runTest {
        // Arrange
        val errorResponseBody = "".toResponseBody(null)
        val httpException = HttpException(Response.error<Any>(500, errorResponseBody))
        coEvery { api.getQuestions(any(), any(), any(), any()) } throws httpException

        // Act
        val result = dataSource.getQuestions(10, null, null, "multiple")

        // Assert
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).contains("Sunucu hatası oluştu (500)")
    }

    @Test
    fun `getQuestions IOException fırlattığında Error döner`() = runTest {
        // Arrang
        val ioException = IOException("Network Error")
        coEvery { api.getQuestions(any(), any(), any(), any()) } throws ioException

        // Act
        val result = dataSource.getQuestions(10, null, null, "multiple")

        // Assert
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).isEqualTo("Ağ Bağlantısı Hatası. İnternetinizi kontrol edin veya daha sonra tekrar deneyin.")
    }
}