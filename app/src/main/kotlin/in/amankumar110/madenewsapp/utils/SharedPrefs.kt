package `in`.amankumar110.madenewsapp.utils

import android.content.Context

object SharedPrefs {

    public val KEY_LAST_VERIFICATION_LINK_SENT_MILISEC = "last_verification_link_sent"

    fun setLong(context: Context, key: String, value: Long) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putLong(key, value)
            apply()
        }
    }

    fun getLong(context: Context, key: String, default : Long): Long {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getLong(key, default)
    }
}