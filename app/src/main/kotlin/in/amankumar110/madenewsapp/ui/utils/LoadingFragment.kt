package `in`.amankumar110.madenewsapp.ui.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import `in`.amankumar110.madenewsapp.R
import `in`.amankumar110.madenewsapp.databinding.FragmentLoadingBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LoadingFragment : DialogFragment() {

    private lateinit var binding: FragmentLoadingBinding
    private lateinit var messages: List<String>
    private var currentIndex = 0
    private val rotationDelay = 3000L
    private var rotateRunnable: Runnable? = null
    private var isRotationRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_MaterialComponents_Dialog)

        // Ensure the fragment survives configuration changes
        retainInstance = false // Don't retain instance to avoid memory leaks
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoadingBinding.inflate(inflater, container, false)
        isCancelable = false

        val initialMessages = arguments?.getStringArray(ARG_MESSAGES)?.toList()
        // Ensure messages list is never truly empty for initialization logic
        messages = if (initialMessages.isNullOrEmpty()) listOf("Loading...") else initialMessages

        binding.tvLoadingMessage.text = messages.firstOrNull() ?: "Loading..."
        // startMessageRotation will handle if it should actually rotate or not
        startMessageRotation()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.transparent)
            setDimAmount(0.8f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopMessageRotation() // Clean up runnable
    }

    override fun onDestroy() {
        super.onDestroy()
        // Additional cleanup
        stopMessageRotation()
    }

    private fun stopMessageRotation() {
        rotateRunnable?.let {
            if (::binding.isInitialized) {
                binding.root.removeCallbacks(it)
            }
        }
        rotateRunnable = null
        isRotationRunning = false
    }

    private fun startMessageRotation() {
        stopMessageRotation() // Ensure any previous rotation is stopped

        if (messages.size <= 1) {
            // If only one (or zero) message, ensure it's displayed and no rotation happens
            if (isAdded && ::binding.isInitialized && messages.isNotEmpty()) {
                binding.tvLoadingMessage.text = messages.first()
                binding.tvLoadingMessage.alpha = 1f // Make sure it's fully visible
            } else if (isAdded && ::binding.isInitialized) {
                // Default if messages somehow became empty after check
                binding.tvLoadingMessage.text = "Loading..."
                binding.tvLoadingMessage.alpha = 1f
            }
            return // No need to start runnable for rotation
        }

        isRotationRunning = true
        rotateRunnable = object : Runnable {
            override fun run() {
                // Additional safety: if messages become empty while runnable was posted
                if (!isAdded || !::binding.isInitialized || messages.isEmpty() || !isRotationRunning) {
                    return
                }
                currentIndex = (currentIndex + 1) % messages.size // messages.size will be > 1 here
                binding.tvLoadingMessage.text = messages[currentIndex]
                binding.tvLoadingMessage.alpha = 0f
                binding.tvLoadingMessage.animate().alpha(1f).setDuration(500).start()
                if (isRotationRunning) { // Continue posting only if still supposed to be running
                    binding.root.postDelayed(this, rotationDelay)
                }
            }
        }
        binding.root.postDelayed(rotateRunnable!!, rotationDelay)
    }

    private fun updateMessages(newMessages: List<String>) {
        // If the view is not created yet, just update messages. onCreateView will handle starting rotation.
        if (!::binding.isInitialized) {
            messages = newMessages.ifEmpty { listOf("Loading...") }
            currentIndex = 0
            return
        }

        stopMessageRotation() // Stop any ongoing rotation

        messages = newMessages.ifEmpty { listOf("Loading...") }
        currentIndex = 0

        if (isAdded) { // Ensure fragment is added
            binding.tvLoadingMessage.text = messages.firstOrNull() ?: "Loading..."
            binding.tvLoadingMessage.alpha = 1f // Reset alpha
            startMessageRotation() // Restart rotation which will check messages.size
        }
    }

    companion object {
        private const val TAG = "LoadingFragment"
        private const val ARG_MESSAGES = "messages"

        fun show(manager: FragmentManager, messages: List<String> = listOf("Loading...")) {
            // Prevent showing if manager is already destroyed (can happen during rapid config changes)
            if (manager.isStateSaved || manager.isDestroyed) {
                Log.w(TAG, "FragmentManager is destroyed or state saved, cannot show LoadingFragment.")
                return
            }

            val existing = manager.findFragmentByTag(TAG) as? LoadingFragment
            if (existing != null) {
                if (existing.isAdded && !existing.isRemoving) {
                    existing.updateMessages(messages)
                    return
                } else {
                    // Remove the existing fragment if it's in a bad state
                    try {
                        manager.beginTransaction().remove(existing).commitAllowingStateLoss()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error removing existing LoadingFragment: ${e.message}")
                    }
                }
            }

            val fragment = LoadingFragment().apply {
                arguments = Bundle().apply {
                    putStringArray(ARG_MESSAGES, messages.toTypedArray())
                }
            }
            try {
                manager.beginTransaction()
                    .add(fragment, TAG)
                    .commitAllowingStateLoss()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Error showing LoadingFragment: ${e.message}")
                // This can happen if trying to commit after onSaveInstanceState
            }
        }

        fun hide(manager: FragmentManager) {
            if (manager.isStateSaved || manager.isDestroyed) {
                Log.w(TAG, "FragmentManager is destroyed or state saved, cannot hide LoadingFragment.")
                return
            }
            val existing = manager.findFragmentByTag(TAG) as? LoadingFragment
            if (existing != null && existing.isAdded && !existing.isRemoving) {
                try {
                    existing.dismissAllowingStateLoss()
                } catch (e: Exception) {
                    // If dismissAllowingStateLoss fails, try removing via transaction
                    try {
                        manager.beginTransaction().remove(existing).commitAllowingStateLoss()
                    } catch (e2: Exception) {
                        Log.e(TAG, "Error hiding LoadingFragment: ${e2.message}")
                    }
                }
            }
        }

        fun isShowing(manager: FragmentManager): Boolean {
            if (manager.isStateSaved || manager.isDestroyed) return false
            val existing = manager.findFragmentByTag(TAG) as? LoadingFragment
            return existing != null && existing.isAdded && !existing.isRemoving && existing.isVisible
        }

        fun forceHide(manager: FragmentManager) {
            // More aggressive hiding method for cleanup
            try {
                val existing = manager.findFragmentByTag(TAG)
                if (existing != null) {
                    manager.beginTransaction().remove(existing).commitAllowingStateLoss()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error force hiding LoadingFragment: ${e.message}")
            }
        }
    }
}