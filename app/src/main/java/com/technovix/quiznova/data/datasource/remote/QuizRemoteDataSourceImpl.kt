package com.technovix.quiznova.data.datasource.remote

import com.technovix.quiznova.data.remote.OpenTriviaApi
import com.technovix.quiznova.data.remote.dto.CategoriesResponse
import com.technovix.quiznova.data.remote.dto.OpenTriviaResponse
import com.technovix.quiznova.util.Resource
import java.io.IOException
import retrofit2.HttpException
import javax.inject.Inject

class QuizRemoteDataSourceImpl @Inject constructor(
    private val api: OpenTriviaApi
) : QuizRemoteDataSource {
    override suspend fun getQuestions(amount: Int, categoryId: Int?, difficulty: String?, type: String): Resource<OpenTriviaResponse> {
        return try {
            val response = api.getQuestions(amount, categoryId, difficulty, type)
            if (response.isSuccessful && response.body() != null) {
                // Open Trivia API'sinin kendi içinde bir response_code'u var, onu da kontrol edelim
                val triviaResponse = response.body()!!
                when (triviaResponse.responseCode) {
                    0 -> Resource.Success(triviaResponse) // Başarılı
                    1 -> Resource.Error("Yeterli soru bulunamadı. Farklı bir kategori veya daha az soru deneyin.") // No Results
                    2 -> Resource.Error("Geçersiz parametre gönderildi. Lütfen tekrar deneyin.") // Invalid Parameter
                    3 -> Resource.Error("Oturum anahtarı bulunamadı.") // Token Not Found (Bu uygulamada token kullanmıyoruz ama API dokümanında var)
                    4 -> Resource.Error("Oturum anahtarı süresi dolmuş. Lütfen kategoriyi tekrar seçin.") // Token Empty
                    else -> Resource.Error("Bilinmeyen bir API yanıt kodu alındı: ${triviaResponse.responseCode}")
                }
            } else {
                // HTTP hatası (4xx, 5xx)
                Resource.Error("API Hatası: ${response.code()} - ${response.message()}")
            }
        } catch (e: HttpException) {
            // Spesifik HTTP hataları (örn: 404 Not Found, 500 Server Error)
            val errorMsg = when(e.code()) {
                404 -> "İstenen kaynak bulunamadı (404)."
                500 -> "Sunucu hatası oluştu (500). Lütfen daha sonra tekrar deneyin."
                else -> "Beklenmeyen bir sunucu hatası (${e.code()}). Lütfen tekrar deneyin."
            }
            Resource.Error(errorMsg)
        } catch (e: IOException) {
            // Ağ bağlantısı sorunları (internet yok, sunucuya ulaşılamıyor vb.)
            Resource.Error("Ağ Bağlantısı Hatası. İnternetinizi kontrol edin veya daha sonra tekrar deneyin.")
        } catch (e: Exception) {
            // Diğer beklenmedik hatalar
            println("QuizRemoteDataSource Hata: ${e}") // Loglama için
            Resource.Error(e.localizedMessage ?: "Sorular alınırken bilinmeyen bir hata oluştu.")
        }
    }

    override suspend fun getCategories(): Resource<CategoriesResponse> {
        return try {
            val response = api.getCategories()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Kategori sunucusu yanıtı başarısız: Kod ${response.code()}")
            }
        } catch (e: HttpException) {
            val errorMsg = when(e.code()) {
                404 -> "Kategori kaynağı bulunamadı (404)."
                500 -> "Kategori sunucusunda hata oluştu (500)."
                else -> "Kategoriler alınırken beklenmeyen bir sunucu hatası (${e.code()})."
            }
            Resource.Error(errorMsg)
        } catch (e: IOException) {
            Resource.Error("Ağ Bağlantısı Hatası. Kategoriler alınamadı.")
        } catch (e: Exception) {
            println("QuizRemoteDataSource Kategori Hatası: ${e}")
            Resource.Error(e.localizedMessage ?: "Kategoriler alınırken bilinmeyen bir hata oluştu.")
        }
    }
}