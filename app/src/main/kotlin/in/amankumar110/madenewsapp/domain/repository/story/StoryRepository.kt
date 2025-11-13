package `in`.amankumar110.madenewsapp.domain.repository.story

import `in`.amankumar110.madenewsapp.domain.models.story.Story

interface StoryRepository {

    suspend fun saveStory(story: Story) : Result<Unit>
    suspend fun deleteStory(storyId: String) : Result<Unit>
    suspend fun getAllStoriesPostedBy(creatorId: String) : Result<List<Story>>
    suspend fun likeStory(storyId: String) : Result<Unit>
    suspend fun dislikeStory(storyId: String) : Result<Unit>
    suspend fun getStory(storyId: String) : Result<Story>

}