package com.technovix.quiznova.di

import android.content.Context
import androidx.room.Room
import com.technovix.quiznova.data.local.QuizDao
import com.technovix.quiznova.data.local.QuizDatabase
import com.technovix.quiznova.data.remote.OpenTriviaApi
import com.technovix.quiznova.util.NetworkMonitor // NetworkMonitor'ü de ekleyelim
import com.google.gson.GsonBuilder
import com.technovix.quiznova.data.datasource.local.QuizLocalDataSource
import com.technovix.quiznova.data.datasource.local.QuizLocalDataSourceImpl
import com.technovix.quiznova.data.datasource.remote.QuizRemoteDataSource
import com.technovix.quiznova.data.datasource.remote.QuizRemoteDataSourceImpl
import com.technovix.quiznova.data.repository.QuizRepository
import com.technovix.quiznova.data.repository.QuizRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Uygulama ömrü boyunca yaşayacak instance'lar
object DataModule { // Data katmanına özel modül de olabilirdi

    // Senior Dev: BASE_URL'i burada tanımlamak merkezi kontrol sağlar.
    private const val BASE_URL = "https://opentdb.com/"

    // Senior Dev: OkHttpClient'i özelleştirebiliriz. Örneğin, loglama ekleyebiliriz.
    // Debug build'lerde logları görmek sorunları ayıklamada çok yardımcı olur.
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // Tüm request/response body'sini logla
            })
            .connectTimeout(30, TimeUnit.SECONDS) // Bağlantı zaman aşımı
            .readTimeout(30, TimeUnit.SECONDS)    // Okuma zaman aşımı
            .build()
    }

    // Senior Dev: Retrofit instance'ını oluşturuyoruz. Base URL, Converter Factory
    // ve OkHttpClient'i bağlıyoruz.
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    }

    // Senior Dev: Retrofit, API interface'imizin implementasyonunu oluşturur.
    @Provides
    @Singleton
    fun provideOpenTriviaApi(retrofit: Retrofit): OpenTriviaApi {
        return retrofit.create(OpenTriviaApi::class.java)
    }

    // Senior Dev: Room veritabanı instance'ını oluşturuyoruz.
    // fallbackToDestructiveMigration, şema versiyonu değiştiğinde eski veriyi silip
    // yeniden oluşturur (geliştirme aşamasında kolaylık sağlar, production'da dikkatli kullanılmalı).
    @Provides
    @Singleton
    fun provideQuizDatabase(@ApplicationContext context: Context): QuizDatabase {
        return Room.databaseBuilder(
            context,
            QuizDatabase::class.java,
            "quiz_database"
        )
            .fallbackToDestructiveMigration() // Dikkatli kullan!
            .build()
    }

    // Senior Dev: DAO instance'ını veritabanı üzerinden sağlıyoruz.
    @Provides
    @Singleton
    fun provideQuizDao(database: QuizDatabase): QuizDao {
        return database.quizDao()
    }

    // NetworkMonitor'ü Singleton olarak sağlıyoruz (zaten @Singleton ile işaretliydi ama burada da belirtmek iyi pratik).
    // Hilt constructor injection sayesinde bunu otomatik yapabilir ama burada göstermek için ekledim.
    // @Provides
    // @Singleton
    // fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
    //     return NetworkMonitor(context)
    // }

    // Repository'yi de burada provide edeceğiz (bir sonraki adımda oluşturunca)

    @Provides
    @Singleton
    fun provideQuizRemoteDataSource(api: OpenTriviaApi): QuizRemoteDataSource {
        return QuizRemoteDataSourceImpl(api)
    }

    @Provides
    @Singleton
    fun provideQuizLocalDataSource(dao: QuizDao): QuizLocalDataSource {
        return QuizLocalDataSourceImpl(dao)
    }

    // --- REPOSITORY SAĞLAYICISI GÜNCELLENDİ ---
    @Provides
    @Singleton
    fun provideQuizRepository(
        remoteDataSource: QuizRemoteDataSource, // Artık DataSource'ları alıyor
        localDataSource: QuizLocalDataSource,
        networkMonitor: NetworkMonitor
    ): QuizRepository {
        // Implementasyon DataSource'lar ile oluşturuluyor
        return QuizRepositoryImpl(remoteDataSource, localDataSource, networkMonitor)
    }
}