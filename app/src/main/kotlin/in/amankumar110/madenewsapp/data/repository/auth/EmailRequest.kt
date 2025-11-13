package `in`.amankumar110.madenewsapp.data.repository.auth

import com.google.gson.annotations.SerializedName

data class EmailRequest(

    @SerializedName("userId")
    val userId : String)
