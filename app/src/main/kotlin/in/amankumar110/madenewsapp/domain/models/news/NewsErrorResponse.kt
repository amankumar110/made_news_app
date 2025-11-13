package `in`.amankumar110.madenewsapp.domain.models.news

import com.google.gson.annotations.SerializedName

data class NewsErrorResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("error")
    val errorMessage: String
)
