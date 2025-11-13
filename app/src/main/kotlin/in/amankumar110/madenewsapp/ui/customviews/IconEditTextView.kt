package `in`.amankumar110.madenewsapp.ui.customviews

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import `in`.amankumar110.madenewsapp.R
import `in`.amankumar110.madenewsapp.databinding.IconEditTextViewBinding
import androidx.core.content.withStyledAttributes

class IconEditTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding : IconEditTextViewBinding = IconEditTextViewBinding.inflate(
        LayoutInflater.from(context),
        this,
        true)

    var onDebouncedTextChanged: ((text: String) -> Unit)? = null
    var validationDelayMillis: Long = 750L

    private val validationHandler = Handler(Looper.getMainLooper())
    private var validationRunnable: Runnable? = null
    private var currentTextWatcher: TextWatcher? = null


    init {
        context.withStyledAttributes(attrs, R.styleable.IconEditTextView) {
            val iconRes = getResourceId(R.styleable.IconEditTextView_icon, 0)
            if (iconRes != 0) binding.ivIcon.setImageResource(iconRes)
            val hintText = getString(R.styleable.IconEditTextView_hint)
            if (hintText != null) binding.etInfo.hint = hintText
        }

        val inputType = attrs?.getAttributeIntValue("http://schemas.android.com/apk/res/android", "inputType", -1)
        if (inputType != null && inputType != -1)
            binding.etInfo.inputType = inputType

        val autofillHints = attrs?.getAttributeValue("http://schemas.android.com/apk/res/android", "autofillHints")
        if (autofillHints != null && autofillHints != "autofillHints") {
            binding.etInfo.setAutofillHints(autofillHints)
        }

        val imeOptions = attrs?.getAttributeIntValue("http://schemas.android.com/apk/res/android", "imeOptions", -1)
        if (imeOptions != null && imeOptions != -1)
            binding.etInfo.imeOptions = imeOptions

        val maxLength = attrs?.getAttributeIntValue("http://schemas.android.com/apk/res/android", "maxLength", -1)
        if (maxLength != null && maxLength != -1)
            binding.etInfo.filters = arrayOf(android.text.InputFilter.LengthFilter(maxLength))

        val maxLines = attrs?.getAttributeIntValue("http://schemas.android.com/apk/res/android", "lines", -1)
        if (maxLines != null && maxLines != -1)
            binding.etInfo.maxLines = maxLines

        setupTextWatcher()
    }

    private fun setupTextWatcher() {
        currentTextWatcher?.let { binding.etInfo.removeTextChangedListener(it) }

        currentTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                withdrawError()
                validationRunnable?.let { validationHandler.removeCallbacks(it) }
            }

            override fun afterTextChanged(editable: Editable?) {
                val text = editable.toString()
                if (onDebouncedTextChanged != null) {
                    validationRunnable = Runnable {
                        onDebouncedTextChanged?.invoke(text)
                    }
                    validationHandler.postDelayed(validationRunnable!!, validationDelayMillis)
                } else {
                    validationRunnable?.let { validationHandler.removeCallbacks(it) }
                }
            }
        }
        binding.etInfo.addTextChangedListener(currentTextWatcher)
    }


    fun getText() = binding.etInfo.text.toString()

    fun setText(text: String) {
        binding.etInfo.setText(text)
    }

    fun setError(text: String) {
        binding.tvError.visibility = VISIBLE
        binding.editTextContainer.setBackgroundResource(R.drawable.icon_edit_text_view_error_background)
        binding.ivIcon.setColorFilter(resources.getColor(R.color.app_color_error))
        binding.vDivider.setBackgroundColor(resources.getColor(R.color.app_color_error))
        binding.tvError.text = text
    }

    fun withdrawError() {
        binding.tvError.visibility = GONE
        binding.editTextContainer.setBackgroundResource(R.drawable.icon_edit_text_view_background)
        binding.ivIcon.setColorFilter(resources.getColor(R.color.app_color_outline))
        binding.vDivider.setBackgroundColor(resources.getColor(R.color.app_color_outline))
        binding.tvError.text = ""
    }

    private class SavedState : BaseSavedState {
        var textState: Parcelable? = null
        var errorTextState: String? = null
        var isErrorVisibleState: Int = GONE

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            textState = parcel.readParcelable(ClassLoader.getSystemClassLoader())
            errorTextState = parcel.readString()
            isErrorVisibleState = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeParcelable(textState, flags)
            out.writeString(errorTextState)
            out.writeInt(isErrorVisibleState)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.textState = binding.etInfo.onSaveInstanceState()
        savedState.errorTextState = binding.tvError.text.toString()
        savedState.isErrorVisibleState = binding.tvError.visibility
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            binding.etInfo.onRestoreInstanceState(state.textState)
            if (state.isErrorVisibleState == VISIBLE && !state.errorTextState.isNullOrEmpty()) {
                setError(state.errorTextState!!)
            } else {
                withdrawError()
            }
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        validationRunnable?.let { validationHandler.removeCallbacks(it) }
        currentTextWatcher?.let { binding.etInfo.removeTextChangedListener(it) }
    }
}

