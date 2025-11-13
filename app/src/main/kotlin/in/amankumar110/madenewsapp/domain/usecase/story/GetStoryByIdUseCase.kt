package `in`.amankumar110.madenewsapp.domain.usecase.story

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.amankumar110.madenewsapp.domain.models.story.Story // Assuming Story model is here
import `in`.amankumar110.madenewsapp.domain.repository.story.StoryRepository
import `in`.amankumar110.madenewsapp.utils.NetworkUtils
import javax.inject.Inject

class GetStoryByIdUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storyRepository: StoryRepository
) {

    /**
     * Retrieves a specific story by its unique identifier.
     *
     * @param storyId The unique identifier of the story to be fetched.
     * @return Result<Story> containing the story on success, or an error on failure (e.g., if not found).
     */
    suspend operator fun invoke(storyId: String): Result<Story> {
        if (!NetworkUtils.isInternetAvailable(context)) {
            return Result.failure(NetworkUtils.InternetConnectionException("No internet connection"))
        }

        // Basic validation (optional, but good practice)
        if (storyId.isBlank()) {
            return Result.failure(IllegalArgumentException("Story ID cannot be blank."))
        }
        return storyRepository.getStory(storyId)
    }
}
