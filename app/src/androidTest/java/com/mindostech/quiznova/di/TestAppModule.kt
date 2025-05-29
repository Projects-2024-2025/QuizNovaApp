package com.mindostech.quiznova.di // Test için DI paketi

import com.mindostech.quiznova.data.datastore.ThemeSettingsRepository
import com.mindostech.quiznova.data.local.entity.CategoryEntity
import com.mindostech.quiznova.data.local.entity.QuestionEntity
import com.mindostech.quiznova.data.repository.QuizRepository
import com.mindostech.quiznova.util.HtmlDecoder
import com.mindostech.quiznova.util.Resource
import com.mindostech.quiznova.util.ThemePreference
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton

interface ConfigurableMockQuizRepository : QuizRepository {
    fun setQuestionsForCategory(categoryName: String, questions: List<QuestionEntity>)
    fun setCategories(categories: List<CategoryEntity>)
    fun setQuestionsError(categoryName: String, error: Exception)
    fun setCategoriesError(error: Exception)
    fun reset()
}

// Gerçek DataModule'ü kaldırıp yerine bunu kullanmasını söylüyoruz
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class] // Gerçek DataModule'ün yerini alır
)
object TestAppModule {

    private fun createFakeQuestion(id: Int, category: String): QuestionEntity {
        // ViewModel decode edeceği için HAM veri döndürebiliriz testte
        val correctAnswer = "Correct $id"
        val incorrectAnswers = listOf("Wrong A$id", "Wrong B$id")
        val allAnswers = listOf(correctAnswer) + incorrectAnswers
        return QuestionEntity(
            id = id, category = category, type = "multiple", difficulty = "easy",
            question = "Test Question $id for $category?",
            correctAnswer = correctAnswer,
            incorrectAnswers = incorrectAnswers,
            allAnswers = allAnswers
        )
    }

    // Ayarlanabilir mock repository implementasyonu (SON HALİ)
    class TestQuizRepositoryImpl : ConfigurableMockQuizRepository {
        private val questionStore = ConcurrentHashMap<String, Resource<List<QuestionEntity>>>()
        private var categoryListResource: Resource<List<CategoryEntity>> = Resource.Loading()

        init {
            reset() // Başlangıç ayarları için reset'i çağır
        }

        // --- QuizRepository Metodları (override ile - GERÇEK İMZALAR) ---

        override fun getQuestions(
            categoryId: Int, // İmzada var, mock içinde kullanılmayabilir
            categoryName: String, // Map key'i olarak kullanılır
            amount: Int // İmzada var, mock içinde kullanılmayabilir
        ): Flow<Resource<List<QuestionEntity>>> {
            println("MockRepo: getQuestions called for categoryName: $categoryName")
            // categoryName'e göre önceden ayarlanmış veriyi veya hatayı döndür
            val resource = questionStore[categoryName]
                ?: Resource.Error("Mock data not configured for category: $categoryName")
            println("MockRepo: Returning resource for $categoryName: ${resource::class.simpleName}")
            return flowOf(resource)
        }

        // ** Düzeltildi: forceRefresh parametresi YOK **
        override fun getCategories(): Flow<Resource<List<CategoryEntity>>> {
            println("MockRepo: getCategories called")
            println("MockRepo: Returning categories: ${categoryListResource::class.simpleName}")
            return flowOf(categoryListResource)
        }

        // --- ConfigurableMockQuizRepository Metodları (Bunlar override etmez) ---
        // Bu metodlar sadece testimizin mock'u ayarlaması içindir.

        override fun setQuestionsForCategory(categoryName: String, questions: List<QuestionEntity>) {
            println("MockRepo: Setting ${questions.size} questions for category: $categoryName")
            questionStore[categoryName] = Resource.Success(questions)
        }

        override fun setCategories(categories: List<CategoryEntity>) {
            println("MockRepo: Setting ${categories.size} categories")
            categoryListResource = Resource.Success(categories)
        }

        override fun setQuestionsError(categoryName: String, error: Exception) {
            println("MockRepo: Setting questions error for category: $categoryName - ${error.message}")
            questionStore[categoryName] = Resource.Error(error.message ?: "Mock Question Error for $categoryName")
        }

        override fun setCategoriesError(error: Exception) {
            println("MockRepo: Setting categories error - ${error.message}")
            categoryListResource = Resource.Error(error.message ?: "Mock Category Error")
        }

        override fun reset() {
            println("MockRepo: Resetting mock data")
            questionStore.clear()
            categoryListResource = Resource.Loading()
            // Varsayılanları tekrar ayarla (testlerin başlangıç noktası için)
            setCategories(listOf(
                CategoryEntity(id = 9, name = "General Knowledge"),
                CategoryEntity(id = 21, name = "Sports")
            ))
            // Başlangıçta "General Knowledge" için 1 soru ayarlayalım (isteğe bağlı)
            setQuestionsForCategory("General Knowledge", listOf(createFakeQuestion(1, "General Knowledge")))
            setQuestionsForCategory("Sports", listOf(createFakeQuestion(3, "Sports")))
        }
    }

    // Sahte (mock) QuizRepository sağlıyoruz
    @Provides
    @Singleton
    fun provideTestQuizRepository(): QuizRepository {
        return TestQuizRepositoryImpl()
    }

    // --- YENİ EKLENEN KISIM ---
    // Sahte (mock) HtmlDecoder sağlıyoruz
    @Provides
    @Singleton
    fun provideTestHtmlDecoder(): HtmlDecoder {
        // MockK ile sahte bir HtmlDecoder oluştur
        val mockDecoder: HtmlDecoder = mockk()
        // Test sırasında decode çağrıldığında girdiyi aynen döndürsün
        every { mockDecoder.decode(any()) } answers { firstArg() }
        return mockDecoder
    }


    // --- MockK ile Settings Repository Sağlayıcısı ---
    @Provides
    @Singleton
    fun provideTestSettingsRepository(): ThemeSettingsRepository {
        // 1. MockK ile ThemeSettingsRepository class'ının sahtesini oluştur.
        val mockRepo: ThemeSettingsRepository = mockk(relaxed = true)

        // 2. Kontrol edilebilir StateFlow oluştur.
        val currentThemeFlow = MutableStateFlow(ThemePreference.LIGHT)

        // 3. Mock'a davranışını öğret:

        //    - themePreferenceFlow **property'si** istendiğinde -> StateFlow'umuzu döndür.
        //      Property'nin tam adını kullanıyoruz.
        every { mockRepo.themePreferenceFlow } returns currentThemeFlow // Property adı DÜZELTİLDİ

        //    - saveThemePreference (suspend fonksiyon) çağrıldığında -> StateFlow'u güncelle.
        coEvery { mockRepo.saveThemePreference(any()) } coAnswers {
            val themeArg = firstArg<ThemePreference>()
            println("MockK SettingsRepo: saveThemePreference called with $themeArg")
            currentThemeFlow.value = themeArg
            // saveThemePreference Unit döndürdüğü için burada bir şey döndürmeye gerek yok. Hata YOK.
        }

        // 4. Oluşturulan sahte (mock) repository'yi döndür.
        return mockRepo
    }


}