package com.mindostech.quiznova.di

import android.content.Context
import androidx.room.Room
import com.mindostech.quiznova.data.local.QuizDao
import com.mindostech.quiznova.data.local.QuizDatabase
import com.mindostech.quiznova.data.remote.OpenTriviaApi
import com.mindostech.quiznova.util.NetworkMonitor // NetworkMonitor'ü de ekleyelim
import com.google.gson.GsonBuilder
import com.mindostech.quiznova.R
import com.mindostech.quiznova.data.datasource.local.QuizLocalDataSource
import com.mindostech.quiznova.data.datasource.local.QuizLocalDataSourceImpl
import com.mindostech.quiznova.data.datasource.remote.QuizRemoteDataSource
import com.mindostech.quiznova.data.datasource.remote.QuizRemoteDataSourceImpl
import com.mindostech.quiznova.data.datastore.ThemeSettingsRepository
import com.mindostech.quiznova.data.repository.QuizRepository
import com.mindostech.quiznova.data.repository.QuizRepositoryImpl
import com.mindostech.quiznova.util.AndroidHtmlDecoder
import com.mindostech.quiznova.util.HtmlDecoder
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
import javax.inject.Named
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
        return ThemeSettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideHtmlDecoder(): HtmlDecoder {
        return AndroidHtmlDecoder()
    }

    @Provides
    @Named("InterstitialAdUnitId")
    @Singleton
    fun provideInterstitialAdUnitId(@ApplicationContext context: Context): String {
        return context.getString(R.string.admob_interstitial_ad_unit_id)
    }

}