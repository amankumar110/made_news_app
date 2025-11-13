package `in`.amankumar110.madenewsapp.domain.repository.news

import `in`.amankumar110.madenewsapp.domain.models.news.Article
import `in`.amankumar110.madenewsapp.domain.models.news.CategorizedArticle
import `in`.amankumar110.madenewsapp.utils.SatireStyle
import okhttp3.ResponseBody

interface NewsRepository {
    suspend fun getWeeklyArticles(): Result<Map<String, List<CategorizedArticle>>>

    suspend fun generateArticle(title: String): Result<Article>

    suspend fun generateArticleWithSatireStyle(title: String, satireStyle: SatireStyle) : Result<Article>
}