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
import com.technovix.quiznova.data.datastore.ThemeSettingsRepository
import com.technovix.quiznova.data.repository.QuizRepository
import com.technovix.quiznova.data.repository.QuizRepositoryImpl
import com.technovix.quiznova.util.AndroidHtmlDecoder
import com.technovix.quiznova.util.HtmlDecoder
import dagger.Binds
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
@InstallIn(SingletonComponent::class)
object DataModule {

    private const val BASE_URL = "https://opentdb.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenTriviaApi(retrofit: Retrofit): OpenTriviaApi {
        return retrofit.create(OpenTriviaApi::class.java)
    }

    @Provides
    @Singleton
    fun provideQuizDatabase(@ApplicationContext context: Context): QuizDatabase {
        return Room.databaseBuilder(
            context,
            QuizDatabase::class.java,
            "quiz_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

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
    fun provideQuizRemoteDataSource(@ApplicationContext context: Context,api: OpenTriviaApi): QuizRemoteDataSource {
        return QuizRemoteDataSourceImpl(context, api)
    }

    @Provides
    @Singleton
    fun provideQuizLocalDataSource(dao: QuizDao): QuizLocalDataSource {
        return QuizLocalDataSourceImpl(dao)
    }

    @Provides
    @Singleton
    fun provideQuizRepository(
        @ApplicationContext context: Context,
        remoteDataSource: QuizRemoteDataSource,
        localDataSource: QuizLocalDataSource,
        networkMonitor: NetworkMonitor
    ): QuizRepository {
        return QuizRepositoryImpl(context, remoteDataSource, localDataSource, networkMonitor)
    }

    @Provides
    @Singleton
    fun provideThemeSettingsRepository(@ApplicationContext context: Context): ThemeSettingsRepository {
        // Bu fonksiyonun DataModule object'inin İÇİNDE olduğundan emin olun
        return ThemeSettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideHtmlDecoder(): HtmlDecoder { // Parametre yok, dönüş türü Arayüz
        return AndroidHtmlDecoder() // Implementasyonu doğrudan oluşturup döndür
    }
}