package `in`.amankumar110.madenewsapp.domain.models.news

import com.google.gson.annotations.SerializedName

data class CategorizedArticle(
    @SerializedName("title")
    val title: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("createdAt")
    var createdAt: String,

    @SerializedName("appGenerated")
    val appGenerated: Boolean,

    @SerializedName("category")
    val category: String,

    @SerializedName("satireStyle")
    val satireStyle: String? = null
)
