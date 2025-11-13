package `in`.amankumar110.madenewsapp.utils

import `in`.amankumar110.madenewsapp.R

enum class NewsCategory(
    val displayName: String,
    val emoji: String,
    val backgroundColorId: Int,
    val subtext: String
) {

    POLITICS("Politics", "🏛️", R.color.app_color_politics, "Weekly Political Absurdity"),
    ALIENS("Aliens", "🛸", R.color.app_color_aliens, "Intergalactic Scandals"),
    EDUCATION("Education", "🎓", R.color.app_color_education, "Classroom Chaos"),
    TECHNOLOGY("Technology", "🤖", R.color.app_color_technology, "Code Gone Rogue"),
    CONSPIRACIES("Conspiracies", "🕵️‍♂️", R.color.app_color_conspiracies, "Theories They Don’t Want You to Read");

    companion object {
        fun getByName(name: String): NewsCategory? {
            return entries.find { it.displayName.equals(name, ignoreCase = true) }
        }
    }
}
