package `in`.amankumar110.madenewsapp.domain.models.story

import `in`.amankumar110.madenewsapp.domain.models.news.Article

// Story is a child of Article class and is primarily used to interact with articles like likes, dislikes, etc.

data class Story(

    val imageUrl : String? = null,
    val creatorId: String = "",
    val storyId: String = "",
    val likes: Int = 0,
    val dislikes: Int = 0,
    val title: String = "",
    val content: String = "",
    val createdAt: String = ""

)
