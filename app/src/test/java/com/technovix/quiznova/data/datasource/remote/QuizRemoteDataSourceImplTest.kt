package com.technovix.quiznova.data.datasource.remote

import com.google.common.truth.Truth.assertThat
import com.technovix.quiznova.data.remote.OpenTriviaApi
import com.technovix.quiznova.data.remote.dto.OpenTriviaResponse
import com.technovix.quiznova.data.remote.dto.QuestionDto
import com.technovix.quiznova.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
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

    private lateinit var dataSource: QuizRemoteDataSourceImpl

    @Before
    fun setUp() {
        // Annotation ile oluşturulan mock'ları başlat
        io.mockk.MockKAnnotations.init(this, relaxUnitFun = true)  //relaxUnitFun = true, Unit dönen fonksiyonları otomatik mock'lar
        dataSource = QuizRemoteDataSourceImpl(api)
    }

    @Test
    fun `getQuestions API başarılı olduğunda Success döner`() = runTest {
        // Arrange: API'nin başarılı bir yanıt döndüreceğini ayarla
        val mockQuestionDto = mockk<QuestionDto>()
        val mockResponseDto = OpenTriviaResponse(responseCode = 0, results = listOf(mockQuestionDto))
        val successResponse: Response<OpenTriviaResponse> = Response.success(mockResponseDto)
        coEvery { api.getQuestions(any(), any(), any(), any()) } returns successResponse

        // Act: DataSource fonksiyonunu çağır
        var result = dataSource.getQuestions(10, null, null, "multiple")

        // Assert: Sonucun Success olduğunu ve doğru veriyi içerdiğini kontrol et
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat((result as Resource.Success).data).isEqualTo(mockResponseDto)
        // API fonksiyonunun doğru parametrelerle çağrıldığını doğrula
        coVerify { api.getQuestions(10, null, null, "multiple") }
    }

    @Test
    fun `getQuestions API response_code 1 döndürdüğünde Error döner`() = runTest {
        // Arrange: API'nin response_code=1 ile yanıt döndüreceğini ayarla
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
        // Arrange: API'nin başarısız bir HTTP yanıtı döndüreceğini ayarla
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
        // Arrange: API çağrısının HttpException fırlatacağını ayarla
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
        // Arrange: API çağrısının IOException fırlatacağını ayarla
        val ioException = IOException("Network Error")
        coEvery { api.getQuestions(any(), any(), any(), any()) } throws ioException

        // Act
        val result = dataSource.getQuestions(10, null, null, "multiple")

        // Assert
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat((result as Resource.Error).message).isEqualTo("Ağ Bağlantısı Hatası. İnternetinizi kontrol edin veya daha sonra tekrar deneyin.")
    }
}