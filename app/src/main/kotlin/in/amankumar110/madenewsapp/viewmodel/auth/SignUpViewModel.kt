package `in`.amankumar110.madenewsapp.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.amankumar110.madenewsapp.domain.usecase.auth.SignUpUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase : SignUpUseCase,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _signUpEvent = MutableSharedFlow<SignUpEvent>(replay = 0, extraBufferCapacity = 1) // Similar to NewsViewModel
    val signUpEvent = _signUpEvent.asSharedFlow()

    fun signUp(username: String,email: String, password: String,age : Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = signUpUseCase.execute(username,email, password, age)
                result.onSuccess {
                    _signUpEvent.emit(SignUpEvent.SignUpSuccess)
                }
                result.onFailure { exception ->
                    _signUpEvent.emit(SignUpEvent.ShowError(exception.message ?: "Unknown Error Occurred"))
                }
            } catch (e: Exception) {
                // Catch any other unexpected errors during the process
                _signUpEvent.emit(SignUpEvent.ShowError(e.message ?: "An unexpected error occurred"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    sealed class SignUpEvent {
        object SignUpSuccess : SignUpEvent()
        data class ShowError(val message: String) : SignUpEvent()
        // Removed Idle and Loading as they are handled by isLoading StateFlow and absence of events
    }
}