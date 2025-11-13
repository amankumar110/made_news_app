package `in`.amankumar110.madenewsapp.data.repository.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import `in`.amankumar110.madenewsapp.domain.models.auth.User
import `in`.amankumar110.madenewsapp.domain.repository.news.UserRepository
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserRepositoryImpl : UserRepository {

    private val firestoreDatabase = FirebaseFirestore.getInstance()
    private val usersCollection = firestoreDatabase.collection("users")
    private val usernamesCollection = firestoreDatabase.collection("usernames")

    override suspend fun addUser(user: User): Result<Unit> {
        return try {
            firestoreDatabase.runTransaction { transaction ->
                val usernameRef = usernamesCollection.document(user.username)

                // Check if username already exists
                if (transaction.get(usernameRef).exists()) {
                    throw Exception("Username already exists")
                }

                // Reserve the username
                transaction.set(usernameRef, mapOf("uid" to user.id))

                // Create the user record
                val userRef = usersCollection.document(user.id)
                transaction.set(userRef, user)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserByUserName(username: String): Result<User> {
        return try {
            val snapshot = usersCollection.whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Result.failure(Exception("User not found"))
            } else {
                Result.success(snapshot.documents[0].toObject(User::class.java)!!)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isUsernameExist(username: String): Result<Boolean> {
        return try {
            val snapshot = usernamesCollection.document(username).get().await()
            Result.success(snapshot.exists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
