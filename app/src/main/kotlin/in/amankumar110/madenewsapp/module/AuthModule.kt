package `in`.amankumar110.madenewsapp.module

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.amankumar110.madenewsapp.data.repository.auth.AuthRepositoryImpl
import `in`.amankumar110.madenewsapp.data.repository.user.UserRepositoryImpl
import `in`.amankumar110.madenewsapp.domain.repository.auth.AuthRepository
import `in`.amankumar110.madenewsapp.domain.repository.news.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository() : AuthRepository {
        return AuthRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideUserRepository() : UserRepository = UserRepositoryImpl()
}