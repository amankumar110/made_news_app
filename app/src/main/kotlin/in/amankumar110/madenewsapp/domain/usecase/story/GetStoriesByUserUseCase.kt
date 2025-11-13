package `in`.amankumar110.madenewsapp.domain.usecase.story

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.amankumar110.madenewsapp.domain.models.story.Story // Assuming Story model is here
import `in`.amankumar110.madenewsapp.domain.repository.story.StoryRepository
import `in`.amankumar110.madenewsapp.utils.NetworkUtils
import javax.inject.Inject

class GetStoriesByUserUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storyRepository: StoryRepository
) {

    /**
     * Retrieves all stories posted by a specific user.
     *
     * @param creatorId The unique identifier of the user whose stories are to be fetched.
     * @return Result<List<Story>> containing the list of stories on success, or an error on failure.
     */
    suspend operator fun invoke(creatorId: String): Result<List<Story>> {
        if (!NetworkUtils.isInternetAvailable(context)) {
            return Result.failure(NetworkUtils.InternetConnectionException("No internet connection"))
        }

        // Basic validation (optional, but good practice)
        if (creatorId.isBlank()) {
            return Result.failure(IllegalArgumentException("Creator ID cannot be blank."))
        }
        return storyRepository.getAllStoriesPostedBy(creatorId)
    }
}
