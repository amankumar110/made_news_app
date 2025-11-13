package `in`.amankumar110.madenewsapp.domain.models.resource

data class Resource<T>(
    val message: String = "",
    val data: T? = null,
    val success: Boolean = false
)
