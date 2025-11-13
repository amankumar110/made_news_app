package `in`.amankumar110.madenewsapp.domain.repository.main

import okhttp3.MultipartBody

interface ImageUploadRepository {
    suspend fun uploadImage(image: String) : Result<String>
}