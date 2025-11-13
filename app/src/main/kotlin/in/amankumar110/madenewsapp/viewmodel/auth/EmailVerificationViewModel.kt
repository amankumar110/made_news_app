package `in`.amankumar110.madenewsapp.viewmodel.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.amankumar110.madenewsapp.domain.models.auth.AuthException
import `in`.amankumar110.madenewsapp.domain.usecase.auth.SendVerificationLinkUseCase
import `in`.amankumar110.madenewsapp.domain.usecase.auth.VerifyEmailUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val verifyEmailUseCase: VerifyEmailUseCase,
    private val sendVerificationLinkUseCase: SendVerificationLinkUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _emailVerificationEvent = MutableSharedFlow<EmailVerificationEvent>(replay = 1)
    val emailVerificationEvent = _emailVerificationEvent.asSharedFlow()

    fun verifyEmail() {
        viewModelScope.launch {
            _isLoading.emit(true)
            try {

                val result = verifyEmailUseCase()
                result.onSuccess { isVerified ->

                    Log.d("EmailVerificationViewModel", "Verification result: $isVerified")

                    if (isVerified) {
                        _emailVerificationEvent.emit(EmailVerificationEvent.Verified)
                    } else {
                        _emailVerificationEvent.emit(EmailVerificationEvent.NotVerified)
                    }
                }
                result.onFailure { exception ->

                    if (exception is AuthException.UserNotLoggedIn) {
                        Log.e(
                            "EmailVerificationViewModel",
                            "Error during verification: USerNotLoggedIn"
                        )
                        _emailVerificationEvent.emit(EmailVerificationEvent.UserNotLoggedIn)
                    } else {
                        _emailVerificationEvent.emit(
                            EmailVerificationEvent.Error(
                                exception.message ?: "Unknown Verification Error"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _emailVerificationEvent.emit(
                    EmailVerificationEvent.Error(
                        e.message ?: "Unexpected error during verification"
                    )
                )
            } finally {
                _isLoading.emit(false)
            }
        }
    }

    fun sendVerificationLink() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = sendVerificationLinkUseCase()
                result.onSuccess {
                    _emailVerificationEvent.emit(EmailVerificationEvent.LinkSent)
                }
                result.onFailure { exception ->

                    _emailVerificationEvent.emit(
                        EmailVerificationEvent.Error(
                            exception.message ?: "Unknown Error Sending Link"
                        )
                    )
                }
            } catch (e: Exception) {
                _emailVerificationEvent.emit(
                    EmailVerificationEvent.Error(
                        e.message ?: "Unexpected error sending link"
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    // resetUiState is removed as it's usually not needed with event-driven shared flow.
    // fun resetUiState() {
    // _emailVerificationUiState.value = EmailVerificationUiState.Idle
    // }

    sealed class EmailVerificationEvent {
        object LinkSent : EmailVerificationEvent()
        object UserNotLoggedIn : EmailVerificationEvent()
        object Verified : EmailVerificationEvent()
        object NotVerified : EmailVerificationEvent()
        data class Error(val message: String) : EmailVerificationEvent()
    }
}
