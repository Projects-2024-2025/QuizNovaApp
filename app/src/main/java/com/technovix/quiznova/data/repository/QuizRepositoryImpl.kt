package com.technovix.quiznova.data.repository

import android.text.Html
import com.technovix.quiznova.data.datasource.local.QuizLocalDataSource
import com.technovix.quiznova.data.datasource.remote.QuizRemoteDataSource
import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.data.local.entity.QuestionEntity
import com.technovix.quiznova.data.remote.dto.CategoryDto
import com.technovix.quiznova.data.remote.dto.QuestionDto
import com.technovix.quiznova.util.NetworkMonitor
import com.technovix.quiznova.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepositoryImpl @Inject constructor(

    private val remoteDataSource: QuizRemoteDataSource,
    private val localDataSource: QuizLocalDataSource,
    private val networkMonitor: NetworkMonitor
) : QuizRepository {

    // Önbellek geçerlilik süresi (örneğin 1 saat)
    private val CACHE_EXPIRY_MS = TimeUnit.HOURS.toMillis(1)

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

    override fun getCategories(): Flow<Resource<List<CategoryEntity>>> = flow {
        emit(Resource.Loading())

        // 1. Her zaman önce yerel veritabanından akışı başlat
        //    Bu, veritabanı güncellendiğinde UI'ın otomatik yenilenmesini sağlar.
        val localCategoriesFlow = localDataSource.getAllCategories()

        // 2. Yerel veriyi dinlemeye başla ve ilk gelen veriyi emit et
        //    Ancak ağ kontrolü ve API çağrısı için akışı hemen bitirme.
        var emittedInitialLocalData = false
        localCategoriesFlow.collect { localCategories ->
            // Eğer yerelde kategori varsa, onu Success olarak gönder.
            // Eğer henüz başlangıç verisi gönderilmediyse VEYA
            // zaten gönderilmişti ama liste boş değilse (güncelleme geldi demektir) gönder.
            if (!emittedInitialLocalData || localCategories.isNotEmpty()) {
                println("QuizRepository: Yerel kategoriler emit ediliyor (sayı: ${localCategories.size}).")
                emit(Resource.Success(localCategories))
                emittedInitialLocalData = true // Başlangıç verisinin gönderildiğini işaretle
            }

            // 3. Yerel veri (ilk başta) boşsa VEYA belki periyodik güncelleme istenseydi, API'yi kontrol et.
            //    Şimdilik sadece yerel veri ilk başta boşsa API'ye gidelim.
            if (localCategories.isEmpty() && emittedInitialLocalData) { // emittedInitialLocalData kontrolü ilk collect'te API'ye gitmeyi sağlar
                println("QuizRepository: Yerel kategoriler boş, uzak kaynak deneniyor...")
                fetchAndCacheCategories() // API'den çekip kaydetme fonksiyonunu çağır
            }
        }

        /*localDataSource.getAllCategories().collect { categories ->
            emit(Resource.Success(categories))
            if (categories.isEmpty() && localDataSource.getCategoryCount() == 0) {
                try {
                    localDataSource.insertCategories(hardcodedCategories)
                } catch (e: Exception) {
                    println("Kategori ekleme hatası: ${e.localizedMessage}")
                }
            }
        }*/
    }.catch { e ->
        emit(Resource.Error("Kategoriler yüklenirken bilinmeyen bir hata oluştu: ${e.localizedMessage}"))
    }.flowOn(Dispatchers.IO)

    // API'den kategorileri çekip veritabanına kaydeden yardımcı fonksiyon
    private suspend fun fetchAndCacheCategories() {
        // Sadece çevrimiçiyse API'ye git
        if (networkMonitor.isOnline.first()) {
            when (val remoteResult = remoteDataSource.getCategories()) {
                is Resource.Success -> {
                    try {
                        println("QuizRepository: Uzak kategoriler başarıyla alındı. Kaydediliyor...")
                        val categoryEntities = mapCategoryDtoToEntity(remoteResult.data!!.trivia_categories)
                        // Eski kategorileri silmeye gerek yok, REPLACE stratejisi güncelleyecektir.
                        localDataSource.insertCategories(categoryEntities)
                        // Kayıt başarılı, Flow zaten dinlediği için yeni veriyi otomatik emit edecek.
                    } catch (e: Exception) {
                        println("QuizRepository Kategori Hatası: Kategoriler kaydedilirken hata: ${e.localizedMessage}")
                        // Burada özel bir hata emit etmeye gerek yok, catch bloğu genel hatayı yakalar.
                    }
                }
                is Resource.Error -> {
                    println("QuizRepository Kategori Hatası: Uzak kategoriler alınamadı: ${remoteResult.message}")
                    // API hatası durumunda belki eski kategoriler varsa UI onları göstermeye devam eder.
                    // Özel bir hata emit etmeye gerek yok.
                }
                is Resource.Loading -> {}
            }
        } else {
            println("QuizRepository: Kategorileri çekmek için çevrimdışı.")
            // Çevrimdışıysak ve yerel veri de boşsa, akış zaten yukarıda boş Success veya Loading'de kalmıştır.
            // Kullanıcıya bir şekilde çevrimdışı olduğunu bildirmek UI katmanının işi olabilir.
        }
    }

    // CategoryDto'yu CategoryEntity'ye çeviren yardımcı fonksiyon
    private fun mapCategoryDtoToEntity(dtos: List<CategoryDto>): List<CategoryEntity> {
        return dtos.map { dto ->
            CategoryEntity(id = dto.id, name = dto.name)
        }
    }

    override fun getQuestions(categoryId: Int, categoryName: String, amount: Int): Flow<Resource<List<QuestionEntity>>> = flow {
        emit(Resource.Loading())

        // Önce yerel veriyi kontrol et (hem çevrimdışı hem de taze önbellek durumu için)
        val localQuestions = localDataSource.getQuestionsByCategory(categoryName, amount)
        val oldestTimestamp = localDataSource.getOldestQuestionTimestamp(categoryName)
        val now = System.currentTimeMillis()

        // Önbellek var mı ve hala geçerli mi?
        val isCacheValid = oldestTimestamp != null && (now - oldestTimestamp < CACHE_EXPIRY_MS)
        val hasEnoughCache = localQuestions.isNotEmpty() && localQuestions.size >= amount

        // Eğer geçerli önbellek varsa ve yeterli soru içeriyorsa, onu kullan
        // (amount kontrolü, belki daha önce daha az soru istenmiş olabilir)
        if (hasEnoughCache && isCacheValid) {
            println("QuizRepository: Geçerli önbellek kullanılıyor ($categoryName).")
            emit(Resource.Success(localQuestions))
            return@flow // Akışı burada bitir, API'ye gitme
        }

        // Geçerli önbellek yoksa veya yeterli değilse, ağ durumunu kontrol et
        val isOnline = networkMonitor.isOnline.first()

        if (isOnline) {
            println("QuizRepository: Ağ bağlantısı var, uzak veri kaynağı deneniyor ($categoryName)...")
            // Ağ varsa, uzak veri kaynağını dene
            when (val remoteResult = remoteDataSource.getQuestions(amount, categoryId, null, "multiple")) {
                is Resource.Success -> {
                    println("QuizRepository: Uzak veri başarıyla alındı ($categoryName). Önbellek güncelleniyor...")
                    try {
                        localDataSource.deleteQuestionsByCategory(categoryName)
                        val questionEntities = mapDtoToEntity(remoteResult.data!!.results, categoryName)
                        localDataSource.insertQuestions(questionEntities)
                        val updatedLocalQuestions = localDataSource.getQuestionsByCategory(categoryName, amount)
                        if (updatedLocalQuestions.isNotEmpty()) {
                            emit(Resource.Success(updatedLocalQuestions))
                        } else {
                            // Bu durum aslında olmamalı ama API 0 sonuç döndürürse olabilir
                            println("QuizRepository Uyarı: API başarı dedi ama sonuç listesi boş geldi? ($categoryName)")
                            emit(Resource.Error("Yeterli soru bulunamadı. Farklı bir kategori deneyin."))
                        }
                    } catch(e: Exception) {
                        println("QuizRepository Hata: Sorular işlenirken/kaydedilirken hata: ${e.localizedMessage}")
                        // Hata durumunda bile, belki hala eski (ama süresi dolmuş) yerel veri vardır?
                        if (localQuestions.isNotEmpty()){
                            emit(Resource.Error("Sorular güncellenirken hata oluştu, eski veriler gösteriliyor: ${e.localizedMessage}", localQuestions))
                        } else {
                            emit(Resource.Error("Sorular işlenirken hata oluştu: ${e.localizedMessage}"))
                        }
                    }
                }
                is Resource.Error -> {
                    println("QuizRepository Hata: Uzak veri alınamadı ($categoryName): ${remoteResult.message}")
                    // API hatası durumunda, yerelde veri varsa onu hata mesajıyla birlikte göster
                    emit(handleDataSourceError(remoteResult.message, categoryName, amount))
                }
                is Resource.Loading -> { /* Bu durum DataSource'dan gelmez */ }
            }
        }
        else {
            println("QuizRepository: Ağ bağlantısı yok ($categoryName). Yerel önbellek kontrol ediliyor...")
            // Ağ yoksa, sadece yerel veriyi kullanmayı dene (zaten başta kontrol etmiştik ama burada tekrar edelim)
            if (localQuestions.isNotEmpty()) {
                println("QuizRepository: Çevrimdışı modda yerel önbellek kullanılıyor ($categoryName).")
                emit(Resource.Success(localQuestions))
            }
            else {
                println("QuizRepository: Çevrimdışı modda yerel önbellek bulunamadı ($categoryName).")
                emit(Resource.Error("Çevrimdışı moddasınız ve bu kategori için kayıtlı soru bulunamadı."))
            }
        }
    }.catch { e ->
        println("QuizRepository Hata: getQuestions akışında bilinmeyen hata: ${e.localizedMessage}")
        emit(Resource.Error("Sorular yüklenirken bilinmeyen bir hata oluştu: ${e.localizedMessage}"))
    }.flowOn(Dispatchers.IO)

    private suspend fun handleDataSourceError(errorMessage: String?, categoryName: String, amount: Int): Resource<List<QuestionEntity>> {
        val localQuestions = localDataSource.getQuestionsByCategory(categoryName, amount)
        val baseMessage = errorMessage ?: "Bilinmeyen bir sunucu veya ağ hatası oluştu."
        return if (localQuestions.isNotEmpty()) {
            Resource.Error("Veri güncellenemedi (${baseMessage}), eski veriler gösteriliyor.", localQuestions)
        } else {
            Resource.Error("Sorular yüklenemedi: ${baseMessage}")
        }
    }

    private fun mapDtoToEntity(dtos: List<QuestionDto>, categoryName: String): List<QuestionEntity> {
        return dtos.map { dto ->
            //val decodedQuestion = Html.fromHtml(dto.question, Html.FROM_HTML_MODE_LEGACY).toString()
            //val decodedCorrectAnswer = Html.fromHtml(dto.correctAnswer, Html.FROM_HTML_MODE_LEGACY).toString()
            //val decodedIncorrectAnswers = dto.incorrectAnswers.map { Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY).toString() }
            val allAnswers = (dto.incorrectAnswers + dto.correctAnswer).shuffled()
            QuestionEntity(
                category = categoryName, type = dto.type, difficulty = dto.difficulty, question = dto.question,
                correctAnswer = dto.correctAnswer, incorrectAnswers = dto.incorrectAnswers, allAnswers = allAnswers
            )
        }
    }
}