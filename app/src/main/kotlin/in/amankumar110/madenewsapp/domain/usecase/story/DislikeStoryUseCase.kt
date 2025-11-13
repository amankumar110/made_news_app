package `in`.amankumar110.madenewsapp.domain.usecase.story

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.amankumar110.madenewsapp.domain.repository.story.StoryRepository
import `in`.amankumar110.madenewsapp.utils.NetworkUtils
import javax.inject.Inject

class DislikeStoryUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storyRepository: StoryRepository
) {

    /**
     * Dislikes a story identified by its storyId.
     * This typically involves incrementing a dislike counter for the story.
     *
     * @param storyId The unique identifier of the story to be disliked.
     * @return Result<Unit> indicating success or failure of the operation.
     */
    suspend operator fun invoke(storyId: String): Result<Unit> {
        if (!NetworkUtils.isInternetAvailable(context)) {
            return Result.failure(NetworkUtils.InternetConnectionException("No internet connection"))
        }

        // Basic validation (optional, but good practice)
        if (storyId.isBlank()) {
            return Result.failure(IllegalArgumentException("Story ID cannot be blank."))
        }
        return storyRepository.dislikeStory(storyId)
    }
}
