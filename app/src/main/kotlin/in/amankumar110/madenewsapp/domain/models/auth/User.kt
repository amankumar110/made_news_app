package `in`.amankumar110.madenewsapp.domain.models.auth

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("email")
    val email: String = "",
    @SerializedName("age")
    val age: Int = 0,
    @SerializedName("username")
    val username: String = "",
    @SerializedName("joinedAt")
    val joinedAt: String = "",
    val emailVerified: Boolean = false
)
