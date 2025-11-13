package `in`.amankumar110.madenewsapp.domain.repository.auth

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    suspend fun signUp(email: String, password: String) : Result<Unit>
    suspend fun signIn(email: String, password: String) : Result<Unit>
    suspend fun getCurrentUser() : FirebaseUser?
    suspend fun signOut()
}