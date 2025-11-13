package `in`.amankumar110.madenewsapp.domain.models.main

import com.google.gson.annotations.SerializedName

data class ImageUploadResponse(
    @SerializedName("data")
    val data: ImgBbUploadData?, // Make data nullable in case of error where it might be missing
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("status")
    val status: Int
) {
    data class ImgBbUploadData(
        // We only care about the direct URL of the image as per your requirement
        @SerializedName("url")
        val url: String? // Make url nullable to handle potential missing field even in "data"
        // Other fields like id, title, url_viewer, display_url, etc., are ignored
        // as per the requirement "rest of information is irrelevant".
    )
}

