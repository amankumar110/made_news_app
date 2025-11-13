package `in`.amankumar110.madenewsapp.data.repository.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.actionCodeSettings
import `in`.amankumar110.madenewsapp.domain.repository.auth.AuthRepository
import `in`.amankumar110.madenewsapp.utils.Constants.BASE_URL
import jakarta.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryImpl : AuthRepository {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun signUp(email: String, password: String): Result<Unit> {
        return suspendCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    cont.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    cont.resume(Result.failure(exception))
                }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {

        return suspendCoroutine { cont ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    Log.d("AuthRepository", "Sign in successful: ${result.user?.email}")
                    cont.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    Log.e("AuthRepository", "Sign in failed: ${exception.message}")
                    cont.resume(Result.failure(exception))
                }
        }
    }

    suspend fun sendVerificationLink(user: FirebaseUser): Result<Unit> {


        val actionCodeSettings = actionCodeSettings {
            url = "${BASE_URL}emailVerified"
            handleCodeInApp = true
            setAndroidPackageName(
                "in.amankumar110.madenewsapp",
                true, // installIfNotAvailable
                "12", // minimumVersion
            )
        }

        return suspendCoroutine { cont ->
            auth.sendSignInLinkToEmail(user.email!!,actionCodeSettings)
                .addOnSuccessListener { result ->
                Log.d("AuthRepository", "Verification email sent successfully.")
                cont.resume(Result.success(Unit))
            }.addOnFailureListener { exception ->
                Log.e("AuthRepository", "Failed to send verification email: ${exception.message}")
                cont.resume(Result.failure(exception))
            }
        }
    }

    override suspend fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun isEmailVerified(user: FirebaseUser): Boolean = suspendCoroutine { cont ->
        user.reload()
            .addOnSuccessListener {
                cont.resume(user.isEmailVerified)
            }
            .addOnFailureListener { exception ->
                cont.resume(false) // fallback: assume not verified
            }
    }

    override suspend fun signOut(){
        auth.signOut()
    }

}