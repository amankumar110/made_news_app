package `in`.amankumar110.madenewsapp.ui.auth

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.R.style.Theme_MaterialComponents_Dialog
import dagger.hilt.android.AndroidEntryPoint
import `in`.amankumar110.madenewsapp.R
import `in`.amankumar110.madenewsapp.databinding.FragmentEmailVerificationBinding
import `in`.amankumar110.madenewsapp.ui.utils.LoadingFragment
import `in`.amankumar110.madenewsapp.utils.AnimationUtils
import `in`.amankumar110.madenewsapp.utils.SharedPrefs
import `in`.amankumar110.madenewsapp.viewmodel.auth.EmailVerificationViewModel
import kotlinx.coroutines.launch
import androidx.core.content.edit

@AndroidEntryPoint
class EmailVerificationDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentEmailVerificationBinding
    private val emailVerificationViewModel: EmailVerificationViewModel by hiltNavGraphViewModels(R.id.nav_graph)
    private val prefs by lazy {
        requireContext().getSharedPreferences(
            "email_verification_prefs",
            Context.MODE_PRIVATE
        )
    }
    private var resendCountdownTimer: CountDownTimer? = null
    private val resendCooldownMillis = 30_000L // 30 seconds
    private var verificationInProcess = false
    private var verificationAttempts = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, Theme_MaterialComponents_Dialog)
        isCancelable = true 

        verificationInProcess = savedInstanceState?.getBoolean(ARG_VERIFICATION_IN_PROCESS) ?: false
        verificationAttempts = savedInstanceState?.getInt(ARG_VERIFICATION_ATTEMPTS) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEmailVerificationBinding.inflate(inflater, container, false)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Send email immediately when dialog opens
        sendVerificationEmail()

        // Retry button click
        binding.btnRetry.setOnClickListener {
            sendVerificationEmail()
        }

        binding.btnVerify.setOnClickListener {
            emailVerificationViewModel.verifyEmail()
        }

        // Listen for verification events
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                emailVerificationViewModel.emailVerificationEvent.collect { event ->

                    // increase attempt counts after receiving event caused by attempt
                    verificationAttempts++;
                    
                    when (event) {

                        is EmailVerificationViewModel.EmailVerificationEvent.Error -> {
                            verificationInProcess = false
                            clearCooldown()
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        EmailVerificationViewModel.EmailVerificationEvent.LinkSent -> {
                            Toast.makeText(
                                requireContext(),
                                "Verification link sent!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        EmailVerificationViewModel.EmailVerificationEvent.Verified -> {
                            verificationInProcess = false
                            clearCooldown()
                            dismiss()
                        }

                        EmailVerificationViewModel.EmailVerificationEvent.NotVerified -> {
                            
                            // dont throw warning in the initial check only attempt
                            if(verificationInProcess && verificationAttempts>1) {
                                Toast.makeText(requireContext(), "Email Not Verified", Toast.LENGTH_SHORT).show()
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (verificationInProcess) {
            emailVerificationViewModel.verifyEmail()
        }
    }

    private fun sendVerificationEmail() {
        verificationInProcess = true
        val lastSentTime = prefs.getLong("last_sent", 0)
        val now = System.currentTimeMillis()

        if (now - lastSentTime < resendCooldownMillis) {
            val remaining = resendCooldownMillis - (now - lastSentTime)
            startCountdown(remaining)
            return
        }

        // Call ViewModel to send email
        emailVerificationViewModel.sendVerificationLink()

        // Save send time
        prefs.edit { putLong("last_sent", now) }

        // Start cooldown timer
        startCountdown(resendCooldownMillis)
    }

    private fun startCountdown(millis: Long) {
        binding.btnRetry.isEnabled = false
        resendCountdownTimer?.cancel()

        resendCountdownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.btnRetry.text = "Retry in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                binding.btnRetry.isEnabled = true
                binding.btnRetry.text = "Retry"
            }
        }.start()
    }

    private fun clearCooldown() {
        prefs.edit { remove("last_sent") }
        resendCountdownTimer?.cancel()
        binding.btnRetry.isEnabled = true
        binding.btnRetry.text = "Retry"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resendCountdownTimer?.cancel()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        clearCooldown()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ARG_VERIFICATION_IN_PROCESS, verificationInProcess)
        outState.putInt(ARG_VERIFICATION_ATTEMPTS, verificationAttempts)
    }

    companion object {
        private const val TAG = "EmailVerificationDialog"
        private const val ARG_VERIFICATION_IN_PROCESS = "verification_in_process"
        private const val ARG_VERIFICATION_ATTEMPTS = "verification_attempts"

        fun show(fragmentManager: FragmentManager) {
            if (!isShowing(fragmentManager)) {
                val dialog = EmailVerificationDialogFragment()
                dialog.show(fragmentManager, TAG)
            }
        }


        fun hide(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(TAG) as? EmailVerificationDialogFragment
            dialog?.dismissAllowingStateLoss()
        }

        fun isShowing(fragmentManager: FragmentManager): Boolean {
            return (fragmentManager.findFragmentByTag(TAG) as? EmailVerificationDialogFragment)?.dialog?.isShowing == true
        }
    }
}

