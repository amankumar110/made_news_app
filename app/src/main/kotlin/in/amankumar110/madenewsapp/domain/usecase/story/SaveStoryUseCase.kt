package `in`.amankumar110.madenewsapp.domain.usecase.story

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.amankumar110.madenewsapp.domain.models.story.Story
import `in`.amankumar110.madenewsapp.domain.repository.main.ImageUploadRepository
import `in`.amankumar110.madenewsapp.domain.repository.story.StoryRepository
import `in`.amankumar110.madenewsapp.utils.NetworkUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import javax.inject.Inject

class SaveStoryUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storyRepository: StoryRepository,
    private val imageUploadRepository: ImageUploadRepository
) {
    suspend operator fun invoke(title: String, content: String, storyImage: String?): Result<Unit> {

        if(!NetworkUtils.isInternetAvailable(context)) {
            return Result.failure(NetworkUtils.InternetConnectionException("No internet connection"))
        }

        val creatorId = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Result.failure(Exception("User not authenticated"))

        val publishedAt = LocalDateTime.now().toString()
        val storyId = "${creatorId}_$publishedAt"


        if(storyImage==null) {
            val defaultImage = "https://i.ibb.co/63z68hB/default-background.png"
            val story = Story(defaultImage,creatorId, storyId, 0, 0, title, content, publishedAt)
            return storyRepository.saveStory(story)
        }

        val publishedImageResult = imageUploadRepository.uploadImage(storyImage)

        if(publishedImageResult.isSuccess) {
            val story = Story(publishedImageResult.getOrNull(),creatorId, storyId, 0, 0, title, content, publishedAt)
            return storyRepository.saveStory(story)
        } else {
            Log.v("UploadError", publishedImageResult.exceptionOrNull()?.message ?: "Unknown Error")
            return Result.failure(Exception("Failed to upload image"))
        }

    }
}
