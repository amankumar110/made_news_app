package `in`.amankumar110.madenewsapp.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.amankumar110.madenewsapp.domain.usecase.user.GetUserByUserNameUseCase
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import `in`.amankumar110.madenewsapp.domain.models.auth.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserByUserNameUseCase: GetUserByUserNameUseCase
) : ViewModel() {


    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading = _isLoading.asStateFlow()

    private val _userEvents = MutableSharedFlow<UserEvents?>(replay = 0)
    val userEvents = _userEvents.asSharedFlow()

    fun getUserByUserName(username: String) {
        viewModelScope.launch {

            try {
                _isLoading.value = true


                getUserByUserNameUseCase(username)
                    .onSuccess { user ->
                        _isLoading.value = false
                        _userEvents.emit(UserEvents.Success(user))
                    }
                    .onFailure { throwable ->
                        _isLoading.value = false
                        _userEvents.emit(UserEvents.Error(throwable.message ?: "Unknown error"))
                    }

            } catch (e: Exception) {
                _isLoading.value = false
                _userEvents.emit(UserEvents.Error(e.message ?: "Unknown error"))
            }
        }

    }


    sealed class UserEvents {
        data class Success(val user: User) : UserEvents()
        data class Error(val message: String) : UserEvents()
    }

}