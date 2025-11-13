package `in`.amankumar110.madenewsapp.viewmodel.news

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.amankumar110.madenewsapp.domain.models.news.Article
import `in`.amankumar110.madenewsapp.domain.models.news.CategorizedArticle
import `in`.amankumar110.madenewsapp.domain.usecase.news.GetArticleByTitleUseCase
import `in`.amankumar110.madenewsapp.domain.usecase.news.GetArticleWithSatireStyleUseCase
import `in`.amankumar110.madenewsapp.domain.usecase.news.GetWeeklyArticlesUseCase
import `in`.amankumar110.madenewsapp.utils.SatireStyle
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getWeeklyArticlesUseCase: GetWeeklyArticlesUseCase,
    private val getArticleByTitleUseCase: GetArticleByTitleUseCase,
    private val getArticleWithSatireStyleUseCase: GetArticleWithSatireStyleUseCase
) : ViewModel() {

    private val _weeklyArticlesState = MutableStateFlow<WeeklyArticlesState>(WeeklyArticlesState.Idle)
    val weeklyArticlesState = _weeklyArticlesState

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _articleRemixLoading = MutableStateFlow(false)
    val articleRemixLoading = _articleRemixLoading.asStateFlow()

    private val _newsEvents = MutableSharedFlow<NewsEvent>(replay = 0, extraBufferCapacity = 1)
    val newsEvents = _newsEvents.asSharedFlow()

    private var weeklyArticlesFetched = false

    fun getWeeklyArticles() {

        if(weeklyArticlesFetched) return

        _isLoading.value = true

        viewModelScope.launch {
            getWeeklyArticlesUseCase()
                .onSuccess { articles ->
                    weeklyArticlesFetched = true
                    _isLoading.value = false
                    _weeklyArticlesState.value = WeeklyArticlesState.Success(articles)
                }
                .onFailure { throwable ->
                    _isLoading.value = false
                    _newsEvents.emit(NewsEvent.ShowMessage(throwable.message ?: "Unknown Error Occured"))
                }
        }
    }

    fun getArticleByTitle(title: String) {

        _isLoading.value = true

        viewModelScope.launch {
            getArticleByTitleUseCase(title)
                .onSuccess { article ->
                    _isLoading.value = false
                    _newsEvents.emit(NewsEvent.ArticleGenerated(article))
                }
                .onFailure { throwable ->
                    _isLoading.value = false
                    _newsEvents.emit(NewsEvent.ShowMessage(throwable.message ?: "Unknown Error Occured"))
                }
        }
    }

    fun remixArticleWithSatireStyle(title:String, satireStyle: SatireStyle) {

        _articleRemixLoading.value = true

        viewModelScope.launch {
            getArticleWithSatireStyleUseCase(title,satireStyle)
                .onSuccess {
                    _articleRemixLoading.value = false
                    _newsEvents.emit(NewsEvent.ArticleRemixed(it, SatireStyle.fromId(it.satireStyle!!)!!))
                }
                .onFailure {
                    _articleRemixLoading.value = false
                }

        }
    }

    // WeeklyArticlesUiState.kt
    sealed class WeeklyArticlesState {
        object Idle : WeeklyArticlesState()
        data class Success(val articles: Map<String,List<CategorizedArticle>>) : WeeklyArticlesState()
    }

    fun resetWeeklyArticlesState() {
        _weeklyArticlesState.value = WeeklyArticlesState.Idle
    }

    sealed class NewsEvent {
        data class ArticleRemixed(val article: Article, val satireStyle: SatireStyle) : NewsEvent()
        data class ArticleGenerated(val article: Article) : NewsEvent()
        data class ShowMessage(val message: String) : NewsEvent()
    }

}
