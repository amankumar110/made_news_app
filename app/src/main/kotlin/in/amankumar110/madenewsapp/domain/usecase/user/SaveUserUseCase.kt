package `in`.amankumar110.madenewsapp.domain.usecase.user

import com.google.firebase.auth.FirebaseAuth
import `in`.amankumar110.madenewsapp.domain.models.auth.User
import `in`.amankumar110.madenewsapp.domain.repository.news.UserRepository
import java.time.LocalDateTime
import javax.inject.Inject

class SaveUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String, email: String, age: Int): Result<Unit> {

        val user = User(
            id = FirebaseAuth.getInstance().uid!!,
            email = email,
            age = age,
            username = username,
            joinedAt = LocalDateTime.now().toString()
        )

        return userRepository.addUser(user)
    }
}

