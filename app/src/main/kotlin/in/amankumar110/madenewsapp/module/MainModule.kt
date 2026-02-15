package `in`.amankumar110.madenewsapp.module

import android.content.Context
import android.media.Image
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.amankumar110.madenewsapp.domain.repository.main.ImageUploadRepository
import `in`.amankumar110.madenewsapp.utils.Constants.IMBB_API_KEY
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

}