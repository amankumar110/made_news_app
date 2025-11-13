package `in`.amankumar110.madenewsapp.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import `in`.amankumar110.madenewsapp.R
import `in`.amankumar110.madenewsapp.databinding.FragmentSignUpBinding
import `in`.amankumar110.madenewsapp.domain.models.validation.ValidationException
import `in`.amankumar110.madenewsapp.ui.utils.LoadingFragment
import `in`.amankumar110.madenewsapp.utils.SharedPrefs
import `in`.amankumar110.madenewsapp.utils.SharedPrefs.KEY_LAST_VERIFICATION_LINK_SENT_MILISEC
import `in`.amankumar110.madenewsapp.utils.ValidationManager
import `in`.amankumar110.madenewsapp.viewmodel.auth.EmailVerificationViewModel
import `in`.amankumar110.madenewsapp.viewmodel.auth.SignUpViewModel
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private val signUpViewModel: SignUpViewModel by hiltNavGraphViewModels(R.id.nav_graph)
    private val emailVerificationViewModel: EmailVerificationViewModel by hiltNavGraphViewModels(R.id.nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = view.findNavController()

        setupInputValidation()
        observeEmailVerificationFlows()
        observeSignUpFlows()
        setupClickListeners()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finishAffinity()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener {
            navController.navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        binding.btnSignUp.setOnClickListener {
            performSignUp()
        }
    }

    private fun setupInputValidation() {
        addEmailInputValidation()
        addUsernameInputValidation()
        addPasswordInputValidation()
        addAgeInputValidation()
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

    private fun addUsernameInputValidation() {
        binding.iconEditTextViewUserName.onDebouncedTextChanged = { username ->
            try {
                ValidationManager.validateUsername(username)
            } catch (e: ValidationException.InvalidUsername) {
                binding.iconEditTextViewUserName.setError(e.message ?: "Invalid Username")
            }
        }
    }

    private fun addAgeInputValidation() {
        binding.iconEditTextViewAge.onDebouncedTextChanged = { age ->
            try {
                ValidationManager.validateAge(age)
            } catch (e: ValidationException.InvalidAge) {
                binding.iconEditTextViewAge.setError(e.message ?: "Invalid Age")
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

    private fun performSignUp() {
        try {
            // Validate all fields before attempting signup
            val validationResults = validateAllFields()

            if (validationResults.all { it }) {
                signup()
            } else {
                showToast("Please fill all the fields correctly")
            }
        } catch (e: Exception) {
            Log.e("SignUpFragment", "Unexpected error during signup", e)
            showToast("An unexpected error occurred. Please check your inputs.")
        }
    }

    private fun validateAllFields(): List<Boolean> {
        val emailValid = try {
            ValidationManager.validateEmail(binding.iconEditTextViewEmail.getText())
            true
        } catch (e: ValidationException.InvalidEmail) {
            binding.iconEditTextViewEmail.setError(e.message ?: "Invalid Email")
            false
        }

        val usernameValid = try {
            ValidationManager.validateUsername(binding.iconEditTextViewUserName.getText())
            true
        } catch (e: ValidationException.InvalidUsername) {
            binding.iconEditTextViewUserName.setError(e.message ?: "Invalid Username")
            false
        }

        val passwordValid = try {
            ValidationManager.validatePassword(binding.iconEditTextViewPassword.getText())
            true
        } catch (e: ValidationException.InvalidPassword) {
            binding.iconEditTextViewPassword.setError(e.message ?: "Invalid Password")
            false
        }

        val ageValid = try {
            ValidationManager.validateAge(binding.iconEditTextViewAge.getText())
            true
        } catch (e: ValidationException.InvalidAge) {
            binding.iconEditTextViewAge.setError(e.message ?: "Invalid Age")
            false
        }

        return listOf(emailValid, usernameValid, passwordValid, ageValid)
    }

    private fun signup() {
        val email = binding.iconEditTextViewEmail.getText().trim()
        val username = binding.iconEditTextViewUserName.getText().trim()
        val password = binding.iconEditTextViewPassword.getText().trim()
        val age = binding.iconEditTextViewAge.getText().trim()

        signUpViewModel.signUp(username, email, password, age.toInt())
    }

    private fun observeSignUpFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    signUpViewModel.signUpEvent.collect { event ->
                        Log.v("TestCase123 : SignUpEvent", event::class.java.toString())

                        when (event) {
                            is SignUpViewModel.SignUpEvent.ShowError -> {
                                Log.e("TestCase123 : SignUpError", event.message)
                                showToast(event.message)
                            }

                            SignUpViewModel.SignUpEvent.SignUpSuccess -> emailVerificationViewModel.verifyEmail()
                        }
                    }
                }

                launch {
                    signUpViewModel.isLoading.collect { isLoading ->
                        handleLoadingState(isLoading)
                    }
                }
            }
        }
    }

    private fun observeEmailVerificationFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    emailVerificationViewModel.emailVerificationEvent.collect { event ->

                        when (event) {

                            EmailVerificationViewModel.EmailVerificationEvent.NotVerified -> {
                                showEmailVerificationDialog()
                            }

                            EmailVerificationViewModel.EmailVerificationEvent.Verified -> {
                                EmailVerificationDialogFragment.hide(childFragmentManager)
                                findNavController().navigate(R.id.action_signUpFragment_to_mainFragment)
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
        if (isLoading && !LoadingFragment.isShowing(parentFragmentManager)) {
            LoadingFragment.show(parentFragmentManager)
        } else if (!isLoading && LoadingFragment.isShowing(parentFragmentManager)) {
            LoadingFragment.hide(parentFragmentManager)
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showEmailVerificationDialog() {
        // Use singleton pattern to prevent multiple dialogs
        if (!EmailVerificationDialogFragment.isShowing(childFragmentManager)) {
            EmailVerificationDialogFragment.show(childFragmentManager)
        }
    }

}