package `in`.amankumar110.madenewsapp.domain.usecase.user

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.amankumar110.madenewsapp.domain.models.auth.User
import `in`.amankumar110.madenewsapp.domain.repository.news.UserRepository
import `in`.amankumar110.madenewsapp.utils.NetworkUtils
import javax.inject.Inject


class GetUserByUserNameUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(username: String): Result<User> {

        if(!NetworkUtils.isInternetAvailable(context)) {
            throw NetworkUtils.InternetConnectionException("No internet connection")
        }

        return userRepository.getUserByUserName(username)
    }

}