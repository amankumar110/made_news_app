package `in`.amankumar110.madenewsapp.utils

import android.view.View

object AnimationUtils {

    fun fadeIn(view : View,duration: Long = 500,onCompleted: (() -> Unit)? = null) {
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .withEndAction { onCompleted?.invoke() }
            .start()

    }

    fun fadeOut(view: View, duration: Long = 500, onCompleted: (() -> Unit)? = null) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction { onCompleted?.invoke() }
            .start()
    }

}