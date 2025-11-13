package `in`.amankumar110.madenewsapp.domain.usecase.news

import android.content.Context
import android.util.Log
import `in`.amankumar110.madenewsapp.domain.models.news.CategorizedArticle
import `in`.amankumar110.madenewsapp.domain.repository.news.NewsRepository
import `in`.amankumar110.madenewsapp.utils.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetWeeklyArticlesUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val newsRepository: NewsRepository
) {

    suspend operator fun invoke(): Result<Map<String, List<CategorizedArticle>>> {

        if(!NetworkUtils.isInternetAvailable(context)) {
            return Result.failure(NetworkUtils.InternetConnectionException("No internet connection"))
        }

        val result = newsRepository.getWeeklyArticles()

        return if (result.isSuccess) {
            val originalMap = result.getOrNull() ?: return result
            val transformedMap = originalMap.mapValues { (_, articles) ->
                transformDates(articles)
            }
            Result.success(transformedMap)
        } else {
            result
        }
    }

    private fun transformDates(categorizedArticles: List<CategorizedArticle>): List<CategorizedArticle> {
        val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")

        return categorizedArticles.map { article ->
            val zonedDateTime = ZonedDateTime.parse(article.createdAt)
            val formattedDate = zonedDateTime.format(formatter)
            Log.v("TestCase1", "Transformed date: $formattedDate")
            article.copy(createdAt = formattedDate)
        }
    }
}
