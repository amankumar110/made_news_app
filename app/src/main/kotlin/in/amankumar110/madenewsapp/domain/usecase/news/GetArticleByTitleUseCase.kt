package `in`.amankumar110.madenewsapp.domain.usecase.news

import android.content.Context
import `in`.amankumar110.madenewsapp.domain.models.news.Article
import `in`.amankumar110.madenewsapp.domain.repository.news.NewsRepository
import `in`.amankumar110.madenewsapp.utils.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GetArticleByTitleUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val newsRepository: NewsRepository
) {

    suspend operator fun invoke(title : String): Result<Article> {
        if(!NetworkUtils.isInternetAvailable(context)) {
            return Result.failure(NetworkUtils.InternetConnectionException("No internet connection"))
        }
        return newsRepository.generateArticle(title)
    }

}