package `in`.amankumar110.madenewsapp.domain.repository.news

import `in`.amankumar110.madenewsapp.domain.models.auth.User

interface UserRepository {

    suspend fun addUser(user: User) : Result<Unit>

    suspend fun getUserByUserName(username: String) : Result<User>
    suspend fun isUsernameExist(username: String) : Result<Boolean>

}