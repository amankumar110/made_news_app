package `in`.amankumar110.madenewsapp.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import `in`.amankumar110.madenewsapp.R
import `in`.amankumar110.madenewsapp.databinding.FragmentSignInBinding
import `in`.amankumar110.madenewsapp.domain.models.validation.ValidationException
import `in`.amankumar110.madenewsapp.ui.utils.LoadingFragment
import `in`.amankumar110.madenewsapp.utils.SharedPrefs
import `in`.amankumar110.madenewsapp.utils.SharedPrefs.KEY_LAST_VERIFICATION_LINK_SENT_MILISEC
import `in`.amankumar110.madenewsapp.utils.ValidationManager
import `in`.amankumar110.madenewsapp.viewmodel.auth.EmailVerificationViewModel
import `in`.amankumar110.madenewsapp.viewmodel.auth.SignInViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private val emailVerificationViewModel: EmailVerificationViewModel by hiltNavGraphViewModels(R.id.nav_graph)
    private val signInViewModel: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = view.findNavController()

        setupInputValidation()
        setupClickListeners()
        observeSignInFlows()
        observeEmailVerificationFlows()

        // In SignInFragment and SignUpFragment onCreate()
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finishAffinity()
        }
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupInputValidation() {
        addEmailInputValidation()
        addPasswordInputValidation()
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            navController.navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        binding.btnSignIn.setOnClickListener {
            performSignIn()
        }
    }

    private fun addEmailInputValidation() {
        binding.iconEditTextViewEmail.onDebouncedTextChanged = { email ->
            try {
                ValidationManager.validateEmail(email)
                // IconEditTextView handles withdrawing error internally on text change
            } catch (e: ValidationException.InvalidEmail) {
                binding.iconEditTextViewEmail.setError(e.message ?: "Invalid Email")
            }
        }
    }

    private fun addPasswordInputValidation() {
        binding.iconEditTextViewPassword.onDebouncedTextChanged = { password ->
            try {
                ValidationManager.validatePassword(password)
            } catch (e: ValidationException.InvalidPassword) {
                binding.iconEditTextViewPassword.setError(e.message ?: "Invalid Password")
            }
        }
    }

    private fun performSignIn() {
        try {
            // Validate both fields before attempting sign in
            val emailValid = try {
                ValidationManager.validateEmail(binding.iconEditTextViewEmail.getText())
                true
            } catch (e: ValidationException.InvalidEmail) {
                binding.iconEditTextViewEmail.setError(e.message ?: "Invalid Email")
                false
            }

            val passwordValid = try {
                ValidationManager.validatePassword(binding.iconEditTextViewPassword.getText())
                true
            } catch (e: ValidationException.InvalidPassword) {
                binding.iconEditTextViewPassword.setError(e.message ?: "Invalid Password")
                false
            }

            if (emailValid && passwordValid) {
                signIn()
            } else {
                showToast("Please fill all the fields correctly")
            }
        } catch (e: Exception) {
            Log.e("SignInFragment", "Unexpected error during signin", e)
            showToast("An unexpected error occurred. Please check your inputs.")
        }
    }

    private fun signIn() {
        val email = binding.iconEditTextViewEmail.getText().trim()
        val password = binding.iconEditTextViewPassword.getText().trim()
        signInViewModel.signIn(email, password)
    }

    private fun observeSignInFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    signInViewModel.events.collect { event ->
                        Log.v("TestCase123 : SignInEvent", event::class.java.toString())

                        when (event) {
                            is SignInViewModel.SignupEvents.Success -> emailVerificationViewModel.verifyEmail()
                            is SignInViewModel.SignupEvents.Error -> handleSignInError(event.message)
                        }
                    }
                }

                launch {
                    signInViewModel.isLoading.collect { isLoading ->
                        handleLoadingState(isLoading)
                    }
                }
            }
        }
    }

    private fun observeEmailVerificationFlows() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    emailVerificationViewModel.emailVerificationEvent.collect { event ->

                        when (event) {

                            EmailVerificationViewModel.EmailVerificationEvent.NotVerified -> {
                                showEmailVerificationDialog()
                            }

                            EmailVerificationViewModel.EmailVerificationEvent.Verified -> {

                                EmailVerificationDialogFragment.hide(childFragmentManager)
                                findNavController().navigate(R.id.action_signInFragment_to_mainFragment)
                            }

                            else -> Unit
                        }

                    }
                }

                launch {
                    emailVerificationViewModel.isLoading.collect { isLoading ->
                        handleLoadingState(isLoading)
                    }
                }
            }
        }

    }

    private fun handleLoadingState(isLoading: Boolean) {
        if (isLoading && !LoadingFragment.isShowing(childFragmentManager)) {
            LoadingFragment.show(childFragmentManager)
        } else if (!isLoading && LoadingFragment.isShowing(childFragmentManager)) {
            LoadingFragment.hide(childFragmentManager)
        }
    }

    private fun handleSignInError(message: String) {
        Log.e("TestCase123 : SignInError", message)
        showToast(message)
    }


    private fun showEmailVerificationDialog() {
        // Use singleton pattern to prevent multiple dialogs
        if (!EmailVerificationDialogFragment.isShowing(childFragmentManager)) {
            EmailVerificationDialogFragment.show(childFragmentManager)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}