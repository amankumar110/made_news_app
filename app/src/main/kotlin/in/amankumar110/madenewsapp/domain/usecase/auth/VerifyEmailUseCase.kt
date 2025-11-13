package `in`.amankumar110.madenewsapp.domain.usecase.auth

import android.util.Log
import `in`.amankumar110.madenewsapp.domain.models.auth.AuthException
import `in`.amankumar110.madenewsapp.domain.repository.auth.AuthRepository
import `in`.amankumar110.madenewsapp.domain.repository.auth.EmailVerificationRepository
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class VerifyEmailUseCase @Inject constructor(
    private val emailVerificationRepository: EmailVerificationRepository,
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): Result<Boolean> {
        val currentUser = authRepository.getCurrentUser()

        Log.d("VerifyEmailUseCase", "Current user before token check: ${currentUser?.uid ?: "null"}")

        if (currentUser == null) {
            return Result.failure(AuthException.UserNotLoggedIn("User is not logged in"))
        }

        try {
            // Step 1: Force token refresh to verify the session is valid
            val tokenResult = currentUser.getIdToken(true).await()

            if (tokenResult.token.isNullOrEmpty()) {
                return Result.failure(AuthException.UserNotLoggedIn("Token refresh failed"))
            }

            // Step 2: Use custom repository to check verification status
            val resource = emailVerificationRepository.isEmailVerified(currentUser.uid)

            return if (!resource.success) {
                Result.failure(AuthException.UnknownError("Unknown Error"))
            } else {
                Result.success(resource.data!!)
            }

        } catch (e: Exception) {
            Log.e("VerifyEmailUseCase", "Auth check failed: ${e.message}", e)
            return Result.failure(AuthException.UnknownError(e.message ?: "Unknown error"))
        }
    }

}
