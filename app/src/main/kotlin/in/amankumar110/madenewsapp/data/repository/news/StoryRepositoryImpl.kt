package `in`.amankumar110.madenewsapp.data.repository.news

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import `in`.amankumar110.madenewsapp.domain.models.story.Story
import `in`.amankumar110.madenewsapp.domain.repository.story.StoryRepository
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class StoryRepositoryImpl : StoryRepository {

    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val storiesCollection = firebaseFirestore.collection("stories")

    override suspend fun saveStory(story: Story): Result<Unit> {
        return suspendCoroutine { continuation ->
            storiesCollection
                .document(story.storyId)
                .set(story)
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { exception -> continuation.resume(Result.failure(exception)) }
        }
    }

    override suspend fun deleteStory(storyId: String): Result<Unit> {
        return suspendCoroutine { continuation ->
            storiesCollection
                .document(storyId)
                .delete()
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { exception -> continuation.resume(Result.failure(exception)) }
        }
    }

    override suspend fun getAllStoriesPostedBy(creatorId: String): Result<List<Story>> {
        return suspendCoroutine { continuation ->
            storiesCollection
                .whereEqualTo("creatorId", creatorId) // Query for stories by creatorId
                .get()
                .addOnSuccessListener { querySnapshot ->
                    try {
                        val stories = querySnapshot.toObjects(Story::class.java)
                        continuation.resume(Result.success(stories))
                    } catch (e: Exception) {
                        continuation.resume(Result.failure(e))
                    }
                }
                .addOnFailureListener { exception -> continuation.resume(Result.failure(exception)) }
        }
    }

    override suspend fun likeStory(storyId: String): Result<Unit> {
        return suspendCoroutine { continuation ->
            storiesCollection
                .document(storyId)
                .update("likes", FieldValue.increment(1)) // Increment the 'likes' field
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { exception -> continuation.resume(Result.failure(exception)) }
        }
    }

    override suspend fun dislikeStory(storyId: String): Result<Unit> {
        return suspendCoroutine { continuation ->
            storiesCollection
                .document(storyId)
                .update("dislikes", FieldValue.increment(1)) // Increment the 'dislikes' field
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { exception -> continuation.resume(Result.failure(exception)) }
        }
    }

    override suspend fun getStory(storyId: String): Result<Story> {
        return suspendCoroutine { continuation ->
            storiesCollection
                .document(storyId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val story = documentSnapshot.toObject(Story::class.java)
                    if (story != null) {
                        continuation.resume(Result.success(story))
                    } else {
                        continuation.resume(Result.failure(Exception("Story not found")))
                    }
                }
                .addOnFailureListener { exception -> continuation.resume(Result.failure(exception)) }
        }
    }
}
