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
import android.content.Context // Context import et
import com.technovix.quiznova.R // R sınıfını import et
import dagger.hilt.android.qualifiers.ApplicationContext // Hilt için ApplicationContext


@Singleton
class QuizRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context, // Context'i enjekte et
    private val remoteDataSource: QuizRemoteDataSource,
    private val localDataSource: QuizLocalDataSource,
    private val networkMonitor: NetworkMonitor
) : QuizRepository {

    // Önbellek geçerlilik süresi (örneğin 1 saat)
    private val CACHE_EXPIRY_MS = TimeUnit.HOURS.toMillis(1)

    // Hardcoded kategoriler hala burada kalabilir, çünkü bunlar geliştirme/varsayılan veri.
    // Kullanıcıya gösterilen mesajlar değiller.
    private val hardcodedCategories = listOf(
        CategoryEntity(id = 9, name = "General Knowledge"),
        // ... diğer hardcoded kategoriler
    )

    override fun getCategories(): Flow<Resource<List<CategoryEntity>>> = flow {
        emit(Resource.Loading())
        val localCategoriesFlow = localDataSource.getAllCategories()
        var emittedInitialLocalData = false

        localCategoriesFlow.collect { localCategories ->
            if (!emittedInitialLocalData || localCategories.isNotEmpty()) {
                println("QuizRepository: Yerel kategoriler emit ediliyor (sayı: ${localCategories.size}).")
                emit(Resource.Success(localCategories))
                emittedInitialLocalData = true
            }
            if (localCategories.isEmpty() && emittedInitialLocalData) {
                println("QuizRepository: Yerel kategoriler boş, uzak kaynak deneniyor...")
                fetchAndCacheCategories()
            }
        }
    }.catch { e ->
        val errorMessage = e.localizedMessage ?: context.getString(R.string.error_unknown_server_or_network)
        emit(Resource.Error(context.getString(R.string.error_categories_unknown, errorMessage)))
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