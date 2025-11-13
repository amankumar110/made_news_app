package `in`.amankumar110.madenewsapp.ui.customviews

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import `in`.amankumar110.madenewsapp.R
import `in`.amankumar110.madenewsapp.utils.TTSManager
import java.time.LocalTime

class FrameProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var strokeColor = Color.TRANSPARENT
    private var strokeWidth = 6f
    private var topLeft = 0f
    private var topRight = 0f
    private var bottomRight = 0f
    private var bottomLeft = 0f

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val path = Path()
    private val pathMeasure = PathMeasure()
    private val segmentPath = Path()
    private var progress = 0f

    // Add animator reference to control it
    private var currentAnimator: ValueAnimator? = null

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.FrameProgressView, 0, 0).apply {
            try {
                topLeft = getDimension(R.styleable.FrameProgressView_topLeftRadius, 0f)
                topRight = getDimension(R.styleable.FrameProgressView_topRightRadius, 0f)
                bottomRight = getDimension(R.styleable.FrameProgressView_bottomRightRadius, 0f)
                bottomLeft = getDimension(R.styleable.FrameProgressView_bottomLeftRadius, 0f)
                strokeColor = getColor(R.styleable.FrameProgressView_strokeColor, Color.YELLOW)
                strokeWidth = getDimension(R.styleable.FrameProgressView_strokeWidth, 6f)
            } finally {
                recycle()
            }
        }

        strokePaint.color = strokeColor
        strokePaint.strokeWidth = strokeWidth
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        segmentPath.reset()

        val length = pathMeasure.length
        val stop = length * progress

        // Calculate offset so progress starts from top-center
        val topCenterOffset = findTopCenterOffset()

        val start = (topCenterOffset) % length
        val end = (start + stop) % length

        if (start < end) {
            pathMeasure.getSegment(start, end, segmentPath, true)
        } else {
            // wrap around the path (e.g., end < start due to loop)
            pathMeasure.getSegment(start, length, segmentPath, true)
            pathMeasure.getSegment(0f, end, segmentPath, true)
        }

        canvas.drawPath(segmentPath, strokePaint)
    }

    private fun findTopCenterOffset(): Float {
        val pathLength = pathMeasure.length
        val pos = FloatArray(2)
        val steps = 200 // Higher = more accurate

        var minDist = Float.MAX_VALUE
        var bestOffset = 0f

        for (i in 0 until steps) {
            val distance = i * pathLength / steps
            pathMeasure.getPosTan(distance, pos, null)

            val dx = pos[0] - width / 2f
            val dy = pos[1] // distance from top
            val dist = dx * dx + dy * dy

            if (dist < minDist) {
                minDist = dist
                bestOffset = distance
            }
        }
        return bestOffset
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildPath(w, h)
    }

    private fun buildPath(w: Int, h: Int) {
        val halfStroke = strokeWidth / 2f
        val rect = RectF(
            halfStroke,
            halfStroke,
            w - halfStroke,
            h - halfStroke
        )

        // Important: reduce radii slightly to account for stroke width
        val radii = floatArrayOf(
            topLeft - halfStroke, topLeft - halfStroke,
            topRight - halfStroke, topRight - halfStroke,
            bottomRight - halfStroke, bottomRight - halfStroke,
            bottomLeft - halfStroke, bottomLeft - halfStroke
        )

        // Clamp to non-negative values
        for (i in radii.indices) {
            radii[i] = radii[i].coerceAtLeast(0f)
        }

        path.reset()
        path.addRoundRect(rect, radii, Path.Direction.CW)
        pathMeasure.setPath(path, true)
    }

    fun playWithTTS(text: String, ttsManager: TTSManager) {
        // Cancel any existing animation
        currentAnimator?.cancel()

        progress = 0f
        segmentPath.reset()
        strokePaint.color = strokeColor
        visibility = VISIBLE
        invalidate()

        val estimatedDuration = estimateSpeechDuration(text)
        val utteranceId = System.currentTimeMillis().toString()

        ttsManager.setListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                post {
                    startBorderAnimation(estimatedDuration)
                }
            }

            override fun onDone(utteranceId: String?) {
                post {
                    // Check if path is complete and complete it if not
                    ensurePathCompleteAndHide()
                }
            }

            override fun onError(utteranceId: String?) {
                post {
                    // Complete path even on error
                    ensurePathCompleteAndHide()
                }
            }
        })

        ttsManager.speak(text, utteranceId)
    }

    private fun estimateSpeechDuration(text: String): Long {
        val clean = text.trim()
        if (clean.isEmpty()) return 0L

        val words = clean.split("\\s+".toRegex()).size

        val calibratedWPM = 184.5  // Based on your real TTS durations

        val minutes = words / calibratedWPM
        val millis = minutes * 60_000
        val pauses = clean.count { it in ".!?," } * 120  // 120 ms per pause

        return (millis + pauses).toLong()
    }

    private fun startBorderAnimation(duration: Long) {
        currentAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
        }
        currentAnimator?.start()
    }

    private fun ensurePathCompleteAndHide() {
        // Cancel current animation if running
        currentAnimator?.cancel()

        // Check if path is already complete
        if (progress >= 1f) {
            // Path is already complete, just hide
            completeAndHide()
        } else {
            // Path is not complete, animate to completion in fixed 1 second
            val completionDuration = 500L // Fixed 0.5 second completion

            currentAnimator = ValueAnimator.ofFloat(progress, 1f).apply {
                duration = completionDuration
                addUpdateListener {
                    progress = it.animatedValue as Float
                    invalidate()
                }
                doOnEnd {
                    // Hide after completion
                    completeAndHide()
                }
            }
            currentAnimator?.start()
        }
    }

    private fun completeAndHide() {
        progress = 0f
        segmentPath.reset()
        invalidate()
        visibility = GONE
        currentAnimator = null
    }

    // Add at the end of the class
    fun clearProgress() {
        currentAnimator?.cancel()
        currentAnimator = null
        progress = 0f
        segmentPath.reset()
        invalidate()
        visibility = GONE
    }


}