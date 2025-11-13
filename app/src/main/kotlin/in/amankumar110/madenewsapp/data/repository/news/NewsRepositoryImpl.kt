package `in`.amankumar110.madenewsapp.data.repository.news

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import `in`.amankumar110.madenewsapp.data.remote.NewsApiService
import `in`.amankumar110.madenewsapp.domain.models.news.Article
import `in`.amankumar110.madenewsapp.domain.models.news.CategorizedArticle
import `in`.amankumar110.madenewsapp.domain.models.news.GenerateArticleResponse
import `in`.amankumar110.madenewsapp.domain.models.news.NewsErrorResponse
import `in`.amankumar110.madenewsapp.domain.repository.news.NewsRepository
import `in`.amankumar110.madenewsapp.utils.Constants.API_KEY
import `in`.amankumar110.madenewsapp.utils.SatireStyle
import jakarta.inject.Inject
import okhttp3.ResponseBody

class NewsRepositoryImpl @Inject constructor(
    private val newsApiService: NewsApiService
) : NewsRepository {

    override suspend fun getWeeklyArticles(): Result<Map<String,List<CategorizedArticle>>> {
        val response = newsApiService.getWeeklyNewsArticles(API_KEY)

        if (response.isSuccessful) {
            response.body()?.let { body ->
                if (body.success) {
                    Log.v("TestCase1", body.success.toString())
                    return Result.success(body.categorizedArticles)
                }
            }
            return Result.failure(RuntimeException("Failed to fetch articles"))
        } else {
            return handleError(response.errorBody())
        }
    }

    override suspend fun generateArticle(title: String): Result<Article> {

        val response = newsApiService.generateArticle(API_KEY, title)

        if (response.isSuccessful) {

            response.body()?.let { body ->
                if (body.success) {
                    Log.v("test3",if(body.satireStyle==null) "NULL" else body.satireStyle.toString())
                    return Result.success(body.asArticle())
                }
            }
            return Result.failure(RuntimeException("Failed to generate article"))

        } else {
            return handleError(response.errorBody())
        }
    }

    override suspend fun generateArticleWithSatireStyle(
        title: String,
        satireStyle: SatireStyle
    ): Result<Article> {

        val response = newsApiService.generateArticle(API_KEY,title,satireStyle.id)

        if (response.isSuccessful) {

            response.body()?.let { body ->
                if (body.success) {
                    Log.v("test3",if(body.satireStyle==null) "NULL" else body.satireStyle.toString())
                    return Result.success(body.asArticle())
                }
            }
            return Result.failure(RuntimeException("Failed to generate article"))

        } else {
            return handleError(response.errorBody())
        }

    }

    private fun parseError(responseBody: ResponseBody?): NewsErrorResponse? {
        return try {
            responseBody?.let {
                Gson().fromJson(it.charStream(), NewsErrorResponse::class.java)
            }
        } catch (e: JsonSyntaxException) {
            null // Parsing failed, return null or handle accordingly
        }
    }

    private fun <T> handleError(responseBody: ResponseBody?): Result<T> {
        val error = parseError(responseBody)
        val errorMessage = error?.errorMessage?.takeIf { it.isNotBlank() } ?: "Unknown API error"
        Log.e("TestCase2", "API Error: $errorMessage")
        return Result.failure(RuntimeException(errorMessage))
    }

    fun GenerateArticleResponse.asArticle(): Article {
        return Article(
            title = this.title,
            content = this.paragraphs.joinToString("\n\n"),
            satireStyle = this.satireStyle,
            createdAt = createdAt
        )
    }
}

