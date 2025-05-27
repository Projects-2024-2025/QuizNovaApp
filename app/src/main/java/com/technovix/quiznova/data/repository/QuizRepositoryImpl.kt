package com.technovix.quiznova.data.repository

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
import android.content.Context
import com.technovix.quiznova.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import timber.log.Timber


@Singleton
class QuizRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteDataSource: QuizRemoteDataSource,
    private val localDataSource: QuizLocalDataSource,
    private val networkMonitor: NetworkMonitor
) : QuizRepository {

    // Önbellek geçerlilik süresi (örneğin 1 saat)
    private val CACHE_EXPIRY_MS = TimeUnit.HOURS.toMillis(1)

    override fun getCategories(): Flow<Resource<List<CategoryEntity>>> = channelFlow {
        // 1. Her zaman ilk olarak yükleme durumunu gönder
        send(Resource.Loading())
        Timber.d("QuizRepository: getCategories - Loading durumu gönderildi")

        var initialCacheChecked = false
        var hasEmittedDataFromCache = false

        // 2. Yerel veritabanındaki değişiklikleri dinle ve UI'a yansıt
        val localDataJob = launch { // Ayrı bir coroutine'de yerel veriyi dinle
            localDataSource.getAllCategories()
                .distinctUntilChanged() // Sadece gerçekten değiştiğinde emit et
                .collect { localCategories ->
                    initialCacheChecked = true // Cache en az bir kez kontrol edildi
                    if (localCategories.isNotEmpty()) {
                        Timber.d("QuizRepository: Yerel kategoriler alındı (${localCategories.size} adet). Success durumu gönderiliyor.")
                        send(Resource.Success(localCategories))
                        hasEmittedDataFromCache = true
                    } else if (hasEmittedDataFromCache) {
                        Timber.d("QuizRepository: Yerel kategoriler daha önce doluydu, şimdi boş. Success(emptyList) gönderiliyor.")
                        send(Resource.Success(emptyList()))
                    }
                }
        }

        // 3. Ağdan veri çekmeyi ve cache'i güncellemeyi dene
        try {
            if (networkMonitor.isOnline.first()) {
                Timber.d("QuizRepository: Ağ bağlantısı var, uzak kategori kaynağı deneniyor...")
                when (val remoteResult = remoteDataSource.getCategories()) {
                    is Resource.Success -> {
                        val categoryDtos = remoteResult.data?.trivia_categories
                        if (categoryDtos != null) {
                            Timber.d("QuizRepository: Uzak kategoriler başarıyla alındı (${categoryDtos.size} adet). Kaydediliyor...")
                            val categoryEntities = mapCategoryDtoToEntity(categoryDtos)

                            localDataSource.insertCategories(categoryEntities) // Veya clearAndInsertCategories
                        } else {
                            Timber.e("QuizRepository: Uzak kategoriler null veya data.trivia_categories null geldi.")
                            if (initialCacheChecked && !hasEmittedDataFromCache) {
                                send(Resource.Error(context.getString(R.string.error_categories_empty_from_server)))
                            }
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("QuizRepository Kategori Hatası: Uzak kategoriler alınamadı: ${remoteResult.message}")
                        // Ağdan veri alınamadı, cache boş değilse kullanıcı cache'i görmeye devam eder.
                        // Eğer cache de boşsa (initialCacheChecked true ve hasEmittedDataFromCache false ise) hata ver.
                        if (initialCacheChecked && !hasEmittedDataFromCache) {
                            send(Resource.Error(remoteResult.message ?: context.getString(R.string.error_categories_fetch_failed)))
                        }
                    }
                    is Resource.Loading -> { /* Bu Resource.Loading remoteDataSource'dan gelmemeli */ }
                }
            } else {
                Timber.e("QuizRepository: Kategorileri çekmek için çevrimdışı.")
                val currentCache = localDataSource.getAllCategories().firstOrNull() // Anlık kontrol
                if (currentCache.isNullOrEmpty()) {
                    send(Resource.Error(context.getString(R.string.error_offline_no_cached_categories)))
                }

            }
        } catch (e: Exception) {
            Timber.e("QuizRepository Kategori Hatası: Ağ veya işleme sırasında istisna: ${e.localizedMessage}")
            // Genel bir hata. Cache boş değilse kullanıcı cache'i görmeye devam eder.
            // Eğer cache de boşsa (initialCacheChecked true ve hasEmittedDataFromCache false ise) hata ver.
            val currentCacheOnException = localDataSource.getAllCategories().firstOrNull()
            if (currentCacheOnException.isNullOrEmpty()) {
                val errorMessage = e.localizedMessage ?: context.getString(R.string.error_unknown_server_or_network)
                send(Resource.Error(context.getString(R.string.error_categories_unknown, errorMessage)))
            }
        }

        // channelFlow'un kapanmasını bekle (Coroutine iptal edilene kadar)
        awaitClose {
            Timber.d("QuizRepository: getCategories channelFlow kapatılıyor.")
            localDataJob.cancel() // Yerel veri dinleme coroutine'ini iptal et
        }

    }.flowOn(Dispatchers.IO)

    private suspend fun fetchAndCacheCategories() {
        if (networkMonitor.isOnline.first()) {
            when (val remoteResult = remoteDataSource.getCategories()) {
                is Resource.Success -> {
                    try {
                        println("QuizRepository: Uzak kategoriler başarıyla alındı. Kaydediliyor...")
                        val categoryEntities = mapCategoryDtoToEntity(remoteResult.data!!.trivia_categories)
                        localDataSource.insertCategories(categoryEntities)
                    } catch (e: Exception) {
                        println("QuizRepository Kategori Hatası: Kategoriler kaydedilirken hata: ${e.localizedMessage}")
                        // Hata durumu zaten yukarıdaki .catch bloğunda genel olarak yakalanacak.
                    }
                }
                is Resource.Error -> {
                    println("QuizRepository Kategori Hatası: Uzak kategoriler alınamadı: ${remoteResult.message}")
                    // Hata durumu, UI'a zaten Error olarak yansıyacak.
                    // Eğer özel bir mesaj göstermek isteniyorsa, burada emit edilebilir ama
                    // getCategories flow'unun .catch bloğu zaten genel hataları yakalıyor.
                }
                is Resource.Loading -> {}
            }
        } else {
            println("QuizRepository: Kategorileri çekmek için çevrimdışı.")
            // UI, eğer yerel veri yoksa ve internet de yoksa EmptyState veya ErrorView gösterecektir.
        }
    }

    private fun mapCategoryDtoToEntity(dtos: List<CategoryDto>): List<CategoryEntity> {
        return dtos.map { dto ->
            CategoryEntity(id = dto.id, name = dto.name)
        }
    }

    override fun getQuestions(categoryId: Int, categoryName: String, amount: Int): Flow<Resource<List<QuestionEntity>>> = flow {
        emit(Resource.Loading())

        val localQuestions = localDataSource.getQuestionsByCategory(categoryName, amount)
        val oldestTimestamp = localDataSource.getOldestQuestionTimestamp(categoryName)
        val now = System.currentTimeMillis()

        val isCacheValid = oldestTimestamp != null && (now - oldestTimestamp < CACHE_EXPIRY_MS)
        val hasEnoughCache = localQuestions.isNotEmpty() && localQuestions.size >= amount

        if (hasEnoughCache && isCacheValid) {
            println("QuizRepository: Geçerli önbellek kullanılıyor ($categoryName).")
            emit(Resource.Success(localQuestions))
            return@flow
        }

        val isOnline = networkMonitor.isOnline.first()

        if (isOnline) {
            println("QuizRepository: Ağ bağlantısı var, uzak veri kaynağı deneniyor ($categoryName)...")
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
                            println("QuizRepository Uyarı: API başarı dedi ama sonuç listesi boş geldi? ($categoryName)")
                            emit(Resource.Error(context.getString(R.string.error_questions_not_enough)))
                        }
                    } catch(e: Exception) {
                        val errorMessage = e.localizedMessage ?: context.getString(R.string.error_unknown_server_or_network)
                        println("QuizRepository Hata: Sorular işlenirken/kaydedilirken hata: $errorMessage")
                        if (localQuestions.isNotEmpty()){
                            emit(Resource.Error(context.getString(R.string.error_questions_update_failed_showing_old, errorMessage), localQuestions))
                        } else {
                            emit(Resource.Error(context.getString(R.string.error_questions_processing_failed, errorMessage)))
                        }
                    }
                }
                is Resource.Error -> {
                    println("QuizRepository Hata: Uzak veri alınamadı ($categoryName): ${remoteResult.message}")
                    emit(handleDataSourceError(remoteResult.message, categoryName, amount))
                }
                is Resource.Loading -> { /* Bu durum DataSource'dan gelmez */ }
            }
        } else {
            println("QuizRepository: Ağ bağlantısı yok ($categoryName). Yerel önbellek kontrol ediliyor...")
            if (localQuestions.isNotEmpty()) {
                println("QuizRepository: Çevrimdışı modda yerel önbellek kullanılıyor ($categoryName).")
                emit(Resource.Success(localQuestions))
            } else {
                println("QuizRepository: Çevrimdışı modda yerel önbellek bulunamadı ($categoryName).")
                emit(Resource.Error(context.getString(R.string.error_offline_no_cached_questions)))
            }
        }
    }.catch { e ->
        val errorMessage = e.localizedMessage ?: context.getString(R.string.error_unknown_server_or_network)
        println("QuizRepository Hata: getQuestions akışında bilinmeyen hata: $errorMessage")
        emit(Resource.Error(context.getString(R.string.error_questions_unknown_flow, errorMessage)))
    }.flowOn(Dispatchers.IO)

    private suspend fun handleDataSourceError(errorMessage: String?, categoryName: String, amount: Int): Resource<List<QuestionEntity>> {
        val localQuestions = localDataSource.getQuestionsByCategory(categoryName, amount)
        val baseMessage = errorMessage ?: context.getString(R.string.error_unknown_server_or_network)
        return if (localQuestions.isNotEmpty()) {
            Resource.Error(context.getString(R.string.error_data_update_failed_showing_old, baseMessage), localQuestions)
        } else {
            Resource.Error(context.getString(R.string.error_questions_load_failed, baseMessage))
        }
    }

    private fun mapDtoToEntity(dtos: List<QuestionDto>, categoryName: String): List<QuestionEntity> {
        return dtos.map { dto ->
            val allAnswers = (dto.incorrectAnswers + dto.correctAnswer).shuffled()
            QuestionEntity(
                category = categoryName, type = dto.type, difficulty = dto.difficulty, question = dto.question,
                correctAnswer = dto.correctAnswer, incorrectAnswers = dto.incorrectAnswers, allAnswers = allAnswers
            )
        }
    }
}