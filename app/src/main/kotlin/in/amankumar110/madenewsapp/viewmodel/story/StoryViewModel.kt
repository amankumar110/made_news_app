package `in`.amankumar110.madenewsapp.viewmodel.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.amankumar110.madenewsapp.domain.models.story.Story
import `in`.amankumar110.madenewsapp.domain.usecase.story.DeleteStoryUseCase
import `in`.amankumar110.madenewsapp.domain.usecase.story.DislikeStoryUseCase
import `in`.amankumar110.madenewsapp.domain.usecase.story.GetStoriesByUserUseCase
import `in`.amankumar110.madenewsapp.domain.usecase.story.GetStoryByIdUseCase
import `in`.amankumar110.madenewsapp.domain.usecase.story.LikeStoryUseCase
import `in`.amankumar110.madenewsapp.domain.usecase.story.SaveStoryUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val saveStoryUseCase: SaveStoryUseCase,
    private val deleteStoryUseCase: DeleteStoryUseCase,
    private val likeStoryUseCase: LikeStoryUseCase,
    private val dislikeStoryUseCase: DislikeStoryUseCase,
    private val getStoriesByUserUseCase: GetStoriesByUserUseCase,
    private val getStoryByIdUseCase: GetStoryByIdUseCase
) : ViewModel() {

    // --- StateFlows ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories = _stories.asStateFlow() // For stories fetched by user

    private val _story = MutableStateFlow<Story?>(null)
    val story = _story.asStateFlow() // For a single story fetched by ID

    // --- SharedFlow for Events ---
    private val _storyEvents = MutableSharedFlow<StoryEvent>(replay = 0, extraBufferCapacity = 1)
    val storyEvents = _storyEvents.asSharedFlow()

    fun saveStory(title: String, storyContent: String, storyImage: String?) {

        viewModelScope.launch {
            _isLoading.value = true
            try {
                saveStoryUseCase(title, storyContent, storyImage)
                    .onSuccess {
                        _storyEvents.emit(StoryEvent.SaveSuccess("Story saved successfully."))
                    }
                    .onFailure { throwable ->
                        _storyEvents.emit(
                            StoryEvent.Error(
                                throwable.message ?: "Failed to save story."
                            )
                        )
                    }
            } catch (e: Exception) {
                _storyEvents.emit(StoryEvent.Error(e.message ?: "An unexpected error occurred."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                deleteStoryUseCase(storyId)
                    .onSuccess {
                        _storyEvents.emit(StoryEvent.StoryDeleted("Story deleted successfully."))
                    }
                    .onFailure { throwable ->
                        _storyEvents.emit(
                            StoryEvent.Error(
                                throwable.message ?: "Failed to delete story."
                            )
                        )
                    }
            } catch (e: Exception) {
                _storyEvents.emit(StoryEvent.Error(e.message ?: "An unexpected error occurred."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun likeStory(storyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                likeStoryUseCase(storyId)
                    .onSuccess {
                        _storyEvents.emit(StoryEvent.StoryLiked("Story liked."))
                    }
                    .onFailure { throwable ->
                        _storyEvents.emit(
                            StoryEvent.Error(
                                throwable.message ?: "Failed to like story."
                            )
                        )
                    }
            } catch (e: Exception) {
                _storyEvents.emit(StoryEvent.Error(e.message ?: "An unexpected error occurred."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dislikeStory(storyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                dislikeStoryUseCase(storyId)
                    .onSuccess {
                        _storyEvents.emit(StoryEvent.StoryDisliked("Story disliked."))
                    }
                    .onFailure { throwable ->
                        _storyEvents.emit(
                            StoryEvent.Error(
                                throwable.message ?: "Failed to dislike story."
                            )
                        )
                    }
            } catch (e: Exception) {
                _storyEvents.emit(StoryEvent.Error(e.message ?: "An unexpected error occurred."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchStoriesByUser(creatorId: String) {

        viewModelScope.launch {
            _isLoading.value = true

            if (_stories.value.isNotEmpty()) {
                _isLoading.value = false
                return@launch
            }

            try {
                getStoriesByUserUseCase(creatorId)
                    .onSuccess { fetchedStories ->
                        _stories.value = fetchedStories // Update StateFlow
                        _storyEvents.emit(StoryEvent.StoriesFetched(fetchedStories)) // Emit Event
                    }
                    .onFailure { throwable ->
                        _stories.value = emptyList() // Clear data on error
                        _storyEvents.emit(
                            StoryEvent.Error(
                                throwable.message ?: "Failed to fetch stories."
                            )
                        )
                    }
            } catch (e: Exception) {
                _stories.value = emptyList()
                _storyEvents.emit(StoryEvent.Error(e.message ?: "An unexpected error occurred."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchStoryById(storyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                getStoryByIdUseCase(storyId)
                    .onSuccess { fetchedStory ->
                        _story.value = fetchedStory // Update StateFlow
                        _storyEvents.emit(StoryEvent.StoryByIdSuccess(fetchedStory)) // Emit Event
                    }
                    .onFailure { throwable ->
                        _story.value = null // Clear data on error
                        _storyEvents.emit(
                            StoryEvent.Error(
                                throwable.message ?: "Story not found or failed to fetch."
                            )
                        )
                    }
            } catch (e: Exception) {
                _story.value = null
                _storyEvents.emit(StoryEvent.Error(e.message ?: "An unexpected error occurred."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- StoryEvent Sealed Class ---
    sealed class StoryEvent {
        // Requested specific events
        data class StoryLiked(val message: String) : StoryEvent()
        data class StoryDisliked(val message: String) : StoryEvent()
        data class StoriesFetched(val stories: List<Story>) : StoryEvent()
        data class StoryDeleted(val message: String) : StoryEvent()

        // Other success events
        data class SaveSuccess(val message: String) : StoryEvent()
        data class StoryByIdSuccess(val story: Story) : StoryEvent()

        // Consolidated Error Event
        data class Error(val message: String) : StoryEvent()
    }
}

