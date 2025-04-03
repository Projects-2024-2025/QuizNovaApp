package com.technovix.quiznova.data.remote

import com.technovix.quiznova.data.remote.dto.OpenTriviaResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenTriviaApi {

    // Senior Dev: API endpoint'lerimizi burada tanımlıyoruz. @GET, @POST gibi annotation'lar
    // ve @Query, @Path, @Body gibi parametre annotation'ları kullanırız.
    // Response<T> kullanmak, HTTP durum kodunu (200 OK, 404 Not Found vs.) ve
    // header'ları kontrol etmemizi sağlar. Hata durumlarını daha iyi yönetiriz.
    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int, // Kaç soru çekileceği
        @Query("category") categoryId: Int? = null, // Belirli bir kategori ID'si (opsiyonel)
        @Query("difficulty") difficulty: String? = null, // Zorluk seviyesi (opsiyonel)
        @Query("type") type: String = "multiple" // Sadece çoktan seçmeli istiyoruz
    ): Response<OpenTriviaResponse>

    // Kategorileri çekecek endpoint (varsa)
    // @GET("api_category.php")
    // suspend fun getCategories(): Response<CategoriesResponse>
}