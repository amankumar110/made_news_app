package `in`.amankumar110.madenewsapp.domain.usecase.auth

import android.util.Log
import `in`.amankumar110.madenewsapp.domain.models.auth.AuthException
import `in`.amankumar110.madenewsapp.domain.repository.auth.AuthRepository
import `in`.amankumar110.madenewsapp.domain.repository.auth.EmailVerificationRepository
import jakarta.inject.Inject

class SendVerificationLinkUseCase @Inject constructor(
    val emailVerificationRepository: EmailVerificationRepository,
    val authRepository: AuthRepository) {

    suspend operator fun invoke(): Result<Unit> {

        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null)
            return Result.failure(AuthException.UserNotLoggedIn("User is not logged in"))

        Log.v("SendVerificationLinkUseCase", "Current user: ${currentUser.uid}")

        val resource = emailVerificationRepository.sendEmailVerification(currentUser.uid)

        return if(resource.success) {
            Result.success(Unit)
        } else {
            Result.failure(AuthException.UnknownError("Unknown Error"))
        }

    }

}