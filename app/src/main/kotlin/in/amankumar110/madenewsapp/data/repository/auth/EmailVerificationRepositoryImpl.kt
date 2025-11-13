package `in`.amankumar110.madenewsapp.data.repository.auth

import `in`.amankumar110.madenewsapp.data.remote.EmailVerificationService
import `in`.amankumar110.madenewsapp.domain.models.resource.Resource
import `in`.amankumar110.madenewsapp.domain.repository.auth.EmailVerificationRepository
import `in`.amankumar110.madenewsapp.utils.Constants.API_KEY
import javax.inject.Inject

class EmailVerificationRepositoryImpl @Inject constructor(
    private val emailVerificationService: EmailVerificationService
) : EmailVerificationRepository {

    override suspend fun sendEmailVerification(uid: String): Resource<String> {
        return emailVerificationService.sendVerificationEmail(API_KEY,EmailRequest(uid))
    }

    override suspend fun isEmailVerified(uid: String): Resource<Boolean> {
        return emailVerificationService.isEmailVerified(API_KEY, uid)
    }

    override suspend fun resendVerificationEmail(uid: String): Resource<String> {
        return emailVerificationService.resendVerificationEmail(API_KEY, EmailRequest(uid))
    }

}