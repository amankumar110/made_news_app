package `in`.amankumar110.madenewsapp.domain.models.news

import com.google.gson.annotations.SerializedName

data class WeeklyArticlesResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("articles")
    val categorizedArticles: Map<String, List<CategorizedArticle>>

)
