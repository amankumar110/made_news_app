package `in`.amankumar110.madenewsapp.domain.models.news

import com.google.gson.annotations.SerializedName

data class GenerateArticleResponse(

    @SerializedName("title")
    val title: String,

    @SerializedName("paragraphs")
    val paragraphs: List<String>,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("appGenerated")
    val appGenerated: Boolean,

    @SerializedName("satireStyle")
    val satireStyle: String? = null
)
