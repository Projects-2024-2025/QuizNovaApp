package com.technovix.quiznova.data.repository

import android.text.Html
import com.technovix.quiznova.data.datasource.local.QuizLocalDataSource
import com.technovix.quiznova.data.datasource.remote.QuizRemoteDataSource
import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.data.local.entity.QuestionEntity
import com.technovix.quiznova.data.remote.dto.QuestionDto
import com.technovix.quiznova.util.NetworkMonitor
import com.technovix.quiznova.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Uygulamada bu sınıftan sadece bir tane örnek (instance) olmasını sağlar.
class QuizRepositoryImpl @Inject constructor(
    // Junior Dev Notu: Artık doğrudan API veya DAO değil, DataSource'ları (yardımcıları) alıyoruz.
    private val remoteDataSource: QuizRemoteDataSource, // İnternet işleri için yardımcı
    private val localDataSource: QuizLocalDataSource,   // Telefon hafızası (DB) işleri için yardımcı
    private val networkMonitor: NetworkMonitor         // İnternet var mı yok mu kontrol eden araç
) : QuizRepository { // <<< QuizRepository interface'indeki sözleri tutacağını söylüyor

    // Sabit kategori listesi (API vermediği için)
    // Senior Dev: Sabit kategori listesi (OpenTDB API'si kategori listesi vermiyor)
    // Gerçek bir API varsa oradan çekilir. ID'ler OpenTDB'nin kullandığı ID'ler olmalı.
    private val hardcodedCategories = listOf(
        CategoryEntity(id = 9, name = "General Knowledge"),
        CategoryEntity(id = 10, name = "Entertainment: Books"),
        CategoryEntity(id = 11, name = "Entertainment: Film"),
        CategoryEntity(id = 12, name = "Entertainment: Music"),
        CategoryEntity(id = 17, name = "Science & Nature"),
        CategoryEntity(id = 18, name = "Science: Computers"),
        CategoryEntity(id = 21, name = "Sports"),
        CategoryEntity(id = 22, name = "Geography"),
        CategoryEntity(id = 23, name = "History"),
        CategoryEntity(id = 27, name = "Animals")
    )


    // --- KATEGORİLERİ GETİRME İŞİ ---
    override fun getCategories(): Flow<Resource<List<CategoryEntity>>> = flow {
        // 1. Önce UI'a "Yüklemeye başlıyorum" diyelim.
        emit(Resource.Loading())

        // 2. Kiler sorumlusuna (LocalDataSource) soralım: "Kategoriler var mı? Varsa sürekli haber ver."
        localDataSource.getAllCategories().collect { categories ->
            // 3. Kiler sorumlusu her cevap verdiğinde (başta veya değişiklik olunca), UI'a "İşte kategoriler" diyelim.
            emit(Resource.Success(categories))

            // 4. Eğer kiler bomboşsa ve daha önce hiç ekleme yapmadıysak, sabit listeyi ekleyelim.
            //    (Normalde burada internet varsa Market Sorumlusuna (RemoteDataSource) sorup yeni kategori var mı diye bakardık)
            if (categories.isEmpty() && localDataSource.getCategoryCount() == 0) {
                try {
                    localDataSource.insertCategories(hardcodedCategories)
                    // Not: Ekleme yapınca getAllCategories() Flow'u otomatik olarak yeni listeyi gönderecek, tekrar emit'e gerek yok.
                } catch (e: Exception) {
                    // Eklemede hata olursa logla (UI'a hata göndermiyoruz, çünkü cache'i doldurma işlemiydi)
                    println("Kategori ekleme hatası: ${e.localizedMessage}")
                }
            }
        }
    }.catch { e ->
        // 5. Bu işlemler sırasında beklenmedik bir hata olursa, UI'a "Hata oldu" diyelim.
        emit(Resource.Error("Kategoriler yüklenirken bilinmeyen bir hata oluştu: ${e.localizedMessage}"))
    }.flowOn(Dispatchers.IO) // 6. Tüm bu işleri arka planda (IO thread) yapalım ki uygulama kasmasın.

    // --- SORULARI GETİRME İŞİ ---
    override fun getQuestions(categoryId: Int, categoryName: String, amount: Int): Flow<Resource<List<QuestionEntity>>> = flow {
        // 1. UI'a "Soruları yüklemeye başlıyorum" diyelim.
        emit(Resource.Loading())

        // 2. İnternet kontrol aracına (NetworkMonitor) soralım: "İnternet var mı?"
        val isOnline = networkMonitor.isOnline.first() // O anki durumu alalım

        // 3. İNTERNET VARSA:
        if (isOnline) {
            // 4. Market sorumlusuna (RemoteDataSource) soralım: "Bu kategoriden şu kadar soru getirir misin?"
            when (val remoteResult = remoteDataSource.getQuestions(amount, categoryId, null, "multiple")) {
                // 5. Market sorumlusu "Başarıyla getirdim, al bakalım" (Success) derse:
                is Resource.Success -> {
                    try {
                        // 6. Önce kilerdeki (DB) bu kategoriye ait eski soruları silelim (Kiler Sorumlusu ile).
                        localDataSource.deleteQuestionsByCategory(categoryName)
                        // 7. Gelen yeni soruları (DTO) bizim anlayacağımız şekle (Entity) çevirelim (mapDtoToEntity ile).
                        val questionEntities = mapDtoToEntity(remoteResult.data!!.results, categoryName)
                        // 8. Yeni soruları kilere (DB) kaydedelim (Kiler Sorumlusu ile).
                        localDataSource.insertQuestions(questionEntities)
                        // 9. **ÖNEMLİ:** UI'a marketten gelen veriyi değil, KİLERE KAYDETTİĞİMİZ güncel veriyi gönderelim.
                        //    Buna "Single Source of Truth" (Tek Doğruluk Kaynağı) diyoruz. Her zaman DB'den okuruz.
                        emit(Resource.Success(localDataSource.getQuestionsByCategory(categoryName, amount)))
                    } catch(e: Exception) {
                        // DB yazma/okuma hatası olursa (nadiren), UI'a hata bildirelim.
                        emit(Resource.Error("Sorular işlenirken hata oluştu: ${e.localizedMessage}"))
                    }
                }
                // 10. Market sorumlusu "Getiremedim, şöyle bir hata oldu" (Error) derse:
                is Resource.Error -> {
                    // 11. Yardımcı fonksiyonu (handleDataSourceError) çağırıp durumu kontrol edelim.
                    //     Bu fonksiyon kilere bakacak, orada varsa hata mesajıyla birlikte onu dönecek, yoksa sadece hata mesajı dönecek.
                    emit(handleDataSourceError(remoteResult.message, categoryName, amount))
                }
                is Resource.Loading -> { /* Bu durum DataSource'dan gelmez */ }
            }
        }
        // 12. İNTERNET YOKSA:
        else {
            // 13. Doğrudan Kiler sorumlusuna (LocalDataSource) soralım: "Bu kategoriden soru var mı?"
            val localQuestions = localDataSource.getQuestionsByCategory(categoryName, amount)
            // 14. Kilerde soru VARSA:
            if (localQuestions.isNotEmpty()) {
                // 15. UI'a "İşte kilerden bulduklarım" diyelim.
                emit(Resource.Success(localQuestions))
            }
            // 16. Kilerde soru YOKSA:
            else {
                // 17. UI'a "İnternet yok ve kilerde de soru bulamadım" diyelim.
                emit(Resource.Error("Çevrimdışı moddasınız ve bu kategori için kayıtlı soru bulunamadı."))
            }
        }
    }.catch { e ->
        // 18. Tüm bu işlemler sırasında beklenmedik bir hata olursa, UI'a "Hata oldu" diyelim.
        emit(Resource.Error("Sorular yüklenirken bilinmeyen bir hata oluştu: ${e.localizedMessage}"))
    }.flowOn(Dispatchers.IO) // 19. Tüm bu işleri yine arka planda yapalım.

    // --- YARDIMCI FONKSİYON: API Hatası Durumunda Cache Kontrolü ---
    private suspend fun handleDataSourceError(errorMessage: String?, categoryName: String, amount: Int): Resource<List<QuestionEntity>> {
        // Kiler sorumlusuna (LocalDataSource) sor: "Bu kategoriden soru var mı?"
        val localQuestions = localDataSource.getQuestionsByCategory(categoryName, amount)
        return if (localQuestions.isNotEmpty()) {
            // Varsa: "API'dan alamadım ama kilerdekileri veriyorum, bilgin olsun (hata mesajı + veri)"
            Resource.Error("Veri güncellenemedi (${errorMessage ?: "Bilinmeyen Hata"}), eski veriler gösteriliyor.", localQuestions)
        } else {
            // Yoksa: "API'dan alamadım ve kiler de boş (sadece hata mesajı)"
            Resource.Error("Sorular yüklenemedi: ${errorMessage ?: "Bilinmeyen Hata"}")
        }
    }

    // --- YARDIMCI FONKSİYON: API Modelini Veritabanı Modeline Çevirme ---
    private fun mapDtoToEntity(dtos: List<QuestionDto>, categoryName: String): List<QuestionEntity> {
        // Junior Dev Notu: Bu fonksiyon API'dan gelen veriyi (DTO) alıp,
        // veritabanına kaydedeceğimiz hale (Entity) getirir.
        // - HTML kodlarını temizler (örn: " yerine ").
        // - Cevap şıklarını karıştırır.
        return dtos.map { dto ->
            val decodedQuestion = Html.fromHtml(dto.question, Html.FROM_HTML_MODE_LEGACY).toString()
            val decodedCorrectAnswer = Html.fromHtml(dto.correctAnswer, Html.FROM_HTML_MODE_LEGACY).toString()
            val decodedIncorrectAnswers = dto.incorrectAnswers.map { Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY).toString() }
            val allAnswers = (decodedIncorrectAnswers + decodedCorrectAnswer).shuffled() // Şıkları karıştır
            QuestionEntity(
                category = categoryName, type = dto.type, difficulty = dto.difficulty, question = decodedQuestion,
                correctAnswer = decodedCorrectAnswer, incorrectAnswers = decodedIncorrectAnswers, allAnswers = allAnswers
            )
        }
    }
}