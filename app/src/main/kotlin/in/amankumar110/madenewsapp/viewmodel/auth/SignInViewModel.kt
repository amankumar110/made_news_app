package `in`.amankumar110.madenewsapp.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.amankumar110.madenewsapp.domain.usecase.auth.SignInUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase : SignInUseCase,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _events = MutableSharedFlow<SignupEvents>()
    val events = _events.asSharedFlow()

     fun signIn(email: String, password: String) {

        viewModelScope.launch {
            _isLoading.emit(true)

            // Simulate network call
            val result = signInUseCase.execute(email, password)
            _isLoading.emit(false)
            result.onSuccess {
                _events.emit(SignupEvents.Success)
            }
            result.onFailure { exception ->
                _events.emit(SignupEvents.Error(exception.message!!))
            }
        }

    }

    sealed class SignupEvents {
        object Success : SignupEvents()
        data class Error(val message : String) : SignupEvents()
    }

}