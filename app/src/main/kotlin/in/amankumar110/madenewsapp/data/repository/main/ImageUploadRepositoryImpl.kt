package `in`.amankumar110.madenewsapp.data.repository.main

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import `in`.amankumar110.madenewsapp.data.remote.ImageUploadService
import `in`.amankumar110.madenewsapp.domain.repository.main.ImageUploadRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import androidx.core.net.toUri

class ImageUploadRepositoryImpl(
    private val imageUploadService: ImageUploadService,
    private val context: Context,
    private val apiKey: String // Your ImgBB API Key
) : ImageUploadRepository {

    override suspend fun uploadImage(image: String): Result<String> {
        return try {
            val imageUri = image.toUri()

            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                return Result.failure(Exception("Could not open InputStream for URI: $image"))
            }

            val imageBytes = inputStream.readBytes()
            inputStream.close()

            val mediaType = context.contentResolver.getType(imageUri)?.toMediaTypeOrNull()
            val requestBody = imageBytes.toRequestBody(mediaType)

            val fileName = getFileName(imageUri) ?: "uploaded_image.${mediaType?.subtype ?: "jpg"}"
            val imagePart = MultipartBody.Part.createFormData("image", fileName, requestBody)

            val response = imageUploadService.uploadImage(apiKey = apiKey, image = imagePart)

            if (response.isSuccessful) {
                val imageUploadResponseBody = response.body()

                if (imageUploadResponseBody != null && imageUploadResponseBody.success) {
                    val uploadedImageUrl = imageUploadResponseBody.data?.url
                    if (!uploadedImageUrl.isNullOrBlank()) {
                        Result.success(uploadedImageUrl)
                    } else {
                        Result.failure(Exception("Uploaded image URL not found or empty in response data. Status: ${imageUploadResponseBody.status}"))
                    }
                } else {
                    val errorMsg = if (imageUploadResponseBody != null) {
                        "Upload reported as not successful. Status: ${imageUploadResponseBody.status}"
                    } else {
                        "Response body was null."
                    }
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Image upload failed with HTTP status: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = it.getString(displayNameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result?.ifBlank { null }
    }
}
