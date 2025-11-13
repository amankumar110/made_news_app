package `in`.amankumar110.madenewsapp.data.remote

import `in`.amankumar110.madenewsapp.data.repository.auth.EmailRequest
import `in`.amankumar110.madenewsapp.domain.models.resource.Resource
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface EmailVerificationService {

    @POST("api/email/send-verification")
    suspend fun sendVerificationEmail(
        @Header("x-api-key") apiKey: String,
        @Body emailRequest: EmailRequest // wrap email in a proper object
    ): Resource<String>

    @GET("api/email/is-verified/{uid}")
    suspend fun isEmailVerified(
        @Header("x-api-key") apiKey: String,
        @Path("uid") uid: String
    ): Resource<Boolean>

    @POST("api/email/resend-verification")
    suspend fun resendVerificationEmail(
        @Header("x-api-key") apiKey: String,
        @Body emailRequest: EmailRequest
    ): Resource<String>
}
