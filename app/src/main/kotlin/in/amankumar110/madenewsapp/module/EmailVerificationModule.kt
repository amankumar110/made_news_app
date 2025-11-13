package `in`.amankumar110.madenewsapp.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.amankumar110.madenewsapp.data.remote.EmailVerificationService
import `in`.amankumar110.madenewsapp.data.repository.auth.EmailVerificationRepositoryImpl
import `in`.amankumar110.madenewsapp.domain.repository.auth.EmailVerificationRepository
import `in`.amankumar110.madenewsapp.utils.Constants.BASE_URL
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object EmailVerificationModule {

    @Provides
    @Singleton
    fun provideEmailVerificationService(): EmailVerificationService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmailVerificationService::class.java)
    }

    @Provides
    @Singleton
    fun provideEmailVerificationRepository(emailVerificationService: EmailVerificationService): EmailVerificationRepository {
        return EmailVerificationRepositoryImpl(emailVerificationService)
    }

}