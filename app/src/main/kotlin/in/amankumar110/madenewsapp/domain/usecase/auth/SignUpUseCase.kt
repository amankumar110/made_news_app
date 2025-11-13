package `in`.amankumar110.madenewsapp.domain.usecase.auth

import android.util.Log
import `in`.amankumar110.madenewsapp.domain.repository.auth.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import `in`.amankumar110.madenewsapp.domain.models.auth.AuthException
import `in`.amankumar110.madenewsapp.domain.repository.news.UserRepository
import `in`.amankumar110.madenewsapp.domain.usecase.user.SaveUserUseCase
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val saveUserUseCase: SaveUserUseCase,
    private val userRepository: UserRepository
) {

    suspend fun execute(
        username: String,
        email: String,
        password: String,
        age: Int
    ): Result<Unit> {

        // Step 1: Check if username already exists
        val usernameCheck = userRepository.isUsernameExist(username)
        if (usernameCheck.isFailure) {
            return Result.failure(usernameCheck.exceptionOrNull()!!)
        }

        if (usernameCheck.getOrDefault(false)) {
            return Result.failure(AuthException.UsernameAlreadyExists("Username already exists"))
        }

        // Step 2: Create Firebase Auth account first
        val authResult = authRepository.signUp(email, password)
        if (authResult.isFailure) {
            return Result.failure(handleSignupError(authResult.exceptionOrNull()!!))
        }

        // Step 3: Save user in Firestore
        val saveResult = saveUserUseCase(username, email, age)
        if (saveResult.isFailure) {
            // Optional rollback: delete auth user if Firestore save fails
            // authRepository.deleteCurrentUser()
            return Result.failure(saveResult.exceptionOrNull()!!)
        }

        return Result.success(Unit)
    }

    private fun handleSignupError(exception: Throwable): Exception {
        return when (exception) {
            is FirebaseAuthUserCollisionException ->
                AuthException.UserAlreadyExists(exception.message ?: "User already exists")

            is FirebaseNetworkException ->
                AuthException.InternetConnection("Internet connection error")

            is FirebaseAuthInvalidCredentialsException ->
                AuthException.InvalidEmail(exception.message ?: "Invalid email format")

            else -> AuthException.UnknownError(exception.message ?: "Unknown error")
        }
    }
}

