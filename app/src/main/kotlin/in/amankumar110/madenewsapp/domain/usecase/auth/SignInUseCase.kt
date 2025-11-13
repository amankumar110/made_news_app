package `in`.amankumar110.madenewsapp.domain.usecase.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import `in`.amankumar110.madenewsapp.domain.models.auth.AuthException
import `in`.amankumar110.madenewsapp.domain.repository.auth.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(private val authRepository: AuthRepository) {

    suspend fun execute(email: String, password: String): Result<Unit> {
        val result = authRepository.signIn(email, password)
        return result.fold(
            onSuccess = {
                Result.success(Unit)
            },
            onFailure = { exception ->
                Result.failure(handleSignInError(exception))
            }
        )
    }

    private fun handleSignInError(exception: Throwable): Exception {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> AuthException.InvalidCredentials(exception.message ?: "Invalid email format")
            is FirebaseNetworkException -> AuthException.InternetConnection("Internet connection error")
            else -> AuthException.UnknownError(exception.message ?: "Unknown error")
        }
    }
}