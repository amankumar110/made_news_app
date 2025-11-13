package `in`.amankumar110.madenewsapp.domain.repository.auth

import `in`.amankumar110.madenewsapp.domain.models.resource.Resource

interface EmailVerificationRepository {

    suspend fun sendEmailVerification(uid: String): Resource<String>
    suspend fun isEmailVerified(uid: String): Resource<Boolean>
    suspend fun resendVerificationEmail(uid: String): Resource<String>

}