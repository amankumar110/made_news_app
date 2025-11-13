package `in`.amankumar110.madenewsapp.data.remote

import `in`.amankumar110.madenewsapp.domain.models.news.Article
import `in`.amankumar110.madenewsapp.domain.models.news.GenerateArticleResponse
import `in`.amankumar110.madenewsapp.domain.models.news.WeeklyArticlesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NewsApiService {

    @GET("api/weeklyArticles")
    suspend fun getWeeklyNewsArticles(@Header("x-api-key") apiKey: String)
        : Response<WeeklyArticlesResponse>

    @GET("api/generate")
    suspend fun generateArticle(@Header("x-api-key") apiKey: String,
                                @Query("title") title: String,
                                @Query("satireStyle") satireStyle: String)
        : Response<GenerateArticleResponse>

    @GET("api/generate")
    suspend fun generateArticle(@Header("x-api-key") apiKey: String,
                                @Query("title") title: String,)
            : Response<GenerateArticleResponse>
}