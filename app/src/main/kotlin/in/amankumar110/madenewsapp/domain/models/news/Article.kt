package `in`.amankumar110.madenewsapp.domain.models.news

import com.google.gson.annotations.SerializedName

open class Article(
    @SerializedName("title")
    open val title: String,

    @SerializedName("content")
    open val content: String,

    @SerializedName("createdAt")
    open val createdAt: String,

    @SerializedName("appGenerated")
    open val appGenerated: Boolean = false,

    @SerializedName("satireStyle")
    val satireStyle: String? = null
)
