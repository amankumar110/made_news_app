package `in`.amankumar110.madenewsapp.data.remote

import `in`.amankumar110.madenewsapp.domain.models.main.ImageUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImageUploadService {

    companion object {
        const val BASE_URL = "https://api.imgbb.com/1/"
    }

    @POST("upload")
    @Multipart
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponse>


}